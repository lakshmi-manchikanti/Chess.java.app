package com.example;

public class Queen extends Piece {
    public Queen(String color) {
        super(color);
    }

    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY, Piece[][] board) {
        // Combine Rook and Bishop logic
        return new Rook(color).isValidMove(startX, startY, endX, endY, board) ||
               new Bishop(color).isValidMove(startX, startY, endX, endY, board);
    }

    @Override
    public String toString() {
        return color.equals("white") ? "Q" : "q";
    }
}