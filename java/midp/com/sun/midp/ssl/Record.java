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

package com.sun.midp.ssl;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

import com.sun.midp.crypto.*;
import com.sun.midp.pki.*;

/**
 * Implements an SSL record layer that sits atop a TCP connection
 * and beneath the user-visible interface to an SSL socket. It 
 * maintains all the state information necessary to encode/decode
 * application data.
 */
class Record {
    /*
     * IMPL_NOTE: We should try to avoid multiple buffer copies by defining a 
     * new message class that allocates a large enough buffer to begin with 
     * so new headers can be prepended and MAC/padding can be appended by 
     * simple pointer manipulation.
     */ 

    /*
     * SSLRecord types: CCS (Change Cipher Spec), ALRT (Alert),
     * HNDSHK (Handshake) and APP (Application Data)
     */
    /** Change Cipher Spec (20). */ 
    static final byte CCS    = 20;
    /** Alert (21). */
    static final byte ALRT   = 21;
    /** Handshake (22). */
    static final byte HNDSHK = 22;
    /** Application data (23). */
    static final byte APP    = 23;

    // There are two severity levels for alerts
    /** Warning severity level for alerts (1). */
    static final byte WARNING     = 1;
    /** Fatal severity level for alerts (2). */
    static final byte FATAL       = 2; 
    
    // Next, we have various Alert types
    /** Close notification alert type (0). */
    static final byte CLOSE_NTFY  = 0;
    /** Unexpected message alert type (10). */
    static final byte UNEXP_MSG   = 10;
    /** Bad MAC alert type (20). */
    static final byte BAD_MAC     = 20;
    // static final byte DECOMP_FAIL = 30;  we do not support compression
    /** Handshake failure alert type (40). */
    static final byte HNDSHK_FAIL = 40;
    /** No certificate found alert type (41). */
    static final byte NO_CERT     = 41;
    /** Bad certificate alert type (42). */
    static final byte BAD_CERT    = 42;
    /** Unsupported certificate alert type (43). */
    static final byte UNSUP_CERT  = 43;
    /** Certificate revoked alert type (44). */
    static final byte CERT_REVKD  = 44;
    /** Certificate expired alaert type (45). */
    static final byte CERT_EXPRD  = 45;
    /** Unknown certificate feature alert type (46). */
    static final byte CERT_UNKWN  = 46;
    /** Bad parameter alert type (47). */
    static final byte BAD_PARAM   = 47;
    
    /*
     * Possible roles for this SSL record layer (client or server).
     */
    /** Server role for SSL record layout (0). */ 
    static final byte SERVER    = 0; 
    /** Client role for SSL record layout (1). */
    static final byte CLIENT    = 1;  
    
     /** Size of record header */
    private final int HEADER_SIZE = 5;
    /** Underlying input stream beneath the record layer. */
    private InputStream in; 
    /** Underlying output stream beneath the record layer. */
    private OutputStream out;
    
    // Connection state information
    /** Flag indicating change cipher spec received. */
    private byte rActive = 0; 
    /** Flag indicating change cipher spec has been sent. */
    private byte wActive = 0; 

    /** The SSL version in one byte (0x30=3.0). */
    private byte ver;
    /** Current input record header. */
    private byte[] inputHeader = new byte[HEADER_SIZE];
    /** How many bytes of the record header have been read. */
    private int headerBytesRead;
    /** How many bytes of the data in the record. */
    private int dataLength;
    /** How many bytes of the data in the record have been read. */
    private int dataBytesRead;
    /** Shutdown flag, true if connection has been shutdown. */
    private boolean shutdown;

    /** Current input record data. */
    byte[] inputData;
    /** Length of the plain text in the input buffer */
    int plainTextLength;

