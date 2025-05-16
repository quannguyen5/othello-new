package com.anhnd.memberservice.model;

import java.util.Date;

public class FriendInvitation {
    private int id;
    private Member requestMem;
    private Member receiveMem;
    private String status;
    private Date timeRequest;
    private Date timeUpdate;

    public FriendInvitation() {
    }

    public FriendInvitation(int id, Member requestMem, Member receiveMem, String status, Date timeRequest, Date timeUpdate) {
        this.id = id;
        this.requestMem = requestMem;
        this.receiveMem = receiveMem;
        this.status = status;
        this.timeRequest = timeRequest;
        this.timeUpdate = timeUpdate;
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Member getRequestMem() {
        return requestMem;
    }

    public void setRequestMem(Member requestMem) {
        this.requestMem = requestMem;
    }

    public Member getReceiveMem() {
        return receiveMem;
    }

    public void setReceiveMem(Member receiveMem) {
        this.receiveMem = receiveMem;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getTimeRequest() {
        return timeRequest;
    }

    public void setTimeRequest(Date timeRequest) {
        this.timeRequest = timeRequest;
    }

    public Date getTimeUpdate() {
        return timeUpdate;
    }

    public void setTimeUpdate(Date timeUpdate) {
        this.timeUpdate = timeUpdate;
    }
}