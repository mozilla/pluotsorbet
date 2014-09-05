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

package com.sun.midp.io.j2me.sms;

import java.io.ByteArrayOutputStream;

/**
 * Text encoder and decoder for GSM 7-bit text and
 * UCS-2 characters.
 */
public class TextEncoder {

    /**
     * GSM 7-bit character to UCS-2 mapping tables.
     */
    protected static byte[] chars7Bit = {
	0x00, // 0x0040,  COMMERCIAL AT
	0x01, // 0x00a3, POUND SIGN
	0x02, // 0x0024, DOLLAR SIGN
	0x03, // 0x00a5, YEN SIGN
	0x04, // 0x00e8, LATIN SMALL LETTER E WITH GRAVE
	0x05, // 0x00e9, LATIN SMALL LETTER E WITH ACUTE
	0x06, // 0x00f9, LATIN SMALL LETTER U WITH GRAVE
	0x07, // 0x00ec, LATIN SMALL LETTER I WITH GRAVE
	0x08, // 0x00f2, LATIN SMALL LETTER O WITH GRAVE
	0x09, // 0x00c7, LATIN CAPITAL LETTER C WITH CEDILLA
	0x0a, // 0x000a, control: line feed
	0x0b, // 0x00d8, LATIN CAPITAL LETTER O WITH STROKE
	0x0c, // 0x00f8, LATIN SMALL LETTER O WITH STROKE
	0x0d, // 0x000d, control: carriage return
	0x0e, // 0x00c5, LATIN CAPITAL LETTER A WITH RING ABOVE
	0x0f, // 0x00e5, LATIN SMALL LETTER A WITH RING ABOVE
	0x10, // 0x0394, GREEK CAPITAL LETTER DELTA
	0x11, // 0x005f, LOW LINE
	0x12, // 0x03a6, GREEK CAPITAL LETTER PHI
	0x13, // 0x0393, GREEK CAPITAL LETTER GAMMA
	0x14, // 0x039b, GREEK CAPITAL LETTER LAMDA
	0x15, // 0x03a9, GREEK CAPITAL LETTER OMEGA
	0x16, // 0x03a0, GREEK CAPITAL LETTER PI
	0x17, // 0x03a8, GREEK CAPITAL LETTER PSI
	0x18, // 0x03a3, GREEK CAPITAL LETTER SIGMA
	0x19, // 0x0398, GREEK CAPITAL LETTER THETA
	0x1a, // 0x039e, GREEK CAPITAL LETTER XI
	0x1b, // 0x001b, escape to extension table
	0x1c, // 0x00c6, LATIN CAPITAL LETTER AE
	0x1d, // 0x00e6, LATIN SMALL LETTER AE
	0x1e, // 0x00df, LATIN SMALL LETTER SHARP S
	0x1f, // 0x00c9, LATIN CAPITAL LETTER E WITH ACUTE
	0x20, // 0x0020, SPACE
	0x21, // 0x0021, EXCLAMATION MARK
	0x22, // 0x0022, QUOTATION MARK
	0x23, // 0x0023, NUMBER SIGN
	0x24, // 0x00a4, CURRENCY SIGN
	0x25, // 0x0025, PERCENT SIGN
	0x26, // 0x0026, AMPERSAND
	0x27, // 0x0027, APOSTROPHE
	0x28, // 0x0028, LEFT PARENTHESIS
	0x29, // 0x0029, RIGHT PARENTHESIS
	0x2a, // 0x002a, ASTERISK
	0x2b, // 0x002b, PLUS SIGN
	0x2c, // 0x002c, COMMA
	0x2d, // 0x002d, HYPHEN-MINUS
	0x2e, // 0x002e, FULL STOP
	0x2f, // 0x002f, SOLIDUS
	0x30, // 0x0030, DIGIT ZERO
	0x31, // 0x0031, DIGIT ONE
	0x32, // 0x0032, DIGIT TWO
	0x33, // 0x0033, DIGIT THREE
	0x34, // 0x0034, DIGIT FOUR
	0x35, // 0x0035, DIGIT FIVE
	0x36, // 0x0036, DIGIT SIX
	0x37, // 0x0037, DIGIT SEVEN
	0x38, // 0x0038, DIGIT EIGHT
	0x39, // 0x0039, DIGIT NINE
	0x3a, // 0x003a, COLON
	0x3b, // 0x003b, SEMICOLON
	0x3c, // 0x003c, LESS-THAN SIGN
	0x3d, // 0x003d, EQUALS SIGN
	0x3e, // 0x003e, GREATER-THAN SIGN
	0x3f, // 0x003f, QUESTION MARK
	0x40, // 0x00a1, INVERTED EXCLAMATION MARK
	0x41, // 0x0041, LATIN CAPITAL LETTER A
	0x42, // 0x0042, LATIN CAPITAL LETTER B
	0x43, // 0x0043, LATIN CAPITAL LETTER C
	0x44, // 0x0044, LATIN CAPITAL LETTER D
	0x45, // 0x0045, LATIN CAPITAL LETTER E
	0x46, // 0x0046, LATIN CAPITAL LETTER F
	0x47, // 0x0047, LATIN CAPITAL LETTER G
	0x48, // 0x0048, LATIN CAPITAL LETTER H
	0x49, // 0x0049, LATIN CAPITAL LETTER I
	0x4a, // 0x004a, LATIN CAPITAL LETTER J
	0x4b, // 0x004b, LATIN CAPITAL LETTER K
	0x4c, // 0x004c, LATIN CAPITAL LETTER L
	0x4d, // 0x004d, LATIN CAPITAL LETTER M
	0x4e, // 0x004e, LATIN CAPITAL LETTER N
	0x4f, // 0x004f, LATIN CAPITAL LETTER O
	0x50, // 0x0050, LATIN CAPITAL LETTER P
	0x51, // 0x0051, LATIN CAPITAL LETTER Q
	0x52, // 0x0052, LATIN CAPITAL LETTER R
	0x53, // 0x0053, LATIN CAPITAL LETTER S
	0x54, // 0x0054, LATIN CAPITAL LETTER T
	0x55, // 0x0055, LATIN CAPITAL LETTER U
	0x56, // 0x0056, LATIN CAPITAL LETTER V
	0x57, // 0x0057, LATIN CAPITAL LETTER W
	0x58, // 0x0058, LATIN CAPITAL LETTER X
	0x59, // 0x0059, LATIN CAPITAL LETTER Y
	0x5a, // 0x005a, LATIN CAPITAL LETTER Z
	0x5b, // 0x00c4, LATIN CAPITAL LETTER A WITH DIARESIS
	0x5c, // 0x00d6, LATIN CAPITAL LETTER O WITH DIARESIS
	0x5d, // 0x00d1, LATIN CAPITAL LETTER N WITH TILDE
	0x5e, // 0x00dc, LATIN CAPITAL LETTER U WITH DIARESIS
	0x5f, // 0x00a7, SECTION SIGN
	0x60, // 0x00bf, INVERTED QUESTION MARK
	0x61, // 0x0061, LATIN SMALL LETTER A
	0x62, // 0x0062, LATIN SMALL LETTER B
	0x63, // 0x0063, LATIN SMALL LETTER C
	0x64, // 0x0064, LATIN SMALL LETTER D
	0x65, // 0x0065, LATIN SMALL LETTER E
	0x66, // 0x0066, LATIN SMALL LETTER F
	0x67, // 0x0067, LATIN SMALL LETTER G
	0x68, // 0x0068, LATIN SMALL LETTER H
	0x69, // 0x0069, LATIN SMALL LETTER I
	0x6a, // 0x006a, LATIN SMALL LETTER J
	0x6b, // 0x006b, LATIN SMALL LETTER K
	0x6c, // 0x006c, LATIN SMALL LETTER L
	0x6d, // 0x006d, LATIN SMALL LETTER M
	0x6e, // 0x006e, LATIN SMALL LETTER N
	0x6f, // 0x006f, LATIN SMALL LETTER O
	0x70, // 0x0070, LATIN SMALL LETTER P
	0x71, // 0x0071, LATIN SMALL LETTER Q
	0x72, // 0x0072, LATIN SMALL LETTER R
	0x73, // 0x0073, LATIN SMALL LETTER S
	0x74, // 0x0074, LATIN SMALL LETTER T
	0x75, // 0x0075, LATIN SMALL LETTER U
	0x76, // 0x0076, LATIN SMALL LETTER V
	0x77, // 0x0077, LATIN SMALL LETTER W
	0x78, // 0x0078, LATIN SMALL LETTER X
	0x79, // 0x0079, LATIN SMALL LETTER Y
	0x7a, // 0x007a, LATIN SMALL LETTER Z
	0x7b, // 0x00e4, LATIN SMALL LETTER A WITH DIARESIS
	0x7c, // 0x00f6, LATIN SMALL LETTER O WITH DIARESIS
	0x7d, // 0x00f1, LATIN SMALL LETTER N WITH TILDE
	0x7e, // 0x00fc, LATIN SMALL LETTER U WITH DIARESIS
	0x7f  // 0x00e0, LATIN SMALL LETTER A WITH GRAVE

	/*
	 * The following are special case values encoded with an
	 * escaped sequence.
	 * 0x1b 0x14, // 0x005e, CIRCUMFLEX ACCENT
	 * 0x1b 0x28, // 0x007b, LEFT CURLY BRACKET
	 * 0x1b 0x29, // 0x007d, RIGHT CURLY BRACKET
	 * 0x1b 0x2f, // 0x005c, REVERSE SOLIDUS
	 * 0x1b 0x3c, // 0x005b, LEFT SQUARE BRACKET
	 * 0x1b 0x3d, // 0x007e, TILDE
	 * 0x1b 0x3e, // 0x005d, RIGHT SQUARE BRACKET
	 * 0x1b 0x40, // 0x007c, VERTICAL LINE
	 * 0x1b 0x65, // 0x20ac, EURO SIGN
	 */
    };

