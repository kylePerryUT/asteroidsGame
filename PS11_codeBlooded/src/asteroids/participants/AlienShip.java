package asteroids.participants;

import java.awt.Shape;
import java.awt.geom.Path2D;
import asteroids.game.Participant;

public class AlienShip extends Participant
{
    /** The outline of the alien ship */
    private Shape outline;
    
    
    
    public AlienShip (int x, int y, double direction)
    {
        // Set the initial position of the alien ship.
        setPosition(x, y);
       
        // Draw the top part of the alien ship.
        Path2D.Double alienShip = new Path2D.Double();
        alienShip.moveTo(-14, -13);
        alienShip.lineTo(-9, -26);
        alienShip.lineTo(9, -26);
        alienShip.lineTo(14, -13);
        alienShip.closePath();
        // Draw the middle part of the alien ship.
        alienShip.moveTo(-30, 0);
        alienShip.lineTo(-15, -13);
        alienShip.lineTo(15, -13);
        alienShip.lineTo(30, 0);
        alienShip.closePath();
        // Draw the bottom part of the alien ship.
        alienShip.moveTo(-30, 0);
        alienShip.lineTo(-14, 15);
        alienShip.lineTo(14, 15);
        alienShip.lineTo(30, 0);
        alienShip.closePath();
        
//        // Draw the top part of the alien ship.
//        Path2D.Double alienShipTop = new Path2D.Double();
//        alienShipTop.moveTo(-20, 5);
//        alienShipTop.lineTo(-18, 10);
//        alienShipTop.lineTo(18, 10);
//        alienShipTop.lineTo(20, 5);
//        alienShipTop.closePath();
//        // Draw the middle part of the alien ship.
//        Path2D.Double alienShipMid = new Path2D.Double();
//        alienShipMid.moveTo(-25, 0);
//        alienShipMid.lineTo(-20, 5);
//        alienShipMid.lineTo(20, 5);
//        alienShipMid.lineTo(25, 0);
//        alienShipMid.closePath();
//        // Draw the bottom part of the alien ship.
//        Path2D.Double alienShipBott = new Path2D.Double();
//        alienShipBott.moveTo(-25, 0);
//        alienShipBott.lineTo(-20, -5);
//        alienShipBott.lineTo(20, -5);
//        alienShipBott.lineTo(25, 0);
//        alienShipBott.closePath();
//        // Append all parts of the ship together.
//        Path2D.Double alienShip = new Path2D.Double();
//        alienShip.append(alienShipBott, false);
//        alienShip.append(alienShipMid, false);
//        alienShip.append(alienShipTop, false);
        
        outline = alienShip;
    }

    @Override
    protected Shape getOutline ()
    {
        return outline;
    }
    
    @Override
    public void collidedWith (Participant p)
    {
        // TODO Auto-generated method stub
        
    }

}