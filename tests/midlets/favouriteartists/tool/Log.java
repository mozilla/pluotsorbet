/*
 * Copyright © 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */

package com.nokia.example.favouriteartists.tool;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Calendar;

import javax.microedition.io.CommConnection;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.midlet.MIDlet;

/**
 * Simple class for logging messages to either console and/or files.
 * Support for log levels isn't provided but instead two constants
 * {@link #TEST} and {@link #IMPL} are provided. In the source code
 * one should use one of these constants to check whether logging should
 * be done.
 * <pre>
 *   // In test code:
 *   if (Log.TEST) Log.note("Test started");
 *   
 *   // In stub implementation:
 *   if (Log.IMPL) Log.note("Method x called");
 * </pre>
 * <p>
 * 
 * For log files there are two locations to which the Log class tries
 * to create the file to. First the memorycard is tried (<code>fileconn.dir.memorycard</code>), 
 * and if that wasn't successful then the Recordings directory is tried
 * (<code>fileconn.dir.recordings</code>).
 * <p>
 * 
 * The implementation assumes that the FileConnection API is supported.
 * Otherwise a {@link ClassNotFoundException} will be thrown when trying
 * to access this class.
 * <p>
 */
public class Log {
    
    /** Index of the {@link PrintStream} attached to a file in the {@link #streams} array. */
    private static final int FILE_INDX = 0;
    
    /** Index of the {@link PrintStream} attached to a <code>System.out</code> in the {@link #streams} array. */
    private static final int SYSOUT_INDX = 1;

    /** Index of the {@link PrintStream} associated with a CommConnection in the {@link #streams} array. */
    private static final int COMM_INDX = 2;

    /** 
     * Holds the {@link PrintStream} objects which are used for outputting log messages.
     * If an array element value is <code>null</code> then it is skipped.
     */
    private static PrintStream[] streams = new PrintStream[3];
    
    /** If not <code>null</code> then this is the {@link FileConnection} that is being used for log output. */
    private static FileConnection fileConn = null;
    
    /** If not <code>null</code> then this is the {@link CommConnection} used for log output. */
    private static CommConnection commConn = null;
    
    /** If set then all log messages will be forwarded to the delegate. */
    private static LogDelegate delegate = null;
    
    /** Default COM port identifier. */
    private static final String DEFAULT_COM_PORT_ID = "USB1";
    
    /** 
     * Indicates whether test case logging has been enabled or disabled. 
     * Enables excluding Log calls completely if used in a surrounding 
     * if-condition.  
     */
    public static final boolean TEST = true;

    /**
     * Like {@link #TEST} but meant for excluding Log calls from
     * stub implementation code. 
     */
    public static final boolean IMPL = false;

    /** Logging setup identifier. */
    private static String id; 
    
    
    /**
     * Enable or disable printing log messages to a file.
     * File logging should be disabled when exiting the MIDlet as it
     * will also close the opened {@link FileConnection}.
     * 
     * @param enabled if <code>true</code> then a new file is created, 
     *                <code>false</code> disables logging to a file.
     * @param filenamePrefix prefix of the filename part of the file path.
     * 
     * @throws IOException if a file could not be created.
     * @throws SecurityException if no permission was given to create a file.
     */
    public static void setFileLogger(boolean enabled, String filenamePrefix) throws IOException, SecurityException {
        setFileLogger(enabled, filenamePrefix, false);
    }
    
    /**
     * Enable or disable printing log messages to a file.
     * File logging should be disabled when exiting the MIDlet as it
     * will also close the opened {@link FileConnection}.
     * 
     * @param enabled if <code>true</code> then a new file is created, 
     *                <code>false</code> disables logging to a file.
     * @param filenamePrefix prefix of the filename part of the file path.
     * @param append if <code>true</code> then don't use timestamp in the filename but append to existing log file.
     * @throws IOException if a file could not be created.
     * @throws SecurityException if no permission was given to create a file.
     */
    public static void setFileLogger(boolean enabled, String filenamePrefix, boolean append) 
        throws IOException, SecurityException 
    {        
        if (enabled) {
            openFileConnection(filenamePrefix, append);
        } else {
            
            if (streams[FILE_INDX] != null) {
                streams[FILE_INDX].close();
                streams[FILE_INDX] = null;
            }
            
            if (fileConn != null) {
                
                try {
                    fileConn.close();
                } catch (IOException e) {
                }
                
                fileConn = null;
            }
        }
    }
    
