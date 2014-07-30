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

package javax.microedition.lcdui;

import java.util.TimerTask;
import java.util.Timer;

/**
 * An object that has the capability of being placed on the display.  A 
 * <code>Displayable</code> object may have a title, a ticker,
 * zero or more commands and a listener associated with it.  The
 * contents displayed and their interaction with the user are defined by 
 * subclasses.
 *
 * <p>The title string may contain
 * <A HREF="Form.html#linebreak">line breaks</a>.
 * The display of the title string must break accordingly.
 * For example, if only a single line is available for a
 * title and the string contains a line break then only the characters
 * up to the line break are displayed.</p>
 *
 * <p>Unless otherwise specified by a subclass, the default state of newly 
 * created <code>Displayable</code> objects is as follows:</p>
 *
 * <ul>
 * <li>it is not visible on the <code>Display</code>;</li>
 * <li>there is no <code>Ticker</code> associated with this
 * <code>Displayable</code>;</li>
 * <li>the title is <code>null</code>;</li>
 * <li>there are no <code>Commands</code> present; and</li>
 * <li>there is no <code>CommandListener</code> present.</li>
 * </ul>
 *
 * @since MIDP 1.0
 */

public abstract class Displayable {

// ************************************************************
//  public member variables
// ************************************************************

// ************************************************************
//  protected member variables
// ************************************************************

    /**
     * Create a new Displayable
     */
    Displayable() {
    }

    /**
     * Create a new Displayable with a passed in title
     *
     * @param title the Displayable's title, or null for no title
     */
    Displayable(String title) {
        synchronized (Display.LCDUILock) {
            this.title = title;
        }
    }

// ************************************************************
//  public methods
// ************************************************************

    /**
     * Gets the title of the <code>Displayable</code>. Returns
     * <code>null</code> if there is no title.
     * @return the title of the instance, or <code>null</code> if no title
     * @see #setTitle
     */
    public String getTitle() {
        synchronized (Display.LCDUILock) {
            return title;
        }
    }

    /**
     * Sets the title of the <code>Displayable</code>. If
     * <code>null</code> is given,
     * removes the title. 
     *
     * <P>If the <code>Displayable</code> is actually visible on
     * the display,
     * the implementation should update 
     * the display as soon as it is feasible to do so.</P>
     * 
     * <P>The existence of a title  may affect the size
     * of the area available for <code>Displayable</code> content. 
     * Addition, removal, or the setting of the title text at runtime
     * may dynamically change the size of the content area.
     * This is most important to be aware of when using the
     * <code>Canvas</code> class.
     * If the available area does change, the application will be notified
     * via a call to {@link #sizeChanged(int, int) sizeChanged()}. </p>
     *
     * @param s the new title, or <code>null</code> for no title
     * @see #getTitle
     */
    public void setTitle(String s) {
        synchronized (Display.LCDUILock) {

            if (title == s || (title != null && title.equals(s))) {
                return;
            }
            String oldTitle = title;
            this.title = s;
            displayableLF.lSetTitle(oldTitle, title);
        }
    }

    /**
     * Gets the ticker used by this <code>Displayable</code>.
     * @return ticker object used, or <code>null</code> if no
     * ticker is present
     * @see #setTicker
     */
    public Ticker getTicker() {
        synchronized (Display.LCDUILock) {
            return ticker;
        }
    }

    /**
     * Sets a ticker for use with this <code>Displayable</code>,
     * replacing any
     * previous ticker.
     * If <code>null</code>, removes the ticker object
     * from this <code>Displayable</code>. The same ticker may be shared by 
     * several <code>Displayable</code>
     * objects within an application. This is done by calling
     * <code>setTicker()</code>
     * with the same <code>Ticker</code> object on several
     * different <code>Displayable</code> objects.
     * If the <code>Displayable</code> is actually visible on the display,
     * the implementation should update 
     * the display as soon as it is feasible to do so.
     * 
     * <p>The existence of a ticker may affect the size
     * of the area available for <code>Displayable's</code> contents. 
     * Addition, removal, or the setting of the ticker at runtime
     * may dynamically change the size of the content area.
     * This is most important to be aware of when using the
     * <code>Canvas</code> class.
     * If the available area does change, the application will be notified
     * via a call to {@link #sizeChanged(int, int) sizeChanged()}. </p>
     *
     * @param ticker the ticker object used on this screen
     * @see #getTicker
     */
    public void setTicker(Ticker ticker) {
        synchronized (Display.LCDUILock) {

            // Return early if there's nothing to do :
            // ticker is the same or (old and new tickers are null)
            if (this.ticker == ticker) {
                return;
            }
            Ticker oldTicker = this.ticker;

            this.ticker = ticker;
            
            displayableLF.lSetTicker(oldTicker, ticker); 
        }
    }

