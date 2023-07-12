package com.jiawa.concert.business.service;

import com.jiawa.concert.business.bean.Concert;
import com.jiawa.concert.business.mapper.ConcertMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConcertService {
    @Resource
    private ConcertMapper concertMapper;

    public List<Concert> queryAll() {
        return concertMapper.selectList(null);
    }
}
