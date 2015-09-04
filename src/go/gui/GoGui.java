package go.gui;


import go.logic.GameBoard;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;


public class GoGui extends JFrame {
	
	private static final long serialVersionUID = 1L;
	//GoGame game ;
    GameBoard board;
    GoPanel boardPanel;
    JPanel infoPanel = new JPanel();
    PlayerPanel p1Panel;
    PlayerPanel p2Panel;
    JLabel statusLbl = new JLabel();
    JButton pass = new JButton("Passen");
    JButton revert = new JButton("Rückgängig");
    int drawX = -1;
    int drawY = -1;
    
    public GoGui(GameBoard pBoard){
        board = pBoard;
        boardPanel  = new GoPanel("resources/Brett.png", pBoard);
        boardPanel.addMouseListener(new GoCanListener(boardPanel));
        boardPanel.addMouseMotionListener(new GoCanMoveListener(boardPanel));
        
        p1Panel = new PlayerPanel(board, board.getPlayer1());
        p2Panel = new PlayerPanel(board, board.getPlayer2());
        
        pass.addActionListener(new PassListener(this));
        revert.addActionListener(new RevertListener(this));
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout());
        buttonPanel.add(pass);
        buttonPanel.add(revert);
        pass.setPreferredSize(new Dimension(250, 100));
        revert.setPreferredSize(new Dimension(250, 100));
        
        statusLbl.setBorder(BorderFactory.createTitledBorder(BorderFactory .createLineBorder(Color.BLACK), "Status"));
        statusLbl.setPreferredSize(new Dimension(450, 50));
        
        infoPanel.setLayout(new GridLayout(3,1,3,10));
        infoPanel.add(p1Panel);
        infoPanel.add(p2Panel);
        infoPanel.add(buttonPanel);
        infoPanel.setPreferredSize(new Dimension(250, 450));

        this.setTitle("GO");
        this.setLayout(new BorderLayout(0,0));
        
        this.add(boardPanel, BorderLayout.CENTER);
        this.add(infoPanel, BorderLayout.EAST);
        this.add(statusLbl, BorderLayout.SOUTH);
        infoPanel.setBounds(450, 0, 600, 450);
        
        
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.pack();
        this.setResizable(false);
        this.setVisible(true);
        this.repaint();
    }
    
    public void setStatus(String pStatus){
        statusLbl.setText(pStatus);
    }
        
    public void drawBoard(){
    	boardPanel.repaint();
    }

    @Override
	public void repaint() {
		statusLbl.setText(board.getStatus());
		if (board.isGameOver()) {
			pass.setEnabled(false);
			revert.setEnabled(false);
			if (board.getPlayer1().getPoints() > board.getPlayer2().getPoints()) {
				p1Panel.setBackground(Color.GREEN);
			} else if (board.getPlayer2().getPoints() > board.getPlayer1().getPoints()) {
				p2Panel.setBackground(Color.GREEN);
			}
		} else {
			revert.setEnabled(board.canUndo());
		}
		
    	super.repaint();
		p1Panel.repaint();
		p2Panel.repaint();
	}
    
    private class GoCanMoveListener implements MouseMotionListener{
    	
    	private GoPanel parent;
    	
    	private GoCanMoveListener(GoPanel pParent) {
    		parent = pParent;
    	}
    	
		@Override
		public void mouseDragged(MouseEvent evt) {
			// TODO Auto-generated method stub
		}

		@Override
		public void mouseMoved(MouseEvent evt) {
			int x = evt.getX() / GoPanel.FIELD_SIZE;
			int y = evt.getY() / GoPanel.FIELD_SIZE;
			if (x != drawX || y != drawY) {
				drawX = x;
				drawY = y;
				parent.setCursor(x, y);	
				parent.repaint();
			}
		}
    	
    }
    
	public class GoCanListener implements MouseListener{
        private GoPanel parent;
                
        public GoCanListener(GoPanel pParent){
            parent = pParent;
        }
        
        
        @Override
        public void mouseClicked(MouseEvent evt) {}
        @Override
        public void mouseEntered(MouseEvent arg0) {}
        @Override
        public void mouseExited(MouseEvent arg0) {
        	drawX = -1;        	
        	drawY = -1;        	
        	parent.setCursor(-1, -1);	
			parent.repaint();
        }
        @Override
        public void mousePressed(MouseEvent evt) {
        	int cx;
        	int cy;
        	cx = evt.getX() / GoPanel.FIELD_SIZE;
        	cy = evt.getY() / GoPanel.FIELD_SIZE;
            board.playerActionSetStone(cx, cy);
            parent.getTopLevelAncestor().repaint();
        }
        @Override
        public void mouseReleased(MouseEvent evt) {}
    }

    public class PassListener implements ActionListener{

    	private GoGui parent;
    	
    	public PassListener(GoGui pParent){
    		parent = pParent;
    	}
    	
    	@Override
        public void actionPerformed(ActionEvent evt) {
            board.playerActionPassMove();
            if (board.isGameOver()) {
            	parent.boardPanel.removeMouseMotionListener(parent.boardPanel.getMouseMotionListeners()[0]);
            	parent.boardPanel.removeMouseListener(parent.boardPanel.getMouseListeners()[0]);
            }
            parent.repaint();
        }
    }
    
    public class RevertListener implements ActionListener{
    	
    	private GoGui parent;
    	
    	public RevertListener(GoGui pParent){
    		parent = pParent;
    	}
    	
        @Override
        public void actionPerformed(ActionEvent e) {
            board.playerActionUndoMove();
            parent.repaint();
        }
    }
}
