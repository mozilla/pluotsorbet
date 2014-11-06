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

/* import javax.microedition.lcdui.KeyConverter; */

import com.sun.midp.configurator.Constants;

/**
 * The <code>Canvas</code> class is a base class for writing
 * only.
 * applications that need to
 * handle low-level events and to issue graphics calls for drawing to the
 * display. Game applications will likely make heavy use of the
 * <code>Canvas</code> class.
 * From an application development perspective, the <code>Canvas</code> class is
 * interchangeable with standard <code>Screen</code> classes, so an
 * application may mix and
 * match <code>Canvas</code> with high-level screens as needed. For
 * example, a List screen
 * may be used to select the track for a racing game, and a
 * <code>Canvas</code> subclass
 * would implement the actual game.
 *
 * <P>The <code>Canvas</code> provides the developer with methods to
 * handle game actions,
 * key events, and
 * pointer events (if supported by the device).  Methods are
 * also provided to identify the device's capabilities and mapping of
 * keys to game actions.
 * The key events are reported with respect to <em>key codes</em>, which
 * are directly bound to concrete keys on the device, use of which may hinder
 * portability.  Portable applications should use game actions instead of key 
 * codes.</p>
 * 
 * <p> Like other subclasses of <code>Displayable</code>, the
 * <code>Canvas</code> class allows the
 * application to register a listener for commands.  Unlike other
 * <code>Displayables</code>,
 * however, the <code>Canvas</code> class requires applications to
 * subclass it in order to
 * use it. The <code>paint()</code> method is declared
 * <code>abstract</code>, and so the
 * application <em>must</em> provide an implementation in its subclass. Other
 * event-reporting methods are not declared <code>abstract,</code> and their
 * default implementations are empty (that is, they do nothing). This allows
 * the application to override only the methods that report events in which the
 * application has interest.  </p>
 *
 * <p> This is in contrast to the {@link Screen Screen} classes, which allow
 * the application to define listeners and to register them with instances of
 * the <code>Screen</code> classes. This style is not used for the
 * <code>Canvas</code> class, because
 * several new listener interfaces would need to be created, one for each kind
 * of event that might be delivered. An alternative would be to have fewer
 * listener interfaces, but this would require listeners to filter out events
 * in which they had no interest. </p>
 *
 * <a name="keyevents"></a>
 * <h3>Key Events</h3>
 *
 * <p> Applications receive keystroke events in which the individual keys are
 * named within a space of <em>key codes</em>. Every key for which events are
 * reported to MIDP applications is assigned a key code.
 * The key code values are unique for each hardware key unless two keys are
 * obvious synonyms for each other.
 * MIDP defines the following key codes:
 * {@link #KEY_NUM0 KEY_NUM0},
 * {@link #KEY_NUM1 KEY_NUM1},
 * {@link #KEY_NUM2 KEY_NUM2},
 * {@link #KEY_NUM3 KEY_NUM3},
 * {@link #KEY_NUM4 KEY_NUM4},
 * {@link #KEY_NUM5 KEY_NUM5},
 * {@link #KEY_NUM6 KEY_NUM6},
 * {@link #KEY_NUM7 KEY_NUM7},
 * {@link #KEY_NUM8 KEY_NUM8},
 * {@link #KEY_NUM9 KEY_NUM9},
 * {@link #KEY_STAR KEY_STAR}, and
 * {@link #KEY_POUND KEY_POUND}.
 * 
 * (These key codes correspond to keys on a ITU-T standard telephone keypad.)
 * Other keys may be present on the keyboard, and they will generally have key
 * codes distinct from those list above.  In order to guarantee portability,
 * applications should use only the standard key codes. </p>
 * 
 * <p>The standard key codes' values are equal to the Unicode encoding for the
 * character that represents the key.  If the device includes any other keys
 * that have an obvious correspondence to a Unicode character, their key code
 * values should equal the Unicode encoding for that character.  For keys that
 * have no corresponding Unicode character, the implementation must use
 * negative values.  Zero is defined to be an invalid key code.  It is thus
 * possible for an application to convert a keyCode into a Unicode character
 * using the following code: </p>
 * 
 * <TABLE BORDER="2">
 * <TR>
 * <TD ROWSPAN="1" COLSPAN="1">
 *    <pre><code>
 *    if (keyCode &gt; 0) {
 *        char ch = (char)keyCode;    
 *        // ...
 *    }    </code></pre>
 * </TD>
 * </TR>
 * </TABLE>
 * <p>This technique is useful only in certain limited cases.  In particular,
 * it is not sufficient for full textual input, because it does not handle
 * upper and lower case, keyboard shift states, and characters that require
 * more than one keystroke to enter.  For textual input, applications should
 * always use {@link TextBox TextBox} or {@link TextField TextField}
 * objects.</p>
 *
 * <p> It is sometimes useful to find the <em>name</em> of a key in order to
 * display a message about this key. In this case the application may use the
 * {@link #getKeyName(int) getKeyName()} method to find a key's name. </p>
 *
 * <a name="gameactions"></a>
 * <h3>Game Actions</h3>
 * <p>
 * Portable applications that need arrow key events and gaming-related events 
 * should use <em>game actions</em> in preference to key codes and key names.  
 * MIDP defines the following game actions:
 * {@link #UP UP},
 * {@link #DOWN DOWN},
 * {@link #LEFT LEFT},
 * {@link #RIGHT RIGHT},
 * {@link #FIRE FIRE},
 * {@link #GAME_A GAME_A},
 * {@link #GAME_B GAME_B},
 * {@link #GAME_C GAME_C}, and
 * {@link #GAME_D GAME_D}.
 * </P>
 * 
 * <P> Each key code may be mapped to at most one game action.  However, a game
 * action may be associated with more than one key code.  The application can
 * translate a key code into a game action using the {@link #getGameAction(int)
 * getGameAction(int keyCode)} method, and it can translate a game action into
 * a key code using the {@link #getKeyCode(int) getKeyCode(int gameAction)}
 * method.  There may be multiple keycodes associated with a particular game 
 * action, but <code>getKeyCode</code> returns only one of them.  Supposing
 * that <code>g</code> is a valid game action and <code>k</code>
 * is a valid key code for a key associated with a game action, consider
 * the following expressions:</p>
 *
 * <TABLE BORDER="2">
 * <TR>
 * <TD ROWSPAN="1" COLSPAN="1">
 *    <pre><code>
 *    g == getGameAction(getKeyCode(g))     // (1)
 *    k == getKeyCode(getGameAction(k))     // (2)    </code></pre>
 * </TD>
 * </TR>
 * </TABLE>
 * <p>Expression (1) is <em>always</em> true.  However, expression (2) 
 * might be true but is <em>not necessarily</em> true.</p>
 *
 * <P>The implementation is not allowed to change the mapping of game
 * actions and key codes during execution of the application.</P>
 *
 * <p> Portable applications that are interested in using game actions should
 * translate every key event into a game action by calling the {@link
 * #getGameAction getGameAction()} method and then testing the result.  For
 * example, on some devices the game actions <code>UP</code>,
 * <code>DOWN</code>, <code>LEFT</code> and <code>RIGHT</code> may be
 * mapped to 4-way navigation arrow keys. In this case,<code>
 * getKeyCode(UP)</code> would
 * return a device-dependent code for the up-arrow key.  On other devices, a
 * possible mapping would be on the number keys <code>2</code>,
 * <code>4</code>, <code>6</code> and <code>8</code>. In this case,
 * <code>getKeyCode(UP)</code> would return <code>KEY_NUM2</code>.  In
 * both cases, the <code>getGameAction()</code>
 * method would return the <code>LEFT</code> game action when the user
 * presses the key that
 * is a &quot;natural left&quot; on her device.  </P>
 *
 * <a name="commands"></a>
 * <h3>Commands</h3>
 * 
 * <p> It is also possible for the user to issue {@link Command commands} when
 * a canvas is current. <code>Commands</code> are mapped to keys and menus in a
 * device-specific fashion. For some devices the keys used for commands may
 * overlap with the keys that will deliver key code events to the canvas. If
 * this is the case, the device will provide a means transparent to the
 * application that enables the user to select a mode that determines whether
 * these keys will deliver commands or key code events to the application.
 * When the <code>Canvas</code> is in normal mode (see <a
 * href="#fullscreen">below</a>),
 * the set of key code events available to a canvas will not change depending
 * upon the number of commands present or the presence of a command listener.
 * When the <code>Canvas</code> is in full-screen mode, if there is no
 * command listener
 * present, the device may choose to deliver key code events for keys that
 * would otherwise be reserved for delivery of commands.  Game developers
 * should be aware that access to commands will vary greatly across devices,
 * and that requiring the user to issue commands during game play may have a
 * great impact on the ease with which the game can be played. </p>
 *
 * <a name="eventdelivery"></a>
 * <h3>Event Delivery</h3>
 *
 * <P> The <code>Canvas</code> object defines several methods that are
 * called by the
 * implementation. These methods are primarily for the purpose of delivering
 * events to the application, and so they are referred to as
 * <em>event delivery</em> methods. The set of methods is: </p>
 *
 * <ul>
 * <li> <code>showNotify()</code> </li>
 * <li> <code>hideNotify()</code> </li>
 * <li> <code>keyPressed()</code> </li>
 * <li> <code>keyRepeated()</code> </li>
 * <li> <code>keyReleased()</code> </li>
 * <li> <code>pointerPressed()</code> </li>
 * <li> <code>pointerDragged()</code> </li>
 * <li> <code>pointerReleased()</code> </li>
 * <li> <code>paint()</code> </li>
 * </ul>
 *
 * <p> These methods are all called serially. That is, the implementation will
 * never call an event delivery method before a prior call to <em>any</em> of
 * the event delivery methods has returned.  The
 * <code>serviceRepaints()</code> method is an exception to this rule, as it
 * blocks until <code>paint()</code> is called and returns. This will occur
 * even if the application is in the midst of one of the event delivery
 * methods when it calls <code>serviceRepaints()</code>.  </p>
 *
 * <p>The {@link Display#callSerially Display.callSerially()} method can be
 * used to serialize some application-defined work with the event stream.
 * For further information, see the
 * <a href="./package-summary.html#events">Event Handling</a> and
 * <a href="./package-summary.html#concurrency">Concurrency</a>
 * sections of the package summary. </p>
 *
 * <p> The key-related, pointer-related, and <code>paint()</code> methods
 * will only be called while the <code>Canvas</code> is actually
 * visible on the output
 * device. These methods will therefore only be called on this
 * <code>Canvas</code> object
 * only after a call to <code>showNotify()</code> and before a call to
 * <code>hideNotify()</code>. After
 * <code>hideNotify()</code> has been called, none of the key,
 * pointer, and <code>paint</code>
 * methods will be called until after a
 * subsequent call to
 * <code>showNotify()</code> has returned.  A call to a
 * <code>run()</code> method resulting from
 * <code>callSerially()</code> may occur irrespective of calls to
 * <code>showNotify()</code> and
 * <code>hideNotify()</code>.  </p>
 *
 * <p> The {@link #showNotify() showNotify()} method is called prior to the
 * <code>Canvas</code> actually being made visible on the display, and
 * the {@link
 * #hideNotify() hideNotify()} method is called after the
 * <code>Canvas</code> has been
 * removed from the display.  The visibility state of a
 * <code>Canvas</code> (or any other
 * <code>Displayable</code> object) may be queried through the use of the {@link
 * Displayable#isShown() Displayable.isShown()} method.  The change in
 * visibility state of a <code>Canvas</code> may be caused by the
 * application management
 * software moving <code>MIDlets</code> between foreground and
 * background states, or by the
 * system obscuring the <code>Canvas</code> with system screens.
 * Thus, the calls to
 * <code>showNotify()</code> and <code>hideNotify()</code> are not
 * under the control of the <code>MIDlet</code> and
 * may occur fairly frequently.  Application developers are encouraged to
 * perform expensive setup and teardown tasks outside the
 * <code>showNotify()</code> and
 * <code>hideNotify()</code> methods in order to make them as
 * lightweight as possible. </p>
 *
 * <a name="fullscreen"></a>
 * <P>A <code>Canvas</code> can be in normal mode or in full-screen
 * mode.  In normal mode,
 * space on the display may be occupied by command labels, a title, and a
 * ticker.  By setting a <code>Canvas</code> into full-screen mode,
 * the application is
 * requesting that the <code>Canvas</code> occupy as much of the
 * display space as is
 * possible.  In full-screen mode, the title and ticker are not displayed even
 * if they are present on the <code>Canvas</code>, and
 * <code>Commands</code> may be presented using some
 * alternative means (such as through a pop-up menu).  Note that the
 * implementation may still consume a portion of the display for things like
 * status indicators, even if the displayed <code>Canvas</code> is in
 * full-screen mode.  In
 * full-screen mode, although the title is not displayed, its text may still 
 * be used for other purposes, such as for the title of a pop-up menu of 
 * <code>Commands</code>.</P>
 *
 * <P><code>Canvas</code> objects are in normal mode by default.  The normal vs.
 * full-screen mode setting is controlled through the use of the {@link
 * #setFullScreenMode} method.</P>
 * 
 * <P>Calling {@link #setFullScreenMode} may result in 
 * {@link #sizeChanged(int, int) sizeChanged()} being called.
 * The default implementation of this method does nothing.
 * The application can override this method to handle changes 
 * in size of available drawing area.
 * </p>
 *
 * <P><strong>Note:</strong> As mentioned in the &quot;Specification
 * Requirements&quot; section
 * of the overview, implementations must provide the user with an indication
 * of network usage. If the indicator is rendered on screen,
 * it must be visible when network activity occurs, even when
 * the <code>Canvas</code> is in full-screen mode.</P>
 *
 * @since MIDP 1.0
 */