    /**
     * <p>Enable or disable logging using {@link CommConnection}. If <code>commPortId</code>
     * value is <code>null</code> then the default COM port identifier will be 
     * used.</p>
     * 
     * <p>Remember to add the permission <code>javax.microedition.io.Connector.comm</code>
     * to the JAD file.</p>
     * 
     * @param enabled <code>true</code> to enable and <code>false</code> to disable.
     * @param commPortId optional COM port identifier.
     * 
     * @throws IOException if a CommConnection could not be opened.
     * @throws SecurityException if no permission was given to create the connection.
     */
    public static void setCommPortLogger(boolean enabled, String commPortId) 
        throws IOException, SecurityException 
    {
        if (commPortId == null) {
            commPortId = DEFAULT_COM_PORT_ID;
        }
        
        if (enabled) {
            openCommConnection(commPortId);
        } else {
            
            if (streams[COMM_INDX] != null) {
                streams[COMM_INDX].close();
                streams[COMM_INDX] = null;
            }
            
            if (commConn != null) {
                
                try {
                    commConn.close();
                } catch (IOException e) {
                }
                
                commConn = null;
            }
        }
    }
    
    /**
     * Opens a {@link CommConnection} using the given COM port identifier.
     * 
     * @param commPortId the COM port identifier, must not be <code>null</code>.
     * 
     * @throws IOException if a CommConnection could not be opened.
     * @throws SecurityException if no permission was given to create the connection.
     */
    private static void openCommConnection(String commPortId) 
        throws IOException, SecurityException
    {
        String portIds = System.getProperty("microedition.commports");
        
        if (portIds == null || portIds.indexOf(commPortId) == -1) {
            throw new IOException("COM port not available: " + commPortId);
        }

        commConn = (CommConnection) Connector.open("comm:" + commPortId);
        streams[COMM_INDX] = new PrintStream(commConn.openOutputStream());
    }
    
    /**
     * Opens the {@link FileConnection} and a {@link PrintStream}.
     * 
     * @param filenamePrefix prefix of the filename part of the file path.
     * @param append if <code>true</code> then don't use timestamp in the filename but append to existing log file.
     * @throws IOException if a file could not be created.
     * @throws SecurityException if no permission was given to create a file.
     * @throws NullPointerException if <code>filenamePrefix</code> is <code>null</code>.
     */
    private static void openFileConnection(String filenamePrefix, boolean append) 
        throws IOException, SecurityException 
    {
        if (!System.getProperty("microedition.io.file.FileConnection.version").equals("1.0")) {
            // FileConnection API version 1.0 isn't supported.
            // Probably a bit unnecessary check as if it isn't supported
            // a ClassNotFoundException would have been thrown earlier.
            throw new IOException("FileConnection not available");
        }
        
        final String filename = createLogFilename(filenamePrefix, !append);
        final String[] pathProperties = {"fileconn.dir.memorycard", "fileconn.dir.recordings"};
        String path = null;
        
        // Attempt to create a file to the directories specified by the 
        // system properties in array pathProperties.
        for (int i = 0; i < pathProperties.length; i++) {
            path = System.getProperty(pathProperties[i]);
            
            // Only throw declared exceptions if this is the last path
            // to try.
            try {
                
                if (path == null) {
                    
                    if (i < (pathProperties.length - 1)) {
                        continue;
                    } else {
                        throw new IOException("Path not available: " + pathProperties[i]);
                    }
                }

                FileConnection fConn = (FileConnection) 
                    Connector.open(path + filename, Connector.READ_WRITE);
                OutputStream os = null;
                
                if (append) {

                    if (!fConn.exists()) {
                        fConn.create();
                    }
                    
                    os = fConn.openOutputStream(fConn.fileSize());
                } else {
                    // Assume that createLogFilename creates such a filename
                    // that is enough to separate filenames even if they
                    // are created in a short interval (seconds).
                    fConn.create();
                    os = fConn.openOutputStream();
                }
                
                streams[FILE_INDX] = new PrintStream(os);
                
                // Opening the connection and stream was successful so don't
                // try other paths.
                fileConn = fConn;
                break;
            } catch (SecurityException se) {
                if (i == (pathProperties.length - 1)) {
                    throw se;
                }
            } catch (IOException ioe) {
                if (i == (pathProperties.length - 1)) {
                    throw ioe;
                }                
            }
        }
    }
    
