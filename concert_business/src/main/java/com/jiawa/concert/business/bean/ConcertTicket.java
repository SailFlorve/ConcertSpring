package com.jiawa.concert.business.bean;

import com.baomidou.mybatisplus.annotation.TableName;

import java.util.Date;

@TableName("concert_ticket")
public class ConcertTicket {
    private Long id;
    private Long memberId;
    private Long audienceId;
    private String audienceName;
    private Long concertId;
    private String concertName;
    private String seatNumber;
    private Date createTime;
    private Date updateTime;

    @Override
    public String toString() {
        return "ConcertTicket{" +
                "id=" + id +
                ", memberId=" + memberId +
                ", audienceId=" + audienceId +
                ", audienceName='" + audienceName + '\'' +
                ", concertId=" + concertId +
                ", seatNumber='" + seatNumber + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getAudienceId() {
        return audienceId;
    }

    public void setAudienceId(Long audienceId) {
        this.audienceId = audienceId;
    }

    public String getAudienceName() {
        return audienceName;
    }

    public void setAudienceName(String audienceName) {
        this.audienceName = audienceName;
    }

    public Long getConcertId() {
        return concertId;
    }

    public String getConcertName() {
        return concertName;
    }

    public void setConcertName(String concertName) {
        this.concertName = concertName;
    }

    public void setConcertId(Long concertId) {
        this.concertId = concertId;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
