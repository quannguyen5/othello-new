//package com.anhnd.botservice.algorithm;
//
//import com.anhnd.botservice.model.Board;
//
//import java.util.List;
//
//public class Test {
//    public static int minimaxMaxNode(int[][] board, int depth) {
//        if(depth > 3) return computeUtility(board);
//        List<int[]> possibleMoves = Board.getPossibleMoves(board, 1);
//        if(possibleMoves.isEmpty()) return computeUtility(board);
//
//        int v = Integer.MIN_VALUE;
//        for(int[] move : possibleMoves) {
//            int x = move[0];
//            int y = move[1];
//            int[][] newBoard = Board.makeMove(board, 1, x, y);
//            v = Math.max(v, minimaxMinNode(newBoard, depth + 1));
//        }
//        return v;
//    }
//
//    public static int minimaxMinNode(int[][] board, int depth) {
//        if(depth > 3) return computeUtility(board);
//        List<int[]> possibleMoves = Board.getPossibleMoves(board, 2);
//        if(possibleMoves.isEmpty()) return computeUtility(board);
//        int v = Integer.MAX_VALUE;
//        for(int[] move : possibleMoves) {
//            int x = move[0];
//            int y = move[1];
//            int[][] newBoard = Board.makeMove(board, 2, x, y);
//            v = Math.min(v, minimaxMinNode(newBoard, depth + 1));
//        }
//        return v;
//    }
//
//    public static int computeUtility(int[][] board) {
//        int[] scores = Board.getScore(board);
//        return scores[0] - scores[1];
//    }
//
//    public static int[] selectMinimaxMove(int[][] board, int color) {
//        List<int[]> possibleMoves = Board.getPossibleMoves(board, color);
//        if(possibleMoves.isEmpty()) {
//            return null;
//        }
//        int[] utilities = new int[possibleMoves.size()];
//        if(color == 1) {
//            for(int i=0; i<possibleMoves.size(); i++) {
//                int x = possibleMoves.get(i)[0];
//                int y = possibleMoves.get(i)[1];
//                int[][] newBoard = Board.makeMove(board, 1, x, y);
//                utilities[i] = minimaxMinNode(newBoard, 1);
//            }
//            int maxIndex = 0;
//            for(int i=1; i<possibleMoves.size(); i++) {
//                if(utilities[i] > utilities[maxIndex]) {
//                    maxIndex = i;
//                }
//            }
//            return possibleMoves.get(maxIndex);
//        } else {
//            for(int i=0; i<possibleMoves.size(); i++) {
//                int x = possibleMoves.get(i)[0];
//                int y = possibleMoves.get(i)[1];
//                int[][] newBoard = Board.makeMove(board, 2, x, y);
//                utilities[i] = minimaxMaxNode(newBoard, 1);
//            }
//            int minIndex = 0;
//            for(int i=1; i<possibleMoves.size(); i++) {
//                if(utilities[i] < utilities[minIndex]) {
//                    minIndex = i;
//                }
//            }
//            return possibleMoves.get(minIndex);
//        }
//    }
//}
