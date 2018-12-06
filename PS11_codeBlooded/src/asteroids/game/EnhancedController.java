package asteroids.game;

import static asteroids.game.Constants.*;
import sounds.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Scanner;
import javax.sound.sampled.Clip;
import javax.swing.*;
import asteroids.destroyers.Powerups;
import asteroids.participants.*;

public class EnhancedController extends Controller
{
    /** records if the teleport key is pressed. */
    private String initials;

    /** Tracks if its time to place a new ship */
    private boolean placeShip;

    /** The powerup creation timer */
    private Timer powerupTimer;
    
    private EnhancedShip eShip;

    /** On screen bullet limit */
    private int bulletLimit;

    public EnhancedController ()
    {
        super();

        // Set the leadBoard to an empty string.
        display.setleaderBoard("");

        // Initializes the placeShip instance variable.
        placeShip = false;

        // Creates a new powerup timer
        powerupTimer = new Timer(RANDOM.nextInt(5001) + 10000, this);
    }

    @Override
    protected void splashScreen ()
    {
        super.splashScreen();
        SoundClips test = new SoundClips();
        Clip splash = test.createClip("/sounds/smb_stage_clear.wav");
        if (splash != null)
        {
            if (splash.isRunning())
            {
                splash.stop();
            }
            splash.setFramePosition(0);
            splash.start();
        }
    }

    @Override
    protected void finalScreen ()
    {
        super.finalScreen();
        SoundClips test = new SoundClips();
        Clip finalScrn = test.createClip("/sounds/smb_gameover.wav");
        if (finalScrn != null)
        {
            if (finalScrn.isRunning())
            {
                finalScrn.stop();
            }
            finalScrn.setFramePosition(0);
            finalScrn.start();
        }
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
                // Stops the Timers.
                beatTimer.stop();

                alienTimer.stop();
                
                powerupTimer.stop();

                // Prompts the user to enter their initials.
                initials = JOptionPane.showInputDialog("Enter Initials:");

                try
                {
                    // Opens the leader board file.
                    File scores = new File(System.getProperty("user.dir") + "/Leaderboards.txt");
                    FileWriter leaderBoards = new FileWriter(scores, true);
                    PrintWriter printWriter = new PrintWriter(leaderBoards, true);

                    // Writes to the end of the file. Score then initials. Separated with a tab and a new line.
                    printWriter.append(score + "\t" + initials + "\n");

                    // Closes the print writer.
                    printWriter.close();

                    // Creates a new scanner that reads from the lead boards file.
                    Scanner read = new Scanner(scores);

                    // Holds the initials.
                    String initials;

                    // Holds the value of the gameScore
                    int gameScore;

                    // The total list of the top 5 scores.
                    String ldrs = "";

                    // Only used to creat a single line that contains the intials first then the score.
                    String total = "";

                    while (read.hasNext())
                    {
                        gameScore = read.nextInt();
                        initials = read.next();
                        total = initials + "   " + gameScore + "\n";

                        // Adds the score and the entire string containing score and initials to the treeMap.
                        leaders.put(gameScore, total);
                    }

                    // Used to pull only the top 5 scores.
                    int index = 0;

                    // Iterates over the descending values which will put the biggest score first.
                    for (Integer key : leaders.descendingKeySet())
                    {
                        index++;

                        // Adds the score and initials the total list.
                        ldrs = ldrs + leaders.get(key) + "\n";

                        // Stops the loop if the top 5 scores have been pulled.
                        if (index == 5)
                        {
                            break;
                        }
                    }
                    // tell the display to show the list of scores.
                    display.setleaderBoard(ldrs);

                    // Close the scanner.
                    read.close();
                }
                // Throw an exception if the file could not be opened.
                catch (IOException e)
                {
                    JOptionPane.showMessageDialog(display, "error opening file", "Error", 1);
                }
                // Display the game over screen and the leader boards.
                finalScreen();
            }

