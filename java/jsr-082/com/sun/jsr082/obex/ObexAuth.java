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
import java.io.UnsupportedEncodingException;
import java.util.Vector;
import javax.obex.Authenticator;
import javax.obex.PasswordAuthentication;

/*
 * Obex protocol authentication functinality.
 */
class ObexAuth {

    /* Debug information, should be false for RR. */
    private static final boolean DEBUG = false;

    private static byte[] column = { (byte)':' };

    String realm;
    boolean userID;
    boolean access;
    byte[] nonce;
    private static int counter = 0;

    // used in prepareChallenge, addChallenge
    private byte[] realm_array;
    private int challengeLength;

    static ObexAuth createChallenge(String realm,
            boolean userID, boolean access) {
        return new ObexAuth(realm, null, userID, access);
    }

    private ObexAuth(String realm, byte[] nonce, boolean userID,
		     boolean access) {
        this.realm = realm;
        this.nonce = nonce;
        this.userID = userID;
        this.access = access;
    }

    /*
     * Prepare challenge before adding to packet.
     * @return length of challenge
     */
    int prepareChallenge() {
        if (challengeLength != 0) {
            return challengeLength;
        }
        try {
            int len = 24;
            realm_array = null;
            if (realm != null) {
                realm_array = realm.getBytes("UTF-16BE");
                len += 3 + realm_array.length;
            }
            challengeLength = len;
        } catch (UnsupportedEncodingException e) {
            if (DEBUG) {
                System.out.println("prepareChallenge(): ERROR, no encoding");
            }
            return 0;
        }
        return challengeLength;
    }

    int addChallenge(byte[] packet, int offset) throws IOException {
        int len = prepareChallenge();

        packet[offset] = (byte)ObexPacketStream.HEADER_AUTH_CHALLENGE;
        packet[offset+1] = (byte) (len >> 8);
        packet[offset+2] = (byte) (len & 255);
        packet[offset+3] = (byte) 0;  // nonce tag (0x0)
        packet[offset+4] = (byte) 16; // nonce len (16) (1 bytes)
        nonce = makeNonce();         // 16 byte of nonce
        if (DEBUG) {
            print("addChallenge: nonce", nonce);
        }
        System.arraycopy(nonce, 0, packet, offset + 5, 16);
        packet[offset+21] = (byte) 1; // options tag (0x1)
        packet[offset+22] = (byte) 1; // options length (1)
        packet[offset+23] = (byte) ((userID ? 1 : 0) + (access ? 0 : 2));
        if (realm != null) {
            int realm_len = realm_array.length;
            packet[offset+24] = (byte) 2; // realm tag (0x2)

            // realm length including encoding (1 byte)
            packet[offset+25] = (byte) (realm_len + 1);
            packet[offset+26] = (byte) 0xFF; // realm encoding UNICODE
            System.arraycopy(realm_array, 0, packet, offset+27, realm_len);
        }
        return len;
    }

    private static byte[] makeNonce() throws IOException {
        SSLWrapper md5 = new SSLWrapper();
        byte[] timestamp = createTimestamp();
        md5.update(timestamp, 0, timestamp.length);
        md5.update(column, 0, 1);
        byte[] privateKey = getPrivateKey();
        byte[] nonce = new byte[16];
        md5.doFinal(privateKey, 0, privateKey.length, nonce, 0);
        return nonce;
    }

    /*
     * Creates timestamp.
     * No strict specification for timestamp generation in OBEX 1.2
     * @return timestamp value
     */
    private static byte[] createTimestamp() {
        long time = System.currentTimeMillis();
        byte[] timestamp = new byte[9];
        timestamp[0] = (byte)(time >> 56);
        timestamp[1] = (byte)(time >> 48);
        timestamp[2] = (byte)(time >> 40);
        timestamp[3] = (byte)(time >> 32);
        timestamp[4] = (byte)(time >> 24);
        timestamp[5] = (byte)(time >> 16);
        timestamp[6] = (byte)(time >> 8);
        timestamp[7] = (byte)(time);

        synchronized (ObexAuth.class) {
            timestamp[8] = (byte)(counter++);
        }
        return timestamp;
    }

    private static byte[] privateKey = null;

