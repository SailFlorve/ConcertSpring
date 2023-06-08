package com.jiawa.concert.business.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.jiawa.concert.business.bean.Concert;
import com.jiawa.concert.business.bean.ConcertTicket;
import com.jiawa.concert.business.bean.ConfirmOrderDoReq;
import com.jiawa.concert.business.bean.Order;
import com.jiawa.concert.business.enums.ConfirmOrderStatusEnum;
import com.jiawa.concert.business.mapper.ConcertMapper;
import com.jiawa.concert.business.mapper.ConcertTickerMapper;
import com.jiawa.concert.business.mapper.OrderMapper;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.exception.BusinessException;
import com.jiawa.train.common.exception.BusinessExceptionEnum;
import com.jiawa.train.common.util.SnowUtil;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
    // public RocketMQTemplate rocketMQTemplate;

    @SentinelResource(value = "beforeDoConfirm", blockHandler = "beforeDoConfirmBlock")
    public Long beforeDoConfirm(ConfirmOrderDoReq req) {
        Long id = null;
        // 校验令牌余量
        boolean validSkToken = redisTemplate.opsForValue().get("sk-token-" + req.getConcertId()) != null;

        if (validSkToken) {
            LOG.info("令牌校验通过");
        } else {
            LOG.info("令牌校验不通过");
//            throw new BusinessException(BusinessExceptionEnum.CONFIRM_ORDER_SK_TOKEN_FAIL);
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
//        ConfirmOrderMQDto confirmOrderMQDto = new ConfirmOrderMQDto();
//        confirmOrderMQDto.setDate(req.getDate());
//        confirmOrderMQDto.setTrainCode(req.getTrainCode());
//        confirmOrderMQDto.setLogId(MDC.get("LOG_ID"));
//        String reqJson = JSON.toJSONString(confirmOrderMQDto);
        // LOG.info("排队购票，发送mq开始，消息：{}", reqJson);
//        rocketMQTemplate.convertAndSend(RocketMQTopicEnum.CONFIRM_ORDER.getCode(), reqJson);
        // LOG.info("排队购票，发送mq结束");
        doConfirm(req, order);
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

    private void doConfirm(ConfirmOrderDoReq req, Order order) {
        // 获取分布式锁
        String lockKey = "concert-ticket-lock-" + req.getConcertId();
        String lockValue = UUID.randomUUID().toString();
        boolean lockSuccess = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, 10, TimeUnit.SECONDS));

        if (lockSuccess || !lockSuccess) {
            try {
                // * 执行业务逻辑: 扣减票，创建订单项，更新订单等 *
                Concert concert = concertMapper.selectById(req.getConcertId());
                concert.setTicketLeft(concert.getTicketLeft() - req.getTicketList().size());
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
                e.printStackTrace();
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
