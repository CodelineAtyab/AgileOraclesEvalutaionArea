package com.agileoracleseval.slitheringeval.fromAbdullahHosni.SnakeMove;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class SnakeGame {
    public static void main(String[] args) {

        //--------------------------------------------
        // STEP 1 - TAKING INPUT .CMD COMMAND LINE .
        //--------------------------------------------
        try {
            if (args.length > 0) {
                for (String input : args) {
                    System.out.println(input);
                }
            }
        } catch (Exception e) {
            System.out.println("Invalid Direction");
        }

        //--------------------------------------------
        // STEP 2 - Loading 2d Array Game board .
        //--------------------------------------------
        Path boardPath = null;
        char array2d[][] = null;

        try {
            boardPath = Path.of("./src/main/java/com.agileoracleseval/slitheringeval/fromAbdullahHosni/SnakeMove/board.txt");
        } catch (Exception e) {
            System.out.println("Cannot load Game");
            throw new RuntimeException(e);
        }

        try {
            String pathContent = Files.readString(boardPath);
            String[] splitContent = pathContent.split("\n");
            int SplitLength = splitContent[0].length();

            array2d = new char[splitContent.length][SplitLength];  // Load it in 2D Array or Array of Arrays

            for (int row = 0; row < splitContent.length; row++) {
                char[] currRow = splitContent[row].toCharArray();
                // System.out.printf("%s\n", linesOfFile[row]);

                for (int col = 0; col < currRow.length; col++) {
                    array2d[row][col] = currRow[col];
                }
            }

        } catch (Exception e) {
            System.out.println("CANNOT BOOT GAME BOARD!");
            throw new RuntimeException(e);
        }

        //print the board rows and columns
        for (int row = 0; row < array2d.length; row++) {
            for (int col = 0; col < array2d[0].length; col++) {
                System.out.printf("%s", array2d[row][col]); //2d array is printing
            }
            System.out.println();
        }

        //--------------------------------------------
        // STEP 3 - Building the snake in game.
        //--------------------------------------------
        Deque<int[]> snakeBody = new LinkedList<>();       //Queue to represent the snake

        Path resumeGamePath = null;

        try {

            resumeGamePath = Path.of("./src/main/java/com.agileoracleseval/slitheringeval/fromAbdullahHosni/SnakeMove/snakePos.txt");

            if (Files.exists(resumeGamePath) && Files.size(resumeGamePath) > 0) {
                System.out.println("LOADING PREVIUSE GAME");

                List<String> savedGame = Files.readAllLines(resumeGamePath);

                for (String line : savedGame) {
                    String[] progress = line.split(",");

                    int rowProgress = Integer.parseInt(progress[0]);
                    int colProgress = Integer.parseInt(progress[1]);
                    snakeBody.add(new int[]{rowProgress, colProgress});
                }

                // CLEAN the 2D array: Remove any default "o" characters
                for (int row = 0; row < array2d.length; row++) {
                    for (int col = 0; col < array2d[0].length; col++) {

                        if (array2d[row][col] == 'o') {
                            array2d[row][col] = '-';
                        }
                    }

                }

                for (int[] line : snakeBody) {
                    array2d[line[0]][line[1]] = 'o';

                }
            } else {
                // DEFAULT game (start new game , restart game)
                for (int row = 0; row < array2d.length; row++) {
                    for (int col = 0; col < array2d[0].length; col++) {

                        if (array2d[row][col] == 'o') {
                            snakeBody.add(new int[]{row, col});  //Enqueing/saving all snake body in {snakebody} queue.
                        }
                    }
                }

            }
        } catch (Exception e) {
            System.out.println("Error syncing snake positions");
            throw new RuntimeException(e);
        }

//        snakeBody.add(new int[]{7,5});
//        snakeBody.add(new int[]{7,6});
//        snakeBody.add(new int[]{7,8});
//        snakeBody.add(new int[]{7,9});


//        System.out.println(" Starting Game now!");
//        for (int[] p : snakeBody){
//            System.out.println(Arrays.toString(p) + " ");
//
//        }

        //--------------------------------------------
        // STEP 4 - snake movement direction .
        //--------------------------------------------
        int rowDirection = 0;
        int colDirection = 0;       //declaring the direction delta {up,down, left ,right )
        String direction = "down";
//                args[0].toLowerCase();

        //direction delta
        if (direction.equals("up")) {
            rowDirection = -1;      //go up of the col
        }
        if (direction.equals("down")) {
            rowDirection = 1;       //go DOWN int the row
        }
        if (direction.equals("left")) {
            colDirection = -1;      //go LEFT of the col
        }
        if (direction.equals("right")) {
            colDirection = 1;       //go right of the col
        }


        int steps = 2;

        for (int loop = 0; loop < steps; loop++) {
            //the move
            int[] currentHead = snakeBody.peekLast();   //head element on queue. {0,1} == {row, col}
            int movementRow = currentHead[0] + rowDirection;
            int movementCol = currentHead[1] + colDirection;


            int[] newhead = new int[]{movementRow, movementCol};
            snakeBody.add(newhead);
            array2d[newhead[0]][newhead[1]] = 'o';


            int[] oldtail = snakeBody.poll();
            array2d[oldtail[0]][oldtail[1]] = '-';




//        array2d[oldtail[0]][oldtail[1]] = '-';
//
//        array2d[newhead[0]][newhead[1]] = 'o';


        }


        //print the board rows and columns
        for (int row = 0; row < array2d.length; row++) {
            for (int col = 0; col < array2d[0].length; col++) {
                System.out.printf("%s", array2d[row][col]); //2d array is printing
            }
            System.out.println();
        }

        //--------------------------------------------
        // STEP 5 - saving game Progress in .txt .
        //--------------------------------------------
        Path positionSnake = null;

        try {
            positionSnake = Path.of("./src/main/java/com.agileoracleseval/slitheringeval/fromAbdullahHosni/SnakeMove/snakePos.txt");

            if (Files.notExists(positionSnake.getParent())) {
                Files.createDirectories(positionSnake.getParent());
            }

            StringBuilder positionData = new StringBuilder();

            for (int[] loop : snakeBody) {
                positionData.append(loop[0]).append(",").append(loop[1]).append("\n");
                ;
            }

            Files.writeString(positionSnake, positionData.toString());

        } catch (Exception e) {
            System.out.println("Error saving the Snake position .txt file");
            throw new RuntimeException(e);
        }

    }
}
