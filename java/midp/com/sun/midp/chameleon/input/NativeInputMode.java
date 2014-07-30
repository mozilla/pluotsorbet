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
package com.sun.midp.chameleon.input;

import javax.microedition.lcdui.Displayable;

/**
 */
public class NativeInputMode implements InputMode {

    /** this index selects in the function state data array
     *  the integer result returned from an interface function */
    private final int STATE_CALLBACK_RES = 0;
    /** this index selects in the function state data array
     *  the integer id of interface function to be called */
    private final int STATE_FUNC_TOKEN = 1;
    /** this index selects in the function state data array
     *  the integer id of the next state to be entered */
    private final int STATE_NEXT_STATE = 2;
    /** this index selects in the function state data array
     *  the integer argument for an interface function */
    private final int STATE_INT_ARG = 3;
    /** this index selects in the function state data array
     *  the value to be returned as a result of the enclosing Java function */
    private final int STATE_FINAL_RES = 4;
    /** this index selects in the function state data array
     *  the value to be internally used by native function */
    private final int STATE_INTERNAL = 5;
    /** this index selects in the function state data array
     *  the value to be internally used by native function */
    private final int STATE_INTERNAL_EXT = 6;
    /** the number of integer elements in the function state data array */
    private final int STATE_DATA_ARRAY_SIZE = 7;

    /** this value, when stored as stateArgs[STATE_FUNC_TOKEN],
     *  tells the executeMediatorCommand function
     * to perform no action with the mediator */
    private final int MEDIATOR_NOOP = 0;
    /** this value, when stored as stateArgs[STATE_FUNC_TOKEN],
     *  tells the executeMediatorCommand function
     * to perform the mediator.commit(String) function */
    private final int MEDIATOR_COMMIT = 1;
    /** this value, when stored as stateArgs[STATE_FUNC_TOKEN],
     *  tells the executeMediatorCommand function
     * to perform the mediator.clear(int) function */
    private final int MEDIATOR_CLEAR = 2;
    /** this value, when stored as stateArgs[STATE_FUNC_TOKEN],
     *  tells the executeMediatorCommand function
     * to perform the mediator.subInputModeChanged() function */
    private final int MEDIATOR_SUBINPUTMODECHANGED = 3;
    /** this value, when stored as stateArgs[STATE_FUNC_TOKEN],
     *  tells the executeMediatorCommand function
     * to perform the mediator.inputModeCompleted() function */
    private final int MEDIATOR_INPUTMODECOMPLETED = 4;
    /** this value, when stored as stateArgs[STATE_FUNC_TOKEN],
     *  tells the executeMediatorCommand function
     * to perform the mediator.isClearKey(int) function */
    private final int MEDIATOR_ISCLEARKEY = 5;
    /** this value, when stored as stateArgs[STATE_FUNC_TOKEN],
     *  tells the executeMediatorCommand function
     * to perform the mediator.getAvailableSize() function */
    private final int MEDIATOR_GETAVAILABLESIZE = 6;
    /** this value, when stored as stateArgs[STATE_FUNC_TOKEN],
     *  tells the executeMediatorCommand function
     * to perform the mediator.isNewlineKey(int) function */
    private final static int MEDIATOR_ISNEWLINEKEY = 7;

    /** constructor; the real initialization is done
     * in the initialize(int) function */
    public NativeInputMode() {
    }

    /**
     * Initialize the instance.
     *
     * @param theId the value to be stored as id
     * @return error code, 0 if ok
     */
    public native int initialize(int theId);

    /**
     * Finalizer. Free the data structures allocated in initialize(int).
     */



    private native void finalize();


    /**
     * Input method identifier. For a given platform, this class may
     * support multiple input methods, and id determines which one is
     * supported by this particular instance.
     */
    public int id;

    /** reserved for instance data */
    protected int instanceData;

     /** The InputModeMediator for the current input session */
    protected InputModeMediator mediator;

