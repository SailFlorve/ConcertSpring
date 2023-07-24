package com.sail.concert.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sail.concert.business.bean.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {
}