    /** Records encoder */
    private RecordEncoder encoder = null;
    /** Records decoder */
    private RecordDecoder decoder = null;
    
    
    /**
     * Creates a new SSL record layer.
     * 
     * @param ins  input stream belonging to the underlying TCP connection
     * @param outs output stream belonging to the underlying TCP connection
     */
    Record(InputStream ins, OutputStream outs) {
        in = ins;
        out = outs;
        ver = (byte) 0x30;  // IMPL_NOTE: This is hardcoded for now
    }
        
        
    /**
     * Chops up a master secret into the client and server MAC secrets, 
     * bulk encryption keys and IVs. Also initializes the Cipher and 
     * MessageDigest objects used in record encoding/decoding. 
     * 
     * @param role role (either CLIENT or SERVER) of this side in the SSL 
     *             negotiation
     * @param clientRand 32-byte random value chosen by the client
     * @param serverRand 32-byte random value chosen by the server
     * @param suite negotiated cipher suite
     * @param masterSecret master secret resulting from the key exchange
     *
     * @exception Exception if the negotiated cipher suite involves an 
     *                      unsupported hash or cipher algorithm
     */
    void init(byte role, byte[] clientRand, byte[] serverRand, 
              byte suite, byte[] masterSecret) throws Exception {
        CipherSuiteData data = new CipherSuiteData(suite);
        data.generateKeys(clientRand, serverRand, masterSecret);

        // depending on role, we choose corresponding MAC secrets 
        // and cipher bulk keys for encoder and decoder
        byte[] encodeSecret;
        byte[] decodeSecret;
        SecretKey decodeCipherKey;
        SecretKey encodeCipherKey;
        if (role == CLIENT) {
            encodeSecret = data.getClientMACSecret();
            decodeSecret = data.getServerMACSecret();
            encodeCipherKey = data.getClientBulkKey();
            decodeCipherKey = data.getServerBulkKey();
        } else {
            encodeSecret = data.getServerMACSecret();
            decodeSecret = data.getClientMACSecret();
            encodeCipherKey = data.getServerBulkKey();
            decodeCipherKey = data.getClientBulkKey();
        }
        
        Cipher encodeCipher = data.getEncodeCipher();
        encodeCipher.init(Cipher.ENCRYPT_MODE, encodeCipherKey);
        Cipher decodeCipher = data.getDecodeCipher();
        decodeCipher.init(Cipher.DECRYPT_MODE, decodeCipherKey);
        
        encoder = new RecordEncoder(data.getEncodeDigest(), encodeSecret,
                data.getPadLength(), encodeCipher);
        decoder = new RecordDecoder(data.getDecodeDigest(), decodeSecret,
                data.getPadLength(), decodeCipher);
    }
            
            
    /**
     * Reads and returns a record (including the 5-byte header) of 
     * the specified type. If the caller asks for application data 
     * and a close_notify warning alert is found as the next available
     * record, this method sets plainTextLength to -1 to signal the end of the 
     * input stream.
     *
     * @param block if true the method will not return until data is available,
     *              or end of stream
     * @param type desired SSL record type
     *
     * @exception IOException if an unexpected record type or SSL alert is
     *                        found in the underlying sockets input stream 
     */ 
    void rdRec(boolean block, byte type) throws IOException {
        if (!rdRec(block)) {
            return;
        }

        if (inputHeader[0] == type) {
            // success
            return;
        }

        // Signal end of stream.
        plainTextLength = -1;

        switch (inputHeader[0]) {
        case CCS: 
            // Can change_cipher_spec can be passed to handshake clients?
            // if (type == HNDSHK) return r; // fall through otherwise
        case HNDSHK:
        case APP:
        default:
            alert(FATAL, UNEXP_MSG);
            throw new IOException("Unexpected SSL record, type: " +
                                  inputHeader[0]);
            
        case ALRT:
            // An Alert record needs to be atleast 2 bytes of data
            if (inputData.length < 2) {
                throw new IOException("Bad alert length");
            }

            if ((inputData[0] == WARNING) && 
                (inputData[1] == CLOSE_NTFY) && 
                (type == APP)) {
                /*
                 * We got a close_notify warning
                 * Shutdown the connection.
                 */ 
                shutdownConnection();
                return;  // signal end of InputStream
            }

            if ((inputData[0] < WARNING) || (inputData[0] > FATAL)) {
                throw new IOException("Bad alert level");
            }

            throw new IOException("Alert (" + inputData[0] + 
                                  "," + inputData[1] + ")");
        }
    }
            
