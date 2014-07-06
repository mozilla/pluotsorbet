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

class URIParser {
  // Character-class masks from RFC 3986 and RFC 2234, 
  // in reverse order because initializers for static fields 
  // cannot make forward references.
  
  // DIGIT    = %x30-39 ; 0-9
  private static final long L_DIGIT = lowMask('0', '9');
  private static final long H_DIGIT = highMask('0', '9');

  // ALPHA    = %x41-5A / %x61-7A ; A-Z / a-z
  private static final long L_ALPHA = 
    lowMask('A', 'Z') | lowMask('a', 'z');
  private static final long H_ALPHA = 
    highMask('A', 'Z') | highMask('a', 'z');

  // HEXDIG   = digit | "A" | "B" | "C" | "D" | "E" | "F"
  //                  | "a" | "b" | "c" | "d" | "e" | "f"
  // Note: the grammar defined in RFC 3896 does not allow lowercase letters 
  // here, but in the text it explicitly says that 'host' is case insensitive.
  private static final long L_HEXDIG
    = L_DIGIT | lowMask('A', 'F') | lowMask('a', 'f');
  private static final long H_HEXDIG
    = H_DIGIT | highMask('A', 'F') | highMask('a', 'f');

  // sub-delims = "!" / "$" / "&" / "'" / "(" / ")"
  //            / "*" / "+" / "," / ";" / "="
  private static final long L_SUB_DELIMS = lowMask("!$&'()*+,;=");
  private static final long H_SUB_DELIMS = highMask("!$&'()*+,;=");

  // gen-delims = ":" / "/" / "?" / "#" / "[" / "]" / "@"
  private static final long L_GEN_DELIMS = lowMask(":/?#[]@");
  private static final long H_GEN_DELIMS = highMask(":/?#[]@");

  // reserved      = gen-delims / sub-delims
  private static final long L_RESERVED = L_GEN_DELIMS | L_SUB_DELIMS;
  private static final long H_RESERVED = H_GEN_DELIMS | H_SUB_DELIMS;

  // The zero'th bit is used to indicate that pct-encoded string are allowed; 
  // this is handled by the scanEscape method below.
  private static final long L_PCT_ENCODED = 1L;
  private static final long H_PCT_ENCODED = 0L;

  // unreserved    = ALPHA / DIGIT / "-" / "." / "_" / "~"
  private static final long L_UNRESERVED 
    = L_ALPHA | L_DIGIT | lowMask("-._~");
  private static final long H_UNRESERVED 
    = H_ALPHA | H_DIGIT | highMask("-._~");

  // pchar         = unreserved / pct-encoded / sub-delims / ":" / "@"
  private static final long L_PCHAR
    = L_UNRESERVED | L_PCT_ENCODED | L_SUB_DELIMS | lowMask(":@");
  private static final long H_PCHAR
    = H_UNRESERVED | H_PCT_ENCODED | H_SUB_DELIMS | highMask(":@");

  // scheme        = alpha *( alpha | digit | "+" | "-" | "." )
  private static final long L_SCHEME = L_ALPHA | L_DIGIT | lowMask("+-.");
  private static final long H_SCHEME = H_ALPHA | H_DIGIT | highMask("+-.");

  // query         = *( pchar / "/" / "?" )
  // fragment      = *( pchar / "/" / "?" )
  private static final long L_QUERY = L_PCHAR | lowMask("/?");
  private static final long H_QUERY = H_PCHAR | highMask("/?");

  // reg-name = *( unreserved / pct-encoded / sub-delims )
  private static final long L_REGNAME 
    = L_UNRESERVED | L_PCT_ENCODED | L_SUB_DELIMS;
  private static final long H_REGNAME
    = H_UNRESERVED | H_PCT_ENCODED | H_SUB_DELIMS;

  // All valid path characters
  private static final long L_PATH = L_PCHAR | lowMask("/");
  private static final long H_PATH = H_PCHAR | highMask("/");

  // alphanum      = alpha | digit
  private static final long L_ALPHANUM = L_DIGIT | L_ALPHA;
  private static final long H_ALPHANUM = H_DIGIT | H_ALPHA;

  // Dash, for use in domainlabel and toplabel
  private static final long L_DASH = lowMask("-");
  private static final long H_DASH = highMask("-");

  // Dot, for use in hostnames
  private static final long L_DOT = lowMask(".");
  private static final long H_DOT = highMask(".");

  // All valid userinfo characters
  // userinfo      = *( unreserved / pct-encoded / sub-delims / ":" )
  private static final long L_USERINFO = 
    L_UNRESERVED | L_PCT_ENCODED | L_SUB_DELIMS | lowMask(":");
  private static final long H_USERINFO = 
    H_UNRESERVED | H_PCT_ENCODED | H_SUB_DELIMS | highMask(":");

