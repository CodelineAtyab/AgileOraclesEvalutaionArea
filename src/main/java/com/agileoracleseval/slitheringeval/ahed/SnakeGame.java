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
        int directionRow = 0;//up /down
        int directionCol = 0;//left / right

        //what user enter in the arg
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
            //1.calculate next positions:
            int nextRow = CurrRightmost[0] + directionRow;
            int nextCol = CurrRightmost[1] + directionCol;
            //this CurrRightmost functions return new head positions
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
        //notice row+col
        int row = nextRightmost[0];
        int col = nextRightmost[1];

        //2.wall collision
        if (row < 0 || row >= 15 || col < 0 || col >= 15) {
            System.out.println("wall collision ");
            return CurrRightmost;
        }

        //3.self collision
        //"i" here like counter of index go through parts of the snake
        for (int i = 0; i < snake.size(); i++) {
            int[] part = snake.get(i);

            if (part[0] == row && part[1] == col) {
                System.out.println("self collision ");
                return CurrRightmost;
            }
        }
        //4.remove tail(leftmost)
        int[] leftmost = snake.removeFirst();
        //update grid area, after remove tail replace with "-"
        area[leftmost[0]][leftmost[1]] = '-';

        //5.add new head(rightmost)
        snake.addLast(nextRightmost);
        area[row][col] = 'o';
        return nextRightmost;
    }
    /*
   in this method will scan area(grid)+ collect
   cell that contain 'o'
    */
    public static LinkedList<int[]> findSnake(char[][] area) {
        LinkedList<int[]> snake = new LinkedList<>();
        //use this loop to check every cell in the area
        for (int row = 0; row < 15; row++) {
            for (int col = 0; col < 15; col++) {
                if (area[row][col] == 'o') {//check if cell contain part of snake
                    snake.add(new int[]{row, col});//store positions into list
                }
            }
        }
        return snake;
    }
    //this print char[][]area
    public static void displayArea(char[][] area) {
        for (int row = 0; row < 15; row++) {//go through rows starts from 0-14 (15)
            for (int col = 0; col < 15; col++) {
                System.out.print(area[row][col]);//will print char in that cell
                if (col < 15 - 1) System.out.print(" ");//print space between cells
                //(col < 15 - 1) avoid printing space after last col
            }
            System.out.println();
        }
    }
    //loadAndGetArea---> read & load file & convert content to char[15][15]
    public static char[][] loadAndGetArea() {
        char[][] area = new char[15][15];//2D
        try {
            //read the file as one string, (relativePath)--> location path
            String fileContent = Files.readString(Path.of(relativePath));
            //spilt it to lines
            String[] lines = fileContent.split("\n");
            //read each line of the file
            for (int row = 0; row < 15; row++){
                //split each row into cells
                String[] cells = lines[row].trim().split(" ");
                for (int col = 0; col < 15; col++) {
                    //cells it's an array of string not char, after split each
                    // element is string even contain but area stores char so convert
                    area[row][col] = cells[col].charAt(0);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return area;
    }
    //FileWriter--> open the file to writing+ auto close the file
    public static void saveArea(char[][] area) {
        try (FileWriter fw = new FileWriter(relativePath)) {
            //scan every r+c
            for (int row = 0; row < 15; row++) {
                for (int col = 0; col < 15; col++) {
                    //write char into file
                    fw.write(area[row][col]);
                    //add space between col
                    if (col < 15 - 1) fw.write(' ');
                }
                fw.write('\n');
            }
            //if writes fails throw Runtime,stop program show an error message
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



