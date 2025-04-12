package com.company.local;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GameFrame extends JFrame implements ActionListener {

    private JButton aiButton;
    private JButton multiplayerButton;

    public GameFrame() {
        // Set up the frame
        setTitle("Game Menu");
        setSize(750, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the frame on the screen

        // Set the background color or a gradient
        getContentPane().setBackground(new Color(34, 40, 49));  // Dark theme background
        setLayout(new BorderLayout());

        // Create a panel to hold the buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 20, 20)); // 2 buttons, vertical layout with spacing
        buttonPanel.setOpaque(false);  // Set panel background to transparent

        // Create buttons with more stylish features
        aiButton = createStyledButton("AI");
        multiplayerButton = createStyledButton("Multiplayer");

        // Add ActionListeners to the buttons
        aiButton.addActionListener(this);
        multiplayerButton.addActionListener(this);

        // Add buttons to the panel
        buttonPanel.add(aiButton);
        buttonPanel.add(multiplayerButton);
       

        // Add button panel to the center of the frame
        add(buttonPanel, BorderLayout.CENTER);

        // Add a custom title label on top
        JLabel titleLabel = new JLabel("Welcome to the Game", JLabel.CENTER);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 40));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(50, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Make the frame visible
        setVisible(true);
    }

    // Method to create a custom button with modern styles
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20));  // Smaller font size
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(68, 138, 255)); // Blue background
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));  // Smaller padding for a compact button
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setSize(200,200);

        return button;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == aiButton) {
            System.out.println("AI button clicked");
            // Handle AI button action ( implement this next)
        } else if (e.getSource() == multiplayerButton) {
        	MyGame game= new MyGame();
            // Handle Multiplayer button 
        }
        this.setVisible(false);
    }
}