    /**
     * GSM 7-bit escaped character to UCS-2 mapping tables.
     */
    protected static byte[] escaped7BitChars = {
	  0x14, // 0x005e, CIRCUMFLEX ACCENT
	  0x28, // 0x007b, LEFT CURLY BRACKET
	  0x29, // 0x007d, RIGHT CURLY BRACKET
	  0x2f, // 0x005c, REVERSE SOLIDUS
	  0x3c, // 0x005b, LEFT SQUARE BRACKET
	  0x3d, // 0x007e, TILDE
	  0x3e, // 0x005d, RIGHT SQUARE BRACKET
	  0x40, // 0x007c, VERTICAL LINE
	  0x65  // 0x20ac, EURO SIGN
    };

    /**
     * GSM UCS-2 mapping tables.
     */
    protected static char[] charsUCS2 = {
	0x0040, // COMMERCIAL AT
	0x00a3, // POUND SIGN
	0x0024, // DOLLAR SIGN
	0x00a5, // YEN SIGN
	0x00e8, // LATIN SMALL LETTER E WITH GRAVE
	0x00e9, // LATIN SMALL LETTER E WITH ACUTE
	0x00f9, // LATIN SMALL LETTER U WITH GRAVE
	0x00ec, // LATIN SMALL LETTER I WITH GRAVE
	0x00f2, // LATIN SMALL LETTER O WITH GRAVE
	0x00c7, // LATIN CAPITAL LETTER C WITH CEDILLA
	0x000a, // control: line feed
	0x00d8, // LATIN CAPITAL LETTER O WITH STROKE
	0x00f8, // LATIN SMALL LETTER O WITH STROKE
	0x000d, // control: carriage return
	0x00c5, // LATIN CAPITAL LETTER A WITH RING ABOVE
	0x00e5, // LATIN SMALL LETTER A WITH RING ABOVE
	0x0394, // GREEK CAPITAL LETTER DELTA
	0x005f, // LOW LINE
	0x03a6, // GREEK CAPITAL LETTER PHI
	0x0393, // GREEK CAPITAL LETTER GAMMA
	0x039b, // GREEK CAPITAL LETTER LAMDA
	0x03a9, // GREEK CAPITAL LETTER OMEGA
	0x03a0, // GREEK CAPITAL LETTER PI
	0x03a8, // GREEK CAPITAL LETTER PSI
	0x03a3, // GREEK CAPITAL LETTER SIGMA
	0x0398, // GREEK CAPITAL LETTER THETA
	0x039e, // GREEK CAPITAL LETTER XI
	0x001b, // escape to extension table
	0x00c6, // LATIN CAPITAL LETTER AE
	0x00e6, // LATIN SMALL LETTER AE
	0x00df, // LATIN SMALL LETTER SHARP S
	0x00c9, // LATIN CAPITAL LETTER E WITH ACUTE
	0x0020, // SPACE
	0x0021, // EXCLAMATION MARK
	0x0022, // QUOTATION MARK
	0x0023, // NUMBER SIGN
	0x00a4, // CURRENCY SIGN
	0x0025, // PERCENT SIGN
	0x0026, // AMPERSAND
	0x0027, // APOSTROPHE
	0x0028, // LEFT PARENTHESIS
	0x0029, // RIGHT PARENTHESIS
	0x002a, // ASTERISK
	0x002b, // PLUS SIGN
	0x002c, // COMMA
	0x002d, // HYPHEN-MINUS
	0x002e, // FULL STOP
	0x002f, // SOLIDUS
	0x0030, // DIGIT ZERO
	0x0031, // DIGIT ONE
	0x0032, // DIGIT TWO
	0x0033, // DIGIT THREE
	0x0034, // DIGIT FOUR
	0x0035, // DIGIT FIVE
	0x0036, // DIGIT SIX
	0x0037, // DIGIT SEVEN
	0x0038, // DIGIT EIGHT
	0x0039, // DIGIT NINE
	0x003a, // COLON
	0x003b, // SEMICOLON
	0x003c, // LESS-THAN SIGN
	0x003d, // EQUALS SIGN
	0x003e, // GREATER-THAN SIGN
	0x003f, // QUESTION MARK
	0x00a1, // INVERTED EXCLAMATION MARK
	0x0041, // LATIN CAPITAL LETTER A
	0x0042, // LATIN CAPITAL LETTER B
	0x0043, // LATIN CAPITAL LETTER C
	0x0044, // LATIN CAPITAL LETTER D
	0x0045, // LATIN CAPITAL LETTER E
	0x0046, // LATIN CAPITAL LETTER F
	0x0047, // LATIN CAPITAL LETTER G
	0x0048, // LATIN CAPITAL LETTER H
	0x0049, // LATIN CAPITAL LETTER I
	0x004a, // LATIN CAPITAL LETTER J
	0x004b, // LATIN CAPITAL LETTER K
	0x004c, // LATIN CAPITAL LETTER L
	0x004d, // LATIN CAPITAL LETTER M
	0x004e, // LATIN CAPITAL LETTER N
	0x004f, // LATIN CAPITAL LETTER O
	0x0050, // LATIN CAPITAL LETTER P
	0x0051, // LATIN CAPITAL LETTER Q
	0x0052, // LATIN CAPITAL LETTER R
	0x0053, // LATIN CAPITAL LETTER S
	0x0054, // LATIN CAPITAL LETTER T
	0x0055, // LATIN CAPITAL LETTER U
	0x0056, // LATIN CAPITAL LETTER V
	0x0057, // LATIN CAPITAL LETTER W
	0x0058, // LATIN CAPITAL LETTER X
	0x0059, // LATIN CAPITAL LETTER Y
	0x005a, // LATIN CAPITAL LETTER Z
	0x00c4, // LATIN CAPITAL LETTER A WITH DIARESIS
	0x00d6, // LATIN CAPITAL LETTER O WITH DIARESIS
	0x00d1, // LATIN CAPITAL LETTER N WITH TILDE
	0x00dc, // LATIN CAPITAL LETTER U WITH DIARESIS
	0x00a7, // SECTION SIGN
	0x00bf, // INVERTED QUESTION MARK
	0x0061, // LATIN SMALL LETTER A
	0x0062, // LATIN SMALL LETTER B
	0x0063, // LATIN SMALL LETTER C
	0x0064, // LATIN SMALL LETTER D
	0x0065, // LATIN SMALL LETTER E
	0x0066, // LATIN SMALL LETTER F
	0x0067, // LATIN SMALL LETTER G
	0x0068, // LATIN SMALL LETTER H
	0x0069, // LATIN SMALL LETTER I
	0x006a, // LATIN SMALL LETTER J
	0x006b, // LATIN SMALL LETTER K
	0x006c, // LATIN SMALL LETTER L
	0x006d, // LATIN SMALL LETTER M
	0x006e, // LATIN SMALL LETTER N
	0x006f, // LATIN SMALL LETTER O
	0x0070, // LATIN SMALL LETTER P
	0x0071, // LATIN SMALL LETTER Q
	0x0072, // LATIN SMALL LETTER R
	0x0073, // LATIN SMALL LETTER S
	0x0074, // LATIN SMALL LETTER T
	0x0075, // LATIN SMALL LETTER U
	0x0076, // LATIN SMALL LETTER V
	0x0077, // LATIN SMALL LETTER W
	0x0078, // LATIN SMALL LETTER X
	0x0079, // LATIN SMALL LETTER Y
	0x007a, // LATIN SMALL LETTER Z
	0x00e4, // LATIN SMALL LETTER A WITH DIARESIS
	0x00f6, // LATIN SMALL LETTER O WITH DIARESIS
	0x00f1, // LATIN SMALL LETTER N WITH TILDE
	0x00fc, // LATIN SMALL LETTER U WITH DIARESIS
	0x00e0  // LATIN SMALL LETTER A WITH GRAVE
    };