  // All characters for IPvFuture address
  // IPvFuture     = "v" 1*HEXDIG "." 1*( unreserved / sub-delims / ":" )
  private static final long L_IPvFUTURE = 
    L_UNRESERVED | L_SUB_DELIMS | lowMask(":");
  private static final long H_IPvFUTURE = 
    H_UNRESERVED | H_SUB_DELIMS | highMask(":");

  private static final int PORT_MIN = 0;
  private static final int PORT_MAX = 65535;
  private static final int[] ALL_PORTS = new int[] { PORT_MIN, PORT_MAX };

  // Components of all URIs: [<scheme>:]<scheme-specific-part>[#<fragment>]
  private String scheme;		// null ==> relative URI
  private String fragment;

  // Hierarchical URI components: [//<authority>]<path>[?<query>]
  private String authority;		// Registry or server

  private String userInfo;
  private String host;		// null ==> registry-based
  private int[] portrange = ALL_PORTS;
  private String portrangeString;

  // Remaining components of hierarchical URIs
  private String path;		// null ==> opaque
  private String query;

  private String schemeSpecificPart;

  private final String input; // URI input string
  private final PortRangeNormalizer portRangeNormalizer; 
      // Scheme-specific normalizer
  private final PathNormalizer pathNormalizer; // Scheme-specific normalizer
  private final boolean normalizeAuthority; 
      // true if authority should be normalized
  private String normalized;  // normalized URI

  URIParser(String s, 
            PortRangeNormalizer portRangeNormalizer, 
            PathNormalizer pathNormalizer,
            boolean normalizeAuthority) {
    if (s == null) {
      throw new NullPointerException();
    }
    input = s;
    this.portRangeNormalizer = portRangeNormalizer;
    this.pathNormalizer = pathNormalizer;
    this.normalizeAuthority = normalizeAuthority;
    normalized = "";
    parse();
  }

  // -- Field accessor methods --

  public String getURI() {
    return normalized;
  }

  public String getScheme() {
    return scheme;
  }
  public String getFragment() {
    return fragment;
  }

  // Hierarchical URI components: [//<authority>]<path>[?<query>]

  // Registry or server
  public String getAuthority() {
    return authority;
  }

  public String getUserInfo() {
    return userInfo;
  }

  // null ==> registry-based
  public String getHost() {
    return host;
  }

  public int[] getPortRange() {
    return portrange;
  }

  // Remaining components of hierarchical URIs
  // null ==> opaque
  public String getPath() {
    return path;		
  }
  public String getQuery() {
    return query;
  }

  public String getSchemeSpecificPart() {
    return schemeSpecificPart;
  }

  // -- Methods for throwing IllegalArgumentException in various ways --

  private void fail(String reason) throws IllegalArgumentException {
    throw new IllegalArgumentException(reason + " : " + input);
  }

  private void fail(String reason, int p) throws IllegalArgumentException {
    throw new IllegalArgumentException(reason + " : " + input);
  }

  private void failExpecting(String expected, int p) 
   throws IllegalArgumentException {
    fail("Expected " + expected, p);
  }
  
  private void failExpecting(String expected, String prior, int p)
   throws IllegalArgumentException {
    fail("Expected " + expected + " following " + prior, p);
  }
  

  // -- Simple access to the input string --

  // Return a substring of the input string
  //
  private String substring(int start, int end) {
    return input.substring(start, end);
  }
  
  // Return the char at position p,
  // assuming that p < input.length()
  //
  private char charAt(int p) {
    return input.charAt(p);
  }
  
  // Tells whether start < end and, if so, whether charAt(start) == c
  //
  private boolean at(int start, int end, char c) {
    return (start < end) && (charAt(start) == c);
  }
  
  // Tells whether start + s.length() < end and, if so,
  // whether the chars at the start position match s exactly
  //
  private boolean at(int start, int end, String s) {
    int p = start;
    int sn = s.length();
    if (sn > end - p)
      return false;
    int i = 0;
    while (i < sn) {
      if (charAt(p++) != s.charAt(i)) {
        break;
      }
      i++;
    }
    return (i == sn);
  }
  
  // -- Scanning --

  // The various scan and parse methods that follow use a uniform
  // convention of taking the current start position and end index as
  // their first two arguments.  The start is inclusive while the end is
  // exclusive, just as in the String class, i.e., a start/end pair
  // denotes the left-open interval [start, end) of the input string.
  //
  // These methods never proceed past the end position.  They may return
  // -1 to indicate outright failure, but more often they simply return
  // the position of the first char after the last char scanned.  Thus
  // a typical idiom is
  //
  //     int p = start;
  //     int q = scan(p, end, ...);
  //     if (q > p)
  //         // We scanned something
  //         ...;
  //     else if (q == p)
  //         // We scanned nothing
  //         ...;
  //     else if (q == -1)
  //         // Something went wrong
  //         ...;


