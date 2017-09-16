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
public class Cell2D extends Cell
{
    int a;
    int b;
    String coord;
    public Cell2D(int x, int y, int z, int t) 
    {
        super(x,y,z, t);
        
        //3d to 2d mapping model from :
        //http://anthony.liekens.net/index.php/Computers/RenderingTutorial3DTo2D
        this.a = (int)Math.round((double)((100*x)/(z+5000) ));
        this.b = (int)Math.round((double)((100*y)/(z+5000)) );
        this.coord = this.a + ":" + this.b;
    }
    
    @Override
    public String getCoord() 
    {
        return this.coord;
    }
   
    
    
}