    /**
     * Returns the next record read from the record layer (the 5-byte
     * SSL record header is included). Set plainTextLength to length of the
     * record or -1 for end of stream.
     *
     * @param block if true the method will not return until data is available,
     *              or end of stream
     *
     * @return true if a record has been read
     *
     * @exception IOException if an I/O error occurs
     */ 
    private boolean rdRec(boolean block) throws IOException {
        int b;

        plainTextLength = 0;

        if (!block && in.available() == 0) {
            return false;
        }

        /*
         * This method could have returned last time after reading the
         * part of the record, in that case headerBytesRead will not be 0.
         */
        if (headerBytesRead == 0) {
            b = in.read(inputHeader, 0, 1);
            if (b == -1) {
                /*
                 * Peer closed SSL connection without close_notify
                 */ 
                plainTextLength = -1;
                return false;
            }

            headerBytesRead = 1;
            dataBytesRead = 0;
            dataLength = 0;
        }

        while (headerBytesRead < inputHeader.length) {
            if (!block && in.available() == 0) {
                return false;
            }

            b = in.read(inputHeader, headerBytesRead,
                        inputHeader.length - headerBytesRead);
            if (b == -1) {
                throw new IOException("SSL connection ended abnormally " +
                                      "while reading record header");
            }

            headerBytesRead += b;
        }

        /*
         * This method could have returned last time after reading the
         * header but not all of the data, in that case dataLength
         * will not be 0.
         */
        if (dataLength == 0) {
            // Check record type and version
            if ((inputHeader[0] < CCS) || 
                (inputHeader[0] > APP) ||
                (inputHeader[1] != (byte) (ver >>> 4)) ||
                (inputHeader[2] != (byte) (ver & 0x0f))) {
                alert(FATAL, UNEXP_MSG);
                throw new IOException("Bad record type (" + inputHeader[0] + 
                                  ") or version (" + inputHeader[1] + "." +
                                  inputHeader[2] + ")");
            }
        
            dataLength = ((inputHeader[3] & 0xff) << 8) + 
                (inputHeader[4] & 0xff);
            inputData = new byte[dataLength];
        }

        while (dataBytesRead < dataLength) {
            if (!block && in.available() == 0) {
                return false;
            }

            b = in.read(inputData, dataBytesRead, dataLength - dataBytesRead);
            if (b == -1) {
                throw new IOException("SSL connection ended abnormally " +
                                      "after reading record byte " +
                                      (dataBytesRead + headerBytesRead));
            }

            dataBytesRead += b;
        }

        if (rActive == 1) {
            try {
                plainTextLength = decoder.decode(inputHeader, inputData);
            } catch (IOException e) {
                if (e.getMessage().compareTo("Bad MAC") == 0) {
                    alert(FATAL, BAD_MAC);
                } else {
                    throw e;
                }
            }
        } else {
            plainTextLength = dataBytesRead;
        }

        if (inputHeader[0] == CCS) {
            rActive = 1;
        }

        // start with a new record header next time
        headerBytesRead = 0;

        return true;
    }
            
    /**
     * Writes an SSL record to the underlying socket's output stream.
     * 
     * @param type record type (one of CCS, ALRT, HNDSHK or APP)
     * @param buf byte array containing the record body (i.e. everything
     * but the 5-byte header)
     * @param off starting offset of the record body inside buf
     * @param len length of the record body, the maximum is 2^14 +2048 as
     *            defined by RFC 2246
     *
     * @exception IOException if an I/O error occurs.
     */
    /*
     * REVISIT: We currently do not handle fragmentation and only 
     * increment sequence numbers when encoding/decoding are 
     * turned on. Is it necessary to maintain these counts for
     * handshake messages as well???
     */ 
    void wrRec(byte type, byte[] buf, int off, int len) throws IOException {
        byte[] rec;

        if (shutdown) {
            throw new IOException("Server has shutdown the connection");
        }

        /*
         * Create a new byte array with room for the header and
         * fill the record header with type, version and length
         */
        rec = new byte[len + 5];
        rec[0] = type;
        rec[1] = (byte) (ver >>> 4);
        rec[2] = (byte) (ver & 0x0f);
        rec[3] = (byte) (len >>> 8);
        rec[4] = (byte) (len & 0xff);
        // Fill the rest of the record
        System.arraycopy(buf, off, rec, 5, len);
        if (wActive == 1) {
            out.write(encoder.encode(rec));
        } else {
            out.write(rec);
        }
        if (type == CCS) wActive = 1;
    }       
            
    /**
     * Sends an alert message of the specified level and type to the SSL peer.
     * 
     * @param level one of WARNING or FATAL)
     * @param type one of CLOSE_NTFY, UNEXP_MSG, BAD_MAC, DECOMP_FAIL,
     *             HNDSHK_FAIL, NO_CERT, BAD_CERT, UNSUP_CERT, CERT_REVKD,
     *             CERT_EXPRD, CERT_UNKWN, BAD_PARAM
     */ 
    public void alert(byte level, byte type) {
        byte[] tmp = new byte[2];
        tmp[0] = level;
        tmp[1] = type;

        try {
            wrRec(ALRT, tmp, 0, 2);
        } catch (IOException e) {
            // ignore, we do not want to step on the real error
        }
    }

