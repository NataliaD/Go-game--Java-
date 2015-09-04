package go.logic;

public class Player {
	
    final static String[] COLOR = {"Schwarz", "Weiss", "Leer"};
    
    private String name;
    private int id; // Reihenfolge des Spieles für Arrayzugriffe, Spieler
    
    // Felder fuer die Punkte des Spielers
    private int prisoners = 0;
    private int area = 0;
    
    public Player(int pId, String pName){
        name = pName;
        id   = pId;
    }
    
    public String getName(){
        return name;
    }
    
    public int getOrd() {
    	return id;
    }
    
    public String getColor(){
        return COLOR[id];
    }
    
    public int getPoints(){
        return prisoners + area;
    }
    
    public int getPrisoners(){
        return prisoners;
    }

    public void setPrisoners(int pPrisoners){
        prisoners = pPrisoners;
    }
    
    public int getArea(){
        return area;
    }
    
    public void setArea(int pArea){
        area = pArea;
    }
    
}
