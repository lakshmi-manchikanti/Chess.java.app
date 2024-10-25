package com.example;

public class Knight extends Piece {
    public Knight(String color) {
        super(color);
    }

    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY, Piece[][] board) {
        return (Math.abs(startX - endX) == 2 && Math.abs(startY - endY) == 1) ||
               (Math.abs(startX - endX) == 1 && Math.abs(startY - endY) == 2);
    }

    @Override
    public String toString() {
        return color.equals("white") ? "N" : "n";
    }
}