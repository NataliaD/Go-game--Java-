package go.logic;
import java.util.LinkedList;
import java.util.Stack;

import javax.swing.JOptionPane;

public class GameBoard{
    
    public final static int SIZE = 9; //Brettgroesse
    Stone[][] stones = new Stone[SIZE][SIZE]; // Das eigentliche Spielfeld
    boolean lastPlayerPassed = false; // Zum merken, ob der letzte Zug ein "passen" war, da dann das naechste Passen zum Ende des Spiels fuehren muss
    boolean lastMoveUndo = false;//Merker ob ein schritt zurueck gemacht wurde
    boolean gameOver = false; // Status fuer beendetes Spiel (wird gesetzt, wenn zwei mal hintereinander gepsaat wurde)
    Player player1;  // "Schwarz"
    Player player2;  // "Weiss"
    Player noPlayer; // "Leer", fuer leere Felder
    Player activePlayer;  //Spieler der dran ist
    Player oppositePlayer;//Spieler, der gearde nicht dran ist
    String statusText = ""; //Ausgabetext fuer aktuelle Spielsituation
    
    //hier werden die geprueften Steine von inneren Farbe abgelegt, um die doppelte Pruefung zu vermeiden
    //und die Anzahl der Steine in der Liste geben die Groesse der Region an
    private LinkedList<Stone> checkedStones = new LinkedList<Stone>();
    private Stack<GameState> undoStack = new Stack<GameState>();
    
