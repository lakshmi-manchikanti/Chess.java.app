package com.chessgame;

import java.util.List;
import java.util.ArrayList;

public class ChessGame {
    private ChessBoard board;
    private boolean whiteTurn = true; // White starts the game
    private final List<String> moveHistory = new ArrayList<>();
    private Position enPassantTarget; // Stores the en passant target square

    public ChessGame() {
        this.board = new ChessBoard();
    }

    public ChessBoard getBoard() {
        return this.board;
    }

    public void resetGame() {
        this.board = new ChessBoard();
        this.whiteTurn = true;
        moveHistory.clear(); // Clear history on reset
        enPassantTarget = null; // Reset en passant target
    }

    public PieceColor getCurrentPlayerColor() {
        return whiteTurn ? PieceColor.WHITE : PieceColor.BLACK;
    }

    private Position selectedPosition;

    public boolean isPieceSelected() {
        return selectedPosition != null;
    }

    public boolean handleSquareSelection(int row, int col) {
        if (selectedPosition == null) {
            Piece selectedPiece = board.getPiece(row, col);
            if (selectedPiece != null
                    && selectedPiece.getColor() == (whiteTurn ? PieceColor.WHITE : PieceColor.BLACK)) {
                selectedPosition = new Position(row, col);
                return false;
            }
        } else {
            boolean moveMade = makeMove(selectedPosition, new Position(row, col));
            selectedPosition = null;
            return moveMade;
        }
        return false;
    }

    // Make this method public to allow access from ChessGameGUI or other classes
    public boolean isEnPassantMove(Position start, Position end, Piece movingPiece) {
        // Check if the moving piece is a Pawn
        if (movingPiece instanceof Pawn && enPassantTarget != null) {
            // En Passant is only possible if the target position matches the enPassantTarget
            boolean isEnPassant = end.equals(enPassantTarget) && Math.abs(start.getColumn() - end.getColumn()) == 1;
            System.out.println("Checking En Passant Move: " + isEnPassant);
            return isEnPassant;
        }
        return false;
    }

    public boolean makeMove(Position start, Position end) {
        Piece movingPiece = board.getPiece(start.getRow(), start.getColumn());
        if (movingPiece == null || movingPiece.getColor() != (whiteTurn ? PieceColor.WHITE : PieceColor.BLACK)) {
            return false;
        }

        // Check if the move is an en passant move
        boolean isEnPassantMove = isEnPassantMove(start, end, movingPiece);

        // First, validate the move, whether it's a regular move or en passant
        if (movingPiece.isValidMove(end, board.getBoard()) || isEnPassantMove) {
            if (isEnPassantMove) {
                executeEnPassant(start, end);
            } else {
                board.movePiece(start, end);
            }

            // Handle pawn's two-square move for en passant
            if (movingPiece instanceof Pawn) {
                if (Math.abs(start.getRow() - end.getRow()) == 2) {
                    // Update the en passant target after two-square pawn advance
                    enPassantTarget = new Position(start.getRow() + (whiteTurn ? -1 : 1), start.getColumn());
                    System.out.println("En Passant Target Set: " + enPassantTarget);
                } else {
                    enPassantTarget = null;  // Reset en passant if pawn moves only one square
                }
            } else {
                enPassantTarget = null; // Not a pawn, so reset en passant
            }

            String moveNotation = generateMoveNotation(start, end);
            moveHistory.add(moveNotation);
            whiteTurn = !whiteTurn;
            return true;
        }
        return false;
    }

    private void executeEnPassant(Position start, Position end) {
        board.movePiece(start, end);  // Move the pawn to its final position
        // Capture the opponent's pawn that was en passant
        int capturedPawnRow = start.getRow();
        int capturedPawnCol = end.getColumn();
        board.setPiece(capturedPawnRow, capturedPawnCol, null); // Remove captured pawn
        System.out.println("En Passant Captured Pawn at: (" + capturedPawnRow + ", " + capturedPawnCol + ")");
    }

    public String getLastMove() {
        if (!moveHistory.isEmpty()) {
            return moveHistory.get(moveHistory.size() - 1);
        }
        return null;
    }

    private String generateMoveNotation(Position start, Position end) {
        char startFile = (char) ('a' + start.getColumn());
        int startRank = 8 - start.getRow();
        char endFile = (char) ('a' + end.getColumn());
        int endRank = 8 - end.getRow();
        return "" + startFile + startRank + endFile + endRank;
    }

