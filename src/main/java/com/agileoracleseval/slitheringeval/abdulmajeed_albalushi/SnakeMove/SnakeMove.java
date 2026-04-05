package com.agileoracleseval.slitheringeval.abdulmajeed_albalushi.SnakeMove;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;

public class SnakeMove {
    public static void main(String[] args) {
        char[][] snake = loadSnake();
        int row = 0;
        int col = 0;
        LinkedList<int[]> startMoving = loadBody(snake);
        boolean found = false;

        for (int rowFind = 0; rowFind < snake.length; rowFind++) {
            for (int colFind =0; colFind <snake[rowFind].length; colFind++) {

                if (snake[rowFind][colFind] == 'o') {
                    System.out.println("the head is at row: " + rowFind +", column: " + colFind);
                    row=rowFind;
                    col=colFind;
                    found = true;
                    break;
                }
            }
            if (found) {
                break;
            }
        }

        if (args.length <2){
            System.out.println("Usage: java ");
            return;
        }

        String move = args[0];
        int steps = Integer.parseInt(args[1]);

        for (int repeat = 0; repeat<steps; repeat ++){
            makeMove(snake, startMoving, move);
            displaySnake(snake);

        }
        savingPosition(snake);
    }
    public static void makeMove(char[][] snake, LinkedList<int[]> startMoving,String move) {
        int[] head = startMoving.getFirst();

        int newRow = head[0];
        int newCol = head[1];

        if (move.equals("up")) {
            newRow = newRow - 1;
        } else if (move.equals("down")) {
            newRow = newRow + 1;
        } else if (move.equals("right")) {
            newCol = newCol + 1;
        } else if (move.equals("left")) {
            newCol = newCol - 1;
        }
        if (newRow<0 || newRow >= snake.length || newCol< 0 || newCol >= snake[newRow].length){
            System.out.println("hit the edge go left or right");
            return;
        }

        if (snake[newRow][newCol] == '-') {

            // add new head
            startMoving.addFirst(new int[]{newRow, newCol});
            snake[newRow][newCol] = 'o';

            // remove tail
            int[] tail = startMoving.removeLast();
            snake[tail[0]][tail[1]] = '-';
        }
    }

    public static char[][] loadSnake(){
        Path snakePath;
        char[][] snake;
        try {
            String mapPath = "src/main/java/com/agileoracleseval/slitheringeval/abdulmajeed_albalushi/SnakeMove/data/map.txt";
            snakePath = Path.of(mapPath);

            String insideFile = Files.readString(snakePath).replace("\r","");
            String[] fileLines = insideFile.split("\n");
            int lineLength = fileLines[0].length();

            snake = new char[fileLines.length][lineLength];
            for (int row = 0; row <fileLines.length; row++) {
                char[] currRow = fileLines[row].toCharArray();
                System.arraycopy(currRow, 0, snake[row], 0, currRow.length);
            }

        } catch (IOException e){
            throw new RuntimeException(e);
        }
        return snake;
    }
    public static void displaySnake(char[][] snake){
        for (int i =0; i<snake.length;i++){
            for (int j=0; j<snake[i].length;j++){
                System.out.printf("%s",snake[i][j]);
            }
            System.out.println();
        }

    }
    public static LinkedList<int[]> loadBody(char[][] snake){
        LinkedList<int[]> body = new LinkedList<>();

        for (int i=0; i<snake.length;i++){
            for (int j=0;j<snake[i].length;j++){
                if (snake[i][j] == 'o'){
                    body.addFirst(new int[] {i,j});
                }
            }
        }
        return body;
    }
    public static void savingPosition(char[][] snake){
        try{
            StringBuilder content = new StringBuilder();
            for (int i =0; i <snake.length;i++){
                for (int j=0; j<snake[i].length;j++){
                    content.append(snake[i][j]);
                }
                content.append("\n");
            }
            Files.write(Path.of("src/main/java/com/agileoracleseval/slitheringeval/abdulmajeed_albalushi/SnakeMove/data/map.txt"), content.toString().getBytes());
        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}