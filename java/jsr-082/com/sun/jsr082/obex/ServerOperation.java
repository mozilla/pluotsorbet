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
package com.sun.jsr082.obex;

import javax.obex.Operation;
import javax.obex.HeaderSet;
import javax.obex.ResponseCodes;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;

/*
 * The class implements server side of put/get operation.
 */
final class ServerOperation implements Operation {
    // Debug information, should be false for RR
    private static final boolean DEBUG = false;

    private Object lock = new Object();
    private HeaderSetImpl recvHeaders;
    private ServerConnectionImpl stream;
    private int opcode;
    private boolean isGet;

    private boolean isAborted;
    private boolean requestEnd;

    private boolean inputStreamOpened  = false;
    private boolean outputStreamOpened = false;
    private boolean inputStreamEof;
    private boolean responseIsSent = false;

    private OperationInputStream  is = new OperationInputStream();
    private OperationOutputStream os = new OperationOutputStream();

    private byte[] head = ObexPacketStream.PACKET_CONTINUE;


    /* Constructor for get operation. */
    ServerOperation(ServerConnectionImpl stream) throws IOException {
        this.stream = stream;
        opcode = ObexPacketStream.OPCODE_GET;
        isGet = true;
        recvHeaders = new HeaderSetImpl(HeaderSetImpl.OWNER_SERVER);
        int mode = waitForData(stream,
                recvHeaders, ObexPacketStream.OPCODE_GET);
        switch (mode) {
            case 0:
                isAborted = true;
                stream.operationClosed = true;
                break;
            case 2:
                requestEnd = true;
                // no data was received
                inputStreamEof = true;
                stream.packetBegin(head);
                // Response packets can contains both TARGET and CONN ID headers
                stream.packetAddConnectionID(stream.getConnectionID(), null);
                stream.packetAddAuthResponses();
                stream.packetAddHeaders(null);
                break;
        }
    }

    /* Constructor for put operation. */
    ServerOperation(ServerConnectionImpl stream, HeaderSetImpl recvHeaders) {
        // data parsing mode already on.
        this.stream = stream;
        opcode = ObexPacketStream.OPCODE_PUT;
        isGet = false;
        this.recvHeaders = recvHeaders;

    }

    public DataOutputStream openDataOutputStream() throws IOException {
        return new DataOutputStream(openOutputStream());
    }

    public OutputStream openOutputStream() throws IOException {
        if (DEBUG) {
            System.out.println("server: openOutputStream()");
        }
        synchronized (lock) {
            if (stream.operationClosed) {
                throw new IOException("operation closed");
            }
            if (outputStreamOpened) {
                throw new IOException("no more output streams available");
            }
            if (!requestEnd) {
                throw new IOException("input data not read out");
            }
            outputStreamOpened = true;
            return os;
        }
    }

    public DataInputStream openDataInputStream() throws IOException {
        return new DataInputStream(openInputStream());
    }

    public InputStream openInputStream() throws IOException {
        if (DEBUG) {
            System.out.println("server: openInputStream()");
        }
        synchronized (lock) {
            if (stream.operationClosed) {
                throw new IOException("operation closed");
            }
            if (inputStreamOpened) {
                throw new IOException("no more input streams available");
            }
            inputStreamOpened = true;
            return is;
        }
    }

    public void abort() throws IOException {
        // forbidden on server
        throw new IOException("not permitted");
    }

    public HeaderSet getReceivedHeaders() throws IOException {
        synchronized (lock) {
            if (stream.operationClosed) {
                throw new IOException("operation closed");
            }
            return new HeaderSetImpl(recvHeaders);
        }
    }

    public int getResponseCode() throws IOException {
        // forbidden on server
        throw new IOException("not permitted");
    }

    public String getEncoding() {
        return null; // acording to docs
    }

    public long getLength() {
        Long res = (Long)recvHeaders.getHeader(HeaderSetImpl.LENGTH);
        if (res == null) {
            return -1;
        }
        return res.longValue();
    }

    public String getType() {
        return (String)recvHeaders.getHeader(HeaderSetImpl.TYPE);
    }

    public void close() {
        stream.operationClosed = true;
    }

    /*
     * Called by ServerRequestHandler to finish any activity remaining
     * and to return errorcode to client.
     */
    void destroy(int status) {
        if (DEBUG) {
            System.out.println("server: destroy()");
        }
        try {
            outputStreamOpened = false;
            if (!responseIsSent) {
                if (!isGet) {
                    stream.packetBegin(head);
                    stream.packetAddConnectionID(stream.getConnectionID(), null);
                    stream.packetAddAuthResponses();
                }
                stream.packetAddHeaders(null);
                close();
                if (isAborted) status = ResponseCodes.OBEX_HTTP_OK;
                stream.setPacketType(status);
                stream.packetEnd(); // send final packet
            }
        } catch (Throwable t) {
            // ignore
        }
        // remaining headers will be lost
        stream.queuedHeaders.removeAllElements();
    }