    /**
     * Ein neues Spielbrett und 2 Spieler werden erzeugt
     */
    public GameBoard(){

    	// Spielernamen erfragen
        while (player1 == null || player1.getName().length() < 1) {
	        player1 = new Player(0, JOptionPane.showInputDialog("Geben Sie den Namen fuer Spieler " + Player.COLOR[0] + " ein: "));
        }
        while (player2 == null || player2.getName().length() < 1) {
	        player2 = new Player(1, JOptionPane.showInputDialog("Geben Sie den Namen fuer Spieler " + Player.COLOR[1] + " ein: "));
        }
        
        // Brett initialisieren mit "leeren" Steinen
        noPlayer = new Player(2, "");
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                stones[x][y] = new Stone(noPlayer, x, y);
            }//for x
        }//for y
        // Startspieler setzen
        switchPlayer();
    }//Konstruktor GameBoard

    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Spieleraktionen ////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Die Funktion dient als Schnittstelle nach Aussen.
     * Um einen Stein zu setzen, wird diese Funktion aufgerufen und prueft die Gueltigkeit des Zuges.
     * Ermittelt nach dem Zug den naechsten Spieler
     * @param x Koordinate vom gesetzten Stein
     * @param y Koordinate vom gesetzten Stein
     * @return wenn der Zug erfolgreich war, wird "true" zurueckgegeben
     */
    public boolean playerActionSetStone(int x, int y){
        boolean resu = false;
        // Wenn das Spiel schon vorbei ist, darf nichts mehr geaendert werden.
        if (!gameOver) {
        	// Der Stein muss auf einem Platz innerhalb des Feldes platziert werden
        	if (x < 0 || x >= SIZE || y < 0 || y >= SIZE) {
        		statusText = "Kann keinen Stein ausserhalb des Spielbretts platzieren!";
        	} else {
        		// Der Platz, an den der Stein gelegt wird, muss noch frei sein
        	    if (stones[x][y].getPlayer() != noPlayer){
        	    	statusText = "Kann keinen Stein auf bereits vorhandenem Stein platzieren!";
        	    } else {
        	    	// Bisherigen Spielstand auf dem Undo-Stack ablegen
	                pushState();
        	    	// Den Stein aufs brett legen
    	            stones[x][y] = new Stone(activePlayer, x, y);
    	            
	                // Mit diesem Zug entstandene Gefangene auswerten (Steine werden vom Brett genommen und Punkte aktialisiert)
	                evaluatePrisoners(x, y);
    	            // Auf Selbstmord-Zug pruefen
    	            resu = verifyNoSuicide(x, y);
    	            if (resu == false) {
    	            	// Es wurde ein Selbstmord-Zug identifiziert, Fehler ausgeben
    	            	statusText = "Selbstmord ist verboten."; 
    	            } else {
	    	            // Auf "Ko" pruefen
		            	resu = verifyNoKoSituation();
		            	if (resu == false) {
		            		// Ko_situation identifiziert, Fehler ausgeben 
		            		statusText = "Ko-Situation! Wiederholung des vorherigen Zuges ist unzulaessig.";
		            	}
    	            }//else  if Ko  	            
    	            if (resu == true) {
    	                // Falls die Aktion als gueltig bewertet wurde, jetzt noch die kontrollierten Flaechen der beiden Spieler neu berechnen,
    	            	// vermerken, dass der letzte Zug kein "Passen" war, den Statustext zuruecksetzen und den naechsten Spieler bestimmen.
    	                calcAreas();
    	                lastPlayerPassed = false;
    	                lastMoveUndo = false;
        	            statusText = "";
	            		switchPlayer(); // Nach diesem Zug ist der naechste Spieler dran
    	            } else {
        	            // Falls ein Fehler aufgetreten ist, Zug zurueck nehmen
    	            	popState();
    	            }
        	    } // Platz noch frei?
            } // Platz liegt auf dem Spielfeld?
        } // Spiel noch nicht vorbei?
        return resu;
    }//setStone
    
    /**
     * Letzten Zug zurueck nehmen
     */
    public void playerActionUndoMove(){
    	if (!gameOver && !lastMoveUndo && popState()) {
        	statusText = "Zug rueckgaengig gemacht";
        	lastMoveUndo = true;
    	}
    }
    
    /**
     * Aktueller Spieler verzichtet auf einen Zug, Gegner erhaelt dafuer einen Gefangenen
     * @return true, wenn das Spiel durch zweimaligen passen beendet ist.
     */
    public boolean playerActionPassMove(){
    	if (!gameOver) {
    		// Beim Passen soll der Gegner einen Gefangenen erhalten. Das entspricht nicht den normalen Regeln, soll aber hier wohl so sein!
	    	oppositePlayer.setPrisoners(oppositePlayer.getPrisoners() + 1);
            lastMoveUndo = false;
    		if (lastPlayerPassed) {
        		// Wenn vorher schon gepasst wurde und jetzt wieder, dann ist das Spiel hier zuende.
        		gameOver = true;
        		Player winner = (player1.getPoints() > player2.getPoints()) ? player1 : (player2.getPoints() > player1.getPoints()) ? player2 : null; 
        		Player loser = (winner == player1) ? player2 : player1;
    	        statusText =  "Das Spiel ist beendet. " + ((winner == null) ? "Es steht unentschieden!" : winner.getName() + " hat mit einem Punktestand von " 
    	        		+ winner.getPoints() + ":" + loser.getPoints() + " gewonnen!");
        	} else {
        		// Das aktuelle passen ist das erste. Es muss nur vermerkt werden, dass das Spiel zuende ist, wenn der naechste Spieler auch passt.  
    	        lastPlayerPassed = true;   
    	        statusText = activePlayer.getName() + " hat gepasst! Wenn " + oppositePlayer.getName() + " jetzt auch passt, ist das Spiel beendet.";
    	    	switchPlayer();
        	}
    	}
    	return gameOver;
    }
    
    
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Hilfsfunktionen ////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////
    
    /**
     * Kernfunktion der Pruefung von Nachbarschaftsbeziehungen!
     * 
     * Prueft, ob an der gegebenen Position eine Flaeche von Steinen eines Spielers liegt, die
     * vollstaendig von Steinen eines anderen Spielers eingeschlossen ist. Der Spieler kann dabei 
     * auch der "Leere" Spieler sein, also der Besitzer der leeren Felder.  
     * @param pX
     * @param pY
     * @param pInner  Spieler der eingeschlossenen Region
     * @param pOuter  Spieler der umgebenden Steine
     * @return true, falls (pX,pY) Teil einer Flaeche des Spielers pInner ist, die vollstaendig von Steinen des Spielers pOuter eingeschlossen ist. 
     */
    private boolean isSurrounded(int pX, int pY, Player pInner, Player pOuter){
        boolean result = true;
        // Falls der gepruefte Platz ausserhalb des Koordinatensystems liegt oder er bereits geprueft wurde,
        // dann soll das Gesamtergebnis nicht veraendert werden. Es muss dann true zurueckgegeben werden.
        if (pX >= 0 && pX < SIZE && pY >= 0 && pY < SIZE
            && !checkedStones.contains(stones[pX][pY])) {
            
        	// FALL 1: Stein gehoert dem Spieler der Randsteine. An dieser Stelle wird die innere Flaeche also von
        	//         der Umgebungsfarbe umrandet, Ergebnis true.
            if (stones[pX][pY].getPlayer() == pOuter) {
                result = true; 
            }
            // FALL 2: Nachbarstein gehoert dem Spieler der inneren Region.
            //         Die innere Region muss auf den gefundenen Stein ausgeweitet und dessen Randsteine wiederum geprueft werden.
            else if (stones[pX][pY].getPlayer() == pInner) {
                // Den gefundenen Stein in die Liste gepruefter Steine aufnehmen
            	checkedStones.add(stones[pX][pY]);
                // Weitere Randsteine an allen vier Raendern pruefen (Einfaches "&", damit alle vier Funktionsaufrufe erfolgen!)
                result =   isSurrounded(pX-1, pY,   pInner, pOuter)
                         & isSurrounded(pX+1, pY,   pInner, pOuter)
                         & isSurrounded(pX,   pY-1, pInner, pOuter)
                         & isSurrounded(pX,   pY+1, pInner, pOuter);
            }
            // FALL 3: Die ueberpruefte Stelle gehoert weder dem Spieler der ineren Region, noch dem der Umrandung.
        	// 		   Die gesamte bisher gepruefte Flaeche ist damit noch offen und NICHT vom Spieler pOuter eingeschlossen, 
            //         es muss als Ergebnis false ausgegeben werden.
            else {
                result = false;
            }            
        }
        return result;
    }
    
    /**
     * Zaehlt alle Steine der Region eines Spielers, die vollstaendig von Steinen eines anderen Spielers eingeschlossen sind. 
     * Falls die innere Region nicht vollstaendig von Steinen des aeusseren Spielers eingeschlossen ist, ist das Ergebnis 0.
     * @param pX
     * @param pY
     * @param pInner  Spieler der eingeschlossenen Region
     * @param pOuter  Spieler der umgebenden Steine
     * @return Anzahl der Steine des Spielers pInner, ausgehend von Position (pX, pY), die vollstaendig von Steinen des Spielers pOuter eingeschlossen sind.
     */
    private int surroundedStones(int pX, int pY, Player pInner, Player pOuter){
    	int result = 0;
    	checkedStones.clear();
    	// isSurrounded fuellt die Liste checkedStones mit allen Steinen der inneren Region. Die Anzahl der eingeschlossenen Steine
    	// ist also gleich der Anzahl der dort aufgelisteten Steine. 
    	// Gibt isSurrounded false zurueck, dann ist in der Umrandung mindestens eine Luecke und die Anzahl muss 0 bleiben. 
    	if (isSurrounded(pX, pY, pInner, pOuter)){
			result = checkedStones.size();
    	}
    	return result;
    }

    /**
     * Prueft, ob das Setzen eines Steins auf (pX,pY) durch dan activePlayer Selbstmord war. Der Stein an der
     * Stelle (pX,pY) muss bereits gesetzt sein, bevor diese Funktion aufgerufen wird!
     * @param pX 
     * @param pY
     * @return true, wenn das Setzen KEIN Selbstmord war.
     */
    private boolean verifyNoSuicide(int pX, int pY){    	
        return (surroundedStones(pX, pY, activePlayer, oppositePlayer) == 0);
    }
    
    /**
     * Identifiziert Ko-Situationen (Wiederholung des letzten Zugpaares)
     * @return true, wenn die aktuelle Situation KEINE ungueltige Ko-Wiederholung darstellt
     */
    private boolean verifyNoKoSituation(){
    	return (undoStack.size() < 2 || !undoStack.get(undoStack.size() - 2).equals(new GameState(this)));
    }
    
    /**
     * Bestimmt und entfernt alle Gefangenen, die durch Setzen eines Steins auf das Feld (pX, pY) entstanden sind,
     * und aktualisiert den Punktestand des aktiven Spielers entsprechend.
     * @param pX
     * @param pY
     */
    private void evaluatePrisoners(int pX, int pY){
        int numPrisoners = 0;//Anzahl der neuen Gefangenen
        checkedStones.clear();
        if (isSurrounded(pX-1, pY, oppositePlayer, activePlayer)){
            // Gegner ist an gepruefter Stelle eingesperrt, gefangene auswerten
            for (Stone s : checkedStones) {
                stones[s.getX()][s.getY()] = new Stone(noPlayer, s.getX(), s.getY());
                numPrisoners ++;
            }
        }
        checkedStones.clear();
        if (isSurrounded(pX+1, pY, oppositePlayer, activePlayer)){
            // Gegner ist an gepruefter Stelle eingesperrt, gefangene auswerten
            for (Stone s : checkedStones) {
                stones[s.getX()][s.getY()] = new Stone(noPlayer, s.getX(), s.getY());
                numPrisoners ++;
            }
        }
        checkedStones.clear();
        if (isSurrounded(pX, pY-1, oppositePlayer, activePlayer)){
            // Gegner ist an gepruefter Stelle eingesperrt, gefangene auswerten
            for (Stone s : checkedStones) {
                stones[s.getX()][s.getY()] = new Stone(noPlayer, s.getX(), s.getY());
                numPrisoners ++;
            }
        }
        checkedStones.clear();
        if (isSurrounded(pX, pY+1, oppositePlayer, activePlayer)){
            // Gegner ist an gepruefter Stelle eingesperrt, gefangene auswerten
            for (Stone s : checkedStones) {
                stones[s.getX()][s.getY()] = new Stone(noPlayer, s.getX(), s.getY());
                numPrisoners ++;
            }
        }
        activePlayer.setPrisoners(activePlayer.getPrisoners() + numPrisoners); 
    }//evaluatePrisoners()
    
    /**
     * Neuberechnung aller Regionen, die von den Spielern aktuell kontrolliert werden.
     */
    private void calcAreas(){
    	
    	LinkedList<Stone> emptyStones = new LinkedList<Stone>();
    	int numStonesP1 = 0;
    	int numStonesP2 = 0;
    	
    	// Zuerst alle leeren Felder in eine Liste aufnehmen. 
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
    			if (stones[x][y].besitzer == noPlayer) {
    				emptyStones.add(stones[x][y]);
    			}
    		}
		}
        
        // Alle leeren Felder jetzt ueberpruefen, zu welchem Spieler sie gehoeren. Nach dem Aufruf von surroundedStones() 
        // ist die Liste checkedStones mit allen Steinen gefuellt, die im Laufe der Pruefung des ersten Steins mit
        // geprueft wurden (alle Steine, die "adjacent" zu dem ersten Stein sind). Diese koennen direkt aus emptyStones
        // entfernt werden, da sie ja bereits geprueft wurden. Die Berechnung der Flaechen wird mit dem ersten Stein
        // des Rests der Liste fortgesetzt, bis alle freien Felder geprueft sind.
        while (emptyStones.size() > 0) {
      		Stone s = emptyStones.removeFirst();
        	numStonesP1 += surroundedStones(s.getX(), s.getY(), noPlayer, player1);
        	numStonesP2 += surroundedStones(s.getX(), s.getY(), noPlayer, player2);
        	emptyStones.removeAll(checkedStones);
		}
        
        // Die aktualisierten Werte den Spielern zuweisen
        player1.setArea(numStonesP1);
        player2.setArea(numStonesP2);
    }
    
    public Player getPlayer1() {
        return player1;
    }

    public Player getPlayer2() {
        return player2;
    }

    public Player getActivePlayer() {
        return activePlayer;
    }
    
    public Stone getStone(int pX, int pY) {
        return stones[pX][pY];
    }

    public String getStatus(){
    	return statusText;
    }

    public boolean canUndo() {
    	return (!lastMoveUndo);
    }
    
    public boolean isGameOver() {
    	return gameOver;
    }

    /**
     * Den naechsten aktiven Spieler nach Beendigung eines Zuges bestimmen
     */
    private void switchPlayer(){
        
        if(activePlayer == player1){
            activePlayer   = player2;
            oppositePlayer = player1;
        }else{
            activePlayer   = player1;
            oppositePlayer = player2;
        }
        
    }//switchPlayer

    /**
     * aktuellen Spielstand auf dem Undo-Stack speichern
     */
    private void pushState(){
    	undoStack.push(new GameState(this));
    	if (undoStack.size() > 3) {
    	    undoStack.remove(0);
    	}
    }
    
    /**
     * letzten Spielstand vom Undo-Stack wiederherstellen 
     */
    private boolean popState() {
    	boolean result = false;
    	if (undoStack.size() > 0) {
    		undoStack.pop().restore(this);
    		result = true;
    	}
    	return result;
    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////
    // Innere Klassen /////////////////////////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Klasse zur Abbildung des Spielzustands 
     *
     */
    private class GameState {
        private Player[][] owner = new Player[GameBoard.SIZE][GameBoard.SIZE];
        private int p1_area;
        private int p1_prisoners;
        private int p2_area;
        private int p2_prisoners;
        private Player activePlayer;
        private boolean lastPlayerPassed;
        
        /**
         * Erzeugt ein GameState Objekt, das alle relevanten Daten zum aktuellen Spielstand enthaelt
         * @param game
         */
        public GameState(GameBoard game){
        	p1_area = game.player1.getArea();
        	p1_prisoners = game.player1.getPrisoners();
        	p2_area = game.player2.getArea();
        	p2_prisoners = game.player2.getPrisoners();
        	activePlayer = game.activePlayer;
        	lastPlayerPassed = game.lastPlayerPassed;
        	for (int y = 0; y < GameBoard.SIZE; y++) {
            	for (int x = 0; x < GameBoard.SIZE; x++) {
        			owner[x][y] = game.stones[x][y].getPlayer();
        		}
    		}
        }//Konstruktor GameState
        
        /**
         * Den Zustand des GameState Objekts wieder auf die aktuelle Spielsituation uebertragen
         * Ein Schritt zurueck im Spiel
         * @param game
         */
        public void restore(GameBoard game){
        	game.player1.setArea(p1_area);
        	game.player1.setPrisoners(p1_prisoners);
        	game.player2.setArea(p2_area);
        	game.player2.setPrisoners(p2_prisoners);
        	game.activePlayer = activePlayer;
        	game.oppositePlayer = ((game.player1 == activePlayer) ? game.player2 : game.player1);
        	game.lastPlayerPassed = lastPlayerPassed;
        	for (int y = 0; y < GameBoard.SIZE; y++) {
            	for (int x = 0; x < GameBoard.SIZE; x++) {
        			game.stones[x][y] = new Stone(owner[x][y], x, y);
        		}
    		}    	
        }//restore()
        
        /**
         * Equals gibt genau dann true zurueck, wenn die Situation auf dem Brett bei beiden verglichenen Staenden identisch ist.
         * @param obj
         * @return
         */
    	@Override
    	public boolean equals(Object obj) {
    		boolean result = false;
    		if (obj instanceof GameState) {
    			GameState gs = (GameState)obj;
    			result = true;
    	    	for (int y = 0; y < GameBoard.SIZE && result; y++) {
    	        	for (int x = 0; x < GameBoard.SIZE && result; x++) {
    	    			if (this.owner[x][y] != gs.owner[x][y]) {
    	    				result = false;
    	    			}
    	    		}
    			}
    		}
    		return result;
    	}//equals
    }//GameState
}//GameBoard
