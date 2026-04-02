package com.agileoracleseval.slitheringeval.khadijaAlloughain;

import java.util.*;
import java.nio.file.*;
import java.io.IOException;

public class SnakeGame {

    public static void main(String[] args) {

        // Accept only 2 arguments
        if (args.length != 2) {
            System.out.println("Usage: java SnakeGame <direction> <steps>");
            return;
        }

        //Creat --> map file
        String mapFile = "map.txt";

        String direction = args[0].trim().toLowerCase();
        int steps;

        // Check Valid Input
        try {
            steps = Integer.parseInt(args[1].trim());
            if (steps <= 0) {
                System.out.println("Steps must be a positive integer!");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Steps must be a valid integer!");
            return;
        }

        // Check Valid direction
        if (!(direction.equals("up") ||
                direction.equals("down") ||
                direction.equals("left") ||
                direction.equals("right"))) {

            System.out.println("Invalid direction! Use: up, down, left, or right.");
            return;
        }
        //Array -->  Map size is validated to be at least 15x15
        char[][] maps = new char[15][15];
        // Use Linkedlist --> to draw the snake body for adding new head for moving and remove tail
        LinkedList<int[]> snake = new LinkedList<>();

        //Read --> map.txt file
        try {
            List<String> lines = Files.readAllLines(Path.of(mapFile));

            for (int i = 0; i < 15; i++) {
                Arrays.fill(maps[i], '-');

                if (i < lines.size()) {
                    String line = lines.get(i);

                    for (int j = 0; j < Math.min(line.length(), 15); j++) {
                        char ch = line.charAt(j);
                        maps[i][j] = ch;

                        if (ch == 'o') {
                            snake.add(new int[]{i, j});
                        }
                    }
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading map file: " + e.getMessage());

        }

        // Default snake if does not  found
        if (snake.isEmpty()) {
            for (int j = 5; j <= 9; j++) {
                maps[7][j] = 'o';
                snake.add(new int[]{7, j});
            }
        }

        // Print initial map
        System.out.println("\nInitial Map:\n");
        printMap(maps);

        // Move snake
        for (int i = 0; i < steps; i++) {

            int[] head = snake.getLast();
            int newRow = head[0];
            int newCol = head[1];

            // Move
            switch (direction) {
                case "up": newRow--;
                    break;
                case "down": newRow++;
                    break;
                case "left": newCol--;
                    break;
                case "right": newCol++;
                    break;
            }

            // Wrap-around --> I try to use to solve the Bouns Wrap-Around Movement (score x 50%)
            if (newRow < 0) newRow = 14;
            if (newRow >= 15) newRow = 0;
            if (newCol < 0) newCol = 14;
            if (newCol >= 15) newCol = 0;

            // Add new head
            snake.addLast(new int[]{newRow, newCol});
            maps[newRow][newCol] = 'o';

            // Remove tail replace -
            if (snake.size() > 5) {
                int[] tail = snake.removeFirst();
                maps[tail[0]][tail[1]] = '-';
            }
        }

        // Show final map
        System.out.println("\nFinal Map:\n");
        printMap(maps);

        int[] head = snake.getLast();
        System.out.println("\nPlayer Head Location: Row " + head[0] + ", Column " + head[1]);

        //Save map
        try {
            List<String> output = new ArrayList<>();

            for (int i = 0; i < 15; i++) {  // moving row by row
                StringBuilder row = new StringBuilder(); //build a string step-by-step
                for (int j = 0; j < 15; j++) {
                    row.append(maps[i][j]);
                }
                output.add(row.toString());
            }

            Files.write(Path.of(mapFile), output);

        } catch (IOException e) {
            System.out.println("Error saving map: " + e.getMessage());
        }
    }

    // Print map
    private static void printMap(char[][] maps) {
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                System.out.print(maps[i][j] + " ");
            }
            System.out.println();
        }
    }
}