    /**
     * This method is called to determine if this InputMode supports
     * the given text input constraints. The semantics of the constraints
     * value are defined in the javax.microedition.lcdui.TextField API. 
     * If this InputMode returns false, this InputMode must not be used
     * to process key input for the selected text component.
     *
     * @param constraints text input constraints. The semantics of the 
     * constraints value are defined in the TextField API.
     *
     * @return true if this InputMode supports the given text component
     *         constraints, as defined in the MIDP TextField API
     */
    public native boolean supportsConstraints(int constraints);
    
    /**
     * Returns the display name which will represent this InputMode to 
     * the user, such as in a selection list or the softbutton bar.
     *
     * @return the locale-appropriate name to represent this InputMode
     *         to the user
     */
    public native String getName();

    /**
     * Returns the command name which will represent this InputMode in
     * the input menu
     *
     * @return the locale-appropriate name to represent this InputMode
     *         to the user
     */
    public native String getCommandName();
    
    /**
     * This method will be called before any input keys are passed
     * to this InputMode to allow the InputMode to perform any needed
     * initialization. A reference to the InputModeMediator which is
     * currently managing the relationship between this InputMode and
     * the input session is passed in. This reference can be used
     * by this InputMode to commit text input as well as end the input
     * session with this InputMode. The reference is only valid until
     * this InputMode's endInput() method is called.
     *
     * @param constraints text input constraints. The semantics of the 
     * constraints value are defined in the TextField API.
     *
     * @param theMediator the InputModeMediator which is negotiating the
     *        relationship between this InputMode and the input session
     *
     * @param inputSubset current input subset
     */
    public void beginInput(InputModeMediator theMediator, String inputSubset,
                           int constraints) 
        throws IllegalStateException {
        mediator = theMediator;
        beginInput0(theMediator, inputSubset, constraints);
    }