    /**
     * GSM escaped character UCS-2 mapping tables.
     */
    protected static char[] escapedUCS2 = {
	0x005e, // CIRCUMFLEX ACCENT
	0x007b, // LEFT CURLY BRACKET
	0x007d, // RIGHT CURLY BRACKET
	0x005c, // REVERSE SOLIDUS
	0x005b, // LEFT SQUARE BRACKET
	0x007e, // TILDE
	0x005d, // RIGHT SQUARE BRACKET
	0x007c, // VERTICAL LINE
	0x20ac  // EURO SIGN
    };

    /**
     * Converts a UCS-2 character array into GSM 7-bit bytes.
     *
     * @param ucsbytes an array of UCS-2 characters in a byte array
     * @return array of GSM 7-bit bytes if the conversion was
     *   successful, otherwise return <code>null</code> to
     *   indicate that some UCS-2 values were included that can
     *   not be translated to the GSM 7-bit format
     */
    public static byte[] encode(byte[] ucsbytes) {
	/*
	 * Initialize a buffer with expected size twice that of
	 * the 7-bit encoded text.
	 */
	ByteArrayOutputStream bos = new ByteArrayOutputStream(ucsbytes.length);

	/*
	 * Walk through the UCS 2 characters 2 bytes at a time.
	 * All characters must be in the direct or extended UCS
	 * character tables. If not we reject the entire conversion.
	 */
	for (int i = 0; i < ucsbytes.length; i += 2) {
	    int j;
	    for (j = 0; j < charsUCS2.length; j++) {
		if (ucsbytes[i] == (charsUCS2[j] >> 8) &&
		    ucsbytes[i+1] == (charsUCS2[j] & 0xFF)) {
		    bos.write(chars7Bit[j]);
		    break;
		}
	    }

	    /*
	     * If you get to the end of the basic character table,
	     * check the extra escaped sequence table, too.
	     */
	    if (j == charsUCS2.length) {
		int k;
		for (k = 0; k < escapedUCS2.length; k++) {
		    if (ucsbytes[i] == (escapedUCS2[k] >> 8) &&
			ucsbytes[i+1] == (escapedUCS2[k] & 0xFF)) {
			bos.write(0x1b);
			bos.write(escaped7BitChars[k]);
			break;
		    }
		}
		/*
		 * If no match is found in either table,
		 * return null to indicate UCS 2 characters
		 * were found that are not included in the
		 * GSM 7 bit encoding.
		 */
		if (k == escapedUCS2.length) {
		    return null;
		}
	    }
	}
	return bos.toByteArray();
    }

