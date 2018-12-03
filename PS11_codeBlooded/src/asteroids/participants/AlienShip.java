package asteroids.participants;

import static asteroids.game.Constants.BULLET_DURATION;
import static asteroids.game.Constants.RANDOM;
import static asteroids.game.Constants.SIZE;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.Random;
import javax.sound.sampled.Clip;
import asteroids.destroyers.AlienDestroyer;
import asteroids.destroyers.AsteroidDestroyer;
import asteroids.destroyers.BulletDestroyer;
import asteroids.game.*;
import sounds.*;
import asteroids.game.Constants.*;

public class AlienShip extends Participant implements AsteroidDestroyer, BulletDestroyer
{
    /** The outline of the alien ship */
    private Shape outline;
    
    /** The Sound Clip object used to make sound clips*/
    private SoundClips alienSounds;
    
    /** The small alien sound clip */
    private Clip smallAlien;
    
    /** The large alien sound clip */
    private Clip bigAlien;
    
    /** The size of the alien ship. Either 0 for small or 1 for big */
    private int size;
    
    /** The game controller */
    private Controller controller;
    
    /** Holds the original direction */
    private double originalDirection;
    
    public AlienShip (int x, int y, double direction, int speed, int size, Controller controller)
    {
        this.controller = controller;
        
        // Set the initial position of the alien ship.
        setPosition(x, y);
        setVelocity(speed, direction);
        setDirection(direction);
        setSpeed(speed);
        originalDirection = direction;
    
        
        
        // Sets the size
        this.size = size;
       
        // Draw the top part of the alien ship.
        Path2D.Double alienShip = new Path2D.Double();
        alienShip.moveTo(-14, -8);
        alienShip.lineTo(-9, -20);
        alienShip.lineTo(9, -20);
        alienShip.lineTo(14, -8);
        alienShip.closePath();
        // Draw the middle part of the alien ship.
        alienShip.moveTo(-30, 5);
        alienShip.lineTo(-15, -8);
        alienShip.lineTo(15, -8);
        alienShip.lineTo(30, 5);
        alienShip.closePath();
        // Draw the bottom part of the alien ship.
        alienShip.moveTo(-30, 5);
        alienShip.lineTo(-14, 20);
        alienShip.lineTo(14, 20);
        alienShip.lineTo(30, 5);
        alienShip.closePath();
        
        // Alien ship is too big, needs to be scaled down.
        AffineTransform test1 = new AffineTransform();
        test1.scale(.8, .8);
        alienShip.transform(test1);
        
        
        // Creates the alienSounds object that will be used to create the 
        // sounds for both alien ships.
        alienSounds = new SoundClips();
           
        // Draws the small alien ship if the size is 0.
        if (size == 0)
        {
            // Creates a new Path2D object out of the big alien ship.
            Path2D.Double smallAlienShip = new Path2D.Double();
            smallAlienShip = alienShip;
            
            // Creates a transformation that will be used to scale the big ship down by .5
            AffineTransform test = new AffineTransform();
            test.scale(.5, .5);
            
            // Applies the scale transform to the smallAlienShip, resulting in a 
            // ship that is scaled by .5 from the big alien ship.
            smallAlienShip.transform(test);
            
            // Assigns the small alien ship to the outline that will be drawn. 
            outline = smallAlienShip; 
            
            // Starts the small alien ship sound clip. 
            playClip("Small");
        }
        
        // Draws the big alien ship if the size is 1.
        else if(size == 1)
        {
            outline = alienShip;
            playClip("Big");
        }
        
        // Starts timer to change direction.
        new ParticipantCountdownTimer(this, "end", RANDOM.nextInt(200) + 400);
    }
    
    /**
     * Creates and plays the provided sound Clip.
     */
    public void playClip(String ship)
    {
        if (ship.equals("Small"))
        {
            // Creates the small alien ship sound clip as long as it is not empty. 
            smallAlien = alienSounds.createClip("/sounds/saucerSmall.wav");
            if ( smallAlien != null)
            {
                // If the clip is already running, stop it. 
                if (smallAlien.isRunning())
                {
                    smallAlien.stop();
                }
                // 
                smallAlien.setFramePosition(0);
                smallAlien.loop(smallAlien.LOOP_CONTINUOUSLY);
            }
        }
        else if (ship.equals("Big"))
        {
            // Creates the big alien ship sound clip as long as it is not empty.
            bigAlien = alienSounds.createClip("/sounds/saucerBig.wav");
            if ( bigAlien != null)
            {
                // If the clip is already running, stop it. 
                if (bigAlien.isRunning())
                {
                    bigAlien.stop();
                }
                // 
                bigAlien.setFramePosition(0);
                bigAlien.loop(bigAlien.LOOP_CONTINUOUSLY);
            }
        }
        
        // Stops the specified sound clip
        else if (ship.equals("smallStop"))
        {
            smallAlien.stop();
        }
        else if (ship.equals("bigStop"))
        {
            bigAlien.stop();
        }
    }
    
    @Override
    public void countdownComplete (Object payload)
    {
        double[] rand;
        if(originalDirection == Math.PI)
        {
            rand = new double[3];
            rand[0] = Math.PI -1;
            rand[1] = Math.PI + 1;
            rand[2] = Math.PI;
        }
        else
        {
            rand = new double[3];
            rand[0] = 0;
            rand[1] = 1;
            rand[2] = -1;
            
        }
        if (payload.equals("end"))
        {
            setDirection(rand[RANDOM.nextInt(3)]);
        }
        
        new ParticipantCountdownTimer(this, "end", RANDOM.nextInt(200) + 500);
    }

    @Override
    protected Shape getOutline ()
    {
        return outline;
    }
    
    /**
     * Returns the size of the asteroid
     */
    public int getSize ()
    {
        return size;
    }
    
    @Override
    public void collidedWith (Participant p)
    {
        if (p instanceof AlienDestroyer)
        {
            Participant.expire(this);
            controller.alienDestroyed(this);
            
            // Plays the alienShip explosion clip when an alien ship is destroyed. 
            Clip alienBoom = alienSounds.createClip("/sounds/bangAlienShip.wav");
            if ( alienBoom != null)
            {
                if (alienBoom.isRunning())
                {
                    alienBoom.stop();
                }
                alienBoom.setFramePosition(0);
                alienBoom.start();
            }   
            
            // If an alien ship has been destroyed, stop the sound clip. 
            if (size == 1 && this.isExpired())
            {
                playClip("bigStop");
            }
            else if (size == 0 && this.isExpired())
            {
                playClip("smallStop");
            }
        }
        
    }

}