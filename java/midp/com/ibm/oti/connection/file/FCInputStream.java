package com.ibm.oti.connection.file;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */

import java.io.*;
import javax.microedition.io.*;

/**
 * FCInputStream is a class for reading bytes from a file.
 *
 * @author		IBM
 * @version		initial
 *
 * @see 		InputStream
 */
public class FCInputStream extends InputStream
{
	/**
	 * The file descriptor representing this FCInputStream.
	 */
	int descriptor = -1;
	
	/**
	 * The file connection that opened this FCInputStream.
	 */
	private Connection conn = null;

	public static final FCInputStream in = new FCInputStream(0);

FCInputStream(int fid) {
	descriptor = fid;
}

/**
 * Constructs a new FCInputStream on the file path <code>filePath</code>.  If the
 * file does not exist, the <code>ConnectionNotFoundException</code> is thrown. 
 *
 * @author		IBM
 * @version		initial
 *
 * @param		filePath	the bytes of file path which this stream will read from.
 *
 * @exception 	ConnectionNotFoundException If the <code>fileName</code> is not found.
 */
public FCInputStream(byte[] filePath, Connection conn) throws ConnectionNotFoundException {
	if ((descriptor = openImpl(filePath)) == -1)
		throw new ConnectionNotFoundException(new String(filePath));
	this.conn = conn;
}

/**
 * Answers a int representing then number of bytes that are available
 * before this InputStream will block.  This method always returns the
 * size of the file minus the current position.
 *
 * @author		IBM
 * @version		initial
 *
 * @return 		the number of bytes available before blocking.
 *
 * @exception 	java.io.IOException	If an error occurs in this stream.
 */
public int available() throws IOException {
	if (descriptor == -1) throw new IOException("File Connection InputStream closed");
	return availableImpl(descriptor);
};

private native int availableImpl(int descriptor) throws IOException;

/**
 * Close the FCInputStream, and notify the connection that has opened it.
 *
 * @author		IBM
 * @version		initial
 *
 * @exception 	java.io.IOException	If an error occurs attempting to close this FCInputStream.
 */
public synchronized void close() throws IOException {
	try {
		closeImpl(descriptor);
	} finally {
		descriptor = -1;
		if (conn!=null)
			conn.notifyInputStreamClosed();
	}
}

private native void closeImpl(int descriptor) throws IOException;

private native int openImpl(byte [] fileName);

/**
 * Reads a single byte from this FCInputStream and returns the result as
 * an int.  The low-order byte is returned or -1 of the end of stream was
 * encountered.
 *
 * @author		IBM
 * @version		initial
 *
 * @return 		the byte read or -1 if end of stream.
 *
 * @exception 	java.io.IOException If the stream is already closed or another IOException occurs.
 */
public int read() throws IOException {
	if (descriptor == -1) throw new IOException("File Connection InputStream closed");
	return readByteImpl(descriptor);
}

private native int readByteImpl(int descriptor ) throws IOException;

/**
 * Reads at most <code>count</code> bytes from the FCInputStream and stores them in byte
 * array <code>buffer</code> starting at <code>offset</code>. Answer the number of
 * bytes actually read or -1 if no bytes were read and end of stream was encountered.
 *
 * @author		IBM
 * @version		initial
 *
 * @param		buffer	the byte array in which to store the read bytes.
 * @param		offset	the offset in <code>buffer</code> to store the read bytes.
 * @param		count	the maximum number of bytes to store in <code>buffer</code>.
 * @return 		the number of bytes actually read or -1 if end of stream.
 *
 * @exception 	java.io.IOException If the stream is already closed or another IOException occurs.
 */
public int read(byte[] buffer, int offset, int count) throws IOException {
	if (descriptor == -1) throw new IOException("File Connection InputStream closed");
	return readImpl(buffer, offset, count, descriptor);
}

private native int readImpl(byte[] buffer, int offset, int count, int descriptor) throws IOException;

/**
 * Skips <code>count</code> number of bytes in this FCInputStream.  Subsequent
 * <code>read()</code>'s will not return these bytes unless <code>reset()</code>
 * is used.  This method may perform multiple reads to read <code>count</code>
 * bytes.  This default implementation reads <code>count</code> bytes into a temporary
 * buffer.
 *
 * @author		IBM
 * @version		initial
 *
 * @param 		count		the number of bytes to skip.
 * @return		the number of bytes actually skipped.
 *
 * @exception 	java.io.IOException If the stream is already closed or another IOException occurs.
 */
public long skip(long count) throws IOException {
	if (descriptor == -1) throw new IOException("File Connection InputStream closed");
	return skipImpl(count, descriptor);
};

private native long skipImpl(long count, int descriptor) throws IOException;
}
