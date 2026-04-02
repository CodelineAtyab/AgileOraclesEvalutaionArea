package com.agileoracleseval.slitheringeval.safaalmaamari.SnakeMove;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.Scanner;
import java.io.BufferedWriter;
import java.io.FileWriter;

public class SnakeMove{
    public static void main(String[] args) {
        Path snakePath = null;
        char[][] map;
        LinkedList<Point> snake = new LinkedList<>();

        try {
            snakePath = Path.of("C:/Users/Codeline/Desktop/Evaluation/AgileOraclesEvalutaionArea/src/main/java/com/agileoracleseval/slitheringeval/safaalmaamari/SnakeMove/map.txt");
        } catch (Exception e) {

            throw new RuntimeException(e);
        }
        try {
            String fileContent = Files.readString(snakePath);
            String[] linesOfFile = fileContent.split("\\R");
            int lineLength = linesOfFile[0].length();
            if (linesOfFile.length < 15 || lineLength < 15) {
                System.out.println("Invalid map length...");
                System.exit(1);
            }
            map = new char[linesOfFile.length][lineLength];  // Load it in 2D Array or Array of Arrays
            for (int row = 0; row < linesOfFile.length; row++) {
                char[] currRow = linesOfFile[row].toCharArray();
             // System.out.printf("%s\n", linesOfFile[row]);

                for (int col = 0; col < currRow.length; col++) {
                    map[row][col] = currRow[col];
                }
            }
        } catch (IOException e) {
            System.out.println("error file not found!");
            return;
        }

        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[row].length; col++) {
                System.out.print(map[row][col] + " ");
            }
            System.out.println();
        }

        //find snake location
        for (int c = 9; c >= 5; c--) {
            snake.add(new Point(7, c));
        }
        Point head = snake.getFirst();
        Point tail = snake.getLast();
        //System.out.println("head in: (" + head.x + "," + head.y + ") tail in: (" + tail.x + "," + tail.y +")");

        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.println("Select <direction>: up, down, left, right, <steps> integer, for example: up 2, or just direction");
            String[] arguments = input.nextLine().split(" ");
            String direction = arguments[0];
            int steps;
            if (arguments.length > 1) {
                steps = Integer.parseInt(arguments[1]);
            } else steps = 1;
            if (steps < 0) {
                System.out.println("Invalid steps!");
                System.exit(1);
            }

            boolean isColl = false;

            for (int i = 0; i < steps; i++) {
                if (direction.equalsIgnoreCase("up")) {
                    head = snake.getFirst();
                    System.out.println("old snake position"+" "+snake.toString());
                    System.out.println("-------------------------------");
                    Point newHead = new Point(head.x-1, head.y);
                    snake.remove(snake.size() - 1);
                    snake.push(newHead);

                    System.out.println("new snake position"+" "+snake.toString());
                    System.out.println("-------------------------------");

                    isColl = isCollision(snake);

                    if (isColl == true) {
                        System.out.println("the is a collision so the program will exit");
                        System.exit(1);
                    }

                    printUpdatedMap(snake);
                    saveMapToFile(snake);

                } else if (direction.equalsIgnoreCase("down")) {
                    head = snake.getFirst();
                    System.out.println("old snake position"+" "+snake.toString());
                    System.out.println("-------------------------------");
                    Point newHead = new Point(head.x+1, head.y);
                    snake.remove(snake.size() - 1);
                    snake.push(newHead);

                    System.out.println("new snake position"+" "+snake.toString());
                    System.out.println("-------------------------------");

                    isColl = isCollision(snake);
                    if (isColl == true) {
                        System.out.println("the is a collision so the program will exit");
                        System.exit(1);
                    }
                    printUpdatedMap(snake);
                    saveMapToFile(snake);

                } else if (direction.equalsIgnoreCase("left")) {
                    head = snake.getFirst();
                    System.out.println("old snake position"+" "+snake.toString());
                    System.out.println("-------------------------------");
                    Point newHead = new Point(head.x, head.y - 1);
                    snake.remove(snake.size() - 1);
                    snake.push(newHead);

                    System.out.println("new snake position"+" "+snake.toString());
                    System.out.println("-------------------------------");

                    isColl = isCollision(snake);
                    if (isColl == true) {
                        System.out.println("the is a collision so the program will exit");
                        System.exit(1);
                    }
                    printUpdatedMap(snake);
                    saveMapToFile(snake);

                } else if (direction.equalsIgnoreCase("right")) {
                    head = snake.getFirst();
                    System.out.println("old snake position"+" "+snake.toString());
                    System.out.println("-------------------------------");
                    Point newHead = new Point(head.x, head.y + 1);
                    snake.remove(snake.size() - 1);
                    snake.push(newHead);
                    System.out.println("new snake position"+" "+snake.toString());
                    System.out.println("-------------------------------");

                    isColl = isCollision(snake);

                    if (isColl == true) {
                        System.out.println("the is a collision so the program will exit");
                        System.exit(1);
                    }
                    printUpdatedMap(snake);
                    saveMapToFile(snake);

                } else {
                    System.out.println("Invalid direction!");
                }
            }
        }
    }
    public static boolean isCollision(LinkedList<Point> snake){
        Point head = snake.getFirst();
        for(int i = 1; i < snake.size(); i++){
            if (head.x == snake.get(i).x && head.y == snake.get(i).y){
                System.out.println("the head collapse with the body point "+snake.get(i).x + " " +snake.get(i).y);
                return true;
            }
        }
        return false;
    }
    public static void printUpdatedMap(LinkedList<Point> snake){
        char[][] map = new char[15][15];
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                map[i][j] = '-';
            }
        }
        for (int i =0;i<snake.size();i++){
            int x = snake.get(i).x;
            int y = snake.get(i).y;
            map[x][y] = 'o';
        }
        for (int row = 0; row < map.length; row++) {
            for (int col = 0; col < map[row].length; col++) {
                System.out.print(map[row][col] + "   ");
            }
            System.out.println();
        }
    }
    public static void saveMapToFile(LinkedList<Point> snake) {
        char[][] map = new char[15][15];
        for (int i = 0; i < 15; i++) {
            for (int j = 0; j < 15; j++) {
                map[i][j] = '-';
            }
        }
        for (int i = 0; i < snake.size(); i++) {
            int x = snake.get(i).x;
            int y = snake.get(i).y;
            map[x][y] = 'o';
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("map.txt"))) {
            for (int row = 0; row < map.length; row++) {
                for (int col = 0; col < map[row].length; col++) {
                    writer.write(map[row][col] + " ");
                }
                writer.newLine(); // go to next line
            }
            System.out.println("Map saved to map.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
