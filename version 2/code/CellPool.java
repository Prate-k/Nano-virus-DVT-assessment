/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JPanel;

/**
 *
 * @author Prateek
 */


public class CellPool extends JPanel
{
    //constants
    private static final int MAX_X = 5000, MAX_Y = 5000, MAX_Z = 5000; //maximum coordinates
    private static final int MIN_X = 1, MIN_Y = 1, MIN_Z = 1; //minimum coordinates
    private static final int POOL_SIZE = 100; //number of cells
    private static final int T_CELLS = (int)((5.0 / POOL_SIZE) * POOL_SIZE); //tumour cells 5%
    private static final int W_CELLS = (int)((25.0 / POOL_SIZE)* POOL_SIZE); //white blood cells 25%
    private static final int R_CELLS = (int)((70.0 / POOL_SIZE) * POOL_SIZE); //red blood cells 70%
    private static final double MAX_DIST = Math.sqrt(((1-5000)^2)*3) + 1; // maximum possible distance on region from 1,1,1 to 5000,5000,5000
    
 
    //variables
    //cells array
    private ArrayList<Cell> pool = new ArrayList<>(); //array to store the randomly generated cells
    
    //arrays to store the specified amount of cells
    private ArrayList<Cell> tumourCells = new ArrayList<>();
    private ArrayList<Cell> redCells = new ArrayList<>();
    private ArrayList<Cell> whiteCells = new ArrayList<>();
    
    
    private Random random = new Random();
    private int cycleCounter = 0; //cycle counter
    private Cell nanoAffected = null; //keeping track of the nano virus
    boolean run  = true; //ending conditions
    private int numDestroyedCells = 0; //number of tumour cells destroyed by the nano virus
    
    //file handlers
    File file = new File("log.txt"); //new text file for logging output
    File map = new File("map.txt"); //new text file to map the cells and nano virus per cycle
    FileWriter fileWriter = null; //file writer to write to the file
    
    
    //methods
    
    //constructor - populating the cells and creating a text file for logging
    public CellPool()
    {        
        try 
        {   
            if(file.exists())
            {
                file.delete();
            }
            if(map.exists())
            {
                map.delete();
            }
            while(!populatePool()); //while loop until a distinct set of coordinates are generated for the cells.
            assignTypes(); //classify randomly generated cells
            
            log("------------------------------------- \r\n");
            log("Format: \r\n<Cycle n> \r\n");
            log("\t Tumour cells <T> : White blood cells <W> : Red blood cells <R> : Total cells <S> \r\n");
            log("Details of executions... \r\n");
            log("------------------------------------- \r\n\r\n");
            log("Cells generated \r\n");
            
        }
        catch(IOException e)
        {
            //error opening file
        }       
        insertNanoVirus();
        plot("Final");
    }
    
    //populating the cells randomly and making sure each cell has a distinct coordinate.
    private boolean populatePool()
    {
        boolean hasDistinctPoints = false;
        
        for (int i = 0; i < POOL_SIZE; i++) 
        {
            //generate random coordinates
            int randX = (random.nextInt(MAX_X-MIN_X) + MIN_X);
            int randY = (random.nextInt(MAX_Y-MIN_Y) + MIN_Y);
            int randZ = (random.nextInt(MAX_Z-MIN_Z) + MIN_Z);
            
            boolean pointTaken = false;
            String coord = randX + ":" + randY + ":" + randZ;
            for(int j = 0; j < i; j ++) //check if coordinates were previously used
            {
                if(pool.get(j).getCoord().equalsIgnoreCase(coord))
                {
                    pointTaken = true;
                    break;
                }
            }

            if(!pointTaken) //if distinct, then use coordinates
            {
                pool.add(i,  new Cell(randX, randY, randZ, -1));
            }
            else
            {
                i --;
            }
            
        }
        
        //double checking for no overlaps, can be removed.
        String coords [] = new String[100];
        for (int i = 0; i < POOL_SIZE; i++) 
        {
           coords[i] = pool.get(i).getCoord();
        }
        
        for (int i = 0; i < POOL_SIZE; i++) 
        {
            for (int j = 0; j < POOL_SIZE; j++) 
            {
                if (coords[j].equalsIgnoreCase(coords[j]))
                {
                    hasDistinctPoints = true;
                    break;
                }
            }
        }
        
        return hasDistinctPoints;
    }
    