    public void sendHeaders(HeaderSet headers) throws IOException {
        if (DEBUG) {
            System.out.println("server: sendHeaders()");
        }
        synchronized (lock) {
            if (stream.operationClosed) {
                throw new IOException("operation closed");
            }
            if (headers == null) {
                throw new NullPointerException("null headerset");
            }
            if (!(headers instanceof HeaderSetImpl)) {
                throw new IllegalArgumentException("wrong headerset class");
            }
            HeaderSetImpl headersImpl = (HeaderSetImpl) headers;
            if (!headersImpl.isSendable()) {
                throw new IllegalArgumentException(
                        "not created with createHeaderSet");
            }

            stream.packetAddHeaders(headersImpl);

            if (requestEnd && isGet) {
                while (!stream.queuedHeaders.isEmpty()) {
                    packetExchange();
                }
            }
        }
    }

    private void packetExchange() throws IOException {
        if (DEBUG) {
            System.out.println("server: packetExchange()");
        }
        if (stream.operationHeadersOverflow) {
            throw new IOException("operation terminated, too long headers");
        }
        if (!requestEnd) {
            // reading out input stream
            requestEnd =
                stream.packetType == (opcode | ObexPacketStream.OPCODE_FINAL);

            // inordenary case: EOF-DATA but no final bit
            if (stream.isEof && !requestEnd) {
                while (recvHeaders.packetType
                        == ObexPacketStream.OPCODE_PUT) {
                    // not final - waiting for final, data not allowed
                    // after EOFB
                    stream.sendPacket(head, stream.getConnectionID(),
				      null, false);
                    stream.recvPacket();
                    stream.parsePacketHeaders(recvHeaders, 3);
                }

                if (recvHeaders.packetType
                        == ObexPacketStream.OPCODE_ABORT) {
                    stream.operationClosed = true;
                    isAborted = true;
                    throw new IOException("operation aborted");
                }

                if (stream.packetType !=
                        (opcode | ObexPacketStream.OPCODE_FINAL)) {
                    stream.operationClosed = true;
                    stream.brokenLink();
                    throw new IOException("protocol error");
                }
            }
            if (requestEnd) {
                // switch to requestEnd packetExchange mode
                stream.packetBegin(head);
                stream.packetAddConnectionID(stream.getConnectionID(), null);
                stream.packetAddAuthResponses();
                stream.packetAddHeaders(null);
                return;
            }
            // stream.parseEnd();
            stream.sendPacket(ObexPacketStream.PACKET_CONTINUE,
			      stream.getConnectionID(), null, false);
            stream.recvPacket();

            if (stream.packetType == ObexPacketStream.OPCODE_ABORT) {
                stream.parsePacketHeaders(recvHeaders, 3);
                isAborted = true;
                stream.operationClosed = true;
                throw new IOException("operation aborted");
            }

            if ((stream.packetType & ~ObexPacketStream.OPCODE_FINAL)
                    != opcode) {
                stream.operationClosed = true;
                stream.brokenLink();
                throw new IOException("protocol error");
            }
            stream.parsePacketDataBegin(recvHeaders, 3);
            return;
        }
        stream.packetEnd();
        stream.recvPacket();
        stream.parsePacketHeaders(recvHeaders, 3);

        if (stream.packetType == ObexPacketStream.OPCODE_ABORT) {
	    // prepare response packet
            stream.packetBegin(ObexPacketStream.PACKET_SUCCESS);
            stream.packetAddConnectionID(stream.getConnectionID(), null);
            stream.packetAddAuthResponses();
            stream.packetAddHeaders(null);

            isAborted = true;
            stream.operationClosed = true;
            throw new IOException("operation aborted");
        }

        if (stream.packetType == ObexPacketStream.OPCODE_DISCONNECT) {
            stream.sendPacket(ObexPacketStream.PACKET_SUCCESS,
                stream.getConnectionID(), null, false);

            stream.close();
            return;
        }

        if (stream.packetType != (opcode | ObexPacketStream.OPCODE_FINAL)) {
            stream.operationClosed = true;
            stream.brokenLink();
            throw new IOException("protocol error");
        }

        stream.packetBegin(head);
        stream.packetAddConnectionID(stream.getConnectionID(), null);
        stream.packetAddAuthResponses();
        stream.packetAddHeaders(null);
    }

