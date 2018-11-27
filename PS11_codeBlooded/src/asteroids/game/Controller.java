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
    private ParticipantState pstate;

    /** The ship (if one is active) or null (otherwise) */
    private Ship ship;
    
    /** Ship life one */
    private ShipLives one;
    
    /** Ship life two */
    private ShipLives two;
    
    /** Ship life three */
    private ShipLives three;

    /** When this timer goes off, it is time to refresh the animation */
    private Timer refreshTimer;
    
    /** When this timer goes off, it is time to play a beat */
    private Timer beatTimer;
    private int nextBeat = INITIAL_BEAT;
    private boolean beat = true;

    /**
     * The time at which a transition to a new stage of the game should be made. A transition is scheduled a few seconds
     * in the future to give the user time to see what has happened before doing something like going to a new level or
     * resetting the current level.
     */
    private long transitionTime;

    /** Number of lives left */
    private int lives;

    /** The game display */
    private Display display;

    /** Records the left key */
    private boolean leftKey;

    /** Records the left key */
    private boolean rightKey;

    /** Records the left key */
    private boolean upKey;

    /** Records the left key */
    private boolean downKey;

    /** Number of asteroids in the next level */
    private int nextLevelAstroids = 5;

    /** Number of asteroids in the current level */
    private int numCurrAstroids = 4;

    /** Number of asteroids in the current level */
    private int level;
    
    /** Records the score */
    private int score;

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

        // Bring up the splash screen and start the refresh timer
        splashScreen();
        display.setVisible(true);
        refreshTimer.start();
        

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
    private void finalScreen ()
    {
        display.setLegend(GAME_OVER);
        display.removeKeyListener(this);
    }

    /**
     * Place a new ship in the center of the screen. Remove any existing ship first.
     */
    private void placeShip ()
    {
        // Place a new ship
        Participant.expire(ship);
        ship = new Ship(SIZE / 2, SIZE / 2, -Math.PI / 2, this);
        addParticipant(ship);
        display.setLegend("");
        
        // Display the level and score when the ship is placed. 
        display.setLevel(level + "");
        display.setScore(score + "");
    }
    
    
    
    /**
     * Place the lives underneath the score
     */
    private void placeLives()
    {
        // Place the lives
        one = new ShipLives(LABEL_HORIZONTAL_OFFSET, LABEL_VERTICAL_OFFSET + 60, -Math.PI / 2, this);
        two = new ShipLives(LABEL_HORIZONTAL_OFFSET + 30, LABEL_VERTICAL_OFFSET + 60, -Math.PI / 2, this);
        three = new ShipLives(LABEL_HORIZONTAL_OFFSET + 60, LABEL_VERTICAL_OFFSET + 60, -Math.PI / 2, this);
        
        addParticipant(one);
        addParticipant(two);
        addParticipant(three);
    }

    /**
     * Place a new bullet at the nose of the ship..
     */
    private void placeBullet ()
    {
        // Place a new bullet
        addParticipant(new Bullets((ship.getXNose()), ship.getYNose(), ship.getRotation(), this));
    }

    /**
     * Places an asteroid near one corner of the screen. Gives it a random velocity and rotation.
     */
    private void placeAsteroids ()
    {
        addParticipant(new Asteroid(RANDOM.nextInt(4), 2, EDGE_OFFSET, EDGE_OFFSET, 3, this));
        addParticipant(new Asteroid(RANDOM.nextInt(4), 2, -EDGE_OFFSET, EDGE_OFFSET, 3, this));
        addParticipant(new Asteroid(RANDOM.nextInt(4), 2, EDGE_OFFSET, -EDGE_OFFSET, 3, this));
        addParticipant(new Asteroid(RANDOM.nextInt(4), 2, -EDGE_OFFSET, -EDGE_OFFSET, 3, this));
    }
    
//    /**
//     * Display a thruster at the tail of the ship..
//     */
//    private void placeThruster ()
//    {
//        // Display a new thruster
//        addParticipant(new Thruster(ship.getX(), ship.getY(), ship.getDirection(), ship.getRotation(), ship.getSpeed(), this));
//    }

    /**
     * Clears the screen so that nothing is displayed
     */
    private void clear ()
    {
        pstate.clear();
        display.setLegend("");
        ship = null;
    }

    /**
     * Sets things up and begins a new game.
     */
    private void initialScreen ()
    {      
        // Reset the level 
        level = 1;
        nextLevelAstroids = 5;
        numCurrAstroids = 4;
        score = 0;
        
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
        
        // Place the lives
        placeLives();

        // Reset statistics
        lives = 3;

        // Start listening to events (but don't listen twice)
        display.removeKeyListener(this);
        display.addKeyListener(this);

        // Give focus to the game screen
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

        // Decrement lives
        lives--;
        
        // Removes the lives when they are destroyed. 
        if (lives == 2)
        {
            three.remove();
        }
        else if (lives == 1)
        {
            two.remove();
        }
        else if(lives == 0)
        {
            one.remove();
        }

        // Stop the beat timer.
        beatTimer.stop();
        
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
                display.addKeyListener(this);
            }
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
            initialScreen();
        }

        // Time to refresh the screen and deal with keyboard input
        else if (e.getSource() == refreshTimer )
        {
            if (rightKey)
            {
                ship.turnRight();
            }
            if (leftKey)
            {
                ship.turnLeft();    
            }
            if (upKey)
            {
                ship.accelerate();
            }
            if (downKey && pstate.countBullets() < 8)
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
                if ( beat1 != null)
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
                if ( beat2 != null)
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
    private void performTransition ()
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
                // Stops the beat Timer.
                beatTimer.stop();

                finalScreen();
            }
            
            // Places a new ship if the previous ship has been destroyed and there are still lives left.
            else if (lives > 0 && pstate.countAsteroids() != 0)
            {
                // Starts the beat timer once the delay is over.
                beatTimer.start();
                
                // Re-places the ship. 
                placeShip();
                
                // Re-adds the key listener.
                display.addKeyListener(this);
            }
            // Proceeds to the next level if all asteroids have been destroyed.
            else if (pstate.countAsteroids() == 0)
            {
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
     * If a key of interest is pressed, record that it is down.
     */
    @Override
    public void keyPressed (KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_RIGHT && ship != null || e.getKeyCode() == KeyEvent.VK_D)
        {
            rightKey = true;
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT && ship != null || e.getKeyCode() == KeyEvent.VK_A)
        {
            leftKey = true;
        }
        else if (e.getKeyCode() == KeyEvent.VK_UP && ship != null || e.getKeyCode() == KeyEvent.VK_W)
        {
            upKey = true;
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN && ship != null || e.getKeyCode() == KeyEvent.VK_S
                || e.getKeyCode() == KeyEvent.VK_SPACE)
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
        if (e.getKeyCode() == KeyEvent.VK_RIGHT && ship != null || e.getKeyCode() == KeyEvent.VK_D)
        {
            rightKey = false;
        }
        else if (e.getKeyCode() == KeyEvent.VK_LEFT && ship != null || e.getKeyCode() == KeyEvent.VK_A)
        {
            leftKey = false;
        }
        else if (e.getKeyCode() == KeyEvent.VK_UP && ship != null || e.getKeyCode() == KeyEvent.VK_W)
        {
            upKey = false;
            ship.setThruster();
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN && ship != null || e.getKeyCode() == KeyEvent.VK_S
                || e.getKeyCode() == KeyEvent.VK_SPACE)
        {
            downKey = false;
        }
    }
}