    /** Close input stream */
    void closeInputStream()  {
        try {
            in.close();
        } catch (IOException e) {
            // ignore
        }
    }

    /** Close output stream */
    void closeOutputStream()  {
        try {
            out.close();
        } catch (IOException e) {
            // ignore
        }
    }

    /**
     * Send a close notify and shutdown the TCP connection if needed.
     */
    public void shutdownConnection() {
        if (shutdown) {
            return;
        }

        alert(Record.WARNING, Record.CLOSE_NTFY);
        shutdown = true;
        closeOutputStream();
        closeInputStream();
    }
}

/**
 * All kinds of data related to cipher suite like keys,
 * digestes, etc.
 */
class CipherSuiteData {
    /** Client MAC secret. */
    private byte[] clientMACSecret = null;
    /** Server MAC secret. */       
    private byte[] serverMACSecret = null; 
    /** Client write key for bulk encryption. */
    private byte[] clientKey = null;
    /** Server write key for bulk encryption. */
    private byte[] serverKey = null;
    /** Client write IV for block encryption. */
    private byte[] clientIV = null;
    /** Server write IV for block encryption. */
    private byte[] serverIV = null;
    /** Clients bulk encryption secret key. */
    private SecretKey clientBulkKey = null;
    /** Servers bulk encryption secret key. */
    private SecretKey serverBulkKey = null;
    /** Length of the digest */
    private int digestLength = 0;
    /** Digest for encoding */
    private MessageDigest encodeDigest = null;
    /** Digest for decoding */
    private MessageDigest decodeDigest = null;
    /** Encode cipher */
    private Cipher encodeCipher = null;
    /** Decode cipher */
    private Cipher decodeCipher = null;
    /** Length of PAD1/PAD2 used in MACs. */
    private int padLength = 0;
    /** Cipher suite type */
    private byte suiteType = 0;
    /** Digest used for keys generation */
    private MessageDigest md = null;
    /** Digest used for keys generation */
    private MessageDigest sd = null;
    /** Block of generated keys */
    private byte[] keyBlock = null;

    /**
     * Constructs CipherSuiteData object
     * 
     * @param suite negotiated cipher suite
     *
     * @exception Exception if the negotiated cipher suite involves an 
     *                      unsupported hash or cipher algorithm
     */       
    CipherSuiteData(byte suite) throws Exception {
        suiteType = suite;
        
        /* 
         * The actual size of our computed key block is the closest 
         * 16-byte multiple and depends on the choice of hashing
         * and encryption algorithms.
         */
        int keyMaterial = 5;

        switch (suite) {
        case Handshake.ARCFOUR_128_MD5:
            keyMaterial = 16;
            // fall through
        case Handshake.ARCFOUR_40_MD5:
            padLength = 48;
            encodeDigest = MessageDigest.getInstance("MD5");
            break;
    
        case Handshake.ARCFOUR_128_SHA:
            keyMaterial = 16;
            padLength = 40;
            encodeDigest = MessageDigest.getInstance("SHA-1");
            break;

        default:
            throw new Exception("Unsupported suite");
        }

        decodeDigest = (MessageDigest)encodeDigest.clone();
        digestLength = encodeDigest.getDigestLength();

        encodeCipher = Cipher.getInstance("ARC4");
        decodeCipher = Cipher.getInstance("ARC4");
        
            
        /* 
         * Key block size is the closest 16-byte multiple larger than
         *       2x(hash_size + key_material + IV_size)
         * int keyMaterial = CipherSuite.cipherList[
         * CipherSuite.suiteList[suite][2]][2];
         * int ivSize = CipherSuite.cipherList[
         *          CipherSuite.suiteList[suite][2]][4];
         */

        int ivSize = 0; // stream ciphers do not use IVs
            
        int blockSize = (digestLength + keyMaterial + ivSize) << 1;
        blockSize = ((blockSize + 15) >>> 4) << 4;
        keyBlock = new byte[blockSize];

        clientMACSecret = new byte[digestLength];
        serverMACSecret = new byte[digestLength];
        clientKey = new byte[keyMaterial];
        serverKey = new byte[keyMaterial];
        clientIV = new byte[ivSize];
        serverIV = new byte[ivSize];

        md = MessageDigest.getInstance("MD5");
        sd = MessageDigest.getInstance("SHA-1");
    }

