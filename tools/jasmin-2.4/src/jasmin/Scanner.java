/* --- Copyright Jonathan Meyer 1996. All rights reserved. -----------------
 > File:        jasmin/src/jasmin/Scanner.java
 > Purpose:     Tokenizer for Jasmin
 > Author:      Jonathan Meyer, 10 July 1996
 */

/* Scanner.java - class for tokenizing Jasmin files. This is rather
 * cheap and cheerful.
*/

package jasmin;

import jas.*;
import java_cup.runtime.*;
import java.util.*;
import java.io.Reader;

class Scanner {
  Reader inp;

    // single lookahead character
    int next_char;

    // temporary buffer
    char chars[];
    static private int chars_size = 512;

    // Whitespace characters
    static final String WHITESPACE = " \n\t\r";

    // Separator characters
    static final String SEPARATORS = WHITESPACE + ":=";

    /*
    // Character can be present in signature
    static final String SIGCHARS = ";:()[/.^*+-<>@";
    */

    // used for error reporting to print out where an error is on the line
    public int line_num, token_line_num, char_num, int_char_num, int_line_num;
    public StringBuffer line;
    public String int_line;

    // used by the .set directive to define new variables.
    public Hashtable dict = new Hashtable();

    //
    // returns true if a character code is a whitespace character
    //
    protected static boolean whitespace(int c) {
        return (WHITESPACE.indexOf(c) != -1);
    }

    //
    // returns true if a character code is a separator character
    //
    protected static boolean separator(int c) {
        return (c == -1 || SEPARATORS.indexOf(c) != -1);
    }


    //
    // Advanced the input by one character
    //
    protected void advance() throws java.io.IOException
    {
        next_char = inp.read();
        switch (next_char) {
        case -1:  // EOF
            if (char_num == 0) {
                char_num = -1;
                break;
            }
            next_char = '\n';
            // pass thru
        case '\n': // a new line
            line_num++;
            char_num = 0;
            break;
        default:
            line.append((char)next_char);
            char_num++;
            return;
        }
        line.setLength(0);
    }

    //
    // initialize the scanner
    //
    public Scanner(Reader i) throws java.io.IOException, jasError
    {
        inp = i;
        line_num = 1;
        char_num = 0;
        line = new StringBuffer();
        chars = new char[chars_size];
        next_char = 0;  // no start comment
        skip_empty_lines();
        if ( next_char == -1 )
            throw new jasError("empty source file");
    }

    private void chars_expand()
    {
        char temp[] = new char[chars_size * 2];
        System.arraycopy(chars, 0, temp, 0, chars_size);
        chars_size *= 2;
        chars = temp;
    }

    private void skip_empty_lines() throws java.io.IOException
    {
        for (;;) {
            if (next_char != ';') {
                do { advance(); } while (whitespace(next_char));
                if (next_char != ';')
                    return;
            }
            do {
                advance();
                if (next_char == -1)
                   return;
            } while (next_char != '\n');
        }
    }

    private char uniEscape()
                throws java.io.IOException, jasError
    {
        int res = 0;
        for(int i = 0; i < 4; i++) {
            advance();
            if(next_char == -1)
                return 0;

            int tmp = Character.digit((char)next_char, 16);
            if (tmp == -1)
                throw new jasError("Bad '\\u' escape sequence");
            res = (res << 4) | tmp;
        }
        return (char)res;
    }

    private char nameEscape()
                throws java.io.IOException, jasError
    {
        advance();
        if (next_char != 'u')
            throw new jasError("Only '\\u' escape sequence allowed in names");
        char chval = uniEscape();
        if (next_char == -1)
            throw new jasError("Left over '\\u' escape sequence");
        /*
        if (   SIGCHARS.indexOf(chval) == -1
            && (   !Character.isJavaIdentifierPart(chval)
                || Character.isIdentifierIgnorable(chval)))
        {
            throw new jasError("Invalid unicode char from name/signature");
        }
        */
        return chval;
    }