  // Scan a specific char: If the char at the given start position is
  // equal to c, return the index of the next char; otherwise, return the
  // start position.
  //
  private int scan(int start, int end, char c) {
    if ((start < end) && (charAt(start) == c))
      return start + 1;
    return start;
  }

  // Scan forward from the given start position.  Stop at the first char
  // in the err string (in which case -1 is returned), or the first char
  // in the stop string (in which case the index of the preceding char is
  // returned), or the end of the input string (in which case the length
  // of the input string is returned).  May return the start position if
  // nothing matches.
  //
  private int scan(int start, int end, String err, String stop) {
    int p = start;
    while (p < end) {
      char c = charAt(p);
      if (err.indexOf(c) >= 0)
        return -1;
      if (stop.indexOf(c) >= 0)
        break;
      p++;
    }
    return p;
  }
  
  // Scan a potential escape sequence, starting at the given position,
  // with the given first char (i.e., charAt(start) == c).
  //
  // This method assumes that if escapes are allowed then visible
  // non-US-ASCII chars are also allowed.
  //
  private int scanEscape(int start, int n, char first)
   throws IllegalArgumentException {
    int p = start;
    char c = first;
    if (c == '%') {
      // Process escape pair
      if ((p + 3 <= n)
          && match(charAt(p + 1), L_HEXDIG, H_HEXDIG)
          && match(charAt(p + 2), L_HEXDIG, H_HEXDIG)) {
        return p + 3;
      }
      fail("Malformed pct-encoded string", p);
    }
    return p;
  }
  
  // Scan chars that match the given mask pair
  //
  private int scan(int start, int n, long lowMask, long highMask)
   throws IllegalArgumentException {
    int p = start;
    while (p < n) {
      char c = charAt(p);
      if (match(c, lowMask, highMask)) {
        p++;
        continue;
      }
      if ((lowMask & L_PCT_ENCODED) != 0) {
        int q = scanEscape(p, n, c);
        if (q > p) {
          p = q;
          continue;
        }
      }
      break;
    }
    return p;
  }
  
  // Check that each of the chars in [start, end) matches the given mask
  //
  private void checkChars(int start, int end, long lowMask, long highMask, 
                          String what)
   throws IllegalArgumentException {
    int p = scan(start, end, lowMask, highMask);
    if (p < end)
      fail("Illegal character in " + what, p);
  }

  // Check that the char at position p matches the given mask
  //
  private void checkChar(int p, long lowMask, long highMask, String what)
   throws IllegalArgumentException {
    checkChars(p, p + 1, lowMask, highMask, what);
  }

  private void append(String s) {
    normalized = normalized + s;
  }

  private String normalizedSubstring(int n) {
    return normalized.substring(n);
  }

  // -- Parsing --

  //
  // URI = scheme ":" hier-part [ "?" query ] [ "#" fragment ]
  //
  private void parse() throws IllegalArgumentException {
    int ssp;			// Start of scheme-specific part
    int n = input.length();
    int p = scan(0, n, "", ":");
    if ((p > 0) && at(p, n, ':')) {
      // scheme        = ALPHA *( ALPHA / DIGIT / "+" / "-" / "." )
      checkChar(0, L_ALPHA, H_ALPHA, "scheme name");
      checkChars(1, p, L_SCHEME, H_SCHEME, "scheme name");
      scheme = substring(0, p).toLowerCase();
      // normalize scheme to lowecase as prescribed by RFC 3986
      append(scheme + ":");
      p++;			// Skip ':'

      ssp = p;

      int q = scan(p, n, "", "?#");
      p = parseHierarchical(p, q);

      if ((p < n) && at(p, n, '?')) {
        // Skip '?'
        p++;
        q = scan(p, n, "", "#");
        p = parseQuery(p, q);
      }

      if ((p < n) && at(p, n, '#')) {
        // Skip '#'
        p++;

        p = parseFragment(p, n);
      }

      schemeSpecificPart = normalizedSubstring(ssp);

      normalizePercentEncoded();
    } else {
      failExpecting("scheme name", 0);
    }
  }

  //
  // RFC 3986 requires that hexadecimal digits in percent-encoding 
  // triplets are normalized to use uppercase letters.
  //
  private void normalizePercentEncoded() {
    int n = normalized.length();
    int p = 0;
    int q = p;
    String s = "";
    while (p + 2 < n) {
      if (normalized.charAt(p) == '%' && 
          match(normalized.charAt(p + 1), L_HEXDIG, H_HEXDIG)
          && match(normalized.charAt(p + 2), L_HEXDIG, H_HEXDIG)) {
        String triplet = normalized.substring(p, p + 3).toUpperCase();
        s += normalized.substring(q, p) + triplet;
        p += 2;
        q = p + 1;
      }
      p++;
    }
    if (q < n) {
      s += normalized.substring(q, n);
    }
    if (!normalized.toUpperCase().equals(s.toUpperCase())) {
      fail("Internal parser error: normalization failed");
    }
    normalized = s;
  }

