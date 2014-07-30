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

package com.sun.midp.io;

import java.io.IOException;

/**
 * A parsed HTTP (or subclass of) URL. Based on RFC 2396.
 * <p>
 * Handles IPv6 hosts, check host[0] for a "[".
 * Can be used for relative URL's that do not have authorities.
 * Can be used for FTP URL's that do not have the username and passwords.
 * <p>
 * Any elements not specified are represented by null, except a
 * non-specified port, which is represented by a -1.
 */
public class HttpUrl {
    /** Scheme of the URL or null. */
    public String scheme;
    /** Authority (host [port]) of the URL. */
    public String authority;
    /** Path of the URL or null. */
    public String path;
    /** Query of the URL or null. */
    public String query;
    /** Fragment of the URL or null. */
    public String fragment;
    /** hHst of the authority or null. */
    public String host;
    /** Port of the authority or -1 for not specified. */
    public int port = -1;
    /** Machine of the host or null. */
    public String machine;
    /** Domain of the host or null. */
    public String domain;

    /**
     * Construct a HttpUrl.
     *
     * @param url HTTP URL to parse
     *
     * @exception IllegalArgumentException if there is a space in the URL or
     *             the port is not numeric
     */
    public HttpUrl(String url) {
        int afterScheme = 0;
        int length;
        int endOfScheme;

        if (url == null) {
            return;
        }

        length = url.length();
        if (length == 0) {
            return;
        }

        // ":" can mark a the scheme in a absolute URL which has a "//".
        endOfScheme = url.indexOf(':');
        if (endOfScheme != -1) {
            if (endOfScheme == length - 1) {
                // just a scheme
                scheme = url.substring(0, endOfScheme);
                return;
            }

            if (endOfScheme < length - 2 &&
                    url.charAt(endOfScheme + 1) == '/' &&
                    url.charAt(endOfScheme + 2) == '/') {
                // found "://", get the scheme
                scheme = url.substring(0, endOfScheme);
                afterScheme = endOfScheme + 1;
            }
        }

        parseAfterScheme(url, afterScheme, length);
    }

    /**
     * Construct a HttpUrl from a scheme and partial HTTP URL.
     *
     * @param theScheme  the protocol component of an HTTP URL
     * @param partialUrl HTTP URL to parse
     *
     * @exception IllegalArgumentException if there is a space in the URL or
     *             the port is not numeric
     */
    public HttpUrl(String theScheme, String partialUrl) {
        int length;

        scheme = theScheme;

        if (partialUrl == null) {
            return;
        }

        length = partialUrl.length();
        if (length == 0) {
            return;
        }

        parseAfterScheme(partialUrl, 0, length);
    }

