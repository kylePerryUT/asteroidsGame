package asteroids.participants;

import javax.sound.sampled.Clip;
import asteroids.game.Controller;
import sounds.SoundClips;

public class EnhancedBullet extends Bullets
{

    public EnhancedBullet (double x, double y, double direction, Controller controller)
    {
        super(x, y, direction, controller);
        // TODO Auto-generated constructor stub
    }
    
    @Override
    protected void playClip()
    {
     // Creates and plays the bullet sound clip.
        SoundClips test = new SoundClips();
        Clip peew = test.createClip("/sounds/smb_fireball.wav");
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
    }

}