            // Places a new ship if the previous ship has been destroyed and there are still lives left.
            else if (lives > 0 && pstate.countAsteroids() != 0)
            {
                // Re-adds the key listener.
                display.addKeyListener(this);

                // Starts the beat timer once the delay is over.
                beatTimer.start();

                // It's time to try place a new ship.
                placeShip = true;

                if (level > 1)
                {
                    if (alienShip != null || smallAlienShip != null)
                    {
                        alienBulletTimer.start();
                    }
                }
                // Stop all active powerups
                Iterator<Participant> iter = this.getParticipants();
                while (iter.hasNext())
                {
                    Participant p = iter.next();
                    if (p instanceof Powerups)
                    {
                        ShipPowerups powUp = (ShipPowerups) p;
                        powUp.stopPowerup();
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

                // It's time to try place a new ship.
                placeShip = true;

                // Add another life for getting to the next level
                placeLives(1);

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
                
                // Disables and expires all active powerups
                // Stop all active powerups
                Iterator<Participant> iter = this.getParticipants();
                while (iter.hasNext())
                {
                    Participant p = iter.next();
                    if (p instanceof Powerups)
                    {
                        ShipPowerups powUp = (ShipPowerups) p;
                        powUp.stopPowerup();
                    }
                }
                
                // Rest the powerup timer
                powerupTimer.restart();
            }
        }
    }

    /**
     * Sets things up and begins a new game.
     */
    @Override
    protected void initialScreen ()
    {
        // Reset the level
        level = 1;
        nextLevelAstroids = 5;
        numCurrAstroids = 4;
        score = 0;
        gamesPlayed = 1;

        // Set up the beat timer.
        nextBeat = INITIAL_BEAT;
        beatTimer = new Timer(nextBeat, this);

        // Start the beat timer.
        beatTimer.stop();
        beatTimer.restart();

        // Clear the screen
        clear();

        // Place asteroids
        placeAsteroids();

        // It's time to try place a new ship.
        placeShip = true;

        // Reset the life placement horizontal offset
        livesHorizOffset = 0;

        // Reset the lives
        lives = 0;

        // Place the initial lives
        placeLives(3);

        // Set the bullet limit
        setBulletLimit(8);

        // Start listening to events (but don't listen twice)
        display.removeKeyListener(this);
        display.addKeyListener(this);

        // Give focus to the game screen
        display.setleaderBoard("");
        display.requestFocusInWindow();
        display.refresh();

        // Start the powerup timer
        powerupTimer.start();
    }

    public void setBulletLimit (int i)
    {
       bulletLimit = i;
    }