    //specifying the types of cells given as probabilities.
    private void assignTypes()
    {       
        for (int i = 0; i < T_CELLS; i++) //tumour cells
        {
            pool.get(i).setType(0);
            tumourCells.add(i, pool.get(i));
        }
        for (int i = 0; i < W_CELLS; i++) //white blood cells
        {
            pool.get(T_CELLS + i).setType(1);
            whiteCells.add(i, pool.get(T_CELLS + i));
        }
        for (int i = 0; i < R_CELLS; i++) //red blood cells
        {
            pool.get(W_CELLS + i).setType(2);
            redCells.add(i, pool.get(W_CELLS + i));
        }
    }
    
    
    //Performing the tasks, as specified, per cycle.
    private void insertNanoVirus()
    {
        try
        {
            //randomise starting point for nano virus within redblood cells.
            int startPoint = random.nextInt(redCells.size());       
            redCells.get(startPoint).setNanoVirus(true);
            nanoAffected = redCells.get(startPoint);
            
            log("The nano virus inserted at (format- x:y:z)> " + nanoAffected.getCoord() + "\r\n");
            log("Initial Cell count: " );
            String out = "<T " + tumourCells.size() 
                            + "> : <W " + whiteCells.size() + 
                            "> : <R " + redCells.size() +
                            "> : <S " + totalCells() + ">\r\n\r\n";
            log(out);
            plot("Initial");
            while(run) //run until tumour cells take over or are completely detroyed
            {
                
                log("<Cycle " + cycleCounter + ">\r\n");
                
                nanoAffected = nanoAction(nanoAffected); //updating nano virus position                                               

                if(cycleCounter % 5 == 0) //tumour cells spreading every 5th cycle
                {
                    log("Spreading tumour...\r\n");
                    spreadTumour();
                }
                
                
                out = "\t <T " + tumourCells.size() 
                            + "> : <W " + whiteCells.size() + 
                            "> : <R " + redCells.size() +
                            "> : <S " + totalCells() + ">\r\n\r\n";
                log(out);
                plot("Cycle");

                if((whiteCells.isEmpty() && redCells.isEmpty()) || tumourCells.isEmpty()) //terminating condition
                {
                    run = false;
                    break;
                }
                cycleCounter ++;
            }
            
            log("\r\nTotal number of tumour cells destroyed: " + numDestroyedCells + "\r\n");
            log("\r\nTotal number of cycles: " + cycleCounter + "\r\n\r\n");
            
            if(whiteCells.isEmpty() && redCells.isEmpty()) //if no more white and red blood cells
            {
                log("\r\n\t***The tumour has taken over all the cells***\r\n\r\n");
            }
            else
            {
                if(tumourCells.isEmpty()) //if no more tumour cells
                {
                    log("\r\n\t***The tumour has been eliminated***\r\n\r\n");
                }
            }
            
            log("\r\n\r\n\r\n");
            log("\t***Additional details***\r\n\r\n");
            log("Initial positions of cells:\r\n\r\n");
            
            String data = "Tumour cells>\r\n\r\n";
            for (int i = 0; i < T_CELLS; i++) //tumour cells
            {
                data += pool.get(i).getCoord() + "\t";        
                if((i+1) % 5 == 0)
                {
                    data += "\r\n";
                }
            }
            data += "\r\nWhite blood cells>\r\n\r\n";
            for (int i = 0; i < W_CELLS; i++) //white blood cells
            {
                data += pool.get(T_CELLS + i).getCoord() + "\t";
                if((i+1) % 5 == 0)
                {
                    data += "\r\n";
                }
                
            }
            data += "\r\nRed blood cells>\r\n\r\n";
            for (int i = 0; i < R_CELLS; i++) //red blood cells
            {
                data += pool.get(W_CELLS + i).getCoord() + "\t";
                if((i+1) % 5 == 0)
                {
                    data += "\r\n";
                }
            }
            log(data);
          
            
            log("\r\n\r\nFinal positions of cells:\r\n\r\n");
            data = "Tumour cells>\r\n\r\n";
            for (int i = 0; i < tumourCells.size(); i++) //tumour cells
            {
                data += tumourCells.get(i).getCoord() + "\t";        
                if((i+1) % 5 == 0)
                {
                    data += "\r\n";
                }
            }
            data += "\r\nWhite blood cells>\r\n\r\n";
            for (int i = 0; i < whiteCells.size(); i++) //white blood cells
            {
                data += whiteCells.get(i).getCoord() + "\t";
                if((i+1) % 5 == 0)
                {
                    data += "\r\n";
                }
                
            }
            data += "\r\nRed blood cells>\r\n\r\n";
            for (int i = 0; i < redCells.size(); i++) //red blood cells
            {
                data += redCells.get(i).getCoord() + "\t";
                if((i+1) % 5 == 0)
                {
                    data += "\r\n";
                }
            }
            log(data);
            
            
            
            
        }catch(IOException e)
        {
        
        }
        
        
    }
    