  // 
  // hier-part     = "//" authority path-abempty
  //            / path-absolute
  //            / path-rootless
  //            / path-empty
  //
  private int parseHierarchical(int start, int n)
   throws IllegalArgumentException {
    int p = start;
    boolean authorityPresent = at(p, n, '/') && at(p + 1, n, '/');
    if (normalizeAuthority || authorityPresent) {
      if (authorityPresent) {
        p += 2;
      }
      int q = scan(p, n, "", "/");

      append("//");

      p = parseAuthority(p, q);
      if (p != q && !at(p, n, '/')) {
        failExpecting("absolute or empty path", 0);
      }
    }

    // path-abempty  = *( "/" segment )
    // path-absolute = "/" [ segment-nz *( "/" segment ) ]
    // path-rootless = segment-nz *( "/" segment )
    // path-empty    = 0<pchar>
    //
    // segment       = *pchar
    // segment-nz    = 1*pchar
    //
    // pchar         = unreserved / pct-encoded / sub-delims / ":" / "@"
    checkChars(p, n, L_PATH, H_PATH, "path");
    path = substring(p, n);
    if (pathNormalizer != null) {
      path = pathNormalizer.normalize(path);
    }
    append(path);
    p = n;
    return p;
  }

  //
  // authority     = [ userinfo "@" ] host [ ":" portspec ]
  //
  private int parseAuthority(int start, int n)
   throws IllegalArgumentException {
    int p = start;
    int q = scan(p, n, L_USERINFO, H_USERINFO);

    if (q > p && at(q, n, '@')) {
      userInfo = substring(p, q);
      // skip '@'
      p = q + 1;

      append(userInfo + "@");
    }

    p = parseHost(p, n);

    q = parsePortRange(p, n);
    if (q > p) {
      p = q;
    }

    return p;
  }

  // 
  // host          = IP-literal / IPv4address / reg-name
  //
  private int parseHost(int start, int n) {
    int p = start;
    int q = p;
    // IP-literal    = "[" ( IPv6address / IPvFuture  ) "]"
    if (at(p, n, '[')) {
      // skip '['
      p++;
      q = scan(p, n, "", "]");
      if (q > p && at(q, n, ']')) {
        // IPvFuture     = "v" 1*HEXDIG "." 1*( unreserved / sub-delims / ":" )
        if (at(p, n, 'v') || at(p, n, 'V')) {
          p = parseIPvFuture(p, q);
          if (p < q) {
            fail("Malformed IPvFuture address", p);
          }
        } else {
          p = parseIPv6Address(p, q);
          if (p < q) {
            fail("Malformed IPv6 address", p);
          }
        }
      } else {
        failExpecting("closing bracket for IPv6 or IPvFuture address", q);
      }
      // skip ']'
      p++;
    } else {
      q = parseIPv4Address(p, n);
      if (q <= p)
        q = parseRegname(p, n);
      p = q;
    }
    
    return p;
  }

  // 
  // portspec = port / port "-" [ port ] / "-" port / "*" / ""
  // port = 1*DIGIT
  //
  private int parsePortRange(int start, int n) {
    int[] range = { -1, -1 };
    int p = start;
    int q = scan(p, n, "", "/");

    String s = substring(p, q);
    portrangeString = s;

    String norm = null;

    if (s.equals(":*")) {
      range[0] = 0;
      range[1] = 65535;
      norm = ":0-65535";
    } else if (portRangeNormalizer != null) {
      norm = portRangeNormalizer.normalize(host, s, range);
    }

    if (norm != null) {
      append(norm);
    } else if (at(p, n, ':')) {
      // skip ':'
      p++;

      int r = scan(p, n, "", "-");
      int low = PORT_MIN;
      int high = PORT_MAX;

      try {
        if (r > p) {
          low = Integer.parseInt(substring(p, r));
          if (r >= n) {
            high = low;
          }
        } 
          
        if (r + 1 < n) {
          high = Integer.parseInt(substring(r + 1, n));
        }
      } catch (NumberFormatException x) {
        fail("Malformed port range", p);
      }

      if (low < 0 || high < 0 || high < low) { 
        fail("Invalid port range", p);
      }

      range[0] = low;
      range[1] = high;

      if (portRangeNormalizer != null) {
        norm = portRangeNormalizer.normalize(range);
      } 

      if (norm == null) {
        if (range[0] == range[1]) {
          norm = ":" + range[0];
        } else {
          norm = ":" + range[0] + "-" + range[1];
        }
      }
       
      append(norm);
    } else if (!"".equals(s)) {
      fail("Malformed port range", p);
    }

    p = q;

    portrange = range;

    return p;
  }

