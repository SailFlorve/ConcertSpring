package com.sail.concert.business.bean;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class ConfirmOrderDoReq {

    private Long memberId;

    private Long concertId;

    @NotNull(message = "【购票列表】不能为空")
    private List<ConcertTicket> ticketList;

    @NotNull(message = "【剩余门票】不能为空")
    private int ticketLeft;

    @NotBlank(message = "【图片验证码】不能为空")
    private String imageCode;

    @NotBlank(message = "【图片验证码】参数非法")
    private String imageCodeToken;

    private String logId;

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getConcertId() {
        return concertId;
    }

    public void setConcertId(Long concertId) {
        this.concertId = concertId;
    }

    public List<ConcertTicket> getTicketList() {
        return ticketList;
    }

    public void setTicketList(List<ConcertTicket> ticketList) {
        this.ticketList = ticketList;
    }

    public int getTicketLeft() {
        return ticketLeft;
    }

    public void setTicketLeft(int ticketLeft) {
        this.ticketLeft = ticketLeft;
    }

    public String getImageCode() {
        return imageCode;
    }

    public void setImageCode(String imageCode) {
        this.imageCode = imageCode;
    }

    public String getImageCodeToken() {
        return imageCodeToken;
    }

    public void setImageCodeToken(String imageCodeToken) {
        this.imageCodeToken = imageCodeToken;
    }

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }
}