    /**
     * Parse the part of the HTTP URL after the scheme.
     *
     * @param url the part of the HTTP URL after the ":" of the scheme
     * @param afterScheme index of the first char after the scheme
     * @param length length of the url
     *
     * @exception IllegalArgumentException if there is a space in the URL or
     *             the port is not numeric
     */
    private void parseAfterScheme(String url, int afterScheme, int length) {
        int start;
        int startOfAuthority;
        int endOfUrl;
        int endOfAuthority;
        int endOfPath;
        int endOfQuery;
        int endOfHost;
        int startOfPort;
        int endOfPort;
        int lastDot;
        int startOfDomain;

        if (url.indexOf(' ') != -1 || url.indexOf('\r') != -1 || 
            url.indexOf('\n') != -1 || url.indexOf('\u0007') != -1) {
            throw new IllegalArgumentException("Space character in URL");
        }

        endOfUrl = length;
        endOfAuthority = endOfUrl;
        endOfPath = endOfUrl;
        endOfQuery = endOfUrl;

        if (url.startsWith("//", afterScheme)) {
            // do not include the "//"
            startOfAuthority = afterScheme + 2;
        } else {
            // no authority, the path starts at 0 and may not begin with a "/"
            startOfAuthority = afterScheme;
        }

        /*
         * Since all of the elements after the authority are optional
         * and they can contain the delimiter of the element before it.
         * Work backwards since we know the end of the last item and will
         * know the end of the next item when find the start of the current
         * item.
         */
        start = url.indexOf('#', startOfAuthority);
        if (start != -1) {
            endOfAuthority = start;
            endOfPath = start;
            endOfQuery = start;

            // do not include the "#"
            start++;

            // do not parse an empty fragment
            if (start < endOfUrl) {
                fragment = url.substring(start, endOfUrl);
            }
        }

        start = url.indexOf('?', startOfAuthority);
        if (start != -1 && start < endOfQuery) {
            endOfAuthority = start;
            endOfPath = start;

            // do not include the "?"
            start++;

            // do not parse an empty query
            if (start < endOfQuery) {
                query = url.substring(start, endOfQuery);
            }
        }

        if (startOfAuthority == afterScheme) {
            // no authority, the path starts after scheme
            start = afterScheme;
        } else {
            // this is not relative URL so the path must begin with "/"
            // can be [IPv6/60].../
            int posSqBr = url.indexOf(']', startOfAuthority);
            if (posSqBr > -1) {
                start = url.indexOf('/', posSqBr);
            } else {
                start = url.indexOf('/', startOfAuthority);
            }
        }

        // do not parse an empty path
        if (start != -1 && start < endOfPath) {
            endOfAuthority = start;

            path = url.substring(start, endOfPath);
        }

        if (startOfAuthority >= endOfAuthority) {
            return;
        }

        authority = url.substring(startOfAuthority, endOfAuthority);
        endOfPort = authority.length();

        // get the port first, to find the end of the host

        // IPv6 address have brackets around them and can have ":"'s
        start = authority.indexOf(']');
        if (start == -1) {
            startOfPort = authority.indexOf(':');
        } else {
            startOfPort = authority.indexOf(':', start);
        }

        if (startOfPort != -1) {
            endOfHost = startOfPort;

            // do not include the ":"
            startOfPort++;

            // do not try parse an empty port
            if (startOfPort < endOfPort) {
                try {
                    port = Integer.parseInt(authority.substring(
                                            startOfPort,
                                            endOfPort));

                    if (port < 0) {
                        throw new
                            IllegalArgumentException("invalid port format");
                    }

                    if (port == 0 || port > 0xFFFF) {
                        throw new IllegalArgumentException(
                            "port out of legal range");
                    }
                } catch (NumberFormatException nfe) {
                    throw new IllegalArgumentException("invalid port format");
                }
            }
        } else {
            endOfHost = endOfPort;
        }

        // there could be a port but no host
        if (endOfHost < 1) {
            return;
        }

        // get the host
        host = authority.substring(0, endOfHost);
        // the last char of the host must not be a minus sign or period
        int hostLength = host.length();
        if ((host.lastIndexOf('.') == hostLength - 1) 
            || (host.lastIndexOf('-') == hostLength - 1)) {
             throw new IllegalArgumentException("invalid host format");
        } 
        
        // find the machine name and domain, if not host is not an IP address
        if (host.charAt(0) == '[') {
            if (!isValidIPv6Address(host)) {
                throw new IllegalArgumentException("invalid IPv6 format");
            }
            return;
        }

        if (Character.isDigit(host.charAt(0))) {
            if (!isValidIPv4Address(host)) {
                throw new IllegalArgumentException("invalid IPv4 format");
            }
            return;
        }

        if (!isValidHostName(host)) {
            throw new IllegalArgumentException("invalid host format");
        }

        startOfDomain = host.indexOf('.');
        if (startOfDomain != -1) {
            // do not include the "."
            startOfDomain++;
            if (startOfDomain < host.length()) {
                domain = host.substring(startOfDomain, host.length());
            }

            machine = host.substring(0, startOfDomain - 1);
        } else {
            machine = host;
        }
    }

    /**
     * Adds a base URL to this URL if this URL is a relative one.
     * Afterwards this URL will be an absolute URL.
     *
     * @param baseUrl an absolute URL
     *
     * @exception IllegalArgumentException if there is a space in the URL or
     *             the port is not numeric
     * @exception IOException if an I/O error occurs processing the URL
     */
    public void addBaseUrl(String baseUrl) throws IOException {
        addBaseUrl(new HttpUrl(baseUrl));
    }