    /**
     * Chops up a master secret into the client and server MAC secrets, 
     * bulk encryption keys and IVs.
     * 
     * @param clientRand 32-byte random value chosen by the client
     * @param serverRand 32-byte random value chosen by the server
     * @param masterSecret master secret resulting from the key exchange
     *
     * @exception GeneralSecurityException thrown in case of failure
     */
    void generateKeys(byte[] clientRand, byte[] serverRand, 
                      byte[] masterSecret) 
            throws GeneralSecurityException {
        generateKeysBlock(clientRand, serverRand, masterSecret);
        chopKeysBlock(clientRand, serverRand, masterSecret);
        keyBlock = null;
        md = null;
        sd = null;
    }

    
    /**
     * Get client MAC secret
     * @return client MAC secret
     */
    byte[] getClientMACSecret() {
        return clientMACSecret;
    }
    
    /**
     * Get server MAC secret
     * @return server MAC secret
     */
    byte[] getServerMACSecret() {
        return serverMACSecret;
    }
        
    /**
     * Get client bulk key
     * @return client bulk key
     */
    SecretKey getClientBulkKey() {
        return clientBulkKey;
    }

    /**
     * Get server bulk key
     * @return server bulk key
     */
    SecretKey getServerBulkKey() {
        return serverBulkKey;
    }

    /**
     * Get digest used for encoding 
     * @return encode digest
     */
    MessageDigest getEncodeDigest() {
        return encodeDigest;
    }

    /**
     * Get digest used for decoding
     * @return decode digest
     */
    MessageDigest getDecodeDigest() {
        return decodeDigest;
    }    

    /**
     * Get cipher used for encoding
     * @return encode cipher
     */
    Cipher getEncodeCipher() {
        return encodeCipher;
    }

    /**
     * Get cipher used for decoding
     * @return decode cipher
     */
    Cipher getDecodeCipher() {
        return decodeCipher;
    }
        

    /**
     * Get pad length used for MAC computation 
     * @return pad length
     */
    int getPadLength() {
        return padLength;
    }    


    /**
     * Generates keys block
     * 
     * @param clientRand 32-byte random value chosen by the client
     * @param serverRand 32-byte random value chosen by the server
     * @param masterSecret master secret resulting from the key exchange
     *
     * @exception GeneralSecurityException thrown in case of failure
     */    
    private void generateKeysBlock(byte[] clientRand, byte[] serverRand, 
                                   byte[] masterSecret)
            throws GeneralSecurityException {
        /* 
         * The following should suffice to generate a total of
         * 16*7 = 112 bytes of key material. 3DES_SHA requires
         * 2*(20 + 24 + 8) = 104 bytes.
         */ 
        byte[] expansion[] = { 
                { 0x41 },                                     // 'A'
                { 0x42, 0x42 },                               // 'BB'
                { 0x43, 0x43, 0x43 },                         // 'CCC'
                { 0x44, 0x44, 0x44, 0x44 },                   // 'DDDD'
                { 0x45, 0x45, 0x45, 0x45, 0x45 },             // 'EEEEE'
                { 0x46, 0x46, 0x46, 0x46, 0x46, 0x46 },       // 'FFFFFF'
                { 0x47, 0x47, 0x47, 0x47, 0x47, 0x47, 0x47 }, // 'GGGGGGG'
        };            

            
        /*
         * key_block =
         *    MD5(master + SHA('A' + master +
         *                     ServerHello.random + ClientHello.random)) +
         *    MD5(master + SHA('BB' + master +
         *                     ServerHello.random + ClientHello.random)) +
         *    MD5(master + SHA('CCC' + master +
         *                     ServerHello.random + ClientHello.random)) +
         *    [..]
         *
         * We set 
         *     subExp = master + ServerHello.random + ClientHello.random
         */ 
        byte[] blockSubExp = new byte[masterSecret.length + 
            serverRand.length + clientRand.length];
        int offset = 0;
        System.arraycopy(masterSecret, 0, blockSubExp, offset, 
                masterSecret.length);
        offset += masterSecret.length;
        System.arraycopy(serverRand, 0, blockSubExp, offset, 
                serverRand.length);
        offset += serverRand.length;
        System.arraycopy(clientRand, 0, blockSubExp, offset,
                clientRand.length);

        for (int i = 0; i < (keyBlock.length >>> 4); i++) {
            md.update(masterSecret, 0, masterSecret.length);
            sd.update(expansion[i], 0, expansion[i].length);
            byte[] res = new byte[20];
            sd.update(blockSubExp, 0, blockSubExp.length);
            sd.digest(res, 0, res.length);
            md.update(res, 0, 20);
            md.digest(keyBlock, i << 4, md.getDigestLength());
        }            
    }

