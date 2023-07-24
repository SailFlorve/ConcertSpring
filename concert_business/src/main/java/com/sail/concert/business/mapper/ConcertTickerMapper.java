package com.sail.concert.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sail.concert.business.bean.ConcertTicket;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConcertTickerMapper extends BaseMapper<ConcertTicket> {
}
