package go;

import go.gui.GoGui;
import go.logic.GameBoard;

public class A2 {
    public static void main(String[] args){
    	
        GameBoard board = new GameBoard();
        new GoGui(board);
        
    }
}
