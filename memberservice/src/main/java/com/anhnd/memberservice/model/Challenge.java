package com.anhnd.memberservice.model;

import java.util.Date;

public class Challenge {
    private int id;
    private Member challenger;
    private Member challenged;
    private Date created_at;
    private Date expires_at;
    private String status;

    public Challenge() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Member getChallenger() {
        return challenger;
    }

    public void setChallenger(Member challenger) {
        this.challenger = challenger;
    }

    public Member getChallenged() {
        return challenged;
    }

    public void setChallenged(Member challenged) {
        this.challenged = challenged;
    }

    public Date getCreated_at() {
        return created_at;
    }

    public void setCreated_at(Date created_at) {
        this.created_at = created_at;
    }

    public Date getExpires_at() {
        return expires_at;
    }

    public void setExpires_at(Date expires_at) {
        this.expires_at = expires_at;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
