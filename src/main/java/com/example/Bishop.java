package com.example;


public class Bishop extends Piece {
    public Bishop(String color) {
        super(color);
    }

    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY, Piece[][] board) {
        if (Math.abs(startX - endX) == Math.abs(startY - endY)) { // Diagonal move
            int xDirection = (endX - startX) > 0 ? 1 : -1;
            int yDirection = (endY - startY) > 0 ? 1 : -1;
            int x = startX + xDirection;
            int y = startY + yDirection;
            while (x != endX && y != endY) {
                if (board[x][y] != null) return false; // Path blocked
                x += xDirection;
                y += yDirection;
            }
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return color.equals("white") ? "B" : "b";
    }
}