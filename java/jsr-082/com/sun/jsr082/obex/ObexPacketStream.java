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

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;
import java.util.Stack;
import javax.microedition.io.Connection;
import javax.obex.ResponseCodes;
import javax.obex.Authenticator;
import com.sun.j2me.log.Logging;

/*
 * Obex core protocol.
 */
public abstract class ObexPacketStream implements Connection {

    /* Debug information, should be false for RR. */
    private static final boolean DEBUG = false;
    private static final boolean DEBUG2 = false;

    // OBEX operations opcodes
    static final int OPCODE_CONNECT = 0x80;
    static final int OPCODE_DISCONNECT = 0x81;
    static final int OPCODE_PUT = 0x02;
    static final int OPCODE_GET = 0x03;
    static final int OPCODE_SETPATH = 0x85;
    static final int OPCODE_CONTINUE = 0x90;
    static final int OPCODE_ABORT = 0xFF;
    static final int OPCODE_FINAL = 0x80;
    static final int OPCODE_GET_FINAL = OPCODE_GET | OPCODE_FINAL;

    static final byte[] PACKET_ABORT = { (byte) OPCODE_ABORT, 0, 0 };
    static final byte[] PACKET_CONTINUE = { (byte) OPCODE_CONTINUE, 0, 0};
    static final byte[] PACKET_DISCONNECT = {
        (byte) OPCODE_DISCONNECT, 0, 0};
    static final byte[] PACKET_SUCCESS = {
        (byte) ResponseCodes.OBEX_HTTP_OK, 0, 0};

    static final byte[] PACKET_BAD_REQUEST = {
        (byte) ResponseCodes.OBEX_HTTP_BAD_REQUEST, 0, 0};

    static final byte[] PACKET_NOT_IMPLEMENTED = {
        (byte) ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED, 0, 0};

    private static final int HEADER_BODY = 0x48;
    private static final int HEADER_EOFBODY = 0x49;
    private static final int HEADER_CONNECTION_ID = 0xCB;
    static final int HEADER_AUTH_CHALLENGE = 0x4D;
    static final int HEADER_AUTH_RESPONSE = 0x4E;

    static TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");

    private static final int TRANSPORT_READ_INTERVAL = 10;

    private ObexTransport transport;

    Authenticator authenticator;

    /*
     * Generated authentication responses. They will be send in sendPacket().
     * Stored in <CODE>byte[]</CODE> format.
     */
    Vector authResponses = new Vector();

    /*
     * Sent authentication challenges.
     * They will be used for check authentication responses.
     */
    Vector authChallenges = new Vector();

    /*
     * True when buffer contains packet for sending and some headers can
     * be added.
     */
    boolean moreHeaders = false;


    /*
     * Outgoing packet contains authentication challenges, so it should be
     * sent immediatly.
     */
    boolean challengesToSend = false;

    /*
     * True if one of sending headers cannot feet in empty packet.
     */
    boolean headerOverflow = false;

    /*
     * True if sending packet contains target header.
     */
    boolean containsTargetHeader = false;

    /*
     * Queue of outgoing headers, not feet in packet.
     */
    Vector queuedHeaders;
    QueuedHeader newHeader;
    Stack emptyHeadersPool;

    /*
     * Set when sending auth challenge,
     * reset when received valid auth response in next
     * packet.
     */
    boolean authFailed = false;

    /*
     * True if this is ClientSession, false in ServerConnectionImpl.
     */
    boolean isClient;

    /*
     * Client is connected flag. Ignored by ServerConnectionImpl.
     */
    boolean isConnected;

    int OBEX_MAXIMUM_PACKET_LENGTH;

    byte[] buffer, cache;
    int packetLength;
    int packetOffset;
    int packetType;

    int maxSendLength;

    boolean dataOpened, dataClosed;
    boolean isEof;
    int dataOffset;

    /*
     * Connection id used in <code>setConnectionID</code>,
     * <code>getConnectioID</code>.
     */

