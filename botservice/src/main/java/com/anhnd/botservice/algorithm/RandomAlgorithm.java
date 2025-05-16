package com.anhnd.botservice.algorithm;

import com.anhnd.botservice.model.Board;

import java.util.List;
import java.util.Random;

public class RandomAlgorithm {

    public static Board getRandomMove(Board board) {
        List<int[]> listPossibleMoves = Board.getPossibleMoves(board);
        if (listPossibleMoves.isEmpty()) {
            return null;
        }

        Random random = new Random();
        int randomIndex = random.nextInt(listPossibleMoves.size());

        Board resBoard = new Board();
        resBoard.setMove(listPossibleMoves.get(randomIndex));
        return resBoard;
    }
}