    /**
     * Extract keys form keys block
     * 
     * @param clientRand 32-byte random value chosen by the client
     * @param serverRand 32-byte random value chosen by the server
     * @param masterSecret master secret resulting from the key exchange
     *
     * @exception GeneralSecurityException thrown in case of failure
     */
    private void chopKeysBlock(byte[] clientRand, byte[] serverRand,
                               byte[] masterSecret) 
            throws GeneralSecurityException {
        int offset = 0;
        
        System.arraycopy(keyBlock, 0, clientMACSecret, 
                0, clientMACSecret.length);
        offset += clientMACSecret.length;
    
        System.arraycopy(keyBlock, offset, serverMACSecret, 
                0, serverMACSecret.length);
        offset += serverMACSecret.length;
        
        System.arraycopy(keyBlock, offset, clientKey, 0, clientKey.length);
        offset += clientKey.length;
        
        System.arraycopy(keyBlock, offset, serverKey, 0, serverKey.length);
        offset += serverKey.length;

        if (suiteType == Handshake.ARCFOUR_128_MD5 ||
            suiteType == Handshake.ARCFOUR_128_SHA) {
        /*
         * NOTE: We know ivSze is always zero for ARCFOUR cipher suites
         * so this is commented out. It wasn't removed in case we need
         * to add support for DES or another block cipher.
         * if (ivSize != 0) {
         *     // bulk encryption uses a block cipher, so initialize IVs
         *     System.arraycopy(keyBlock, 
         *         2*(clientMACSecret.length + clientKey.length), 
         *         clientIV, 0, ivSize);
         *     System.arraycopy(keyBlock, 
         *         2*(clientMACSecret.length + clientKey.length) 
         *         + ivSize, serverIV, 0, ivSize);
         * }
         */ 
        } else {
        /* 
         * IMPL_NOTE: The only other option is ARCFOUR_40_MD5
         * 
         * Expand the keys for exportable cipher suites
         *  final_client_write_key = MD5(client_write_key +
         *                                ClientHello.random +
         *                               ServerHello.random);
         *  final_server_write_key = MD5(server_write_key +
         *                               ServerHello.random +
         *                               ClientHello.random);
         */
        byte[] res = new byte[16];
        md.update(clientKey, 0, clientKey.length);
        md.update(clientRand, 0, clientRand.length);
        md.update(serverRand, 0, serverRand.length);
        md.digest(res, 0, res.length);
            
        /* 
         * NOTE: For both ARCFOUR_128_MD5 and ARCFOUR_40_MD5, 
         * expanded key is 16
         */ 
        byte[] fcKey = new byte[16]; 
        System.arraycopy(res, 0, fcKey, 0, fcKey.length);
                
        md.update(serverKey, 0, serverKey.length);
        md.update(serverRand, 0, serverRand.length);
        md.update(clientRand, 0, clientRand.length);
        md.digest(res, 0, res.length);
        byte[] fserverKey = new byte[fcKey.length];
        System.arraycopy(res, 0, fserverKey, 0, fserverKey.length);
            
        clientKey = fcKey;
        serverKey = fserverKey;
            
        /* 
         * ... and compute IVs in a special way
         * client_write_IV = MD5(ClientHello.random + ServerHello.random)
         * server_write_IV = MD5(ServerHello.random + ClientHello.random)
         * 
         * NOTE: We know for the chosen ciphersuites, ivSize is zero
         * so this code is commented out for now
            if (ivSize != 0) {
                md.update(clientRand, (short) 0, 
                    (short) clientRand.length);
                md.doFinal(serverRand, (short) 0, 
                    (short) serverRand.length, res, (short) 0);
                System.arraycopy(res, 0, clientIV, 0, ivSize);
                
                md.update(serverRand, (short) 0, 
                    (short) serverRand.length);
                md.doFinal(clientRand, (short) 0, 
                    (short) clientRand.length, res, (short) 0);
                System.arraycopy(res, 0, serverIV, 0, ivSize);
        }
        */ 
        }

        /*
         * Now initialize the ciphers and keys. FOr now this is always
         * ARCFOUR and we comment out support for other ciphers.
         */ 
        clientBulkKey = new SecretKey(clientKey, 0, clientKey.length, "ARC4");
        serverBulkKey = new SecretKey(serverKey, 0, serverKey.length, "ARC4");
    }
}