    static int waitForData(ServerConnectionImpl stream,
            HeaderSetImpl inputHeaderSet, int op) throws IOException {
        if (DEBUG) {
            System.out.println("server: waitForData()");
        }

        // check of errorcode should be done before after data parsing
        stream.parsePacketDataBegin(inputHeaderSet, 3);

        // special request to check data availability
        int hasData = stream.parsePacketData(inputHeaderSet, null, 0, 0);

        // waiting for data or final bit or abort
        while (true) {
            if (stream.packetType == ObexPacketStream.OPCODE_ABORT) {
                return 0;
            }

            if (hasData == 1 || stream.isEof) {
                return 1; // has data
            }

            if (stream.packetType == (op | ObexPacketStream.OPCODE_FINAL)) {
                return 2; // final
            }

            if (stream.packetType != op) {
                stream.brokenLink();
                throw new IOException("protocol error");
            }

            stream.sendPacket(ObexPacketStream.PACKET_CONTINUE,
			      stream.getConnectionID(), null, false);
            stream.recvPacket();

            // check of errorcode should be done before after data parsing
            stream.parsePacketDataBegin(inputHeaderSet, 3);

            // special request to check data availability
            hasData = stream.parsePacketData(inputHeaderSet, null, 0, 0);
        }
    }

    private class OperationInputStream extends InputStream {
        OperationInputStream() {}

        public int read() throws IOException {
            byte[] b = new byte[1];
            int len = read(b, 0, 1);
            if (len == -1) {
                return -1;
            }
            return b[0] & 0xFF;
        }

        public int read(byte[] b, int offset, int len) throws IOException {
            if (DEBUG) {
                // System.out.println("server: is.read()");
            }
            synchronized (lock) {
                if (!inputStreamOpened) {
                    throw new IOException("operation finished");
                }
                // Nullpointer check is here
                if (len < 0 || offset < 0 || offset + len > b.length) {
                    throw new ArrayIndexOutOfBoundsException();
                }

                if (len == 0) {
                    return 0;
                }

                if (inputStreamEof) {
                    return -1;
                }

                int result = 0;
                while (true) {
                    int rd = stream.parsePacketData(recvHeaders, b,
						    offset, len);
                    if (rd != 0) {
                        offset += rd;
                        len -= rd;
                        result += rd;
                        if (len == 0) {
                            if (stream.dataOffset != stream.packetOffset) { 
                                return result;
                            }
                        }
                    } else {
                        if ((len == 0) && !stream.isEof) {
                            return result;
                        }
                    }

                    // need more data, packet is finished
                    if (stream.isEof) {
                        inputStreamEof = true;
                        if (stream.dataOffset == stream.packetOffset) { 
                            requestEnd = stream.packetType == 
                                      (opcode | ObexPacketStream.OPCODE_FINAL);
                            return (result == 0) ? -1 : result;
                        } else {
                            return result;
                        }
                    }
                    packetExchange();
                }
            }
        }

        public void close() throws IOException {
            if (DEBUG) {
                System.out.println("server: is.close()");
            }
            // errorcode unknown yet,
            // ServerRequestHandler will send errorcode packet
            synchronized (lock) {
                inputStreamOpened = false;
                inputStreamEof = false;
            }
        }
    }

    private class OperationOutputStream extends OutputStream {
        OperationOutputStream() {}

        public void write(int b) throws IOException {
            write(new byte[] { (byte)b }, 0, 1);
        }

        public void write(byte[] b, int offset, int len) throws IOException {
            if (DEBUG) {
                // System.out.println("server: os.write()");
            }
            synchronized (lock) {
                if (!outputStreamOpened) {
                    throw new IOException("operation finished");
                }
                if (len < 0 || offset < 0 || offset + len > b.length) {
                    throw new ArrayIndexOutOfBoundsException();
                }
                while (len > 0) {
                    int wr = stream.packetAddData(b, offset, len);
                    if (wr != len) {
                        packetExchange();
                    }
                    len -= wr;
                    offset += wr;
                }
            }
        }

        public void flush() throws IOException {
            if (DEBUG) {
                System.out.println("server: os.flush()");
            }
            synchronized (lock) {
                if (!outputStreamOpened) {
                    throw new IOException("operation finished");
                }
                if (isGet) {
                    packetExchange();
                }
            }
        }


        public void close() throws IOException {
            if (DEBUG) {
                System.out.println("server: os.close()");
            }

            synchronized (lock) {
                if (outputStreamOpened) {
                    outputStreamOpened = false;
                    boolean res = stream.packetEOFBody();
                    if (!res) { // error adding EOFB previous packet too long
                        packetExchange();
                        stream.packetEOFBody();
                    }
                }
                // Unknown errorcode yet, not sending: stream.packetEnd();
            }
        }
    }
}
