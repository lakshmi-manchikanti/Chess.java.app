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
                    Color color = (piece.getColor() == PieceColor.WHITE) ? Color.WHITE : Color.BLACK;
                    squares[row][col].setPieceSymbol(symbol, color);
                } else {
                    squares[row][col].clearPieceSymbol();
                }
            }
        }

        SwingUtilities.invokeLater(this::repaint);
    }

    private void handleSquareClick(int row, int col) {
        try {
            boolean moveResult = game.handleSquareSelection(row, col);
            clearHighlights();

            if (moveResult) {
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
                squares[row][col].setBackground(isDarkTheme ? new Color(105, 105, 105) : (row + col) % 2 == 0 ? Color.LIGHT_GRAY : new Color(205, 133, 63));
            }
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

        setJMenuBar(menuBar);
    }

    private void changeTheme(boolean dark) {
        isDarkTheme = dark;
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

    public static void main(String[] args) {
        if (GraphicsEnvironment.isHeadless()) {
            System.out.println("Error: Headless environment detected. GUI cannot be created.");
            System.exit(1);
        } else {
            System.setProperty("sun.java2d.uiScale", "3");
            SwingUtilities.invokeLater(ChessGameGUI::new);
        }
    }
}