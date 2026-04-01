package com.agileoracleseval.slitheringeval.tibyan_saad.ProjectSlithering;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MoveSnake {

    public static void main(String[] args) throws InterruptedException, URISyntaxException {
        char[][] mapArray2D;
        Path snakeFile = Path.of("src/main/java/com/agileoracleseval/slitheringeval/tibyan_saad/ProjectSlithering/map.txt");
        try {
            // read file
            ArrayList<String> mapArray = (ArrayList<String>) Files.readAllLines(snakeFile);
            for (int i = 0; i < mapArray.size(); i++) {
                mapArray.set(i, mapArray.get(i).replace(" ", "")); // remove the spaces
            }


            mapArray2D = new char[mapArray.size()][mapArray.get(0).length()];// convert to 2d array w/row and column length
            for (int i = 0; i < mapArray.size(); i++) {
                mapArray2D[i] = mapArray.get(i).toCharArray();// convert to char 2d array
            }
        } catch (IOException e) {
            System.err.println("ERROR READING FILE: " + e.getMessage()); // misreading the file
            throw new RuntimeException(e);// exception handling of reading file
        }


        ArrayList<int[]> snakeBody = new ArrayList<>();

        // Read snake positions from snakeCoordinates.txt to save previous snake coordinates
        try {
            List<String> coordinate = Files.readAllLines(Path.of("src/main/java/com/agileoracleseval/slitheringeval/tibyan_saad/ProjectSlithering/snakeCoordinates.txt"));
            for (String sCoordinate : coordinate) {
                sCoordinate = sCoordinate.trim();

                // Split by comma
                String[] parts = sCoordinate.split(",");
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);

                snakeBody.add(new int[]{row, col});
            }
        } catch (IOException e) {
            System.err.println("ERROR READING FILE: " + e.getMessage()); // misreading the file
            throw new RuntimeException(e);// exception handling of file writing
        }

        // args[0] -> direction
        // args[1] -> steps

        if (args.length == 0){
            System.out.println("Please specify the direction");
        }else{
            int steps = 1; // if steps not mentioned then default is 1
            if (args.length > 1) {
                if (!args[1].isEmpty()) {
                    steps = Integer.parseInt(args[1]);
                    if (steps <= 0) {
                        System.out.println("Steps must be a positive value");
                    }
                }
            }

            int[] head = snakeBody.get(0); // get head location
            int headRowComparison = head[0];
            int headColumnComparison = head[1];

            int[] bodyOne = snakeBody.get(1);// body segment after head location
            int afterHeadRow = bodyOne[0];
            int afterHeadColumn = bodyOne[1];

            // moving in the upper direction
            if (args[0].equalsIgnoreCase("up")){
                for (int move = 0; move < steps; move++) {
                    int[] currentHead = snakeBody.get(0);// current head location
                    int newHeadRow = currentHead[0] - 1; // move up
                    int newHeadCol = currentHead[1];// same column
                    if (invalidMovementCondition(afterHeadRow, afterHeadColumn, newHeadRow, newHeadCol, headRowComparison,headColumnComparison) && notBodyCollision(newHeadRow,newHeadCol)){
                        if (snakeNotOutside(newHeadRow, newHeadCol, mapArray2D,snakeBody)) {
                            snakeBody.add(0, new int[]{newHeadRow, newHeadCol});// add new head at the front of the list
                            mapArray2D[newHeadRow][newHeadCol] = 'o'; // add new head for movement
                            int[] oldTail = snakeBody.remove(snakeBody.size() - 1);// remove old tail to keep snake length constant
                            mapArray2D[oldTail[0]][oldTail[1]] = '-'; // remove old tail for movement
                            displayMap(mapArray2D,snakeBody);
                            writeMap(snakeBody,mapArray2D);
                            snakeTrackingFile(snakeBody);
                        }
                    }
                }
            }
            // moving in the downwards direction
            else if (args[0].equalsIgnoreCase("down")){
                for (int move = 0; move < steps; move++) {
                    int[] currentHead = snakeBody.get(0);
                    int newRow = currentHead[0] + 1; // move down
                    int newCol = currentHead[1];// same column
                    if (invalidMovementCondition(afterHeadRow, afterHeadColumn,newRow,newCol, headRowComparison,headColumnComparison)&& notBodyCollision(newRow,newCol)){
                        if (snakeNotOutside(newRow, newCol, mapArray2D,snakeBody)) {
                            snakeBody.add(0, new int[]{newRow, newCol});
                            mapArray2D[newRow][newCol] = 'o';
                            int[] oldTail = snakeBody.remove(snakeBody.size() - 1);
                            mapArray2D[oldTail[0]][oldTail[1]] = '-';
                            displayMap(mapArray2D,snakeBody);
                            writeMap(snakeBody,mapArray2D);
                            snakeTrackingFile(snakeBody);
                        }
                    }
                }
            }
            // moving in the left direction
            else if (args[0].equalsIgnoreCase("left")){
                for (int move = 0; move < steps; move++) {
                    int[] currentHead = snakeBody.get(0);
                    int newRow = currentHead[0]; // same row
                    int newCol = currentHead[1] - 1;// go left
                    if (invalidMovementCondition(afterHeadRow, afterHeadColumn,newRow,newCol, headRowComparison,headColumnComparison)&& notBodyCollision(newRow,newCol)){
                        if (snakeNotOutside(newRow, newCol, mapArray2D,snakeBody)) {
                            snakeBody.add(0, new int[]{newRow, newCol});
                            mapArray2D[newRow][newCol] = 'o';
                            int[] oldTail = snakeBody.remove(snakeBody.size() - 1);
                            mapArray2D[oldTail[0]][oldTail[1]] = '-';
                            displayMap(mapArray2D,snakeBody);
                            writeMap(snakeBody,mapArray2D);
                            snakeTrackingFile(snakeBody);
                        }
                    }
                }
            }
            // moving in the right direction
            else if (args[0].equalsIgnoreCase("right")){
                for (int move = 0; move < steps; move++) {
                    int[] currentHead = snakeBody.get(0);
                    int newRow = currentHead[0]; // same row
                    int newCol = currentHead[1] + 1;// go right
                    if (invalidMovementCondition(afterHeadRow, afterHeadColumn,newRow,newCol, headRowComparison,headColumnComparison)&& notBodyCollision(newRow,newCol)){
                        if (snakeNotOutside(newRow, newCol, mapArray2D,snakeBody)) {
                            snakeBody.add(0, new int[]{newRow, newCol});
                            mapArray2D[newRow][newCol] = 'o';
                            int[] oldTail = snakeBody.remove(snakeBody.size() - 1);
                            mapArray2D[oldTail[0]][oldTail[1]] = '-';
                            displayMap(mapArray2D,snakeBody);
                            writeMap(snakeBody,mapArray2D);
                            snakeTrackingFile(snakeBody);
                        }
                    }
                }
            }
            else {
                System.out.println("Direction is not valid. \n Choose Up, Down, Left or Right.");
            }
        }



    }

    //snake doesnt go out of map
    public static boolean snakeNotOutside(int newHeadRow, int newHeadColumn, char[][] mapArray2D, ArrayList<int[]> snakeBody ){
        int maxRow = mapArray2D.length;
        int maxCol = mapArray2D[0].length;

        if (newHeadRow >= 0 && newHeadRow < maxRow && newHeadColumn >= 0 && newHeadColumn < maxCol) {
            return true;
        } else {
            return false;
        }
    }
    //display map
    public static void displayMap(char[][] mapArray2D,ArrayList<int[]> snakeBody) throws InterruptedException {

        for (char[] row : mapArray2D) {
            for (char space : row) {
                System.out.print(space + " ");
            }
            System.out.println();
        }
        for (int[] coord : snakeBody) {
            System.out.println(coord[0] + "," + coord[1]);
        }
        for (int space =0;space<7;space++){
            System.out.println();
        }
        Thread.sleep(1000);
    }

    // write to map.txt
    public static void writeMap(ArrayList<int[]> snakeBody, char[][] mapArray2D) throws URISyntaxException {

        for (int[] body : snakeBody) {
            mapArray2D[body[0]][body[1]] = 'o';// update snake positions
        }
        Path snakeFile = Path.of("src/main/java/com/agileoracleseval/slitheringeval/tibyan_saad/ProjectSlithering/map.txt");// write map to file
        List<String> mapLines = new ArrayList<>();
        for (char[] row : mapArray2D) {
            String line = "";
            for (int i = 0; i < row.length; i++) {
                line += row[i];
                if (i < row.length - 1) {
                    line += " "; // add space after printing and not removing them
                }
            }
            mapLines.add(line);
        }

        try {
            Files.write(snakeFile, mapLines); // overwrite the file
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // to save the state of snake every run and keep track of head location
    public static void snakeTrackingFile(ArrayList<int[]> snakeBody){
        try {
            FileWriter writer = new FileWriter("src/main/java/com/agileoracleseval/slitheringeval/tibyan_saad/ProjectSlithering/snakeCoordinates.txt");

            for (int[] snakeCoord : snakeBody) {
                // Write each coordinate pair tgt
                writer.write(snakeCoord[0] + "," + snakeCoord[1] + "\n");
            }
            System.out.println();

            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    //prevent snake invalid movement(head collision with prev body segment)
    public static Boolean invalidMovementCondition(int afterHeadRow, int afterHeadColumn, int newHeadRow, int newHeadCol, int headRowComparison, int headColumnComparison){
        // compare head coordinates with body coordinates thts before head
        if (afterHeadRow==newHeadRow && afterHeadColumn==newHeadCol){ // if new head value = 1st body segment value
            if(headRowComparison<newHeadRow){// down
                System.out.println("Only valid directions are left, right and up");
                return false;
            }
            if(headRowComparison>newHeadRow){// up
                System.out.println("Only valid directions are left, right and down");
                return false;
            }
            if(headColumnComparison<newHeadCol){// right
                System.out.println("Only valid directions are left, up and down");
                return false;
            }
            if(headColumnComparison>newHeadCol){// left
                System.out.println("Only valid directions are up, right and down");
                return false;
            }
        }
        return true;
    }

    // snake head w/body collision
    public static Boolean notBodyCollision(int newHeadRow, int newHeadCol) {
        HashSet<String> snakeBodyCollision = new HashSet<>();
        try {
            List<String> coordinate = Files.readAllLines(
                    Path.of("src/main/java/com/agileoracleseval/slitheringeval/tibyan_saad/ProjectSlithering/snakeCoordinates.txt")
            );
            for (String sCoordinate : coordinate) {
                sCoordinate = sCoordinate.trim();

                // Split by comma
                String[] parts = sCoordinate.split(",");
                int row = Integer.parseInt(parts[0]);
                int col = Integer.parseInt(parts[1]);

                // Store as "row,col"
                snakeBodyCollision.add(row + "," + col);
            }
        } catch (IOException e) {
            System.err.println("ERROR READING FILE: " + e.getMessage());
            throw new RuntimeException(e);
        }

        // Represent new head as string
        String newHead = newHeadRow + "," + newHeadCol;

        if (snakeBodyCollision.contains(newHead)) {
            System.out.println("Body collision");
            return false;
        }

        return true;
    }


}

//- - - - - - - - - - - - - - -
//- - - - - - - - - - - - - - -
//- - - - - - - - - - - - - - -
//- - - - - - - - - - - - - - -
//- - - - - - - - - - - - - - -
//- - - - - - - - - - - - - - -
//- - - - - - - - - - - - - - -
//- - - - - o o o o o - - - - -
//- - - - - - - - - - - - - - -
//- - - - - - - - - - - - - - -
//- - - - - - - - - - - - - - -
//- - - - - - - - - - - - - - -
//- - - - - - - - - - - - - - -
//- - - - - - - - - - - - - - -
//- - - - - - - - - - - - - - -

// initial coordinates
//7,9
//7,8
//7,7
//7,6
//7,5