package asteroids.game;

import static asteroids.game.Constants.*;
import sounds.*;
import java.awt.Dialog;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeMap;
import javax.sound.sampled.Clip;
import javax.swing.*;
import asteroids.participants.*;

public class EnhancedController extends Controller
{
    /** records if the teleport key is pressed. */
    private String initials;
    
    
    
    
   public EnhancedController()
   {
       super();
       display.setleaderBoard("");
       
   }
   
   /**
    * If the transition time has been reached, transition to a new state
    */
   @Override
   protected void performTransition ()
   {

       // Do something only if the time has been reached
       if (transitionTime <= System.currentTimeMillis())
       {
           // Clear the transition time
           transitionTime = Long.MAX_VALUE;

           // If there are no lives left, the game is over. Show the final
           // screen.
           if (lives <= 0)
           {
               if (alienShip != null)
               {
                   Participant.expire(alienShip);
                   alienBulletTimer.stop();
                   alienShip.playClip("bigStop");
               }
               else if (smallAlienShip != null)
               {
                   Participant.expire(smallAlienShip);
                   alienBulletTimer.stop();
                   smallAlienShip.playClip("smallStop");
               }
               // Stops the beat Timer.
               beatTimer.stop();
               
               alienTimer.stop();
               initials = JOptionPane.showInputDialog("Enter anitials:");
               
            try
            {
                File demo = new File(System.getProperty("user.dir") + "/Leaderboards.txt");
                FileWriter leaderBoards = new FileWriter(demo, true);
                PrintWriter printWriter = new PrintWriter(leaderBoards, true);
                
                printWriter.append(score + "\t" + initials + "\n");
                //printWriter.println(initials + " " + score);
                printWriter.close();
                
                Scanner read = new Scanner(demo);
                
                String initials;
                int gameScore;
                String ldrs = "";
                String total = "";
                while (read.hasNext())
                {
                    gameScore = read.nextInt();
                    initials = read.next();
                    total = initials + "   " + gameScore + "\n";
                    
                    leaders.put(gameScore, total);  
                }
                
                
                int index = 0;
                 for(Integer key: leaders.descendingKeySet())
                 {
                    index ++;
                    ldrs = ldrs + leaders.get(key) + "\n";
                    if (index == 5)
                    {
                        break;
                    }
                 }
                 display.setleaderBoard(ldrs);
                 read.close();
            }
            catch (IOException e)
            {
                JOptionPane.showMessageDialog(display, "error opening file", "Error", 1);
                e.printStackTrace();
            }
               
               
               finalScreen();
           }

           // Places a new ship if the previous ship has been destroyed and there are still lives left.
           else if (lives > 0 && pstate.countAsteroids() != 0)
           {
               // Re-adds the key listener.
               display.addKeyListener(this);

               // Starts the beat timer once the delay is over.
               beatTimer.start();

               // Re-places the ship.
               placeShip();

               if (level > 1)
               {
                   if (alienShip != null || smallAlienShip != null)
                   {
                       alienBulletTimer.start();
                   }
               }
           }
           // Proceeds to the next level if all asteroids have been destroyed.
           else if (pstate.countAsteroids() == 0)
           {
               // Expires the last alien ship and stops the sound clip.
               if (level == 2 && alienShip != null)
               {
                   alienShip.playClip("bigStop");
                   Participant.expire(alienShip);
                   alienBulletTimer.stop();
               }
               else if (level > 2 && smallAlienShip != null)
               {
                   smallAlienShip.playClip("smallStop");
                   Participant.expire(smallAlienShip);
                   alienBulletTimer.stop();
               }

               // Starts the timer for an alien ship to appear
               alienTimer.start();

               // Re-adds the key listener.
               display.addKeyListener(this);

               // The beat timer and interval is reset and restarted.
               nextBeat = INITIAL_BEAT;
               beatTimer.restart();

               // Updates the level
               level++;
               display.setLevel(level + "");

               // Places a new ship
               placeShip();

               // Places 4 starting asteroids
               placeAsteroids();

               // Places one more asteroid onto the board per each level passed.
               // lvl 1 will have 4, lvl 2 will have 5, lvl 3 will have 6 and so on.
               while (numCurrAstroids < nextLevelAstroids)
               {
                   // Adds a large asteroid at a random location along the edge of the board.
                   addParticipant(new Asteroid(RANDOM.nextInt(4), 2, RANDOM.nextInt(151 + 1 + 151) - 151,
                           RANDOM.nextInt(151 + 1 + 151) - 151, 3, this));

                   // Sets the current asteroids equal to the next level asteroids in order to exit the loop.
                   numCurrAstroids++;
               }

               // Sets the next amount of Asteroids to 1 more than the previous.
               nextLevelAstroids++;

               // Refreshes the display to show the changes.
               display.refresh();

           }

       }
   }

    
    
    /**
     * Teleports the ship to a random location on screen. 
     */
    @Override
    public void keyPressed(KeyEvent k)
    {
        super.keyPressed(k);
        
        if (k.getKeyCode() == KeyEvent.VK_T && ship != null)
        {
            ship.setPosition(RANDOM.nextInt(SIZE), RANDOM.nextInt(SIZE));
        }
    }
    
    
}
