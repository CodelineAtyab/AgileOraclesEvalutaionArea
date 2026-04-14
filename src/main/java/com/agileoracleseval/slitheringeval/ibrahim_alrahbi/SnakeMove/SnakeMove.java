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
            return;
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

        ArrayList<String[]> rowsData = new ArrayList<>();
        // [ADDED] A variable to store the snake order line if it exists in the file
        String snakeOrderLine = null;

        File mapFile = new File("src/main/java/com/agileoracleseval/slitheringeval/ibrahim_alrahbi/SnakeMove/map");
        System.out.println(mapFile.getAbsolutePath());

        try {
            Scanner fileReader = new Scanner(mapFile);

            //  looping as long as there is another line in the file
            while (fileReader.hasNextLine()) {
                //  removes extra spaces at the beginning and end of the line
                String line = fileReader.nextLine().trim();
                // This checks if line is not empty
                if (!line.isEmpty()) {
                    // [ADDED] Check if this line is the snake order line (starts with "(")
                    // If yes, save it separately instead of adding it to the map rows
                    if (line.startsWith("(")) {
                        snakeOrderLine = line;
                    } else {
                        String[] parts = line.split(" "); // Split lines by spaces; ease to access
                        rowsData.add(parts);
                    }
                }
            }
            fileReader.close();

        } catch (FileNotFoundException e) {
            System.out.println("Error: map.txt file not found.");
            return;
        }


        //  Part 2: Validate map.txt content & Create 2D Array
        int rows = rowsData.size();
        if (rows == 0) {
            System.out.println("Error: Map file is empty.");
            return;
        }

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
                map[row][col] = rowsData.get(row)[col].charAt(0);
            }
        }
        //  Printing the grid map.txt

        System.out.println("\nThe map:");
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                System.out.print(map[row][col] + " ");
            }
            System.out.println();
        }


        //  Locating the Snake in the Grid
        LinkedList<int[]> snake = new LinkedList<>();

        // [ADDED] If the snake order line exists, read the snake order from it
        // This gives us the correct tail-to-head order for any snake shape
        if (snakeOrderLine != null) {
            // [ADDED] Each point looks like (row,col) and they are separated by spaces
            // For example: (2,3) (2,4) (2,5)
            String[] points = snakeOrderLine.split(" ");
            for (String point : points) {
                // [ADDED] Remove the "(" and ")" and split by ","
                point = point.replace("(", "").replace(")", "");
                String[] snakeLocation = point.split(",");
                int r = Integer.parseInt(snakeLocation[0].trim());
                int c = Integer.parseInt(snakeLocation[1].trim());
                snake.add(new int[]{r, c});
            }

        } else {
            // [ADDED] First time running: no order line exists yet, so fall back to
            // the original scan method to at least find the snake segments
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < columns; col++) {
                    if (map[row][col] == 'o') {
                        snake.add(new int[]{row, col});
                    }
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
        BufferedWriter fileWrite = new BufferedWriter(new FileWriter(
                "src/main/java/com/agileoracleseval/slitheringeval/ibrahim_alrahbi/SnakeMove/map"));

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                fileWrite.write(map[row][col] + " ");
            }
            fileWrite.newLine();
        }

        // [ADDED] After saving the map, write the updated snake order on the extra line
        // This way, the next run will read the correct order from this line
        StringBuilder snakeOrderBuilder = new StringBuilder();
        for (int i = 0; i < snake.size(); i++) {
            int[] segment = snake.get(i);
            snakeOrderBuilder.append("(").append(segment[0]).append(",").append(segment[1]).append(")");
            // [ADDED] Add a space between points but not after the last one
            if (i < snake.size() - 1) {
                snakeOrderBuilder.append(" ");
            }
        }
        fileWrite.write(snakeOrderBuilder.toString());
        fileWrite.newLine();
        // [ADDED] End of snake order line writing

        fileWrite.close();

        //  Print map.txt
        System.out.println("\nUpdated Map:");
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                System.out.print(map[row][col] + " ");
            }
            System.out.println();
        }

        // [ADDED] Print the snake order so the user can see it in the console too
        System.out.println("Snake order (tail to head): " + snakeOrderBuilder.toString());
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