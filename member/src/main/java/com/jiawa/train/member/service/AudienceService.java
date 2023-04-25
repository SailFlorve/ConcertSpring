package com.jiawa.train.member.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.jiawa.train.common.context.LoginMemberContext;
import com.jiawa.train.common.resp.PageResp;
import com.jiawa.train.common.util.SnowUtil;
import com.jiawa.train.member.domain.Audience;
import com.jiawa.train.member.domain.Member;
import com.jiawa.train.member.mapper.AudienceMapper;
import com.jiawa.train.member.mapper.MemberMapper;
import com.jiawa.train.member.req.AudienceQueryReq;
import com.jiawa.train.member.req.AudienceSaveReq;
import com.jiawa.train.member.resp.AudienceQueryResp;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class AudienceService {

    private static final Logger LOG = LoggerFactory.getLogger(AudienceService.class);

    @Resource
    private AudienceMapper audienceMapper;

    @Resource
    private MemberMapper memberMapper;

    public void save(AudienceSaveReq req) {
        DateTime now = DateTime.now();
        Audience audience = BeanUtil.copyProperties(req, Audience.class);
        if (ObjectUtil.isNull(audience.getId())) {
            audience.setMemberId(LoginMemberContext.getId());
            audience.setId(SnowUtil.getSnowflakeNextId());
            audience.setCreateTime(now);
            audience.setUpdateTime(now);
            audienceMapper.insert(audience);
        } else {
            audience.setUpdateTime(now);
            audienceMapper.updateById(audience);
        }
    }

    public PageResp<AudienceQueryResp> queryList(AudienceQueryReq req) {
        // 创建查询条件
        QueryWrapper<Audience> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");

        if (ObjectUtil.isNotNull(req.getMemberId())) {
            queryWrapper.eq("member_id", req.getMemberId());
        }

        LOG.info("查询页码：{}", req.getPage());
        LOG.info("每页条数：{}", req.getSize());
        PageHelper.startPage(req.getPage(), req.getSize());
        List<Audience> audienceList = audienceMapper.selectList(queryWrapper);

        PageInfo<Audience> pageInfo = new PageInfo<>(audienceList);
        LOG.info("总行数：{}", pageInfo.getTotal());
        LOG.info("总页数：{}", pageInfo.getPages());

        List<AudienceQueryResp> list = BeanUtil.copyToList(audienceList, AudienceQueryResp.class);

        PageResp<AudienceQueryResp> pageResp = new PageResp<>();
        pageResp.setTotal(pageInfo.getTotal());
        pageResp.setList(list);
        return pageResp;
    }

    public void delete(Long id) {
        audienceMapper.deleteById(id);
    }

    /**
     * 查询我的所有乘客
     */
    public List<AudienceQueryResp> queryMine() {
        // 创建查询条件
        QueryWrapper<Audience> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("member_id", LoginMemberContext.getId());
        queryWrapper.orderByAsc("name");

        // 查询数据
        List<Audience> list = audienceMapper.selectList(queryWrapper);

        return BeanUtil.copyToList(list, AudienceQueryResp.class);
    }

    public void init() {
        DateTime now = DateTime.now();

        // 创建查询条件
        QueryWrapper<Member> memberQueryWrapper = new QueryWrapper<>();
        memberQueryWrapper.eq("mobile", "13000000000");

        // 查询数据
        List<Member> memberList = memberMapper.selectList(memberQueryWrapper);
        Member member = memberList.get(0);

        List<Audience> passengerList = new ArrayList<>();

        List<String> nameList = Arrays.asList("张三", "李四", "王五");
        for (String s : nameList) {
            Audience passenger = new Audience();
            passenger.setId(SnowUtil.getSnowflakeNextId());
            passenger.setMemberId(member.getId());
            passenger.setName(s);
            passenger.setIdCard("123456789123456789");
            passenger.setCreateTime(now);
            passenger.setUpdateTime(now);
            passengerList.add(passenger);
        }

        for (Audience passenger : passengerList) {
            // 创建查询条件
            QueryWrapper<Audience> passengerQueryWrapper = new QueryWrapper<>();
            passengerQueryWrapper.eq("name", passenger.getName());

            // 查询数据
            int count = Math.toIntExact(audienceMapper.selectCount(passengerQueryWrapper));

            if (count == 0) {
                audienceMapper.insert(passenger);
            }
        }

    }
}
