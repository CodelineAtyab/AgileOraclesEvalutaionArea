package com.agileoracleseval.slitheringeval.ahed;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;

public class SnakeGame {
    public static void main(String[] args) {
        // load the area into 2D char array
        char[][] area = loadAndGetArea();
        //check the arg if its just 2
        if (args.length != 2) {
            System.out.println("command used: java SnakeGame.java <direction> <Step> ");
            return;
        }
        //define the arg
        String direction = args[0];
        int step = Integer.parseInt(args[1]);

        //for the movement of directions
        int directionRow = 0;
        int directionCol = 0;
        if (direction.equals("up")) {
            directionRow = -1;
        } else if (direction.equals("Down")) {
            System.out.println("Only open direction up,left.right");
            return;
        } else if (direction.equals("left")) {
            directionCol = -1;
        } else if (direction.equals("right")) {
            directionCol = 1;
        } else {
            System.out.println("Invalid direction");
            return;
        }
        //used linkedList for snack body
        LinkedList<int[]> snake = findSnake(area);
        if (snake.isEmpty()) {
            System.out.println("No snake find!");
            return;
        }
        //before move
        System.out.println("Game before move ");
        displayArea(area);
        //current head(rightmost) positions
        int[] CurrRightmost = snake.getLast();

        //movement for steps:
        for (int i = 0; i < step; i++) {
            int nextRow = CurrRightmost[0] + directionRow;
            int nextCol = CurrRightmost[1] + directionCol;

            CurrRightmost = moveSnake(area, snake, CurrRightmost, new int[]{nextRow, nextCol});
        }
        //after move
        System.out.println("Game after move ");
        displayArea(area);

        //save updated area to file
        saveArea(area);
        //print lines for spices
        printEmptyLines();
    }

    //move snake from CurrRightmost to nextRightmost
    public static int[] moveSnake(char[][] area, LinkedList<int[]> snake, int[] CurrRightmost, int[] nextRightmost) {
        int rowRightmost = nextRightmost[0];
        int colRightmost = nextRightmost[1];

        //wall collision
        if (rowRightmost < 0 || rowRightmost >= 15 || colRightmost < 0 || colRightmost >= 15) {
            System.out.println("wall collision ");
            return CurrRightmost;
        }

        //self collision
        for (int i = 0; i < snake.size(); i++) {
            int[] part = snake.get(i);

            if (part[0] == rowRightmost && part[1] == colRightmost) {
                System.out.println("self collision ");
                return CurrRightmost;
            }
        }
        //remove tail(leftmost)
        int[] leftmost = snake.removeFirst();
        area[leftmost[0]][leftmost[1]] = '-';

        //add new head(rightmost)
        snake.addLast(CurrRightmost);
        area[rowRightmost][colRightmost] = 'o';
        return nextRightmost;
    }

    public static LinkedList<int[]> findSnake(char[][] area) {
        LinkedList<int[]> snake = new LinkedList<>();
        for (int rowRightmost = 0; rowRightmost < 15; rowRightmost++) {
            for (int colRightmost = 0; colRightmost < 15; colRightmost++) {
                if (area[rowRightmost][colRightmost] == 'o') {
                    snake.add(new int[]{rowRightmost, colRightmost});
                }
            }
        }
        return snake;
    }

    public static void displayArea(char[][] area) {
        for (int rowRightmost = 0; rowRightmost < 15; rowRightmost++) {
            for (int colRightmost = 0; colRightmost < 15; colRightmost++) {
                System.out.print(area[rowRightmost][colRightmost]);
                if (colRightmost < 15 - 1) System.out.print(" ");
            }
            System.out.println();
        }
    }

    public static char[][] loadAndGetArea() {
        char[][] area = new char[15][15];
        try {
            String fileContent = Files.readString(Path.of(relativePath));
            String[] lines = fileContent.split("\n");

            for (int rowRightmost = 0; rowRightmost < 15; rowRightmost++) {
                String[] cells = lines[rowRightmost].trim().split(" ");
                for (int colRightmost = 0; colRightmost < 15; colRightmost++) {
                    area[rowRightmost][colRightmost] = cells[colRightmost].charAt(0);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return area;
    }

    public static void saveArea(char[][] area) {
        try (FileWriter fw = new FileWriter(relativePath)) {
            for (int rowRightmost = 0; rowRightmost < 15; rowRightmost++) {
                for (int colRightmost = 0; colRightmost < 15; colRightmost++) {
                    fw.write(area[rowRightmost][colRightmost]);
                    if (colRightmost < 15 - 1) fw.write(' ');
                }
                fw.write('\n');
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void printEmptyLines() {
        for (int count = 0; count < 18; count++) {
            System.out.println();
        }
    }

    public static final String relativePath = "src/main/resources/Game.txt";
}