    /**
     * Adds a base URL to this URL if this URL is a relative one.
     * Afterwards this URL will be an absolute URL.
     *
     * @param baseUrl a parsed absolute URL
     */
    public void addBaseUrl(HttpUrl baseUrl) {
        String basePath;

        if (authority != null) {
            return;
        }

        scheme = baseUrl.scheme;
        authority = baseUrl.authority;

        if (path == null) {
            path = baseUrl.path;
            return;
        }

        if (path.charAt(0) == '/' || baseUrl.path == null ||
               baseUrl.path.charAt(0) != '/') {
            return;
        }

        // find the base path
        basePath = baseUrl.path.substring(0, baseUrl.path.lastIndexOf('/'));

        path = basePath + '/' + path;
    }

    /**
     * Converts this URL into a string.
     *
     * @return string representation of this URL
     */
    public String toString() {
        StringBuffer url = new StringBuffer();

        if (scheme != null) {
            url.append(scheme);
            url.append(':');
        }

        if (authority != null || scheme != null) {
            url.append('/');
            url.append('/');
        }

        if (authority != null) {
            url.append(authority);
        }

        if (path != null) {
            url.append(path);
        }

        if (query != null) {
            url.append('?');
            url.append(query);
        }

        if (fragment != null) {
            url.append('#');
            url.append(fragment);
        }

        return url.toString();
    }

