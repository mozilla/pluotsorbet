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
 * The MIDlet's main class. Handles the game lifecycle and owns
 * the various game screens and commands.
 * @author Jean-Francois Doue
 * @version 1.4, 2002/10/14
 */
public class Game extends MIDlet implements CommandListener {
    /**
     * The random number generator used throughout the
     * game
     */
    public static Random random = new Random();

    /**
     * A timer used to schedule the transitions between
     * the various game screens.
     */
    public static Timer timer;

    /**
     * The game's main screen, where all the drawing occurs.
     */
    public static Field field;

    /**
     * A form used to get the player's initials for high scores.
     */
    public static Form scoreForm;

    /**
     * A form used to display license info.
     */
    public static Form licenseForm;

    /**
     * The high score database.
     */
    public static Scores scores;

    /**
     * The MIDlet display.
     */
    public static Display display;
    private static Command _exitCommand;
    private static Command _startCommand;
    private static Command _licenseCommand;
    private static Command _scoreOkCommand;
    private static Command _licenseOkCommand;
    private static TextField _scoreField;
    private static Displayable _currentDisplayable;

    /**
     * Constructor invoked by the application management software.
     * Do not obfuscate.
     */
    public Game() {

        // Retrieve the display.
        display =  Display.getDisplay(this);

        // Create a form which will be used to display
        // licensing info.
        licenseForm = new Form("License");
        licenseForm.append("This game is free software licensed under the GNU General Public License. For details, see http://jfdoue.free.fr");
        _licenseOkCommand = new Command("Ok", Command.SCREEN, 1);
        licenseForm.addCommand(_licenseOkCommand);
        licenseForm.setCommandListener(this);

        // Instantiate the main game canvas.
        // Add two commands to the canvas, one to start the game
        // and one to exit the MIDlet
        field = new Field();
        _exitCommand = new Command("Exit", Command.EXIT, 1);
        _startCommand = new Command("Start", Command.SCREEN, 1);
        _licenseCommand = new Command("License", Command.SCREEN, 3);
        field.addCommand(_exitCommand);
        field.addCommand(_startCommand);
        field.addCommand(_licenseCommand);
        field.setCommandListener(this);
        _currentDisplayable = field;

        // Create a form which will be used to enter the
        // user name for high scores.
        scoreForm = new Form("Congratulations");
        _scoreField = new TextField("Your score is one of the 3 best. Enter your initials", "", 3, TextField.ANY);
        scoreForm.append(_scoreField);
        _scoreOkCommand = new Command("Done", Command.SCREEN, 1);
        scoreForm.addCommand(_scoreOkCommand);
        scoreForm.setCommandListener(this);

        // Read the high scores from persistent storage.
        scores = new Scores();

        // Initialize the random number generator.
        random = new Random();
    }

    /**
     * Method used to switch screens.
     */
    public static void setDisplayable(Displayable displayable) {
        _currentDisplayable = displayable;
        display.setCurrent(displayable);
    }

    /**
     * Invoked by Field to ask the game to switch to
     * the high score form.
     */
    public static void enterHighScore() {
        _scoreField.setString("");
        setDisplayable(scoreForm);
    }

    // Methods overwritten from MIDlet. These methods are
    // invoked by the application management software
    // to ask to MIDlet to enter the desired state.

    /**
     * Overriden from MIDlet.
     */
    protected void startApp() {
        //System.out.println("startApp");

        // The game has two application threads
        // + One timer thread to trigger a switch between
        // the different game screens while not in game mode.
        // + A thread to run the main game loop.
        timer = new Timer();
        setDisplayable(_currentDisplayable);
        Thread thread = new Thread(field);
        thread.start();
        field.setState(field.getState());
    }

    /**
     * Overriden from MIDlet.
     */
    protected void pauseApp() {
        //System.out.println("pauseApp");

        // Kill the two game threads.
        field.pause();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    /**
     * Overriden from MIDlet.
     */
    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {

        field.pause();
        timer.cancel();
        timer = null;
        random = null;
        field = null;
        scores = null;
        display = null;
        _exitCommand = null;
        _startCommand = null;
        _scoreOkCommand = null;
        scoreForm = null;
        licenseForm = null;
        _scoreField = null;
        _currentDisplayable = null;
        //System.out.println("computeavg = " + (Field.computeavg / Field.frames));
        //System.out.println("paintavg = " + (Field.paintavg / Field.frames));
    }

    // Implementation of the CommandListener interface
    /**
     * CommandListener interface implementation.
     */
    public void commandAction(Command c, Displayable d) {
       if (c == _exitCommand) {
            try {
                destroyApp(false);
            } catch(MIDletStateChangeException e) {
            }
          notifyDestroyed();
       } else if (c == _startCommand) {
           field.setState(Field.GAME_STATE);
           field.newGame();
       } else if (c == _scoreOkCommand) {
           scores.addHighScore(field.getScore(), _scoreField.getString());
           field.setState(Field.HISCORE_STATE);
           setDisplayable(field);
            field.setFrozen(false);
       } else if (c == _licenseCommand) {
            field.setFrozen(true);
           setDisplayable(licenseForm);
       } else if (c == _licenseOkCommand) {
           setDisplayable(field);
          field.setState(field.getState());
            field.setFrozen(false);
       }
    }
}
