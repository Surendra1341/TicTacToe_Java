package com.company.local;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Date;

import javax.sound.midi.Soundbank;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class MyGame extends JFrame implements ActionListener {

	JLabel heading, clockLabel;
	Font font = new Font(null, Font.BOLD, 40);
	JPanel mainPanel;

	JButton[] buttons = new JButton[9];

	// game variable
	int[] gameChances = { 2, 2, 2, 2, 2, 2, 2, 2, 2 }; // ready state
	int activePlayer = 0; // 0 ->O 1-> X
	int[][] wps = { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 }, { 0, 3, 6 }, { 1, 4, 7 }, { 2, 5, 8 }, { 0, 4, 8 },
			{ 2, 4, 6 } };
	int winner = 2;
	boolean gameOver=false;

	public MyGame() {

		setTitle("Tic Tac Toe Game...");
		setSize(750, 750);
		ImageIcon image = new ImageIcon("resources/OX.png");
		setIconImage(image.getImage());

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		createGUI();
		setVisible(true);
	}

	private void createGUI() {
		this.getContentPane().setBackground(Color.decode("#2196F3"));
		this.setLayout(new BorderLayout());

		// heading section

		heading = new JLabel("Tic Tac Toe");
		heading.setFont(font);
		heading.setHorizontalAlignment(SwingConstants.CENTER);
		heading.setForeground(Color.white);

		this.add(heading, BorderLayout.NORTH);

		clockLabel = new JLabel("Clock");
		clockLabel.setFont(font);
		clockLabel.setHorizontalAlignment(SwingConstants.CENTER);
		clockLabel.setForeground(Color.white);

		this.add(clockLabel, BorderLayout.SOUTH);

		Thread t = new Thread() {
			public void run() {
				try {
					while (true) {
						String datetime = new Date().toLocaleString();

						clockLabel.setText(datetime);

						Thread.sleep(1000);
					}
				} catch (Exception e) {
					System.out.println(e.getMessage());
				}
			}
		};
		t.start();

		// panel section
		mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(3, 3));

		for (int i = 1; i <= 9; i++) {
			JButton btn = new JButton();

			btn.setBackground(Color.decode("#90caf9"));
			btn.setFont(font);
			mainPanel.add(btn);
			buttons[i - 1] = btn;
			btn.addActionListener(this);
			btn.setName(String.valueOf(i - 1));
		}

		this.add(mainPanel, BorderLayout.CENTER);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		JButton current = (JButton) e.getSource();
		int name = Integer.parseInt(current.getName());
		
		if(gameOver) {
			JOptionPane.showMessageDialog(this, "Game Already over !..");
			return;
		}

		if (gameChances[name] == 2) {

			gameChances[name] = activePlayer;
			if (activePlayer == 1) {
				current.setIcon(new ImageIcon("resources/X.png"));
				activePlayer = 0;
			} else {
				current.setIcon(new ImageIcon("resources/O.png"));
				activePlayer = 1;
			}

			// finding winner
			for (int[] temp : wps) {
				if ((gameChances[temp[0]] == gameChances[temp[1]]) && (gameChances[temp[1]] == gameChances[temp[2]])
						&& gameChances[temp[2]] != 2) {
					winner = gameChances[temp[0]];
					gameOver=true;
					String str = winner == 1 ? "X" : "O";
					JOptionPane.showMessageDialog(null, "player " + str + " has won the game");
					int i = JOptionPane.showConfirmDialog(this, "play again");
					if (i == 0) {
						this.setVisible(false);
						new MyGame();
					} else if (i == 1) {
						System.exit(2444);
					} else {

					}
					break;
				}
			}
			
			//draw logic
			int count=0;
			for(int temp : gameChances) {
				if(temp==2) {
					count++;
					break;
				}
			}
			if(count==0 && gameOver==false) {
				JOptionPane.showMessageDialog(null, "Its Draw");
				int i = JOptionPane.showConfirmDialog(this, "play more");
				if (i == 0) {
					 resetGame();
				} else if (i == 1) {
					System.exit(2444);
				} else {

				}
				gameOver=true;
			}
			
		} else {
			JOptionPane.showMessageDialog(this, "Position already occupied");
		}

	}
	
	private void resetGame() {
	    gameChances = new int[9];  // Reset game state
	    for (int i = 0; i < 9; i++) {
	        buttons[i].setIcon(null);  // Clear the icons on buttons
	    }
	    activePlayer = 0;  // Start with Player O
	    winner = 2;  // No winner yet
	    gameOver = false;  // Game is not over
	    clockLabel.setText("Clock");  // Reset the clock display
	}


}