public abstract class Canvas extends Displayable {
    
    /**
     * Constant for the <code>UP</code> game action.
     *
     * <P>Constant value <code>1</code> is set to <code>UP</code>.</P>
     */
    public static final int UP = 1;
    
    /**
     * Constant for the <code>DOWN</code> game action.
     *
     * <P>Constant value <code>6</code> is set to <code>DOWN</code>.</P>
     */
    public static final int DOWN = 6;
    
    
    /**
     * Constant for the <code>LEFT</code> game action.
     *
     * <P>Constant value <code>2</code> is set to <code>LEFT</code>.</P>
     */
    public static final int LEFT = 2;
    
    /**
     * Constant for the <code>RIGHT</code> game action.
     *
     * <P>Constant value <code>5</code> is set to <code>RIGHT</code>.</P>
     */
    public static final int RIGHT = 5;
    
    /**
     * Constant for the <code>FIRE</code> game action.
     *
     * <P>Constant value <code>8</code> is set to <code>FIRE</code>.</P>
     */
    public static final int FIRE = 8;
    
    
    /**
     * Constant for the general purpose &quot;<code>A</code>&quot; game action.
     *
     * <P>Constant value <code>9</code> is set to <code>GAME_A</code>.</P>
     */
    public static final int GAME_A = 9;
    
