package com.jiawa.concert.business.controller;

import com.jiawa.concert.business.bean.Concert;
import com.jiawa.concert.business.service.ConcertService;
import com.jiawa.train.common.resp.CommonResp;
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
