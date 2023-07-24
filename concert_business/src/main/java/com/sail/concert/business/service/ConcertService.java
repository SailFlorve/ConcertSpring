package com.sail.concert.business.service;

import com.sail.concert.business.bean.Concert;
import com.sail.concert.business.mapper.ConcertMapper;
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