    /**
     * Constant for the general purpose &quot;<code>B</code>&quot; game action.
     *
     * <P>Constant value <code>10</code> is set to <code>GAME_B</code>.</P>
     */
    public static final int GAME_B = 10;
  
    /**
     * Constant for the general purpose &quot;<code>C</code>&quot; game action.
     *
     * <P>Constant value <code>11</code> is set to <code>GAME_C</code>.</P>
     */
    public static final int GAME_C = 11;
  
    /**
     * Constant for the general purpose &quot;<code>D</code>&quot; game action.
     *
     * <P>Constant value <code>12</code> is set to <code>GAME_D</code>.</P>
     */
    public static final int GAME_D = 12;
    
    /**
     * keyCode for ITU-T key <code>0</code>.
     *
     * <P>Constant value <code>48</code> is set to <code>KEY_NUM0</code>.</P>
     */
    public static final int KEY_NUM0 = 48;
    
    /**
     * keyCode for ITU-T key <code>1</code>.
     *
     * <P>Constant value <code>49</code> is set to <code>KEY_NUM1</code>.</P>
     */
    public static final int KEY_NUM1 = 49;
    
    /**
     * keyCode for ITU-T key <code>2</code>.
     *
     * <P>Constant value <code>50</code> is set to <code>KEY_NUM2</code>.</P>
     */
    public static final int KEY_NUM2 = 50;
    
