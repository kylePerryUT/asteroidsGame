package asteroids.game;

import static asteroids.game.Constants.*;
import sounds.*;
import java.awt.event.*;
import java.util.Iterator;
import javax.sound.sampled.Clip;
import javax.swing.*;
import asteroids.participants.*;

/**
 * Controls a game of Asteroids.
 */
public class Controller implements KeyListener, ActionListener
{
    /** The state of all the Participants */
    protected ParticipantState pstate;

    /** The ship (if one is active) or null (otherwise) */
    protected Ship ship;

    /** The alien ship (if one is active) or null otherwise */
    protected AlienShip alienShip;
    protected AlienShip smallAlienShip;

    /** When this timer goes off, it is time to refresh the animation */
    protected Timer refreshTimer;

    /** When this timer goes off, it is time to play a beat */
    protected Timer beatTimer;
    protected int nextBeat;
    protected boolean beat;

    /** When these timers go off, it's time to place an alien ship and shoot a bullet. */
    protected Timer alienTimer;
    protected Timer alienBulletTimer;

    /**
     * The time at which a transition to a new stage of the game should be made. A transition is scheduled a few seconds
     * in the future to give the user time to see what has happened before doing something like going to a new level or
     * resetting the current level.
     */
    protected long transitionTime;

    /** Number of lives left */
    protected int lives;

    /** The game display */
    protected Display display;

    /** Records the left key */
    protected boolean leftKey;

    /** Records the left key */
    protected boolean rightKey;

    /** Records the left key */
    protected boolean upKey;

    /** Records the left key */
    protected boolean downKey;

    /** Number of asteroids in the next level */
    protected int nextLevelAstroids = 5;

    /** Number of asteroids in the current level */
    protected int numCurrAstroids = 4;

    /** Number of asteroids in the current level */
    protected int level;

    /** Records the score */
    protected int score;

    /** Records the games played */
    protected int gamesPlayed;

    /** Stores the distance from the lives and the side of the screen */
    protected int livesHorizOffset;

    /**
     * Constructs a controller to coordinate the game and screen
     */
    public Controller ()
    {
        // Initialize the ParticipantState
        pstate = new ParticipantState();

        // Set up the refresh timer.
        refreshTimer = new Timer(FRAME_INTERVAL, this);

        // Clear the transitionTime
        transitionTime = Long.MAX_VALUE;

        // Record the display object
        display = new Display(this);

        // Set the level to 1.
        level = 1;

        // Set the score to 0.
        score = 0;

        // Sets up the beat timer.
        nextBeat = INITIAL_BEAT;
        beat = true;

        // Sets up the alien timers
        alienTimer = new Timer(5000, this);
        alienBulletTimer = new Timer(2500, this);

        // Bring up the splash screen and start the refresh timer
        splashScreen();
        display.setVisible(true);
        refreshTimer.start();

        // Initializes the games played
        gamesPlayed = 0;

        // Initialize the lives counter
        lives = 0;

        // Initialize the live offset
        livesHorizOffset = 0;

    }

    /**
     * Returns the ship, or null if there isn't one
     */
    public Ship getShip ()
    {
        return ship;
    }

    /**
     * Configures the game screen to display the splash screen
     */
    private void splashScreen ()
    {
        // Clear the screen, reset the level, and display the legend
        clear();
        display.setLegend("Asteroids");

        // Place four asteroids near the corners of the screen.
        placeAsteroids();
    }

    /**
     * The game is over. Displays a message to that effect.
     */
    protected void finalScreen ()
    {
        display.setLegend(GAME_OVER);
        display.removeKeyListener(this);
    }

    /**
     * Place a new ship in the center of the screen. Remove any existing ship first.
     */
    protected void placeShip ()
    {
        // Expire the last ship.
        Participant.expire(ship);

        // Place a new ship in the middle of the screen.
        ship = new Ship(SIZE / 2, SIZE / 2, -Math.PI / 2, this);
        addParticipant(ship);
        display.setLegend("");

        // Display the level and score when the ship is placed.
        display.setLevel(level + "");
        display.setScore(score + "");
    }

