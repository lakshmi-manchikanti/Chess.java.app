package com.example;

public class Board {
    private Piece[][] board;

    public Board() {
        board = new Piece[8][8];
        setupBoard();
    }

    private void setupBoard() {
        // Set up white pieces
        board[0][0] = new Rook("white");
        board[0][7] = new Rook("white");
        for (int i = 0; i < 8; i++) {
            board[1][i] = new Pawn("white");
        }

        // Set up black pieces
        board[7][0] = new Rook("black");
        board[7][7] = new Rook("black");
        for (int i = 0; i < 8; i++) {
            board[6][i] = new Pawn("black");
        }

        // Add other pieces (Knights, Bishops, Queens, Kings) similarly...
    }

    public Piece[][] getBoard() {
        return board;
    }

    public boolean movePiece(int startX, int startY, int endX, int endY) {
        Piece piece = board[startX][startY];
        if (piece != null && piece.isValidMove(startX, startY, endX, endY, board)) {
            board[endX][endY] = piece;
            board[startX][startY] = null;
            return true;
        }
        return false;
    }
}