  // Scan a string of decimal digits whose value fits in a byte
  //
  private int scanByte(int start, int n) throws IllegalArgumentException {
    int p = start;
    int q = scan(p, n, L_DIGIT, H_DIGIT);
    if (q <= p) return q;
    String s = substring(p, q);
    int value = Integer.parseInt(s);
    if (value > 255) return p;
    // This check guarantees there are no leading zeroes
    // and normalization not needed
    if (!String.valueOf(value).equals(s)) return p;
    return q;
  }

  // Scan an IPv4 address.
  //
  // If the strict argument is true then we require that the given
  // interval contain nothing besides an IPv4 address; if it is false
  // then we only require that it start with an IPv4 address.
  //
  // If the interval does not contain or start with (depending upon the
  // strict argument) a legal IPv4 address characters then we return -1
  // immediately; otherwise we insist that these characters parse as a
  // legal IPv4 address and throw an exception on failure.
  //
  // We assume that any string of decimal digits and dots must be an IPv4
  // address.  It won't parse as a hostname anyway, so making that
  // assumption here allows more meaningful exceptions to be thrown.
  //
  private int scanIPv4Address(int start, int n, boolean strict)
   throws IllegalArgumentException {
    int p = start;
    int q;
    int m = scan(p, n, L_DIGIT | L_DOT, H_DIGIT | H_DOT);
    if ((m <= p) || (strict && (m != n)))
      return -1;
    for (;;) {
      // Per RFC2732: At most three digits per byte
      // Further constraint: Each element fits in a byte
      if ((q = scanByte(p, m)) <= p) break;   p = q;
      if ((q = scan(p, m, '.')) <= p) break;  p = q;
      if ((q = scanByte(p, m)) <= p) break;   p = q;
      if ((q = scan(p, m, '.')) <= p) break;  p = q;
      if ((q = scanByte(p, m)) <= p) break;   p = q;
      if ((q = scan(p, m, '.')) <= p) break;  p = q;
      if ((q = scanByte(p, m)) <= p) break;   p = q;
      if (q < m) break;
      return q;
    }
    fail("Malformed IPv4 address", q);
    return -1;
  }
  
  // Take an IPv4 address: Throw an exception if the given interval
  // contains anything except an IPv4 address
  //
  private int takeIPv4Address(int start, int n, String expected)
   throws IllegalArgumentException {
    int p = scanIPv4Address(start, n, true);
    if (p <= start)
      failExpecting(expected, start);
    return p;
  }
  
  // Attempt to parse an IPv4 address, returning -1 on failure but
  // allowing the given interval to contain [:<characters>] after
  // the IPv4 address.
  //
  private int parseIPv4Address(int start, int n) {
    int p;
    
    try {
      p = scanIPv4Address(start, n, false);
    } catch (IllegalArgumentException x) {
      return -1;
    }
    
    if (p > start && p < n) {
      // IPv4 address is followed by something - check that
      // it's a ":" as this is the only valid character to
      // follow an address.
      if (charAt(p) != ':') {
        p = -1;
      }
    }
    
    if (p > start) {
      host = substring(start, p);
      append(host);
    }
    
    return p;
  }

  //
  // reg-name = *( unreserved / pct-encoded / sub-delims )
  // 
  private int parseRegname(int start, int n)
   throws IllegalArgumentException {
    int p = scan(start, n, L_REGNAME, H_REGNAME);
    
    if ((p < n) && !at(p, n, ':'))
      fail("Illegal character in hostname", p);
    
    // normalize hostname to lowercase as prescribed by RFC 3986
    host = substring(start, p).toLowerCase();
    if (normalizeAuthority && "localhost".equals(host)) {
      host = "";
    }
    append(host);

    return p;
  }
  

  private String canonicalIPv6Name(String name) {
    try {
      byte[] address = textToNumericFormatIPv6(name);
      // IPv4-mapped IPv6 address is returned as a plain IPv4 address
      if (address.length == IN4ADDRSZ) {
        return numericToTextFormatIPv4(address);
      } else {
        return numericToTextFormatIPv6(address);
      }
    } catch (RuntimeException e) {
      throw new IllegalArgumentException("Malformed IPv6 address");
    }
  }

