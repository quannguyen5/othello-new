package com.anhnd.botservice.model;


import com.anhnd.botservice.algorithm.MinimaxAlgorithm;
import com.anhnd.botservice.algorithm.RandomAlgorithm;

import java.util.List;
import java.util.Random;

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

    public Board makeMove(Board board) {
        switch (algorithm.toLowerCase()) {
            case "minimax":
                return MinimaxAlgorithm.selectMoveMinimax(board);
            case "random":
                return RandomAlgorithm.getRandomMove(board);
            default:
                throw new IllegalArgumentException("Thuật toán không được hỗ trợ: " + algorithm);
        }
    }
}
