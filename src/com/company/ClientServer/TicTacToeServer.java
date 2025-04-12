package com.company.ClientServer;
// Server-side code
// TicTacToeServer.java

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Formatter;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TicTacToeServer extends JFrame {
    // Constants
    private static final int PORT = 12345;
    private static final int PLAYER_X = 0;
    private static final int PLAYER_O = 1;
    private static final String[] MARKS = {"X", "O"};

    // Game state
    private int[] board = {-1, -1, -1, -1, -1, -1, -1, -1, -1}; // -1 means empty cell
    private JTextArea outputArea;
    private Player[] players;
    private ServerSocket server;
    private int currentPlayer;
    private ExecutorService executor;
    private Lock gameLock;
    private Condition otherPlayerConnected;
    private Condition otherPlayerTurn;
    private boolean gameOver;

    // Winning patterns (row, column, diagonal combinations)
    private final int[][] winPatterns = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},  // rows
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8},  // columns
            {0, 4, 8}, {2, 4, 6}              // diagonals
    };

    public TicTacToeServer() {
        super("Tic-Tac-Toe Server");

        // Initialize components
        executor = Executors.newFixedThreadPool(2);
        gameLock = new ReentrantLock();
        otherPlayerConnected = gameLock.newCondition();
        otherPlayerTurn = gameLock.newCondition();
        players = new Player[2];
        currentPlayer = PLAYER_X;
        gameOver = false;

        try {
            server = new ServerSocket(PORT, 2);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Setup UI
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        add(new JScrollPane(outputArea), BorderLayout.CENTER);
        outputArea.setText("Server awaiting connections\n");

        setSize(400, 300);
        setVisible(true);
    }

    public void execute() {
        // Accept both player connections
        for (int i = 0; i < players.length; i++) {
            try {
                displayMessage("Waiting for Player " + (i + 1) + "...\n");
                players[i] = new Player(server.accept(), i);
                executor.execute(players[i]);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        // Start game when both players are connected
        gameLock.lock();
        try {
            players[PLAYER_X].setSuspended(false);
            otherPlayerConnected.signal();
        } finally {
            gameLock.unlock();
        }
    }

    public void displayMessage(final String message) {
        SwingUtilities.invokeLater(() -> outputArea.append(message));
    }

    // Validate move and update game state
    public boolean validateAndMove(int location, int player) {
        // Wait for player's turn
        while (player != currentPlayer) {
            gameLock.lock();
            try {
                otherPlayerTurn.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                gameLock.unlock();
            }
        }

        // Check if move is valid
        if (location >= 0 && location < 9 && board[location] == -1) {
            board[location] = currentPlayer;
            currentPlayer = (currentPlayer + 1) % 2;

            // Notify other player about the move
            players[currentPlayer].otherPlayerMoved(location);

            // Signal that it's now the other player's turn
            gameLock.lock();
            try {
                otherPlayerTurn.signal();
            } finally {
                gameLock.unlock();
            }

            // Check if game is over after this move
            return true;
        } else {
            return false;
        }
    }

    // Check if game is over (win or draw)
    public boolean isGameOver() {
        // Check for win
        for (int[] pattern : winPatterns) {
            if (board[pattern[0]] != -1 &&
                    board[pattern[0]] == board[pattern[1]] &&
                    board[pattern[0]] == board[pattern[2]]) {
                gameOver = true;
                notifyGameOver(board[pattern[0]]);
                return true;
            }
        }

        // Check for draw
        boolean isDraw = true;
        for (int cell : board) {
            if (cell == -1) {
                isDraw = false;
                break;
            }
        }

        if (isDraw) {
            gameOver = true;
            notifyGameOver(-1); // -1 indicates draw
            return true;
        }

        return false;
    }

    // Notify both players about game result
    private void notifyGameOver(int winner) {
        if (winner == -1) {
            displayMessage("\nGame over: Draw\n");
            for (Player player : players) {
                player.sendMessage("Game over: Draw");
            }
        } else {
            String mark = MARKS[winner];
            displayMessage("\nGame over: Player " + mark + " wins\n");
            for (Player player : players) {
                player.sendMessage("Game over: Player " + mark + " wins");
            }
        }
    }

    // Player thread class
    private class Player implements Runnable {
        private Socket connection;
        private Scanner input;
        private Formatter output;
        private int playerNumber;
        private String mark;
        private boolean suspended = true;

        public Player(Socket socket, int number) {
            playerNumber = number;
            mark = MARKS[playerNumber];
            connection = socket;

            try {
                input = new Scanner(connection.getInputStream());
                output = new Formatter(connection.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }

        public void otherPlayerMoved(int location) {
            output.format("Opponent Moved\n");
            output.format("%d\n", location);
            output.flush();
        }

        public void sendMessage(String message) {
            output.format("%s\n", message);
            output.flush();
        }

        @Override
        public void run() {
            try {
                displayMessage("Player " + mark + " connected\n");

                // Send player's mark
                output.format("%s\n", mark);
                output.flush();

                if (playerNumber == PLAYER_X) {
                    output.format("%s\n%s", "Player X connected", "Waiting for another player");
                    output.flush();

                    // Wait for player O to connect
                    gameLock.lock();
                    try {
                        while (suspended) {
                            otherPlayerConnected.await();
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } finally {
                        gameLock.unlock();
                    }

                    // Start game
                    output.format("Other player connected. Your move.\n");
                    output.flush();
                } else {
                    output.format("Player O connected, please wait\n");
                    output.flush();
                }

                // Game loop
                while (!gameOver) {
                    if (input.hasNext()) {
                        int location = input.nextInt();

                        if (validateAndMove(location, playerNumber)) {
                            displayMessage("\nPlayer " + mark + " moved to position: " + location + "\n");
                            output.format("Valid move.\n");
                            output.flush();

                            // Check if game is over after this move
                            if (isGameOver()) {
                                break;
                            }
                        } else {
                            output.format("Invalid move, try again\n");
                            output.flush();
                        }
                    }
                }
            } finally {
                try {
                    connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void setSuspended(boolean status) {
            suspended = status;
        }
    }
}