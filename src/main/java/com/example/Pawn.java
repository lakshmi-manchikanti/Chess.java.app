package com.example;

public class Pawn extends Piece {
    public Pawn(String color) {
        super(color);
    }

    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY, Piece[][] board) {
        int direction = color.equals("white") ? -1 : 1;
        if (startX + direction == endX && startY == endY && board[endX][endY] == null) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return color.equals("white") ? "P" : "p";
    }
}