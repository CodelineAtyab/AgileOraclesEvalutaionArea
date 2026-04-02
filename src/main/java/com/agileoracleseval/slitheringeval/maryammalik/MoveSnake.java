package com.agileoracleseval.slitheringeval.maryammalik;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class MoveSnake {

    public static void main(String[] args) {

        String direction = "";
        int steps = 1;

        if (args.length == 0) {
            System.out.println("MoveSnake <direction> <steps>");
            System.out.println("the directions:");
            System.out.println("up");
            System.out.println("down");
            System.out.println("left");
            System.out.println("right");
            System.out.println("example:");
            System.out.println("javac MoveSnake.java");
            System.out.println("java MoveSnake.java up 3");
            return;
        }
        else {
            direction = args[0].toLowerCase();
            if (args.length > 1) {
                try {
                    steps = Integer.parseInt(args[1]);
                    if (steps <= 0) {
                        System.out.println("steps must be positive!");
                        return;
                    }
                }
                catch (NumberFormatException e) {
                    System.out.println("steps must be a valid number!");
                    return;
                }
            }
        }

        char[][] map;
        try {
            List<String> lines = Files.readAllLines(Path.of("map.txt"));
            int rows = lines.size();
            int cols = lines.get(0).length();
            map = new char[rows][cols];
            for (int i = 0; i < rows; i++) {
                map[i] = lines.get(i).toCharArray();
            }
        }
        catch (IOException e) {
            System.out.println("error");
            return;
        }

        int rows = map.length;
        int cols = map[0].length;

        // find snake positions
        ArrayList<int[]> snake = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (map[r][c] >= '1' && map[r][c] <= '5') {
                    int index = map[r][c] - '1';
                    while (snake.size() <= index) {
                        snake.add(null);
                    }
                    snake.set(index, new int[]{r, c});
                }
            }
        }

        // move snake
        for (int step = 0; step < steps; step++) {
            ArrayList<int[]> prevPositions = new ArrayList<>();
            for (int i = 0; i < snake.size(); i++) {
                int[] posit = snake.get(i);
                prevPositions.add(new int[]{posit[0], posit[1]});
            }

            // calculate new head
            int[] head = snake.get(0);
            int newR = head[0];
            int newC = head[1];
            if (direction.equals("up")) {
                newR--;
            }
            else if (direction.equals("down")) {
                newR++;
            }
            else if (direction.equals("left")) {
                newC--;
            }
            else if (direction.equals("right")) {
                newC++;
            }
            else {
                List<String> openDirctss = getOpenDirections(head, map, snake);
                System.out.println("invalid direction! the available open directions: " + openDirctss);
                return;
            }

            // check boundaries
            if (newR < 0 || newR >= rows || newC < 0 || newC >= cols) {
                List<String> openDirctss = getOpenDirections(head, map, snake);
                System.out.println("move out of map boundaries! the available open directions: " + openDirctss);
                return;
            }

            // check self-collision
            boolean collision = false;
            for (int i = 1; i < snake.size(); i++) {
                if (snake.get(i)[0] == newR && snake.get(i)[1] == newC) {
                    collision = true;
                }
            }

            if (collision) {
                List<String> openDirctss = getOpenDirections(head, map, snake);
                System.out.println("collision! head hit the body! the available open directions: " + openDirctss);
                return;
            }

            // move head
            snake.get(0)[0] = newR;
            snake.get(0)[1] = newC;

            // move body
            for (int i = 1; i < snake.size(); i++) {
                snake.get(i)[0] = prevPositions.get(i - 1)[0];
                snake.get(i)[1] = prevPositions.get(i - 1)[1];
            }

            // clear map
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (map[r][c] >= '1' && map[r][c] <= '5') {
                        map[r][c] = '-';
                    }
                }
            }

            // snake
            for (int i = 0; i < snake.size(); i++) {
                int[] pos = snake.get(i);
                map[pos[0]][pos[1]] = (i + 1 + "").charAt(0);
            }
        }

        System.out.println("Live view of the snake:");
        displayMap(map);

        // save updated map
        saveMap(map);
    }

    public static void displayMap(char[][] map) {
        for (int i = 0; i < map.length; i++) {
            for (int j = 0; j < map[i].length; j++) {
                System.out.print(map[i][j]);
            }
            System.out.println();
        }
    }

    public static void saveMap(char[][] map) {
        List<String> newLines = new ArrayList<>();
        for (int i = 0; i < map.length; i++) {
            newLines.add(new String(map[i]));
        }
        try {
            Files.write(Path.of("map.txt"), newLines);
        }
        catch (IOException e) {
            System.out.println("Error saving map");
        }
    }

    //open directions
    public static List<String> getOpenDirections(int[] head, char[][] map, ArrayList<int[]> snake) {
        List<String> openDirctss = new ArrayList<>();
        int rows = map.length;
        int cols = map[0].length;
        int r = head[0];
        int c = head[1];

        // up
        if (r - 1 >= 0 && !isBody(r - 1, c, snake)){
            openDirctss.add("up");
        }
        // down
        if (r + 1 < rows && !isBody(r + 1, c, snake)){
            openDirctss.add("down");
        }
        // left
        if (c - 1 >= 0 && !isBody(r, c - 1, snake)){
            openDirctss.add("left");
        }
        // right
        if (c + 1 < cols && !isBody(r, c + 1, snake)){
            openDirctss.add("right");
        }

        return openDirctss;
    }

    public static boolean isBody(int r, int c, ArrayList<int[]> snake) {
        for (int i = 1; i < snake.size(); i++) {
            if (snake.get(i)[0] == r && snake.get(i)[1] == c){
                return true;
            }
        }
        return false;
    }
}