    //calculating the distance between the 2 cells using the distance formula.
    private double calcDist(Cell cell1, Cell cell2)
    {
        int x1 = cell1.getX();  int x2 = cell2.getX();
        int y1 = cell1.getY();  int y2 = cell2.getY();
        int z1 = cell1.getZ();  int z2 = cell2.getZ();
       
        int x = (x1-x2)^2;
        int y = (y1-y2)^2;
        int z = (z1-z2)^2;
        
        return Math.sqrt(x+y+z);
    }
    
    //spreading the tumour to nearby cells every 5th cycle
    //list used to store the new possible infected cells.
    //first focusing on the red blood cells then the white blood cells.
    private void spreadTumour()
    {
        ArrayList<Cell> newInfected = new ArrayList<>();
        for (int i = 0; i < tumourCells.size(); i++) //allowing all the current tumour cells to infect nearby cells
        {
            double minDist = MAX_DIST; //dummy value
            int minIndex = 0;
            Cell temp = null;
            if(!redCells.isEmpty()) //if there are still red blood cells
            {
                
                for (int j = 0; j < redCells.size(); j++) 
                {
                    double dist = calcDist(tumourCells.get(i), redCells.get(j));
                    if(dist < minDist) //record the nearest one
                    {
                        minDist = dist;
                        minIndex = j;
                    }
                }
                temp = redCells.remove(minIndex); //remove the newly infected cell from the red blood cells to avoid reuse.
            }
            else //if no red blood cells remaining
            {
                if(!whiteCells.isEmpty()) //if there are still white blood cells
                {
                    for (int j = 0; j < whiteCells.size(); j++) 
                    {
                        
                        double dist = calcDist(tumourCells.get(i), whiteCells.get(j));
                        if(dist < minDist) //record the nearest one
                        {
                            minDist = dist;
                            minIndex = j;
                        }                        
                    }
                    temp = whiteCells.remove(minIndex); //remove the newly infected cell from the red blood cells to avoid reuse.
                }
            }
            if(temp != null) //if any nodes were found.
            {
                temp.setType(0); //set as infected by tumour
                newInfected.add(temp); //added to list           
            }
        }
        tumourCells.addAll(newInfected);   //add all entries in the list to the list of tumour cells.
    }
 
