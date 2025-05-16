package com.anhnd.client.model;

import java.util.*;

public class Board {

    private static int[][] board;
    private String algorithm;
    private int color;
    private int[] move;

    private static final int[][] directions = {
            {0, 1}, {1, 1}, {1, 0}, {1, -1},
            {0, -1}, {-1, -1}, {-1, 0}, {-1, 1}
    };

    public static List<List<int[]>> findLines(int row, int col, int player) {
        List<List<int[]>> lines = new ArrayList<>();
        for(int[] dir : directions) {
            int xdir = dir[0];
            int ydir = dir[1];
            int u = row;
            int v = col;
            u += xdir;
            v += ydir;

            boolean found = false;
            List<int[]> line = new ArrayList<>();

            while (u >= 0 && u < board.length && v >= 0 && v < board.length) {
                if (board[u][v] == player) {
                    found = true;
                    break;
                } else if (board[u][v] == 0) {
                    break;
                } else {
                    line.add(new int[]{u, v});
                }
                u += xdir;
                v += ydir;
            }
            if (found && !line.isEmpty()) {
                lines.add(line);
            }
        }
        return lines;
    }

    public static List<int[]> getPossibleMoves(Board board) {
        List<int[]> listMove = new ArrayList<>();

        for(int i=0; i<board.getBoard().length; i++) {
            for(int j=0; j<board.getBoard().length; j++) {
                if(board.getBoard()[i][j] == 0) {
                    List<List<int[]>> moves = findLines(i, j, board.getColor());
                    if(moves.size() > 0) {
                        listMove.add(new int[]{i, j});
                    }
                }
            }
        }

        return listMove;
    }

    public static int[][] makeMove(Board board, int player, int i, int j) {
        int[][] newBoard = new int[board.getBoard().length][board.getBoard().length];
        for (int row = 0; row < board.getBoard().length; row++) {
            for (int col = 0; col < board.getBoard().length; col++) {
                newBoard[row][col] = board.getBoard()[row][col];
            }
        }
        List<List<int[]>> lines = findLines(i, j, player);
        newBoard[j][i] = player;

        for (List<int[]> line : lines) {
            for (int[] pos : line) {
                int u = pos[0];
                int v = pos[1];
                newBoard[v][u] = player;
            }
        }
        return newBoard;
    }

    public static int[] getScore(Board board) {
        int p1 = 0;
        int p2 = 0;
        for(int i=0; i<board.getBoard().length; i++) {
            for(int j=0; j<board.getBoard().length; j++) {
                if(board.getBoard()[i][j] == 1) {
                    p1++;
                } else if(board.getBoard()[i][j] == 2) {
                    p2++;
                }
            }
        }
        return new int[]{p1, p2};
    }

    public int[][] getBoard() {
        return board;
    }

    public void setBoard(int[][] board) {
        this.board = board;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int[] getMove() {
        return move;
    }

    public void setMove(int[] move) {
        this.move = move;
    }

    //    public static void main(String[] args) {
//        int[][] board = {
//                {0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 1, 2, 0, 0, 0},
//                {0, 0, 0, 2, 2, 1, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0},
//                {0, 0, 0, 0, 0, 0, 0, 0}
//        };
//        List<List<int[]>> lines = findLines(board, 4, 2, 1);
//        for(List<int[]> line : lines) {
//            for(int[] pos : line) {
//                System.out.println(Arrays.toString(pos));
//            }
//        }
//    }
}