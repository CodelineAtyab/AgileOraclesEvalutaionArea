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
        Path path = Paths.get("src/main/java/com/agileoracleseval/slitheringeval/khadijaAlloughain/map.txt");

        try {
            if (!Files.exists(path)) {
                throw new IOException("map.txt not found! Please create the file first.");
            }

            System.out.println("\nInitial Map:");

            for (int r = 0; r < 15; r++) {
                for (int c = 0; c < 15; c++) {
                    if (r == 7) {
                        if (c == 5) {
                            System.out.print("& "); // tail -->(7,5)
                        } else if (c >= 6 && c <= 8) {
                            System.out.print("o "); // snake body
                        } else if (c == 9) {
                            System.out.print("@ "); // head --> (7,9)
                        } else {
                            System.out.print("- ");
                        }
                    } else {
                        System.out.print("- ");
                    }
                }
                System.out.println();
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
            // 1. create temp array
            List<int[]> bodyParts = new ArrayList<>();
            int[] headPos = null;

            // 2. save last position in temp arraylist
            for (int r = 0; r < 15; r++) {
                for (int c = 0; c < 15; c++) {
                    if (maps[r][c] == 'o' || maps[r][c] == '&') {
                        bodyParts.add(new int[]{r, c}); // put body and tail here
                    } else if (maps[r][c] == '@') {
                        headPos = new int[]{r, c}; // save head here in a lone array to make the movement always from it
                    }
                }
            }

            snake.clear();
            snake.addAll(bodyParts); // first pos for tail because read the map start from left to right
            if (headPos != null) {
                snake.addLast(headPos); // for head
            }

            System.out.println("\nPrevious Map:");
            printMap(maps);

            // 4. Movement logic
            boolean collision = false;
            for (int i = 0; i < steps; i++) {
                int[] head = snake.getLast();//---> the head (the newest part of the snake)
                int newRow = head[0];
                int newCol = head[1];

                // Direction:
                switch (direction) {
                    case "up":    newRow--; break;
                    case "down":  newRow++; break;
                    case "left":  newCol--; break;
                    case "right": newCol++; break;
                }

                // Wrap-around ---> %50 Bouns
                if (newRow < 0) newRow = 14;
                if (newRow >= 15) newRow = 0;
                if (newCol < 0) newCol = 14;
                if (newCol >= 15) newCol = 0;

                // Collision detection
                if (maps[newRow][newCol] == 'o' || maps[newRow][newCol] == '@' || maps[newRow][newCol] == '&') {
                    System.out.println("GAME OVER! Hit body at: " + newRow + "," + newCol);
                    collision = true;
                    break;
                }

                // Update Snake Position
                snake.addLast(new int[]{newRow, newCol});//new head
                int[] tail = snake.removeFirst();
                maps[tail[0]][tail[1]] = '-';

                // Snake body:
                for (int j = 0; j < snake.size(); j++) {
                    int[] part = snake.get(j);
                    if (j == 0) {// first element inside in  snake body
                        maps[part[0]][part[1]] = '&'; // tail
                    } else if (j == snake.size() - 1) { // size = 5 ---> 5-1 = 4 last index in the row which mean the head (7,9).
                        maps[part[0]][part[1]] = '@'; // head
                    } else {
                        maps[part[0]][part[1]] = 'o'; // middle body
                    }
                }
            }

            if (!collision) {
                System.out.println("\nMovement complete.");

                System.out.println("\nFinal Map:");
                printMap(maps);
            }

            //  Save the updated map
            StringBuilder finalMapData = new StringBuilder();
            for (int i = 0; i < 15; i++) {
                for (int j = 0; j < 15; j++) {
                    finalMapData.append(maps[i][j]).append(" ");// add space between - -
                }
                finalMapData.append("\n");// New line at the end of every row
            }

            Files.writeString(path, finalMapData.toString());
            //System.out.println("\nMap updated successfully in map.txt!");

        } catch (IOException e) {
            System.out.println("Error: " + e.getMessage());// show specific
            // problem that caused the program to stop at that moment.
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