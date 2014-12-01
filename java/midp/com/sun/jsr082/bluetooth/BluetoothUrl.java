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
package com.sun.jsr082.bluetooth;

import javax.bluetooth.UUID;
import javax.bluetooth.BluetoothConnectionException;
import java.util.Hashtable;

/*
 * Represents a bluetooth url, i.e. connection string.
 * There are two ways of usage. First one is constructing it giving
 * url string in order to parse it into a set of fields. Second one
 * is constructig it giving fields values in order to get string
 * representation. Whenever incompatible url parts are found
 * <code>IllegalArgumentException</code> is thrown.
 */
public class BluetoothUrl {
    /* Indicates if it is a sever connection string. */
    public boolean isServer = false;

    /* Keeps server address for client url, "localhost" for server. */
    public String address = null;
    /* PSM for L2CAP or channel id for RFCOMM. */
    public int port = -1;
    /* Master parameter, true by default for server. */
    public boolean master = false;
    /* Encrypt parameter. */
    public boolean encrypt = false;
    /* Authenticate parameter. */
    public boolean authenticate = false;

    /* Value to indicate L2CAP protocol. */
    public static final int L2CAP = 0;
    /* Value to indicate RFCOMM protocol. */
    public static final int RFCOMM = 1;
    /* Value to indicate OBEX protocol. */
    public static final int OBEX = 2;
    /* Value to indicate unknown protocol. */
    public static final int UNKNOWN = 3;
    /* Indicates protocol type. */
    public int protocol = UNKNOWN;

    /* Amount of protocols supported. */
    private static final int PROTOCOLS_AMOUNT = 3;
    /*
     * Keeps protocols indicating strings.
     * Usage:
     * <code>protocolName[L2CAP]</code> to get "l2cap"
     */
    private static final String[] protocolName =
        { "btl2cap://", "btspp://", "btgoep://" };

    /*
     * Keeps uuid from server connection string,
     * <code>null</code> for client's one.
     * L2CAP, RFCOMM specific.
     */
    public String uuid = null;

    /*
     * Name parameter of server url, <code>null</code> for client's one.
     * L2CAP, RFCOMM specific.
     */
    public String name = null;

    /* Url string to parse, lower case. */
    private String url;
    /*
     * Url string to parse, original case.
     * Required for correct "name" parameter parsing for it is case-sensitive.
     */
    public String caseSensitiveUrl;

    /* Authorize parameter. L2CAP specific. */
    public boolean authorize = false;
    /* RecieveMTU parameter. L2CAP specific. */
    public int receiveMTU = -1;
    /* TransmitMTU parameter. L2CAP specific. */
    public int transmitMTU = -1;

    /* UUID value to create a transport for Service Discovery Protocol. */
    public static final UUID UUID_SDP = new UUID(0x0001);

    /* Indicates if an explicit "authenticate" parameter found. */
    private boolean explicitAuthenticate = false;

    /* Keeps server host string. */
    private static final String LOCALHOST = "localhost";

    /* Keeps length of url. */
    private int length = 0;

    /* Master parameter name. */
    private static final String MASTER = ";master=";
    /* Encrypt parameter name. */
    private static final String ENCRYPT = ";encrypt=";
    /* Authenticate parameter name. */
    private static final String AUTHENTICATE = ";authenticate=";
    /* Authorize parameter name. */
    private static final String AUTHORIZE = ";authorize=";
    /* TransmitMTU parameter name. */
    private static final String TRANSMITMTU = ";transmitmtu=";
    /* ReceiveMTU parameter name. */
    private static final String RECEIVEMTU = ";receivemtu=";
    /* Name parameter name. */
    private static final String NAME = ";name=";

    /* "true" literal. */
    private static final String TRUE = "true";
    /* "false" literal. */
    private static final String FALSE = "false";

    /* the URL parameters. */
    private Hashtable parameters;

    /* Stub object for values in parameters hashtable.*/
    private final static Object on = new Object();

    /* Shows whether this url is generated and validated by SDP routines. */
    private boolean isSystem = false;

    /*
     * Constructs url object by specified url string. Constructing
     * <code>BluetoothUrl</code> in this manner is a way to parse
     * an url represented by string.
     *
     * @param urlString url string.
     */
    public BluetoothUrl(String urlString) {
        this(UNKNOWN, urlString, null);
    }

