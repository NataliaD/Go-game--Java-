package go.gui;


import go.logic.GameBoard;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;


public class GoPanel extends JPanel{
	
	private static final long serialVersionUID = 1L;
	private GameBoard board;
    private Image   backgroundImg;
    private Image[] stoneImg;
    private int drawCursorX = -1;
    private int drawCursorY = -1;
    public final static int FIELD_SIZE = 50;
    
    public GoPanel(String pBackground, GameBoard pBoard){
        board = pBoard;
        try {
			backgroundImg   = ImageIO.read(new File(pBackground));
			stoneImg        = new Image[4];
			stoneImg[0]     = ImageIO.read(new File("resources/SteinS.png"));
			stoneImg[1]     = ImageIO.read(new File("resources/SteinW.png"));
			stoneImg[2]     = ImageIO.read(new File("resources/SteinCS.png"));
			stoneImg[3]     = ImageIO.read(new File("resources/SteinCW.png"));
		} catch (IOException e) {
			System.out.println("Fehler beim Laden eines Bitmaps!");
			e.printStackTrace();
		}
        this.setPreferredSize(new Dimension(450,450));
    }
    
    public void setCursor(int x, int y) {
    	drawCursorX = x;
    	drawCursorY = y;
    }
        
    @Override
    public void paint(Graphics g) {

    	g.drawImage(backgroundImg, 0, 0, null);
    	
    	for (int y = 0; y < GameBoard.SIZE; y++) {
        	for (int x = 0; x < GameBoard.SIZE; x++) {
                if (!board.getStone(x, y).isEmpty()) {
                	g.drawImage(stoneImg[board.getStone(x, y).getPlayer().getOrd()], x*FIELD_SIZE, y*FIELD_SIZE, null);
                }
                if (x == drawCursorX && y == drawCursorY) {
                	g.drawImage(stoneImg[board.getActivePlayer().getOrd() + 2], x*FIELD_SIZE, y*FIELD_SIZE, null);
                }
    		}
		}
        	
    }
}