    ObexPacketStream(ObexTransport transport) {
        this.transport = transport;
        OBEX_MAXIMUM_PACKET_LENGTH = transport.getMaximumPacketSize();
        buffer = new byte[OBEX_MAXIMUM_PACKET_LENGTH];
        cache = new byte[OBEX_MAXIMUM_PACKET_LENGTH];
        maxSendLength = OBEX_MAXIMUM_PACKET_LENGTH;
        newHeader = new QueuedHeader(this);
        queuedHeaders = new Vector();
        emptyHeadersPool = new Stack();
    }


    // interface visible function
    public void close() {
        try {
            if (transport != null) {
                transport.close();
            }
        } catch (IOException e) {
            // nothing
        }
        transport = null;
    }

    public void setAuthenticator(Authenticator authenticator) {
        if (authenticator == null) {
            throw new NullPointerException("null authenticator");
        }
        this.authenticator = authenticator;
    }

    public Connection getTransport()
        throws IOException {
        if (transport == null) {
            throw new IOException("connection error");
        }
        return transport.getUnderlyingConnection();
    }

    /*
     * Sets link broken flag.
     */
    void brokenLink() {
        close();
    }

    boolean isClosed() {
        return transport == null;
    }

    void packetBegin(byte[] head) {
        if (DEBUG) {
            System.out.println("packetBegin()");
        }

        containsTargetHeader = false;
        moreHeaders = true;
        challengesToSend = false;
        System.arraycopy(head, 0, buffer, 0, head.length);
        packetLength = head.length;
        authChallenges.removeAllElements();
        dataOpened = false;
        dataClosed = false;
        dataOffset = -3; // generate aoobe when accessed
    }

    int packetAddData(byte[] data, int offset, int length) {
        if (DEBUG) {
            System.out.println("packetAddData()");
        }
        // preventing writing several data blocks, just in case
        if (dataClosed)
	    return 0;

        if (!dataOpened) {
            // let it be at least 3 bytes workload to create new Body header
            if (packetLength + 6 > maxSendLength) {
                return 0;
            }
            buffer[packetLength] = HEADER_BODY;
            dataOffset = packetLength;
            packetLength += 3;
            dataOpened = true;
        }

        int len;
        if (packetLength + length > maxSendLength) {
            len = maxSendLength - packetLength;
        } else {
            len = length;
        }
        System.arraycopy(data, offset, buffer, packetLength, len);
        packetLength += len;
        return len;
    }

    int getPacketLength() {
        return packetLength;
    }

    void restorePacketLength(int len) {
        packetLength = len;
    }

    boolean packetEOFBody() {
        if (DEBUG) {
            System.out.println("packetEOFBody()");
        }
        if (dataClosed) {
            return false;
        }
        if (dataOpened) {
            buffer[dataOffset+0] = HEADER_EOFBODY;
            return true;
        } else {
            if (packetLength + 3 > maxSendLength) {
                return false;
            }
            buffer[packetLength++] = HEADER_EOFBODY;
            buffer[packetLength++] = 0; // length
            buffer[packetLength++] = 3;
            return true;
        }
    }

    void packetMarkFinal() {
        if (DEBUG) {
            System.out.println("packetMarkFinal()");
        }
        buffer[0] |= 0x80;
    }

    void setPacketType(int type) {
        if (DEBUG) {
            System.out.println("setPacketType()");
        }
        buffer[0] = (byte) type;
    }

    /*
     * Finish packet and send it. Remove Connection ID header if packet also
     * contains TARGET header.
     */
    void packetEndStripConnID() throws IOException {

        // first header id is in 3 byte on all packet except CONNECT
        // and this function is known not to be called for connect() operation.
        if ((buffer[3] & 0xFF) == HeaderSetImpl.TARGET) {
            packetLength -= 5;

            // length of Connection ID packet is 5 bytes:
            //  1 byte header + 4 byte int value
            for (int i = 3; i < packetLength; i++) {
                buffer[i] = buffer[i + 5];
            }
        }
        packetEnd();
    }

