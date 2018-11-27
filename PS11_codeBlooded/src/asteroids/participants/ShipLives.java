package asteroids.participants;

import java.awt.Shape;
import java.awt.geom.Path2D;
import asteroids.game.Controller;
import asteroids.game.Participant;

public class ShipLives extends Participant
{
    /** The outline of the ship */
    private Shape outline;

    /** Game controller */
    private Controller controller;

    public ShipLives (int x, int y, double direction, Controller controller)
    {
        this.controller = controller;
        setPosition(x, y);
        setRotation(direction);
        setDirection(direction);
        
        Path2D.Double poly = new Path2D.Double();
        poly.moveTo(21, 0);
        poly.lineTo(-21, 12);
        poly.lineTo(-14, 10);
        poly.lineTo(-14, -10);
        poly.lineTo(-21, -12);
        poly.closePath();
        outline = poly;
        
    }

    @Override
    public void collidedWith (Participant p)
    {
        
    }
    
    public void remove()
    {
        Participant.expire(this);
    }

    @Override
    protected Shape getOutline ()
    {
        // TODO Auto-generated method stub
        return outline;
    }
}
