package com.anhnd.resultservice.model;

import java.time.LocalDateTime;

public class Result {
    private int id;
    private Member memberA;
    private Member memberB;
    private Bot bot;
    private int resAtoB;
    private int resAToBot;
    private LocalDateTime createdAt;

    public Result() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Member getMemberA() {
        return memberA;
    }

    public void setMemberA(Member memberA) {
        this.memberA = memberA;
    }

    public Member getMemberB() {
        return memberB;
    }

    public void setMemberB(Member memberB) {
        this.memberB = memberB;
    }

    public Bot getBot() {
        return bot;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    public int getResAtoB() {
        return resAtoB;
    }

    public void setResAtoB(int resAtoB) {
        this.resAtoB = resAtoB;
    }

    public int getResAToBot() {
        return resAToBot;
    }

    public void setResAToBot(int resAToBot) {
        this.resAToBot = resAToBot;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

}
