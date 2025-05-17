package com.example.ai_project;

import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;

public class HelloController {


    @FXML private GridPane Grid;
    @FXML private TextField cols;
    @FXML private TextField rows;
    @FXML private Pane grassPane;
    @FXML private Pane obstaclePane;
    @FXML private Pane waterPane;
    @FXML private TextField xCoordinateField;
    @FXML private TextField yCoordinateField;
    @FXML private TextField elevationField;
    @FXML private TextField pathField;
    private Maze maze;
    private Tile startTile = null;
    private Tile endTile = null;
    private final Perceptron perceptron = new Perceptron(0.1);

    @FXML
    public void initialize() {
        setupDraggablePane(grassPane, Tile.TileType.GRASS);
        setupDraggablePane(waterPane, Tile.TileType.WATER);
        setupDraggablePane(obstaclePane, Tile.TileType.OBSTACLE);
    }

    @FXML
    private void handleSetSize() {
        try {
            int numRows = Integer.parseInt(rows.getText());
            int numCols = Integer.parseInt(cols.getText());
            if (numRows < 1 || numCols < 1) return;

            maze = new Maze(numRows, numCols);
            setupGrid(numRows, numCols);

            for (int row = 0; row < numRows; row++) {
                for (int col = 0; col < numCols; col++) {
                    Tile.TileType type = randomTileType();
                    int elevation = (int) (Math.random() * 11);
                    Tile tile = new Tile(row, col, type, elevation);
                    maze.setTile(row, col, tile);
                }
            }
            calculateObstacleDistances(maze);
            displayMaze();
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter valid integers.");
        }
    }

    private void setupGrid(int numRows, int numCols) {
        Grid.getChildren().clear();
        Grid.getRowConstraints().clear();
        Grid.getColumnConstraints().clear();

        for (int i = 0; i < numCols; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / numCols);
            cc.setHalignment(HPos.CENTER);
            Grid.getColumnConstraints().add(cc);
        }

