package com.agileoracleseval.slitheringeval.alharithAlkindi;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class SlitheringCodeChallenge {
    public static void main(String[] args) throws IOException {
        System.out.println("------------------------------");
        System.out.println("---------Game Started---------");
        System.out.println("------------------------------");


        ArrayList<String[]> grid = new ArrayList<>();
        File readMap = new File("src/main/java/com/agileoracleseval/slitheringeval/alharithAlkindi/map.txt");

        try (Scanner mapScanner = new Scanner(readMap)) {
            while (mapScanner.hasNextLine()) {
                String data = mapScanner.nextLine();
                String[] row = data.split(" ");
                grid.add(row);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int rows = grid.size();
        int cols = grid.get(0).length;

        String[][] mapArray = new String[rows][cols];
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                mapArray[i][j] = grid.get(i)[j];


        LinkedList<int[]> snake = new LinkedList<>();
        for (int row = 0; row < mapArray.length; row++)
            for (int col = 0; col < mapArray[0].length; col++)
                if (mapArray[row][col].equals("o"))
                    snake.addFirst(new int[]{row, col}); // rightmost = head (getFirst)

        System.out.println("Snake Head : " + Arrays.toString(snake.getFirst()));
        System.out.println("Snake Tail : " + Arrays.toString(snake.getLast()));


        printMap(mapArray, rows, cols);


        Scanner input = new Scanner(System.in);
        boolean isMove = true;

        while (isMove) {
            System.out.println("Enter (W-A-S-D) to move  or Q to quit: ");
            String dir = input.nextLine();

            int[] head = snake.getFirst();
            int newRow = head[0];
            int newCol = head[1];

            switch (dir.toLowerCase()) {
                case "w":
                    System.out.println("Snake moves Up");
                    newRow--;
                    break;
                case "s":
                    System.out.println("Snake moves Down");
                    newRow++;
                    break;
                case "d":
                    System.out.println("Snake moves Right");
                    newCol++;
                    break;
                case "a":
                    System.out.println("Snake moves Left");
                    newCol--;
                    break;
                case "q":
                    System.out.println("Game Over! Goodbye.");
                    isMove = false;
                    continue;
                default:
                    System.out.println("Invalid direction! Use U, D, L, R");
                    continue;
            }


            if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols) {
                System.out.println("Cannot move there! Hit the wall.");
                continue;
            }


            int[] tail = snake.getLast();
            mapArray[tail[0]][tail[1]] = ".";


            snake.removeLast();


            snake.addFirst(new int[]{newRow, newCol});


            mapArray[newRow][newCol] = "o";


            System.out.println("Snake Head : " + Arrays.toString(snake.getFirst()));
            System.out.println("Snake Tail : " + Arrays.toString(snake.getLast()));
            printMap(mapArray, rows, cols);
        }

        input.close();
    }


    static void printMap(String[][] mapArray, int rows, int cols) {
        System.out.println("\n--- Current Map ---");
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                System.out.print(mapArray[i][j] + " ");
            }
            System.out.println();
        }
        System.out.println("-------------------\n");
    }
}