    public native void beginInput0(InputModeMediator theMediator, String inputSubset,
                           int constraints);
    /**
     * Process the given key code as input.
     * 
     * This method will return true if the key was processed successfully,
     * false otherwise.
     *
     * @param keyCode the keycode of the key which was input
     * @param longPress return true if it's long key press otherwise false
     * @return true if the key was processed by this InputMode, false
     *         otherwise.
     */
    public int processKey(int keyCode, boolean longPress)
        throws IllegalStateException {
        int iterationCount = 0;
        final int maxIterations = 32767;
        final int clearKeyFlag =
            mediator == null ? -1 :
            mediator.isClearKey(keyCode) ? 1 : 0;
        int[] stateArgs = new int[STATE_DATA_ARRAY_SIZE];
        String stringArg;
        do {
            if (iterationCount++ > maxIterations) {
                throw new RuntimeException("too many iterations inside processKey()");
            }
            stringArg = processKey0(keyCode, longPress, clearKeyFlag,stateArgs);
            executeMediatorCommand(stateArgs, stringArg);
        } while (0 != stateArgs[STATE_NEXT_STATE]);

        if (0 != stateArgs[STATE_INTERNAL]
         || 0 != stateArgs[STATE_INTERNAL_EXT]) {
            // If we are here,
            // the programmer has either forgot to free memory,
            // or has not modified the next_state number.
            throw new RuntimeException("the internal state parameter "
                                        +"record have not been released");
        }
        return stateArgs[STATE_FINAL_RES];
    }
    /**
     * Execute a mediator command whose id and integer arguments are stored
     * in the stateArgs array, and the string argument, if required, is
     * passed as stringArg; the returned value, if any, is stored
     * as stateArgs[STATE_CALLBACK_RES].
     *
     * This function implements the functionality of processKey, but
     * when it needs to call some mediator function, it stores the current
     * state into stateArgs  and returns; processKey calls the necessary
     * interface function and calls processKey0 again.
     *
     * stateArgs[STATE_CALLBACK_RES] -- mediator function result. <br>
     * stateArgs[STATE_FUNC_TOKEN] -- the mediator function id. <br>
     * stateArgs[STATE_NEXT_STATE] -- not used by executeMediatorCommand,
     *      a native function uses this value to store the integer state id,
     *      the Java function that calls the native function repeats calls
     *      until this value becomes zero. <br>
     * stateArgs[STATE_INT_ARG] -- int argument for the mediator function,
     *                              if required. <br>
     * stateArgs[STATE_FINAL_RES] -- not used by executeMediatorCommand,
     *      the native function store there a result to be returned
     *      by the Java function. <br>
     * stateArgs[STATE_INTERNAL] -- for use by native functions. <br>
     * stateArgs[STATE_INTERNAL_EXT] -- for use by native functions. <br>
     *
     * @param stateArgs the function state data array
     * @param stringArg the string argument, this value is used only if the
     *          function specified as stateArgs[STATE_FUNC_TOKEN] requires
     *          a string argument.
     */
    protected void executeMediatorCommand(int [] stateArgs, String stringArg) {
        if (null == mediator) {
            return;
        }
        switch(stateArgs[STATE_FUNC_TOKEN]) {
            default:
            case MEDIATOR_NOOP:
                break;
            case MEDIATOR_COMMIT:
                mediator.commit(stringArg);
                break;
            case MEDIATOR_CLEAR:
                mediator.clear(stateArgs[STATE_INT_ARG]);
                break;
            case MEDIATOR_SUBINPUTMODECHANGED:
                mediator.subInputModeChanged();
                break;
            case MEDIATOR_INPUTMODECOMPLETED:
                mediator.inputModeCompleted();
                break;
            case MEDIATOR_ISCLEARKEY:
                {
                    boolean res = mediator.isClearKey(stateArgs[STATE_INT_ARG]);
                    stateArgs[STATE_CALLBACK_RES] = res ? 1 : 0;
                }
                break;
            case MEDIATOR_GETAVAILABLESIZE:
                stateArgs[STATE_CALLBACK_RES] = mediator.getAvailableSize();
                break;
            case MEDIATOR_ISNEWLINEKEY:
                {
                    boolean res = mediator.isNewlineKey(stateArgs[STATE_INT_ARG]);
                    stateArgs[STATE_CALLBACK_RES] = res ? 1 : 0;
                }
                break;
        }
        stateArgs[STATE_FUNC_TOKEN] = MEDIATOR_NOOP;
    }

    /**
     * Process the given key code as input.
     *
     * This method will return true if the key was processed successfully,
     * false otherwise.
     *
     * @param keyCode the keycode of the key which was input
     * @param longPress return true if it's long key press otherwise false
     * @param isClearKey 1 if it's a claer key, 0 if it's not, and -1 if
     *              it cannot be determined because the mediator is null
     * @param stateArgs contains state information that survives across
     *              repeated reinvocations of this function, and data
     *              to be passed to/from the mediator functions.
     * @return the string argument to be passed to the mediator function,
     *         or null.
     */
    protected native String processKey0(int keyCode, boolean longPress, int isClearKey, int[] stateArgs)
        throws IllegalStateException;

    /**
     * return the pending char
     * used to bypass the asynchronous commit mechanism
     * e.g. to immediately commit a char before moving the cursor
     * @return return the pending char
     */
    public native char getPendingChar();

    /**
     * Return the next possible match for the key input processed thus
     * far by this InputMode. A call to this method should be preceeded
     * by a check of hasMoreMatches(). If the InputMode has more available
     * matches for the given input, this method will return them one by one.
     *
     * @return a String representing the next available match to the key 
     *         input thus far, or 'null' if no pending input is available
     */
    public native String getNextMatch();
        
    /**
     * True, if after processing a key, there is more than one possible
     * match to the input. If this method returns true, the getNextMatch()
     * method can be called to return the value.
     *
     * @return true if after processing a key, there is more than the one
     *         possible match to the given input
     */
    public native boolean hasMoreMatches();