  // IPvFuture     = "v" 1*HEXDIG "." 1*( unreserved / sub-delims / ":" )
  private int parseIPvFuture(int start, int n)
   throws IllegalArgumentException {
    int p = start;
    if (!at(p, n, 'v') && !at(p, n, 'V')) {
      failExpecting("IPvFuture address", p);
    }
    // skip 'v'
    p++;
    int q = scan(p, n, L_HEXDIG, H_HEXDIG);
    if (p >= q || !at(q, n, '.')) {
      failExpecting("IPvFuture address", p);
    }
    // skip '.'
    p = q + 1;
    q = scan(p, n, L_IPvFUTURE, H_IPvFUTURE);
    if (p >= q) {
      failExpecting("IPvFuture address", p);
    }

    // Including braces
    host = substring(start-1, q+1);
    return q;
  }
  
  // IPv6 address parsing, from RFC 3986
  //
  // IPv6address   =                            6( h16 ":" ) ls32
  //               /                       "::" 5( h16 ":" ) ls32
  //               / [               h16 ] "::" 4( h16 ":" ) ls32
  //               / [ *1( h16 ":" ) h16 ] "::" 3( h16 ":" ) ls32
  //               / [ *2( h16 ":" ) h16 ] "::" 2( h16 ":" ) ls32
  //               / [ *3( h16 ":" ) h16 ] "::"    h16 ":"   ls32
  //               / [ *4( h16 ":" ) h16 ] "::"              ls32
  //               / [ *5( h16 ":" ) h16 ] "::"              h16
  //               / [ *6( h16 ":" ) h16 ] "::"
  //
  // h16           = 1*4HEXDIG
  // ls32          = ( h16 ":" h16 ) / IPv4address
  //
  
  private int ipv6byteCount = 0;
  
  private int parseIPv6Address(int start, int n)
   throws IllegalArgumentException {
    int p = start;
    int q;
    boolean compressedZeros = false;
    
    q = scanHexSeq(p, n);
    
    if (q > p) {
      p = q;
      if (at(p, n, "::")) {
        compressedZeros = true;
        p = scanHexPost(p + 2, n);
      } else if (at(p, n, ':')) {
        p = takeIPv4Address(p + 1,  n, "IPv4 address");
        ipv6byteCount += 4;
      }
    } else if (at(p, n, "::")) {
      compressedZeros = true;
      p = scanHexPost(p + 2, n);
    }
    if (ipv6byteCount > 16)
      fail("IPv6 address too long", start);
    if (!compressedZeros && ipv6byteCount < 16) 
      fail("IPv6 address too short", start);
    if (compressedZeros && ipv6byteCount == 16)
      fail("Malformed IPv6 address", start);
    
    {
      String hostComponent = substring(start, p);
      host = "[" + canonicalIPv6Name(hostComponent) + "]";
      // normalize scheme to lowecase as prescribed by RFC 3986
      append(host);
    }
    return p;
  }
  
  private int scanHexPost(int start, int n)
   throws IllegalArgumentException {
    int p = start;
    int q;

    if (p == n)
      return p;

    q = scanHexSeq(p, n);
    if (q > p) {
      p = q;
      if (at(p, n, ':')) {
        p++;
        p = takeIPv4Address(p, n, "hex digits or IPv4 address");
        ipv6byteCount += 4;
      }
    } else {
      p = takeIPv4Address(p, n, "hex digits or IPv4 address");
      ipv6byteCount += 4;
    }
    return p;
  }

  // Scan a hex sequence; return -1 if one could not be scanned
  //
  private int scanHexSeq(int start, int n) throws IllegalArgumentException {
    int p = start;
    int q;

    q = scan(p, n, L_HEXDIG, H_HEXDIG);
    if (q <= p)
      return -1;
    if (at(q, n, '.'))		// Beginning of IPv4 address
      return -1;
    if (q > p + 4)
      fail("IPv6 hexadecimal digit sequence too long", p);
    ipv6byteCount += 2;
    p = q;
    while (p < n) {
      if (!at(p, n, ':'))
        break;
      if (at(p + 1, n, ':'))
        break;		// "::"
      p++;
      q = scan(p, n, L_HEXDIG, H_HEXDIG);
      if (q <= p)
        failExpecting("digits for an IPv6 address", p);
      if (at(q, n, '.')) {	// Beginning of IPv4 address
        p--;
        break;
      }
      if (q > p + 4)
        fail("IPv6 hexadecimal digit sequence too long", p);
      ipv6byteCount += 2;
      p = q;
    }
    
    return p;
  }

  // Parse query:
  // query         = *( pchar / "/" / "?" )
  private int parseQuery(int start, int n) {
    checkChars(start, n, L_QUERY, H_QUERY, "query component");
    query = substring(start, n);
    append("?" + query);
    return n;
  }

  // Parse fragment:
  // fragment      = *( pchar / "/" / "?" )
  private int parseFragment(int start, int n) {
    checkChars(start, n, L_QUERY, H_QUERY, "fragment component");
    fragment = substring(start, n);
    append("#" + fragment);
    return n;
  }