    /*
     * Create and return private key.
     * Weak security scheme. Should be rewritten for more secure
     * implementation if used outside emulator.
     */
    private synchronized static byte[] getPrivateKey() throws IOException {
        if (privateKey != null) {
            return privateKey;
        }
        SSLWrapper md5 = new SSLWrapper();
        byte[] keyData = null;

        try {
            keyData = "timestamp = ".getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        md5.update(keyData, 0, keyData.length);
        byte[] timestamp = createTimestamp();
        privateKey = new byte[16];
        md5.doFinal(timestamp, 0, timestamp.length, privateKey, 0);
        return privateKey;
    }

    static void makeDigest(byte[] buffer, int offset, byte[] nonce,
			   byte[] password)
            throws IOException {
        SSLWrapper md5 = new SSLWrapper();
        md5.update(nonce, 0, 16);
        md5.update(column, 0, 1);
        md5.doFinal(password, 0, password.length, buffer, offset);
    }

    static ObexAuth parseAuthChallenge(byte[] buffer, int packetOffset,
				       int length)
            throws IOException {
        if (DEBUG) {
            System.out.println("ObexAuth.parseAuthChallenge()");
        }

        // default values
        boolean readonly = false;
        boolean needUserid = false;
        byte[] nonce = null;
        String realm = null;

        // skiping header type and length
        int offset = packetOffset + 3;
        length += packetOffset;

        // decoding data in buffer
        while (offset < length) {
            int tag = buffer[offset] & 0xFF;
            int len = buffer[offset + 1] & 0xFF;
            offset += 2;


            switch (tag) {
                case 0x0: // nonce
                    if (len != 16 || nonce != null) {
                        throw new IOException("protocol error");
                    }
                    nonce = new byte[16];
                    System.arraycopy(buffer, offset, nonce, 0, 16);
                    if (DEBUG) {
                        print("got challenge: nonce", nonce);
                    }
                    break;
                case 0x1: // options
                    if (len != 1) {
                        throw new IOException("protocol error");
                    }
                    int options = buffer[offset];
                    readonly = ((options & 2) != 0);
                    needUserid = ((options & 1) != 0);
                    break;
                case 0x2: // realm
                    try {
                        int encodingID = buffer[offset] & 0xFF;
                        String encoding = null;
                        if (encodingID == 255) encoding = "UTF-16BE";
                        else if (encodingID == 0) encoding = "US-ASCII";
                        else if (encodingID < 10) encoding = "ISO-8859-"
			    + encoding;
                        else throw new UnsupportedEncodingException();

                        realm = new String(buffer, offset + 1,
                            len - 1, encoding);
                    } catch (UnsupportedEncodingException e) {
                        // already: realm = null;
                    }
            }
            offset += len;
        }
        if (offset != length) {
            throw new IOException("protocol error");
        }
        return new ObexAuth(realm, nonce, needUserid, !readonly);
    }

    int replyAuthChallenge(byte[] buffer, int packetOffset,
            Authenticator authenticator) throws IOException {

        if (DEBUG) {
            System.out.println("ObexAuth.replyAuthChallenge()");
        }

        if (realm == null) {
            realm = "";
        }

        byte[] password = null;
        byte[] username = null;

        try {
            PasswordAuthentication pass =
                authenticator.onAuthenticationChallenge(realm, userID, access);

            password = pass.getPassword();
            int uidLen = 0;
	    // userid subheader length with subheader <tag> and <len>

            username = pass.getUserName();
            if (userID || username != null) {

                // username is required but not provided
                if (userID && username.length == 0) {
                    if (DEBUG) {
                        System.out.println("ObexAuth.replyAuthChallenge():"
                                + " required username not provided");
                    }
                    throw new Exception();
                }
                uidLen = 2 + username.length;

                // maximum supported username length = 20
                if (uidLen > 22) uidLen = 22;
            }

            int len = 39 + uidLen;
            // byte[] response = new byte[len];
            buffer[packetOffset + 0] = (byte)ObexPacketStream
		.HEADER_AUTH_RESPONSE;
            buffer[packetOffset + 1] = (byte) (len >> 8);
            buffer[packetOffset + 2] = (byte) (len & 255);
            buffer[packetOffset + 3] = 0x0; // tag (Request-Digest)
            buffer[packetOffset + 4] = 16;  // digest len (16)
            makeDigest(buffer, packetOffset + 5, nonce, password);
            buffer[packetOffset + 21] = 0x02; // tag nonce
            buffer[packetOffset + 22] = 16;   // nonce len (16)
            System.arraycopy(nonce, 0, buffer, packetOffset + 23, 16);
            if (DEBUG) {
                print("send response: nonce", nonce);
            }
            if (uidLen > 2) {
                buffer[packetOffset + 39] = 0x01; // tag userid
                buffer[packetOffset + 40] = (byte) (uidLen - 2); // userid len
                System.arraycopy(username, 0, buffer, packetOffset + 41,
				 uidLen - 2);
            }

            if (DEBUG) {
                System.out.println("ObexAuth.replyAuthChallenge():"
                        + " response generated");
            }
            return len;

            // need to create authentication response
            // we should resend previous packet with the authentication response

        } catch (Throwable t) {
            if (DEBUG) {
                System.out.println("ObexAuth.replyAuthChallenge(): exception");
                t.printStackTrace();
            }
            // will caught NullPointerException if authenticator
	    //  was not provided
            // will caught exceptions in handler
            // will caught NullPointerException exception
	    //  if username == null and needUserid
            //
            // wrong response from client application,
	    //  ignoring authentication challenge
            // client will receive UNAUTHORIZED
        }
        return 0;
    }

    private static void print(String msg, byte[] array) {
        if (DEBUG) {
            System.out.println(msg);
            if (array == null) {
                System.out.println("[0] = NULL");
                return;
            }
            System.out.print("[" + array.length + "]");
            for (int i = 0; i < array.length; i++) {
                System.out.print(" " + Integer.toHexString(array[i] & 0xFF));
            }
            System.out.println("");
        }
    }

    private static boolean compare(byte[] src1, byte[] src2) {
        for (int i = 0; i < 16; i++) {
            if (src1[i] != src2[i])
		return false;
        }
        return true;
    }

    static boolean checkAuthResponse(byte[] buffer, int packetOffset,
            int length, ObexPacketStream stream, Vector challenges)
            throws IOException {

        if (DEBUG) {
            System.out.println("ObexAuth.parseAuthResponse()");
        }

        // skiping header type and length
        int offset = packetOffset + 3;
        length += packetOffset;

        byte[] digest = null;
        byte[] username = null;
        byte[] nonce = null;

        // decoding data in buffer
        while (offset < length) {
            int tag = buffer[offset] & 0xFF;
            int len = buffer[offset + 1] & 0xFF;
            offset += 2;

            switch (tag) {
                case 0x0: // digest
                    if (DEBUG) {
                        System.out.println("got digest");
                    }
                    if (len != 16 || digest != null) {
                        throw new IOException("protocol error (1)");
                    }
                    digest = new byte[16];
                    System.arraycopy(buffer, offset, digest, 0, 16);
                    if (DEBUG) {
                        print("got response: digest", digest);
                    }
                    break;
                case 0x1: // username
                    if (DEBUG) {
                        System.out.println("got username");
                    }
                    if (len > 20 || len == 0 || username != null) {
                        throw new IOException("protocol error (2)");
                    }
                    username = new byte[len];
                    System.arraycopy(buffer, offset, username, 0, len);
                    break;
                case 0x2: // nonce
                    if (DEBUG) {
                        System.out.println("got nonce");
                    }
                    if (len != 16 || nonce != null) {
                        throw new IOException("protocol error (3)");
                    }
                    nonce = new byte[16];
                    System.arraycopy(buffer, offset, nonce, 0, 16);
                    if (DEBUG) {
                        print("got response: nonce", nonce);
                    }
                    break;
                default:
                    if (DEBUG) {
                        System.out.println("unknown tag = " + tag);
                    }
            }
            offset += len;
        }

        if (offset != length) {
            throw new IOException("protocol error (4)");
        }


        // check nonce and select auth object
        ObexAuth auth = null;

        if (nonce == null) {
            if (DEBUG) {
                System.out.println("no nonce received, using first auth");
            }
            if (challenges.size() == 0) {
                return false;
            }
            auth = (ObexAuth) challenges.elementAt(0);
            nonce = auth.nonce;
            challenges.removeElementAt(0);
        } else {
            if (DEBUG) {
                System.out.println("nonce provided, searching for auth");
                System.out.println("challenges = " + challenges.size());
            }
            for (int i = 0; i < challenges.size(); i++) {
                ObexAuth a = (ObexAuth) challenges.elementAt(i);
                if (compare(nonce, a.nonce)) {
                    if (DEBUG) {
                        System.out.println("nonce is in " + i + " challenge");
                    }
                    auth = a;
                    challenges.removeElementAt(i);
                    break;
                }
            }
            if (DEBUG) {
                System.out.println("auth = " + auth);
            }
            if (auth == null)
		return false;
        }

        // check username existance
        if (auth.userID && username == null) {
            if (DEBUG) {
                System.out.println("need username!");
            }
            // NOTE: may be too strict
            stream.onAuthenticationFailure(username);
            return false;
        }

        // ask password from authenticator and check digest
        try {
            if (DEBUG) {
                System.out.println("running onAuthenticationResponse()...");
            }
            byte[] password =
                stream.authenticator.onAuthenticationResponse(username);
            byte[] localDigest = new byte[16];
            makeDigest(localDigest, 0, nonce, password);
            if (DEBUG) {
                System.out.println("digest created");
            }
            boolean res = compare(localDigest, digest);

            if (res == false) {
                if (DEBUG) {
                    System.out.println("Calling onAuthenticationFailure()..");
                }
                stream.onAuthenticationFailure(username);
            }
            return res;
        } catch (Throwable t) {
            // catch localDigest = null, crypto and user code exception
            if (DEBUG) {
                System.out.println("exception");
            }
            stream.onAuthenticationFailure(username);
            return false;
        }
    }
}
