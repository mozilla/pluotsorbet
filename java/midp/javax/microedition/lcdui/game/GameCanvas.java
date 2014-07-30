/*
 *   
 *
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package javax.microedition.lcdui.game;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Canvas;
import com.sun.midp.lcdui.GameMap;
import com.sun.midp.lcdui.GameCanvasLFImpl;
import com.sun.midp.lcdui.GameAccess;


/**
 * The GameCanvas class provides the basis for a game user interface.  In
 * addition to the features inherited from Canvas (commands, input events,
 * etc.) it also provides game-specific capabilities such as an
 * off-screen graphics buffer and the ability to query key status.
 * <p>
 * A dedicated buffer is created for each GameCanvas instance.  Since a
 * unique buffer is provided for each GameCanvas instance, it is preferable
 * to re-use a single GameCanvas instance in the interests of minimizing
 * heap usage.  The developer can assume that the contents of this buffer
 * are modified only by calls to the Graphics object(s) obtained from the
 * GameCanvas instance; the contents are not modified by external sources
 * such as other MIDlets or system-level notifications.  The buffer is
 * initially filled with white pixels.
 * <p>
 * The buffer's size is set to the maximum dimensions of the GameCanvas.
 * However, the area that may be flushed is limited by the current
 * dimensions of the GameCanvas (as influenced by the presence of a Ticker,
 * Commands, etc.) when the flush is requested.  The current dimensions of
 * the GameCanvas may be obtained by calling
 * {@link javax.microedition.lcdui.Canvas#getWidth getWidth} and
 * {@link javax.microedition.lcdui.Canvas#getHeight getHeight}.
 * <p>
 * A game may provide its own thread to run the game loop.  A typical loop
 * will check for input, implement the game logic, and then render the updated
 * user interface. The following code illustrates the structure of a typical
 * game loop: <code>
 * <pre>
 * // Get the Graphics object for the off-screen buffer
 * Graphics g = getGraphics();
 *
 * while (true) {
 *      // Check user input and update positions if necessary
 *      int keyState = getKeyStates();
 *      if ((keyState & LEFT_PRESSED) != 0) {
 *          sprite.move(-1, 0);
 *      }
 *      else if ((keyState & RIGHT_PRESSED) != 0) {
 *          sprite.move(1, 0);
 *      }
 *
 *	// Clear the background to white
 *	g.setColor(0xFFFFFF);
 *	g.fillRect(0,0,getWidth(), getHeight());
 *
 *      // Draw the Sprite
 *      sprite.paint(g);
 *
 *      // Flush the off-screen buffer
 *      flushGraphics();
 * }
 * </pre>
 * </code>
 * <P>
 **/

public abstract class GameCanvas extends Canvas
{
    /** The look&feel implementation associated with this GameCanvas */
    private GameCanvasLFImpl gameCanvasLF;

    /** Implementor of GameAccess interface handed out to external packages */ 
    private static GameAccess gameAccess;

    /**
     * The bit representing the UP key.  This constant has a value of 
     * <code>0x0002</code> (1 << Canvas.UP).
     */
    public static final int UP_PRESSED = 1 << Canvas.UP;

    /**
     * The bit representing the DOWN key.  This constant has a value of 
     * <code>0x0040</code> (1 << Canvas.DOWN).
     */
    public static final int DOWN_PRESSED = 1 << Canvas.DOWN;

    /**
     * The bit representing the LEFT key.  This constant has a value of 
     * <code>0x0004</code> (1 << Canvas.LEFT).
     */
    public static final int LEFT_PRESSED = 1 << Canvas.LEFT;

    /**
     * The bit representing the RIGHT key.  This constant has a value of 
     * <code>0x0020</code> (1 << Canvas.RIGHT).
     */
    public static final int RIGHT_PRESSED = 1 << Canvas.RIGHT;

    /**
     * The bit representing the FIRE key.  This constant has a value of 
     * <code>0x0100</code> (1 << Canvas.FIRE).
     */
    public static final int FIRE_PRESSED = 1 << Canvas.FIRE;

    /**
     * The bit representing the GAME_A key (may not be supported on all 
     * devices).  This constant has a value of 
     * <code>0x0200</code> (1 << Canvas.GAME_A).
     */
    public static final int GAME_A_PRESSED = 1 << Canvas.GAME_A;

    /**
     * The bit representing the GAME_B key (may not be supported on all 
     * devices).  This constant has a value of 
     * <code>0x0400</code> (1 << Canvas.GAME_B).
     */
    public static final int GAME_B_PRESSED = 1 << Canvas.GAME_B;