    /*
     * Constructs url object by specified protocol and url string without
     * leading protocol name and colon or if protocol is unknown by s string
     * that contains full url.
     *
     * @param protocol prootocol type, must be one of
     *        <code>L2CAP, RFCOMM, OBEX, UNKNOWN</code>.
     * @param urlString whole url if <code>protocol</code> value is
     *        <code>UNKNOWN</code>, a part of url string beyond
     *        "protocol:" otherwise.
     */
    public BluetoothUrl(int protocol, String urlString) {
        this(protocol, urlString, null);
    }

    /*
     * Constructs url object with specified protocol, url and special system
     * token.
     * @see BluetoothUrl(int, String)
     *
     * @param protocol prootocol type
     * @param urlString URL
     * @param systemToken special object that validates this URL as system
     *        if has proper value, usually it is <code>null</code>
     */
    public BluetoothUrl(int protocol, String urlString, Object systemToken) {
        this(protocol);

        isSystem = SDP.checkSystemToken(systemToken);
        caseSensitiveUrl = urlString;
        url = urlString.toLowerCase();
        length = url.length();
        int start;
        int separator = url.indexOf(':');

        if (protocol == UNKNOWN) {
            // url is "PROTOCOL://ADDRESS:...", parsing protocol name
            assertTrue(separator > 0, "Cannot parse protocol name: " + url);
            start = separator + 3; // skip "://"
            String name = urlString.substring(0, start);

            for (int i = 0; i < PROTOCOLS_AMOUNT; i++) {
                if (protocolName[i].equals(name)) {
                    this.protocol = i;
                    separator = url.indexOf(':', start);
                    break;
                }
            }

        } else {
            // url is "//ADDRESS:...", parsing protocol name
            assertTrue(urlString.startsWith("//"),
            "address and protocol name have to be separated by //: " + url);
            // skip "//"
            start = 2;
        }

        assertTrue(separator > start, "Cannot parse address: " + url);

        address = url.substring(start, separator);
        start = separator + 1;


        if (this.protocol == L2CAP) {
            // parsing psm or uuid
            if (address.equals(LOCALHOST)) {
                isServer = true;
                // Now uuid goes till end of string or semicolon.
                separator = getSeparator(start);
                uuid = url.substring(start, separator);

             } else {
                // Now psm goes which is represented by 4 hex digits.
                assertTrue((separator = start + 4) <= length,
                "psm has to be represented by 4 hex digits: " + url);
                port = parseInt(start, separator, 16);
             }

        } else if (this.protocol == RFCOMM ||
                   this.protocol == OBEX) {
            separator = getSeparator(start);
            if (address.equals(LOCALHOST)) {
                isServer = true;
                // Now uuid goes till end of string or semicolon.
                uuid = url.substring(start, separator);
            } else {
                // Now channel id goes which is represented by %d1-30.
                assertTrue(separator <= length,
                "channel id has to go after address: " + url);
                port = parseInt(start, separator, 10);
            }
        } else {
            separator = getSeparator(start);
            port = parseInt(start, separator, 16);
        }

        if (isServer) {
            int length;
            assertTrue(uuid != null && (length = uuid.length()) > 0 &&
                length <= 32, "Invalid UUID");
        } else {
            checkBluetoothAddress();
        }

        // parsing parameters
        parameters = new Hashtable();
        for (start = separator; start < length; start = parseParameter(start));
        parameters = null;

        assertTrue(start == length, "Cannot parse the parameters: " + url);
    }

    /*
     * Creates url that represents client connection string.
     *
     * @param protocol identifies protocol. Should be one of
     * <code>
     * BluetoothUrl.L2CAP, BluetoothUrl.RFCOMM, BluetoothUrl.OBEX
     * </code>
     *
     * @param btaddr Bluetooth address of server device.
     *
     * @param port PSM in case of L2CAP or channel id otherwise.
     *
     * @return <code>BluetoothUrl</code> instance that represents
     * desired connection string.
     *
     * @exception IllegalArgument exception if provived parameters are invalid.
     */
    public static BluetoothUrl createClientUrl(int protocol,
                                               String btaddr, int port)
            throws IllegalArgumentException {

        assertTrue(protocol != UNKNOWN && btaddr != null,
        "Either unknown protocol name or address");
        BluetoothUrl url = new BluetoothUrl(protocol);

        url.address = btaddr.toLowerCase();
        url.checkBluetoothAddress();
        url.port = port;

        return url;
    }

    /*
     * Universal private constructor.
     * @param protocol identifies protocol.
     * @exception IllegalArgument exception if provived parameters are invalid.
     */
    private BluetoothUrl(int protocol) {
        assertTrue(protocol <= UNKNOWN, "Unknown protocol name: " + protocol);
        this.protocol = protocol;
    }

