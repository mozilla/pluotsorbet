/**
 * Copyright 2001-2002 Jean-Francois Doue
 *
 * This file is part of Asteroid Zone. Asteroid Zone is free software;
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 * Asteroid Zone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Asteroid Zone; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 */

package asteroids;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.util.*;

/**
 * The game main screen. The class also implements the game's main loop.
 * @author Jean-Francois Doue
 * @author Pavel Machek
 * @version 1.4, 2002/10/14
 */
public class Field extends Canvas implements Runnable {
    /**
     * The 'title' game state. Displays the game name and author.
     */
    public static final byte TITLE_STATE = 0;

    /**
     * The 'control' game state. Displays the mapping between keys and game commands.
     */
    public static final byte CONTROL_STATE = 1;

    /**
     * The 'points' game state. Displays the value of each screen items.
     */
    public static final byte POINTS_STATE = 2;

    /**
     * The 'high score' game state. Displays the high score table.
     */
    public static final byte HISCORE_STATE = 3;

    /**
     * The 'game over' game state. Displays the 'game over' message.
     */
    public static final byte GAMEOVER_STATE = 4;

    /**
     * The 'new high score' game state. Lets players enter their name.
     */
    public static final byte NEWHIGHSCORE_STATE = 5;

    /**
     * The 'game' game state. The player is actively playing the game.
     */
    public static final byte GAME_STATE = 6;

    /**
     * When not in game mode, the game switches its state every TIMEOUT ms
     */
    public static final long TIMEOUT = 8000L;

