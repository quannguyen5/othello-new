package com.anhnd.client.model;

public class Match {
    private int id;
    private Challenge challenge;
    private int whiteToBlack;

    public Match() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public int getWhiteToBlack() {
        return whiteToBlack;
    }

    public void setWhiteToBlack(int whiteToBlack) {
        this.whiteToBlack = whiteToBlack;
    }
}