  void checkNoFragment() {
    if (fragment != null && !"".equals(fragment)) {
      fail("Fragment component not allowed");
    }
  }

  void checkNoUserInfo() {
    if (userInfo != null && !"".equals(userInfo)) {
      fail("Userinfo component not allowed");
    }
  }

  void checkNoPath() {
    if (path != null && !"".equals(path)) {
      fail("Path component not allowed");
    }
  }

  void checkNoQuery() {
    if (query != null && !"".equals(query)) {
      fail("Query component not allowed");
    }
  }

  void checkNoPortRange() {
    if (isPortRangeSpecified()) {
      fail("Port range component not allowed");
    }
  }
  
  void checkNoHost() {
    if (host != null && !"".equals(host)) {
      fail("Host component not allowed");
    }
  }
  
  void checkPortRange() {
    if (!isPortRangeSpecified()) {
      fail("Port range not specified");
    }
  }
  
  boolean isPortRangeSpecified() {
    return portrangeString != null && !"".equals(portrangeString) && 
      !":".equals(portrangeString);
  }

  // Compute the low-order mask for the characters in the given string
  private static long lowMask(String chars) {
    int n = chars.length();
    long m = 0;
    for (int i = 0; i < n; i++) {
      char c = chars.charAt(i);
      if (c < 64)
        m |= (1L << c);
    }
    return m;
  }

  // Compute the high-order mask for the characters in the given string
  private static long highMask(String chars) {
    int n = chars.length();
    long m = 0;
    for (int i = 0; i < n; i++) {
      char c = chars.charAt(i);
      if ((c >= 64) && (c < 128))
        m |= (1L << (c - 64));
    }
    return m;
  }

  // Compute a low-order mask for the characters
  // between first and last, inclusive
  private static long lowMask(char first, char last) {
    long m = 0;
    if (first < 64) {
      int f = Math.max(Math.min(first, 63), 0);
      int l = Math.max(Math.min(last, 63), 0);
      for (int i = f; i <= l; i++)
        m |= 1L << i;
    }
    return m;
  }

  // Compute a high-order mask for the characters
  // between first and last, inclusive
  private static long highMask(char first, char last) {
    long m = 0;
    if (last >= 64) {
      int f = Math.max(Math.min(first, 127), 64) - 64;
      int l = Math.max(Math.min(last, 127), 64) - 64;
      for (int i = f; i <= l; i++)
        m |= 1L << i;
    }
    return m;
  }

  // Tell whether the given character is permitted by the given mask pair
  private static boolean match(char c, long lowMask, long highMask) {
    if (c < 64)
      return ((1L << c) & lowMask) != 0;
    if (c < 128)
      return ((1L << (c - 64)) & highMask) != 0;
    return false;
  }

  private final static int IN4ADDRSZ = 4;

  /* 
   * Converts IPv4 binary address into a string suitable for presentation.
   *
   * @param src a byte array representing an IPv4 numeric address
   * @return a String representing the IPv4 address in 
   *         textual representation format
   */
  private static String numericToTextFormatIPv4(byte[] src) {
    return (src[0] & 0xff) + "." + (src[1] & 0xff) + "." + 
      (src[2] & 0xff) + "." + (src[3] & 0xff);
  }

  /*
   * Converts IPv4 address in its textual presentation form 
   * into its numeric binary form.
   * 
   * @param src a String representing an IPv4 address in standard format
   * @return a byte array representing the IPv4 numeric address
   */
  private static byte[] textToNumericFormatIPv4(String src) {
    if (src.length() == 0) {
      return null;
    }
    
    int octets;
    char ch;
    byte[] dst = new byte[IN4ADDRSZ];
    char[] srcb = src.toCharArray();
    boolean saw_digit = false;
    
    octets = 0;
    int i = 0;
    int cur = 0;
    while (i < srcb.length) {
      ch = srcb[i++];
      if (Character.isDigit(ch)) {
        // note that Java byte is signed, so need to convert to int
        int sum = (dst[cur] & 0xff)*10
          + (Character.digit(ch, 10) & 0xff);
        
        if (sum > 255)
          return null;
        
        dst[cur] = (byte)(sum & 0xff);
        if (! saw_digit) {
          if (++octets > IN4ADDRSZ)
            return null;
          saw_digit = true;
        }
      } else if (ch == '.' && saw_digit) {
        if (octets == IN4ADDRSZ)
          return null;
        cur++;
        dst[cur] = 0;
        saw_digit = false;
      } else
        return null;
    }
    if (octets < IN4ADDRSZ)
      return null;
    return dst;
  }

  private final static int IN6ADDRSZ = 16;

