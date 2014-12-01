package javax.microedition.io.file;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */
 
/**
 * Represents an exception thrown when a method is invoked on a file
 * connection but the method cannot be completed because the connection
 * is closed.
 *
 * @since FileConnection 1.0
 */

public class ConnectionClosedException extends java.lang.RuntimeException {

    /**
    * Constructs a new instance of this class with its stack trace filled in.
    *
    */
    public ConnectionClosedException() {
        super();
    }

    /**
    * Constructs a new instance of this class with its stack trace and message
    * filled in.
    *
    * @param  detailMessage String
    *         The detail message for the exception.
    */
    public ConnectionClosedException(String detailMessage) {
        super(detailMessage);
    }
}