    //updating the pool after changing it by actions such as destroying of tumour cells by the nano virus
    private void updatePool(int type) //type specifies the type of update.
    {
        if(type == 0) //remove destroyed cells from the tumour cells list.
        {
             for (int i = 0; i < tumourCells.size(); i++) 
             {
                 if(tumourCells.get(i).isDestroyed())
                 {
                     tumourCells.remove(i);
                     numDestroyedCells ++;                         
                 }
             }
        }     
    }
    
    
    //the 3 specific actions the nano virus can perform
    //1st if- is to check if the nano virus is on a tumour cell it destroyed in the previous cycle, if so, move.
    //2nd if- to check if nano virus is on a tumour cell which still needs to be destroyed, if so, destroy cell.
    //3rd if- if its on a red/white blood cell, if so, move to a random cell within 5000 units.
    private Cell nanoAction(Cell nano)
    {        
        Cell temp = nano;
        try
        {
            //check if nano on tumour it previously destroyed
            if(nano.getType() == 0 && nano.isDestroyed())
            {
                temp = moveNano(nano); //move nano to a random cell within 5000 units
                updatePool(0); //update pool as to remove destroyed cells in the previous cycle
                if(!temp.getCoord().equals(nano.getCoord()))
                {
                    log("Nano virus moved from > " + nano.getCoord() + " to > " + temp.getCoord() + "\r\n");
                }
                else
                {
                    log("Nano virus took no action this cycle\r\n");
                }
                return temp;
            }

            if(nano.getType() == 0  && !nano.isDestroyed())
            {
                log("Nano virus at > " + nano.getCoord() + " destroying a tumour cell...\r\n");
                
                destroyTumour(nano); //mark the tumour cell with the nano virus  as destroyed for the next cycle
                return temp;
            }

            //if on any other cell move or do nothing if no cell within 5000
            if(nano.getType() != 0)
            {
                temp = moveNano(nano); //simple move to a random cell within 5000 units, if any. Otherwise do nothing.
                if(!temp.getCoord().equals(nano.getCoord()))
                {
                    log("Nano virus moved from > " + nano.getCoord() + " to > " + temp.getCoord() + "\r\n");
                }
                else
                {
                    log("Nano virus took no action this cycle\r\n");
                }
            }

            
        }
        catch(IOException e)
        {
            
        }
        return temp;
    }
    
    //moving the nano virus to any random cell that is withing 5000 units of its current position
    private Cell moveNano(Cell nano)
    {
        ArrayList<Cell> moveTo = new ArrayList<>();
        moveTo.add(nano); //add the current position incase there are no other new positons available.
        int counter = 1;
        for (int i = 0; i < tumourCells.size(); i++) //check through all tumour cells for any in close vicinity.
        {
            double dist = calcDist(nano, tumourCells.get(i));
            if(dist <= 5000)
            {
                moveTo.add(tumourCells.get(i)); //add to list if within 5000 units
                counter ++;
            }
            if(nano.getType() == 0 && nano.getCoord().equals(tumourCells.get(i).getCoord()))
            {
                tumourCells.get(i).setNanoVirus(false);
            }
        }
        
        for (int i = 0; i < whiteCells.size(); i++) //check through all white blood cells for any in close vicinity.
        {
            double dist = calcDist(nano, whiteCells.get(i));
            if(dist <= 5000)
            {
                moveTo.add(whiteCells.get(i)); //add to list if within 5000 units
                counter ++;
            }
            
            if(nano.getType() == 1 && nano.getCoord().equals(whiteCells.get(i).getCoord()))
            {
                whiteCells.get(i).setNanoVirus(false);
                
            }
        }
        
        for (int i = 0; i < redCells.size(); i++) //check through all red blood cells for any in close vicinity.
        {
            double dist = calcDist(nano, redCells.get(i));
            if(dist <= 5000)
            {
                moveTo.add(redCells.get(i)); //add to list if within 5000 units
                counter ++;
            }
            if(nano.getType() == 2 && nano.getCoord().equals(redCells.get(i).getCoord()))
            {
                redCells.get(i).setNanoVirus(false);
            }
        }
        
        int randomMove = random.nextInt(counter); //generate a random index to move the nano virus to from the list.
        switch(moveTo.get(randomMove).getType()) //using a switch to update only the correct list of cells to set as having the nano virus.
        {
            case 0: 
                for (int i = 0; i < tumourCells.size(); i++) 
                {
                    if(tumourCells.get(i).getCoord().equals(moveTo.get(randomMove).getCoord()))
                    {
                        tumourCells.get(i).setNanoVirus(true);
                        return tumourCells.get(i);
                    }
                }
                break;
            case 1:
                for (int i = 0; i < whiteCells.size(); i++) 
                {
                    if(whiteCells.get(i).getCoord().equals(moveTo.get(randomMove).getCoord()))
                    {
                        whiteCells.get(i).setNanoVirus(true);
                        return whiteCells.get(i);
                    }
                }
                break;
            case 2:
                for (int i = 0; i < redCells.size(); i++) 
                {
                    if(redCells.get(i).getCoord().equals(moveTo.get(randomMove).getCoord()))
                    {
                        redCells.get(i).setNanoVirus(true);
                        return redCells.get(i);
                    }
                }
                break;
        }
        return nano;
    }
    