    /**
     * Checks is IPv6 address has a valid format.
     *
     * @param address the string representation of IPv6 address
     * @return true when IPv6 address has valid format else false
     */
    private boolean isValidIPv6Address(String address) {
        int addressLength = address.length();
        if (addressLength < 4) { // empty IPv6
            return false;
        }
        if (address.charAt(0) != '[' || address.charAt(addressLength - 1) != ']') {
            return false;
        }
        String IPv6 = address.substring(1, addressLength - 1);
        // Format according to RFC 3513
        int IPv6Length = addressLength - 2;
        int ptrChar = 0;
        int numHexPieces = 0; // number of 16-bit pieces in the address
        char currChar = 0;
        String hexString = null;
        String separator = null;
        int length;
        boolean isDoubleColon = false;
        boolean lastSeparator = true;
        while (ptrChar < IPv6Length) {
            currChar = IPv6.charAt(ptrChar);
            if (isHex(currChar)) {
                hexString = getNextHexValue(IPv6, ptrChar, false);
                length = hexString.length();
                if (length > 4) { // 16-bit value couldn't contain more than 4 digits
                    return false;
                }
                ptrChar += length;
                lastSeparator = false;
            } else if (currChar == ':') { // colon
                if (++numHexPieces > 7) { // more than 8 hex pieces
                    return false;
                }
                separator = getNextHexValue(IPv6, ptrChar, true);
                length = separator.length();
                if (separator.equals("::")) { // double colon
                    // double colon
                    if (isDoubleColon) { // double colon twice
                        return false;
                    }
                    isDoubleColon = true;
                } else if (length > 1) { // wrong separator
                    return false;
                } else  { // separator is equal ":"
                    if (ptrChar == 0) { // first symbol is ":"
                        return false;
                    }
                    if (isDoubleColon && numHexPieces > 6) { // no more 7 pieces when "::"
                        return false;
                    }
                }
                ptrChar += length;
                lastSeparator = true;
            } else if (currChar == '.') { // IPv4 suffix
                if (hexString == null || !isDecimal(hexString)) {
                    // previous hex piece must be start of IPv4
                    return false;
                }
                if (!((!isDoubleColon && numHexPieces == 6) || (isDoubleColon && numHexPieces < 6))) {
                    return false;
                }
                ptrChar -= hexString.length();
                return isValidIPv4Address(IPv6.substring(ptrChar));
            } else if (currChar == '/') { // bit prefix
                break;
            } else { // wrong symbol
                return false;
            }
        } // end of while
        if (lastSeparator && separator.equals(":")) {
            return false;
        }
        if (!((!isDoubleColon && numHexPieces == 7) || (isDoubleColon && numHexPieces < 7))) {
            return false;
        }
        if (currChar == '/') { // bit prefix
            ptrChar++;
            String decString = getNextDecValue(IPv6, ptrChar, false);
            length = decString.length();
            if (length == 0 || ptrChar + length < IPv6Length) {
                return false;
            }
            int i = Integer.parseInt(decString);
            if (i < 1 || i > 128) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks is IPv4 address has a valid format.
     *
     * @param address the string representation of IPv4 address
     * @return true when IPv4 address has valid format else false
     */
    private boolean isValidIPv4Address(String address) {
        if (address.length() < 7) { // less than 0.0.0.0
            return false;
        }
        int IPv4Length = address.length();
        int ptrChar = 0;
        int numDecPieces = 0; // number of 8-bit pieces in the address
        char currChar;
        String decString;
        String separator;
        int length, value;
        boolean lastSeparator = true;
        while (ptrChar < IPv4Length) {
            currChar = address.charAt(ptrChar);
            if (Character.isDigit(currChar)) {
                decString = getNextDecValue(address, ptrChar, false);
                value = Integer.parseInt(decString);
                if (value < 0 || value > 255) {
                    return false;
                }
                ptrChar += decString.length();
                lastSeparator = false;
            } else if (currChar == '.') {
                if (++numDecPieces > 3) { // more than 4 hex pieces
                    return false;
                }
                separator = getNextDecValue(address, ptrChar, true);
                length = separator.length();
                if (length > 1) {
                    return false;
                }
                ptrChar += length;
                lastSeparator = true;
            } else { // wrong symbol
                return false;
            }
        } // end of while
        if (lastSeparator || numDecPieces < 3) {
            return false;
        }
        return true;
    }

    /**
     * Checks is host name has a valid format (RFC 2396).
     *
     * @param hose the host name for checking
     * @return true when the host name has a valid format
     */
    private boolean isValidHostName(String host) {
        char currChar;
        int ptrChar = 0;
        int lenDomain = 0;
        while (ptrChar < host.length()) {
            currChar = host.charAt(ptrChar++);
            if (currChar == '.') {
                if (lenDomain == 0) {
                    return false;
                }
                lenDomain = 0;
            } else if (currChar == '-' || Character.isDigit(currChar)) {
                if (lenDomain == 0) {
                    return false;
                }
                lenDomain++;
            } else if (Character.isLowerCase(currChar) || Character.isUpperCase(currChar)) {
                lenDomain++;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks is the next symbol is hex.
     *
     * @param sym the given symbol
     * @return true when symbol is hex
     */
    private boolean isHex(char sym) {
        return (Character.isDigit(sym) || "ABCDEFabcdef".indexOf(sym) > -1);
    }

    /**
     * Gets the next hex substring.
     *
     * @param str the source string
     * @param offset the start index of substring
     * @param isSeparator false when we need hex value else separator
     * @return the hex substring or separator
     */
    private String getNextHexValue(String str, int offset, boolean isSeparator) {
        StringBuffer strOut = new StringBuffer();
        int length = str.length();
        int i = offset;
        char sym;
        while (i < length) {
            sym = str.charAt(i++);
            if ((!isHex(sym) && !isSeparator) || ((isHex(sym) || sym == '/') && isSeparator)) {
                break;
            }
            strOut.append(sym);
        }
        return strOut.toString();
    }

    /**
     * Gets the next decimal substring.
     *
     * @param str the source string
     * @param offset the start index of substring
     * @param isSeparator false when we need dec value else separator
     * @return the dec substring or separator
     */
    private String getNextDecValue(String str, int offset, boolean isSeparator) {
        StringBuffer strOut = new StringBuffer();
        int length = str.length();
        int i = offset;
        char sym;
        while (i < length) {
            sym = str.charAt(i++);
            if ((!Character.isDigit(sym) && !isSeparator) || (Character.isDigit(sym) && isSeparator)) {
                break;
            }
            strOut.append(sym);
        }
        return strOut.toString();
    }

    /**
     * Checks is the given string contains decimal symbols only.
     *
     * @param str the source string
     * @return true when all symbols are dacimal else false
     */
    private boolean isDecimal(String str) {
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }

        }
        return true;
       
    }
}