    void packetEnd() throws IOException {
        if (DEBUG) {
            System.out.println("packetEnd()");
        }
        moreHeaders = false;

        if (transport == null) {
            throw new IOException("connection error");
        }

        if (dataOpened) {
            // closing Body header
            int len = packetLength - dataOffset;
            buffer[dataOffset+1] = (byte)(len >> 8);
            buffer[dataOffset+2] = (byte)len;
            dataOpened = false;
            dataClosed = true;
        }
        // update packet length field
        buffer[1] = (byte)(packetLength / 0x100);
        buffer[2] = (byte)(packetLength % 0x100);

        if (DEBUG) {
            int len = packetLength;
            if (!DEBUG2 && len > 20) {
                len = 20;
            }
            System.out.println("send:");
            for (int i = 0; i < len; i++) {
                System.out.print(" 0x" + Integer.toHexString(buffer[i] & 0xFF));
                int chr = buffer[i] & 0xFF;
                if (chr >= 32 && chr < 128) {
                    System.out.print("(" + (char)(buffer[i] & 0xFF) + ")");
                }
            }
            if (packetLength != len) {
                System.out.print("...");
            }
            System.out.println("");
        }
        try {
            transport.write(buffer, packetLength);
        } catch (IOException e) {
            brokenLink();
            throw e;
        }
    }

    /*
     * Connection Identifier:
     *     * must be first header in packet
     *     * 0xFFFFFFFF considered invalid - it is up to application
     *     * can't be sent on connect() request
     *     * can't be used with Target header in one packet
     */
    final void packetAddConnectionID(long id, HeaderSetImpl headers) {
        // SPEC: Illegal to send a Connection Id and a Target header
        // in the same operation.
        if (headers != null
                && headers.getHeader(HeaderSetImpl.TARGET) != null) {
            return;
        }
        if (id < 0L || id > 0xFFFFFFFFL) {
            return;
        }
        buffer[packetLength++] = (byte)HEADER_CONNECTION_ID;
        encodeInt(id);
    }

    /*
     * This method is called to handle a situation than header is too large.
     * @throws IOException
     */
    abstract void headerTooLarge() throws IOException;

    /*
     * Adds the specified headers to the packet.
     */
    final void packetAddHeaders(HeaderSetImpl headers)
        throws IOException {
        if (DEBUG) {
            System.out.println("packetAddHeaders()");
        }
        headerOverflow = false;
        newHeader.sendAllQueued();

        if (headers == null) {
            return;
        }

        int[] idList = headers.getHeaderList();

        if (!headers.challenges.isEmpty()) {
            newHeader.sendOrQueue(HEADER_AUTH_CHALLENGE,
                    headers.challenges);
        }

        if (idList == null) {
            return;
        }

        for (int i = 0; i < idList.length; i++) {
            int id = idList[i];
            Object value = headers.getHeader(id);
            newHeader.sendOrQueue(id, value);
        }
    }