  private final static int INT16SZ = 2;
  /*
   * Convert IPv6 binary address into presentation (printable) format.
   *
   * @param src a byte array representing the IPv6 numeric address
   * @return a String representing an IPv6 address in 
   *         textual representation format
   */
  private static String numericToTextFormatIPv6(byte[] src) {
    StringBuffer sb = new StringBuffer(39);
    for (int i = 0; i < (IN6ADDRSZ / INT16SZ); i++) {
      sb.append(Integer.toHexString(((src[i<<1]<<8) & 0xff00)
                                    | (src[(i<<1)+1] & 0xff)));
      if (i < (IN6ADDRSZ / INT16SZ) -1 ) {
        sb.append(":");
      }
    }
    return sb.toString();
  }

  /* 
   * Convert IPv6 presentation level address to network order binary form.
   * credit:
   *  Converted from C code from Solaris 8 (inet_pton)
   *
   * @param src a String representing an IPv6 address in textual format
   * @return a byte array representing the IPv6 numeric address
   */
  private static byte[] textToNumericFormatIPv6(String src) {
    if (src.length() == 0) {
      return null;
    }

    int colonp;
    char ch;
    boolean saw_xdigit;
    int val;
    char[] srcb = src.toCharArray();
    byte[] dst = new byte[IN6ADDRSZ];

    colonp = -1;
    int i = 0, j = 0;
    /* Leading :: requires some special handling. */
    if (srcb[i] == ':')
      if (srcb[++i] != ':')
        return null;
    int curtok = i;
    saw_xdigit = false;
    val = 0;
    while (i < srcb.length) {
      ch = srcb[i++];
      int chval = Character.digit(ch, 16);
      if (chval != -1) {
        val <<= 4;
        val |= chval;
        if (val > 0xffff)
          return null;
        saw_xdigit = true;
        continue;
      }
      if (ch == ':') {
        curtok = i;
        if (!saw_xdigit) {
          if (colonp != -1)
            return null;
          colonp = j;
          continue;
        } else if (i == srcb.length) {
          return null;
        }
        if (j + INT16SZ > IN6ADDRSZ)
          return null;
        dst[j++] = (byte) ((val >> 8) & 0xff);
        dst[j++] = (byte) (val & 0xff);
        saw_xdigit = false;
        val = 0;
        continue;
      }
      if (ch == '.' && ((j + IN4ADDRSZ) <= IN6ADDRSZ)) {
        byte[] v4addr = textToNumericFormatIPv4(src.substring(curtok));
        if (v4addr == null) {
          return null;
        }
        for (int k = 0; k < IN4ADDRSZ; k++) {
          dst[j++] = v4addr[k];
        }
        saw_xdigit = false;
        break;	/* '\0' was seen by inet_pton4(). */
      }
      return null;
    }
    if (saw_xdigit) {
      if (j + INT16SZ > IN6ADDRSZ)
        return null;
      dst[j++] = (byte) ((val >> 8) & 0xff);
      dst[j++] = (byte) (val & 0xff);
    }
    
    if (colonp != -1) {
      int n = j - colonp;
      
      if (j == IN6ADDRSZ)
        return null;
      for (i = 1; i <= n; i++) {
        dst[IN6ADDRSZ - i] = dst[colonp + n - i];
        dst[colonp + n - i] = 0;
      }
      j = IN6ADDRSZ;
    }
    if (j != IN6ADDRSZ)
      return null;
    byte[] newdst = convertFromIPv4MappedAddress(dst);
    if (newdst != null) {
      return newdst;
    } else {
      return dst;
    }
  }

  /**
   * Utility routine to check if the InetAddress is an
   * IPv4 mapped IPv6 address. 
   *
   * @return a <code>boolean</code> indicating if the InetAddress is 
   * an IPv4 mapped IPv6 address; or false if address is IPv4 address.
   */
  private static boolean isIPv4MappedAddress(byte[] addr) {
    if (addr.length < IN6ADDRSZ) {
      return false;
    }
    if ((addr[0] == 0x00) && (addr[1] == 0x00) && 
        (addr[2] == 0x00) && (addr[3] == 0x00) && 
        (addr[4] == 0x00) && (addr[5] == 0x00) && 
        (addr[6] == 0x00) && (addr[7] == 0x00) && 
        (addr[8] == 0x00) && (addr[9] == 0x00) && 
        (addr[10] == (byte)0xff) && 
        (addr[11] == (byte)0xff))  {   
      return true;
    }
    return false;
  }

  private static byte[] convertFromIPv4MappedAddress(byte[] addr) {
    if (isIPv4MappedAddress(addr)) {
      byte[] newAddr = new byte[IN4ADDRSZ];
      System.arraycopy(addr, 12, newAddr, 0, IN4ADDRSZ);
      return newAddr;
    }
    return null;
  }
}