    /**
     * Checks if the <code>Displayable</code> is actually visible
     * on the display.  In order
     * for a <code>Displayable</code> to be visible, all of the
     * following must be true:
     * the <code>Display's</code> <code>MIDlet</code> must be
     * running in the foreground, the <code>Displayable</code>
     * must be the <code>Display's</code> current screen, and the
     * <code>Displayable</code> must not be
     * obscured by a <a href="Display.html#systemscreens">
     * system screen</a>.
     *
     * @return <code>true</code> if the
     * <code>Displayable</code> is currently visible
     */
    public boolean isShown() {
        synchronized (Display.LCDUILock) {
            return displayableLF.lIsShown();
        }
    }

    /**
     * Adds a command to the <code>Displayable</code>. The
     * implementation may choose,
     * for example,
     * to add the command to any of the available soft buttons or place it 
     * in a menu.
     * If the added command is already in the screen (tested by comparing the
     * object references), the method has no effect.
     * If the <code>Displayable</code> is actually visible on the
     * display, and this call
     * affects the set of visible commands, the implementation should update 
     * the display as soon as it is feasible to do so.
     * 
     * @param cmd the command to be added
     *
     * @throws NullPointerException if <code>cmd</code> is
     * <code>null</code>
     */
    public void addCommand(Command cmd) {
        if (cmd == null) {
            throw new NullPointerException();
        }

        synchronized (Display.LCDUILock) {
            addCommandImpl(cmd);
            if (displayableLF!=null) {
                displayableLF.lAddCommand(cmd, numCommands-1);
            }
        }
    }

    /**
     * Removes a command from the <code>Displayable</code>.
     * If the command is not in the <code>Displayable</code>
     * (tested by comparing the
     * object references), the method has no effect.
     * If the <code>Displayable</code> is actually visible on the
     * display, and this call
     * affects the set of visible commands, the implementation should update 
     * the display as soon as it is feasible to do so.
     * If <code>cmd</code> is <code>null</code>, this method
     * does nothing.
     * 
     * @param cmd the command to be removed
     */
    public void removeCommand(Command cmd) {
        synchronized (Display.LCDUILock) {
            int i = removeCommandImpl(cmd);
            if (i != -1) {
                displayableLF.lRemoveCommand(cmd, i);
            }
        }
    }

    /**
     * Sets a listener for {@link Command Commands} to this
     * <code>Displayable</code>,
     * replacing any previous <code>CommandListener</code>. A
     * <code>null</code> reference is
     * allowed and has the effect of removing any existing listener.
     *
     * @param l the new listener, or <code>null</code>.
     */
    public void setCommandListener(CommandListener l) {
        synchronized (Display.LCDUILock) {
            listener = l;
        }
	displayableLF.updateCommandSet();
    }

    /**
     * Gets the width in pixels of the displayable area available to the 
     * application.  The value returned is appropriate for the particular 
     * <code>Displayable</code> subclass.  This value may depend
     * on how the device uses the
     * display and may be affected by the presence of a title, a ticker, or 
     * commands.
     * This method returns the proper result at all times, even if the
     * <code>Displayable</code> object has not yet been shown.
     * 
     * @return width of the area available to the application
     */
    public int getWidth() {
        synchronized (Display.LCDUILock) {
            return displayableLF.lGetWidth();
        }
    }

    /**
     * Gets the height in pixels of the displayable area available to the 
     * application.  The value returned is appropriate for the particular 
     * <code>Displayable</code> subclass.  This value may depend
     * on how the device uses the
     * display and may be affected by the presence of a title, a ticker, or 
     * commands.
     * This method returns the proper result at all times, even if the
     * <code>Displayable</code> object has not yet been shown.
     * 
     * @return height of the area available to the application
     */
    public int getHeight() {
        synchronized (Display.LCDUILock) {
            return displayableLF.lGetHeight();
        }
    }

// ************************************************************
//  protected methods
// ************************************************************

