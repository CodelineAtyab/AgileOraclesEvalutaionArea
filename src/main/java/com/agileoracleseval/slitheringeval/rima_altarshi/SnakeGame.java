package com.agileoracleseval.slitheringeval.rima_altarshi;

import java.io.*;
import java.util.*;

public class SnakeGame {
    static int width = 15;
    static int height = 15;
    static int score = 0;

    static Deque<int[]> snake = new LinkedList<>(); //stores snake body positions
    static Set<String> snakeSet = new HashSet<>();

    static int foodX;
    static int foodY;
    static Random random = new Random();
    //this meaning , moving right
    static int directionX = 0;
    static int directionY = 1;
    //where the game state is saved
    static String filePath = "src/main/java/com/agileoracleseval/slitheringeval/rima_altarshi/map.txt";

    public static void main(String[] args) {
        loadGame(); //load previous state if file exists
        Scanner sc = new Scanner(System.in); //read the input
        while (true) {
            printBoard(); //showing snake , food, score
            String direction;
            int steps = 1; // by default = 1
            while (true) {
                System.out.println("Enter move (up,down, left and right): ");
                String input = sc.nextLine().trim().toLowerCase();
                String[] parts = input.split("\\s+"); //split by spaces
                if (parts.length == 0 || parts[0].isEmpty()) {
                    System.out.println(" Invalid input!");
                    continue;
                }
                direction = parts[0];
                if (!(direction.equals("up") || direction.equals("down") ||
                        direction.equals("left") || direction.equals("right"))) {
                    System.out.println(" Invalid direction!");
                    continue;
                }
                if (parts.length > 1) {
                    try {
                        steps = Integer.parseInt(parts[1]);

                        if (steps <= 0) {
                            System.out.println(" Steps must be >= 1.");
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        System.out.println(" Invalid number.");
                        continue;
                    }
                }
                break;
            }
            setDirection(direction); //Converts e.g "up" to (directionX, directionY)
            for (int i = 0; i < steps; i++) {
                if (!moveSnake()) {
                    System.out.println("The only open directions are: " + getOpenDirections());
                    break;
                }
                saveGame(); // persist after each successful move
            }
        }
    }

    //init snake game
    static void initGame() {
        score = 0; //while the snake not exists the food
        snake.clear();
        snakeSet.clear();
        int row = 7; //snake coordinates init/start in row 7
        for (int col = 5; col <= 9; col++) { //from 5 to 9 col (snake length 5  firstly)
            snake.addFirst(new int[]{row, col});
            snakeSet.add(row + "," + col);
        }
        placeFood();
    }

    // move the snake
    // [0] is X position ( row )
    // [1] is Y position (col)
    static boolean moveSnake() {
        int[] head = snake.peekFirst(); //gets the current head position [7,9]
        //new position = current position + direction
        int newX = head[0] + directionX; //[left and right]
        int newY = head[1] + directionY; //[down and up]
        // handling the vertical wrapping round
        if (newX < 0) {
            newX = height - 1;
        } else if (newX >= height) {
            newX = 0;
        }
        // handling the horizontal wrapping round
        if (newY < 0) {
            newY = width - 1;
        } else if (newY >= width) {
            newY = 0;
        }
        String newHead = newX + "," + newY;
        if (snakeSet.contains(newHead)) //checking collision
            return false;
        //add new position to snake linked list and snakeSet for fast lookup, so the snake moves forward
        snake.addFirst(new int[]{newX, newY});
        snakeSet.add(newHead);
        if (newX == foodX && newY == foodY) {
            score++; //if eat food increase score by 1
            placeFood(); // then generate new food
        } else { //else remove tail and convert position to string
            int[] tail = snake.removeLast();
            snakeSet.remove(tail[0] + "," + tail[1]);
        }
        return true; //the movement successfully complete
    }

    // directions
    // directionX is controls row movement (up/down)
    // directionY is controls column movement (left/right)
    static void setDirection(String direction) {
        switch (direction) {
            case "up":
                directionX = -1;
                directionY = 0;
                break;
            case "down":
                directionX = 1;
                directionY = 0;
                break;
            case "left":
                directionX = 0;
                directionY = -1;
                break;
            case "right":
                directionX = 0;
                directionY = 1;
                break;
        }
    }

    // food
    static void placeFood() {
        while (true) {
            //generate random position
            foodX = random.nextInt(height);
            foodY = random.nextInt(width);
            if (!snakeSet.contains(foodX + "," + foodY))
                break;
        }
    }

    // display the current game state
    static void printBoard() {
        //2D array
        char[][] board = new char[height][width];
        //fill board with empty cells
        for (int i = 0; i < height; i++)
            Arrays.fill(board[i], '-');
        //draw the snake
        for (int[] part : snake)
            board[part[0]][part[1]] = 'O';
        //draw the food
        board[foodX][foodY] = '*';
        System.out.println("\n--- SNAKE GAME ---");
        System.out.println("Score: " + score);
        //print the board (row by row)
        for (int row = 0; row < height; row++) {
            for (int col = 0; col < width; col++) {
                System.out.print(board[row][col] + " ");
            }
            System.out.println();
        }
    }

    //checking which directions the snake can safely move without hitting itself
    static String getOpenDirections() {
        int[] head = snake.peekFirst(); //gets current head position
        //create list of safe directions to stores all valid moves
        List<String> open = new ArrayList<>();
        // up direction
        int upX = head[0] - 1;
        int upY = head[1];
        //wrap round (if need ), if snake head go above the board it will teleport to bottom
        if (upX < 0) {
            upX = height - 1;
        }
        //check collision
        if (!snakeSet.contains(upX + "," + upY)) {
            open.add("UP"); // if safe the up direction is allowed
        }
        // down direction
        int downX = head[0] + 1;
        int downY = head[1];

        if (downX >= height) {
            downX = 0;
        }
        if (!snakeSet.contains(downX + "," + downY)) {
            open.add("DOWN");
        }
        // left direction
        int leftX = head[0];
        int leftY = head[1] - 1;
        if (leftY < 0) {
            leftY = width - 1;
        }
        if (!snakeSet.contains(leftX + "," + leftY)) {
            open.add("LEFT");
        }
        //right direction
        int rightX = head[0];
        int rightY = head[1] + 1;
        if (rightY >= width) {
            rightY = 0;
        }
        if (!snakeSet.contains(rightX + "," + rightY)) {
            open.add("RIGHT");
        }
        //if no directions are safe
        if (open.isEmpty()) {
            return "NONE";
        }
        return String.join(", ", open);
    }

    //  saves the current game board to a map.txt file
    static void saveGame() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            // Save score position
            pw.println("SCORE " + score);
            // Save food position
            pw.println("FOOD " + "[" + foodX + "," + foodY + "]");
            // Save snake position(from head to tail)
            pw.print("SNAKE ");
            for (int[] part : snake) {
                pw.print("[" + part[0] + "," + part[1] + "] ");
            }
        } catch (IOException e) {
            System.out.println("Error saving game: " + e.getMessage());
        }
    }

    // loads previous game state from map.txt file
    static void loadGame() {
        File file = new File(filePath);
        // if no saved game , so start a new game
        if (!file.exists()) {
            initGame();
            return; //stop the method
        }
        //remove previous snake positions and Prepare to rebuild from file
        snake.clear();
        snakeSet.clear();
        //list will store snake positions read from map.txt file
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith("SCORE")) {
                    score = Integer.parseInt(line.split(" ")[1]);
                } else if (line.startsWith("FOOD")) {
                    String value = line.substring(line.indexOf("[") + 1, line.indexOf("]"));
                    String[] xy = value.split(",");
                    foodX = Integer.parseInt(xy[0].trim());
                    foodY = Integer.parseInt(xy[1].trim());
                } else if (line.startsWith("SNAKE")) {
                    String body = line.replace("SNAKE", "").trim();
                    String[] parts = body.split("\\s+");
                    for (String p : parts) {
                        p = p.replace("[", "").replace("]", ""); // to remove brackets
                        String[] xy = p.split(",");
                        int x = Integer.parseInt(xy[0].trim());
                        int y = Integer.parseInt(xy[1].trim());
                        snake.addLast(new int[]{x, y});
                        snakeSet.add(x + "," + y);
                    }
                }
            }
            // if no snake found then start new game (initGame method)
            if (snake.isEmpty()) {
                initGame();
            }

        } catch (Exception e) {
            System.out.println("Error loading game: " + e.getMessage());
            initGame();
        }
    }
}