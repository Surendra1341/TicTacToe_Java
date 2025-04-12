package com.company.ClientServer;

import javax.swing.*;

public class TicTacToeClientTest {
    public static void main(String[] args) {
        String serverAddress = "127.0.0.1"; // Default to localhost

        // Use command line argument if provided
        if (args.length > 0) {
            serverAddress = args[0];
        }

        TicTacToeClient client = new TicTacToeClient(serverAddress);
        client.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
