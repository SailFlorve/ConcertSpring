package com.sail.concert.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sail.concert.business.bean.Concert;
import com.sail.concert.business.bean.ConcertTicket;
import com.sail.concert.business.bean.ConfirmOrderDoReq;
import com.sail.concert.business.bean.Order;
import com.sail.concert.business.enums.ConfirmOrderStatusEnum;
import com.sail.concert.business.mapper.ConcertMapper;
import com.sail.concert.business.mapper.ConcertTickerMapper;
import com.sail.concert.business.mapper.OrderMapper;
import com.sail.train.common.context.LoginMemberContext;
import com.sail.train.common.exception.BusinessException;
import com.sail.train.common.exception.BusinessExceptionEnum;
import com.sail.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class BeforeConfirmOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(BeforeConfirmOrderService.class);

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Resource
    private OrderMapper confirmOrderMapper;

    @Resource
    private ConcertTickerMapper concertTickerMapper;

    @Resource
    private ConcertMapper concertMapper;

    // @Resource
    // public RocketMQTemplate rocket
    @Resource
    public RocketMQTemplate rocketMQTemplate;

    public int getTicketLeftFromRedis(long concertId) {
        String key = "concert-id-" + concertId;
        String value = redisTemplate.opsForValue().get(key);
        return value == null ? 0 : Integer.parseInt(value);
    }

    @Scheduled(fixedRate = 1000)
    public void addTokens() {
        // 获取所有演唱会的ID列表
        List<Long> concertIds = concertMapper.selectList(null)
                .stream()
                .map(Concert::getId)
                .toList();

        // 向每个演唱会的令牌桶中添加令牌
        for (Long concertId : concertIds) {
            String key = "token-bucket-" + concertId;
            int tokenCount = redisTemplate.opsForValue().get(key) == null ? 0 : Integer.parseInt(redisTemplate.opsForValue().get(key));
            int maxTokens = 500; // 桶的容量

            // 如果令牌数量小于桶的容量，则添加一个令牌
            if (tokenCount < maxTokens) {
                redisTemplate.opsForValue().set(key, String.valueOf(tokenCount + 1));
            }
        }
    }

    public boolean tryAcquireToken(long concertId) {
        String key = "token-bucket-" + concertId;
        int tokenCount = redisTemplate.opsForValue().get(key) == null ? 0 : Integer.parseInt(redisTemplate.opsForValue().get(key));

        // 如果没有足够的令牌，返回false
        if (tokenCount <= 0) {
            return false;
        }

        // 如果有足够的令牌，将令牌数量减1并更新到Redis中
        redisTemplate.opsForValue().set(key, String.valueOf(tokenCount - 1));
        return true;
    }

    @SentinelResource(value = "beforeDoConfirm", blockHandler = "beforeDoConfirmBlock")
    @Transactional(rollbackFor = Exception.class)
    public Long beforeDoConfirm(ConfirmOrderDoReq req) {
        Long id = null;

        if (!tryAcquireToken(req.getConcertId())) {
            LOG.info("令牌校验通过");
        } else {
            LOG.info("令牌校验不通过");
//            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_SK_TOKEN_FAIL);
        }

        int ticketCount = req.getTicketList().size();
        int ticketLeft = getTicketLeftFromRedis(req.getConcertId());
        if (ticketLeft < ticketCount) {
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_TICKET_COUNT_ERROR);
        }

        // 保存确认订单表，状态初始
        DateTime now = DateTime.now();
        Order order = new Order();
        order.setId(SnowUtil.getSnowflakeNextId());
        order.setTicketId(0L);
        order.setMemberId(LoginMemberContext.getId());
        order.setConcertId(req.getConcertId());
        order.setOrderStatus(ConfirmOrderStatusEnum.INIT.getCode());
        order.setCreateTime(now);
        order.setUpdateTime(now);
        confirmOrderMapper.insert(order);

        // 发送MQ排队购票
        String reqJson = JSON.toJSONString(req);
        LOG.info("排队购票，发送mq开始，消息：{}", reqJson);
        rocketMQTemplate.convertAndSend("confirm_order", reqJson);
        LOG.info("排队购票，发送mq结束");

        id = order.getId();

        return id;
    }

    /**
     * 降级方法，需包含限流方法的所有参数和BlockException参数
     *
     * @param req
     * @param e
     */
    public void beforeDoConfirmBlock(ConfirmOrderDoReq req, BlockException e) {
        LOG.info("购票请求被限流：{}", req);
        throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_FLOW_EXCEPTION);
    }

    public void doConfirm(ConfirmOrderDoReq req, Order order) {
        // 获取分布式锁
        String lockKey = "concert-ticket-lock-" + req.getConcertId();
        String lockValue = UUID.randomUUID().toString();
        boolean lockSuccess = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, 10, TimeUnit.SECONDS));

        if (lockSuccess || !lockSuccess) {
            try {
                // * 执行业务逻辑: 扣减票，创建订单项，更新订单等 *
                Concert concert = concertMapper.selectById(req.getConcertId());
                int newTicketLeft = concert.getTicketLeft() - req.getTicketList().size();
                redisTemplate.opsForValue().set("concert-id-" + req.getConcertId(), String.valueOf(newTicketLeft));
                concert.setTicketLeft(newTicketLeft);
                concertMapper.updateById(concert);

                // 更新订单状态为成功
                order.setOrderStatus(ConfirmOrderStatusEnum.SUCCESS.getCode());
                UpdateWrapper<Order> uw = new UpdateWrapper<>();
                uw.eq("id", order.getId());

                confirmOrderMapper.update(order, uw);

                for (ConcertTicket concertTicket : req.getTicketList()) {
                    Date now = new Date();
                    ConcertTicket ticket = new ConcertTicket();
                    BeanUtil.copyProperties(concertTicket, ticket);
                    ticket.setId(SnowUtil.getSnowflakeNextId());
                    ticket.setMemberId(LoginMemberContext.getId());
                    ticket.setUpdateTime(now);
                    ticket.setCreateTime(now);
                    ticket.setConcertName(concert.getTitle());
                    concertTickerMapper.insert(ticket);
                }

            } catch (Exception e) {
                LOG.error("处理订单时出现错误：{}", e.getMessage());
                // 更新订单状态为失败
                order.setOrderStatus(ConfirmOrderStatusEnum.FAILURE.getCode());
                UpdateWrapper<Order> uw = new UpdateWrapper<>();
                uw.eq("id", order.getId());
                confirmOrderMapper.update(order, uw);

                throw new RuntimeException("处理订单时出现错误", e);

            } finally {
                // 释放分布式锁
                if (lockKey.equals(redisTemplate.opsForValue().get(lockKey))) {
                    redisTemplate.delete(lockKey);
                }
            }
        } else {
            // 获取锁失败，返回错误信息
            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_LOCK_FAIL);
        }
    }

    public int queryLineCount(long id) {
        String status = confirmOrderMapper.selectById(id).getOrderStatus();
        return switch (status) {
            case "S" -> -1;
            default -> -2;
        };
    }

    public int cancel(long id) {
        UpdateWrapper<Order> uw = new UpdateWrapper<>();
        uw.eq("id", id);
        uw.setSql("order_status = " + ConfirmOrderStatusEnum.CANCEL.getCode());
        return confirmOrderMapper.update(null, uw);
    }
}