    //marking the tumour cell with the nano virus so it can be destroyed in the next cycle and the nano virus can be moved.
    private void destroyTumour(Cell nano)
    {
        for (int i = 0; i < tumourCells.size(); i++) 
        {
            if(nano.getCoord().equals(tumourCells.get(i).getCoord()))
            {
                tumourCells.get(i).destroyCell();
                return;
            }
        }
    }
    
    //keeping track of the total number of cells to validate the number of destroyed tumour cells.
    private int totalCells()
    {
        return tumourCells.size() + redCells.size() + whiteCells.size();
    }
    
    //writing to a file used tutorial point example as a guideline
    private void log(String msg) throws IOException
    {
        fileWriter = new FileWriter(file, true); 
        fileWriter.write(msg); 
        fileWriter.flush();
        fileWriter.close();
    }
    
    
    private void plot(String type)
    {
        char array [][] = new char[POOL_SIZE][POOL_SIZE]; //the map of size 100 by 100 
        Cell2D arrayT [] = new Cell2D[tumourCells.size()];
        Cell2D arrayW [] = new Cell2D[whiteCells.size()];
        Cell2D arrayR [] = new Cell2D[redCells.size()];
        
        for (int i = 0; i < POOL_SIZE; i++) 
        {
            for (int j = 0; j < POOL_SIZE; j++) 
            {
                array[i][j] = ' ';
            }
        }
        
        for (int i = 0; i < arrayT.length; i++) 
        {
            Cell temp = tumourCells.get(i);
            arrayT[i] = new Cell2D(temp.getX(), temp.getY(), temp.getZ(), temp.getType());
            if(temp.hasNanoVirus())
            {
                array[arrayT[i].a][arrayT[i].b] = 'T';
            }
            else
            {
                array[arrayT[i].a][arrayT[i].b] = 't';
            }
            
            //System.out.println(tumourCells.get(i).getCoord() + "\t" + arrayT[i].getCoord());
        }
        for (int i = 0; i < arrayW.length; i++) 
        {
            Cell temp = whiteCells.get(i);
            arrayW[i] = new Cell2D(temp.getX(), temp.getY(), temp.getZ(), temp.getType());
            if(temp.hasNanoVirus())
            {
                array[arrayW[i].a][arrayW[i].b] = 'W';
            }
            else
            {
                array[arrayW[i].a][arrayW[i].b] = 'w';
            }            
        }
        for (int i = 0; i < arrayR.length; i++) 
        {
            Cell temp = redCells.get(i);
            arrayR[i] = new Cell2D(temp.getX(), temp.getY(), temp.getZ(), temp.getType());
            if(temp.hasNanoVirus())
            {
                array[arrayR[i].a][arrayR[i].b] = 'R';
            }
            else
            {
                array[arrayR[i].a][arrayR[i].b] = 'r';
            }
        }
        
        try
        {
            fileWriter = new FileWriter(map, true); 
            if(type.equals("Final"))
            {
                fileWriter.write("_________FINAL__________\r\n");
            }
            if(type.equals("Initial"))
            {
                fileWriter.write("_________INITIAL__________\r\n");
            }
            if(type.equals("Cycle"))
            {
                fileWriter.write("_________CYCLE " + cycleCounter + "__________\r\n");
            }
            fileWriter.flush();
            
            for (int i = 0; i < POOL_SIZE; i++) 
            {
                String msg = "";
                for (int j = 0; j < POOL_SIZE; j++) 
                {
                    msg += array[i][j] + " ";
                }
                msg += "\r\n";
                fileWriter.write(msg); 
                fileWriter.flush();
            }
            fileWriter.write("_________________________________________________\r\n\r\n");
            fileWriter.flush();
            fileWriter.close();
        }
        catch(IOException e)
        {
            
        }
        
        
    }
    
    
}