/**
 * Implements MAC computation
 */
class MAC {
    /** 
     * PAD1 is a 48-byte array filled with 0x36
     */
    static final byte[] PAD1 = {    
        0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36,
        0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36,
        0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36,
        0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36,
        0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36,
        0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36, 0x36
    };
        
    /** 
     * PAD2 is a 48-byte array filled with 0x5c
     */
    static final byte[] PAD2 = {
        0x5c, 0x5c, 0x5c, 0x5c, 0x5c, 0x5c, 0x5c, 0x5c,
        0x5c, 0x5c, 0x5c, 0x5c, 0x5c, 0x5c, 0x5c, 0x5c,
        0x5c, 0x5c, 0x5c, 0x5c, 0x5c, 0x5c, 0x5c, 0x5c,
        0x5c, 0x5c, 0x5c, 0x5c, 0x5c, 0x5c, 0x5c, 0x5c,
        0x5c, 0x5c, 0x5c, 0x5c, 0x5c, 0x5c, 0x5c, 0x5c,
        0x5c, 0x5c, 0x5c, 0x5c, 0x5c, 0x5c, 0x5c, 0x5c
    };
    
    /**
     * Data used for MAC computation
     */
    /** MAC secret */
    protected byte[] macSecret = null;
    /** Message digest */
    protected MessageDigest digest = null;
    /** Message digest length */
    protected int digestLength = 0;
    /** pad length */
    protected int padLength = 0;
    /** Write sequence number */
    private long sequenceNumber = 0;
        
        
    /** 
     * Computes the MAC for an SSLCompressed structure.
     * 
     * @param type SSL record type of the SSLCompressed structure
     * @param buf byte array containing the SSLCompressed fragment
     * @param offset starting offset of the fragment in buf
     * @param length length of the fragment
     *
     * @return a byte array containing the MAC
     */
    byte[] getMAC(byte type, byte[] buf, int offset, int length) {
        /* 
         * MAC = hash(MAC_secret + PAD2 +
         *    hash(MAC_secret + PAD1 + seq_num + type + len +
         *         compressed_fragment));
         */ 

        // Compute the inner hash first
        byte[] byteArray = null;
        digest.update(macSecret, 0, macSecret.length);
        digest.update(PAD1, 0, padLength);
        byteArray = Utils.longToBytes(sequenceNumber);
        digest.update(byteArray, 0, byteArray.length);
        
        byteArray = new byte[3];
        byteArray[0] = type;
        byteArray[1] = (byte) (length >>> 8);
        byteArray[2] = (byte) (length & 0xff);
        digest.update(byteArray, 0, byteArray.length);
        byte[] innerHash = new byte[digest.getDigestLength()];
        digest.update(buf, offset, length);
        try {
            digest.digest(innerHash, 0, innerHash.length);
        } catch (DigestException e) {
            // Ignore this exception, it should never happen
        }
            
        // Now, the outer hash
        digest.update(macSecret, 0, macSecret.length);
        digest.update(PAD2, 0, padLength);
        byte[] mac = new byte[innerHash.length];
        digest.update(innerHash, 0, innerHash.length);
        try {
            digest.digest(mac, 0, mac.length);
        } catch (DigestException e) {
            // Ignore this exception, it should never happen
        }
            
            return mac;
    }
        
    /**
     * Increments write sequence number
     * @exception IOException if the sequence numbers rolls around
     */ 
    void incrementSequenceNumber() throws IOException {
        if (++sequenceNumber == (long) 0) 
            throw new IOException("Sequence number rolled over");
    }
}    

/**
 * Implements record's encoding
 */
class RecordEncoder extends MAC {
    /** Cipher used for encryption */
    private Cipher cipher;

    /**
     * Constructs RecordEncoder object
     * 
     * @param dgst digest for MAC computation
     * @param secret MAC secret
     * @param padLen padding length
     * @param cphr cipher used for encoding
     */
    RecordEncoder(MessageDigest dgst, byte[] secret, int padLen, Cipher cphr) {
        macSecret = secret;
        digest = dgst;
        digestLength = digest.getDigestLength();
        padLength = padLen;
        cipher = cphr;
    }

