package asteroids.participants;

import javax.sound.sampled.Clip;
import asteroids.destroyers.ShipDestroyer;
import asteroids.game.Controller;
import asteroids.game.Participant;
import sounds.SoundClips;

public class EnhancedShip extends Ship
{

    public EnhancedShip (int x, int y, double direction, Controller controller)
    {
        super(x, y, direction, controller);
        // TODO Auto-generated constructor stub
    }
    
    @Override
    public void collidedWith (Participant p)
    {
        super.collidedWith(p);
        
        if ( p instanceof ShipDestroyer)
        {
            SoundClips test = new SoundClips();
            Clip shipBoom = test.createClip("/sounds/smb_mariodie.wav");
            if ( shipBoom != null)
            {
                if (shipBoom.isRunning())
                {
                    shipBoom.stop();
                }
                shipBoom.setFramePosition(0);
                shipBoom.start();
            }
        }
    }
    
}
