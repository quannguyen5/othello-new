package com.anhnd.botservice.algorithm;

import com.anhnd.botservice.model.Board;
import java.util.List;

public class MinimaxAlgorithm {

    private static final int MAX_DEPTH = 3;

    public static Board selectMoveMinimax(Board board) {
        List<int[]> moves = Board.getPossibleMoves(board);
        if (moves.isEmpty()) {
            return null;
        }

        int[] utilities = new int[moves.size()];

        if (board.getColor() == 1) {
            for (int i = 0; i < moves.size(); i++) {
                int x = moves.get(i)[0];
                int y = moves.get(i)[1];
                Board newBoard = new Board();
                newBoard.setBoard(Board.makeMove(board, 1, x, y));
                utilities[i] = minimaxMinNode(newBoard, 1);
            }

            int maxIndex = 0;
            for (int i = 1; i < utilities.length; i++) {
                if (utilities[i] > utilities[maxIndex]) {
                    maxIndex = i;
                }
            }
            Board resBoard = new Board();
            resBoard.setMove(moves.get(maxIndex));
            return resBoard;

        } else {
            for (int i = 0; i < moves.size(); i++) {
                int x = moves.get(i)[0];
                int y = moves.get(i)[1];
                Board newBoard = new Board();
                newBoard.setBoard(Board.makeMove(board, 2, x, y));
                utilities[i] = minimaxMaxNode(newBoard, 1);
            }

            int minIndex = 0;
            for (int i = 1; i < utilities.length; i++) {
                if (utilities[i] < utilities[minIndex]) {
                    minIndex = i;
                }
            }
            Board resBoard = new Board();
            resBoard.setMove(moves.get(minIndex));
            return resBoard;
        }
    }

    public static int minimaxMaxNode(Board board, int depth) {
        if (depth >= MAX_DEPTH) {
            return computeUtility(board);
        }

        List<int[]> moves = Board.getPossibleMoves(board);

        if (moves.isEmpty()) {
            return computeUtility(board);
        }

        int v = Integer.MIN_VALUE;
        for (int[] move : moves) {
            int x = move[0];
            int y = move[1];
            Board newBoard = new Board();
            newBoard.setBoard(Board.makeMove(board, 1, x, y));
            v = Math.max(v, minimaxMinNode(newBoard, depth + 1));
        }
        return v;
    }

    public static int minimaxMinNode(Board board, int depth) {
        if (depth >= MAX_DEPTH) {
            return computeUtility(board);
        }

        List<int[]> moves = Board.getPossibleMoves(board);

        if (moves.isEmpty()) {
            return computeUtility(board);
        }

        int v = Integer.MAX_VALUE;
        for (int[] move : moves) {
            int x = move[0];
            int y = move[1];
            Board newBoard = new Board();
            newBoard.setBoard(Board.makeMove(board, 2, x, y));
            v = Math.min(v, minimaxMaxNode(newBoard, depth + 1));
        }
        return v;
    }

    public static int computeUtility(Board board) {
        int[] scores = Board.getScore(board);
        return scores[0] - scores[1];
    }
}