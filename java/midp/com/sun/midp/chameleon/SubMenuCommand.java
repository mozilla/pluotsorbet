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

package com.sun.midp.chameleon;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;

/**
 * The SubMenuCommand is an extension of LCDUI's Command object.
 * It represents a collection of Commands which should be made
 * available as a type of "sub menu" under a single Command heading.
 */
public class SubMenuCommand extends Command {

    /** An array holding the Commands that are part of the "submenu". */
    protected Command[] subCommands;
    
    /** The CommandListener to notify when a sub command is selected. */
    protected CommandListener listener;
    
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
     * @param priority the command's priority value
     *
     * @throws NullPointerException if label is <code>null</code>
     * @throws IllegalArgumentException if the <code>commandType</code>
     * is an invalid type
     *
     * @see #Command(String, String, int, int)
     */
    public SubMenuCommand(String label, int commandType, int priority) {
        super(label, commandType, priority);
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
     * @param priority the command's priority value
     * 
     * @throws NullPointerException if <code>shortLabel</code> is
     * <code>null</code>
     * @throws IllegalArgumentException if the <code>commandType</code> is an
     * invalid type
     */
    public SubMenuCommand(String shortLabel, String longLabel, 
                          int commandType, int priority)
    {
        super(shortLabel, longLabel, commandType, priority);
    }
    
    /**
     * Removes all commands that are currently in the list of sub commands.
     */
    public synchronized void removeAll() {
        subCommands = null;
    }
    
    /**
     * Adds the given Command to the list of sub commands.
     *
     * @param cmd The Command to add to the current list of submenu commands
     *
     * @throws IllegalArgumentException if the given Command is null
     */
    public synchronized void addSubCommand(Command cmd) {
        if (cmd == null) {
            throw new IllegalArgumentException("Added command was null");
        }
        
        if (subCommands == null) {
            subCommands = new Command[] { cmd };
        } else {
            Command[] newCommands = new Command[subCommands.length + 1];
            System.arraycopy(subCommands, 0, newCommands, 0, 
                             subCommands.length);
            newCommands[subCommands.length] = cmd;
            subCommands = newCommands;
            newCommands = null;
        }
    }
    
    /**
     * Adds the given set of Commands to the list of sub commands.
     *
     * @param cmds An array of Commands to add to the current list of 
     *        submenu commands
     *
     * @throws IllegalArgumentException if the given array is null
     */
    public synchronized void addSubCommands(Command[] cmds) {
        if (cmds == null) {
            throw new IllegalArgumentException("Added command array was null");
        }
        
        if (subCommands == null) {
            subCommands = new Command[cmds.length];
            System.arraycopy(cmds, 0, subCommands, 0, cmds.length);
        } else {
            Command[] newCommands = 
                new Command[subCommands.length + cmds.length];
            System.arraycopy(subCommands, 0, newCommands, 0, 
                             subCommands.length);
            System.arraycopy(cmds, 0, newCommands, subCommands.length,
                             cmds.length);
            subCommands = newCommands;
            newCommands = null;
        }
    }
    
    /**
     * Retrieves the set of subcommands from this SubMenuCommand.
     * This method will return a copy of the internal array holding
     * the set of sub menu Commands.
     * 
     * @return an array holding the set of Commands to be shown
     *         on the "submenu" for this SubMenuCommand. 'null'
     *         means this SubMenuCommand has no sub commands.
     */
    public synchronized Command[] getSubCommands() {
	if (subCommands == null) {
	    return null;
	}

        Command[] cmdCopy = new Command[subCommands.length];
        System.arraycopy(subCommands, 0, cmdCopy, 0, subCommands.length);
        return cmdCopy;
    }
    
    /**
     * Sets the CommandListener of this SubMenuCommand to notify when one
     * of the sub commands is selected.
     *
     * @param listener the CommandListener to notify when a sub command is
     *        selected
     */
    public void setListener(CommandListener listener) {
        this.listener = listener;
    }
    
    /**
     * Called to notify the CommandListener of this SubMenuCommand that
     * a sub command has been selected.
     *
     * @param c the Command that was selected
     */
    public synchronized void notifyListener(Command c) {
        if (listener != null) {
            listener.commandAction(c, null);
        }
    }
}
