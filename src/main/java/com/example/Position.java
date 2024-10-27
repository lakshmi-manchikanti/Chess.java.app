package com.example;

public class Position {
    private int row;
    private int column;
  
    public Position(int i, int j) {
        this.row = i;
        this.column = j;
    }
  
    public int getRow() {
        return row;
    }
  
    public int getColumn() {
        return column;
    }
}