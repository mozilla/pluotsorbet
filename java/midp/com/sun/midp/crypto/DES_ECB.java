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

package com.sun.midp.crypto;

/**
 * DES cipher implementation.
 */
class DES_ECB extends BlockCipherBase {
    /** True, if this class is being used for triple DES (EDE). */
    private boolean tripleDes;

    /** Algorithm of the key. */
    protected String keyAlgorithm;

    /** DES ciphers encrypt/decrypt in block size of 8 bytes. */
    protected static final int BLOCK_SIZE = 8;

    /** s0p. */
    private static final int s0p[] = initTable(0x40410100,
                                0x72cf4bacb769d40aL, 0x2853f695813e1de0L,
                                0xf5a7295e13cb8c6L, 0xf36d49a024d78e1bL);

    /** s1p. */
    private static final int s1p[] = initTable(0x08021002,
                                0xf06c93a62d1a5ec1L, 0x4bd27805b7e9843fL,
                                0x5ea7f590834d287bL, 0xe2091f6cd43ab1c6L);

    /** s2p. */
    private static final int s2p[] = initTable(0x20808020,
                                0xc71da4d35268f98eL, 0xa719f2ce5b6304bL,
                                0x71829a6d073ea4f8L, 0xbc4f25d350e9cb16L);

    /** s3p. */
    private static final int s3p[] = initTable(0x02080201,
                                0x7a1f0cb5e9839748L, 0xd6216bc2305eadf4L,
                                0xd3496a1cb0250de2L, 0x8f74f1a756cb389eL);

    /** s4p. */
    private static final int s4p[] = initTable(0x01002084,
                                0x842fda7c4196bde0L, 0x6853a7091bf5c23eL,
                                0xeb5c419a86f07825L, 0xb20fde346da317c9L);

    /** s5p. */
    private static final int s5p[] = initTable(0x10040408,
                                0x950e52b43f68a9c7L, 0x4bd021edfc83167aL,
                                0x38a7e50a82d45f61L, 0xf64b9c7029bec31dL);

    /** s6p. */
    private static final int s6p[] = initTable(0x80200840,
                                0x215cfa304d968769L, 0xd2af05c3eb78be14L,
                                0xb6e9214edb301c85L, 0xd5392f478afc76aL);

    /** s7p. */
    private static final int s7p[] = initTable(0x04104010,
                                0xde30a5cf18637b9cL, 0x275af90684bd42e1L,
                                0x429f3806dba5e15aL, 0xf4c963bc1e708d27L);

    /** initPermRight. */
    private static int[] initPermRight = initPerm(128, 32, 2, -3,
                              0xc3b3432020332020L, 0x83b3432020332020L);
    /** initPermLeft. */
    private static int[] initPermLeft = initPerm(128, 32, 2, -3,
                              0x8742032067420320L, 0x4742032067420320L);
     /** perm. */
    private static int[] perm = initPerm(64, 64, 4, -1,
                              0x4420411201004021L, 0x1001200101000000L);

    /**
     * Data used for key expansion.
     */
    private static byte[] expandData = ("\020\0313KM1L3}\014I1JT*}\025" +
            "\017\0128R=M;KM,Zx<I1X\017\020}\013}\000(R=}\021-H<Zk\020" +
            "%}\001Z5P8I(R3}\014:\134;\017*R5P8I1{}\014:\134-U1\017y1}" +
            "\023<M0T-U:I4KH\015tM0KqZ94KxR3[\017\013=H<Zx<S}\000(R=MI" +
            "\020\000\134*}\031-H<IL3}\033}\0002\017LI1JT*}\025;KM1L3P" +
            "\016u:I4KxI}\023<M0K5\016:94KxR3[<M0KqJ\0169(R3}\014:\134" +
            ";qZ5P8\0174<:\134-U:I=P8I1}\023\020\005SM1L3}\03391JT*}" +
            "\0313\017\034:=M;KM1Rx<I1J8\016\014S}\000(R=MI-H<Zx\01751Z" +
            "5P8I1J3}\014:\134-\0171=P8I1}\023t:\134-U:R\020\040I}\023<" +
            "M0K5U:I4K}\021\016,U0KqZ5<KxR3}\024\017\0238<Zx<I}\010(R=M" +
            ";=\020\0222}\031-H<Z<3}\033}\000(\134\020\03391JT*}\0313KM" +
            "1L3}\014\017!JI4KxR}\013<M0K}\005\016-<KxR3}\024M0KqZ9\020" +
            ")J3}\014:\134-}\001Z5P8I(\020=*\134-U:I4X8I1}\023<<\017" +
            "\010U1L3}\033iJT*}\031-S\020\002MM;KM1L}\000<I1JT:\017\021" +
            "}\010(R=M;=H<Zx<S\020\032<3}\033}\000(\134*}\031-H<I\020$X" +
            "8I1}\023<<:\134-U:I=\0172}\013<M0K}\005:I4KxI\016\042HKqZ5" +
            "P3xR3}\014U\017\035,Zx<I1X(R=M;K8\017\015}\021-H<Zk}\033}" +
            "\000(R2\017AJT*}\031-SM1L3}\0339\020\024}\000<I1JT:=M;KM1" +
            "R\01683xR3}\014U0KqZ5<\017c}\014:\134-U1Z5P8I1J\020(T-U:I" +
            "4KHI1}\023<M*\020\004IL3}\033}\0002T*}\031-HU\017U;KM1L3P" +
            "<I1JT*M\016\001X(R=M;K8<Zx<I\017C}\033}\000(R2}\031-H<Z<" +
            "\020+HI1}\023<M*\134-U:I4X\015#[<M0KqJI4KxR\016<;qZ5P8pR3" +
            "}\014:H\017\011Rx<I1J8R=M;KM,\016\025I-H<Zx}\013}\000(R=" +
            "\017\0302T*}\031-HU1L3}\033i\017\003P<I1JT*MM;KM1L\0160pR" +
            "3}\014:HKqZ5P3\016;t:\134-U:R5P8I1{\02035U:I4K}\0211}\023" +
            "<M0T").getBytes();

