package asteroids.participants;

import static asteroids.game.Constants.*;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import asteroids.game.Controller;
import asteroids.game.Participant;
import asteroids.game.ParticipantCountdownTimer;

public class Debris extends Participant
{
    /** The outline of the bullet */
    private Shape outline;

    /** Game controller */
    private Controller controller;

    /**
     * Constructs a bullet at the specified coordinates that is pointed in the given direction.
     */
    public Debris (double x, double y, Controller controller, String type, int size)
    {
        this.controller = controller;
        setPosition(x, y);
        setRotation(2 * Math.PI * RANDOM.nextDouble());
        
        // Sets a random velocity for the debris which causes it to move.
        setVelocity(1, RANDOM.nextDouble() * 2 * Math.PI);
        
        // Expire debris after a random time within a set range
        new ParticipantCountdownTimer(this, "end", RANDOM.nextInt(3500 - 3000) + 750);
       
        // Draws the debris based on the type of object destroyed
        if (type.equals("Asteroid"))
        {
            //Draw asteroid debris
            Ellipse2D.Double asteroidDebris = new Ellipse2D.Double(0, 0, 1, 1);
            outline = asteroidDebris;
        }
        else if (type.equals("Ship"))
        {
            //Draw ship debris
            if (size == 0)
            {
                Path2D.Double shipDebris = new Path2D.Double();
                shipDebris.moveTo(0, 0);
                shipDebris.lineTo(0, 10);
                outline = shipDebris;
            }
            else if (size == 1)
            {
                Path2D.Double shipDebris = new Path2D.Double();
                shipDebris.moveTo(20, 0);
                shipDebris.lineTo(-10, 7);
                outline = shipDebris;
            }
        }
        else if (type.equals("AlienShip"))
        {
            //Draw alien ship debris
        }

    }


    @Override
    protected Shape getOutline ()
    {
        return outline;
    }

    @Override
    public void collidedWith (Participant p)
    {
        
    }
    
    @Override
    public void countdownComplete (Object payload)
    {
        if (payload.equals("end"))
        {
            expire(this);
        }
    }

}
