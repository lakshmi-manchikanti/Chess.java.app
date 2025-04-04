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
    private boolean stockfishPlaysBlack = false; // Toggle for Stockfish playing as Black
    private JSlider stockfishLevelSlider;
    private JLabel skillLevelLabel;
    private int stockfishSkillLevel = 10; // Default skill level (0-20)

    public ChessGameGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("Chess.java");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        addMenuOptions();
        JPanel boardPanel = new JPanel(new GridBagLayout());
        initializeBoard(boardPanel);
        add(boardPanel, BorderLayout.CENTER);
        add(createSidePanel(), BorderLayout.EAST);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                game.closeStockfish();
            }
        });

        SwingUtilities.invokeLater(this::refreshBoard);

        pack();
        setVisible(true);
    }

    private void initializeBoard(JPanel boardPanel) {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        // Add file labels (a-h) at the top
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 8;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel topLabelPanel = new JPanel(new GridLayout(1, 8));
        for (char file = 'a'; file <= 'h'; file++) {
            JLabel fileLabel = new JLabel(String.valueOf(file), SwingConstants.CENTER);
            fileLabel.setFont(new Font("Arial", Font.BOLD, 12));
            topLabelPanel.add(fileLabel);
        }
        boardPanel.add(topLabelPanel, gbc);

        // Add rank labels (1-8) on the left
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        for (int rank = 8; rank >= 1; rank--) {
            gbc.gridy = 8 - rank + 1;
            JLabel rankLabel = new JLabel(String.valueOf(rank), SwingConstants.CENTER);
            rankLabel.setFont(new Font("Arial", Font.BOLD, 12));
            boardPanel.add(rankLabel, gbc);
        }

        // Add chess squares (8x8 grid)
        for (int row = 0; row < 8; row++) {
            gbc.gridx = 9;
            gbc.gridy = row + 1;
            JLabel rightRankLabel = new JLabel(String.valueOf(8 - row), SwingConstants.CENTER);
            rightRankLabel.setFont(new Font("Arial", Font.BOLD, 12));
            boardPanel.add(rightRankLabel, gbc);

            for (int col = 0; col < 8; col++) {
                gbc.gridx = col + 1;
                gbc.gridy = row + 1;
                ChessSquareComponent square = new ChessSquareComponent(row, col);
                int finalRow = row, finalCol = col;

                square.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        handleSquareClick(finalRow, finalCol);
                    }
                });

                boardPanel.add(square, gbc);
                squares[row][col] = square;
            }
        }

        // Add file labels (a-h) at the bottom
        gbc.gridx = 1;
        gbc.gridy = 9;
        gbc.gridwidth = 8;
        gbc.anchor = GridBagConstraints.CENTER;
        JPanel bottomLabelPanel = new JPanel(new GridLayout(1, 8));
        for (char file = 'a'; file <= 'h'; file++) {
            JLabel fileLabel = new JLabel(String.valueOf(file), SwingConstants.CENTER);
            fileLabel.setFont(new Font("Arial", Font.BOLD, 12));
            bottomLabelPanel.add(fileLabel);
        }
        boardPanel.add(bottomLabelPanel, gbc);

        refreshBoard();
    }

    @SuppressWarnings("unused")
    private JPanel createSidePanel() {
        JPanel sidePanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.HORIZONTAL; // Allow components to stretch horizontally

        JLabel title = new JLabel("Opponent Selection");
        title.setFont(new Font("Arial", Font.BOLD, 14));
        gbc.gridx = 0;
        gbc.gridy = 0;
        sidePanel.add(title, gbc);

        ButtonGroup group = new ButtonGroup();
        JRadioButton humanOpponent = new JRadioButton("Human (Both Players)", !stockfishPlaysBlack);
        JRadioButton stockfishOpponent = new JRadioButton("Stockfish (Black)", stockfishPlaysBlack);

        group.add(humanOpponent);
        group.add(stockfishOpponent);

        ActionListener radioListener = e -> {
            if (humanOpponent.isSelected()) {
                stockfishPlaysBlack = false;
                resetGame();
                stockfishLevelSlider.setEnabled(false);
                skillLevelLabel.setEnabled(false);
            } else if (stockfishOpponent.isSelected()) {
                stockfishPlaysBlack = true;
                stockfishLevelSlider.setEnabled(true);
                skillLevelLabel.setEnabled(true);
                startStockfishVsHuman();
            }
        };

        humanOpponent.addActionListener(radioListener);
        stockfishOpponent.addActionListener(radioListener);

        gbc.gridy = 1;
        sidePanel.add(humanOpponent, gbc);
        gbc.gridy = 2;
        sidePanel.add(stockfishOpponent, gbc);

        gbc.gridy = 3;
        skillLevelLabel = new JLabel("Stockfish Level: " + stockfishSkillLevel);
        skillLevelLabel.setFont(new Font("Arial", Font.PLAIN, 14)); // Increase font size for readability
        sidePanel.add(skillLevelLabel, gbc);

        gbc.gridy = 4;
        stockfishLevelSlider = new JSlider(JSlider.HORIZONTAL, 0, 20, stockfishSkillLevel);
        stockfishLevelSlider.setMajorTickSpacing(5);
        stockfishLevelSlider.setMinorTickSpacing(1);
        stockfishLevelSlider.setPaintTicks(true);
        stockfishLevelSlider.setPaintLabels(true);
        stockfishLevelSlider.setEnabled(stockfishPlaysBlack);

        // Increase the size of the slider
        stockfishLevelSlider.setPreferredSize(new Dimension(250, 80)); // Increased width and height
        stockfishLevelSlider.setFont(new Font("Arial", Font.PLAIN, 14)); // Larger font for labels

        stockfishLevelSlider.addChangeListener(e -> {
            stockfishSkillLevel = stockfishLevelSlider.getValue();
            skillLevelLabel.setText("Stockfish Level: " + stockfishSkillLevel);
            game.setStockfishSkillLevel(stockfishSkillLevel);
        });

        sidePanel.add(stockfishLevelSlider, gbc);

        // Add a button to show Stockfish's best move
        JButton bestMoveButton = new JButton("Show Stockfish Best Move");
        bestMoveButton.setFont(new Font("Arial", Font.PLAIN, 14)); // Increase font size for readability
        bestMoveButton.addActionListener(e -> showStockfishBestMove());
        gbc.gridy = 5;
        sidePanel.add(bestMoveButton, gbc);

        JButton resetButton = new JButton("Reset Game");
        resetButton.setFont(new Font("Arial", Font.PLAIN, 14)); // Increase font size for readability
        resetButton.addActionListener(e -> resetGame());
        gbc.gridy = 6;
        sidePanel.add(resetButton, gbc);

        sidePanel.setPreferredSize(new Dimension(300, getHeight())); // Increase side panel width to accommodate larger slider
        return sidePanel;
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
                checkForPawnPromotion(row, col);
                refreshBoard();
                checkGameState();
                checkGameOver();

                // Show Stockfish's best move if it's White's turn and Stockfish plays Black
                if (stockfishPlaysBlack && game.getCurrentPlayerColor() == PieceColor.WHITE) {
                    SwingUtilities.invokeLater(this::showStockfishBestMove);
                }

                if (stockfishPlaysBlack && game.getCurrentPlayerColor() == PieceColor.BLACK) {
                    SwingUtilities.invokeLater(this::playStockfishMove);
                }
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
        if (stockfishPlaysBlack) {
            game.setStockfishSkillLevel(stockfishSkillLevel);
        }
        refreshBoard();
    }

    private void checkGameOver() {
        PieceColor currentPlayer = game.getCurrentPlayerColor();

        if (game.isCheckmate(currentPlayer)) {
            int response = JOptionPane.showConfirmDialog(this, "Checkmate! Would you like to play again?", "Game Over", JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.YES_OPTION) {
                resetGame();
            } else {
                game.closeStockfish();
                System.exit(0);
            }
        } else if (game.isStalemate(currentPlayer)) {
            JOptionPane.showMessageDialog(this, "Stalemate! The game is a draw.");
            resetGame();
        }
    }

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

            game.getBoard().setPiece(row, col, null);

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

            if (promotedPiece != null) {
                game.getBoard().setPiece(row, col, promotedPiece);
            }

            refreshBoard();
        }
    }

    private void startStockfishVsHuman() {
        resetGame();
        stockfishPlaysBlack = true;
        game.setStockfishSkillLevel(stockfishSkillLevel);
        JOptionPane.showMessageDialog(this, "Stockfish will play as Black at level " + stockfishSkillLevel + ". You start as White.");
        refreshBoard();
    }

    private void playStockfishMove() {
        game.playStockfishMove();
        refreshBoard();
        checkGameState();
        checkGameOver();
    }

    private void showStockfishBestMove() {
        // Clear any previous highlights
        clearHighlights();

        // Get Stockfish's best move
        String stockfishMove = game.getStockfishMove();
        if (stockfishMove != null && stockfishMove.length() == 4) {
            // Parse the move (e.g., "e2e4")
            Position start = new Position(8 - (stockfishMove.charAt(1) - '0'), stockfishMove.charAt(0) - 'a');
            Position end = new Position(8 - (stockfishMove.charAt(3) - '0'), stockfishMove.charAt(2) - 'a');

            // Highlight the start and end positions with a distinct color (e.g., yellow)
            squares[start.getRow()][start.getColumn()].setBackground(Color.YELLOW);
            squares[end.getRow()][end.getColumn()].setBackground(Color.YELLOW);
        } else {
            JOptionPane.showMessageDialog(this, "Unable to retrieve Stockfish's best move.");
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