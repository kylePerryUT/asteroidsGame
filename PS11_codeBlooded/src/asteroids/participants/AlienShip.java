package asteroids.participants;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import javax.sound.sampled.Clip;
import asteroids.destroyers.AlienDestroyer;
import asteroids.destroyers.AsteroidDestroyer;
import asteroids.game.*;
import sounds.*;

public class AlienShip extends Participant implements AsteroidDestroyer
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
    
    public AlienShip (int x, int y, double direction, int size)
    {
        // Set the initial position of the alien ship.
        setPosition(x, y);
        
        // Sets the size
        this.size = size;
       
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
    protected Shape getOutline ()
    {
        return outline;
    }
    
    @Override
    public void collidedWith (Participant p)
    {
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
        if (size == 1)
        {
            playClip("bigStop");
        }
        else
        {
            playClip("smallStop");
        }
        
        expire(this);
    }

}