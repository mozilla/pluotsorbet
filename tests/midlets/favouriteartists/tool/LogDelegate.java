/*
 * Copyright © 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.favouriteartists.tool;

/**
 * An object implementing this interface can be specified for {@link Log} in order
 * to let another logging library to handle the logging.
 */
public interface LogDelegate {

    /**
     * Called to print a log message. The delegate is responsible for printing timestamps.
     * 
     * @param printObj if <code>true</code> then <code>obj</code> should be included in the message.
     * @param message the log message.
     * @param obj optional object to include in the message.
     * @param loggerName optional name of the logger instance.
     * @param levelName optional name of the logging level used for the message.
     */
    public void print(boolean printObj, String message, Object obj, String loggerName, String levelName);
    
}
