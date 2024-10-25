package com.example;

public class King extends Piece {
    public King(String color) {
        super(color);
    }

    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY, Piece[][] board) {
        return (Math.abs(startX - endX) <= 1 && Math.abs(startY - endY) <= 1); // Move one square in any direction
    }

    @Override
    public String toString() {
        return color.equals("white") ? "K" : "k";
    }
}