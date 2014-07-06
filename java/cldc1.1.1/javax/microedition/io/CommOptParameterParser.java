/*
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
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

package javax.microedition.io;

/**
 * Implements path normalization for comm protocol.
 */
class CommOptParameterParser implements PathNormalizer {
  /** Bit flag: 1 stop bits. */
  private final static int serSettingsFlagStopBits1     = 1 << 0;
  /** Bit flag: 2 stop bits. */
  private final static int serSettingsFlagStopBits2     = 1 << 1;
  /** Bit flag: parity none. */
  private final static int serSettingsFlagParityNoneM   = 1 << 2;
  /** Bit flag: parity on. */
  private final static int serSettingsFlagParityOddM    = 1 << 3;
  /** Bit flag: parity even. */
  private final static int serSettingsFlagParityEvenM   = 1 << 4;
  /** Bit flag: RTS rcv flow control. */
  private final static int serSettingsFlagRTSAutoM      = 1 << 5;
  private final static int serSettingsFlagRTSAutoOffM   = 1 << 6;
  /** Bit flag: CTS xmit flow control. */
  private final static int serSettingsFlagCTSAutoM      = 1 << 7;
  private final static int serSettingsFlagCTSAutoOffM   = 1 << 8;
  /** Bit flag: 7 bits/char. */
  private final static int serSettingsFlagBitsPerChar7  = 1 << 9;
  /** Bit flag: 8 bits/char. */
  private final static int serSettingsFlagBitsPerChar8  = 1 << 10;
  /** Bit flag: blocking */
  private final static int serSettingsFlagBlockingOn    = 1 << 11;
  private final static int serSettingsFlagBlockingOff   = 1 << 12;

  /** Port name */
  public String port;
  /** Bit per char. */
  public int bbc      = serSettingsFlagBitsPerChar7 | 
                         serSettingsFlagBitsPerChar8;
  /** Stop bits. */
  public int stop     = serSettingsFlagStopBits1 | 
                         serSettingsFlagStopBits2;
  /** Parity. */
  public int parity   = serSettingsFlagParityNoneM | 
                         serSettingsFlagParityOddM  |
                         serSettingsFlagParityEvenM;
  /** RTS. */
  public int rts      = serSettingsFlagRTSAutoM | 
                         serSettingsFlagRTSAutoOffM;
  /** CTS. */
  public int cts      = serSettingsFlagCTSAutoM | 
                         serSettingsFlagCTSAutoOffM;
  /** Blocking. */
  public int blocking = serSettingsFlagBlockingOn | 
                         serSettingsFlagBlockingOff;
  /** Baud rate. */
  public int baud     = -1;