    public boolean isInCheck(PieceColor kingColor) {
        Position kingPosition = findKingPosition(kingColor);
        for (int row = 0; row < board.getBoard().length; row++) {
            for (int col = 0; col < board.getBoard()[row].length; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.getColor() != kingColor) {
                    if (piece.isValidMove(kingPosition, board.getBoard())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private Position findKingPosition(PieceColor color) {
        for (int row = 0; row < board.getBoard().length; row++) {
            for (int col = 0; col < board.getBoard()[row].length; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece instanceof King && piece.getColor() == color) {
                    return new Position(row, col);
                }
            }
        }
        throw new RuntimeException("King not found, which should never happen.");
    }

    public boolean isCheckmate(PieceColor kingColor) {
        if (!isInCheck(kingColor)) {
            return false;
        }

        Position kingPosition = findKingPosition(kingColor);
        King king = (King) board.getPiece(kingPosition.getRow(), kingPosition.getColumn());

        for (int rowOffset = -1; rowOffset <= 1; rowOffset++) {
            for (int colOffset = -1; colOffset <= 1; colOffset++) {
                if (rowOffset == 0 && colOffset == 0) {
                    continue;
                }
                Position newPosition = new Position(kingPosition.getRow() + rowOffset,
                        kingPosition.getColumn() + colOffset);

                if (isPositionOnBoard(newPosition) && king.isValidMove(newPosition, board.getBoard())
                        && !wouldBeInCheckAfterMove(kingColor, kingPosition, newPosition)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isPositionOnBoard(Position position) {
        return position.getRow() >= 0 && position.getRow() < board.getBoard().length &&
                position.getColumn() >= 0 && position.getColumn() < board.getBoard()[0].length;
    }

    private boolean wouldBeInCheckAfterMove(PieceColor kingColor, Position from, Position to) {
        Piece temp = board.getPiece(to.getRow(), to.getColumn());
        board.setPiece(to.getRow(), to.getColumn(), board.getPiece(from.getRow(), from.getColumn()));
        board.setPiece(from.getRow(), from.getColumn(), null);

        boolean inCheck = isInCheck(kingColor);

        board.setPiece(from.getRow(), from.getColumn(), board.getPiece(to.getRow(), to.getColumn()));
        board.setPiece(to.getRow(), to.getColumn(), temp);

        return inCheck;
    }

    public List<Position> getLegalMovesForPieceAt(Position position) {
        Piece selectedPiece = board.getPiece(position.getRow(), position.getColumn());
        if (selectedPiece == null) {
            return new ArrayList<>();
        }
    
        List<Position> legalMoves = new ArrayList<>();
    
        // If the piece is a Pawn, use specific pawn logic
        if (selectedPiece instanceof Pawn) {
            addPawnMoves(position, selectedPiece.getColor(), legalMoves);
        } else {
            // For all other pieces, use their general isValidMove method
            for (int row = 0; row < board.getBoard().length; row++) {
                for (int col = 0; col < board.getBoard()[row].length; col++) {
                    Position newPos = new Position(row, col);
                    if (selectedPiece.isValidMove(newPos, board.getBoard())) {
                        legalMoves.add(newPos);
                    }
                }
            }
        }
        return legalMoves;
    }

    private void addPawnMoves(Position position, PieceColor color, List<Position> legalMoves) {
        int direction = color == PieceColor.WHITE ? -1 : 1;
        Position newPos = new Position(position.getRow() + direction, position.getColumn());
        if (isPositionOnBoard(newPos) && board.getPiece(newPos.getRow(), newPos.getColumn()) == null) {
            legalMoves.add(newPos);
        }

        // Handle two-square advance
        if ((color == PieceColor.WHITE && position.getRow() == 6) ||
                (color == PieceColor.BLACK && position.getRow() == 1)) {
            newPos = new Position(position.getRow() + 2 * direction, position.getColumn());
            Position intermediatePos = new Position(position.getRow() + direction, position.getColumn());
            if (isPositionOnBoard(newPos) && board.getPiece(newPos.getRow(), newPos.getColumn()) == null
                    && board.getPiece(intermediatePos.getRow(), intermediatePos.getColumn()) == null) {
                legalMoves.add(newPos);
            }
        }

        // Add capture moves and en passant
        for (int colOffset : new int[]{-1, 1}) {
            Position capturePos = new Position(position.getRow() + direction, position.getColumn() + colOffset);
            if (isPositionOnBoard(capturePos)) {
                Piece capturePiece = board.getPiece(capturePos.getRow(), capturePos.getColumn());
                if (capturePiece != null && capturePiece.getColor() != color) {
                    legalMoves.add(capturePos);  // Regular capture
                } else if (capturePos.equals(enPassantTarget)) {
                    legalMoves.add(capturePos);  // En passant capture
                }
            }
        }
    }

    public boolean isCastlingMove(Position start, Position end) {
        Piece movingPiece = board.getPiece(start.getRow(), start.getColumn());
        
        if (!(movingPiece instanceof King)) {
            return false;
        }
    
        King king = (King) movingPiece;
        if (king.hasMoved()) {
            return false;
        }
    
        int row = start.getRow();
        int colDiff = end.getColumn() - start.getColumn();
    
        if (Math.abs(colDiff) != 2) {
            return false;
        }
    
        int rookCol = (colDiff > 0) ? 7 : 0;
        Piece rook = board.getPiece(row, rookCol);
    
        if (!(rook instanceof Rook) || ((Rook) rook).hasMoved()) {
            return false;
        }
    
        // Ensure the squares between king and rook are empty
        int step = (colDiff > 0) ? 1 : -1;
        for (int col = start.getColumn() + step; col != rookCol; col += step) {
            if (board.getPiece(row, col) != null) {
                return false;
            }
        }
    
        // Ensure the king does not move through or into check
        Position middlePosition = new Position(row, start.getColumn() + step);
        if (isInCheck(king.getColor()) || wouldBeInCheckAfterMove(king.getColor(), start, middlePosition) ||
            wouldBeInCheckAfterMove(king.getColor(), start, end)) {
            return false;
        }
    
        return true;
    }
    
    public boolean isStalemate(PieceColor kingColor) {
        if (isInCheck(kingColor)) {
            return false;
        }
    
        for (int row = 0; row < board.getBoard().length; row++) {
            for (int col = 0; col < board.getBoard()[row].length; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.getColor() == kingColor) {
                    Position position = new Position(row, col);
                    List<Position> legalMoves = getLegalMovesForPieceAt(position);
                    if (!legalMoves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        
        return true;
    }
}