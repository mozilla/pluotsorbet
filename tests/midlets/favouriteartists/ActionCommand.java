/*
 * Copyright © 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.favouriteartists;
import javax.microedition.lcdui.Command;


/**
 * Extends the standard {@link Command} class to allow definition of a action
 * identifier. The associated action should be executed when the command
 * is selected in the UI.<p>
 * 
 * The action identifiers are listed in the class {@link Actions}.<p>
 * 
 * The same label might be used for different actions so because of that
 * separate identifiers for the action and the label are needed.<p>
 */
public class ActionCommand extends Command {

    /** The identifier of the associated action.  */
    private short actionId;

    /**
     * Creates a new ActionCommand object.
     * 
     * @param actionId the identifier of the associated action.
     * @param label command label.
     * @param commandType command type.
     * @param priority commands priority.
     * 
     * @see Command#Command(String, int, int)
     */
    public ActionCommand(short actionId, String label, int commandType, int priority) {
        super(label, commandType, priority);
        this.actionId = actionId;
    }
    
    /**
     * @return the identifier of the associated action.
     */
    public short getActionId() {
        return this.actionId;
    }
    
}
