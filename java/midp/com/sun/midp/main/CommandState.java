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

package com.sun.midp.main;

/** Holds the state for MIDlet Suite management commands. */
class CommandState {
    /** return value from Main, so we know that Main exited normally */
    static final int MAIN_EXIT = 2001;

    /** status for shutdown */
    static final int SHUTDOWN = 1;

    /** status for success */
    static final int OK = 0;

    /**
     * Get the command state.
     *
     * @return current command state
     */
    static CommandState getCommandState() {
        CommandState state = new CommandState();

        restoreCommandState(state);
        return state;
    }

    /**
     * Save the command state.
     *
     * @param state current command state
     */
    private static native void saveCommandState(CommandState state);

    /**
     * Restore the command state.
     *
     * @param state current command state
     */
    private static native void restoreCommandState(CommandState state);

    /**
     * Exit the VM with an error code. Our private version of Runtime.exit.
     * <p>
     * This is needed because the MIDP version of Runtime.exit cannot tell
     * if it is being called from a MIDlet or not, so it always throws an
     * exception.
     * <p>
     *
     * @param status Status code to return.
     */
    static native void exitInternal(int status);

    /** Status of the last command. */
    int status;
    /** The ID given to a suite load. */
    int suiteId;
    /** Class name of MIDlet. */
    String midletClassName;
    /** Has the application manager MIDlet displayed the Java logo yet? */
    boolean logoDisplayed;
    /** The ID of suite to load when there is no other queued. */
    int lastSuiteId;
    /** The MIDlet class name for the suite to load. */
    String lastMidletClassName;
    /** The argument for a last MIDlet, will be app property arg-0. */
    String lastArg0;
    /** The argument for a last MIDlet, will be app property arg-1. */
    String lastArg1;
    /** The argument for a MIDlet in the suite, will be app property arg-0. */
    String arg0;
    /** The argument for a MIDlet in the suite, will be app property arg-1. */
    String arg1;
    /** The argument for a MIDlet in the suite, will be app property arg-2. */
    String arg2;
    /**
     * true if the new midlet must be started in debug
     * mode, false otherwise.
     */
    boolean isDebugMode;
    /** Structure containing the run time information about the midlet. */
    RuntimeInfo runtimeInfo = new RuntimeInfo();

    /** Only the factory method can instantiate this class */
    private CommandState() {
    }

    /**
     * Save the command state.
     */
    void save() {
        saveCommandState(this);
    }

    /**
     * Returns the string form of this object.
     *
     * @return displayable string representation of this object
     */
    public String toString() {
        return "CommandState:" +
            "\n  status: " + status +
            "\n  suite ID: " + suiteId +
            "\n  class name: " + midletClassName +
            "\n  logo displayed: " + logoDisplayed +
            "\n  last suite ID: " + lastSuiteId +
            "\n  last MIDlet class name: " + lastMidletClassName +
            "\n  arg 0: " + arg0 +
            "\n  arg 1: " + arg1 +
            "\n  arg 2: " + arg2 +
            "\n  memory reserved: " + runtimeInfo.memoryReserved +
            "\n  memory total: " + runtimeInfo.memoryTotal +
            "\n  priority:" + runtimeInfo.priority +
            "\n  profile name: " + runtimeInfo.profileName +
            "\n  debug mode:" + isDebugMode;
    }
}