    /**
     * The bit representing the GAME_C key (may not be supported on all 
     * devices).  This constant has a value of 
     * <code>0x0800</code> (1 << Canvas.GAME_C).
     */
    public static final int GAME_C_PRESSED = 1 << Canvas.GAME_C;

    /**
     * The bit representing the GAME_D key (may not be supported on all 
     * devices).  This constant has a value of 
     * <code>0x1000</code> (1 << Canvas.GAME_D).
     */
    public static final int GAME_D_PRESSED = 1 << Canvas.GAME_D;


    /**
     * Creates a new instance of a GameCanvas.  A new buffer is also created
     * for the GameCanvas and is initially filled with white pixels.
     * <p>
     * If the developer only needs to query key status using the getKeyStates
     * method, the regular key event mechanism can be suppressed for game keys
     * while this GameCanvas is shown.  If not needed by the application, the 
     * suppression of key events may improve performance by eliminating 
     * unnecessary system calls to keyPressed, keyRepeated and keyReleased 
     * methods. 
     * <p>
     * If requested, key event suppression for a given GameCanvas is started 
     * when it is shown (i.e. when showNotify is called) and stopped when it
     * is hidden (i.e. when hideNotify is called).  Since the showing and 
     * hiding of screens is serialized with the event queue, this arrangement
     * ensures that the suppression effects only those key events intended for
     * the corresponding GameCanvas.  Thus, if key events are being generated
     * while another screen is still shown, those key events will continue to
     * be queued and dispatched until that screen is hidden and the GameCanvas
     * has replaced it.
     * <p>
     * Note that key events can be suppressed only for the defined game keys 
     * (UP, DOWN, FIRE, etc.); key events are always generated for all other 
     * keys.  
     * <p>
     * @param suppressKeyEvents <code>true</code> to suppress the regular
     * key event mechanism for game keys, otherwise <code>false</code>.
     */
    protected GameCanvas(boolean suppressKeyEvents) {
        // Create and offscreen Image object that
        // acts as the offscreen buffer to which we draw to.
        // the contents of this buffer are flushed to the display
        // only when flushGraphics() has been called.
        super();
        setSuppressKeyEvents((Canvas)this, suppressKeyEvents);
        gameCanvasLF = new GameCanvasLFImpl(this);

        // Create and hand out game accessor tunnel instance
        if (gameAccess == null) {
            gameAccess = new GameAccessImpl();
            GameMap.registerGameAccess(gameAccess);
        }
    }

    /**
     * Gets look&feel implementation associated with this GameCanvas
     * @return GameCanvasLFImpl instance of this GameCanvas
     */
    GameCanvasLFImpl getLFImpl() {
        return gameCanvasLF;
    }

    /**
     * Obtains the Graphics object for rendering a GameCanvas.  The returned 
     * Graphics object renders to the off-screen buffer belonging to this 
     * GameCanvas.
     * <p>	 
     * Rendering operations do not appear on the display until flushGraphics()
     * is called; flushing the buffer does not change its contents (the pixels
     * are not cleared as a result of the flushing operation).
     * <p>
     * A new Graphics object is created and returned each time this method is
     * called; therefore, the needed Graphics object(s) should be obtained
     * before the game starts then re-used while the game is running.  
     * For each GameCanvas instance, all of the provided graphics objects will
     * render to the same off-screen buffer. 
     * <P>
     * <P>The newly created Graphics object has the following properties:
     * </P>
     * <ul>
     * <LI>the destination is this GameCanvas' buffer;
     * <LI>the clip region encompasses the entire buffer;
     * <LI>the current color is black;
     * <LI>the font is the same as the font returned by
     * {@link javax.microedition.lcdui.Font#getDefaultFont
     * Font.getDefaultFont()};
     * <LI>the stroke style is {@link Graphics#SOLID SOLID}; and
     * <LI>the origin of the coordinate system is located at the upper-left
     * corner of the buffer.
     * </ul>     
     * <p>
     * @return the Graphics object that renders to this GameCanvas' 
     * off-screen buffer
     * @see #flushGraphics()
     * @see #flushGraphics(int, int, int, int)
     */
    protected Graphics getGraphics() {
        return gameCanvasLF.getGraphics();
    }

