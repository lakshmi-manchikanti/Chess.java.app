package com.example;

public class Rook extends Piece {
    public Rook(String color) {
        super(color);
    }

    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY, Piece[][] board) {
        return startX == endX || startY == endY;
    }

    @Override
    public String toString() {
        return color.equals("white") ? "R" : "r";
    }
}