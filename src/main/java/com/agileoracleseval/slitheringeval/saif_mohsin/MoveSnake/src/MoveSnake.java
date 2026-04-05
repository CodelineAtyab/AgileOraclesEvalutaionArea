import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

public class MoveSnake {
    // Store snake segment
    static class Position {
        int row;
        int col;

        // Reads the grid as coordinate positions / Stores snake position
        Position(int row, int col) {
            this.row = row;
            this.col = col;
        }

    }

    public static void main(String[] args) {

        // CLI INPUT VALIDATION
        try {
            if (args.length == 0) {
                System.out.println("Please provide movement option: up, down, left, right");
                return;
            }

            String direction = args[0].toLowerCase();
            int steps = 1; // default movement
            if (args.length > 1) {
                steps = Integer.parseInt(args[1]);
            }

            // LOAD FILE
            // Using path path for easier reading and syntax understanding. BufferedReader <or> Scanner scanner
            Path path = Path.of("src/map.txt");
            List<String> lines = Files.readAllLines(path);

            char[][] grid = new char[lines.size()][]; //Convert the file into 2D Array / Creates Grid

            for (int row = 0; row < lines.size(); row++) {
                //BREAK LINES INTO COLUMNS
                String line = lines.get(row);
                String[] parts = line.split(" ");
                grid[row] = new char[parts.length];

                for (int col = 0; col < parts.length; col++) {
                    grid[row][col] = parts[col].charAt(0); //Convert string to char
                }
            }

            // Creates linkedlist to store snake segment
            LinkedList<Position> snake = new LinkedList<>();

            //Scan the entire grid and collect all snake positions.
            for (int row = 0; row < grid.length; row++) {
                for (int col = 0; col < grid[row].length; col++) {
                    if (grid[row][col] == 'o') {
                        snake.add(new Position(row, col));
                    }
                }
            }

            // PRINT THE SNAKE POSITION COORDINATES
            System.out.println("Snake position:");
            for (Position part : snake) {
                System.out.println("(" + part.row + ", " + part.col + ")");
            }

            // PRINT GRIDS INSTEAD OF LINES AFTER SCANNING
            System.out.println("Position on grid:");
            for (int row = 0; row < grid.length; row++) {
                for (int col = 0; col < grid[row].length; col++) {
                    System.out.print(grid[row][col] + " ");

                }
                System.out.println();
            }

            // PRINT THE SNAKE POSITION
//            System.out.println("Snake positions:");
//            for (Position part : snake) {
//                System.out.println("(" + part.row + ", " + part.col + ")");


            // MOVE SNAKE
//            if (args.length == 0) {
//                System.out.println("Please provide movement option: up, down, left, right");
//                return;
//            }
//
//            // CLI input
//            String direction = args[0].toLowerCase();
//            int steps = 1; // default movement
//            if (args.length > 1) {
//                steps = Integer.parseInt(args[1]);
//            }

            for (int i = 0; i < steps; i++) {
                Position head = snake.getLast();
                int newRow = head.row;
                int newCol = head.col;

                if (direction.equals("up")) {
                    newRow--;
                } else if (direction.equals("down")) {
                    newRow++;
                } else if (direction.equals("left")) {
                    newCol--;
                } else if (direction.equals("right")) {
                    newCol++;
                }


                snake.addLast(new Position(newRow, newCol)); // Adds new head
                snake.removeFirst(); // Removes Old tail

                for (Position part : snake) {
                    System.out.println("(" + part.row + ", " + part.col + ")");
                }
            }

                // Clear the grid
                for (int row = 0; row < grid.length; row++) {
                    for (int col = 0; col < grid[row].length; col++) {
                        grid[row][col] = '-';
                    }
                }

                // Draw updated snake position coordinates
                for (Position part : snake) {
                    grid[part.row][part.col] = 'o';
                }

                // Print the grid of updated snake position
                System.out.println("New Position on Grid:");
                for (int row = 0; row < grid.length; row++) {
                    for (int col = 0; col < grid[row].length; col++) {
                        System.out.print(grid[row][col] + " ");
                    }
                    System.out.println(" ");
                }

            } catch(IOException e){
                System.out.println("Error reading the file");
            }
        }
    }