    /**
     * Converts a GSM 7-bit encoded byte array into a UCS-2 byte array.
     *
     * @param gsm7bytes an array of GSM 7-bit encoded characters
     * @return an array of UCS-2 characters in a byte array
     */
    public static byte[] decode(byte[] gsm7bytes) {
	/*
	 * Initialize a buffer with expected size twice that of
	 * the 7-bit encoded text
	 */
	ByteArrayOutputStream bos =
	    new ByteArrayOutputStream(gsm7bytes.length * 2);

	for (int i = 0; i < gsm7bytes.length; i++) {
	    /*
	     * Check for escaped characters first.
	     */
	    if (gsm7bytes[i] == 0x1b) {
		/*
		 * Advance the pointer past the escape.
		 */
		i++;

		for (int j = 0; j < escaped7BitChars.length; j++) {
		    if (gsm7bytes[i] == escaped7BitChars[j]) {
			bos.write(escapedUCS2[j] >> 8);
			bos.write(escapedUCS2[j] & 0xFF);
			break;
		    }
		}

	    } else {
		for (int j = 0; j < chars7Bit.length; j++) {
		    if (gsm7bytes[i] == chars7Bit[j]) {
			bos.write(charsUCS2[j] >> 8);
			bos.write(charsUCS2[j] & 0xFF);
			break;
		    }
		}
	    }
	}

	return bos.toByteArray();
    }

    /**
     * Gets a <code>String</code> from the UCS-2 byte array.
     * @param ucsbytes an array of UCS-2 characters as a byte array
     * @return Java string
     */
    public static String toString(byte[] ucsbytes) {
	char[] c = new char [ucsbytes.length/2];
	/*
	 * Create a string from the raw UCS 2 bytes.
	 */
	for (int i = 0; i < ucsbytes.length; i += 2) {
	    c[i/2] = (char)((ucsbytes[i] << 8)
			    +  (ucsbytes[i+1] & 0xFF));
	}
	return new String(c);
    }

    /**
     * Converts a string to a UCS-2 byte array.
     *
     * @param data a String to be converted
     * @return an array of bytes in UCS-2 character
     */
    public static byte[] toByteArray(String data) {
	char[] c = data.toCharArray();
	ByteArrayOutputStream bos =
	    new ByteArrayOutputStream(data.length());
	for (int i = 0; i < c.length; i ++) {
	    bos.write(c[i] >> 8);
	    bos.write(c[i] & 0xFF);
	}
	return bos.toByteArray();
    }

}
