package models;

import java.util.Objects;

public class MapPoint {

    private final String id;   //Representa un punto (interseccion) del mapa y es el valor generico
    private int x;             //almacenado dentro de Graph<MapPoint>, guarda informacion propia del
    private int y;             //punto y las conexiones son responsabilidad de Graph.
    
    public MapPoint(String id, int x, int y) {
        this.id = id;
        this.x = x;
        this.y = y;
    }

    public String getId() {
        return id;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setX(int x) {
        this.x = x;
    }

    public void setY(int y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapPoint)) return false;
        MapPoint other = (MapPoint) o;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return id;
    }
    
}
