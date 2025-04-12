package com.company.ClientServer;

// TicTacToeServerTest.java

import javax.swing.*;

public class TicTacToeServerTest {
    public static void main(String[] args) {
        TicTacToeServer server = new TicTacToeServer();
        server.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        server.execute();
    }
}