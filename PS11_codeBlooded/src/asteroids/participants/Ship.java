package asteroids.participants;

import static asteroids.game.Constants.*;
import java.awt.Shape;
import java.awt.geom.*;
import javax.sound.sampled.Clip;
import asteroids.destroyers.*;
import asteroids.game.Controller;
import asteroids.game.Participant;
import asteroids.game.ParticipantCountdownTimer;
import sounds.SoundClips;

/**
 * Represents ships
 */
public class Ship extends Participant implements AsteroidDestroyer
{
    /** The outline of the ship */
    private Shape outline;
    
    /** The outline of the ship without the thruster */
    private Shape shipNoThruster;
    
    /** The outline of the ship with the thruster */
    private Shape shipAndThruster;

    /** Game controller */
    private Controller controller;
    
    /** Thruster indicator */
    private boolean thruster;

    /**
     * Constructs a ship at the specified coordinates that is pointed in the given direction.
     */
    public Ship (int x, int y, double direction, Controller controller)
    {
        this.controller = controller;
        setPosition(x, y);
        setRotation(direction);
        setDirection(direction);
        thruster = false;
        
        Path2D.Double shipPoly = new Path2D.Double();
        shipPoly.moveTo(21, 0);
        shipPoly.lineTo(-21, 12);
        shipPoly.lineTo(-14, 10);
        shipPoly.lineTo(-14, -10);
        shipPoly.lineTo(-21, -12);
        shipPoly.closePath();
        shipNoThruster = shipPoly;
        
        // Draw the thruster
        Path2D.Double thrusterPoly = new Path2D.Double();
        thrusterPoly.moveTo(-14, -7);
        thrusterPoly.lineTo(-25, 0);
        thrusterPoly.lineTo(-14, 7);
        // Append the ship to the thruster
        thrusterPoly.append(shipPoly, false);
        shipAndThruster = thrusterPoly;
        
        // Initialize the ship without the thruster
        outline = shipNoThruster;

    }

    /**
     * Returns the x-coordinate of the point on the screen where the ship's nose is located.
     */
    public double getXNose ()
    {
        Point2D.Double point = new Point2D.Double(20, 0);
        transformPoint(point);
        return point.getX();
    }

    /**
     * Returns the x-coordinate of the point on the screen where the ship's nose is located.
     */
    public double getYNose ()
    {
        Point2D.Double point = new Point2D.Double(20, 0);
        transformPoint(point);
        return point.getY();
    }

    @Override
    protected Shape getOutline ()
    {
        if (thruster)
        {
            outline = shipAndThruster;
        }
        else
        {
            outline = shipNoThruster;
        }
        return outline;
    }

    /**
     * Customizes the base move method by imposing friction
     */
    @Override
    public void move ()
    {
        applyFriction(SHIP_FRICTION);
        super.move();
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

    /**
     * Accelerates by SHIP_ACCELERATION
     */
    public void accelerate ()
    {
        thruster = true;
        new ParticipantCountdownTimer(this, "end", THRUSTER_DURATION);
        accelerate(SHIP_ACCELERATION);
        SoundClips test = new SoundClips();
        Clip accelerate = test.createClip("/sounds/thrust.wav");
        if ( accelerate != null)
        {
            if (accelerate.isRunning())
            {
                accelerate.stop();
            }
            accelerate.setFramePosition(0);
            accelerate.start();
        }
    }

    /**
     * When a Ship collides with a ShipDestroyer, it expires
     */
    @Override
    public void collidedWith (Participant p)
    {
        if (p instanceof ShipDestroyer)
        {
            // When the ship is destroyed, an explosion is played. 
            SoundClips test = new SoundClips();
            Clip shipBoom = test.createClip("/sounds/bangShip.wav");
            if ( shipBoom != null)
            {
                if (shipBoom.isRunning())
                {
                    shipBoom.stop();
                }
                shipBoom.setFramePosition(0);
                shipBoom.start();
            }
            // Expire the ship from the game
            Participant.expire(this);

            // Tell the controller the ship was destroyed
            controller.shipDestroyed();
        }
    }

    /**
     * This method is invoked when a ParticipantCountdownTimer completes its countdown.
     */
    @Override
    public void countdownComplete (Object payload)
    {
        // Give a burst of acceleration, then schedule another
        // burst for 200 msecs from now.
        if (payload.equals("move"))
        {
            accelerate();
            new ParticipantCountdownTimer(this, "move", 200);
        }
        // Turn the thruster off once the thruster duration has passed
        if (payload.equals("end"))
        {
            thruster = false;
        }
    }
}
