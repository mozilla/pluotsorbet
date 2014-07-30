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

import com.sun.midp.lcdui.CommandAccess;

/**
 * The <code>Command</code> class is a construct that encapsulates
 * the semantic information of an action. The behavior that the command
 * activates is not encapsulated in this object. This means that command 
 * contains
 * only information about &quot;command&quot; not the actual action
 * that happens when
 * command
 * is activated. The action is defined in a 
 * {@link CommandListener CommandListener}
 * associated
 * with the <code>Displayable</code>. <code>Command</code> objects are
 * <em>presented</em>
 * in the user interface and the way they are presented
 * may depend on the semantic information contained within the command.
 *
 * <P><code>Commands</code> may be implemented in any user interface
 * construct that has
 * semantics for activating a single action. This, for example, can be a soft
 * button, item in a menu, or some other direct user interface construct.
 * For example, a
 * speech interface may present these commands as voice tags. </P>
 *
 * <P>The mapping to concrete user interface constructs may also depend on the
 * total number of the commands.
 * For example, if an application asks for more abstract commands than can
 * be mapped onto
 * the available physical buttons on a device, then the device may use an
 * alternate human interface such as a menu. For example, the abstract 
 * commands that
 * cannot be mapped onto physical buttons are placed in a menu and the label
 * &quot;Menu&quot; is mapped onto one of the programmable buttons. </P>
 *
 * <p>A command contains four pieces of information: a <em>short label</em>,
 * an optional <em>long label</em>, a
 * <em>type</em>, and a <em>priority</em>.
 * One of the labels is used for the visual
 * representation of the command, whereas the type and the priority indicate
 * the semantics of the command. </p>
 *
 * <a name="label"></a>
 * <h3>Labels</h3>
 *
 * <p> Each command includes one or two label strings.  The label strings are
 * what the application requests to be shown to the user to represent this
 * command. For example, one of these strings may appear next to a soft button
 * on the device or as an element in a menu. For command types other than
 * <code>SCREEN</code>, the labels provided may be overridden by a
 * system-specific label
 * that is more appropriate for this command on this device. The contents of
 * the label strings are otherwise not interpreted by the implementation. </p>
 * 
 * <p>All commands have a short label.  The long label is optional.  If the
 * long label is not present on a command, the short label is always used.
 * </p>
 *
 * <p>The short label string should be as short as possible so that it
 * consumes a minimum of screen real estate.  The long label can be longer and
 * more descriptive, but it should be no longer than a few words.  For
 * example, a command's short label might be &quot;Play&quot;, and its
 * long label
 * might be &quot;Play Sound Clip&quot;.</p>
 *
 * <p>The implementation chooses one of the labels to be presented in the user
 * interface based on the context and the amount of space available.  For
 * example, the implementation might use the short label if the command
 * appears on a soft button, and it might use the long label if the command
 * appears on a menu, but only if there is room on the menu for the long
 * label.  The implementation may use the short labels of some commands and
 * the long labels of other commands, and it is allowed to switch between
 * using the short and long label at will.  The application cannot determine
 * which label is being used at any given time.  </p>
 *
 * <a name="type"></a>
 * <h3>Type</h3>
 *
 * <p> The application uses the command
 * type to specify the intent of this command. For example, if the
 * application specifies that the command is of type
 * <code>BACK</code>, and if the device
 * has a standard of placing the &quot;back&quot; operation on a
 * certain soft-button,
 * the implementation can follow the style of the device by using the semantic
 * information as a guide. The defined types are
 * {@link #BACK BACK},
 * {@link #CANCEL CANCEL},
 * {@link #EXIT EXIT},
 * {@link #HELP HELP},
 * {@link #ITEM ITEM},
 * {@link #OK OK},
 * {@link #SCREEN SCREEN},
 * and
 * {@link #STOP STOP}. </p>
 *
 * <a name="priority"></a>
 * <h3>Priority</h3>
 *
 * <p> The application uses the priority
 * value to describe the importance of this command relative to other commands
 * on the same screen. Priority values are integers, where a lower number
 * indicates greater importance. The actual values are chosen by the
 * application. A priority value of one might indicate the most important
 * command, priority values of two, three, four, and so on indicate commands
 * of lesser importance. </p>
 *
 * <p>Typically,
 * the implementation first chooses the placement of a command based on
 * the type of command and then places similar commands based on a priority
 * order. This could mean that the command with the highest priority is
 * placed so that user can trigger it directly and that commands with lower
 * priority are placed on a menu. It is not an error for there to be commands
 * on the same screen with the same priorities and types. If this occurs, the
 * implementation will choose the order in which they are presented. </p>
 *
 * <p>For example, if the application has the following set of commands: </P>
 * <TABLE BORDER="2">
 * <TR>
 * <TD ROWSPAN="1" COLSPAN="1">
 *    <pre><code>
 *    new Command("Buy", Command.ITEM, 1);
 *    new Command("Info", Command.ITEM, 1);
 *    new Command("Back", Command.BACK, 1);    </code></pre>
 * </TD>
 * </TR>
 * </TABLE>
 * <P>
 * An implementation with two soft buttons may map the
 * <code>BACK</code> command to
 * the right
 * soft button and create an &quot;Options&quot; menu on the left soft
 * button to contain
 * the other commands.<BR>
 * <IMG SRC="doc-files/command1.gif" width=190 height=268><BR>
 * When user presses the left soft button, a menu with the two remaining
 * <code>Commands</code> appears:<BR>
 * <IMG SRC="doc-files/command2.gif" width=189 height=260><BR>
 * If the application had three soft buttons, all commands can be mapped 
 * to soft buttons:
 * <BR><IMG SRC="doc-files/command3.gif" width=189 height=261></P>
 *
 * <p>The application is always responsible for providing the means for the
 * user to progress through different screens. An application may set up a
 * screen that has no commands. This is allowed by the API but is generally
 * not useful; if this occurs the user would have no means to move to another
 * screen. Such program would simply considered to be in error. A typical
 * device should provide a means for the user to direct the application manager
 * to kill the erroneous application.
 * @since MIDP 1.0
 */

