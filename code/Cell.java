/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

/**
 *
 * @author Prateek
 */
public class Cell 
{
    //variables
    private int x, y, z; //the coordinates of the cell
    private String coord; //the coordinates of the cell used as an unique identifier
    private int type; //the type of cell: 0 = tumour cells; 1 = white blood cells; 2 = red blood cells;
    private boolean hasNanoVirus; //true if this cell currently has the nano virus, else false.
    private boolean isDestroyed; //true if this cell is destroyed by the nano virus, only if type = 0.
    
    
    //methods
    
    //constructor
    public Cell(int x, int y, int z, int t)
    {
        this.x = x; this.y = y; this.z = z; this.type = t;
        this.coord = x + ":" + y + ":" + z;
        this.hasNanoVirus = false;
        this.isDestroyed = false;
    }

    //accessors and modifiers.
    public String getCoord() 
    {
        return coord;
    }

    public void setCoord(String coord) 
    {
        this.coord = coord;
    }

    public int getType() 
    {
        return type;
    }

    public void setType(int type) 
    {
        this.type = type;
    }
    
    public void setCoord(int x, int y, int z)
    {
        setX(x);
        setY(y);
        setZ(z);
    }

    public int getX() 
    {
        return x;
    }

    public void setX(int x) 
    {
        this.x = x;
    }

    public int getY() 
    {
        return y;
    }

    public void setY(int y) 
    {
        this.y = y;
    }

    public int getZ() 
    {
        return z;
    }

    public void setZ(int z) 
    {
        this.z = z;
    }

    public boolean hasNanoVirus() 
    {
        return hasNanoVirus;
    }

    public void setNanoVirus(boolean hasNanoVirus) 
    {
        this.hasNanoVirus = hasNanoVirus;
    }

    public boolean isDestroyed() 
    {
        return isDestroyed;
    }

    public void destroyCell() 
    {
        this.isDestroyed = true;
    }
}
