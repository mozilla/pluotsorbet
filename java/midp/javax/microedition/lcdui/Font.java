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

package javax.microedition.lcdui;

/**
 * The <code>Font</code> class represents fonts and font
 * metrics. <code>Fonts</code> cannot be
 * created by applications. Instead, applications query for fonts
 * based on
 * font attributes and the system will attempt to provide a font that
 * matches
 * the requested attributes as closely as possible.
 *
 * <p> A <code>Font's</code> attributes are style, size, and face. Values for
 * attributes must be specified in terms of symbolic constants. Values for
 * the style attribute may be combined using the bit-wise
 * <code>OR</code> operator,
 * whereas values for the other attributes may not be combined. For example,
 * the value </p>
 *
 * <p> <code>
 * STYLE_BOLD | STYLE_ITALIC
 * </code> </p>
 *
 * <p> may be used to specify a bold-italic font; however </p>
 *
 * <p> <code>
 * SIZE_LARGE | SIZE_SMALL
 * </code> </p>
 *
 * <p> is illegal. </p>
 *
 * <p> The values of these constants are arranged so that zero is valid for
 * each attribute and can be used to specify a reasonable default font
 * for the system. For clarity of programming, the following symbolic
 * constants are provided and are defined to have values of zero: </p>
 *
 * <p> <ul>
 * <li> <code> STYLE_PLAIN </code> </li>
 * <li> <code> SIZE_MEDIUM </code> </li>
 * <li> <code> FACE_SYSTEM </code> </li>
 * </ul> </p>
 *
 * <p> Values for other attributes are arranged to have disjoint bit patterns
 * in order to raise errors if they are inadvertently misused (for example,
 * using <code>FACE_PROPORTIONAL</code> where a style is
 * required). However, the values
 * for the different attributes are not intended to be combined with each
 * other. </p>
 * @since MIDP 1.0
 */

public final class Font {

    /**
     * The plain style constant. This may be combined with the
     * other style constants for mixed styles. 
     *
     * <P>Value <code>0</code> is assigned to <code>STYLE_PLAIN</code>.</P>
     */
    public static final int STYLE_PLAIN = 0;
  
    /**
     * The bold style constant. This may be combined with the
     * other style constants for mixed styles.
     *
     * <P>Value <code>1</code> is assigned to <code>STYLE_BOLD</code>.</P>
     */
    public static final int STYLE_BOLD = 1;
  
    /**
     * The italicized style constant. This may be combined with
     * the other style constants for mixed styles.
     *
     * <P>Value <code>2</code> is assigned to <code>STYLE_ITALIC</code>.</P>
     */
    public static final int STYLE_ITALIC = 2;
  
    /**
     * The underlined style constant. This may be combined with
     * the other style constants for mixed styles.
     *
     * <P>Value <code>4</code> is assigned to <code>STYLE_UNDERLINED</code>.</P>
     */
    public static final int STYLE_UNDERLINED = 4;
  
    /**
     * The &quot;small&quot; system-dependent font size.
     *
     * <P>Value <code>8</code> is assigned to <code>STYLE_SMALL</code>.</P>
     */
    public static final int SIZE_SMALL = 8;
  
    /**
     * The &quot;medium&quot; system-dependent font size.
     *
     * <P>Value <code>0</code> is assigned to <code>STYLE_MEDIUM</code>.</P>
     */
    public static final int SIZE_MEDIUM = 0;
  
    /**
     * The &quot;large&quot; system-dependent font size.
     *
     * <P>Value <code>16</code> is assigned to <code>SIZE_LARGE</code>.</P>
     */
    public static final int SIZE_LARGE = 16;
  
    /**
     * The &quot;system&quot; font face.
     *
     * <P>Value <code>0</code> is assigned to <code>FACE_SYSTEM</code>.</P>
     */
    public static final int FACE_SYSTEM = 0;
  
    /**
     * The &quot;monospace&quot; font face.
     *
     * <P>Value <code>32</code> is assigned to <code>FACE_MONOSPACE</code>.</P>
     */
    public static final int FACE_MONOSPACE = 32;
  
    /**
     * The &quot;proportional&quot; font face.
     *
     * <P>Value <code>64</code> is assigned to
     * <code>FACE_PROPORTIONAL</code>.</P>
     */
    public static final int FACE_PROPORTIONAL = 64;
  
    /**
     * Default font specifier used to draw Item and Screen contents.
     *
     * <code>FONT_STATIC_TEXT</code> has the value <code>0</code>.
     *
     * @see #getFont(int fontSpecifier)
     */
    public static final int FONT_STATIC_TEXT = 0;

    /**
     * Font specifier used by the implementation to draw text input by
     * a user.
     *
     * <code>FONT_INPUT_TEXT</code> has the value <code>1</code>.
     *
     * @see #getFont(int fontSpecifier)
     */
    public static final int FONT_INPUT_TEXT = 1;