    /**
     * Gets the possible string matches 
     *
     * @return returns the set of options.
     */
    public native String[] getMatchList();
    
    /**
     * Mark the end of this InputMode's processing. The only possible call
     * to this InputMode after a call to endInput() is a call to beginInput()
     * to begin a new input session.
     */
    public void endInput() 
        throws IllegalStateException {
        endInput0();
    }

    public native void endInput0()
        throws IllegalStateException;

    /**
     * Gets displayable for particular input method. If the input method has no
     * specific displayable representation returns null.  
     * @return displayable 
     */
    public Displayable getDisplayable() {
        return null;
    }
    

    /** 
     * Returns true if input mode is using its own displayable, false ifinput
     * mode does not require the speial displayable for its representation 
     * @return true if input mode is using its own displayable, otherwise false
     */
    public boolean hasDisplayable() {
        return false;
    }

    /**
     * Returns the map specifying this input mode is proper one for the
     * particular pair of input subset and constraint. The form of the map is
     *
     *                       |ANY|EMAILADDR|NUMERIC|PHONENUMBER|URL|DECIMAL|
     * ---------------------------------------------------------------------
     * IS_FULLWIDTH_DIGITS   |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_FULLWIDTH_LATIN    |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_HALFWIDTH_KATAKANA |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_HANJA              |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_KANJI              |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_LATIN              |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_LATIN_DIGITS       |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_SIMPLIFIED_HANZI   |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_TRADITIONAL_HANZI  |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * MIDP_UPPERCASE_LATIN  |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * MIDP_LOWERCASE_LATIN  |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * NULL                  |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     *
     * @return input subset x constraint map
     */
    public boolean[][] getIsConstraintsMap() {
        return isMap;
    }


    /** input subset x constraint map */
/*    private static boolean[][] isMap = {
        // |ANY|EMAILADDR|NUMERIC|PHONENUMBER|URL|DECIMAL
        { false, false, false, false, false, false }, // IS_FULLWIDTH_DIGITS
        { false, false, false, false, false, false }, // IS_FULLWIDTH_LATIN
        { false, false, false, false, false, false }, // IS_HALFWIDTH_KATAKANA
        { false, false, false, false, false, false }, // IS_HANJA
        { false, false, false, false, false, false }, // IS_KANJI
        { false, false, false, false, false, false }, // IS_LATIN
        { false, false, false, false, false, false }, // IS_LATIN_DIGITS
        { false, false, false, false, false, false }, // IS_SIMPLIFIED_HANZI
        { false, false, false, false, false, false }, // IS_TRADITIONAL_HANZI
        { false, false, false, false, false, false }, // MIDP_UPPERCASE_LATIN
        { false, false, false, false, false, false }, // MIDP_LOWERCASE_LATIN
        { false, false, false, false, false, false }  // NULL
    };
*/
    /** input subset x constraint map */
    private boolean[][] isMap = new boolean[][] {
        // |ANY|EMAILADDR|NUMERIC|PHONENUMBER|URL|DECIMAL
        { true, true, true, true, true, true }, // IS_FULLWIDTH_DIGITS
        { true, true, true, true, true, true }, // IS_FULLWIDTH_LATIN
        { true, true, true, true, true, true }, // IS_HALFWIDTH_KATAKANA
        { true, true, true, true, true, true }, // IS_HANJA
        { true, true, true, true, true, true }, // IS_KANJI
        { true, true, true, true, true, true }, // IS_LATIN
        { true, true, true, true, true, true }, // IS_LATIN_DIGITS
        { true, true, true, true, true, true }, // IS_SIMPLIFIED_HANZI
        { true, true, true, true, true, true }, // IS_TRADITIONAL_HANZI
        { true, true, true, true, true, true }, // MIDP_UPPERCASE_LATIN
        { true, true, true, true, true, true }, // MIDP_LOWERCASE_LATIN
        { true, true, true, true, true, true }  // NULL
    };

    public String toString() {
        return super.toString()+"[id="+id+"]";
    }
}