    /*
     * Checks url parts consistency and creates string representation.
     * @return string representation of the URL.
     * @exception IllegalArgumentException if URL parts are inconsistent.
     */
    public String toString() {
        assertTrue(protocol == L2CAP ||
                   protocol == RFCOMM ||
                   protocol == OBEX,
                   "Incorrect protocol bname: " + protocol);

        StringBuffer buffer = new StringBuffer();

        buffer = new StringBuffer(getResourceName());
        buffer.append(':');

        if (isServer) {
            buffer.append(uuid);
            buffer.append(AUTHORIZE).append(authorize ? TRUE : FALSE);
        } else {
            String portStr;

            if (protocol == L2CAP) {
                // in case of l2cap, the psm is 4 hex digits
                portStr = Integer.toHexString(port);
                for (int pad = 4 - portStr.length(); pad > 0; pad--) {
                    buffer.append('0');
                }

            } else if (protocol == RFCOMM ||
                       protocol == OBEX) {
                portStr = Integer.toString(port);
            } else {
                portStr = Integer.toString(port);
            }

            buffer.append(portStr);
        }

        /*
         * note: actually it's not required to add the boolean parameter if it
         * equals to false because if it is not present in the connection
         * string, this is equivalent to 'such parameter'=false.
         * But some TCK tests check the parameter is always present in
         * URL string even its value is false.
         * IMPL_NOTE: revisit this code if TCK changes.
         */
        buffer.append(MASTER).append(master ? TRUE : FALSE);
        buffer.append(ENCRYPT).append(encrypt ? TRUE: FALSE);
        buffer.append(AUTHENTICATE).append(authenticate ? TRUE : FALSE);

        if (receiveMTU != -1) {
            buffer.append(RECEIVEMTU).append(
                Integer.toString(receiveMTU, 10));
        }
        if (transmitMTU != -1) {
            buffer.append(TRANSMITMTU).append(
                Integer.toString(transmitMTU, 10));
        }

        return buffer.toString();
    }

    /*
     * Creates string representation of the URL without parameters.
     * @return "PROTOCOL://ADDRESS" string.
     */
    public String getResourceName() {
        assertTrue(protocol == L2CAP ||
                   protocol == RFCOMM ||
                   protocol == OBEX,
                   "Incorrect protocol bname: " + protocol);
        assertTrue(address != null, "Incorrect address: "+ address);
        return protocolName[protocol] + address;
    }

    /*
     * Tests if this URL is system one. System URL can only by created
     * by SDP server or client and is processed in special way.
     *
     * @return <code>true</code> if this url is a system one created
     *         by SDP routines, <code>false</code> otherwise
     */
    public final boolean isSystem() {
        return isSystem;
    }

    /*
     * Checks the string given is a valid Bluetooth address, which means
     * consists of 12 hexadecimal digits.
     *
     * @exception IllegalArgumentException if string given is not a valid
     *             Bluetooth address
     */
    private void checkBluetoothAddress()
            throws IllegalArgumentException {

        String errorMessage = "Invalid Bluetooth address";
        assertTrue(address != null && address.length() == 12 &&
                address.indexOf('-') == -1, errorMessage);

        try {
            Long.parseLong(address, 16);
        } catch (NumberFormatException e) {
            assertTrue(false, errorMessage);
        }
    }

