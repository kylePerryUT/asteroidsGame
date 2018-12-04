package asteroids.participants;

import static asteroids.game.Constants.*;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import javax.sound.sampled.Clip;
import asteroids.destroyers.AlienBulletDestroyer;
import asteroids.destroyers.AlienDestroyer;
import asteroids.destroyers.AsteroidDestroyer;
import asteroids.destroyers.BulletDestroyer;
import asteroids.destroyers.ShipDestroyer;
import asteroids.game.Controller;
import asteroids.game.Participant;
import asteroids.game.ParticipantCountdownTimer;
import sounds.*;
 

public class AlienBullets extends Participant implements AsteroidDestroyer, ShipDestroyer
{
    /** The outline of the bullet */
    private Shape outline;

    /** Game controller */
    private Controller controller;

    /**
     * Constructs a bullet at the specified coordinates that is pointed in the given direction.
     */
    public AlienBullets (double x, double y, double direction, Controller controller)
    {
        this.controller = controller;
       
        // Draws the bullet
        Ellipse2D.Double bullet = new Ellipse2D.Double(x, y, 1, 1);
        outline = (Shape) bullet;
        
        // Sets the velocity of the bullet which causes it to move.
        setVelocity(BULLET_SPEED, direction);
        
        // Creates and plays the bullet sound clip.
        SoundClips test = new SoundClips();
        Clip peew = test.createClip("/sounds/fire.wav");
        if ( peew != null)
        {
            if (peew.isRunning())
            {
                peew.stop();
            }
            peew.setFramePosition(0);
            peew.setLoopPoints(0, 1);
            peew.loop(1);
        }
        
        new ParticipantCountdownTimer(this, "end", BULLET_DURATION);
    }


    @Override
    protected Shape getOutline ()
    {
        return outline;
    }

    @Override
    public void collidedWith (Participant p)
    {
        if (p instanceof AlienBulletDestroyer)
        {
            // Expire the bullet from the game
            Participant.expire(this);
        }
        
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