    //
    // recognize and return the next complete token
    //
    public token next_token()
                throws java.io.IOException, jasError
    {
        token_line_num = line_num;

        for (;;) switch (next_char) {
            case ';':  // a comment
            case '\n':
                // return single SEP token (skip multiple newlines
                // interspersed with whitespace or comments)
                skip_empty_lines();
                token_line_num = line_num;
                return new token(sym.SEP);

            case -1:                // EOF token
                char_num = -1;
                return new token(sym.EOF);

            case '-': case '+':
            case '0': case '1': case '2': case '3': case '4':
            case '5': case '6': case '7': case '8': case '9':
            case '.':                       // a number
            {
                int pos = 0;
                do {
                    chars[pos] = (char)next_char;
                    pos++;
                    if(pos == chars_size) chars_expand();
                    advance();
                }while(!separator(next_char));

                String str = new String(chars, 0, pos);
                token tok;

                // This catches directives like ".method"
                if ((tok = ReservedWords.get(str)) != null)
                    return tok;

                Number num;
                try {
                    num = ScannerUtils.convertNumber(str);
                } catch (NumberFormatException e) {
                    if (chars[0] != '.')
                        throw new jasError("Badly formatted number");
                    throw new jasError("Unknown directive or badly formed number.");
                }

                if (num instanceof Integer) {
                    int_line     = line.toString();
                    int_line_num = token_line_num;
                    int_char_num = char_num;
                    return new int_token(sym.Int, num.intValue());
                }
                return new num_token(sym.Num, num);
            }

            case '"':   // quoted string
            {
                boolean already = false;
                for (int pos = 0; ; ) {
                    if (already) already = false;
                    else advance();

                    if (next_char == '"') {
                        advance(); // skip close quote
                        return new str_token(sym.Str, new String(chars, 0, pos));
                    }

                    if(next_char == -1)
                        throw new jasError("Unterminated string");

                    char chval = (char)next_char;

                    if (chval == '\\') {
                        advance();
                        switch (next_char) {
                        case -1: already = true; continue;
                        case 'n':   chval = '\n'; break;
                        case 'r':   chval = '\r'; break;
                        case 't':   chval = '\t'; break;
                        case 'f':   chval = '\f'; break;
                        case 'b':   chval = '\b'; break;
                        case '"' :  chval = '"';  break;
                        case '\'' : chval = '\''; break;
                        case '\\' : chval = '\\'; break;

                        case 'u':
                            chval = uniEscape();
                            if(next_char == -1) {
                                already = true;
                                continue;
                            }
                            break;

                        case '0': case '1': case '2': case '3':
                        case '4': case '5': case '6': case '7':
                        {
                            int res = next_char&7;
                            advance();
                            if (next_char < '0' || next_char > '7')
                                already = true;
                            else {
                                res = res*8 + (next_char&7);
                                advance();
                                if (next_char < '0' || next_char > '7')
                                    already = true;
                                else {
                                    int val = res*8 + (next_char&7);
                                    if (val >= 0x100)
                                        already = true;
                                    else
                                        res = val;
                                }
                            }
                            chval = (char)res;
                        }
                        break;

                        default:
                            throw new jasError("Bad backslash escape sequence");
                        }
                    }
                    chars[pos] = chval;
                    pos++;
                    if(pos == chars_size) chars_expand();
                }
            }

            case '\'':  // quotation for overloading reserved words
                for (int pos = 0; ; ) {
                    advance();
                    if (separator(next_char))
                        throw new jasError("Unterminated ''-enclosed name");
                    if (next_char == '\'') {
                        if (pos == 0)
                            throw new jasError("Empty ''-enclosed name");
                        advance(); // skip close quote
                        if (!separator(next_char))
                            throw new jasError("Not separator after ''-enclosed name");
                        return new str_token(sym.Word, new String(chars, 0, pos));
                    }
                    char chval = (char)next_char;
                    if (next_char == '\\')
                        chval = nameEscape();
                    chars[pos] = chval;
                    pos++;
                    if(pos == chars_size) chars_expand();
                }

            case ' ':
            case '\t':
            case '\r':              // whitespace
                advance();
                break;

            case '=':               // EQUALS token
                advance();
                return new token(sym.EQ);

            case ':':               // COLON token
                advance();
                return new token(sym.COLON);

            default:
            {
                // read up until a separatorcharacter
               int pos = 0;
               boolean only_name = false;

               do {
                  char chval = (char)next_char;
                  if (next_char == '\\') {
                      chval = nameEscape();
                      only_name = true;
                  }
                  chars[pos] = chval;
                  pos++;
                  if(pos == chars_size) chars_expand();
                  advance();
                }while(!separator(next_char));
                // convert the byte array into a String
                String str = new String(chars, 0, pos);

                if (!only_name) {
                    token tok;

                    // Jasmin keyword or directive ?
                    if ((tok = ReservedWords.get(str)) != null)
                        return tok;

                    // its a JVM instruction ?
                    if (InsnInfo.contains(str))
                        return new str_token(sym.Insn, str);

                    if (str.charAt(0) == '$') {
                        String s = str.substring(1);
                        Object v;
                        int n = 10;
                        boolean neg = false;
                        boolean sign = false;
                        switch(s.charAt(0)) {
                        default:
                            break;

                        case '-':
                            neg = true;;
                        case '+':
                            s = s.substring(1);
                            if (s.startsWith("0x")) {
                                n = 16;
                                s = s.substring(2);
                            }
                            try {
                                n = Integer.parseInt(s, n);
                            } catch (NumberFormatException e) {
                                throw new jasError("Badly relative offset number");
                            }
                            if(neg) n = -n;
                                return new relative_num_token(sym.Relative, n);
                        }
                        // Perform variable substitution
                        if ((v = dict.get(s)) != null)
                            return (token)v;
                    } // not begin from '$'
                } // !only_name
                // Unrecognized string token (e.g. a classname)
                return new str_token(sym.Word, str);
            } /* default */
        } /* switch and for */
    }

};

/* --- Revision History ---------------------------------------------------
--- Iouri Kharon, Mar 13 2006
    Added support for '\\u' escape sequnce in name/signature
    Added '' enclosed names (overload of reserved words)
--- Iouri Kharon, Feb 17 2006
    Remove infinite loop when last line in source file do not have EOL
--- Iouri Kharon, Dec 19 2005
    Added '\\u' escape sequence
    Change '\octal' escape sequence
    Added very long string support
--- Daniel Reynaud, Oct 19 2005
    Added '\\' escape sequence
--- Jonathan Meyer, Feb 8 1997
    Converted to be non-static
--- Jonathan Meyer, Oct 30 1996
    Added support for more \ escapes in quoted strings (including octals).
--- Jonathan Meyer, Oct 1 1996
    Added .interface and .implements
--- Jonathan Meyer, July 25 1996
    changed IN to IS. Added token_line_num, which is the line number of the
    last token returned by next_token().
--- Jonathan Meyer, July 24 1996 added mods to recognize '\r' as whitespace.
*/