    void packetAddAuthResponses() throws IOException {
        try {
            for (int i = 0; i < authResponses.size(); i++) {
                if (DEBUG) {
                    System.out.println(
                            "packetAddAuthResponses(): added response");
                }
                ObexAuth response = (ObexAuth) authResponses.elementAt(i);
                int len = response.replyAuthChallenge(buffer, packetLength,
                        authenticator);
                packetLength += len;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("auth response request too large");
        }
        if (packetLength > maxSendLength) {
            throw new IOException("auth response request too large");
        }
    }

    final void sendPacket(byte[] head, long connectionId,
            HeaderSetImpl headers, boolean allHeaders) throws IOException {
        packetBegin(head);
        packetAddConnectionID(connectionId, headers);
        packetAddAuthResponses();
        packetAddHeaders(headers);
        if (allHeaders && !queuedHeaders.isEmpty()) {
            queuedHeaders.removeAllElements();
            throw new IOException("packet too large for peer");
        }
        packetEnd();
    }

    private final void encodeInt(long val) {
        buffer[packetLength++] = (byte)(val >> 24);
        buffer[packetLength++] = (byte)(val >> 16);
        buffer[packetLength++] = (byte)(val >> 8);
        buffer[packetLength++] = (byte)val;
    }

    /*
     * Reads at least <code>length</code> bytes starting from the given offset
     * into the internal buffer. More than <code>length</code> bytes may be
     * actually read. The calling function must ensure the buffer is large
     * enough to store the entire packet.
     *
     * @param offset starting offset in the destination buffer
     * @param length minimum number of bytes to read
     * @return number of bytes actually read
     * @throws IOException if an I/O error occurs
     */
    private int readLeast(int offset, int length)
	throws IOException {
        if (transport == null) {
            throw new IOException("connection error");
        }
        int read = 0;
        while (read < length) {
            int count = transport.read(cache);
    	    System.arraycopy(cache, 0, buffer, offset + read, count);
            read += count;
            if (read < length) {
                try {
                    Thread.sleep(TRANSPORT_READ_INTERVAL);
                } catch (InterruptedException e) {
                    throw new InterruptedIOException(e.getMessage());
                }
            }
        }
        return read;
    }

    final void recvPacket() throws IOException {
        authResponses.removeAllElements();
        if (transport == null) {
            throw new IOException("connection error");
        }
        try {
	    int read = readLeast(0, 3);
	    packetType = buffer[0] & 0xff;
	    packetLength = ((buffer[1] & 0xff) << 8) + (buffer[2] & 0xff);
	    if (DEBUG) {
		Logging.report(Logging.INFORMATION, 0,
		    "Expecting " + packetLength + " bytes to arrive...");
	    }
	    if (read < packetLength) {
		readLeast(read, packetLength - read);
	    }

            // dump packet:
            if (DEBUG) {
                int len = packetLength;
                if (!DEBUG2 && len > 20) {
                    len = 20;
                }

                System.out.println("recv: ");
                for (int i = 0; i < len; i++) {
                    System.out.print(" 0x"
                            + Integer.toHexString(buffer[i] & 0xFF)
				     .toUpperCase());
                }
                if (len != packetLength) System.out.print("...");
                System.out.println("");
            }
        } catch (IOException e) {
            brokenLink();
            throw e;
        }
    }

    private final void parseHeader(HeaderSetImpl headers)
            throws IOException {
        if (DEBUG) {
            System.out.println("parseHeader()");
        }
        try {
            int headerId = buffer[packetOffset++] & 0xff;
            int inputType = headerId >> 6;
            int outputType = HeaderSetImpl.internalType(headerId);
            if (outputType != HeaderSetImpl.TYPE_UNSUPPORTED) {
                inputType = outputType;
            }

            Object result = null;

            switch (inputType) {
                // ids which require special handling
                case HeaderSetImpl.TYPE_SPECIAL_TIME_ISO:
		    try {
                	result = decodeTime8601();
		    } catch (IOException e) {
			// IMPL_NOTE: Got invalid time header,
			// should probably just ignore it.
		    }
                    break;

                case HeaderSetImpl.TYPE_SPECIAL_TIME_4:
                    long date = decodeInt();
                    Calendar cal = Calendar.getInstance(utcTimeZone);
                    cal.setTime(new Date(date * 1000L));
                    result = cal;
                    packetOffset += 4;
                    break;

                case HeaderSetImpl.TYPE_SPECIAL_TYPE:
                    int len = decodeLength16(packetOffset) - 3;
                    packetOffset += 2;
                    if (buffer[packetOffset + len - 1] != 0) {
                        throw new IOException(
                                "protocol error, "
                                + "type field not null terminated");
                    }
                    result = new String(buffer, packetOffset, len - 1,
                            "ISO-8859-1");
                    packetOffset += len;
                    break;

                    // normal ids
                case HeaderSetImpl.TYPE_LONG:
                    result = new Long(decodeInt());
                    packetOffset += 4;
                    break;

                case HeaderSetImpl.TYPE_UNICODE:
                    len = decodeLength16(packetOffset) - 3;
                    packetOffset += 2;
                    if (len < 2 || buffer[packetOffset + len - 1] != 0
                            || buffer[packetOffset + len - 2] != 0) {
                        throw new IOException("protocol error, " +
                                "unicode string is not null terminated");
                    }
                    result = new String(buffer,
                                   packetOffset, len - 2, "UTF-16BE");
                    // result = new String(buffer, packetOffset, len,
                    //        "ISO-8859-1");
                    packetOffset += len;
                    break;

                case HeaderSetImpl.TYPE_BYTEARRAY:
                    len = decodeLength16(packetOffset) - 3;
                    packetOffset += 2;
                    result = new byte[len];
                    System.arraycopy(buffer, packetOffset, result, 0, len);
                    packetOffset += len;
                    break;

                case HeaderSetImpl.TYPE_BYTE:
                    result = new Byte(buffer[packetOffset++]);
                    break;

                case HeaderSetImpl.TYPE_AUTH_CHALLENGE:
                    len = decodeLength16(packetOffset);
                    ObexAuth response =
                        ObexAuth.parseAuthChallenge(buffer, packetOffset-1,
						    len);
                    if (response != null)
                        authResponses.addElement(response);
                    packetOffset += len - 1;
                    return;

                case HeaderSetImpl.TYPE_AUTH_RESPONSE:
                    len = decodeLength16(packetOffset);
                    boolean good =
                        ObexAuth.checkAuthResponse(buffer, packetOffset-1, len,
                                this, authChallenges);
                    if (good) authFailed = false;
                    packetOffset += len - 1;
                    if (DEBUG) {
                        System.out.println("checkAuthResponse() = " + good);
                    }
                    return;
            }

            if (packetOffset > packetLength) {
                throw new IOException("protocol error");
            }

            if (outputType != HeaderSetImpl.TYPE_UNSUPPORTED) {
                headers.setHeader(headerId, result);
            } else if (DEBUG) {
                System.out.println("unsupported header id = 0x"
                        + Integer.toHexString(headerId).toUpperCase());
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IOException("protocol error");
        }
    }

    /*
     * Called when requested authentication failed.
     * Implemented on server to call handler.
     */
    abstract void onAuthenticationFailure(byte[] username) throws IOException;

    void onMissingAuthResponse() throws IOException {}

    boolean shouldSendAuthResponse() {
        return (packetType == ResponseCodes.OBEX_HTTP_UNAUTHORIZED)
            && (authResponses.size() != 0);
    }

    /*
     * Parser all packet headers, BODY headers should not apear
     * and silently ignored.
     */
    final void parsePacketHeaders(HeaderSetImpl headers,
            int offset) throws IOException {
        if (DEBUG) {
            System.out.println("parsePacketHeaders()");
        }

        packetOffset = offset;
        headers.packetType = buffer[0] & 0xFF;

        parseConnectionID();

        while (packetOffset != packetLength) {
            parseHeader(headers);
        }
        parseEnd();
    }

    private final void parseConnectionID() {

        int headerId = buffer[packetOffset] & 0xFF;
        // parse connection ID
        if (packetOffset + 5 > packetLength
                || headerId != HEADER_CONNECTION_ID) {
            return;
        }
        packetOffset++;
        long id = decodeInt();
        packetOffset += 4;
        setConnectionID(id);
    }

    public abstract void setConnectionID(long id);
    public abstract long getConnectionID();

    /*
     * Begin parsing packet headers in packet possibly containing BODY
     * (data fields).
     */
    final void parsePacketDataBegin(HeaderSetImpl headers, int offset) {
        if (DEBUG) {
            System.out.println("parsePacketDataBegin()");
        }
        packetOffset = offset;
        headers.packetType = buffer[0] & 0xFF;

        parseConnectionID();
        dataOffset = packetOffset;
    }

    /*
     * Parse packet headers, put BODY field content (data) in specified output
     * array. If output is null, search for a BODY block and return 1 if it is
     * found.
     * @return number of bytes put in output.
     */
    final int parsePacketData(HeaderSetImpl headers,
            byte[] output, int outputOffset, int outputLength)
            throws IOException {
        if (DEBUG2) {
            System.out.println("parsePacketData()");
        }
        int result = 0;
        while (true) {
            int len = packetOffset - dataOffset;
            if (DEBUG2) {
                System.out.print("packetOffset = "+packetOffset+
                        " dataOffset = " +dataOffset);
                System.out.println(" len = " + len);
            }
            if (len > 0) {
                if (output == null) {
                    // special case for serching first data block
                    // without actual read
                    return 1;
                }
                if (outputLength == 0) {
		            return result;
                }
                if (len > outputLength) len = outputLength;
                System.arraycopy(buffer, dataOffset,
                        output, outputOffset, len);
                outputOffset += len;
                outputLength -= len;
                dataOffset += len;
                result += len;
                continue;
            }

            if (DEBUG) {
                System.out.println("packetOffset = " + packetOffset
                        +" packetLength = " + packetLength);
            }
            if (packetOffset == packetLength) {
                return result;
            }
            int headerId = buffer[packetOffset] & 0xff;

            if (headerId == HEADER_BODY || headerId == HEADER_EOFBODY) {
                isEof = (headerId == HEADER_EOFBODY);
                dataOffset = packetOffset + 3;
                int length = decodeLength16(packetOffset + 1);
                if (packetOffset + length > packetLength) {
                    throw new IOException("protocol error");
                }
                packetOffset += length;
                continue;
            }

            parseHeader(headers);
            dataOffset = packetOffset;
        }
    }

    final void parseEnd() throws IOException {
        if (DEBUG) {
            System.out.println("parseEnd()");
        }
        if (authFailed) {
            authFailed = false;
            onMissingAuthResponse();
        }
    }

    final int decodeLength16(int off) {
        return ((((int)buffer[off]) & 0xFF) << 8)
            + (((int)buffer[off + 1]) & 0xFF);
    }

    private final long decodeInt() {
        return ((buffer[packetOffset+0]& 0xffl) << 24)
             + ((buffer[packetOffset+1]& 0xffl) << 16)
             + ((buffer[packetOffset+2]& 0xffl) << 8)
             +  (buffer[packetOffset+3]& 0xffl);
    }

    private final Calendar decodeTime8601() throws IOException {
        int year, month, date, hour, minute, second;

        int len = decodeLength16(packetOffset) - 3;
        packetOffset += 2;

        if (len < 15 || len > 16
                || buffer[packetOffset + 8] != 0x54 // 'T'
                || (len == 16 && buffer[packetOffset + 15] != 0x5A)) { // 'Z'
	    packetOffset += len;
            throw new IOException("corrupted time header");
        }
        for (int i = 0; i < 14; i++) {
            if (i == 8) continue;
            int chr = buffer[packetOffset + i] - 0x30; // '0'
            if (chr < 0 || chr > 9) {
		packetOffset += len;
                throw new IOException("corrupted time header");
            }
        }

        year = (buffer[packetOffset+0] - 0x30) * 1000
             + (buffer[packetOffset+1] - 0x30) * 100
             + (buffer[packetOffset+2] - 0x30) * 10
             + (buffer[packetOffset+3] - 0x30);
        month = (buffer[packetOffset+4] - 0x30) * 10
	    + (buffer[packetOffset+5] - 0x30);
        date = (buffer[packetOffset+6] - 0x30) * 10
             + (buffer[packetOffset+7] - 0x30);

        hour = (buffer[packetOffset+9] - 0x30) * 10
             + (buffer[packetOffset+10] - 0x30);
        minute = (buffer[packetOffset+11] - 0x30) * 10
             + (buffer[packetOffset+12] - 0x30);
        second = (buffer[packetOffset+13] - 0x30) * 10
             + (buffer[packetOffset+14] - 0x30);

        // is check validness of time fields required?

        Calendar cal;
	// 'len' value 15 means local time,
	// 16 means UTC (in this case time string has 'Z' suffix)
        if (len == 16) {
            cal = Calendar.getInstance(utcTimeZone);
        } else {
            cal = Calendar.getInstance();
        }
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1); // Calendar.JANUARY = 0
        cal.set(Calendar.DATE, date);

	// ISO 8601 standard uses the 24-hour clock
	// Therefore you use HOUR_OF_DAY not HOUR
        cal.set(Calendar.HOUR_OF_DAY, hour);

        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);

        // set milliseconds to zero since
        // ISO 8601 uses only second precision
	cal.set(Calendar.MILLISECOND, 0);

        packetOffset += len;
        return cal;
    }

