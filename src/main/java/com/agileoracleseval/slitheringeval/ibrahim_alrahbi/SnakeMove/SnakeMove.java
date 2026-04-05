package com.agileoracleseval.slitheringeval.ibrahim_alrahbi.SnakeMove;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;


public class SnakeMove {
    public static void main(String[] argumentsInput) throws IOException {

        //  Part 1. Validate CLI user input

        //  Validate Arguments
        if (argumentsInput.length == 0) {
            System.out.println("Error: Must run via command line with: java MoveSnake.java <direction> <steps> ");
            return; //  Stop the program
        }

        if (argumentsInput.length > 2) {
            System.out.println("Error: Input Must Be java MoveSnake <direction> <steps>");
            return;
        }

        //  Validate Direction
        String direction = argumentsInput[0].toLowerCase(); // So That it Can Read Any Letter
        if (!direction.equals("up") &&
                !direction.equals("down") &&
                !direction.equals("left") &&
                !direction.equals("right")) {
            System.out.println("Error: Invalid Direction.");
            return;
        }

        //  Validate Steps
        int steps = 1; // Default if User Don't Specify
        if (argumentsInput.length == 2) {
            try {
                steps = Integer.parseInt(argumentsInput[1]);

                if (steps <= 0) {
                    System.out.println("Error: Steps Must be a Positive Integer.");
                    return;
                }

            } catch (NumberFormatException e) {
                System.out.println("Error: Steps Must be a Valid Integer.");
                return;
            }
        }
        //  Printing Direction and Steps
        System.out.println("Direction: " + direction);
        System.out.println("Steps: \n" + steps);


        //  Part 2. Read file and split each row immediately

        //  Reading the file & Splitting
        ArrayList<String[]> rowsData = new ArrayList<>();
        File mapFile = new File("src/main/java/com/agileoracleseval/slitheringeval/ibrahim_alrahbi/SnakeMove/map");
        System.out.println(new File("src/main/java/com/agileoracleseval/slitheringeval/ibrahim_alrahbi/SnakeMove/map").getAbsolutePath());

        try {
            Scanner fileReader = new Scanner(mapFile);

            //  looping as long as there is another line in the file
            while (fileReader.hasNextLine()) {
                //  removes extra spaces at the beginning and end of the line
                String line = fileReader.nextLine().trim();

                // This checks if line is not empty.
                if (!line.isEmpty()) {
                    String[] parts = line.split(" "); // Split lines by spaces; ease to access
                    rowsData.add(parts);
                }
            }
            fileReader.close();

        } catch (FileNotFoundException e) {
            System.out.println("Error: map.txt file not found.");
            return;
        }


        //  Part 2: Validate map.txt content & Create 2D Array
        //  Rows
        int rows = rowsData.size();
        if (rows == 0) {
            System.out.println("Error: Map file is empty.");
            return;
        }

        //  Columns
        int columns = rowsData.get(0).length;

        //  Check if n*n & n>=15
        if (rows < 15 || columns < 15) {
            System.out.println("Error: Map must be at least 15x15.");
            return;
        }

        for (int i = 0; i < rows; i++) {
            if (rowsData.get(i).length != columns) {
                System.out.println("Error: Invalid map.txt format.");
                return;
            }
        }

        //  Validate symbols - & o if there are another symbols
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                String cell = rowsData.get(row)[col];
                if (!cell.equals("-") && !cell.equals("o")) {
                    System.out.println("Error: Invalid symbol found in the map.txt.");
                    return;
                }
            }
        }

        //  Create 2D Array & filling
        char[][] map = new char[rows][columns];
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                map[row][col] = rowsData.get(row)[col].charAt(0); //
            }
        }

        //  Printing the grid map.txt
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                System.out.print(map[row][col] + " ");
            }
            System.out.println();
        }


        //  Locating the Snake in the Grid
        LinkedList<int[]> snake = new LinkedList<>();

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                if (map[row][col] == 'o') {
                    snake.add(new int[]{row, col});
                }
            }
        }
        if (snake.isEmpty()) {
            System.out.println("No snake found in map.txt!");
            return;
        }

        //  Part.3 Moving the Snake
        // Repeating Number of Steps Entered
        for (int step = 0; step < steps; step++) {
            //  Get the Last Element in the LinkedList
            int[] head = snake.getLast();
            //  Copy the current Head Position Into Row and Column
            int newRow = head[0];
            int newCol = head[1];

            if (direction.equals("up")) {
                newRow--;
            } else if (direction.equals("down")) {
                newRow++;
            } else if (direction.equals("left")) {
                newCol--;
            } else if (direction.equals("right")) {
                newCol++;
            }

            //  If Outside the map
            if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= columns) {
                System.out.println("Invalid move! Out of boundaries.");
                printValidDirections(head, map);
                return;
            }

            //  Collision
            if (map[newRow][newCol] == 'o') {
                System.out.println("Collision! Snake hit itself.");
                printValidDirections(head, map);
                return;
            }

            // Moving: Add Head & Remove Tail
            snake.addLast(new int[]{newRow, newCol});
            int[] tail = snake.removeFirst();
            map[newRow][newCol] = 'o';
            map[tail[0]][tail[1]] = '-';
        }

        //  Part.4 Save map.txt to file & Print it
        BufferedWriter fileWrite = new BufferedWriter(new FileWriter("src/main/java/com/agileoracleseval/slitheringeval/ibrahim_alrahbi/SnakeMove/map"));
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                fileWrite.write(map[row][col] + " ");
            }
            fileWrite.newLine();
        }
        fileWrite.close();

        //  Print map.txt
        System.out.println("Updated Map:");
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                System.out.print(map[row][col] + " ");
            }
            System.out.println();
        }
    }

    //  Validate directions (Inside Map or Not Part of Snake)
    public static void printValidDirections(int[] head, char[][] map) {
        int row = head[0];
        int col = head[1];
        int rows = map.length;
        int columns = map[0].length;

        System.out.print("Valid directions: ");

        if (row > 0 && map[row - 1][col] != 'o') System.out.print("up ");
        if (row < rows - 1 && map[row + 1][col] != 'o') System.out.print("down ");
        if (col > 0 && map[row][col - 1] != 'o') System.out.print("left ");
        if (col < columns - 1 && map[row][col + 1] != 'o') System.out.print("right ");

        System.out.println();
    }
}