public class Command {

    // public members //

    /**
     * Specifies an application-defined command that pertains to the current
     * screen. Examples could be &quot;Load&quot; and
     * &quot;Save&quot;.  A <code>SCREEN</code> command
     * generally applies to the entire screen's contents or to navigation
     * among screens.  This is in contrast to the <CODE>ITEM</CODE> type,
     * which applies to the currently activated or focused item or element
     * contained within this screen.
     *
     * <P>Value <code>1</code> is assigned to <code>SCREEN</code>.</P>
     */
    public static final int SCREEN = 1;
    
    /**
     * A navigation command that returns the user to the logically 
     * previous screen.
     * The jump to the previous screen is not done automatically by the 
     * implementation
     * but by the {@link CommandListener#commandAction commandAction} 
     * provided by
     * the application.
     * Note that the application defines the actual action since the strictly
     * previous screen may not be logically correct.
     *
     * <P>Value <code>2</code> is assigned to <code>BACK</code>.</P>
     *
     * @see #CANCEL
     * @see #STOP
     */
    public static final int BACK = 2;
    
    /**
     * A command that is a standard negative answer to a dialog implemented by
     * current screen.
     * Nothing is cancelled automatically by the implementation; cancellation
     * is implemented
     * by the {@link CommandListener#commandAction commandAction} provided by
     * the application.
     *
     * <p> With this command type, the application hints to the implementation
     * that the user wants to dismiss the current screen without taking any 
     * action
     * on anything that has been entered into the current screen, and usually
     * that
     * the user wants to return to the prior screen. In many cases
     * <code>CANCEL</code> is
     * interchangeable with <code>BACK</code>, but <code>BACK</code>
     * is mainly used for navigation
     * as in a browser-oriented applications. </p>
     *
     * <P>Value <code>3</code> is assigned to <code>CANCEL</code>.</P>
     *
     * @see #BACK
     * @see #STOP
     */
    public static final int CANCEL = 3;
    
