package asteroids.participants;

import static asteroids.game.Constants.*;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import javax.sound.sampled.Clip;
import asteroids.destroyers.PowerupsDestroyer;
import asteroids.game.Controller;
import asteroids.game.Participant;
import asteroids.game.ParticipantCountdownTimer;
import sounds.SoundClips;

public class ShipPowerups extends Participant
{
    /** The outline of the alien ship */
    private Shape outline;
    
    /** The Sound Clip object used to make sound clips*/
    private SoundClips powerupSound;
    
    /** The type of powerup */
    private String powerupType;
    
    /** The game controller */
    private Controller controller;
    
    public ShipPowerups (Controller controller)
    {
        this.controller = controller;
        
        // Set a random position of the powerup.
        int x = RANDOM.nextInt(651) + 50;
        int y = RANDOM.nextInt(651) + 50;
        setPosition(x, y);
        setVelocity(0, 0);
        
        // Choose one of the powerups at random
        choosePowerup();
        
        // Set the participant outline
        powerupOutline(powerupType);
        
        // Start a new powerup timer
        new ParticipantCountdownTimer(this, "end", RANDOM.nextInt(2001) + 3000);
        
        // rotate the powerup
        setRotation(Math.PI);
    }

    @Override
    protected Shape getOutline ()
    {
        return outline;
    }
    
    /** 
     * Choose one of the powerups at random and set it's type
     */
    private void choosePowerup()
    {
        // Add more powerups in the future
        int rand = RANDOM.nextInt(2);
        if (rand == 0)
        {
            powerupType = "ExtraLife";
        }
        else if (rand == 1)
        {
            powerupType = "UnlimitedBullets";
        }
        else if (rand == 2)
        {
            powerupType = "Indestructible";
        }
    }
    
    /**
     * Draw the powerup shape 
     */
    private void powerupOutline(String powerupType)
    {
       
        Path2D.Double powerupShape = new Path2D.Double();
        if (powerupType == "ExtraLife")
        {
            // draw extra life shape
            powerupShape.moveTo(3, 9);
            powerupShape.lineTo(-3, 9);
            powerupShape.lineTo(-3, 3);
            powerupShape.lineTo(-9, 3);
            powerupShape.lineTo(-9, -3);
            powerupShape.lineTo(-9, -3);
            powerupShape.lineTo(-3, -3);
            powerupShape.lineTo(-3, -9);
            powerupShape.lineTo(3, -9);
            powerupShape.lineTo(3, -3);
            powerupShape.lineTo(9, -3);
            powerupShape.lineTo(9, 3);
            powerupShape.lineTo(3, 3);
            powerupShape.closePath();
            outline = powerupShape;
        }
        else if (powerupType == "UnlimitedBullets")
        {
            // draw unlimited bullet shape
            powerupShape.moveTo(-5, 0);
            powerupShape.lineTo(5, 0);
            powerupShape.lineTo(5, 15);
            powerupShape.curveTo(5,15,0,25,-5,15);
            powerupShape.closePath();
            outline = powerupShape;
        }
        else if (powerupType == "Indestructible")
        {
            // draw shape
            outline = powerupShape;
        }
    }
    
    private void powerupEffect ()
    {
        // Depending on the powerup type take appropriate action.
        if (powerupType == "ExtraLife")
        {
            // Whatever the powerup does
            controller.addLife();
        }
        else if (powerupType == "UnlimitedBullets")
        {
            // Whatever the powerup does
        }
        else if (powerupType == "Indestructible")
        {
            // Whatever the powerup does
        }
    }

    /**
     * When a Powerup collides with a PowerupDestroyer, it expires
     */
    @Override
    public void collidedWith (Participant p)
    {
        if (p instanceof PowerupsDestroyer)
        {
            // When the powerup is collected, a sound is played. 
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
            
            // Do whatever the powerup does
            powerupEffect();
            
            // Expire the Powerup from the game
            Participant.expire(this);
        }
    }
    
    @Override
    public void countdownComplete (Object payload)
    {
        // If the powerup is not expired, expire it
        if (payload.equals("end") && !this.isExpired())
        {
            Participant.expire(this);
        }
    }

}
