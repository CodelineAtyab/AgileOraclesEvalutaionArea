package com.agileoracleseval.slitheringeval.mariyaallamki.ProjectSlithering;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.nio.file.*;
import java.util.*;


public class MoveSnake {

    public static Path mapFilePath = null;

    public static String mapFile = "src/main/java/com/agileoracleseval/slitheringeval/mariyaallamki/ProjectSlithering/map.txt";
    public static ArrayList<ArrayList<String>> grids = new ArrayList<>();
    public static ArrayList<int[]> snakeArrow = new ArrayList<>();




    public static void main(String[] args){
        if (args.length==0){
            System.out.println("Enter a Valid CMD code: java javaFileName -Direction- -Steps-");
            return;
        }


        String direction = args[0].toLowerCase();
        int snakeSteps = 1;

        if(args.length >=2){
            snakeSteps = Integer.parseInt(args[1]);
        }

        try{

            filePath();
            loadSnakeMap();
            getSnake();


            boolean passed = true;
            for(int x=0;x<snakeSteps; x++) {
                if (!moveSnake(direction)) {
                    passed = false;
                    break;
                }
            }
            if (!passed)
            {
                showDirection();
            }
            else{

                saveSnakeMap();
                printSnakePath();
            }
        }
        catch (Exception e){
            System.out.println("Error");
        }
    }




    public static void filePath() {
        mapFilePath = Paths.get(mapFile);
    }





    public static void loadSnakeMap() throws IOException {
        if(!Files.exists(mapFilePath)){
            System.out.println("File Not Found");
            System.exit(0);
        }

        String mapFileContent = Files.readString(mapFilePath); //Non-blocking approach
        String[] line = mapFileContent.split("\n");

        //2D Grid
        for( String l : line){
            if (l.trim().isEmpty())
                continue;


            String[] section = l.trim().split(" ");
            ArrayList<String> rows = new ArrayList<>(Arrays.asList(section));
            grids.add(rows);
        }
    }





    public static void getSnake() {
        snakeArrow.clear();
        for (int c=0;c<grids.size(); c++ )
        {
            for (int b=0;b<grids.get(c).size();b++)
            {
                //ArrayList<ArrayList<> usage
                if(grids.get(c).get(b).equals("o"))
                {
                    snakeArrow.add(new int[]{c,b}); //ArrayList<int[]> usage
                }
            }
        }
        snakeArrow.sort((x,y)->(x[1] != y[1]) ? x[1]-y[1]:x[0]-y[0]); //Rightmost is head
    }




    public static boolean moveSnake(String direction){
        int[] snakeHead = snakeArrow.get(snakeArrow.size() -1);

        int rows = snakeHead[0];
        int columns = snakeHead[1];


        if(direction.equals("up")) rows--;
        if(direction.equals("down")) rows++;
        if(direction.equals("right")) columns++;
        if(direction.equals("left")) columns--;

        //if(isInvalid(rows,columns)) return false;


        int[] head = {rows,columns};
        snakeArrow.add(head);
        grids.get(rows).set(columns,"o");

        int[] removeTail = snakeArrow.remove(0); //Remove old tail
        grids.get(removeTail[0]).set(removeTail[1],"-");
        return true;
    }




    public static void showDirection(){
        ArrayList<String> validDirection = new ArrayList<>();
        String[] directios = {"up","down","left","right"};

        int[] snakeHead = snakeArrow.get(snakeArrow.size()-1);

        for ( String dircts :directios){

            int rowDir = snakeHead[0];
            int columnDir = snakeHead[1];

            if (dircts.equals("down"))   rowDir++;
            else if (dircts.equals("up"))   rowDir--;
            else if (dircts.equals("right"))   columnDir++;
            else if (dircts.equals("left"))   columnDir--;

        }
        System.out.println("Open Directions are "+String.join(",",validDirection));

    }




    public static void saveSnakeMap() throws IOException{
        StringBuilder mapSave = new StringBuilder();
        for(ArrayList<String> rowMap:grids){
            mapSave.append(String.join("",rowMap)).append("\n");
        }
        Files.writeString(mapFilePath,mapSave.toString());
    }



    public static void printSnakePath(){
        for( ArrayList<String>  rowMap:grids ){
            System.out.println(String.join(" ",rowMap));
        }
    }


}