    /**
     * Creates a filename to be used for the log file.
     * 
     * @param filenamePrefix prefix for the filename.
     * @param useTimestamp if <code>true</code> then append timestamp to the filename.
     * @return a log file name.
     */
    private static String createLogFilename(String filenamePrefix, boolean useTimestamp) {
        StringBuffer fn = new StringBuffer(filenamePrefix);
        
        if (useTimestamp) {
            fn.append('_');
            appendTimeStamp(fn, true);    
        }

        fn.append(".log");
        return fn.toString();
    }

    /**
     * Appends 2 digits to the given {@link StringBuffer}. If the 
     * <code>num</code> is less than 10 a 0 will be appended before it. 
     * 
     * @param sb the destination for the digits.
     * @param num the positive integer number to append.
     * 
     * @throws NullPointerException if <code>sb</code> is <code>null</code>.
     */
    private static void append2Digits(StringBuffer sb, int num) {
        
        if (num < 10) {
            sb.append('0');
        }
        
        sb.append(num);
    }

    /**
     * Prints 2 digits to the given {@link PrintStream}. If the 
     * <code>num</code> is less than 10 a 0 will be appended before it. 
     * 
     * @param ps the destination for the digits.
     * @param num the positive integer number to append.
     * 
     * @throws NullPointerException if <code>ps</code> is <code>null</code>.
     */
    private static final void print2Digits(PrintStream ps, int num) {

        if (num < 10) {
            ps.print('0');
        }
        
        ps.print(num);
    }
    
    /**
     * Append a timestamp to the given {@link StringBuffer}. 
     * The syntax is <code>hhMMss</code> for time and <code>ddMMyyyy</code>
     * for date. Example without a date: <code>102404</code>, and with a date:
     * <code>26042007_102404</code>.
     * 
     * @param sb the destination for the timestamp.
     * @param includeDate if <code>true</code> then date is also included in the timestamp.
     * 
     * @throws NullPointerException if <code>sb</code> is <code>null</code>.
     */
    public static void appendTimeStamp(StringBuffer sb, boolean includeDate) {
        Calendar c = Calendar.getInstance();
        
        if (includeDate) {
            int day = c.get(Calendar.DAY_OF_MONTH);
            int month = c.get(Calendar.MONTH) + 1;
            int year = c.get(Calendar.YEAR);
            append2Digits(sb, day);
            append2Digits(sb, month);
            sb.append(year);
            sb.append('_');
        }
        
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int mins = c.get(Calendar.MINUTE);
        int secs = c.get(Calendar.SECOND);
        append2Digits(sb, hour);
        append2Digits(sb, mins);
        append2Digits(sb, secs);
    }
    
    /**
     * Same as {@link #appendTimeStamp(StringBuffer, boolean)} but prints
     * the timestamp parts directly to the given PrintStream
     * 
     * @param ps stream where to print the timestamp.
     * @param includeDate if <code>true</code> then date is also included in the timestamp.
     * 
     * @throws NullPointerException if <code>ps</code> is <code>null</code>.
     */
    public static void printTimeStamp(PrintStream ps, boolean includeDate) {
        Calendar c = Calendar.getInstance();
        
        if (includeDate) {
            int day = c.get(Calendar.DAY_OF_MONTH);
            int month = c.get(Calendar.MONTH) + 1;
            int year = c.get(Calendar.YEAR);
            print2Digits(ps, day);
            print2Digits(ps, month);
            ps.print(year);
            ps.print('_');
        }
        
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int mins = c.get(Calendar.MINUTE);
        int secs = c.get(Calendar.SECOND);
        print2Digits(ps, hour);
        print2Digits(ps, mins);
        print2Digits(ps, secs);
    }
    
    /**
     * Enable or disable printing log messages to console (<code>System.out</code>).
     * 
     * @param enabled defines whether console log is enabled (<code>true</code>) or disabled (<code>false</code>).
     */
    public static void setConsoleLogger(boolean enabled) {
        
        if (enabled) {
            streams[SYSOUT_INDX] = System.out;
        } else {
            streams[SYSOUT_INDX] = null;
        }
    }
    
    /**
     * @return <code>true</code> if printing log output to a file is enabled.
     */
    public static boolean isUsingFiles() {
        return (streams[FILE_INDX] != null);
    }
    
    /**
     * @return <code>true</code> if printing log output to console is enabled.
     */
    public static boolean isUsingConsole() {
        return (streams[SYSOUT_INDX] != null);
    }
    
