package com.example.ai_project;

public class Tile {
    public enum TileType {GRASS, WATER, OBSTACLE}
    private final int row;
    private final int col;
    private TileType type;
    private int elevation;
    private int distanceToNearestObstacle;
    private boolean isSafe;
    public Tile(int row, int col, TileType type, int elevation) {
        this.row = row;
        this.col = col;
        this.type = type;
        this.elevation = elevation;
    }
    public int getRow() { return row; }
    public int getCol() { return col; }
    public TileType getType() { return type; }
    public void setType(TileType newType) {this.type = newType;}
    public int getElevation() { return elevation; }
    public void setElevation(int elevation) {
        this.elevation = elevation;
    }
    public int getDistanceToNearestObstacle() { return distanceToNearestObstacle; }
    public void setDistanceToNearestObstacle(int distance) {
        this.distanceToNearestObstacle = distance;
    }
    public boolean isSafe() { return isSafe; }
    public void setSafe(boolean safe) { this.isSafe = safe; }
}