    /**
     * Place an alien ship on the screen.
     */
    protected void placeAlienShip ()
    {
        if (level == 2)
        {
            // places a large alien ship on level two.
            alienShip = new AlienShip(-32, RANDOM.nextInt(SIZE), (RANDOM.nextInt(2) + 1) * Math.PI, 5, 1, this);

            // Adds the participant.
            addParticipant(alienShip);

            // Stops the timer since there is already an alien ship on the board.
            alienTimer.stop();

            // Starts the bullet timer since there is now an alien that needs to shoot stuff.
            alienBulletTimer.start();
        }
        else if (level > 2)
        {
            // Expires the previous alien ship and places a new ship.
            AlienShip.expire(alienShip);

            // Place a smaller alien ship that moves much faster is far more accurate.
            smallAlienShip = new AlienShip(-32, RANDOM.nextInt(SIZE), (RANDOM.nextInt(2) + 1) * Math.PI, 10, 0, this);

            // Adds the participant.
            addParticipant(smallAlienShip);

            // Stops the timer since there is already an alien ship on the board.
            alienTimer.stop();

            // Time to shoot stuff!
            alienBulletTimer.start();
        }
    }

    /**
     * Place the alien ship bullets
     */
    protected void placeAlienBullet ()
    {
        // places a bullet with a random direction as long as there is an alien ship and regular ship.
        if (level == 2 && alienShip != null && ship != null)
        {
            addParticipant(
                    new AlienBullets(alienShip.getX(), alienShip.getY(), RANDOM.nextDouble() * 2 * Math.PI, this));
            
            // Starts the timer again so that the ship continues to shoot while active.
            alienBulletTimer.start();
        }
        // On all levels above 2, the small alien ship shoots in the direction of the ship.
        else if (level > 2 && smallAlienShip != null && ship != null)
        {
            addParticipant(new AlienBullets(smallAlienShip.getX(), smallAlienShip.getY(),
                    Math.atan2(ship.getY() - smallAlienShip.getY(), ship.getX() - smallAlienShip.getX()), this));
            
            // Starts the timer so that the ship continues to shoot while its active.
            alienBulletTimer.start();
        }
    }

    /**
     * Place the lives underneath the score and set the number of lives.
     */
    protected void placeLives (int numLives)
    {
        // Place the lives
        int numLife = 1;
        while ((numLife <= numLives) && (lives <= 5))
        {
            lives++;
            ShipLives life = new ShipLives(LABEL_HORIZONTAL_OFFSET + livesHorizOffset, LABEL_VERTICAL_OFFSET + 60,
                    -Math.PI / 2, lives, this);
            addParticipant(life);
            livesHorizOffset += 30;
            numLife++;
        }
    }

    protected void removeLife ()
    {
        if (lives > 0)
        {
            // Find find the "last", or most far right ship life on screen and remove it
            Participant nextShipLife = null;
            Iterator<Participant> iter = this.getParticipants();
            while (iter.hasNext())
            {
                Participant p = iter.next();
                if (p instanceof ShipLives)
                {
                    p = (ShipLives) p;
                    if (((ShipLives) p).getLifeNum() == lives)
                    {
                        nextShipLife = p;
                    }
                }
            }
            Participant.expire(nextShipLife);
            // Decrement the lives
            lives--;
            // Move the offset back
            livesHorizOffset -= 30;
        }
    }

    /**
     * Place a new bullet at the nose of the ship..
     */
    protected void placeBullet ()
    {
        // Place a new bullet
        addParticipant(new Bullets((ship.getXNose()), ship.getYNose(), ship.getRotation(), this));
    }

    /**
     * Places an asteroid near one corner of the screen. Gives it a random velocity and rotation.
     */
    protected void placeAsteroids ()
    {
        addParticipant(new Asteroid(RANDOM.nextInt(4), 2, EDGE_OFFSET, EDGE_OFFSET, 3, this));
        addParticipant(new Asteroid(RANDOM.nextInt(4), 2, -EDGE_OFFSET, EDGE_OFFSET, 3, this));
        addParticipant(new Asteroid(RANDOM.nextInt(4), 2, EDGE_OFFSET, -EDGE_OFFSET, 3, this));
        addParticipant(new Asteroid(RANDOM.nextInt(4), 2, -EDGE_OFFSET, -EDGE_OFFSET, 3, this));
    }