    /**
     * Gets the states of the physical game keys.  Each bit in the returned
     * integer represents a specific key on the device.  A key's bit will be
     * 1 if the key is currently down or has been pressed at least once since
     * the last time this method was called.  The bit will be 0 if the key
     * is currently up and has not been pressed at all since the last time 
     * this method was called.  This latching behavior ensures that a rapid
     * key press and release will always be caught by the game loop, 
     * regardless of how slowly the loop runs.
     * <p>
     * For example:<code><pre>
     * 
     *      // Get the key state and store it
     *      int keyState = getKeyStates();
     *      if ((keyState & LEFT_KEY) != 0) {
     *          positionX--;
     *      }
     *      else if ((keyState & RIGHT_KEY) != 0) {
     *          positionX++;
     *      }
     *
     * </pre></code>
     * <p>
     * Calling this method has the side effect of clearing any latched state.
     * Another call to getKeyStates immediately after a prior call will 
     * therefore report the system's best idea of the current state of the
     * keys, the latched bits having been cleared by the first call.
     * <p>
     * Some devices may not be able to query the keypad hardware directly and
     * therefore, this method may be implemented by monitoring key press and
     * release events instead.  Thus the state reported by getKeyStates might
     * lag the actual state of the physical keys since the timeliness
     * of the key information is be subject to the capabilities of each
     * device.  Also, some devices may be incapable of detecting simultaneous
     * presses of multiple keys.
     * <p>
     * This method returns 0 unless the GameCanvas is currently visible as
     * reported by {@link javax.microedition.lcdui.Displayable#isShown}.
     * Upon becoming visible, a GameCanvas will initially indicate that 
     * all keys are unpressed (0); if a key is held down while the GameCanvas
     * is being shown, the key must be first released and then pressed in 
     * order for the key press to be reported by the GameCanvas.
     * <p>
     * @see #UP_PRESSED
     * @see #DOWN_PRESSED
     * @see #LEFT_PRESSED
     * @see #RIGHT_PRESSED
     * @see #FIRE_PRESSED
     * @see #GAME_A_PRESSED
     * @see #GAME_B_PRESSED
     * @see #GAME_C_PRESSED
     * @see #GAME_D_PRESSED
     * @return An integer containing the key state information (one bit per 
     * key), or 0 if the GameCanvas is not currently shown.
     */
    public int getKeyStates() {
        return gameCanvasLF.getKeyStates();
    }

    /**
     * Paints this GameCanvas.  By default, this method renders the 
     * the off-screen buffer at (0,0).  Rendering of the buffer is 
     * subject to the clip region and origin translation of the Graphics
     * object.
     * @param g the Graphics object with which to render the screen.
     * @throws NullPointerException if <code>g</code> is <code>null</code>
     */
    public void paint(Graphics g) {
        gameCanvasLF.drawBuffer(g);
    }

    /**
     * Flushes the specified region of the off-screen buffer to the display. 
     * The contents of the off-screen buffer are not changed as a result of 
     * the flush operation.  This method does not return until the flush has
     * been completed, so the app may immediately begin to render the next 
     * frame to the same buffer once this method returns.
     * <p>
     * If the specified region extends beyond the current bounds of the 
     * GameCanvas, only the intersecting region is flushed.  No pixels are
     * flushed if the specified width or height is less than 1.
     * <p>
     * This method does nothing and returns immediately if the GameCanvas is
     * not currently shown or the flush request cannot be honored because the
     * system is busy.
     * <p>
     * @see #flushGraphics()
     * @param x the left edge of the region to be flushed 
     * @param y the top edge of the region to be flushed
     * @param width the width of the region to be flushed
     * @param height the height of the region to be flushed
     */
    public void flushGraphics(int x, int y,
                              int width, int height) {
        gameCanvasLF.flushGraphics(x, y, width, height);
    }

    /**
     * Flushes the off-screen buffer to the display.  The size of the flushed 
     * area is equal to the size of the GameCanvas.  The contents
     * of the  off-screen buffer are not changed as a result of the flush 
     * operation.  This method does not return until the flush has been
     * completed, so the app may immediately begin to render the next frame
     * to the same buffer once this method returns.
     * <p>
     * This method does nothing and returns immediately if the GameCanvas is
     * not currently shown or the flush request cannot be honored because the
     * system is busy.
     * <p>
     * @see #flushGraphics(int,int,int,int)
     */
    public void flushGraphics() {
        gameCanvasLF.flushGraphics();
    }

    /**
     * Set a private field in the <code>Canvas</code> object. We use a
     * native method to work around the package boundary.
     * @param c this <code>GameCanvas</code> cast to a <code>Canvas</code>
     * @param suppressKeyEvents whether or not to suppress key events
     */
    private native void setSuppressKeyEvents(Canvas c,
                                             boolean suppressKeyEvents);
}
