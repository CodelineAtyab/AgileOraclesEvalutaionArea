package com.agileoracleseval.slitheringeval.aljolanda_alhandhali_MyEvalProject;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MoveSnake {

    // Global variables
    static final String file_name = "src/main/java/com/agileoracleseval/slitheringeval/aljolanda_alhandhali_MyEvalProject/map.txt"; // file name
    static int rows, cols; // number of rows and columns
    static char[][] grid; //2D array (- and o)
    static LinkedList<int[]> snake = new LinkedList<>(); // snake body

    public static void main(String[] args) {

        // Check the number of input
        if (args.length < 1 || args.length > 2) {
            System.out.println("Error: java MoveSnake <direction> <steps>");
            return;
        }

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

        // Reading the trend
        String direction = args[0].toLowerCase();

        // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
        // Read the number of steps (default = 1)
        int steps = 1; // default
        if (args.length == 2) {
            try {
                steps = Integer.parseInt(args[1]);
            }
            catch (NumberFormatException e) {
                System.out.println("Steps must be a number!");
                return;
            }
        }

        // Input validation
        if (!isValidateDirection(direction) || steps <= 0) {
            System.out.println("Invalid input");
            return;
        }

        loadMap(); // Load the map from the file
        extractSnake(); // Extract snake locations from the map

        printSnake();

        boolean moved = moveSnake(direction, steps);

        if (!moved) {
            printAvailableMoves();
            return;
        }

        saveMap();

        printMap(); // Display the map


    }

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Load the map
    static void loadMap() {
        try {
            List<String[]> lines = new ArrayList<>();
            BufferedReader read_file = new BufferedReader(new FileReader(file_name));
            String line;

            // read each line
            while ((line = read_file.readLine()) != null) {
                lines.add(line.split(" "));
            }
            read_file.close();

            // Determining dimension
            rows = lines.size(); // all rows in file
            cols = lines.get(0).length; // length of first line = number of columns
            grid = new char[rows][cols]; // 2D matrix

            // Filling the matrix
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    grid[i][j] = lines.get(i)[j].charAt(0);
                }
            }

            // Check Map size
            if (rows < 15 || cols < 15) {
                System.out.println("Map must be at least 15x15");
                System.exit(0);
            }
        }
        catch (IOException e) {
            System.out.println("Error loading file!");
        }
    }

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Extract the snake
    static void extractSnake() {

        snake.clear();

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (grid[i][j] == 'o') {
                    snake.add(new int[]{i, j});
                }
            }
        }
    }

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Executing the movement
    static boolean moveSnake(String direction, int steps) {
        for (int s = 0; s < steps; s++) {

            int[] head = snake.getLast();
            int[] tail = snake.getFirst();

            int new_row = head[0];
            int new_col = head[1];

            if (direction.equals("up")) {
                new_row--;
            }
            else if (direction.equals("down")) {
                new_row++;
            }
            else if (direction.equals("left")) {
                new_col--;
            }
            else if (direction.equals("right")) {
                new_col++;
            }

            // Border exit
            if (new_row < 0 || new_row >= rows || new_col < 0 || new_col >= cols) {
                return false;
            }

            // Checks self-collision
            if (grid[new_row][new_col] == 'o') {
                if (!(new_row == tail[0] && new_col == tail[1])) {
                    return false;
                }
            }

            // Adding the new heads
            snake.addLast(new int[]{new_row, new_col});
            grid[new_row][new_col] = 'o';

            // Delete the tail
            int[] removedTail = snake.removeFirst();
            grid[removedTail[0]][removedTail[1]] = '-';

        }
        return true;
    }

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Save the map
    static void saveMap() {
        try {
            BufferedWriter write_file = new BufferedWriter(new FileWriter(file_name));
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols; j++) {
                    write_file.write(grid[i][j]);
                    if (j < cols - 1) {
                        write_file.write(" ");
                    }
                }
                write_file.newLine();
            }
            write_file.close();
        }
        catch (IOException e){
            System.out.println("Error saving file!");
        }
    }

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Display tha map to the file
    static void printMap() {
        for (char[] row : grid) {
            for (char c : row) {
                System.out.print(c + " ");
            }
            System.out.println();
        }
    }

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Validate the direction
    static boolean isValidateDirection (String dir) {
        return dir.equals("up") || dir.equals("down") || dir.equals("left") || dir.equals("right");
    }

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Display available Moves
    static void printAvailableMoves() {
        List<String> validMoves = new ArrayList<>();

        int[] head = snake.getLast();
        int r = head[0];
        int c = head[1];

        if (isSafe(r - 1, c)) {
            validMoves.add("up");
        }
        if (isSafe(r + 1, c)) {
            validMoves.add("down");
        }
        if (isSafe(r, c - 1)) {
            validMoves.add("left");
        }
        if (isSafe(r, c + 1)) {
            validMoves.add("right");
        }

        System.out.println("The only open directions are " + String.join(", ", validMoves));
    }

    // +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
    // Check if the place is safe
    static boolean isSafe(int r, int c) {
        return r >= 0 && r < rows && c >= 0 && c < cols && grid[r][c] != 'o';
    }

    static void printSnake() {
        System.out.println("Snake positions:");
        for (int[] part : snake) {
            System.out.println("(" + part[0] + ", " + part[1] + ")");
        }
    }
}