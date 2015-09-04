package go.logic;



public class Stone{

	Player besitzer;
    int x;
    int y;

	public Stone(Player player, int x, int y){
        
        this.besitzer = player;
        this.x = x;
        this.y = y;
        
    }
    
    public Player getPlayer() {
        return besitzer;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }    
    
    public boolean isEmpty(){
    	return (besitzer == null || besitzer.getOrd() == 2); // Spieler 2 ist per Definition "Leer"!
    }
}