    /*
     * Parses parameter in url starting at given position and cheks simple
     * rules or parameters compatibility. Parameter is ";NAME=VALUE". If
     * parsing from given position or a check fails,
     * <code>IllegalArgumentException</code> is thrown.
     *
     * @param start position to start parsing at, if it does not point to
     *        semicolon, parsing fails as well as instance constructing.
     * @return position number that immediately follows parsed parameter.
     * @exception IllegalArgumentException if parsing fails or incompatible
     *        parameters occured.
     */
    private int parseParameter(int start) throws IllegalArgumentException {
        assertTrue(url.charAt(start) == ';',
                   "Cannot parse url parameters: " + url);

        int separator = url.indexOf('=', start) + 1;
        assertTrue(separator > 0, "Cannot parse url parameters: " + url);
        // name is ";NAME="
        String name = url.substring(start, separator);

        start = separator;
        separator = getSeparator(start);

        assertTrue(!parameters.containsKey(name),
        "Duplicate parameter " + name);
        parameters.put(name, on);

        if (name.equals(MASTER)) {
            master = parseBoolean(start, separator);

        } else if (name.equals(ENCRYPT)) {
            encrypt = parseBoolean(start, separator);
            if (encrypt && !explicitAuthenticate) {
                authenticate = true;
            }

        } else if (name.equals(AUTHENTICATE)) {
            authenticate = parseBoolean(start, separator);
            explicitAuthenticate = true;

        } else if (name.equals(NAME)) {
            assertTrue(isServer, "Incorrect parameter for client: " + name);
            // this parameter is case-sensitive
            this.name = caseSensitiveUrl.substring(start, separator);
            assertTrue(checkNameFormat(this.name),
            "Incorrect name format: " + this.name);

        } else if (name.equals(AUTHORIZE)) {
            assertTrue(isServer,  "Incorrect parameter for client: " + name);
            authorize = parseBoolean(start, separator);
            if (authorize && !explicitAuthenticate) {
                authenticate = true;
            }

        } else if (protocol == L2CAP) {
            if (name.equals(RECEIVEMTU)) {
                receiveMTU = parseInt(start, separator, 10);
                assertTrue(receiveMTU > 0,
                           "Incorrect receive MTU: " + receiveMTU);
            } else if (name.equals(TRANSMITMTU)) {
                transmitMTU = parseInt(start, separator, 10);
                assertTrue(transmitMTU > 0,
                           "Incorrect transmit MTU: " + transmitMTU);
            } else {
                assertTrue(false, "Unknown parameter name = " + name);
            }
        } else {
            assertTrue(false, "Unknown parameter name = " + name);
        }
        return separator;
    }

    /*
     * Checks name format.
     * name = 1*( ALPHA / DIGIT / SP / "-" / "_")
     * The core rules from RFC 2234.
     *
     * @param name the name
     * @return <code>true</code> if the name format is valid,
     *         <code>false</code> otherwise
     */
    private boolean checkNameFormat(String name) {
        char[] a = name.toCharArray();
        boolean ret = a.length > 0;
        for (int i = a.length; --i >= 0 && ret;) {
            ret &= (a[i] >= 'a' && a[i] <= 'z') ||
                (a[i] >= 'A' && a[i] <= 'Z') ||
                (a[i] >= '0' && a[i] <= '9') ||
                (a[i] == '-') ||
                (a[i] == '_') ||
                (a[i] == ' ');
        }
        return ret;
    }

    /*
     * Retrieves position of semicolon in the rest of url string, returning
     * length of the string if no semicolon found. It also assertd that a
     * non-empty substring starts from <code>start</code> given and ends
     * before semicolon or end of string.
     *
     * @param start position in url string to start searching at.
     *
     * @return position of first semicolon or length of url string if there
     * is no semicolon.
     *
     * @exception IllegalArgumentException if there is no non-empty substring
     * before semicolon or end of url.
     */
    private int getSeparator(int start) {
        int separator = url.indexOf(';', start);
        if (separator < 0) {
            separator = length;
        }
        assertTrue(start < separator, "Correct separator is not found");

        return separator;
    }

    /*
     * Parses boolean value from url string.
     *
     * @param start position to start parsing from.
     * @param separator position that immediately follows value to parse.
     *
     * @return true if, comparing case insensitive, specified substring is
     *        "TRUE", false if it is "FALSE".
     * @exception IllegalArgumentException if specified url substring is
     *        neither "TRUE" nor "FALSE", case-insensitive.
     */
    private boolean parseBoolean(int start, int separator)
            throws IllegalArgumentException {
        String value = url.substring(start, separator);
        if (value.equals(TRUE)) {
            return true;
        }

        assertTrue(value.equals(FALSE), "Incorrect boolean parsing: " + value);
        return false;
    }

    /*
     * Parses integer value from url string.
     *
     * @param start position to start parsing from.
     * @param separator position that immediately follows value to parse.
     * @param radix the radix to use.
     *
     * @return integer value been parsed.
     * @exception IllegalArgumentException if given string is not
     *        case-insensitive "TRUE" or "FALSE".
     */
    private int parseInt(int start, int separator, int radix)
            throws IllegalArgumentException {
        int result = -1;

        try {
            result = Integer.parseInt(
                url.substring(start, separator), radix);
        } catch (NumberFormatException e) {
            assertTrue(false, "Incorrect int parsing: " +
                       url.substring(start, separator));
        }

        return result;
    }

    /*
     * Asserts that given condition is true.
     * @param condition condition to check.
     * @param details condition's description details.
     * @exception IllegalArgumentException if given condition is flase.
     */
    private static void assertTrue(boolean condition, String details)
            throws IllegalArgumentException {
        if (!condition) {
            throw new IllegalArgumentException("unexpected parameter: " +
                                               details);
        }
    }
}