    static int validateStatus(int status) {
        switch (status) {
            case ResponseCodes.OBEX_DATABASE_FULL:
            case ResponseCodes.OBEX_DATABASE_LOCKED:
            case ResponseCodes.OBEX_HTTP_ACCEPTED:
            case ResponseCodes.OBEX_HTTP_BAD_GATEWAY:
            case ResponseCodes.OBEX_HTTP_BAD_METHOD:
            case ResponseCodes.OBEX_HTTP_BAD_REQUEST:
            case ResponseCodes.OBEX_HTTP_CONFLICT:
            case ResponseCodes.OBEX_HTTP_CREATED:
            case ResponseCodes.OBEX_HTTP_ENTITY_TOO_LARGE:
            case ResponseCodes.OBEX_HTTP_FORBIDDEN:
            case ResponseCodes.OBEX_HTTP_GATEWAY_TIMEOUT:
            case ResponseCodes.OBEX_HTTP_GONE:
            case ResponseCodes.OBEX_HTTP_INTERNAL_ERROR:
            case ResponseCodes.OBEX_HTTP_LENGTH_REQUIRED:
            case ResponseCodes.OBEX_HTTP_MOVED_PERM:
            case ResponseCodes.OBEX_HTTP_MOVED_TEMP:
            case ResponseCodes.OBEX_HTTP_MULT_CHOICE:
            case ResponseCodes.OBEX_HTTP_NO_CONTENT:
            case ResponseCodes.OBEX_HTTP_NOT_ACCEPTABLE:
            case ResponseCodes.OBEX_HTTP_NOT_AUTHORITATIVE:
            case ResponseCodes.OBEX_HTTP_NOT_FOUND:
            case ResponseCodes.OBEX_HTTP_NOT_IMPLEMENTED:
            case ResponseCodes.OBEX_HTTP_NOT_MODIFIED:
            case ResponseCodes.OBEX_HTTP_OK:
            case ResponseCodes.OBEX_HTTP_PARTIAL:
            case ResponseCodes.OBEX_HTTP_PAYMENT_REQUIRED:
            case ResponseCodes.OBEX_HTTP_PRECON_FAILED:
            case ResponseCodes.OBEX_HTTP_PROXY_AUTH:
            case ResponseCodes.OBEX_HTTP_REQ_TOO_LARGE:
            case ResponseCodes.OBEX_HTTP_RESET:
            case ResponseCodes.OBEX_HTTP_SEE_OTHER:
            case ResponseCodes.OBEX_HTTP_TIMEOUT:
            case ResponseCodes.OBEX_HTTP_UNAUTHORIZED:
            case ResponseCodes.OBEX_HTTP_UNAVAILABLE:
            case ResponseCodes.OBEX_HTTP_UNSUPPORTED_TYPE:
            case ResponseCodes.OBEX_HTTP_USE_PROXY:
            case ResponseCodes.OBEX_HTTP_VERSION:
                return status;
            default:
                return ResponseCodes.OBEX_HTTP_INTERNAL_ERROR;
        }
    }
}