    /**
     * Gets the <code>Font</code> used by the high level user interface
     * for the <code>fontSpecifier</code> passed in. It should be used
     * by subclasses of
     * <code>CustomItem</code> and <code>Canvas</code> to match user
     * interface on the device.
     *
     * @param fontSpecifier one of <code>FONT_INPUT_TEXT</code>, or
     * <code>FONT_STATIC_TEXT</code>
     * @return font that corresponds to the passed in font specifier
     * @throws IllegalArgumentException if <code>fontSpecifier</code> is not 
     * a valid fontSpecifier
     */
    public static Font getFont(int fontSpecifier) {

	Font font;

	switch (fontSpecifier) {
	case FONT_STATIC_TEXT: 
	case FONT_INPUT_TEXT:
	    font = getDefaultFont();
	    break;
	default:
	    throw new IllegalArgumentException();
	}
	return font;
    }

    /**
     * Construct a new Font object
     *
     * @param inp_face The face to use to construct the Font
     * @param inp_style The style to use to construct the Font
     * @param inp_size The point size to use to construct the Font
     */
    private Font(int inp_face, int inp_style, int inp_size) {
        face  = inp_face;
        style = inp_style;
        size  = inp_size;

        init(inp_face, inp_style, inp_size);
    }

    /**
     * Gets the default font of the system.
     * @return the default font
     */
    public static Font getDefaultFont() {
        synchronized (Display.LCDUILock) {
            if (DEFAULT_FONT == null)
                DEFAULT_FONT = new Font(FACE_SYSTEM, STYLE_PLAIN, SIZE_MEDIUM);
            return DEFAULT_FONT;
        }
    }

    /**
     * Obtains an object representing a font having the specified face, style,
     * and size. If a matching font does not exist, the system will
     * attempt to provide the closest match. This method <em>always</em> 
     * returns
     * a valid font object, even if it is not a close match to the request. 
     *
     * @param inp_face one of <code>FACE_SYSTEM</code>,
     * <code>FACE_MONOSPACE</code>, or <code>FACE_PROPORTIONAL</code>
     * @param inp_style <code>STYLE_PLAIN</code>, or a combination of
     * <code>STYLE_BOLD</code>,
     * <code>STYLE_ITALIC</code>, and <code>STYLE_UNDERLINED</code>
     * @param inp_size one of <code>SIZE_SMALL</code>, <code>SIZE_MEDIUM</code>,
     * or <code>SIZE_LARGE</code>
     * @return instance the nearest font found
     * @throws IllegalArgumentException if <code>face</code>, 
     * <code>style</code>, or <code>size</code> are not
     * legal values
     */
    public static Font getFont(int inp_face, int inp_style, int inp_size) {
        if ((inp_face != FACE_SYSTEM) 
            && (inp_face != FACE_MONOSPACE)
            && (inp_face != FACE_PROPORTIONAL)) {
            throw new IllegalArgumentException("Unsupported face");
        }

        if ((inp_style & ((STYLE_UNDERLINED << 1) - 1)) != inp_style) {
            throw new IllegalArgumentException("Illegal style");
        }

        if ((inp_size != SIZE_SMALL) 
            && (inp_size != SIZE_MEDIUM)
            && (inp_size != SIZE_LARGE)) {
            throw new IllegalArgumentException("Unsupported size");
        }

        synchronized (Display.LCDUILock) {
            /* IMPL_NOTE: this makes garbage.  But hashtables need Object keys. */
            Integer key = new Integer(inp_face | inp_style | inp_size);
            Font f = (Font)table.get(key);
            if (f == null) {
                f = new Font(inp_face, inp_style, inp_size);
                table.put(key, f);
            }

            return f;
        }
    }

