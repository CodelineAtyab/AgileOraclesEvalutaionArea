package com.agileoracleseval.slitheringeval.rima_altarshi;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
public class SnakeGame {
   static int foodRow = -1;
    static int foodCol = -1;
    public static void main(String[] args) throws InterruptedException {
        try {
            //Read the map.txt file
            Path snakePath = Path.of("src//main/java/com/agileoracleseval/slitheringeval/rima_altarshi/map.txt");
            //store each row in the list as string
            List<String> lines = Files.readAllLines(snakePath);
            //remove the spaces form each row
            for (int i = 0; i < lines.size(); i++) {
                lines.set(i, lines.get(i).replace(" ", ""));
            }

            //count the rows and cols
            int rows = lines.size();
            int cols = lines.get(0).length();

            // 2D Array
            char[][] map = new char[rows][cols];
            for (int i = 0; i < rows; i++) {
                map[i] = lines.get(i).toCharArray();
            }

            //check the input args "validation"
            if (args.length == 0) {
                System.out.println("Please provide direction (up, down, left, right)");
                return;
            }

            // convert the user input into lowercase
            String direction = args[0].toLowerCase();

            // validate the direction
            if (!direction.equals("up") && !direction.equals("down") &&
                    !direction.equals("left") && !direction.equals("right")) {
                System.out.println("Invalid direction!");
                return;
            }

            //validate the number of steps
            int steps = 1; // by default assume steps is 1
            // check the string input and convert it to integer
            if (args.length > 1) {
                try {
                    steps = Integer.parseInt(args[1]);
                    if (steps <= 0) {
                        System.out.println("Steps must be positive number!");
                        return;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Steps must be a number!");
                    return;
                }
            }

            //using HashMap to store the snake movement
            Map<String, int[]> moves = new HashMap<>();
            moves.put("up", new int[]{-1, 0});
            moves.put("down", new int[]{1, 0});
            moves.put("left", new int[]{0, -1});
            moves.put("right", new int[]{0, 1});

            // generate and to store snake coordinates in the list
            List<int[]> snake = new ArrayList<>();
            Set<String> snakeSet = new HashSet<>();

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (map[r][c] == 'o') {
                        snake.add(new int[]{r, c});
                        snakeSet.add(r + "," + c);
                    }
                }
            }

            //food loop
            boolean foodExists = false;

            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    if (map[r][c]==('*')) {
                        foodRow = r;
                        foodCol = c;
                        foodExists = true;
                    }
                }
            }

            if (!foodExists) {
                int[] food = generateFood(snakeSet, rows, cols);
                foodRow = food[0];
                foodCol = food[1];
            }

            String currentDirection = "";

            if (snake.size() > 1) {
                int[] head = snake.get(snake.size() - 1);
                int[] beforeHead = snake.get(snake.size() - 2);

                if (head[0] == beforeHead[0]) {
                    currentDirection = (head[1] > beforeHead[1]) ? "right" : "left";
                } else {
                    currentDirection = (head[0] > beforeHead[0]) ? "down" : "up";
                }
            }

            // prevent the reverse
            boolean reverse =
                    (currentDirection.equals("right") && direction.equals("left")) ||
                            (currentDirection.equals("left") && direction.equals("right")) ||
                            (currentDirection.equals("up") && direction.equals("down")) ||
                            (currentDirection.equals("down") && direction.equals("up"));

            if (reverse) {
                printOpenDirections(snake, snakeSet, moves, rows, cols, currentDirection);
                return;
            }

            // snake game loop
            for (int step = 0; step < steps; step++) {

                int[] head = snake.get(snake.size() - 1);

                int newRow = head[0] + moves.get(direction)[0];
                int newCol = head[1] + moves.get(direction)[1];

                // wrap round
                if (newRow < 0) newRow = rows - 1;
                else if (newRow >= rows) newRow = 0;

                if (newCol < 0) newCol = cols - 1;
                else if (newCol >= cols) newCol = 0;

                String nextPos = newRow + "," + newCol;

                // condition for snake body collision
                if (snakeSet.contains(nextPos)) {
                    printOpenDirections(snake, snakeSet, moves, rows, cols, direction);
                    return;
                }

                // generate the food
                if (newRow == foodRow && newCol == foodCol) {

                    snake.add(new int[]{newRow, newCol});
                    snakeSet.add(nextPos);

                    int[] newFood = generateFood(snakeSet, rows, cols);
                    foodRow = newFood[0];
                    foodCol = newFood[1];

                } else {

                    snake.add(new int[]{newRow, newCol});
                    snakeSet.add(nextPos);

                    int[] removed = snake.remove(0);
                    snakeSet.remove(removed[0] + "," + removed[1]);
                }

                //updating the map
                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {
                        map[r][c] = '-';
                    }
                }
                for (int[] part : snake) {
                    map[part[0]][part[1]] = 'o';
                }
                map[foodRow][foodCol] = '*';

                // save the update map into map.txt
                FileWriter writer = new FileWriter("src//main/java/com/agileoracleseval/slitheringeval/rima_altarshi/map.txt");

                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {
                        writer.write(map[r][c] + " ");
                    }
                    writer.write("\n");
                }

                writer.close();

                // print
                System.out.println("\nStep " + (step + 1) + " - moved " + direction);

                for (int r = 0; r < rows; r++) {
                    for (int c = 0; c < cols; c++) {
                        System.out.print(map[r][c] + " ");
                    }
                    System.out.println();
                }

                System.out.println("Score: " + snake.size());
            }

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void printOpenDirections(List<int[]> snake, Set<String> snakeSet,
                                           Map<String, int[]> moves,
                                           int rows, int cols,
                                           String currentDirection) {

        int[] head = snake.get(snake.size() - 1);
        int[] tail = snake.get(0);
        String tailPos = tail[0] + "," + tail[1];

        List<String> open = new ArrayList<>();

        for (String dir : moves.keySet()) {

            boolean reverse =
                    (currentDirection.equals("right") && dir.equals("left")) ||
                            (currentDirection.equals("left") && dir.equals("right")) ||
                            (currentDirection.equals("up") && dir.equals("down")) ||
                            (currentDirection.equals("down") && dir.equals("up"));

            if (reverse) continue;

            int r = head[0] + moves.get(dir)[0];
            int c = head[1] + moves.get(dir)[1];

            if (r < 0) r = rows - 1;
            else if (r >= rows) r = 0;

            if (c < 0) c = cols - 1;
            else if (c >= cols) c = 0;

            String pos = r + "," + c;

            if (!snakeSet.contains(pos) || pos.equals(tailPos)) {
                open.add(dir);
            }
        }

        System.out.println("The only open directions are " + String.join(", ", open));
    }

    // function to generate the food
    public static int[] generateFood(Set<String> snakeSet, int rows, int cols) {

        int r, c;

        do {
            r = (int) (Math.random() * rows);
            c = (int) (Math.random() * cols);
        } while (snakeSet.contains(r + "," + c));

        return new int[]{r, c};
    }
}