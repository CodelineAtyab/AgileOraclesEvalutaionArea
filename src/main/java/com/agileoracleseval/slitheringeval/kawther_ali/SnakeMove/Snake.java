package com.agileoracleseval.slitheringeval.kawther_ali.SnakeMove;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Snake {
    public static void main(String[] args) {
        // check if user provided the direction: :
        if (args.length < 1) {
            System.out.println("ERROR : pls provide direction and steps");
            return;
        }
        //read the direction and change to LowerCase to avoid status error:
        String moveDir = args[0].toLowerCase();
        //assign the num of steps that expect user inter :
        int moveCount = 1;
        if (args.length > 1) {
            try {
                moveCount = Integer.parseInt(args[1]);
              if (moveCount<=0){
                  System.out.println("ERROR: steps must be a positive integer");
                  return;
              }
            } catch (NumberFormatException e) {
                System.out.println("ERROR :the steps must be a positive integer");
                return;

            }
        }
        // check valid direction
        if (!moveDir.equals("up") && !moveDir.equals("down") &&
                !moveDir.equals("left") && !moveDir.equals("right")) {
            System.out.println("ERROR: invalid direction");
            return;
        }
        // msg to check the test successful get data:
        System.out.println("Testing part 1:");
        System.out.println("Direction received : " + moveDir);
        System.out.println("Steps received :" + moveCount);
        //upload map :
        try {
            char[][] grid = loadGrid();
            System.out.println("Grid loaded!");
            printGrid(grid);

//1-use arraylist to implement to num :
            ArrayList<String> snakeParts = new ArrayList<>();
            //2- save the place (o) search about etch place :
            for (int i = 0; i < grid.length; i++) {
                for (int j = 0; j < grid[0].length; j++) {
                    if (grid[i][j] == 'o') {
                        snakeParts.add(i + "," + j);

                    }
                }
            }
            // msg check test :
            System.out.println(" Snake found! Length:" + snakeParts.size() + "segment.");


            //check the snake is available first :
            if (!snakeParts.isEmpty()) {
                for (int i = 0; i < moveCount; i++) {
                    //Step 5:Snake movement logic

                    // 1- Get the current head position (last item in list)
                    String headPos = snakeParts.get(snakeParts.size() - 1);
                    String[] coords = headPos.split(",");
                    int headRow = Integer.parseInt(coords[0]);
                    int headCol = Integer.parseInt(coords[1]);

                    // 2- Calculate the next position based on direction
                    if (moveDir.equals("right"))
                        headCol++;
                    else if (moveDir.equals("left"))
                        headCol--;
                    else if (moveDir.equals("up"))
                        headRow--;
                    else if (moveDir.equals("down"))
                        headRow++;


                    // 3- Check for collision with walls or snake body
                    //if (headRow < 0 || headRow >= 15 || headCol < 0 || headCol >= 15 ) {
                    if (headRow < 0 || headRow >= grid.length ||
                            headCol < 0 || headCol >= grid[0].length) {
                        printAvailableDirections(grid, snakeParts);
                        return;
                    }
                    if (grid[headRow][headCol] == 'o') {
                        printAvailableDirections(grid, snakeParts);
                        return;
                    }
                    // 4- Actual movement update

                    // A. Draw new head on the grid and add it to the list
                    grid[headRow][headCol] = 'o';
                    snakeParts.add(headRow + "," + headCol);

                    // B. Remove the old tail from the list and the grid
                     String tailPos = snakeParts.remove(0);
                     String[] tailCoords = tailPos.split(",");
                     grid[Integer.parseInt(tailCoords[0])][Integer.parseInt(tailCoords[1])] = '-';

                }
                // End of move loop

                // 5- Print and save the map after all steps are done
                System.out.println("map after movement :");
                printGrid(grid);
                saveGrid(grid);
                System.out.println("Movement saved to map.txt");
            }


        } catch (IOException e) {
            System.out.println("ERROR: could not find map.txt");
        }
    }
    public static void printAvailableDirections(char[][] grid, ArrayList<String> snakeParts) {

        String headPos = snakeParts.get(snakeParts.size() - 1);
        String[] coords = headPos.split(",");
        int row = Integer.parseInt(coords[0]);
        int col = Integer.parseInt(coords[1]);

        ArrayList<String> openDirs = new ArrayList<>();

        if (row - 1 >= 0 && grid[row - 1][col] != 'o') openDirs.add("up");
        if (row + 1 < grid.length && grid[row + 1][col] != 'o') openDirs.add("down");
        if (col - 1 >= 0 && grid[row][col - 1] != 'o') openDirs.add("left");
        if (col + 1 < grid[0].length && grid[row][col + 1] != 'o') openDirs.add("right");

        System.out.println("The only open directions are: " + String.join(", ", openDirs));
    }
    //save the grid to file:
    public static void saveGrid(char[][] grid) throws IOException {
        Path myPath = Path.of("src/main/java/com/agileoracleseval/slitheringeval/kawther_ali/SnakeMove/map.txt");
        StringBuilder sb = new StringBuilder();
        for (char[] row : grid) {
            for (int j = 0; j < row.length; j++) {
                sb.append(row[j]).append(j == row.length - 1 ? "" : " ");
            }
            sb.append("\n");
        }
        Files.writeString(myPath, sb.toString());
    }

    //load grid from file :
    public static char[][] loadGrid() throws IOException {
        // my path:
        Path myPath = Path.of("src/main/java/com/agileoracleseval/slitheringeval/kawther_ali/SnakeMove/map.txt");
        List<String> lines = Files.readAllLines(myPath);
        int rows = lines.size();
        int cols = lines.get(0).replace(" ", "").length();
        char[][] grid = new char[rows][cols];

        for (int i = 0; i < rows; i++) {
            // read lines and delete the space and move it to array litters:
            grid[i] = lines.get(i).replace(" ", "").toCharArray();

        }
        return grid;
    }

    // print grid to console:
    public static void printGrid(char[][] grid) {
        for (char[] row : grid) {
            for (char cell : row) {
                System.out.print(cell +" ");

            }
            System.out.println();
        }
    }
}