    /**
     * Gets the style of the font. The value is an <code>OR'ed</code>
     * combination of
     * <code>STYLE_BOLD</code>, <code>STYLE_ITALIC</code>, and
     * <code>STYLE_UNDERLINED</code>; or the value is
     * zero (<code>STYLE_PLAIN</code>).
     * @return style of the current font
     *
     * @see #isPlain()
     * @see #isBold()
     * @see #isItalic()
     */
    public int getStyle() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return style;
    };

    /**
     * Gets the size of the font.
     *
     * @return one of <code>SIZE_SMALL</code>, <code>SIZE_MEDIUM</code>,
     * <code>SIZE_LARGE</code>
     */
    public int getSize() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return size;
    }

    /**
     * Gets the face of the font.
     *
     * @return one of <code>FACE_SYSTEM</code>,
     * <code>FACE_PROPORTIONAL</code>, <code>FACE_MONOSPACE</code>
     */
    public int getFace() { 
        // SYNC NOTE: return of atomic value, no locking necessary
        return face;
    }

    /**
     * Returns <code>true</code> if the font is plain.
     * @see #getStyle()
     * @return <code>true</code> if font is plain
     */
    public boolean isPlain() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return style == STYLE_PLAIN;
    }

    /**
     * Returns <code>true</code> if the font is bold.
     * @see #getStyle()
     * @return <code>true</code> if font is bold
     */
    public boolean isBold() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return (style & STYLE_BOLD) == STYLE_BOLD;
    }
 
    /**
     * Returns <code>true</code> if the font is italic.
     * @see #getStyle()
     * @return <code>true</code> if font is italic
     */
    public boolean isItalic() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return (style & STYLE_ITALIC) == STYLE_ITALIC;
    }

    /**
     * Returns <code>true</code> if the font is underlined.
     * @see #getStyle()
     * @return <code>true</code> if font is underlined
     */
    public boolean isUnderlined() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return (style & STYLE_UNDERLINED) == STYLE_UNDERLINED;
    }
        
    /**
     * Gets the standard height of a line of text in this font. This value
     * includes sufficient spacing to ensure that lines of text painted this
     * distance from anchor point to anchor point are spaced as intended by the
     * font designer and the device. This extra space (leading) occurs below 
     * the text.
     * @return standard height of a line of text in this font (a 
     * non-negative value)
     */
    public int getHeight() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return height;
    }

    /**
     * Gets the distance in pixels from the top of the text to the text's
     * baseline.
     * @return the distance in pixels from the top of the text to the text's
     * baseline
     */
    public int getBaselinePosition() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return baseline;
    }

    /**
     * Gets the advance width of the specified character in this Font.
     * The advance width is the horizontal distance that would be occupied if
     * <code>ch</code> were to be drawn using this <code>Font</code>, 
     * including inter-character spacing following
     * <code>ch</code> necessary for proper positioning of subsequent text.
     * 
     * @param ch the character to be measured
     * @return the total advance width (a non-negative value)
     */
    public native int charWidth(char ch);

    /**
     * Returns the advance width of the characters in <code>ch</code>, 
     * starting at the specified offset and for the specified number of
     * characters (length).
     * The advance width is the horizontal distance that would be occupied if
     * the characters were to be drawn using this <code>Font</code>,
     * including inter-character spacing following
     * the characters necessary for proper positioning of subsequent text.
     *
     * <p>The <code>offset</code> and <code>length</code> parameters must
     * specify a valid range of characters
     * within the character array <code>ch</code>. The <code>offset</code>
     * parameter must be within the
     * range <code>[0..(ch.length)]</code>, inclusive.
     * The <code>length</code> parameter must be a non-negative
     * integer such that <code>(offset + length) &lt;= ch.length</code>.</p>
     *
     * @param ch the array of characters
     * @param offset the index of the first character to measure
     * @param length the number of characters to measure
     * @return the width of the character range
     * @throws ArrayIndexOutOfBoundsException if <code>offset</code> and
     * <code>length</code> specify an
     * invalid range
     * @throws NullPointerException if <code>ch</code> is <code>null</code>
     */
    public native int charsWidth(char[] ch, int offset, int length);

    /**
     * Gets the total advance width for showing the specified
     * <code>String</code>
     * in this <code>Font</code>.
     * The advance width is the horizontal distance that would be occupied if
     * <code>str</code> were to be drawn using this <code>Font</code>, 
     * including inter-character spacing following
     * <code>str</code> necessary for proper positioning of subsequent text.
     * 
     * @param str the <code>String</code> to be measured
     * @return the total advance width
     * @throws NullPointerException if <code>str</code> is <code>null</code>
     */
    public native int stringWidth(java.lang.String str);

    /**
     * Gets the total advance width for showing the specified substring in this
     * <code>Font</code>.
     * The advance width is the horizontal distance that would be occupied if
     * the substring were to be drawn using this <code>Font</code>,
     * including inter-character spacing following
     * the substring necessary for proper positioning of subsequent text.
     *
     * <p>
     * The <code>offset</code> and <code>len</code> parameters must
     * specify a valid range of characters
     * within <code>str</code>. The <code>offset</code> parameter must
     * be within the
     * range <code>[0..(str.length())]</code>, inclusive.
     * The <code>len</code> parameter must be a non-negative
     * integer such that <code>(offset + len) &lt;= str.length()</code>.
     * </p>
     *
     * @param str the <code>String</code> to be measured
     * @param offset zero-based index of first character in the substring
     * @param len length of the substring
     * @return the total advance width
     * @throws StringIndexOutOfBoundsException if <code>offset</code> and
     * <code>length</code> specify an
     * invalid range
     * @throws NullPointerException if <code>str</code> is <code>null</code>
     */
    public native int substringWidth(String str, int offset, int len);


    // private implementation //

    /** The face of this Font */
    private int face;
    /** The style of this Font */
    private int style;
    /** The point size of this Font */
    private int size;
    /** The baseline of this Font */
    private int baseline;
    /** The height of this Font */
    private int height;

    /**
     * The "default" font, constructed from the 'system' face,
     * plain style, and 'medium' size point.
     */
    private static Font DEFAULT_FONT;

    /**
     * A hashtable used to maintain a store of created Fonts
     * so they are not re-created in the future
     */
    private static java.util.Hashtable table = new java.util.Hashtable(4);

    /**
     * Natively initialize this Font object's peer
     *
     * @param inp_face The face to initialize the native Font
     * @param inp_style The style to initialize the native Font
     * @param inp_size The point size to initialize the native Font
     */
    private native void init(int inp_face, int inp_style, int inp_size);
}

