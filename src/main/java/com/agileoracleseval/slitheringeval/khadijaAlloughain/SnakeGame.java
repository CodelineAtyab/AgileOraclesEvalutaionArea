package com.agileoracleseval.slitheringeval.khadijaAlloughain;

import java.util.*;
import java.nio.file.*;
import java.io.IOException;

public class SnakeGame {

    public static void main(String[] args) {

        // Accept only 2 arguments
        if (args.length != 2) {
            System.out.println("<direction> <steps>");
            return;
        }

        String direction = args[0].toLowerCase();
        int steps;

        // Check valid steps 1- positive 2- Integer
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

        // Check valid direction
        if (!(direction.equals("up") || direction.equals("down") ||
                direction.equals("left") || direction.equals("right"))) {
            System.out.println("Invalid direction! Use: up, down, left, or right.");
            return;
        }

        // Initialize map and snake - Fixed map size
        char[][] maps = new char[15][15];
        LinkedList<int[]> snake = new LinkedList<>();

        // Reading From File
        Path pathFile = Paths.get("map.txt");
        List<String> lines = new ArrayList<>();
        try {
            lines = Files.readAllLines(pathFile);
        } catch (IOException e) {
            System.out.println("Cannot read map.txt. Using empty map.");
        }

        // Fill map array
        for (int i = 0; i < 15; i++) {
            Arrays.fill(maps[i], '-');
            if (i < lines.size()) {
                String line = lines.get(i);
                for (int j = 0; j < Math.min(line.length(), 15); j++) {
                    maps[i][j] = line.charAt(j);
                }
            }
        }


        // Clear old snake positions
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                if (maps[i][j] == 'o') maps[i][j] = '-';
            }
        }

        // Initialize snake size = 5
        snake.clear();
        for (int j = 5; j <= 9; j++) {
            maps[7][j] = 'o';
            snake.add(new int[]{7, j});
        }

        System.out.println("\nInitial Map:\n");
        printMap(maps);

        // Move snake
        for (int i = 0; i < steps; i++) {
            int[] head = snake.getLast();
            int newRow = head[0];
            int newCol = head[1];

            switch (direction) {
                case
                        "up": newRow--;
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

            // Wrap-around ---> The Bouns 50%
            if (newRow < 0) newRow = 14;
            if (newRow >= 15) newRow = 0;
            if (newCol < 0) newCol = 14;
            if (newCol >= 15) newCol = 0;

            // Add new head
            snake.addLast(new int[]{newRow, newCol});
            maps[newRow][newCol] = 'o';

            // Remove tail to keep size = 5
            if (snake.size() > 5) {
                int[] tail = snake.removeFirst();
                maps[tail[0]][tail[1]] = '-';
            }
        }

        System.out.println("\nFinal Map:");
        printMap(maps);

        int[] head = snake.getLast();
        System.out.println("Player Head Location: Row " + head[0] + ", Column " + head[1]);

        // Saving The New Movement
        try {
            List<String> output = new ArrayList<>();
            for (int i = 0; i < 15; i++) {
                StringBuilder row = new StringBuilder();
                for (int j = 0; j < 15; j++) {
                    row.append(maps[i][j]);
                }
                output.add(row.toString());
            }
            Files.write(pathFile, output);
            System.out.println("\nMap updated successfully!");
        } catch (IOException e) {
            System.out.println("Error saving map: " + e.getMessage());
        }
        // End Saving
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