    /**
     * Clears the screen so that nothing is displayed
     */
    protected void clear ()
    {
        pstate.clear();
        display.setLegend("");
        ship = null;
    }

    /**
     * Sets things up and begins a new game.
     */
    protected void initialScreen ()
    {
        // Reset the level
        level = 1;
        
        // Sets the number asteriods that will be displayed on the next level.
        nextLevelAstroids = 5;
        
        // Sets the current number of asteroids. 
        numCurrAstroids = 4;
        
        // Reset the score to 0.
        score = 0;
        
        // Reset the games played.
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

        // Place the ship
        placeShip();

        // Reset the life placement horizontal offset
        livesHorizOffset = 0;

        // Reset the lives
        lives = 0;

        // Place the initial lives
        placeLives(3);

        // Start listening to events (but don't listen twice)
        display.removeKeyListener(this);
        display.addKeyListener(this);

        // Give focus to the game screen
        display.setleaderBoard("");
        display.setAccuracy("");
        display.requestFocusInWindow();
        display.refresh();
    }

    /**
     * Adds a new Participant
     */
    public void addParticipant (Participant p)
    {
        pstate.addParticipant(p);
    }

    /**
     * The ship has been destroyed
     */
    public void shipDestroyed ()
    {
        // Prevents Exceptions from being thrown when input is given but there is no ship.
        upKey = false;
        downKey = false;
        rightKey = false;
        leftKey = false;
        display.removeKeyListener(this);

        // Null out the ship
        ship = null;

        // Remove one life.
        removeLife();

        // Stop the beat timer.
        beatTimer.stop();
        alienBulletTimer.stop();

        // Since the ship was destroyed, schedule a transition
        scheduleTransition(END_DELAY);
    }

    /**
     * An asteroid has been destroyed
     */
    public void asteroidDestroyed (Asteroid p)
    {

        // Creates two medium asteroids if a larger one is destroyed.
        if (p.getSize() == 2)
        {
            addParticipant(new Asteroid(RANDOM.nextInt(4), 1, p.getX(), p.getY(), RANDOM.nextInt(3) + 3, this));
            addParticipant(new Asteroid(RANDOM.nextInt(4), 1, p.getX(), p.getY(), RANDOM.nextInt(3) + 3, this));

            // 20 points for destroying a large asteroid.
            score = score + 20;
            display.setScore(score + "");
        }
        // Creates two small asteroids if a medium one is destroyed.
        else if (p.getSize() == 1)
        {
            addParticipant(new Asteroid(RANDOM.nextInt(4), 0, p.getX(), p.getY(), RANDOM.nextInt(6) + 3, this));
            addParticipant(new Asteroid(RANDOM.nextInt(4), 0, p.getX(), p.getY(), RANDOM.nextInt(6) + 3, this));

            // 50 points for destroying a medium asteroid
            score = score + 50;
            display.setScore(score + "");
        }
        else if (p.getSize() == 0)
        {
            // 100 points for destroying a small asteroid
            score = score + 100;
            display.setScore(score + "");

            // If all the asteroids are gone, schedule a transition to move to the next level
            if (pstate.countAsteroids() == 0)
            {
                scheduleTransition(END_DELAY);
                beatTimer.stop();

                // Stop the alien timer if it running.
                if (alienTimer.isRunning())
                {
                    alienTimer.stop();
                    alienBulletTimer.stop();
                }

            }
        }
    }

