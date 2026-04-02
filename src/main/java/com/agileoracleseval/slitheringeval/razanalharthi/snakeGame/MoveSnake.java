package com.agileoracleseval.slitheringeval.razanalharthi.snakeGame;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.nio.file.Path;


public class MoveSnake {
    public static void main(String[] args)
            throws Exception {


        Path mapPath = Path.of("src/main/java/com/agileoracleseval/slitheringeval/razanalharthi/snakeGame/map.txt");
        //read file
        String snake = Files.readString(mapPath);

        //split lines

        String[] Lines = snake.split("\n");
        int row = Lines.length;
        int col = Lines[0].trim().split(" ").length;


        if (row < 15 || col < 15) {
            System.out.println("15*15");
            return;
        }
        // char[][] map----> easy for print and audit
        char[][] map = new char[row][col];

        //fill map
        for (int ro = 0; ro < row; ro++) {
            String[] parts = Lines[ro].trim().split(" ");
            for (int co = 0; co < col; co++) {
                map[ro][co] = parts[co].charAt(0);

            }
        }
        System.out.println(" Start : ");
        PrintMap(map);

        //snake body
        ArrayList<int[]> snackes = new ArrayList<>();

        for (int r = 0; r < row; r++) {
            for (int c = 0; c < row; c++) {
                if (map[r][c] == 'o') {
                    snackes.add(new int[]{r, c});
                }
            }
        }

        //args
        if (args.length == 0) {
            System.out.println("usage: java Snake <direction> <steps>");
            return;

        }
        String direction = args[0].toLowerCase();

        int steps = 1;
        if (args.length == 2) {
            try {
                steps = Integer.parseInt(args[1]);
                if (steps >= 0) {
                    System.out.println("positive step");
                    return;
                }
            } catch (Exception e) {
                System.out.println("invalid");

                return;
            }
        }
        //tail ----> head
        snackes.sort(Comparator.comparing(a -> a[1]));

        //snake movment

        for (int s = 0; s < steps; s++) {

            int[] head = snackes.get(snackes.size() - 1);

            int newRow = head[0];
            int newCol = head[1];

            //direction control

            if (direction.equals("up")) {
                newRow--;
            } else if (direction.equals("down")) {
                newRow++;
            } else if (direction.equals("right")) {
                newCol++;
            } else {
                System.out.println("invalid");
                return;
            }

            //check
            if (newRow < 0 || newRow >= row || newCol < 0 || newCol >= col) {
                System.out.println(" oops out ");
                return;
            }

            //collision
            if (map[newRow][newCol] == 'o') {
                System.out.println("collision");
                return;

            }
            //add head
            snackes.add(new int[]{newRow, newCol});
            map[newRow][newCol] = 'o';


            //remove tail
            int[] tail = snackes.remove(0);
            map[tail[0]][tail[1]] = '-';

        }

        //after move print the map
        System.out.println("after move print the map: ");
        PrintMap(map);


        // save

        saveMap(map, mapPath);

    }
    public static void PrintMap ( char[][] map) {
        for (int r = 0; r < map.length; r++) {

            for (int c = 0; c < map[0].length; c++) {
                System.out.print(map[r][c] + " ");
            }
            System.out.println();

        }
    }

    static void saveMap ( char[][] map, Path path) throws IOException {
        StringBuilder s = new StringBuilder();

        for (char[] row : map) {
            for (int c = 0; c < row.length; c++) {
                s.append(row[c]);
                if (c != row.length - 1) s.append(" ");
            }

            s.append("\n");
        }
        Files.writeString(path, s.toString());
    }
}