    /**
     * The implementation calls this method when the available area of the
     * <code>Displayable</code> has been changed. 
     * The &quot;available area&quot; is the area of the display that
     * may be occupied by
     * the application's contents, such as <code>Items</code> in a
     * <code>Form</code> or graphics within
     * a <code>Canvas</code>.  It does not include space occupied
     * by a title, a ticker,
     * command labels, scroll bars, system status area, etc.  A size change
     * can occur as a result of the addition, removal, or changed contents of 
     * any of these display features.
     *
     * <p> This method is called at least once before the
     * <code>Displayable</code> is shown for the first time.
     * If the size of a <code>Displayable</code> changes while
     * it is visible,
     * <CODE>sizeChanged</CODE> will be called.  If the size of a
     * <code>Displayable</code>
     * changes while it is <em>not</em> visible, calls to
     * <CODE>sizeChanged</CODE> may be deferred.  If the size had changed
     * while the <code>Displayable</code> was not visible,
     * <CODE>sizeChanged</CODE> will be
     * called at least once at the time the
     * <code>Displayable</code> becomes visible once
     * again.</p>
     *
     * <p>The default implementation of this method in <code>Displayable</code>
     * and its
     * subclasses defined in this specification must be empty.
     * This method is intended solely for being overridden by the
     * application. This method is defined on <code>Displayable</code>
     * even though applications are prohibited from creating 
     * direct subclasses of <code>Displayable</code>.
     * It is defined here so that applications can override it in
     * subclasses of <code>Canvas</code> and <code>Form</code>.
     * This is useful for <code>Canvas</code> subclasses to tailor
     * their graphics and for <code>Forms</code> to modify
     * <code>Item</code> sizes and layout
     * directives in order to fit their contents within the the available
     * display area.</p>
     * 
     * @param w the new width in pixels of the available area
     * @param h the new height in pixels of the available area
     */ 
    protected void sizeChanged(int w, int h) {
        // this method is intended to be overridden by the application
    }

// ************************************************************
//  package private methods
// ************************************************************

    /**
     * Gets look&feel for this Displayable object
     * This method is implemented in the subclasses.
     * @return - DisplayableLF for this Displayable object
     */
    DisplayableLF getLF() {
        return displayableLF;
    }


    /**
     * Called to schedule a call to itemStateChanged() due to
     * a change in the given Item.
     *
     * @param src the Item which has changed
     */
    void itemStateChanged(Item src) {
        /*
         * This call could happen on a Displayable that is not currently
         * visible (either not current, or the Display instance is not
         * foreground).
         */
        Display.itemStateChanged(src);
    }


    /**
     * Called by the event handler to notify any ItemStateListener
     * of a change in the given Item. 
     * The default implementation of this function does nothing.
     *
     * @param src The Item which has changed
     */
    void uCallItemStateChanged(Item src) { }


    /**
     * Add a Command to this Displayable
     *
     * @param cmd The Command to add to this Displayable
     * @return command index
     */
    int addCommandImpl(Command cmd) {
        for (int i = 0; i < numCommands; ++i) {
            if (commands[i] == cmd) {
                return -1;
            }
        }

        if ((commands == null) || (numCommands == commands.length)) {
            Command[] newCommands = new Command[numCommands + 4];
            if (commands != null) {
                System.arraycopy(commands, 0, newCommands, 0, numCommands);
            }
            commands = newCommands;
        }

        commands[numCommands] = cmd;
        ++numCommands;

        return numCommands-1;
    }

    /**
     * Remove a Command from this Displayable
     *
     * @param cmd The Command to remove from this Displayable
     * @return command index
     */
    int removeCommandImpl(Command cmd) {
        for (int i = 0; i < numCommands; ++i) {
            if (commands[i] == cmd) {
                commands[i] = commands[--numCommands];
                commands[numCommands] = null;
                return i;
            }
        }

        return -1;
    }

// ************************************************************
//  private methods
// ************************************************************
    
// ************************************************************
//  package private member variables
// ************************************************************

    /** An array of Commands added to this Displayable */
    Command commands[];

    /** The number of Commands added to this Displayable */
    int numCommands;

    /** The CommandListener for Commands added to this Displayable */
    CommandListener listener;

    /** True, if this Displayable is in full screen mode */
    boolean isInFullScreenMode; // = false

    /** True, if this Displayable is rotated */
    boolean isRotated; // = false

    /** The title for this Displayable */
    String title;

    /** The ticker that may be set for this Displayable */
    Ticker ticker;

    /** The Look &amps; Feel object associated with this Displayable */
    DisplayableLF displayableLF;


// ************************************************************
//  private member variables
// ************************************************************


// ************************************************************
//  Static initializer, constructor
// ************************************************************

} // Displayable

