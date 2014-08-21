package com.ibm.oti.connection.file;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */

import java.io.*;
import javax.microedition.io.*;

/**
 * FCOutputStream is a class whose underlying stream is represented by
 * a file in the operating system.  The bytes that are written to this stream are
 * passed directly to the underlying operating system equivalent function.
 *
 * @author		IBM
 * @version		initial
 *
 * @see			OutputStream
 */
public class FCOutputStream extends OutputStream {

	/**
	 * The file descriptor representing this FCOutputStream.
	 */
	int descriptor = -1;

	/**
	 * The file connection that opened this FCOutputStream.
	 */
	private Connection conn = null;

	public static final FCOutputStream out = new FCOutputStream(1);
	public static final FCOutputStream err = new FCOutputStream(2);

FCOutputStream(int fid) {
	descriptor = fid;
}

/**
 * Constructs a new FCOutputStream on the file path <code>filePath</code>. If
 * the file exists, it is written over.  
 *
 * @author		IBM
 * @version		initial
 *
 * @param		filePath	the bytes of the file path on which the stream writes to.
 *
 * @exception 	ConnectionNotFoundException If the <code>filePath</code> cannot be opened for writing.
 */
public FCOutputStream(byte[] filePath, Connection conn) throws ConnectionNotFoundException {
	if ((descriptor = openImpl(filePath)) == -1)
		throw new ConnectionNotFoundException(new String(filePath));
	this.conn = conn;
}

/**
 * Constructs a new FCOutputStream on the file path <code>filePath</code>. If
 * the file exists, it is written over.  
 *
 * @author		IBM
 * @version		initial
 *
 * @param		filePath	the bytes of the file path on which the stream writes to.
 * @param		offset		the offset where the stream will start writing from.
 *
 * @exception 	ConnectionNotFoundException If the <code>filePath</code> cannot be opened for writing.
 */
public FCOutputStream(byte[] filePath, long offset, Connection conn) throws ConnectionNotFoundException {
	if ((descriptor = openOffsetImpl(filePath, offset)) == -1)
		throw new ConnectionNotFoundException(new String(filePath));
	this.conn = conn;
}

/**
 * Close the FCOutputStream, and notify the connection that has opened it.
 * This implementation closes the underlying OS resources allocated
 * to represent this stream.
 *
 * @author		IBM
 * @version		initial
 *
 * @exception 	java.io.IOException	If an error occurs attempting to close this FCOutputStream.
 */
public synchronized void close() throws IOException {
	try {
		closeImpl(descriptor);
	} finally {
		descriptor = -1;
		if (conn!= null)
			conn.notifyOutputStreamClosed();
	}

};

private native void closeImpl(int descriptor) throws IOException;

private native int openImpl(byte[] fileName);

private native int openOffsetImpl(byte[] fileName, long offset);


/**
 * Writes <code>count</code> <code>bytes</code> from the byte array
 * <code>buffer</code> starting at <code>offset</code> to this
 * FCOutputStream.
 *
 * @author		IBM
 * @version		initial
 *
 * @param		buffer		the buffer to be written
 * @param		offset		offset in buffer to get bytes
 * @param		count		number of bytes in buffer to write
 *
 * @throws java.io.IOException	If an error occurs attempting to write to this FCOutputStream.
 * @throws	java.lang.IndexOutOfBoundsException If offset or count are outside of bounds.
 * @throws	java.lang.NullPointerException If buffer is <code>null</code>.
 */
public void write(byte[] buffer, int offset, int count) throws IOException {
	if (descriptor == -1) throw new IOException("File Connection OutputStream closed");
	writeImpl(buffer, offset, count, descriptor);
}

private native void writeImpl(byte[] buffer, int offset, int count, int descriptor) throws IOException;

/**
 * Writes the specified byte <code>oneByte</code> to this FCOutputStream.  Only
 * the low order byte of <code>oneByte</code> is written.
 *
 * @author		IBM
 * @version		initial
 *
 * @param		oneByte		the byte to be written
 *
 * @exception 	java.io.IOException	If an error occurs attempting to write to this FCOutputStream.
 */
public void write(int oneByte) throws IOException {
	if (descriptor == -1) throw new IOException("File Connection OutputStream closed");
	writeByteImpl(oneByte, descriptor);
}

private native void writeByteImpl(int oneByte, int descriptor) throws IOException;

public void flush() throws IOException {
	//do not try to sync for System.out and System.err
	if (descriptor > 2)
		syncImpl(descriptor);
}

private native void syncImpl(int descriptor) throws IOException;

}
