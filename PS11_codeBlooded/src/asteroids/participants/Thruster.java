package asteroids.participants;

import java.awt.Shape;
import java.awt.geom.Path2D;
import asteroids.game.Controller;
import asteroids.game.Participant;

public class Thruster extends Participant
{
    /** The outline of the ship */
    private Shape outline;

    /** Game controller */
    private Controller controller;

    public Thruster (double x, double y, double direction, Controller controller)
    {
        this.controller = controller;
        setPosition(x, y);
        setDirection(direction);
        
        Path2D.Double poly = new Path2D.Double();
        poly.moveTo(-14, 10);
        poly.lineTo(-23, 0);
        poly.lineTo(-14, -10);
        poly.closePath();
        outline = poly;
       
    }

    public void remove(Participant p)
    {
        expire(p);
    }
    
    @Override
    protected Shape getOutline ()
    {
        // TODO Auto-generated method stub
        return outline;
    }

    @Override
    public void collidedWith (Participant p)
    {
        
    }
    
    /**
     * Turns right by Pi/16 radians
     */
    public void turnRight ()
    {
        rotate(Math.PI / 16);
    }

    /**
     * Turns left by Pi/16 radians
     */
    public void turnLeft ()
    {
        rotate(-Math.PI / 16);
    }
    

}
