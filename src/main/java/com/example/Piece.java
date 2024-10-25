package com.example;

public abstract class Piece {
    protected String color;  // "white" or "black"

    public Piece(String color) {
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public abstract boolean isValidMove(int startX, int startY, int endX, int endY, Piece[][] board);
}