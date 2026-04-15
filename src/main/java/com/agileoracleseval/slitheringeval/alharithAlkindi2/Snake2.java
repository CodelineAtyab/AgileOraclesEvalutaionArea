package com.agileoracleseval.slitheringeval.alharithAlkindi2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Snake2 {
    public static void main(String[] args) throws IOException {

        ArrayList<String[]> mapGrid = new ArrayList<>();
        LinkedList<int[]> snake = new LinkedList<>();
        String filePath = "src/main/java/com/agileoracleseval/slitheringeval/alharithAlkindi2/map.txt";
        File file = new File(filePath);


        try (Scanner mapScanner = new Scanner(file)) {
            while (mapScanner.hasNextLine()) {
                String data = mapScanner.nextLine().trim();

                if (data.isEmpty()) continue;

                if (data.startsWith("SNAKE")) {
                    String[] parts = data.split(" ");
                    for (int i = 1; i < parts.length; i += 2) {
                        int r = Integer.parseInt(parts[i]);
                        int c = Integer.parseInt(parts[i + 1]);
                        snake.addLast(new int[]{r, c});
                    }
                } else {
                    String[] row = data.split(" ");
                    mapGrid.add(row);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return;
        }



        int rows = mapGrid.size();
        int cols = mapGrid.get(0).length;
        String[][] map = new String[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                map[r][c] = mapGrid.get(r)[c];
            }
        }


        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                System.out.print(map[i][j] + " ");
            }
            System.out.println();
        }



        if (snake.isEmpty()) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (map[r][c].equals("o")) {
                        snake.addLast(new int[]{r, c});
                    }
                }
            }
        }



        System.out.println("Tail: [" + snake.getFirst()[0] + "," + snake.getFirst()[1] + "]");
        System.out.println("Head: [" + snake.getLast()[0] + "," + snake.getLast()[1] + "]");

        if (args.length < 2)
        {
            System.out.println("Usage: java Snake2 <direction> <steps>");
            return;
        }
        String dir = args[0].toLowerCase();
        int steps =Integer.parseInt(args[1]);
        boolean isHitWall = false;


        for(int step =0; step< steps;step++) {
            int[] head = snake.getLast();
            int headRow = head[0];
            int headCol = head[1];

            int newRow = headRow;
            int newCol = headCol;

            if (dir.equals("up")) newRow = headRow - 1;
            if (dir.equals("down")) newRow = headRow + 1;
            if (dir.equals("left")) newCol = headCol - 1;
            if (dir.equals("right")) newCol = headCol + 1;

            if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols) {
                System.out.println("=================================\n" +
                        "==== hehehehehehe you hit the wall hehehe =============" +"at the "+ dir+
                        "\n================================" );
                isHitWall = true;
                break;
            }

            int[] oldTail=snake.getFirst();
            snake.addLast(new int[]{newRow,newCol});
            snake.removeFirst();
            map[oldTail[0]][oldTail[1]]="-";
            map[newRow][newCol] = "o";

        }

        if(!isHitWall) {
            System.out.println("\t=== new map here  === ");
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[i].length; j++) {
                    System.out.print(map[i][j] + " ");
                }
                System.out.println(" ");
            }
        }
        System.out.println("Uptdated Tail: [" + snake.getFirst()[0] + "," + snake.getFirst()[1] + "]");
        System.out.println("Uptdated Head: [" + snake.getLast()[0] + "," + snake.getLast()[1] + "]");


        StringBuilder snakeLine = new StringBuilder("SNAKE");
        for (int[] segment : snake) {
            snakeLine.append(" ").append(segment[0]).append(" ").append(segment[1]);
        }

        try (PrintWriter writer = new PrintWriter(file)) {
            for (int i = 0; i < map.length; i++) {
                for (int j = 0; j < map[i].length; j++) {
                    if (j > 0) writer.print(" ");
                    writer.print(map[i][j]);
                }
                writer.println();
            }
            //writer.println(snakeLine);
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }



    }
}
