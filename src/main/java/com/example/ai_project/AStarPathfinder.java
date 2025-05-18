package com.example.ai_project;

import java.util.*;

public class AStarPathfinder {

    static class Node implements Comparable<Node> {
        int row, col;
        int g;
        int h;
        Node parent;

        public Node(int row, int col, int g, int h, Node parent) {
            this.row = row;
            this.col = col;
            this.g = g;
            this.h = h;
            this.parent = parent;
        }

        public int f() {
            return g + h;
        }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.f(), other.f());
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Node other) {
                return this.row == other.row && this.col == other.col;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return Objects.hash(row, col);
        }
    }

    public static List<Tile> findPath(Maze maze, Perceptron perceptron, int startRow, int startCol, int endRow, int endCol) {
        maze.classifyTiles(perceptron);



        int rows = maze.getRows();
        int cols = maze.getCols();

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<String> closedSet = new HashSet<>();

        Node startNode = new Node(startRow, startCol, 0, manhattan(startRow, startCol, endRow, endCol), null);
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            if (current.row == endRow && current.col == endCol) {
                printPath(current);
                return reconstructPath(current, maze);
            }

            closedSet.add(current.row + "," + current.col);

            for (int[] dir : new int[][]{{0,1},{1,0},{0,-1},{-1,0}}) {
                int newRow = current.row + dir[0];
                int newCol = current.col + dir[1];

                if (!inBounds(newRow, newCol, rows, cols)) continue;
                if (closedSet.contains(newRow + "," + newCol)) continue;

                Tile tile = maze.getTile(newRow, newCol);
                if (!tile.isSafe()) continue;

                int g = current.g + 1;
                int h = manhattan(newRow, newCol, endRow, endCol);
                Node neighbor = new Node(newRow, newCol, g, h, current);
                openSet.add(neighbor);
            }
        }

        System.out.println("No safe path found.");
        return null;
    }

    private static void printPath(Node node) {
        List<String> path = new ArrayList<>();
        while (node != null) {
            path.add("(" + node.row + "," + node.col + ")");
            node = node.parent;
        }
        Collections.reverse(path);
        System.out.println("Safe Path Found:");
        for (String step : path) {
            System.out.print(step + " -> ");
        }
        System.out.println("END");
    }

    private static boolean inBounds(int row, int col, int rows, int cols) {
        return row >= 0 && row < rows && col >= 0 && col < cols;
    }

    private static int manhattan(int r1, int c1, int r2, int c2) {
        return Math.abs(r1 - r2) + Math.abs(c1 - c2);
    }

    private static List<Tile> reconstructPath(Node node, Maze maze) {
        List<Tile> path = new ArrayList<>();
        while (node != null) {
            path.add(0, maze.getTile(node.row, node.col));
            node = node.parent;
        }
        return path;
    }

}