package com.anhnd.matchservice.model;

public class Bot {
    private int id;
    private String algorithm;

    public Bot() {
    }

    public Bot(int id, String algorithm) {
        this.id = id;
        this.algorithm = algorithm;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

}
