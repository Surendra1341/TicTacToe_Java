// Client-side code
// TicTacToeClient.java
package com.company.ClientServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Formatter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TicTacToeClient extends JFrame implements Runnable {
    // Constants
    private static final String X_MARK = "X";
    private static final String O_MARK = "O";
    private static final int PORT = 12345;

    // UI components
    private JTextField statusField;
    private JTextArea messageArea;
    private JPanel boardPanel;
    private Square[][] board;

    // Network components
    private Socket connection;
    private Scanner input;
    private Formatter output;
    private String serverAddress;

    // Game state
    private String myMark;
    private boolean myTurn;
    private Square currentSquare;
    private boolean gameOver;

    public TicTacToeClient(String host) {
        super("Tic-Tac-Toe Client");
        serverAddress = host;
        gameOver = false;

        // Create UI components
        statusField = new JTextField();
        statusField.setEditable(false);
        add(statusField, BorderLayout.NORTH);

        messageArea = new JTextArea(4, 30);
        messageArea.setEditable(false);
        add(new JScrollPane(messageArea), BorderLayout.SOUTH);

        // Create game board
        boardPanel = new JPanel();
        boardPanel.setLayout(new GridLayout(3, 3, 2, 2));
        boardPanel.setBackground(Color.BLACK);

        board = new Square[3][3];
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                board[row][col] = new Square(row, col);
                boardPanel.add(board[row][col]);
            }
        }

        JPanel centerPanel = new JPanel();
        centerPanel.add(boardPanel);
        add(centerPanel, BorderLayout.CENTER);

        setSize(400, 400);
        setVisible(true);

        // Connect to server
        startClient();
    }

    private void startClient() {
        try {
            connection = new Socket(InetAddress.getByName(serverAddress), PORT);
            input = new Scanner(connection.getInputStream());
            output = new Formatter(connection.getOutputStream());

            // Start client thread
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(this);
        } catch (IOException e) {
            e.printStackTrace();
            displayMessage("Error connecting to server: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            // Get player's mark (X or O)
            myMark = input.nextLine();
            SwingUtilities.invokeLater(() ->
                    statusField.setText("You are player \"" + myMark + "\"")
            );

            // Set initial turn based on mark
            myTurn = myMark.equals(X_MARK);

            // Game loop
            while (!gameOver) {
                if (input.hasNextLine()) {
                    processMessage(input.nextLine());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            displayMessage("Connection to server lost");
        } finally {
            try {
                connection.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void processMessage(String message) {
        // Handle different message types from -> server
        if (message.startsWith("Valid move.")) {
            displayMessage("Valid move, please wait\n");
            setMark(currentSquare, myMark);
        } else if (message.startsWith("Invalid move")) {
            displayMessage(message + "\n");
            myTurn = true;
        } else if (message.startsWith("Opponent Moved")) {
            int location = input.nextInt();
            input.nextLine(); // Consume newline
            int row = location / 3;
            int col = location % 3;
            setMark(board[row][col], (myMark.equals(X_MARK) ? O_MARK : X_MARK));
            displayMessage("Opponent moved, your turn\n");
            myTurn = true;
        } else if (message.startsWith("Game over")) {
            displayMessage(message + "\n");
            gameOver = true;
        } else {
            displayMessage(message + "\n");
        }
    }

    private void displayMessage(final String message) {
        SwingUtilities.invokeLater(() -> messageArea.append(message));
    }

    private void setMark(final Square square, final String mark) {
        SwingUtilities.invokeLater(() -> {
            square.setMark(mark);
            repaint();
        });
    }

    public void sendMove(int location) {
        if (myTurn) {
            output.format("%d\n", location);
            output.flush();
            myTurn = false;
            displayMessage("You moved, waiting for opponent\n");
        }
    }

    // Square component representing a cell in the game board
    private class Square extends JPanel {
        private String mark;
        private int row;
        private int col;

        public Square(int row, int col) {
            this.row = row;
            this.col = col;
            this.mark = "";

            setBackground(Color.WHITE);
            setBorder(BorderFactory.createLineBorder(Color.BLACK));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseReleased(MouseEvent e) {
                    if (myTurn && mark.equals("") && !gameOver) {
                        currentSquare = Square.this;
                        sendMove(getSquareLocation());
                    }
                }
            });
        }

        public int getSquareLocation() {
            return row * 3 + col;
        }

        public void setMark(String newMark) {
            mark = newMark;
            repaint();
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(80, 80);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            if (mark.equals(X_MARK)) {
                g2.setColor(Color.RED);
                g2.setStroke(new BasicStroke(4));
                int padding = 15;
                g2.drawLine(padding, padding, getWidth() - padding, getHeight() - padding);
                g2.drawLine(getWidth() - padding, padding, padding, getHeight() - padding);
            } else if (mark.equals(O_MARK)) {
                g2.setColor(Color.BLUE);
                g2.setStroke(new BasicStroke(4));
                int padding = 10;
                g2.drawOval(padding, padding, getWidth() - 2 * padding, getHeight() - 2 * padding);
            }
        }
    }
}