  public String normalize(String path) {
    if (path == null || "".equals(path)) {
      throw new IllegalArgumentException("Expected scheme-specific part");
    }

    int p = 0;
    int len = path.length();

    for (p = 0; p < len && isAlphaNum(path.charAt(p)); p++) {}

    // Asterisk can be used to indicate a wildcard match 
    // in the port identifier field
    if (p < len && path.charAt(p) == '*') {
      if (p != len - 1) {
        throw new IllegalArgumentException(
          "Asterisk can appear only at the end: " + path);
      }
      p++;
    }

    if (p == 0) {
      throw new IllegalArgumentException("Expected port identifier: " + path);
    }

    port = path.substring(0, p);

    while (p < len) {
      if (path.charAt(p) != ';') {
        throw new IllegalArgumentException(
          "Expected semi-colon delimiter: " + path);
      }
      p++;
      int q = path.indexOf('=', p);
      if (q == -1) {
        throw new IllegalArgumentException(
          "Malformed optional parameters: " + path);
      }
      String name = path.substring(p, q);
      q++;
      int r = path.indexOf(';', q);
      if (r == -1) {
        r = len;
      }
      String value = path.substring(q, r);
      if ("baudrate".equals(name)) {
        try {
          baud = Integer.parseInt(value);
        } catch (NumberFormatException e) {
          throw new IllegalArgumentException(
            "Invalid baudrate value: " + path);
        }
        if (baud <= 0) {
          throw new IllegalArgumentException(
            "Invalid baudrate value: " + path);
        }
      } else if ("bitsperchar".equals(name)) {
        if ("7".equals(value)) {
          bbc = serSettingsFlagBitsPerChar7;
        } else if ("8".equals(value)) {
          bbc = serSettingsFlagBitsPerChar8;
        } else {
          throw new IllegalArgumentException(
            "Invalid bitsperchar value: " + path);
        }
      } else if ("stopbits".equals(name)) {
        if ("1".equals(value)) {
          stop = serSettingsFlagStopBits1;
        } else if ("2".equals(value)) {
          stop = serSettingsFlagStopBits2;
        } else {
          throw new IllegalArgumentException(
            "Invalid stopbits value: " + path);
        }
      } else if ("parity".equals(name)) {
        if ("odd".equals(value)) {
          parity = serSettingsFlagParityOddM;
        } else if ("even".equals(value)) {
          parity = serSettingsFlagParityEvenM;
        } else if ("none".equals(value)) {
          parity = serSettingsFlagParityNoneM;
        } else {
          throw new IllegalArgumentException(
            "Invalid parity value: " + path);
        }
      } else if ("blocking".equals(name)) {
        if ("on".equals(value)) {
          blocking = serSettingsFlagBlockingOn;
        } else if ("off".equals(value)) {
          blocking = serSettingsFlagBlockingOff;
        } else {
          throw new IllegalArgumentException(
            "Invalid blocking value: " + path);
        }
      } else if ("autocts".equals(name)) {
        if ("on".equals(value)) {
          cts = serSettingsFlagCTSAutoM;
        } else if ("off".equals(value)) {
          cts = serSettingsFlagCTSAutoOffM;
        } else {
          throw new IllegalArgumentException("Invalid autocts value: " + path);
        }
      } else if ("autorts".equals(name)) {
        if ("on".equals(value)) {
          rts = serSettingsFlagRTSAutoM;
        } else if ("off".equals(value)) {
          rts = serSettingsFlagRTSAutoOffM;
        } else {
          throw new IllegalArgumentException("Invalid autorts value: " + path);
        }
      } else {
        throw new IllegalArgumentException("Unknown parameter used: " + path);
      }
      p = r;
    }

    { 
      String normalizedPath = port;

      if (baud > 0) {
        normalizedPath += ";baudrate=" + baud;
      }

      switch (bbc) {
      case serSettingsFlagBitsPerChar7: 
        normalizedPath += ";bitsperchar=7"; break;
      case serSettingsFlagBitsPerChar8: 
        normalizedPath += ";bitsperchar=8"; break;
      }

      switch (stop) {
      case serSettingsFlagStopBits1:
        normalizedPath += ";stopbits=1"; break;
      case serSettingsFlagStopBits2:
        normalizedPath += ";stopbits=2"; break;
      }

      switch (parity) {
      case serSettingsFlagParityNoneM:
        normalizedPath += ";parity=none"; break;
      case serSettingsFlagParityOddM:
        normalizedPath += ";parity=odd"; break;
      case serSettingsFlagParityEvenM:
        normalizedPath += ";parity=even"; break;
      }

      switch (blocking) {
      case serSettingsFlagBlockingOn:
        normalizedPath += ";blocking=on"; break;
      case serSettingsFlagBlockingOff:
        normalizedPath += ";blocking=off"; break;
      }

      switch (cts) {
      case serSettingsFlagCTSAutoM:
        normalizedPath += ";autocts=on"; break;
      case serSettingsFlagCTSAutoOffM:
        normalizedPath += ";autocts=off"; break;
      }

      switch (rts) {
      case serSettingsFlagRTSAutoM:
        normalizedPath += ";autorts=on"; break;
      case serSettingsFlagRTSAutoOffM:
        normalizedPath += ";autorts=off"; break;
      }

      return normalizedPath;
    }
  }

  private static boolean isAlphaNum(char c) {
    return Character.isDigit(c) || ('a' <= c && c <= 'z') ||
      ('A' <= c && c <= 'Z');
  }
}
