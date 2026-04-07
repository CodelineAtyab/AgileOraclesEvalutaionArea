package com.agileoracleseval.slitheringeval.khadijaAlloughain;

import java.util.*;
import java.nio.file.*;
import java.io.IOException;

public class SnakeGame {

    public static void main(String[] args) {

        // 1. CLI arguments validation
        if (args.length != 2) {
            System.out.println(" Enter in CLI 1.direction 2.steps");
            return;
        }

        // 2. steps validation
        String direction = args[0].toLowerCase();
        int steps = 0;
        try {
            steps = Integer.parseInt(args[1].trim());
        } catch (NumberFormatException e) {
            System.out.println("Steps must be a positive integer!");
            return;
        }

        // 3. 2D -> map 15 * 15
        char[][] maps = new char[15][15];
        // 4. Linkedlist --> (FIFO): first in first out //addLast  //removefirst
        LinkedList<int[]> snake = new LinkedList<>();
        // path of map
        Path path = Paths.get("map.txt");

        try {
            if (!Files.exists(path)) {
                throw new IOException("map.txt not found! Please create the file first.");
            }

            // Read from text file map.txt
            List<String> lines = Files.readAllLines(path);

            // Load map from file into the 2D array
            for (int i = 0; i < 15; i++) {
                String[] cells;
                if (i < lines.size()) {
                    cells = lines.get(i).trim().split("\\s+");
                } else {
                    cells = new String[0];
                }
                for (int j = 0; j < 15; j++) {
                    if (j < cells.length && !cells[j].isEmpty()) {
                        maps[i][j] = cells[j].charAt(0);
                    } else {
                        maps[i][j] = '-';
                    }
                }
            }

            // Search about snake body location

            for (int r = 0; r < 15; r++) {
                for (int c = 0; c < 15; c++) {
                    if (maps[r][c] == 'o') {
                        snake.add(new int[]{r, c});
                    }
                }
            }

            System.out.println("\nInitial Map:");
            printMap(maps);

            // 4. Movement logic
            boolean collision = false;
            for (int i = 0; i < steps; i++) {
                int[] head = snake.getLast();
                int newRow = head[0];
                int newCol = head[1];

                switch (direction) {
                    case
                            "up":    newRow--;
                        break;
                    case
                            "down":  newRow++;
                        break;
                    case
                            "left":  newCol--;
                        break;
                    case
                            "right": newCol++;
                        break;
                }

                // Wrap-around ---> %50 Bouns
                if (newRow < 0) newRow = 14;
                if (newRow >= 15) newRow = 0;
                if (newCol < 0) newCol = 14;
                if (newCol >= 15) newCol = 0;

                // Collision detection
                if (maps[newRow][newCol] == 'o') {
                    System.out.println("GAME OVER! Hit body at: " + newRow + "," + newCol);
                    collision = true;
                    break;
                }

                // Update Snake Position
                snake.addLast(new int[]{newRow, newCol});
                maps[newRow][newCol] = 'o';
                int[] tail = snake.removeFirst();
                maps[tail[0]][tail[1]] = '-';
            }

            if (!collision) {
                System.out.println("\nMovement complete.");
            }

            System.out.println("\nFinal Map:");
            printMap(maps);

            //  Save the updated map
            StringBuilder finalMapData = new StringBuilder();
            for (int i = 0; i < 15; i++) {
                for (int j = 0; j < 15; j++) {
                    finalMapData.append(maps[i][j]).append(" ");// add space between - -
                }
                finalMapData.append("\n");// New line at the end of every row
            }

            Files.writeString(path, finalMapData.toString());
            System.out.println("\nMap updated successfully in map.txt!");

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());// show specific cause of the problem that caused the program to stop at that moment.

        }
    }

    private static void printMap(char[][] maps) {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                System.out.print(maps[i][j] + " ");
            }
            System.out.println();
        }
    }
}