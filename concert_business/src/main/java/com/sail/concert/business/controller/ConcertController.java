package com.sail.concert.business.controller;

import com.sail.concert.business.bean.Concert;
import com.sail.concert.business.service.ConcertService;
import com.sail.train.common.resp.CommonResp;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/concert")
public class ConcertController {
    @Resource
    private ConcertService stationService;

    @GetMapping("/query-list")
    public CommonResp<List<Concert>> queryList() {
        List<Concert> list = stationService.queryAll();
        return new CommonResp<>(list);
    }
}
