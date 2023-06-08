package com.jiawa.concert.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jiawa.concert.business.bean.Concert;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ConcertMapper extends BaseMapper<Concert> {
}