    /**
     * Converts a byte array containing an SSLPlaintext structure
     * to the corresponding SSLCiphertext structure. The process 
     * typically involves the addition of a MAC followed by 
     * encryption.
     * 
     * @param plainText byte array containing SSLPlaintext
     * @return the number of bytes written to the OutputStream
     *
     * @exception IOException if a problem is encountered during
     * encryption
     */ 
    byte[] encode(byte[] plainText) throws IOException {
        /*
         * Since we only support NULL compression, SSLPlaintext
         * the same as SSLCompressed.
         */ 
        byte[] fragAndMAC = null; // fragment plus MAC
        
        if (digest != null) {
            fragAndMAC = new byte[plainText.length - 5 + digestLength];
            System.arraycopy(plainText, 5, fragAndMAC, 
                    0, plainText.length - 5);
            byte[] mac = getMAC(plainText[0], 
                    plainText, 5, plainText.length - 5);
            System.arraycopy(mac, 0, fragAndMAC, 
                    (plainText.length - 5), digestLength);
            } else {
                fragAndMAC = new byte[plainText.length - 5];
                System.arraycopy(plainText, 5, fragAndMAC, 
                        0, plainText.length - 5);
        }
        
        // ... now we need to encrypt fragAndMAC and possibly update IVs
        byte[] efragAndMAC = null; 
        if (cipher != null) {
            try {
                /*
                * NOTE: For now, we always have a stream cipher so this
                * is commented out.
                */ 
                // We have a stream cipher
                efragAndMAC = fragAndMAC;
                cipher.update(fragAndMAC, 0, 
                        fragAndMAC.length, efragAndMAC, 0);
            } catch (Exception e) {
                throw new IOException("Encode caught " + e);
            }
        } else {
            // Cipher algorithm is NULL
            efragAndMAC = fragAndMAC;
        }
        
    	if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
	        Logging.report(Logging.INFORMATION, LogChannels.LC_SECURITY,
			   "efragAndMAC: " + Utils.hexEncode(efragAndMAC));
	    }
        
        byte[] record = new byte[efragAndMAC.length + 5];
        System.arraycopy(plainText, 0, record, 0, 3);
        record[3] = (byte) (efragAndMAC.length >>> 8);
        record[4] = (byte) (efragAndMAC.length & 0xff);
        System.arraycopy(efragAndMAC, 0, record, 5, efragAndMAC.length);
        
        // We have encoded one more record, increment seq number
        incrementSequenceNumber();
        
        return record;
    }
}

/**
 * Implements record's decoding
 */
class RecordDecoder extends MAC {
    /** Cipher used for decryption */
    private Cipher cipher;

    /**
     * Constructs RecordDecoder object
     * 
     * @param dgst digest for MAC computation
     * @param secret MAC secret
     * @param padLen padding length
     * @param cphr cipher used for decoding
     */
    RecordDecoder(MessageDigest dgst, byte[] secret, int padLen, Cipher cphr) {
        macSecret = secret;
        digest = dgst;
        digestLength = digest.getDigestLength();
        padLength = padLen;
        cipher = cphr;
    }    
    

    /**
     * Converts a byte array containing an SSLCiphertext structure
     * to the corresponding SSLPlaintext structure. The process
     * typically involves decryption followed by MAC verification
     * and MAC stripping.
     * @param recordHeader record header
     * @param recordData record data
     * @return Length of the decrypted data in the input buffer.
     * 
     * @exception IOException if a problem is encountered during decryption
     *                        or MAC verification
     */ 
    int decode(byte[] recordHeader, byte[] recordData) 
               throws IOException {
        if (cipher != null) {
            // Cipher algorithm is not NULL (ctxt needs to be decrypted)

            try {
                /*
                 * NOTE: For now, we only have ARCFOUR, a stream cipher
                 * so there is no IV or padding that a block cipher has.
                 *
                 * Otherwise we would have to find the plaintext offset
                 * after we decrypt.
                 */

                // We have a stream cipher (NOTE: assuming CLIENT role)

                // We can decode in place w/o using additional memory
                cipher.update(recordData, 0, recordData.length, recordData, 0);
            } catch (Exception e) {
                throw new IOException("Decode caught " + e);
            }
        }

        int length = recordData.length - digestLength;
        if (digest != null) {
            byte[] expMAC = null;  // expected MAC
            expMAC = getMAC(recordHeader[0], recordData, 0, length);
            if (!Utils.byteMatch(expMAC, 0, recordData, length, 
                        digestLength)) {
                throw new IOException("Bad MAC");
            }
        }
        
        incrementSequenceNumber();

        return length;
    }
}

