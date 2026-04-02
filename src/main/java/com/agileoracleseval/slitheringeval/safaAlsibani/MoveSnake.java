package com.agileoracleseval.slitheringeval.safaAlsibani;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Queue;

public class MoveSnake {
    public static void main(String[] args) throws Exception {

        Path file = Path.of("src/main/resources/map.txt");
        String data = Files.readString(file);
        String[] lines = data.split("\\R");

        char[][] grid = new char[lines.length][lines[0].length()];
        for (int i = 0; i < lines.length; i++) {
            grid[i] = lines[i].toCharArray();
        }

        // snake body
        Queue<int[]> snake = new LinkedList<>();

        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] == 'o') {
                    snake.add(new int[]{i, j});
                }
            }
        }

        // head
        int[] head = ((LinkedList<int[]>) snake).getLast();

        // input
        String direction = args[0];
        int steps = Integer.parseInt(args[1]);

        // check invalid direction
        if (direction.equals("down")) {
            System.out.println("The only open directions are up, left and right");
            return;
        }

        // the movement
        for (int s = 0; s < steps; s++) {

            int newRow = head[0];
            int newCol = head[1];

            if (direction.equals("right")) newCol++;
            else if (direction.equals("left")) newCol--;
            else if (direction.equals("up")) newRow--;

            // wall collision
            if (newRow < 0 || newRow >= grid.length ||
                    newCol < 0 || newCol >= grid[0].length) {
                System.out.println("Game Over - wall");
                return;
            }

            //  body collision
            for (int[] part : snake) {
                if (part[0] == newRow && part[1] == newCol) {
                    System.out.println("Game Over - Hit itself");
                    return;
                }
            }

            // update snake
            int[] tail = snake.peek();
            head = new int[]{newRow, newCol};

            snake.add(head);
            snake.remove();

            // update grid
            grid[tail[0]][tail[1]] = '-';
            grid[newRow][newCol] = 'o';

            // print
            printGrid(grid);
            System.out.println();
        }
    }

    public static void printGrid(char[][] grid) {
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                System.out.print(grid[i][j]);
            }
            System.out.println();
        }
    }
}

