package go.gui;

import go.logic.GameBoard;
import go.logic.Player;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;


public class PlayerPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	JLabel labelPrisoners = new JLabel();
    JLabel labelArea      = new JLabel();
    JLabel labelScore     = new JLabel();
	GameBoard board;
	Player player;
	private Border borderActive;
	private Border borderInactive;
	
	public PlayerPanel(GameBoard pBoard, Player pPlayer) {
		super();
		board = pBoard;
		player = pPlayer;
		borderActive   = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.RED, 2),  "Spieler " + pPlayer.getColor());
		borderInactive = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.BLACK, 2),  "Spieler " + pPlayer.getColor());
		this.setBorder(borderActive); 
		this.setLayout(new GridLayout(4,2));
        this.add(new JLabel("Name:   " + pPlayer.getName()));
        this.add(labelPrisoners);
        this.add(labelArea);
        this.add(labelScore);
	}

	@Override
	public void repaint() {
		if (board != null && player != null) {
			this.setBorder((board.getActivePlayer() == player && !board.isGameOver()) ? borderActive : borderInactive);
			labelPrisoners.setText("Gefangene: " + player.getPrisoners());
			labelArea.setText("Fläche: " + player.getArea());
			labelScore.setText("Punkte: " + player.getPoints());
		}
		super.repaint();
	}
	
	
}
