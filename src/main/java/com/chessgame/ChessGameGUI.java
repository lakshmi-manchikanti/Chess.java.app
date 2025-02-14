package com.chessgame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChessGameGUI extends JFrame {
    private final ChessSquareComponent[][] squares = new ChessSquareComponent[8][8];
    private final ChessGame game = new ChessGame();

    private final Map<Class<? extends Piece>, String> pieceUnicodeMap = new HashMap<>() {
        {
            put(Pawn.class, "\u265F");
            put(Rook.class, "\u265C");
            put(Knight.class, "\u265E");
            put(Bishop.class, "\u265D");
            put(Queen.class, "\u265B");
            put(King.class, "\u265A");
        }
    };

    private boolean isDarkTheme = false;
    private String boardStyle = "Wood"; // Default board style

    public ChessGameGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Chess Game");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new GridLayout(8, 8));
        initializeBoard();
        addMenuOptions();

        SwingUtilities.invokeLater(this::refreshBoard);

        pack();
        setVisible(true);
    }

    private void initializeBoard() {
        for (int row = 0; row < squares.length; row++) {
            for (int col = 0; col < squares[row].length; col++) {
                ChessSquareComponent square = new ChessSquareComponent(row, col);
                int finalRow = row, finalCol = col;

                square.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        handleSquareClick(finalRow, finalCol);
                    }
                });

                add(square);
                squares[row][col] = square;
            }
        }
        refreshBoard();
    }

    private void refreshBoard() {
        ChessBoard board = game.getBoard();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null) {
                    String symbol = pieceUnicodeMap.get(piece.getClass());
                    Color color = piece.getColor() == PieceColor.WHITE ? Color.WHITE : (isDarkTheme ? Color.BLACK : Color.BLACK);
                    squares[row][col].setPieceSymbol(symbol, color);
                } else {
                    squares[row][col].clearPieceSymbol();
                }
            }
        }

        clearHighlights();
        SwingUtilities.invokeLater(this::repaint);
    }

    private void handleSquareClick(int row, int col) {
        try {
            boolean moveResult = game.handleSquareSelection(row, col);
            clearHighlights();

            if (moveResult) {
                checkForPawnPromotion(row, col); // Check for pawn promotion
                refreshBoard();
                checkGameState();
                checkGameOver();
            } else if (game.isPieceSelected()) {
                highlightLegalMoves(new Position(row, col));
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "An error occurred: " + e.getMessage());
        }
    }

    private void checkForPawnPromotion(int row, int col) {
        Piece piece = game.getBoard().getPiece(row, col);
        if (piece instanceof Pawn) {
            Pawn pawn = (Pawn) piece;
            if ((pawn.getColor() == PieceColor.WHITE && row == 0) || (pawn.getColor() == PieceColor.BLACK && row == 7)) {
                promotePawn(pawn);
            }
        }
    }

    private void checkGameState() {
        PieceColor currentPlayer = game.getCurrentPlayerColor();
        boolean inCheck = game.isInCheck(currentPlayer);

        if (inCheck) {
            JOptionPane.showMessageDialog(this, currentPlayer + " is in check!");
        }
    }

    private void highlightLegalMoves(Position position) {
        List<Position> legalMoves = game.getLegalMovesForPieceAt(position);

        for (Position move : legalMoves) {
            Piece movingPiece = game.getBoard().getPiece(position.getRow(), position.getColumn());

            if (movingPiece != null && game.isEnPassantMove(position, move, movingPiece)) {
                squares[move.getRow()][move.getColumn()].setBackground(Color.PINK);
            } else if (game.isCastlingMove(position, move)) {
                squares[move.getRow()][move.getColumn()].setBackground(Color.BLUE);
            } else {
                squares[move.getRow()][move.getColumn()].setBackground(Color.GREEN);
            }
        }
    }


    private void clearHighlights() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col].setBackground(getSquareColor(row, col));
            }
        }
    }

    private Color getSquareColor(int row, int col) {
        if (isDarkTheme) {
            return (row + col) % 2 == 0 ? Color.DARK_GRAY : Color.GRAY;
        } else if (boardStyle.equals("Light Wood")) {
            return (row + col) % 2 == 0 ? new Color(245, 222, 179) : new Color(150, 75, 0);
        } else if (boardStyle.equals("Dark Wood")) {
            return (row + col) % 2 == 0 ? new Color(240, 217, 181) : new Color(101, 67, 33);
        } else if (boardStyle.equals("Green Board")) {
            return (row + col) % 2 == 0 ? new Color(238, 238, 210) : new Color(118, 150, 86);
        } else { // Default "Wood" style
            return (row + col) % 2 == 0 ? Color.LIGHT_GRAY : new Color(205, 133, 63);
        }
    }

    @SuppressWarnings("unused")
    private void addMenuOptions() {
        JMenuBar menuBar = new JMenuBar();

        JMenu gameMenu = new JMenu("Game");
        JMenuItem resetItem = new JMenuItem("Reset");
        resetItem.addActionListener(e -> resetGame());
        gameMenu.add(resetItem);
        menuBar.add(gameMenu);

        JMenu themesMenu = new JMenu("Themes");
        JMenuItem lightThemeItem = new JMenuItem("Light Theme");
        JMenuItem darkThemeItem = new JMenuItem("Dark Theme");

        lightThemeItem.addActionListener(e -> changeTheme(false));
        darkThemeItem.addActionListener(e -> changeTheme(true));

        themesMenu.add(lightThemeItem);
        themesMenu.add(darkThemeItem);
        menuBar.add(themesMenu);

        JMenu boardsMenu = new JMenu("Boards");
        JMenuItem woodItem = new JMenuItem("Wood");
        JMenuItem lightWoodItem = new JMenuItem("Light Wood");
        JMenuItem darkWoodItem = new JMenuItem("Dark Wood");
        JMenuItem greenBoard = new JMenuItem("Green");

        woodItem.addActionListener(e -> changeBoardStyle("Wood"));
        lightWoodItem.addActionListener(e -> changeBoardStyle("Light Wood"));
        darkWoodItem.addActionListener(e -> changeBoardStyle("Dark Wood"));
        greenBoard.addActionListener(e -> changeBoardStyle("Green Board"));

        boardsMenu.add(woodItem);
        boardsMenu.add(lightWoodItem);
        boardsMenu.add(darkWoodItem);
        boardsMenu.add(greenBoard);
        menuBar.add(boardsMenu);

        setJMenuBar(menuBar);
    }

    private void changeTheme(boolean dark) {
        isDarkTheme = dark;
        refreshBoard();
    }

    private void changeBoardStyle(String style) {
        boardStyle = style;
        refreshBoard();
    }

    private void resetGame() {
        game.resetGame();
        refreshBoard();
    }

    private void checkGameOver() {
        PieceColor currentPlayer = game.getCurrentPlayerColor();

        if (game.isCheckmate(currentPlayer)) {
            int response = JOptionPane.showConfirmDialog(this, "Checkmate! Would you like to play again?", "Game Over", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                resetGame();
            } else {
                System.exit(0);
            }
        } else if (game.isStalemate(currentPlayer)) {
            JOptionPane.showMessageDialog(this, "Stalemate! The game is a draw.");
            resetGame();
        }
    }

    // Add pawn promotion dialog
    private void promotePawn(Pawn pawn) {
        String[] options = {"Queen", "Rook", "Bishop", "Knight"};
        String selectedOption = (String) JOptionPane.showInputDialog(this,
                "Choose a piece to promote your pawn to:",
                "Pawn Promotion",
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);
    
        if (selectedOption != null) {
            int row = pawn.getPosition().getRow();
            int col = pawn.getPosition().getColumn();
    
            // Remove the pawn from the board
            game.getBoard().setPiece(row, col, null);
    
            // Create the promoted piece based on the user's selection
            Piece promotedPiece = null;
            switch (selectedOption) {
                case "Queen":
                    promotedPiece = new Queen(pawn.getColor(), pawn.getPosition());
                    break;
                case "Rook":
                    promotedPiece = new Rook(pawn.getColor(), pawn.getPosition());
                    break;
                case "Bishop":
                    promotedPiece = new Bishop(pawn.getColor(), pawn.getPosition());
                    break;
                case "Knight":
                    promotedPiece = new Knight(pawn.getColor(), pawn.getPosition());
                    break;
            }
    
            // Place the promoted piece on the board
            if (promotedPiece != null) {
                game.getBoard().setPiece(row, col, promotedPiece);
            }
    
            // Refresh the board to reflect the changes
            refreshBoard();
        }
    }

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Error: Headless environment detected. GUI cannot be created.");
            System.exit(1);
        } else {
            System.setProperty("sun.java3d.uiScale", "4");
            SwingUtilities.invokeLater(ChessGameGUI::new);
        }
    }
}