    /** DES key data. */
    private byte[][] dkey;
    
    /**
     * Constructor.
     *
     * @param useTripleDes true if the class is being used for triple DES
     *
     */
    public DES_ECB(boolean useTripleDes) {
        super(BLOCK_SIZE);
        tripleDes = useTripleDes;

        if (useTripleDes) {
            keyAlgorithm = "DESEDE";
        } else {
            keyAlgorithm = "DES";
        }
    }

    /**
     * Called by the factory method to set the mode and padding parameters.
     * Need because Class.newInstance does not take args.
     *
     * @param mode the mode parsed from the transformation parameter of
     *             getInstance and upper cased
     * @param padding the paddinge parsed from the transformation parameter of
     *                getInstance and upper cased
     */
    protected void setChainingModeAndPadding(String mode, String padding)
            throws NoSuchPaddingException {
        // Note: The chaining mode is implicitly set by using this class.

        setPadding(padding);
    }

    /**
     * Initializes this cipher with a key and a set of algorithm
     * parameters.
     *
     * @param mode the operation mode of this cipher
     * @param key the encryption key
     * @param params the algorithm parameters
     *
     * @exception java.security.InvalidKeyException if the given key
     * is inappropriate for initializing this cipher
     * @exception java.security.InvalidAlgorithmParameterException
     * if the given algorithm parameters are inappropriate for this cipher
     */
    public void init(int mode, Key key, CryptoParameter params)
            throws InvalidKeyException, InvalidAlgorithmParameterException {
        doInit(mode, keyAlgorithm, key, false, null);
    }

    /**
     * Initializes key.
     * @param data key data
     * @param mode cipher mode
     * @exception InvalidKeyException if the given key is inappropriate
     * for this cipher
     */
    protected void initKey(byte[] data, int mode)
            throws InvalidKeyException {

        if (data.length != (tripleDes ? 24 : 8)) {
            throw new InvalidKeyException();
        }

        int keyCount = data.length >> 3;
        dkey = new byte[keyCount][];
        for (int i = 0; i < keyCount; i++) {
            dkey[i] = expandKey(data, i << 3);
        }
    }

    /**
     * Depending on the mode, either encrypts
     * or decrypts the data in the queue.
     * @param out will contain the result of encryption
     * or decryption operation
     * @param offset is the offset in out
     */
    protected void processBlock(byte[] out, int offset) {

        if (dkey.length == 1) {
            cipherBlock(0, mode == Cipher.ENCRYPT_MODE);
        } else {
            if (mode == Cipher.ENCRYPT_MODE) {
                cipherBlock(0, true);
                cipherBlock(1, false);
                cipherBlock(2, true);
            } else {
                cipherBlock(2, false);
                cipherBlock(1, true);
                cipherBlock(0, false);
            }
        }
        System.arraycopy(holdData, 0, out, offset, BLOCK_SIZE);
        holdCount = 0;
    }

    /**
     * Initializes data for permutation.
     * @param value seed value
     * @param period perion for value modification
     * @param divisor divisor of value
     * @param offset initial offset in the table
     * @param deltas1 packed offsets for elements 0 - 15
     * @param deltas2 packed offsets for elements 16 - 31
     * @return initialized data
     */
    private static int[] initPerm(int value, int period,
                  int divisor, int offset, long deltas1, long deltas2) {

        int[] result = new int[256];
        int count = 0;

        while (true) {

            offset += ((((count & 0x1f) < 16 ? deltas1 : deltas2) >>
                      ((15 - count & 0xf) << 2)) & 0xf) + 1;

            if (offset > 1023) {
                return result;
            }

            count++;

            if (count > 1 && count % period == 1) {
                value = value == 1 ? 128 : (value / divisor);
            }

            result[offset >> 2] |= (value << ((3 - offset & 3) << 3));
        }
    }