    /**
     * Alien ship returns after 5 seconds
     */
    public void alienDestroyed (AlienShip A)
    {
        if (level == 2)
        {
            // Null out the alien ship since it has been destroyed. 
            alienShip = null;
        }
        else if (level > 2)
        {
            // Null out the small alien ship since it has been destroyed. 
            smallAlienShip = null;
        }

        // Stop the bullet timer so that bullets are not fired when there isn't an active ship. 
        alienBulletTimer.stop();

        // Increase the score if and start a timer for the next alien ship. 
        if (A.getSize() == 0)
        {
            alienTimer.start();
            score = score + 1000;
            display.setScore(score + "");
        }
        // Increase the score if and start a timer for the next alien ship.
        else if (A.getSize() == 1)
        {
            alienTimer.start();
            score = score + 200;
            display.setScore(score + "");
        }
    }

    /**
     * Schedules a transition m msecs in the future
     */
    private void scheduleTransition (int m)
    {
        transitionTime = System.currentTimeMillis() + m;
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
                    
                    // Start new game.
                    initialScreen();
                }
                else if (level == 2 && alienShip != null)
                {
                    alienShip.playClip("bigStop");
                    Participant.expire(alienShip);
                    
                    // Start new game.
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
            if (downKey && pstate.countBullets() < 8 && !ship.equals(null))
            {
                placeBullet();
            }

            // It may be time to make a game transition
            performTransition();

            // Move the participants to their new locations
            pstate.moveParticipants();

            // Refresh screen
            display.refresh();

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
            // Places an alienShip and stops the timer.
            placeAlienShip();
            alienTimer.stop();
        }
        else if (e.getSource().equals(alienBulletTimer))
        {
            // Places an alien bullet.
            placeAlienBullet();
        }

    }

    /**
     * Returns an iterator over the active participants
     */
    public Iterator<Participant> getParticipants ()
    {
        return pstate.getParticipants();
    }

    /**
     * If the transition time has been reached, transition to a new state
     */
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
                
                // Expire any active bullet participants
                Iterator<Participant> iter = this.getParticipants();
                while (iter.hasNext())
                {
                    Participant p = iter.next();
                    if (p instanceof Bullets)
                    {    
                        Participant.expire(p);
                    }
                }

            }

        }
    }

    /**
     * If a key of interest is pressed, record that it is down.
     */
    @Override
    public void keyPressed (KeyEvent e)
    {
        if ((e.getKeyCode() == KeyEvent.VK_RIGHT && ship != null) || (e.getKeyCode() == KeyEvent.VK_D && ship != null))
        {
            rightKey = true;
        }
        else if ((e.getKeyCode() == KeyEvent.VK_LEFT && ship != null)
                || (e.getKeyCode() == KeyEvent.VK_A && ship != null))
        {
            leftKey = true;
        }
        else if ((e.getKeyCode() == KeyEvent.VK_UP && ship != null)
                || (e.getKeyCode() == KeyEvent.VK_W && ship != null))
        {
            upKey = true;
        }
        else if ((e.getKeyCode() == KeyEvent.VK_DOWN && ship != null)
                || (e.getKeyCode() == KeyEvent.VK_S && ship != null)
                || (e.getKeyCode() == KeyEvent.VK_SPACE && ship != null))
        {
            downKey = true;
        }

    }

    /**
     * These events are ignored.
     */
    @Override
    public void keyTyped (KeyEvent e)
    {
    }

    /**
     * If a key of interest is released, record that it is no longer down.
     */
    @Override
    public void keyReleased (KeyEvent e)
    {
        if ((e.getKeyCode() == KeyEvent.VK_RIGHT && ship != null) || (e.getKeyCode() == KeyEvent.VK_D && ship != null))
        {
            rightKey = false;
        }
        else if ((e.getKeyCode() == KeyEvent.VK_LEFT && ship != null)
                || (e.getKeyCode() == KeyEvent.VK_A && ship != null))
        {
            leftKey = false;
        }
        else if ((e.getKeyCode() == KeyEvent.VK_UP && ship != null)
                || (e.getKeyCode() == KeyEvent.VK_W && ship != null))
        {
            upKey = false;
            ship.setThruster();
        }
        else if ((e.getKeyCode() == KeyEvent.VK_DOWN && ship != null)
                || (e.getKeyCode() == KeyEvent.VK_S && ship != null)
                || (e.getKeyCode() == KeyEvent.VK_SPACE && ship != null))
        {
            downKey = false;
        }
    }
}
