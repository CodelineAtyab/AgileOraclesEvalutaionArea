package com.agileoracleseval.slitheringeval.sulaiman_yousuf_alfarsi;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.*;

public class MoveSnake {

    static char[][] grid = null;
    static int ROWS = 0;
    static int COLS = 0;
    static ArrayList<int[]> snake = new ArrayList<>();
    static int savedHeadRow = -1;
    static int savedHeadCol = -1;

    public static void main(String[] args) {

        if (args.length < 1) {
            System.out.println("Usage: java MoveSnake <direction> [steps]");
            System.out.println("Example: java MoveSnake right 2");
            System.out.println("To reset: java MoveSnake reset");
            return;
        }

        // reset
        if (args[0].equalsIgnoreCase("reset")) {
            Path resetPath = null;
            try {
                resetPath = Path.of(
                        MoveSnake.class.getResource("map.txt").toURI());
            } catch (Exception e) {
                System.out.println("ERROR: map.txt not found!");
                return;
            }
            resetMap(resetPath);
            System.out.println("Map has been reset!");
            return;
        }

        // direction and steps
        String direction = args[0].toLowerCase().trim();

        int steps = 1;
        if (args.length >= 2) {
            try {
                steps = Integer.parseInt(args[1]);
                if (steps <= 0) {
                    System.out.println("Error: Steps must be a positive number.");
                    return;
                }
            } catch (NumberFormatException e) {
                System.out.println("Error: Steps must be a number.");
                return;
            }
        }

        if (!direction.equals("up") && !direction.equals("down")
                && !direction.equals("left") && !direction.equals("right")) {
            System.out.println("Error: Invalid direction '" + direction + "'");
            System.out.println("Valid directions: up, down, left, right");
            return;
        }

        // read file
        Path mapPath = null;

        try {
            mapPath = Path.of(
                    MoveSnake.class.getResource("map.txt").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            System.out.println("ERROR: map.txt not found!");
            return;
        }

        try {
            String fileContent = Files.readString(mapPath);
            String[] linesOfFile = fileContent.split("\\R");

            // check if last line is SNAKE data
            String lastLine = linesOfFile[linesOfFile.length - 1].trim();
            boolean hasSnakeLine = lastLine.startsWith("SNAKE:");

            // map rows = all lines except SNAKE line
            int mapLines = hasSnakeLine ? linesOfFile.length - 1 : linesOfFile.length;
            ROWS = mapLines;

            String[] firstLineParts = linesOfFile[0].trim().split("\\s+");
            COLS = firstLineParts.length;

            if (ROWS < 15 || COLS < 15) {
                System.out.println("Error: Map must be at least 15x15.");
                return;
            }

            grid = new char[ROWS][COLS];

            for (int i = 0; i < ROWS; i++) {
                String[] parts = linesOfFile[i].trim().split("\\s+");

                if (parts.length != COLS) {
                    System.out.println("Error: Row " + i + " is wrong.");
                    return;
                }

                for (int j = 0; j < COLS; j++) {
                    char c = parts[j].charAt(0);

                    if (c != '-' && c != 'o' && c != 'H') {
                        System.out.println("Error: Invalid character '" + c + "'");
                        return;
                    }

                    if (c == 'H') {
                        savedHeadRow = i;
                        savedHeadCol = j;
                        grid[i][j] = 'o';
                    } else {
                        grid[i][j] = c;
                    }
                }
            }

            // load snake order directly from SNAKE line
            if (hasSnakeLine) {
                snake.clear();
                String snakeData = lastLine.substring(6);
                String[] positions = snakeData.split(";");

                for (String pos : positions) {
                    String[] rc = pos.split(",");
                    int r = Integer.parseInt(rc[0].trim());
                    int c = Integer.parseInt(rc[1].trim());
                    snake.add(new int[]{r, c});
                }

                // update head from last element
                if (!snake.isEmpty()) {
                    int[] h = snake.get(snake.size() - 1);
                    savedHeadRow = h[0];
                    savedHeadCol = h[1];
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading file.");
            return;
        }

        findSnake();

        if (snake.isEmpty()) {
            System.out.println("Error: No snake found on the map!");
            return;
        }

        boolean moved = moveSnake(direction, steps);

        if (moved) {
            saveMap(mapPath);
        }

        printMap();
    }


    // move snake
    static boolean moveSnake(String direction, int steps) {

        for (int step = 0; step < steps; step++) {

            int[] head = snake.get(snake.size() - 1);
            int headRow = head[0];
            int headCol = head[1];

            int newRow = headRow;
            int newCol = headCol;

            if (direction.equals("up"))    newRow--;
            if (direction.equals("down"))  newRow++;
            if (direction.equals("left"))  newCol--;
            if (direction.equals("right")) newCol++;

            // wrap around
            if (newRow < 0)     newRow = ROWS - 1;
            if (newRow >= ROWS) newRow = 0;
            if (newCol < 0)     newCol = COLS - 1;
            if (newCol >= COLS) newCol = 0;

            // self collision check — skip tail because it will move
            int[] tail = snake.get(0);
            boolean hitSelf = false;

            for (int i = 0; i < snake.size(); i++) {
                int r = snake.get(i)[0];
                int c = snake.get(i)[1];
                if (r == tail[0] && c == tail[1]) continue;
                if (r == newRow && c == newCol) {
                    hitSelf = true;
                    break;
                }
            }

            if (hitSelf) {
                System.out.println("Cannot move " + direction +
                        " — snake head would hit its own body" +
                        " at row=" + newRow + " col=" + newCol + "!");
                printOpenDirections(headRow, headCol);
                return false;
            }

            // perform move
            snake.add(new int[]{newRow, newCol});
            grid[newRow][newCol] = 'o';

            int[] oldTail = snake.remove(0);
            grid[oldTail[0]][oldTail[1]] = '-';

            savedHeadRow = newRow;
            savedHeadCol = newCol;
        }

        return true;
    }


    // find snake — if already loaded from SNAKE line skip tracing
    static void findSnake() {
        if (!snake.isEmpty()) return;

        ArrayList<int[]> ends = new ArrayList<>();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (grid[row][col] == 'o') {
                    if (countNeighbors(row, col) <= 1) {
                        ends.add(new int[]{row, col});
                    }
                }
            }
        }

        if (ends.isEmpty()) return;

        int[] current = ends.get(0);
        int[] previous = null;

        while (current != null) {
            snake.add(current);
            if (snake.size() > ROWS * COLS) break;

            int[] next = null;
            int[][] dirs = {
                    {current[0]-1, current[1]},
                    {current[0]+1, current[1]},
                    {current[0],   current[1]-1},
                    {current[0],   current[1]+1}
            };

            for (int[] dir : dirs) {
                int r = dir[0];
                int c = dir[1];
                if (r < 0)     r = ROWS - 1;
                if (r >= ROWS) r = 0;
                if (c < 0)     c = COLS - 1;
                if (c >= COLS) c = 0;
                if (grid[r][c] != 'o') continue;
                if (previous != null && r == previous[0] && c == previous[1]) continue;
                boolean alreadyVisited = false;
                for (int[] visited : snake) {
                    if (visited[0] == r && visited[1] == c) {
                        alreadyVisited = true;
                        break;
                    }
                }
                if (alreadyVisited) continue;
                next = new int[]{r, c};
                break;
            }

            previous = current;
            current = next;
        }

        if (savedHeadRow != -1 && savedHeadCol != -1) {
            int[] lastEl = snake.get(snake.size() - 1);
            if (lastEl[0] != savedHeadRow || lastEl[1] != savedHeadCol) {
                Collections.reverse(snake);
            }
        }
    }

    // count neighbors with wrap
    static int countNeighbors(int row, int col) {
        int count = 0;
        int[][] dirs = {
                {row-1, col}, {row+1, col},
                {row, col-1}, {row, col+1}
        };
        for (int[] dir : dirs) {
            int r = dir[0];
            int c = dir[1];
            if (r < 0)     r = ROWS - 1;
            if (r >= ROWS) r = 0;
            if (c < 0)     c = COLS - 1;
            if (c >= COLS) c = 0;
            if (grid[r][c] == 'o') count++;
        }
        return count;
    }


    // print open directions
    static void printOpenDirections(int headRow, int headCol) {

        int[][] directions = {
                {headRow-1, headCol,    0},
                {headRow+1, headCol,    1},
                {headRow,   headCol-1,  2},
                {headRow,   headCol+1,  3}
        };
        String[] names = {"up", "down", "left", "right"};
        ArrayList<String> openDirs = new ArrayList<>();

        for (int[] dir : directions) {
            int r = dir[0];
            int c = dir[1];
            int nameIndex = dir[2];

            // apply wrap to check correct cell
            int wrappedR = r;
            int wrappedC = c;
            if (wrappedR < 0)     wrappedR = ROWS - 1;
            if (wrappedR >= ROWS) wrappedR = 0;
            if (wrappedC < 0)     wrappedC = COLS - 1;
            if (wrappedC >= COLS) wrappedC = 0;

            boolean blocked = false;
            for (int i = 1; i < snake.size(); i++) {
                if (snake.get(i)[0] == wrappedR && snake.get(i)[1] == wrappedC) {
                    blocked = true;
                    break;
                }
            }
            if (!blocked) openDirs.add(names[nameIndex]);
        }

        if (openDirs.isEmpty()) {
            System.out.println("There are no open directions.");
        } else if (openDirs.size() == 1) {
            System.out.println("The only open direction is " + openDirs.get(0));
        } else {
            StringBuilder msg = new StringBuilder("The only open directions are ");
            for (int i = 0; i < openDirs.size(); i++) {
                if (i == openDirs.size() - 1) {
                    msg.append("and ").append(openDirs.get(i));
                } else {
                    msg.append(openDirs.get(i)).append(", ");
                }
            }
            System.out.println(msg.toString());
        }
    }


    // reset map to original
    static void resetMap(Path mapPath) {
        String original =
                        "- - - - - - - - - - - - - - -\n" +
                        "- - - - - - - - - - - - - - -\n" +
                        "- - - - - - - - - - - - - - -\n" +
                        "- - - - - - - - - - - - - - -\n" +
                        "- - - - - - - - - - - - - - -\n" +
                        "- - - - - - - - - - - - - - -\n" +
                        "- - - - - - - - - - - - - - -\n" +
                        "- - - - - o o o o H - - - - -\n" +
                        "- - - - - - - - - - - - - - -\n" +
                        "- - - - - - - - - - - - - - -\n" +
                        "- - - - - - - - - - - - - - -\n" +
                        "- - - - - - - - - - - - - - -\n" +
                        "- - - - - - - - - - - - - - -\n" +
                        "- - - - - - - - - - - - - - -\n" +
                        "- - - - - - - - - - - - - - -\n";
        try {
            Files.writeString(mapPath, original);
        } catch (IOException e) {
            System.out.println("Error resetting map: " + e.getMessage());
        }
    }


    // save map with snake order at end
    static void saveMap(Path mapPath) {
        try {
            StringBuilder content = new StringBuilder();

            for (int row = 0; row < ROWS; row++) {
                for (int col = 0; col < COLS; col++) {
                    if (row == savedHeadRow && col == savedHeadCol) {
                        content.append("H");
                    } else {
                        content.append(grid[row][col]);
                    }
                    if (col < COLS - 1) content.append(" ");
                }
                content.append("\n");
            }

            // save snake positions in order — tail to head
            content.append("SNAKE:");
            for (int i = 0; i < snake.size(); i++) {
                content.append(snake.get(i)[0]).append(",").append(snake.get(i)[1]);
                if (i < snake.size() - 1) content.append(";");
            }
            content.append("\n");

            Files.writeString(mapPath, content.toString());

        } catch (IOException e) {
            System.out.println("Error saving map: " + e.getMessage());
        }
    }


    // print map
    static void printMap() {
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                char c = grid[row][col];
                System.out.print(c == 'H' ? "o" : c);
                if (col < COLS - 1) System.out.print(" ");
            }
            System.out.println();
        }
    }

}