    /**
     * A command that is a standard positive answer to a dialog implemented by
     * current screen.
     * Nothing is done automatically by the implementation; any action taken
     * is implemented
     * by the {@link CommandListener#commandAction commandAction} provided by
     * the application.
     *
     * <p> With this command type the application hints to the 
     * implementation that
     * the user will use this command to ask the application to confirm 
     * the data
     * that has been entered in the current screen and to proceed to the next
     * logical screen. </p>
     *
     * <P><code>CANCEL</code> is often used together with <code>OK</code>.</P>
     *
     * <P>Value <code>4</code> is assigned to <code>OK</code>.</P>
     *
     * @see #CANCEL
     */
    public static final int OK = 4;
    
    /**
     * This command specifies a request for on-line help.
     * No help information is shown automatically by the implementation.
     * The
     * {@link CommandListener#commandAction commandAction} provided by the
     * application is responsible for showing the help information. 
     *
     * <P>Value <code>5</code> is assigned to <code>HELP</code>.</P>
     */
    public static final int HELP = 5;
    
    /**
     * A command that will stop some currently running
     * process, operation, etc.
     * Nothing is stopped automatically by the implementation.
     * The cessation must
     * be performed
     * by the {@link CommandListener#commandAction commandAction} provided by
     * the application.
     *
     * <p> With this command type the application hints to the 
     * implementation that
     * the user will use this command to stop any currently running process
     * visible to the user on the current screen. Examples of running processes
     * might include downloading or sending of data. Use of the
     * <code>STOP</code>
     * command does
     * not necessarily imply a switch to another screen. </p>
     *
     * <P>Value <code>6</code> is assigned to <code>STOP</code>.</P>
     *
     * @see #BACK
     * @see #CANCEL
     */
    public static final int STOP = 6;
    
    /**
     * A command used for exiting from the application.  When the user
     * invokes this command, the implementation does not exit automatically.
     * The application's 
     * {@link CommandListener#commandAction commandAction}
     * will be called, and it should exit the application if it
     * is appropriate to do so.
     *
     * <P>Value <code>7</code> is assigned to <code>EXIT</code>.</P>
     */
    public static final int EXIT = 7;

    /**
     * With this command type the application can hint to the
     * implementation that the command is specific to the items of
     * the <code>Screen</code> or the elements of a
     * <code>Choice</code>. Normally this
     * means that command relates to the focused item or element.
     * For example, an implementation of <code>List</code> can use
     * this information for
     * creating context sensitive menus.
     * 
     * <P>Value <code>8</code> is assigned to <code>ITEM</code>.</P>
     */
    public static final int ITEM = 8;

    // protected members //

    /**
     * A command used for executing Virtual Keyboard
     */
    static final int VIRTUAL = 9;

    /**
     * The label rendered on the screen for this Command.
     * Chosen from the available set of labels.
     */
    String      shortLabel;
    /**
     * The long Label for this Command
     */
    String      longLabel;
    /**
     * The type of this Command
     */
    int         commandType;
    /**
     * The priority of this Command
     */
    int         priority;

    // private members //

    /**
     * This is a private id that is set when adding a command to 
     * a Displayable,
     * such that the Command may be easily identified at a later
     * date only by its id
     */
    private int     id;

    // Constructors //

