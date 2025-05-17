package com.example.ai_project;

public class Maze {
    private final int rows;
    private final int cols;
    private final Tile[][] grid;
    private Tile startTile;
    private Tile endTile;

    public Maze(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new Tile[rows][cols];
    }

    public void setTile(int row, int col, Tile tile) {
        grid[row][col] = tile;
    }

    public Tile getTile(int row, int col) {
        return grid[row][col];
    }

    public Tile[][] getGrid() {
        return grid;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public Tile getStartTile() {
        return startTile;
    }

    public void setStartTile(Tile startTile) {
        this.startTile = startTile;
    }

    public Tile getEndTile() {
        return endTile;
    }

    public void setEndTile(Tile endTile) {
        this.endTile = endTile;
    }

    public void classifyTiles(Perceptron perceptron) {
        for (int row = 0; row < getRows(); row++) {
            for (int col = 0; col < getCols(); col++) {
                Tile tile = getTile(row, col);

                if (tile.getType() == Tile.TileType.OBSTACLE) {
                    tile.setSafe(false);
                    continue;
                }

                int tileType = (tile.getType() == Tile.TileType.GRASS) ? 0 : 1;
                int elevation = tile.getElevation();
                int distance = tile.getDistanceToNearestObstacle();

                tile.setSafe(perceptron.Predict(tileType, elevation, distance) == 1);
            }
        }
    }

}