    public static Font smallFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    public static Font bigFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE);

    /**
     * Time spent moving objects and computing collisions.
     */
    public static long computeavg;

    /**
     * Time spent painting screens.
     */
    public static long paintavg;

    /**
     * Number of game frames since beginning.
     */
    public static long frames;

    private byte _state;         // The current game state
    private Slideshow _nextSlide;   // A TimeTask used to trigger the transition to the next state.
    private int _lives;          // Number of lives left.
    private int _nextBonusLife;     // Score above which next extra life will be awarded.
    private int _score;          // Current score.
    private byte _level;         // Current level.
    private boolean _paused;     // Set the true by the Game if it wants to enter its paused state
                           // This causes the main game thread to exit.
    private boolean _frozen;     // Set when another Displayable is being displayed by the game
                           // (nothing happens in the game in between, but the game 
                           // must be able to resume).

    private Image _buffer;       // To implement double-buffering on platforms which do not have it.
    private char[] _scoreString;
    private char _liveString;
    private char[] _levelString     // The current level indicator
     = {'L', 'e', 'v', 'e', 'l', ' ', '0'};
    private byte _levelStringTimer; // The number of frames during which the level indicator is displayed
    private int _liveStringWidth;   // In pixels.
    private int _lastKeyPressed; // The last key pressed by the player.
    private boolean _isRepeatedKey; // True if _lastKeyPressed was obtained through keyRepeated().

    public Field() {
        computeavg = 0;
        paintavg = 0;
        frames = 0;

        // Determine the dimensions of the Canvas.
        // The game has been developed using the j2mewtk's
        // DefaultColorPhone, using a resolution of 100x96.
      // Object are sized proportionaly on other platforms
      // ratioDenom.
        Mobile.width = getWidth();
        Mobile.height = getHeight();
        Mobile.ratioNum = Math.min(Mobile.width, Mobile.height);

        // The score is stored as a char array for greater
        // efficiency.
        _scoreString = new char[4];

        // For device which do not support double buffering,
        // create an offscreen image for double buffering.
        if (!isDoubleBuffered()) {
            _buffer = Image.createImage(Mobile.width, Mobile.height);
        }
    }

    /**
     * Starts a new game.
     */
    public final void newGame() {
        _level = 0;
        setLives(4);
        _nextBonusLife = 500;
        setScore(0);
        Asteroid.asteroids.removeAll();
        _lastKeyPressed = 0;
        _isRepeatedKey = false;
      Ship.ship.reset();
    }

   /**
    * Returns the number of remaining ships
    */
    public final int getLives() {
      return _lives;
   }

   /**
    * Sets the number of remaining ships
    */
    public final void setLives(int lives) {
        if (lives < 0) {
            lives = 0;
        }
        _lives = lives;
        _liveString = (char)('0' + _lives);
        _liveStringWidth = smallFont.charWidth(_liveString);
    }

    /**
     * Returns the current score.
     */
    public final int getScore() {
        return _score;
    }

    /**
     * Updates the score.
     */
    public final void setScore(int score) {
        _score = score;

        // Convert the score to an array of chars
        // of the form: 0000
        Scores.toCharArray(_score, _scoreString);

        // Extra lives are awarded every 500 points.
        if (_score >= _nextBonusLife) {
            _nextBonusLife += 500;
            setLives(_lives + 1);
        }
    }

    /**
     * Initializes a new level.
     */
    private final void _nextLevel() {
        // At most 7 large asteroids per screen.
        if (_level < 5) {
            _level++;
        }

        // Populate the game level with large asteroids.
        for (byte i = 0; i < 3 + _level; i++) {
            Asteroid asteroid = (Asteroid)Asteroid.asteroids.addNewObject();
            if (asteroid != null) {
                Asteroid.randomInit(asteroid);
            }
        }

        // Clear all the existing rockets or explosions and reset
        // the ship.
        Rocket.rockets.removeAll();
        Explosion.explosions.removeAll();

      if (_level >= 1) {
          _levelString[6] = (char)('0' + _level);
          _levelStringTimer = 20;
      }
    }

    /**
     * Overriden from Canvas.
     */
    protected void paint(Graphics g) {
        Graphics gr = (_buffer != null) ? _buffer.getGraphics() : g;
        gr.setColor(0x00FFFFFF);;
        gr.fillRect(0, 0, getWidth() - 1, getHeight() - 1);
        gr.setColor(0x00000000);
        gr.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

         // Draw the asteroids
        gr.setColor(0x000000FF);
        Asteroid.draw(gr);

        // Draw the explosions
        gr.setColor(0x00FF00FF);
        Explosion.draw(gr);

        // Draw the rockets
        gr.setColor(0x00FF0000);
        Rocket.draw(gr);

        switch(_state) {
            case TITLE_STATE:
                Slideshow.drawTitleScreen(gr);
                break;

            case CONTROL_STATE:
                Slideshow.drawControlScreen(gr);
                break;

            case POINTS_STATE:
                Slideshow.drawPointsScreen(gr);
                break;

            case HISCORE_STATE:
                Slideshow.drawHighScoresScreen(gr);
                break;

            case GAME_STATE:
                gr.setColor(0x00000000);
                //gr.drawRect(10, 10, 16, 16);

                gr.setFont(smallFont);
                gr.drawChars(_scoreString, 0, 4, 2, getHeight() - 1, Graphics.BOTTOM|Graphics.LEFT);
                gr.drawChar(_liveString, Mobile.width - _liveStringWidth - 1,  Mobile.height, Graphics.BOTTOM|Graphics.LEFT);
                Ship.draw(0, Mobile.width - Ship.radius - 2 - _liveStringWidth, Mobile.height - Ship.radius - 1, gr);
              // Display the level indicator (only during the first few frames of the level)
              if (_levelStringTimer > 0) {
                _levelStringTimer--;
                int x = (Mobile.width - Field.smallFont.charsWidth(_levelString, 0, _levelString.length)) >> 1;
                int y = ((Mobile.height) >> 1) + Field.smallFont.getHeight();
               gr.drawChars(_levelString, 0, _levelString.length, x, y, Graphics.BOTTOM|Graphics.LEFT);
            }
                Ship.ship.draw(gr);
                break;

            case GAMEOVER_STATE:
                Slideshow.drawGameOverScreen(gr);
                break;
        }
        if (_buffer != null) {
            g.drawImage(_buffer, 0, 0, Graphics.TOP|Graphics.LEFT);
        }
    }

    /**
     * Overriden from Canvas.
     */
    protected void keyPressed(int keyCode) {
      _lastKeyPressed = keyCode;
    }

   protected void keyRepeated(int keyCode) {
      _lastKeyPressed = keyCode;
      _isRepeatedKey = true;
    }

    /**
     * Returns the current state.
     */
    public final byte getState() {
        return _state;
    }

    /**
     * Triggers a transition from one state of the game automaton
     * to the other.
     */
    public final void setState(byte newState) {
        switch (newState) {
            case TITLE_STATE:
                _nextSlide = new Slideshow(CONTROL_STATE);
                Game.timer.schedule(_nextSlide, TIMEOUT);
                break;
            case CONTROL_STATE:
                _nextSlide = new Slideshow(POINTS_STATE);
                Game.timer.schedule(_nextSlide, TIMEOUT);
                break;
            case POINTS_STATE:
                _nextSlide = new Slideshow(HISCORE_STATE);
                Game.timer.schedule(_nextSlide, TIMEOUT);
                break;
            case HISCORE_STATE:
                _nextSlide = new Slideshow(TITLE_STATE);
                Game.timer.schedule(_nextSlide, TIMEOUT);
                break;
            case GAMEOVER_STATE:
                if (Game.scores.isHighScore(_score)) {
                    _nextSlide = new Slideshow(NEWHIGHSCORE_STATE);
                    Game.timer.schedule(_nextSlide, TIMEOUT / 2);
                } else {
                    _nextSlide = new Slideshow(HISCORE_STATE);
                    Game.timer.schedule(_nextSlide, TIMEOUT);
                }
                break;
            case NEWHIGHSCORE_STATE:
               setFrozen(true);
                if (Game.display.getCurrent() != Game.scoreForm) {
                    Game.enterHighScore();
                }
                break;
            case GAME_STATE:
                if (_nextSlide != null) {
                    _nextSlide.cancel();
                    _nextSlide = null;
                }
                break;
        }
        _state = newState;
    }

    /**
     * Asks the game's main thread to exit.
     */
    public final void pause() {
        _paused = true;
    }

    /**
     *
     */
    public final void setFrozen(boolean frozen) {
      _frozen = frozen;
      if (_frozen) {
            if (_nextSlide != null) {
                _nextSlide.cancel();
                _nextSlide = null;
            }
      }
    }

    /**
     * Executes the game's main loop.
     */
    public void run() {
        try {
            while (!_paused) {
                long time1 = System.currentTimeMillis();
                long time2;
                long time3;
                long ellapsed = 0;
                if (!_frozen) {

               // If the field has been cleared, change the level.
                    if (Asteroid.asteroids.size() == 0) {
                        _nextLevel();
                    }

                    // Animate the explosion and remove those which are done.
                    Explosion.explode();

                    // Move the asteroids.
               Asteroid.move();

                    // Move the rockets and remove those which have expired.
                    Rocket.move();


               // Handle user events
                    // Move the ship (only while the game is actually proceeding)
                    if (_state == GAME_STATE) {
                     if (_lastKeyPressed != 0) {
                     int gameAction = getGameAction(_lastKeyPressed);
                     if (gameAction != 0) {
                         switch(gameAction) {
                             case LEFT:
                                 Ship.ship.rotate(-2);
                                 break;
                             case RIGHT:
                                 Ship.ship.rotate(2);
                                 break;
                             case FIRE:
                                 Ship.ship.shoot(Rocket.rockets);
                                 break;
                             case GAME_A:
                             case UP:
                                 Ship.ship.burn();
                                 break;
                             case GAME_B:
                              if (!_isRepeatedKey) {
                                    Ship.ship.teleport();
                                 }
                                 break;
                         }
                     }
                        _lastKeyPressed = 0;
                        _isRepeatedKey = false;
                     }
                        Ship.ship.move();
                    }

                    // Compute collisions between the asteroids and
                    // the ship and the rockets.
                    Asteroid.collisionDetection();

               // Detect game over
                   if ((_state == GAME_STATE) && (_lives <= 0) && (Ship.ship.isAlive)) {
                       setState(GAMEOVER_STATE);
                   }

                    // Determine the time spent to compute the frame.
                    time2 = System.currentTimeMillis();
                    computeavg += (time2 - time1);

                    // Force a screen refresh.
                    repaint();
                    serviceRepaints();

                    // Determine the time spent to draw the frame.
                    time3 = System.currentTimeMillis();
                    paintavg += (time3 - time2);
                    frames++;

                    // Determine the total time for the frame.
                    ellapsed = time3 - time1;
                }
                // Sleep for a while (at least 20ms)
                // try {
                  // Thread.currentThread().sleep(Math.max(50 - ellapsed, 20));
                  Thread.yield();
                // } catch(java.lang.InterruptedException e) {
                // }
              if (frames % 60 == 0) {
                System.out.println("computeavg = " + (Field.computeavg / Field.frames));
                System.out.println("paintavg = " + (Field.paintavg / Field.frames));
              }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