    /**
     * Creates a new command object with the given short
     * 
     * <a href="#label">label</a>,
     * <a href="#type">type</a>, and
     * <a href="#priority">priority</a>.
     *
     * The newly created command has no long label.  This constructor is
     * identical to <code>Command(label, null, commandType, priority)</code>.
     *
     * @param label the command's short label
     * @param commandType the command's type
     * @param inp_priority the command's priority value
     *
     * @throws NullPointerException if label is <code>null</code>
     * @throws IllegalArgumentException if the <code>commandType</code>
     * is an invalid type
     *
     * @see #Command(String, String, int, int)
     */
    public Command(String label, int commandType, int inp_priority) {
	this(label, null, commandType, inp_priority);
    }

    /**
     * Creates a new command object with the given
     * <a href="#label">labels</a>,
     * <a href="#type">type</a>, and
     * <a href="#priority">priority</a>.
     *
     * <p>The short label is required and must not be
     * <code>null</code>.  The long label is
     * optional and may be <code>null</code> if the command is to have
     * no long label.</p>
     * 
     * @param shortLabel the command's short label
     * @param longLabel the command's long label, or <code>null</code> if none
     * @param commandType the command's type
     * @param inp_priority the command's priority value
     * 
     * @throws NullPointerException if <code>shortLabel</code> is
     * <code>null</code>
     * @throws IllegalArgumentException if the <code>commandType</code> is an
     * invalid type
     * 
     */
    public Command(String shortLabel, String longLabel, int commandType,
		   int inp_priority) {
        initialize(commandType, inp_priority);
        setLabel(shortLabel, longLabel);
    }

    // public method implementations //

    /**
     * Gets the short label of the command.
     * 
     * @return the <code>Command's</code> short label
     */
    public String getLabel() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return shortLabel;
    }

    /**
     * Gets the long label of the command.
     * 
     * @return the <code>Command's</code> long label, or
     * <code>null</code> if the <code>Command</code> has no long
     * label
     * 
     */
    public String getLongLabel() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return longLabel;
    }

    /**
     * Gets the type of the command.
     *
     * @return type of the <code>Command</code>
     */
    public int getCommandType() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return commandType;
    }
    
    /**
     * Gets the priority of the command.
     *
     * @return priority of the <code>Command</code>
     */
    public int getPriority() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return priority;
    }

    // protected method implementations //

    /**
     * Get the internal ID of this Command
     *
     * @return int  The integer id associated with this specific Command
     */
    int getID() {
        return id;
    }

    // package private method implementations //

    /**
     * Sets the internal id used to uniquely identify
     * this command.
     * This method is intended to be called from Displayable
     * addCommand()
     *
     * @param num the int to set this id to
     */
    void setInternalID(int num) {
	this.id = num;
    }

    // private method implementations //

    /**
     * Sets the label of the command. If the label is null
     * throw NullPointerException.
     * @param shortLabel the short Label string
     * @param longLabel the long Label string
     */
    private void setLabel(String shortLabel, String longLabel) {

        if (shortLabel == null) {
	    throw new NullPointerException();
        }
        this.shortLabel = shortLabel;
        this.longLabel = longLabel;
    }

    /**
     * This method will initialize this Command object is only called
     * by its constructors.
     *
     * @param commandType   The command's <a href="#type">type</a>, one of
     *                      {@link #BACK BACK},
     *                      {@link #CANCEL CANCEL},
     *                      {@link #EXIT EXIT},
     *                      {@link #HELP HELP},
     *                      {@link #ITEM ITEM},
     *                      {@link #OK OK},
     *                      {@link #SCREEN SCREEN},
     *                      or
     *                      {@link #STOP STOP}
     *
     * @param inp_priority      The command's <a href="#priority">priority</a>
     *                      value
     *
     * @throws IllegalArgumentException if the commandType is an invalid
     *                                  type
     * @throws NullPointerException     if label is null, or if any one of
     *                                  the array of labels is null
     */
    private final void initialize(int commandType, int inp_priority) {

        if ((commandType < SCREEN) || (commandType > ITEM)) {
            throw new IllegalArgumentException();
        }

        this.commandType = commandType;
        priority    = inp_priority;
    }

} // class Command