    /**
     * keyCode for ITU-T key <code>3</code>.
     *
     * <P>Constant value <code>51</code> is set to <code>KEY_NUM3</code>.</P>
     */
    public static final int KEY_NUM3 = 51;
    
    /**
     * keyCode for ITU-T key <code>4</code>.
     *
     * <P>Constant value <code>52</code> is set to <code>KEY_NUM4</code>.</P>
     */
    public static final int KEY_NUM4 = 52;
    
    /**
     * keyCode for ITU-T key <code>5</code>.
     *
     * <P>Constant value <code>53</code> is set to <code>KEY_NUM5</code>.</P>
     */
    public static final int KEY_NUM5 = 53;
    
    /**
     * keyCode for ITU-T key <code>6</code>.
     *
     * <P>Constant value <code>54</code> is set to <code>KEY_NUM6</code>.</P>
     */
    public static final int KEY_NUM6 = 54;
    
    /**
     * keyCode for ITU-T key <code>7</code>.
     *
     * <P>Constant value <code>55</code> is set to <code>KEY_NUM7</code>.</P>
     */
    public static final int KEY_NUM7 = 55;
    
    /**
     * keyCode for ITU-T key <code>8</code>.
     *
     * <P>Constant value <code>56</code> is set to <code>KEY_NUM8</code>.</P>
     */
    public static final int KEY_NUM8 = 56;
    
    /**
     * keyCode for ITU-T key <code>9</code>.
     *
     * <P>Constant value <code>57</code> is set to <code>KEY_NUM09</code>.</P>
     */
    public static final int KEY_NUM9 = 57;
    
    /**
     * keyCode for ITU-T key &quot;star&quot; (<code>*</code>).
     *
     * <P>Constant value <code>42</code> is set to <code>KEY_STAR</code>.</P>
     */
    public static final int KEY_STAR = 42;
    
    /**
     * keyCode for ITU-T key &quot;pound&quot; (<code>#</code>).
     *
     * <P>Constant value <code>35</code> is set to <code>KEY_POUND</code>.</P>
     */
    public static final int KEY_POUND = 35;