        for (int i = 0; i < numRows; i++) {
            RowConstraints rc = new RowConstraints();
            rc.setPercentHeight(100.0 / numRows);
            rc.setValignment(VPos.CENTER);
            Grid.getRowConstraints().add(rc);
        }
    }

    private Tile.TileType randomTileType() {
        double r = Math.random();
        if (r < 0.7) return Tile.TileType.GRASS;
        else if (r < 0.9) return Tile.TileType.WATER;
        else return Tile.TileType.OBSTACLE;
    }

    private void displayMaze() {
        Grid.getChildren().clear();
        for (int row = 0; row < maze.getRows(); row++) {
            for (int col = 0; col < maze.getCols(); col++) {
                Tile tile = maze.getTile(row, col);
                StackPane cell = new StackPane();
                cell.setStyle("-fx-border-color: black;" + getTileStyle(tile));
                cell.setPrefSize(40, 40);

                // Clickable to show coordinates and elevation
                cell.setOnMouseClicked(e -> {
                    xCoordinateField.setText(String.valueOf(tile.getCol()));
                    yCoordinateField.setText(String.valueOf(tile.getRow()));
                    elevationField.setText(String.valueOf(tile.getElevation()));
                });

                Grid.add(cell, col, row);

                cell.setOnDragOver(event -> {
                    if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    }
                    event.consume();
                });

                cell.setOnDragDropped(event -> {
                    Dragboard db = event.getDragboard();
                    boolean success = false;
                    if (db.hasString()) {
                        String typeName = db.getString();
                        try {
                            Tile.TileType newType = Tile.TileType.valueOf(typeName);

                            tile.setType(newType);
                            tile.setSafe(false);
                            displayMaze();
                            success = true;
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                    event.setDropCompleted(success);
                    event.consume();

                });
            }
        }
    }

    private String getTileStyle(Tile tile) {
        String color = switch (tile.getType()) {
            case GRASS -> tile.isSafe() ? "lightgreen" : "darkgreen";
            case WATER -> tile.isSafe() ? "skyblue" : "blue";
            case OBSTACLE -> "gray";
        };
        return "-fx-background-color: " + color + ";";
    }

    private void calculateObstacleDistances(Maze maze) {
        for (int row = 0; row < maze.getRows(); row++) {
            for (int col = 0; col < maze.getCols(); col++) {
                Tile tile = maze.getTile(row, col);
                int minDistance = Integer.MAX_VALUE;

                for (int r = 0; r < maze.getRows(); r++) {
                    for (int c = 0; c < maze.getCols(); c++) {
                        Tile other = maze.getTile(r, c);
                        if (other.getType() == Tile.TileType.OBSTACLE) {
                            int dist = Math.abs(r - row) + Math.abs(c - col);
                            minDistance = Math.min(minDistance, dist);
                        }
                    }
                }
                tile.setDistanceToNearestObstacle(minDistance);
            }
        }
    }

    private void classifyTiles(Maze maze, Perceptron perceptron) {
        for (int row = 0; row < maze.getRows(); row++) {
            for (int col = 0; col < maze.getCols(); col++) {
                Tile tile = maze.getTile(row, col);
                if (tile.getType() != Tile.TileType.OBSTACLE) {
                    int tileType = (tile.getType() == Tile.TileType.GRASS) ? 0 : 1;
                    int elevation = tile.getElevation();
                    int distance = tile.getDistanceToNearestObstacle();
                    int result = perceptron.Predict(tileType, elevation, distance);
                    tile.setSafe(result == 1);
                } else {
                    tile.setSafe(false);
                }
            }
        }
    }

    @FXML
    private void handleSetStart() {
        try {
            int x = Integer.parseInt(xCoordinateField.getText());
            int y = Integer.parseInt(yCoordinateField.getText());

            Tile newStartTile = maze.getTile(y, x);

            // Reset old start tile
            if (startTile != null) {
                resetTileStyle(startTile);
            }

            // Assign and mark new start tile
            startTile = newStartTile;
            updateTileLabel(startTile, "Start", "yellow");

        } catch (Exception e) {
            System.out.println("Invalid coordinates for start tile.");
        }
    }

    @FXML
    private void handleSetEnd() {
        try {
            int x = Integer.parseInt(xCoordinateField.getText());
            int y = Integer.parseInt(yCoordinateField.getText());

            Tile newEndTile = maze.getTile(y, x);

            if (endTile != null) {
                resetTileStyle(endTile);
            }

            endTile = newEndTile;
            updateTileLabel(endTile, "End", "yellow");

        } catch (Exception e) {
            System.out.println("Invalid coordinates for end tile.");
        }
    }
    private void resetTileStyle(Tile tile) {
        StackPane cell = new StackPane();
        cell.setStyle("-fx-border-color: black;" + getTileStyle(tile));
        cell.setPrefSize(40, 40);

        // Re-add click handler to keep it interactive
        cell.setOnMouseClicked(e -> {
            xCoordinateField.setText(String.valueOf(tile.getCol()));
            yCoordinateField.setText(String.valueOf(tile.getRow()));
            elevationField.setText(String.valueOf(tile.getElevation()));
        });

        Grid.add(cell, tile.getCol(), tile.getRow());
    }


    @FXML
    private void handleSetElevation() {
        try {
            int x = Integer.parseInt(xCoordinateField.getText());
            int y = Integer.parseInt(yCoordinateField.getText());
            int elevation = Integer.parseInt(elevationField.getText());
            Tile tile = maze.getTile(y, x);
            tile.setElevation(elevation);
            System.out.println("Elevation set to " + elevation);
        } catch (Exception e) {
            System.out.println("Failed to set elevation.");
        }
    }

    @FXML
    private void handleTrainPerceptron() {
        try {
            String path = pathField.getText();
            path = "src/main/resources/com/example/ai_project/" + path;
            perceptron.train(path, 100);
            classifyTiles(maze, perceptron);
            displayMaze();
            System.out.println("Perceptron trained and maze updated.");
        } catch (Exception e) {
            System.out.println("Error training perceptron: " + e.getMessage());
        }
    }

    private void updateTileLabel(Tile tile, String labelText, String bgColor) {
        StackPane cell = new StackPane();
        cell.setStyle("-fx-background-color: " + bgColor + "; -fx-border-color: black;");
        Label label = new Label(labelText);
        cell.getChildren().add(label);
        Grid.add(cell, tile.getCol(), tile.getRow());
    }


    private void setupDraggablePane(Pane pane, Tile.TileType type) {
        pane.setOnDragDetected(event -> {
            Dragboard db = pane.startDragAndDrop(TransferMode.COPY);
            ClipboardContent content = new ClipboardContent();
            content.putString(type.name()); // Pass the tile type as string
            db.setContent(content);
            event.consume();
        });
    }


    private final java.util.List<Tile> highlightedPathTiles = new java.util.ArrayList<>();

    @FXML
    private void handleFindPath() {
        if (maze == null || startTile == null || endTile == null) {
            System.out.println("Maze, start tile, or end tile is not set.");
            return;
        }
        resetHighlightedTiles();
        classifyTiles(maze, perceptron);

        java.util.List<Tile> path = AStarPathfinder.findPath(maze,perceptron, startTile.getRow(), startTile.getCol(), endTile.getRow(), endTile.getCol());

        if (path == null) {
            System.out.println("No safe path found.");
        } else {
            // Redraw path on the grid
            for (Tile tile : path) {
                if (tile != startTile && tile != endTile) {
                    StackPane cell = new StackPane();
                    cell.setStyle(getTileStyle(tile) + "-fx-background-color: orange;");
                    Grid.add(cell, tile.getCol(), tile.getRow());

                    highlightedPathTiles.add(tile);
                }
            }
            System.out.println("Path found and highlighted.");
        }
    }

    private void resetHighlightedTiles() {
        for (Tile tile : highlightedPathTiles) {
            updateCell(tile); // force redraw the original safe/unsafe style
        }
        highlightedPathTiles.clear();
    }



    private void updateCell(Tile tile) {
        // Remove existing node at (col, row) if any
        Grid.getChildren().removeIf(node ->
                Grid.getColumnIndex(node) != null &&
                        Grid.getRowIndex(node) != null &&
                        Grid.getColumnIndex(node) == tile.getCol() &&
                        Grid.getRowIndex(node) == tile.getRow()
        );

        StackPane cell = new StackPane();
        cell.setPrefSize(40, 40);
        cell.setStyle("-fx-border-color: black;" + getTileStyle(tile));

        cell.setOnMouseClicked(e -> {
            xCoordinateField.setText(String.valueOf(tile.getCol()));
            yCoordinateField.setText(String.valueOf(tile.getRow()));
            elevationField.setText(String.valueOf(tile.getElevation()));
        });

        Grid.add(cell, tile.getCol(), tile.getRow());
    }




}
