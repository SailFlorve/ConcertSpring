package com.jiawa.train.member.controller;

import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.resp.CommonResp;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.member.req.AudienceQueryReq;
import com.jiawa.train.member.req.AudienceSaveReq;
import com.jiawa.train.member.resp.AudienceQueryResp;
import com.jiawa.train.member.service.AudienceService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/audience")
public class AudienceController {

    @Resource
    private AudienceService audienceService;

    @PostMapping("/save")
    public CommonResp<Object> save(@Valid @RequestBody AudienceSaveReq req) {
        audienceService.save(req);
        return new CommonResp<>();
    }

    @GetMapping("/query-list")
    public CommonResp<PageResp<AudienceQueryResp>> queryList(@Valid AudienceQueryReq req) {
        req.setMemberId(LoginMemberContext.getId());
        PageResp<AudienceQueryResp> list = audienceService.queryList(req);
        return new CommonResp<>(list);
    }

    @DeleteMapping("/delete/{id}")
    public CommonResp<Object> delete(@PathVariable Long id) {
        audienceService.delete(id);
        return new CommonResp<>();
    }

    @GetMapping("/query-mine")
    public CommonResp<List<AudienceQueryResp>> queryMine() {
        List<AudienceQueryResp> list = audienceService.queryMine();
        return new CommonResp<>(list);
    }

    @GetMapping("/init")
    public CommonResp<Object> init() {
        audienceService.init();
        return new CommonResp<>();
    }

}