    /**
     * @return <code>true</code> if printing log output using a CommConnection is enabled.
     */
    public static boolean isUsingCommConn() {
        return (streams[COMM_INDX] != null);
    }
    
    /**
     * Print the specified message to the enabled streams. A <code>null</code>
     * message will be replaced with an empty string.
     * 
     * @param printObj if <code>true</code> then then the <code>obj</code> Object will be included in the message.
     * @param message the message to print.
     * @param obj the optional object to include in the message.
     * @param loggerName the optional logger name, if <code>null</code> then this will not be included.
     * @param levelName name of the logging level, must not be <code>null</code>.
     */
    public static synchronized void print(boolean printObj, String message, Object obj,
            String loggerName, String levelName) 
    {
        
        if (delegate != null) {
            delegate.print(printObj, message, obj, loggerName, levelName);
            return;
        }
        
        String objStr = (obj == null ? "NULL" : obj.toString());
        
        if (message == null) {
            message = "";
        }
        
        for (int i = 0; i < streams.length; i++) {
            PrintStream ps = streams[i];
            
            if (ps != null) {
                printTimeStamp(ps, false);
                ps.print(' ');
                
                if (loggerName != null) {
                    ps.print('[');
                    ps.print(loggerName);
                    ps.print("] ");
                }
                
                ps.print(levelName);
                ps.print(' ');
                
                ps.print(message);
              
                if (printObj) {
                    ps.print(": ");
                    ps.print(objStr);
                }
                
                ps.println();
                ps.flush();
            }
        }
    }
    
    /**
     * Print the specified message to the enabled streams. A <code>null</code>
     * message will be replaced with an empty string.
     * 
     * @param error if <code>true</code> then the printed message will indicate that this is an error message.
     * @param printObj if <code>true</code> then then the <code>obj</code> Object will be included in the message.
     * @param message the message to print.
     * @param obj the optional object to include in the message.
     */
    private static void print(boolean error, boolean printObj, String message, Object obj) {
        print(printObj, message, obj, null, (error ? "**ERROR**" : "TRACE"));
    }
    
    /**
     * Prints the specified message to enabled log streams.
     * 
     * @param message message to print. If null only timestamp will be printed.
     */
    public static void note(String message) {
        print(false, false, message, null);
    }
    
    /**
     * Prints the specified message and the object to enabled log streams.
     * 
     * @param message message to print. If null only timestamp and value will be printed.
     */
    public static void note(String message, Object value) {
        print(false, true, message, value);
    }
    
    /**
     * Prints the specified message and integer value to enabled log streams.
     * 
     * @param message message to print. If null only timestamp and value will be printed.
     */
    public static void note(String message, int value) {
        print(false, true, message, new Integer(value));
    }
    
    /**
     * Prints the specified message and long integer value to enabled log streams.
     * 
     * @param message message to print. If null only timestamp and value will be printed.
     */
    public static void note(String message, long value) {
        print(false, true, message, new Long(value));
    }
    
    /**
     * Prints the specified error message and boolean value to enabled log streams.
     * 
     * @param message message to print. If null only timestamp and value will be printed.
     */
    public static void note(String message, boolean value) {
        print(false, true, message, new Boolean(value));
    }
    
    /**
     * Prints the specified error message to enabled log streams.
     * 
     * @param message message to print. If null only timestamp will be printed.
     */
    public static void error(String message) {
        print(true, false, message, null);
    }
    
    /**
     * Prints the specified error message and the object to enabled log streams.
     * 
     * @param message message to print. If null only timestamp and value will be printed.
     */
    public static void error(String message, Object value) {
        print(true, true, message, value);
    }
    
    /**
     * Prints the specified error message and integer value to enabled log streams.
     * 
     * @param message message to print. If null only timestamp and value will be printed.
     */
    public static void error(String message, int value) {
        print(true, true, message, new Integer(value));
    }
    
    /**
     * Prints the specified error message and long integer value to enabled log streams.
     * 
     * @param message message to print. If null only timestamp and value will be printed.
     */
    public static void error(String message, long value) {
        print(true, true, message, new Long(value));
    }
    
    /**
     * Prints the specified error message and boolean value to enabled log streams.
     * 
     * @param message message to print. If null only timestamp and value will be printed.
     */
    public static void error(String message, boolean value) {
        print(true, true, message, new Boolean(value));
    }
    
    
    //=== Log initialization and closing related methods ======================>
    
    /**
     * @return logging setup identifier or <code>null</code> if not initialized yet.
     */
    public static String getId() {
        return Log.id;
    }
    
