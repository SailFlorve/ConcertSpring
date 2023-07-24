package com.sail.concert.business.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sail.concert.business.bean.ConfirmOrderDoReq;
import com.sail.concert.business.bean.Order;
import com.sail.concert.business.mapper.OrderMapper;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RocketMQMessageListener(topic = "${rocketmq.topic}", consumerGroup = "${rocketmq.consumer.group}")
public class OrderConsumerService {

    @Autowired
    private BeforeConfirmOrderService beforeConfirmOrderService;

    @Autowired
    private OrderMapper orderMapper;

    public void processOrderMessage(String message) {
        ConfirmOrderDoReq confirmOrderDto = JSON.parseObject(message, ConfirmOrderDoReq.class);
        ConfirmOrderDoReq req = new ConfirmOrderDoReq();

        LambdaQueryWrapper<Order> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Order::getConcertId, confirmOrderDto.getConcertId())
                .eq(Order::getMemberId, confirmOrderDto.getMemberId());
        Order order = orderMapper.selectOne(queryWrapper);

        beforeConfirmOrderService.doConfirm(req, order);
    }
}