    /**
     * Constructs a new <code>Canvas</code> object.
     */
    protected Canvas()  { 
        synchronized (Display.LCDUILock) {
            displayableLF = canvasLF = LFFactory.getFactory().getCanvasLF(this);
        }
    }
    
    /**
     * Checks if the <code>Canvas</code> is double buffered by the
     * implementation.
     * @return <code>true</code> if double buffered,
     * <code>false</code> otherwise
     */
    public boolean isDoubleBuffered() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return Constants.IS_DOUBLE_BUFFERED;
    }

    /**
     * Checks if the platform supports pointer press and release events.
     * @return <code>true</code> if the device supports pointer events
     */
    public boolean hasPointerEvents() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return Constants.POINTER_SUPPORTED;
    }
    
    /**
     * Checks if the platform supports pointer motion events (pointer dragged).
     * Applications may use this method to determine if the platform is capable
     * of supporting motion events.
     * @return <code>true</code> if the device supports pointer motion events
     */
    public boolean hasPointerMotionEvents() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return Constants.MOTION_SUPPORTED;
    }
    
    /**
     * Checks if the platform can generate repeat events when key
     * is kept down.
     * @return <code>true</code> if the device supports repeat events
     */
    public boolean hasRepeatEvents() { 
        // SYNC NOTE: return of atomic value, no locking necessary
        return Constants.REPEAT_SUPPORTED;
    }
    
    /**
     * Gets a key code that corresponds to the specified game action on the
     * device.  The implementation is required to provide a mapping for every
     * game action, so this method will always return a valid key code for
     * every game action.  See <a href="#gameactions">above</a> for further
     * discussion of game actions.  There may be multiple keys associated
     * with the same game action; however, this method will return only one of
     * them.  Applications should translate the key code of every key event
     * into a game action using {@link #getGameAction} and then interpret the 
     * resulting game action, instead of generating a table of key codes at 
     * using this method during initialization.
     * 
     * <P>The mapping between key codes and game actions
     * will not change during the execution of the application.</P>
     *
     * @param gameAction the game action
     * @return a key code corresponding to this game action
     * @throws IllegalArgumentException if <code>gameAction</code> 
     * is not a valid game action
     */
    public int getKeyCode(int gameAction) {
        // SYNC NOTE: no locking necessary as we are doing a static
        // table lookup and getKeyCode() is implemented natively
        int n = KeyConverter.getKeyCode(gameAction);

        if (n == 0) {
            throw new IllegalArgumentException();
        }

        return n;
    }
    
    /**
     * Gets an informative key string for a key. The string returned will
     * resemble the text physically printed on the key.  This string is 
     * suitable for displaying to the user.  For example, on a device
     * with function keys <code>F1</code> through <code>F4</code>,
     * calling this method on the <code>keyCode</code> for
     * the <code>F1</code> key will return the string
     * &quot;<code>F1</code>&quot;. A typical use for this string
     * will be to compose help text such as &quot;Press
     * <code>F1</code> to proceed.&quot;
     *
     * <p> This method will return a non-empty string for every valid key code.
     * </p>
     *
     * <p> There is no direct mapping from game actions to key names. To get
     * the string name for a game action <code>GAME_A</code>, the
     * application must call </p>
     *
     * <TABLE BORDER="2">
     * <TR>
     * <TD ROWSPAN="1" COLSPAN="1">
     *    <pre><code>
     *    getKeyName(getKeyCode(GAME_A));    </code></pre>
     * </TD>
     * </TR>
     * </TABLE>
     * @param keyCode the key code being requested
     * @return a string name for the key
     * @throws IllegalArgumentException if <code>keyCode</code> 
     * is not a valid key code
     */
    public String getKeyName(int keyCode) {
        // SYNC NOTE: no locking necessary as we are doing a static
        // table lookup and getKeyName() is implemented natively
        String s = KeyConverter.getKeyName(keyCode);

        if (s == null) {
            throw new IllegalArgumentException();
        }

        return s;
    }

    /**
     * Gets the game action associated with the given key code of the
     * device.  Returns zero if no game action is associated with this key
     * code.  See <a href="#gameactions">above</a> for further discussion of
     * game actions. 
     *
     * <P>The mapping between key codes and game actions
     * will not change during the execution of the application.</P>
     * 
     * @param keyCode the key code
     * @return the game action corresponding to this key, or 
     * <code>0</code> if none
     * @throws IllegalArgumentException if <code>keyCode</code> 
     * is not a valid key code
     */
    public int getGameAction(int keyCode) {
        // SYNC NOTE: no locking necessary as we are doing a static
        // table lookup and getGameAction() is implemented natively
        int n = KeyConverter.getGameAction(keyCode);

        if (n == -1) {
            throw new IllegalArgumentException();
        }

        return n;
    }

    /**
     * Controls whether the <code>Canvas</code> is in full-screen mode
     * or in normal mode.
     *
     * @param mode <code>true</code> if the <code>Canvas</code>
     * is to be in full screen mode, <code>false</code> otherwise
     *
     */
    public void setFullScreenMode(boolean mode) {

        // Do not do everything if already in the mode given
        if (mode == isInFullScreenMode) {
            return;
        }

        // Ask the Displayable to set fullscrn natively
        // To handle it better (and optimize correctly),
        // it should sent an event to the event queue.
        synchronized (Display.LCDUILock) {
            isInFullScreenMode = mode;
	}
	canvasLF.uSetFullScreenMode(mode);
    }

    /**
     * Called when a key is pressed.
     *
     * <P>The <code>getGameAction()</code> method can be called to
     * determine what game action, if any, is mapped to the key.
     * Class <code>Canvas</code> has an empty implementation of this method, and
     * the subclass has to redefine it if it wants to listen this method.
     * @param keyCode the key code of the key that was pressed
     */
    protected void keyPressed(int keyCode) {
    }

    /**
     * Called when a key is repeated (held down). 
     *
     * <P>The <code>getGameAction()</code> method can
     * be called to determine what game action,
     * if any, is mapped to the key.
     * Class <code>Canvas</code> has an empty implementation of this method, and
     * the subclass has to redefine it if it wants to listen this method.
     * </P>
     * @param keyCode the key code of the key that was repeated
     * @see #hasRepeatEvents()
     */
    protected void keyRepeated(int keyCode) {
    }

    /**
     * Called when a key is released.
     * <P>
     * The <code>getGameAction()</code> method can be called to
     * determine what game action, if any, is mapped to the key.
     * Class <code>Canvas</code> has an empty implementation of this method, and
     * the subclass has to redefine it if it wants to listen this method.
     * </P>
     * @param keyCode the key code of the key that was released
     */
    protected void keyReleased(int keyCode) {
    }
    
    /**
     * Called when the pointer is pressed. 
     *
     * <P>
     * The {@link #hasPointerEvents() hasPointerEvents()}
     * method may be called to determine if the device supports pointer events.
     * Class <code>Canvas</code> has an empty implementation of this method, and
     * the subclass has to redefine it if it wants to listen this method.
     * </P>
     * @param x the horizontal location where the pointer was pressed (relative
     * to the <code>Canvas</code>)
     * @param y the vertical location where the pointer was pressed
     * (relative to the <code>Canvas</code>)
     */
    protected void pointerPressed(int x, int y) {
    }
    
    /**
     * Called when the pointer is released. 
     *
     * <P>
     * The {@link #hasPointerEvents() hasPointerEvents()}
     * method may be called to determine if the device supports pointer events.
     * Class <code>Canvas</code> has an empty implementation of this method, and
     * the subclass has to redefine it if it wants to listen this method.
     * </P>
     * @param x the horizontal location where the pointer was released
     * (relative to the <code>Canvas</code>)
     * @param y the vertical location where the pointer was released
     * (relative to the <code>Canvas</code>)
     */
    protected void pointerReleased(int x, int y) {
    }
    
    /**
     * Called when the pointer is dragged.
     *
     * <P>
     * The {@link #hasPointerMotionEvents() hasPointerMotionEvents()}
     * method may be called to determine if the device supports pointer events.
     * Class <code>Canvas</code> has an empty implementation of this method, and
     * the subclass has to redefine it if it wants to listen this method.
     * </P>
     * @param x the horizontal location where the pointer was dragged
     * (relative to the <code>Canvas</code>)
     * @param y the vertical location where the pointer was dragged
     * (relative to the <code>Canvas</code>)
     */
    protected void pointerDragged(int x, int y) {
    }
    
    /**
     * Requests a repaint for the specified region of the
     * <code>Canvas</code>. Calling
     * this method may result in subsequent call to
     * <code>paint()</code>, where the passed
     * <code>Graphics</code> object's clip region will include at
     * least the specified
     * region.
     *
     * <p> If the canvas is not visible, or if width and height are zero or 
     * less, or if the rectangle does not specify a visible region of 
     * the display, this call has no effect. </p>
     *
     * <p> The call to <code>paint()</code> occurs asynchronously of
     * the call to <code>repaint()</code>.
     * That is, <code>repaint()</code> will not block waiting for
     * <code>paint()</code> to finish. The
     * <code>paint()</code> method will either be called after the
     * caller of <code>repaint(</code>)
     * returns
     * to the implementation (if the caller is a callback) or on another thread
     * entirely. </p>
     *
     * <p> To synchronize with its <code>paint()</code> routine,
     * applications can use either
     * {@link Display#callSerially(Runnable) Display.callSerially()} or
     * {@link #serviceRepaints() serviceRepaints()}, or they can code explicit
     * synchronization into their <code>paint()</code> routine. </p>
     *
     * <p> The origin of the coordinate system is above and to the left of the
     * pixel in the upper left corner of the displayable area of the
     * <code>Canvas</code>.
     * The X-coordinate is positive right and the Y-coordinate is 
     * positive downwards.
     * </p>
     *
     * @param x the x coordinate of the rectangle to be repainted
     * @param y the y coordinate of the rectangle to be repainted
     * @param width the width of the rectangle to be repainted
     * @param height the height of the rectangle to be repainted
     *
     * @see Display#callSerially(Runnable)
     * @see #serviceRepaints()
     */
    public final void repaint(int x, int y, int width, int height) {
        synchronized (Display.LCDUILock) {
            if (width > 0 && height > 0) {
                canvasLF.lRepaint(x, y, width, height, null);
            }
        }
    }

    /**
     * Requests a repaint for the entire <code>Canvas</code>. The
     * effect is identical to
     * <p> <code> repaint(0, 0, getWidth(), getHeight()); </code>
     */
    public final void repaint() {
        synchronized (Display.LCDUILock) {
            canvasLF.lRepaint();
        }
    }
    
    /**
     * Forces any pending repaint requests to be serviced immediately. This
     * method blocks until the pending requests have been serviced. If 
     * there are
     * no pending repaints, or if this canvas is not visible on the display,
     * this call does nothing and returns immediately. 
     *
     * <p><strong>Warning:</strong> This method blocks until the call to the
     * application's <code>paint()</code> method returns. The
     * application has no
     * control over
     * which thread calls <code>paint()</code>; it may vary from
     * implementation to
     * implementation. If the caller of <code>serviceRepaints()</code>
     * holds a lock that the
     * <code>paint()</code> method acquires, this may result in
     * deadlock. Therefore, callers
     * of <code>serviceRepaints()</code> <em>must not</em> hold any
     * locks that might be
     * acquired within the <code>paint()</code> method. The
     * {@link Display#callSerially(Runnable) Display.callSerially()}
     * method provides a facility where an application can be called back after
     * painting has completed, avoiding the danger of deadlock.
     * </p>
     *
     * @see Display#callSerially(Runnable)
     */
    public final void serviceRepaints() {
        // SYNC NOTE: unlike most public API methods, no locking is done
        // here.  This is necessary because Display.serviceRepaints()
        // needs to handle its own locking.
        canvasLF.uServiceRepaints();
    }

    /**
     * The implementation calls <code>showNotify()</code>
     * immediately prior to this <code>Canvas</code> being made
     * visible on the display.
     * Canvas subclasses may override
     * this method to perform tasks before being shown, such
     * as setting up animations, starting timers, etc.
     * The default implementation of this method in class
     * <code>Canvas</code> is empty.
     */
    protected void showNotify() {
    }
    
    /**
     * The implementation calls <code>hideNotify()</code> shortly
     * after the <code>Canvas</code> has been
     * removed from the display.
     * <code>Canvas</code> subclasses may override this method in
     * order to pause
     * animations, 
     * revoke timers, etc.  The default implementation of this 
     * method in class <code>Canvas</code> is empty.
     */
    protected void hideNotify()  {
    }

    /**
     * Renders the <code>Canvas</code>. The application must implement
     * this method in
     * order to paint any graphics.
     *
     * <p>The <code>Graphics</code> object's clip region defines the
     * area of the screen
     * that is considered to be invalid. A correctly-written
     * <code>paint()</code> routine
     * must paint <em>every</em> pixel within this region. This is necessary
     * because the implementation is not required to clear the region prior to
     * calling <code>paint()</code> on it.  Thus, failing to paint
     * every pixel may result
     * in a portion of the previous screen image remaining visible. </p>
     *
     * <p>Applications <em>must not</em> assume that
     * they know the underlying source of the <code>paint()</code>
     * call and use this
     * assumption
     * to paint only a subset of the pixels within the clip region. The 
     * reason is
     * that this particular <code>paint()</code> call may have
     * resulted from multiple
     * <code>repaint()</code>
     * requests, some of which may have been generated from outside the
     * application. An application that paints only what it thinks is 
     * necessary to
     * be painted may display incorrectly if the screen contents had been
     * invalidated by, for example, an incoming telephone call. </p>
     *
     * <p>Operations on this graphics object after the <code>paint()
     * </code>call returns are
     * undefined. Thus, the application <em>must not</em> cache this
     * <code>Graphics</code>
     * object for later use or use by another thread. It must only be 
     * used within
     * the scope of this method. </p>
     *
     * <p>The implementation may postpone visible effects of
     * graphics operations until the end of the paint method.</p>
     *
     * <p> The contents of the <code>Canvas</code> are never saved if
     * it is hidden and then
     * is made visible again. Thus, shortly after
     * <code>showNotify()</code> is called,
     * <code>paint()</code> will always be called with a
     * <code>Graphics</code> object whose clip region
     * specifies the entire displayable area of the
     * <code>Canvas</code>.  Applications
     * <em>must not</em> rely on any contents being preserved from a previous
     * occasion when the <code>Canvas</code> was current. This call to
     * <code>paint()</code> will not
     * necessarily occur before any other key or pointer
     * methods are called on the <code>Canvas</code>.  Applications
     * whose repaint
     * recomputation is expensive may create an offscreen
     * <code>Image</code>, paint into it,
     * and then draw this image on the <code>Canvas</code> when
     * <code>paint()</code> is called. </p>
     *
     * <P>The application code must never call <code>paint()</code>;
     * it is called only by
     * the implementation.</P>
     *
     * <P>The <code>Graphics</code> object passed to the
     * <code>paint()</code> method has the following
     * properties:</P>
     * <UL>
     * <LI>the destination is the actual display, or if double buffering is in 
     * effect, a back buffer for the display;</LI>
     * <LI>the clip region includes at least one pixel
     * within this <code>Canvas</code>;</LI>
     * <LI>the current color is black;</LI>
     * <LI>the font is the same as the font returned by
     * {@link Font#getDefaultFont() Font.getDefaultFont()};</LI>
     * <LI>the stroke style is {@link Graphics#SOLID SOLID};</LI>
     * <LI>the origin of the coordinate system is located at the upper-left
     * corner of the <code>Canvas</code>; and</LI>
     * <LI>the <code>Canvas</code> is visible, that is, a call to
     * <code>isShown()</code> will return
     * <code>true</code>.</LI>
     * </UL>
     *
     * @param g the <code>Graphics</code> object to be used for
     * rendering the <code>Canvas</code>
     */
    protected abstract void paint(Graphics g);

    /**
     * Called when the drawable area of the <code>Canvas</code> has
     * been changed.  This
     * method has augmented semantics compared to {@link
     * Displayable#sizeChanged(int, int) Displayable.sizeChanged}.
     * 
     * <p>In addition to the causes listed in
     * <code>Displayable.sizeChanged</code>, a size change can occur on a
     * <code>Canvas</code> because of a change between normal and
     * full-screen modes.</p>
     *
     * <p>If the size of a <code>Canvas</code> changes while it is
     * actually visible on the
     * display, it may trigger an automatic repaint request.  If this occurs,
     * the call to <code>sizeChanged</code> will occur prior to the call to
     * <code>paint</code>.  If the <code>Canvas</code> has become smaller, the
     * implementation may choose not to trigger a repaint request if the
     * remaining contents of the <code>Canvas</code> have been
     * preserved.  Similarly, if
     * the <code>Canvas</code> has become larger, the implementation
     * may choose to trigger
     * a repaint only for the new region.  In both cases, the preserved
     * contents must remain stationary with respect to the origin of the
     * <code>Canvas</code>.  If the size change is significant to the
     * contents of the
     * <code>Canvas</code>, the application must explicitly issue a
     * repaint request for the
     * changed areas.  Note that the application's repaint request should not
     * cause multiple repaints, since it can be coalesced with repaint
     * requests that are already pending.</p>
     * 
     * <p>If the size of a <code>Canvas</code> changes while it is not
     * visible, the
     * implementation may choose to delay calls to <code>sizeChanged</code>
     * until immediately prior to the call to <code>showNotify</code>.  In
     * that case, there will be only one call to <code>sizeChanged</code>,
     * regardless of the number of size changes.</p>
     * 
     * <p>An application that is sensitive to size changes can update instance
     * variables in its implementation of <code>sizeChanged</code>.  These
     * updated values will be available to the code in the
     * <code>showNotify</code>, <code>hideNotify</code>, and
     * <code>paint</code> methods.</p>
     * 
     * @param w the new width in pixels of the drawable area of the
     * <code>Canvas</code>
     * @param h the new height in pixels of the drawable area of
     * the <code>Canvas</code>
     */ 
    protected void sizeChanged(int w, int h) {
        // this method is intended to be overridden by the application
    }


    /**
     * Used by GameCanvas to suppress Game action key events
     */
    boolean suppressKeyEvents; // = false

    /** The Canvas look&feel object associated with this Canvas */
    CanvasLF canvasLF;

    /** Get current display in which this Canvas displays. */
    public Display getCurrentDisplay() {
        if (canvasLF == null) {
            return null;
        }

        return canvasLF.lGetCurrentDisplay();
    }
}