    /**
     * This method will be invoked because of button presses and timer events.
     */
    @Override
    public void actionPerformed (ActionEvent e)
    {
        // The start button has been pressed. Stop whatever we're doing
        // and bring up the initial screen
        if (e.getSource() instanceof JButton)
        {
            if (gamesPlayed == 0)
            {
                initialScreen();
            }
            else
            {
                // if there is an alien ship stop the clip
                if (level > 2 && smallAlienShip != null)
                {
                    smallAlienShip.playClip("smallStop");
                    Participant.expire(smallAlienShip);
                    initialScreen();
                }
                else if (level == 2 && alienShip != null)
                {
                    alienShip.playClip("bigStop");
                    Participant.expire(alienShip);
                    initialScreen();
                }
                else
                {
                    // start new game
                    initialScreen();
                }
            }
        }

        // Time to refresh the screen and deal with keyboard input
        else if (e.getSource() == refreshTimer)
        {
            if (rightKey && !ship.equals(null))
            {
                ship.turnRight();
            }
            if (leftKey && !ship.equals(null))
            {
                ship.turnLeft();
            }
            if (upKey && !ship.equals(null))
            {
                ship.accelerate();
            }
            if (downKey && pstate.countBullets() < bulletLimit && !ship.equals(null))
            {
                placeBullet();
            }

            // It may be time to make a game transition
            performTransition();

            // Move the participants to their new locations
            pstate.moveParticipants();

            // Refresh screen
            display.refresh();

            // If it's time to place a ship try and add one
            if (placeShip)
            {
                placeShip();
            }

        }
        // Plays the beat sound clip after the timer has gone off.
        else if (e.getSource() == beatTimer)
        {
            // Used to create sound clips.
            SoundClips test = new SoundClips();

            // If the interval is greater than 300, it decreased by the beat delta.
            if (nextBeat > 300)
            {
                nextBeat = nextBeat - BEAT_DELTA;
            }
            // If the interval reaches 300 then the beat will remain at its current pace.
            else
            {
                nextBeat = 300;
            }

            // Sets the new delay
            beatTimer.setDelay(nextBeat);

            // Plays the first beat clip if the other has already been played.
            // Beats should alternate.
            if (beat)
            {
                // Sets the beat to false so that the SECOND beat will be played next.
                beat = false;

                Clip beat1 = test.createClip("/sounds/beat1.wav");
                if (beat1 != null)
                {
                    if (beat1.isRunning())
                    {
                        beat1.stop();
                    }
                    beat1.setFramePosition(0);
                    beat1.start();
                }
            }
            else if (!beat)
            {
                // Sets the beat to true so that the FIRST beat will be played
                beat = true;

                Clip beat2 = test.createClip("/sounds/beat2.wav");
                if (beat2 != null)
                {
                    if (beat2.isRunning())
                    {
                        beat2.stop();
                    }
                    beat2.setFramePosition(0);
                    beat2.start();
                }
            }
        }
        else if (e.getSource().equals(alienTimer))
        {
            placeAlienShip();
            alienTimer.stop();
        }
        else if (e.getSource().equals(alienBulletTimer))
        {
            placeAlienBullet();
        }
        else if (e.getSource() == powerupTimer)
        {
            // If the powerup timer is running, Stop the timer
            if (powerupTimer.isRunning())
            {
                powerupTimer.stop();
            }

            // Count the number of active powerup participants
            int numPowerUps = 0;
            Iterator<Participant> iter = this.getParticipants();
            while (iter.hasNext())
            {
                Participant p = iter.next();
                if (p instanceof ShipPowerups)
                {
                    numPowerUps++;
                }
            }

            // Don't create a power up if there are already 3 on screen
            if (numPowerUps < 3)
            {
                // create and add a powerup participant
                addParticipant(new ShipPowerups(this));
            }

            // create and start a new powerup timer
            powerupTimer = new Timer(RANDOM.nextInt(5001) + 10000, this);
            powerupTimer.start();
        }

    }

    /**
     * Teleports the ship to a random location on screen.
     */
    @Override
    public void keyPressed (KeyEvent k)
    {
        super.keyPressed(k);

        if (k.getKeyCode() == KeyEvent.VK_T && ship != null)
        {
            ship.setPosition(RANDOM.nextInt(SIZE), RANDOM.nextInt(SIZE));
        }
    }

    /**
     * Place a new ship in the center of the screen. Remove any existing ship first.
     */
    protected boolean spawnZoneSafe ()
    {
        // assume the area is safe initially
        boolean areaSafe = true;
        Iterator<Participant> iter = this.getParticipants();
        while (iter.hasNext())
        {
            Participant p = iter.next();
            // if a participant other than a power up is in the ship spawn zone deem the area unsafe
            if ( !(p instanceof ShipPowerups) && (p.getX() > spawnLeftBound && p.getX() < spawnRightBound)
                    && (p.getY() > spawnLowerBound && p.getY() < spawnUpperBound))
            {
                areaSafe = false;
            }
        }
        return areaSafe;

    }

    /**
     * Place a new ship in the center of the screen. Remove any existing ship first.
     */
    @Override
    protected void placeShip ()
    {
        if (spawnZoneSafe())
        {
            // Place a new ship
            Participant.expire(ship);
            eShip = new EnhancedShip(SIZE / 2, SIZE / 2, -Math.PI / 2, this);
            ship = eShip;
            addParticipant(ship);
            display.setLegend("");

            // Display the level and score when the ship is placed.
            display.setLevel(level + "");
            display.setScore(score + "");
            placeShip = false;
        }
    }
    
    @Override
    protected void placeBullet()
    {
        // Place a new bullet
        addParticipant(new EnhancedBullet((ship.getXNose()), ship.getYNose(), ship.getRotation(), this));
    }
}