    /**
     * <p>Defines a delegate that will take care of printing the log messages to a file, 
     * console or where ever. If set, then {@link Log} will not print anything
     * to any of the defined outputs.</p>
     * 
     * <p>Delegate enables a MIDlet to use another logging library without losing
     * SA Library's log messages.</p>
     * 
     * @param delegate the log delegate, <code>null</code> to unset the delegate.
     */
    public static void setDelegate(LogDelegate delegate) {
        Log.delegate = delegate;
    }
    
    /**
     * Enables or disables console (sysout/commconn) and file logging based on the 
     * classname (packagename not counted for) of the specified MIDlet.
     * 
     * @return <code>true</code> if log initialization was successful and 
     *         <code>false</code> if an error occurred.
     */
    public static boolean initLogging(MIDlet midlet) {
        return initLogging(midlet, null);
    }
    
    /**
     * Enables or disables console (sysout/commconn) and file logging based on the 
     * classname (packagename not counted for) of the specified MIDlet.
     * The identifier can be used for allowing MIDlet specific logging settings
     * in case a MIDlet suite has multiple MIDlets. The 
     * MIDlet specific JAD attribute names are prefixed with 
     * <code>[id]-</code>, e.g. if identifier is <code>FgMidlet</code> 
     * the attributes are be <code>FgMidlet-logfile</code>,
     * <code>FgMidlet-logsysout</code> and <code>FgMidlet-logcomm</code>.
     * If MIDlet specific attributes are not defined then the common ones
     * are used instead, i.e. ones without the identifier prefix.
     * Note that since all logging methods are static methods this means
     * that there can be only one setup for a single classloader. So if
     * MIDlet suite is a normal one with multiple MIDlets then
     * usually they all use the same Log class and thus only one setup is possible.
     * 
     * @return <code>true</code> if log initialization was successful and 
     *         <code>false</code> if an error occurred.
     */
    public static boolean initLogging(MIDlet midlet, String id) {
        
        if (id == null) {
            id = midlet.getClass().getName();
            int lastDot = id.lastIndexOf('.');
            
            if (lastDot > -1) {
                id = id.substring(lastDot + 1);
            }
        }
        
        Log.id = id;
        
        try {
            
            // Read default log settings from Jad attributes.
            if (isEnabled(midlet, id, "logfile", false)) {
                boolean append = isEnabled(midlet, id, "logfile-append", false);
                Log.setFileLogger(true, id, append);
            } else {
                Log.setFileLogger(false, id);
            }
            
            if (isEnabled(midlet, id, "logsysout", false)) {
                Log.setConsoleLogger(true);
            } else {
                Log.setConsoleLogger(false);
            }
            
            if (isEnabled(midlet, id, "logcomm", false)) {
                Log.setCommPortLogger(true, midlet.getAppProperty("logcomm-id"));
            } else {
                Log.setCommPortLogger(false, null);
            }
            
            return true;
        } catch (IOException ioe) { 
            return false;
        }
    }
        
    /**
     * Checks the value of a Jad attribute and returns the corresponding 
     * boolean value.
     * Only attributes that have <code>enabled</code> and <code>disabled</code>
     * should be used with this method.
     * 
     * @param midlet the MIDlet which Jad attributes to read.
     * @param id logging setup identifier or <code>null</code> to use common logging setup.
     * @param attrName name of the attribute to check.
     * @param defaultValue default value to be used in case the attribute is not defined.
     * 
     * @return <code>true</code> if the specified attribute has the value 
     *         <code>enabled</code> and <code>false</code> otherwise.
     */
    private static final boolean isEnabled(MIDlet midlet, String id, String attrName, boolean defaultValue) {
        String origAttrName = attrName;
        
        if (id != null) {
            // Read setup specific attribute first.
            attrName = id + "-" + attrName;
        }
        
        String value = midlet.getAppProperty(attrName);

        if (value == null) {
            
            if (id != null) {
                // Setup specific value not specified - use common logging setup instead.
                return isEnabled(midlet, null, origAttrName, defaultValue);
            }
            
            return defaultValue;
        } else if (value.equalsIgnoreCase("enabled")) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Closes logging connections.
     */
    public static final void closeLogging() {
    	try {
    		setFileLogger(false, null);
    	}
    	catch (Exception e) {
    	}
        try {
            setCommPortLogger(false, null);
        }
        catch (Exception e) {
        }
    }
}