    /**
     * Initializes static data used by DES.
     * @param bitmask mask of bits used in this table
     * @param l1 order of words 0 - 15
     * @param l2 order of words 16 - 31
     * @param l3 order of words 32 - 47
     * @param l4 order of words 48 - 63
     * @return the table
     */
    private static final int[] initTable(int bitmask, long l1, long l2,
                                     long l3, long l4) {

        int[] words = new int[16];
        int count = 1;

        for (int i = 0; i < 8; i++) {

            int mask;
            if ((mask = bitmask & (0xf << (i << 2))) == 0) {
                continue;
            }

            for (int j = 0; j < count; j++) {
                words[count + j] = words[j] | mask;
            }
            count += count;
        }

        int[] data = new int[64];
        for (int i = 0; i < 64; i++) {
            data[i] = words[((int) ((i < 32 ? (i < 16 ? l1 : l2) :
                                    (i < 48 ? l3 : l4)) >>
                                     ((15 - (i & 0xf)) << 2))) & 0xf];
        }
        return data;
    }

    /**
     * Performs the encryption/decryption of data.
     * @param keyIndex index of the the key
     * @param encryptMode indicates if its encryption or decryption
     */
    private void cipherBlock(int keyIndex, boolean encryptMode) {

        byte[] key = dkey[keyIndex];
        byte[] data = holdData;

        int j = encryptMode ? 0 : 128 - BLOCK_SIZE;
        int offset = (encryptMode ? 0 : 16) - BLOCK_SIZE;

        // initial permutations

        int t, v;
        int left = 0;
        for (int i = 0; i < 8; i++) {
            left |= initPermLeft[(v = i << 5) + 16 + ((t = data[i]) & 0xf)] |
                    initPermLeft[v + ((t >> 4) & 0xf)];
        }

        int right = 0;
        for (int i = 0; i < 8; i++) {
            right |= initPermRight[(v = i << 5) + 16 + ((t = data[i]) & 0xf)] |
                     initPermRight[v + ((t >> 4) & 0xf)];
        }

        int i = 0;
        while (true) {
            // making the first bit and last bit adjacent
            // move the first bit to the last
            int temp = (right << 1) | ((right >> 31) & 1);

            // Mangler Function
            // every 6 bit is fed into the sbox, which
            // produces 4 bit output
            left ^= s0p[(temp & 0x3f) ^ key[j]]
                    ^ s1p[((temp >>  4) & 0x3f) ^ key[j + 1]]
                    ^ s2p[((temp >>  8) & 0x3f) ^ key[j + 2]]
                    ^ s3p[((temp >> 12) & 0x3f) ^ key[j + 3]]
                    ^ s4p[((temp >> 16) & 0x3f) ^ key[j + 4]]
                    ^ s5p[((temp >> 20) & 0x3f) ^ key[j + 5]]
                    ^ s6p[((temp >> 24) & 0x3f) ^ key[j + 6]];

            // making the last sbox input last bit from right[0]
            temp = ((right & 1) << 5) | ((right >> 27) & 0x1f);
            left ^= s7p[temp ^ key[j + 7]];

            if (i++ == 15) {
                break;
            }

            temp = left;
            left = right;
            right = temp;
            j -= offset;
        }

        // permutations

        int high = perm[left & 0xf] |
                   perm[32 + ((left >> 8) & 0xf)] |
                   perm[64 + ((left >> 16) & 0xf)] |
                   perm[96 + ((left >> 24) & 0xf)] |
                   perm[128 + (right & 0xf)] |
                   perm[160 + ((right >> 8) & 0xf)] |
                   perm[192 + ((right >> 16) & 0xf)] |
                   perm[224 + ((right >> 24) & 0xf)];

        int low  = perm[16 + ((left >> 4) & 0xf)] |
                   perm[48 + ((left >> 12) & 0xf)] |
                   perm[80 + ((left >> 20) & 0xf)] |
                   perm[112 + ((left >> 28) & 0xf)] |
                   perm[144 + ((right >> 4) & 0xf)] |
                   perm[176 + ((right >> 12) & 0xf)] |
                   perm[208 + ((right >> 20) & 0xf)] |
                   perm[240 + ((right >> 28) & 0xf)];

        data[0] = (byte) low;
        data[1] = (byte) (low >> 8);
        data[2] = (byte) (low >> 16);
        data[3] = (byte) (low >> 24);
        data[4] = (byte) high;
        data[5] = (byte) (high >> 8);
        data[6] = (byte) (high >> 16);
        data[7] = (byte) (high >> 24);
    }

    /**
     * Implements part of the DES algorithm.
     * @param key An 8 byte array containing the key data
     * @param keyOffset offset into the key byte array
     * @return the result of operation
     */
    private static byte[] expandKey(byte[] key, int keyOffset) {

        byte ek[] = new byte[128];
        int pos = 0;

        for (int i = 0; i < 8; i++) {

            int octet = key[keyOffset++];
            int len;

            for (int j = 0; j < 7; j++)  {

                len = expandData[pos++];
                int offset = 0;

                if ((octet & (0x80 >> j)) != 0) {

                    while (len-- > 0) {
                        int v;
                        if ((v = expandData[pos++]) == 125) {
                            offset += 16;
                        } else {
                            ek[offset += (v >> 3)] |= (1 << (v & 0x7));
                        }
                    }
                } else {
                    pos += len;
                }
            }
        }
        return ek;
    }
}
