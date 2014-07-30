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

import com.sun.midp.lcdui.DynamicCharacterArray;

/**
 * A <code>TextField</code> is an editable text component that may be
 * placed into
 * a {@link Form Form}. It can be
 * given a piece of text that is used as the initial value.
 *
 * <P>A <code>TextField</code> has a maximum size, which is the
 * maximum number of characters
 * that can be stored in the object at any time (its capacity). This limit is
 * enforced when the <code>TextField</code> instance is constructed,
 * when the user is editing text within the <code>TextField</code>, as well as
 * when the application program calls methods on the
 * <code>TextField</code> that modify its
 * contents. The maximum size is the maximum stored capacity and is unrelated
 * to the number of characters that may be displayed at any given time.
 * The number of characters displayed and their arrangement into rows and
 * columns are determined by the device. </p>
 *
 * <p>The implementation may place a boundary on the maximum size, and the
 * maximum size actually assigned may be smaller than the application had
 * requested.  The value actually assigned will be reflected in the value
 * returned by {@link #getMaxSize() getMaxSize()}.  A defensively-written
 * application should compare this value to the maximum size requested and be
 * prepared to handle cases where they differ.</p>
 *
 * <a name="constraints"></a>
 * <h3>Input Constraints</h3>
 *
 * <P>The <code>TextField</code> shares the concept of <em>input
 * constraints</em> with the {@link TextBox TextBox} class. The different
 * constraints allow the application to request that the user's input be
 * restricted in a variety of ways. The implementation is required to
 * restrict the user's input as requested by the application. For example, if
 * the application requests the <code>NUMERIC</code> constraint on a
 * <code>TextField</code>, the
 * implementation must allow only numeric characters to be entered. </p>
 *
 * <p>The <em>actual contents</em> of the text object are set and modified by
 * and are
 * reported to the application through the <code>TextBox</code> and
 * <code>TextField</code> APIs.  The <em>displayed contents</em> may differ
 * from the actual contents if the implementation has chosen to provide
 * special formatting suitable for the text object's constraint setting.
 * For example, a <code>PHONENUMBER</code> field might be displayed with
 * digit separators and punctuation as
 * appropriate for the phone number conventions in use, grouping the digits
 * into country code, area code, prefix, etc. Any spaces or punctuation
 * provided are not considered part of the text object's actual contents. For
 * example, a text object with the <code>PHONENUMBER</code>
 * constraint might display as
 * follows:</p>
 *
 * <TABLE BORDER="2">
 * <TR>
 * <TD ROWSPAN="1" COLSPAN="1">
 *     <pre><code>
 *     (408) 555-1212    </code></pre>
 * </TD>
 * </TR>
 * </TABLE>
 *
 * <p>but the actual contents of the object visible to the application
 * through the APIs would be the string
 * &quot;<code>4085551212</code>&quot;.
 * The <code>size</code> method reflects the number of characters in the
 * actual contents, not the number of characters that are displayed, so for
 * this example the <code>size</code> method would return <code>10</code>.</p>
 *
 * <p>Some constraints, such as <code>DECIMAL</code>, require the
 * implementation to perform syntactic validation of the contents of the text
 * object.  The syntax checking is performed on the actual contents of the
 * text object, which may differ from the displayed contents as described
 * above.  Syntax checking is performed on the initial contents passed to the
 * constructors, and it is also enforced for all method calls that affect the
 * contents of the text object.  The methods and constructors throw
 * <code>IllegalArgumentException</code> if they would result in the contents
 * of the text object not conforming to the required syntax.</p>
 *
 * <p>The value passed to the {@link #setConstraints setConstraints()} method
 * consists of a restrictive constraint setting described above, as well as a
 * variety of flag bits that modify the behavior of text entry and display.
 * The value of the restrictive constraint setting is in the low order
 * <code>16</code> bits
 * of the value, and it may be extracted by combining the constraint value
 * with the <code>CONSTRAINT_MASK</code> constant using the bit-wise
 * <code>AND</code> (<code>&amp;</code>) operator.
 * The restrictive constraint settings are as follows:
 *
 * <blockquote><code>
 * ANY<br>
 * EMAILADDR<br>
 * NUMERIC<br>
 * PHONENUMBER<br>
 * URL<br>
 * DECIMAL<br>
 * </code></blockquote>
 *
 * <p>The modifier flags reside in the high order <code>16</code> bits
 * of the constraint
 * value, that is, those in the complement of the
 * <code>CONSTRAINT_MASK</code> constant.
 * The modifier flags may be tested individually by combining the constraint
 * value with a modifier flag using the bit-wise <code>AND</code>
 * (<code>&amp;</code>) operator.  The
 * modifier flags are as follows:
 *
 * <blockquote><code>
 * PASSWORD<br>
 * UNEDITABLE<br>
 * SENSITIVE<br>
 * NON_PREDICTIVE<br>
 * INITIAL_CAPS_WORD<br>
 * INITIAL_CAPS_SENTENCE<br>
 * </code></blockquote>
 *
 * <a name="modes"></a>
 * <h3>Input Modes</h3>
 *
 * <p>The <code>TextField</code> shares the concept of <em>input
 * modes</em> with the {@link
 * TextBox TextBox} class.  The application can request that the
 * implementation use a particular input mode when the user initiates editing
 * of a <code>TextField</code> or <code>TextBox</code>.  The input
 * mode is a concept that exists within
 * the user interface for text entry on a particular device.  The application
 * does not request an input mode directly, since the user interface for text
 * entry is not standardized across devices.  Instead, the application can
 * request that the entry of certain characters be made convenient.  It can do
 * this by passing the name of a Unicode character subset to the {@link
 * #setInitialInputMode setInitialInputMode()} method.  Calling this method
 * requests that the implementation set the mode of the text entry user
 * interface so that it is convenient for the user to enter characters in this
 * subset.  The application can also request that the input mode have certain
 * behavioral characteristics by setting modifier flags in the constraints
 * value.
 *
 * <p>The requested input mode should be used whenever the user initiates the
 * editing of a <code>TextBox</code> or <code>TextField</code> object.
 * If the user had changed input
 * modes in a previous editing session, the application's requested input mode
 * should take precedence over the previous input mode set by the user.
 * However, the input mode is not restrictive, and the user is allowed to
 * change the input mode at any time during editing.  If editing is already in
 * progress, calls to the <code>setInitialInputMode</code> method do not
 * affect the current input mode, but instead take effect at the next time the
 * user initiates editing of this text object.
 *
 * <p>The initial input mode is a hint to the implementation.  If the
 * implementation cannot provide an input mode that satisfies the
 * application's request, it should use a default input mode.
 *
 * <P>The input mode that results from the application's request is not a
 * restriction on the set of characters the user is allowed to enter.  The
 * user MUST be allowed to switch input modes to enter any character that is
 * allowed within the current constraint setting.  The constraint
 * setting takes precedence over an input mode request, and the implementation
 * may refuse to supply a particular input mode if it is inconsistent with the
 * current constraint setting.
 *
 * <P>For example, if the current constraint is <code>ANY</code>, the call</P>
 *
 * <TABLE BORDER="2">
 * <TR>
 * <TD ROWSPAN="1" COLSPAN="1">
 *    <pre><code>
 *    setInitialInputMode("MIDP_UPPERCASE_LATIN");    </code></pre>
 * </TD>
 * </TR>
 * </TABLE>
 *
 * <p>should set the initial input mode to allow entry of uppercase Latin
 * characters.  This does not restrict input to these characters, and the user
 * will be able to enter other characters by switching the input mode to allow
 * entry of numerals or lowercase Latin letters.  However, if the current
 * constraint is <code>NUMERIC</code>, the implementation may ignore
 * the request to set an
 * initial input mode allowing <code>MIDP_UPPERCASE_LATIN</code>
 * characters because these
 * characters are not allowed in a <code>TextField</code> whose
 * constraint is <code>NUMERIC</code>.  In
 * this case, the implementation may instead use an input mode that allows
 * entry of numerals, since such an input mode is most appropriate for entry
 * of data under the <code>NUMERIC</code> constraint.
 *
 * <P>A string is used to name the Unicode character subset passed as a
 * parameter to the
 * {@link #setInitialInputMode setInitialInputMode()} method.
 * String comparison is case sensitive.
 *
 * <P>Unicode character blocks can be named by adding the prefix
 * &quot;<code>UCB</code>_&quot; to the
 * the string names of fields representing Unicode character blocks as defined
 * in the J2SE class <code>java.lang.Character.UnicodeBlock</code>.  Any
 * Unicode character block may be named in this fashion.  For convenience, the
 * most common Unicode character blocks are listed below.
 *
 * <blockquote><code>
 * UCB_BASIC_LATIN<br>
 * UCB_GREEK<br>
 * UCB_CYRILLIC<br>
 * UCB_ARMENIAN<br>
 * UCB_HEBREW<br>
 * UCB_ARABIC<br>
 * UCB_DEVANAGARI<br>
 * UCB_BENGALI<br>
 * UCB_THAI<br>
 * UCB_HIRAGANA<br>
 * UCB_KATAKANA<br>
 * UCB_HANGUL_SYLLABLES<br>
 * </code></blockquote>
 *
 * <P>&quot;Input subsets&quot; as defined by the J2SE class
 * <code>java.awt.im.InputSubset</code> may be named by adding the prefix
 * &quot;<code>IS_</code>&quot; to the string names of fields
 * representing input subsets as defined
 * in that class.  Any defined input subset may be used.  For convenience, the
 * names of the currently defined input subsets are listed below.
 *
 * <blockquote><code>
 * IS_FULLWIDTH_DIGITS<br>
 * IS_FULLWIDTH_LATIN<br>
 * IS_HALFWIDTH_KATAKANA<br>
 * IS_HANJA<br>
 * IS_KANJI<br>
 * IS_LATIN<br>
 * IS_LATIN_DIGITS<br>
 * IS_SIMPLIFIED_HANZI<br>
 * IS_TRADITIONAL_HANZI<br>
 * </code></blockquote>
 *
 * <P>MIDP has also defined the following character subsets:
 *
 * <blockquote>
 * <code>MIDP_UPPERCASE_LATIN</code> - the subset of
 * <code>IS_LATIN</code> that corresponds to
 * uppercase Latin letters
 * </blockquote>
 * <blockquote>
 * <code>MIDP_LOWERCASE_LATIN</code> - the subset of
 * <code>IS_LATIN</code> that corresponds to
 * lowercase Latin letters
 * </blockquote>
 *
 * <p>
 * Finally, implementation-specific character subsets may be named with
 * strings that have a prefix of &quot;<code>X_</code>&quot;.  In
 * order to avoid namespace conflicts,
 * it is recommended that implementation-specific names include the name of
 * the defining company or organization after the initial
 * &quot;<code>X_</code>&quot; prefix.
 *
 * <p> For example, a Japanese language application might have a particular
 * <code>TextField</code> that the application intends to be used
 * primarily for input of
 * words that are &quot;loaned&quot; from languages other than Japanese.  The
 * application might request an input mode facilitating Hiragana input by
 * issuing the following method call:</p>
 *
 * <TABLE BORDER="2">
 * <TR>
 * <TD ROWSPAN="1" COLSPAN="1">
 *    <pre><code>
 *    textfield.setInitialInputMode("UCB_HIRAGANA");       </code></pre>
 * </TD>
 * </TR>
 * </TABLE>
 * <h3>Implementation Note</h3>
 *
 * <p>Implementations need not compile in all the strings listed above.
 * Instead, they need only to compile in the strings that name Unicode
 * character subsets that they support.  If the subset name passed by the
 * application does not match a known subset name, the request should simply
 * be ignored without error, and a default input mode should be used.  This
 * lets implementations support this feature reasonably inexpensively.
 * However, it has the consequence that the application cannot tell whether
 * its request has been accepted, nor whether the Unicode character subset it
 * has requested is actually a valid subset.
 *
 * @since MIDP 1.0
 */

public class TextField extends Item {

    /**
     * The user is allowed to enter any text.
     * <A HREF="Form.html#linebreak">Line breaks</A> may be entered.
     *
     * <P>Constant <code>0</code> is assigned to <code>ANY</code>.</P>
     */
    public final static int ANY = 0;

    /**
     * The user is allowed to enter an e-mail address.
     *
     * <P>Constant <code>1</code> is assigned to <code>EMAILADDR</code>.</P>
     */
    public final static int EMAILADDR = 1;

    /**
     * The user is allowed to enter only an integer value. The implementation
     * must restrict the contents either to be empty or to consist of an
     * optional minus sign followed by a string of one or more decimal
     * numerals.  Unless the value is empty, it will be successfully parsable
     * using {@link java.lang.Integer#parseInt(String)}.
     *
     * <P>The minus sign consumes space in the text object.  It is thus
     * impossible to enter negative numbers into a text object whose maximum
     * size is <code>1</code>.</P>
     *
     * <P>Constant <code>2</code> is assigned to <code>NUMERIC</code>.</P>
     */
    public final static int NUMERIC = 2;

    /**
     * The user is allowed to enter a phone number. The phone number is a
     * special
     * case, since a phone-based implementation may be linked to the
     * native phone
     * dialing application. The implementation may automatically start a phone
     * dialer application that is initialized so that pressing a single key
     * would be enough to make a call. The call must not made automatically
     * without requiring user's confirmation.  Implementations may also
     * provide a feature to look up the phone number in the device's phone or
     * address database.
     *
     * <P>The exact set of characters allowed is specific to the device and to
     * the device's network and may include non-numeric characters, such as a
     * &quot;+&quot; prefix character.</P>
     *
     * <P>Some platforms may provide the capability to initiate voice calls
     * using the {@link javax.microedition.midlet.MIDlet#platformRequest
     * MIDlet.platformRequest} method.</P>
     *
     * <P>Constant <code>3</code> is assigned to <code>PHONENUMBER</code>.</P>
     */
    public final static int PHONENUMBER = 3;

    /**
     * The user is allowed to enter a URL.
     *
     * <P>Constant <code>4</code> is assigned to <code>URL</code>.</P>
     */
    public final static int URL = 4;


    /**
     * The user is allowed to enter numeric values with optional decimal
     * fractions, for example &quot;-123&quot;, &quot;0.123&quot;, or
     * &quot;.5&quot;.
     *
     * <p>The implementation may display a period &quot;.&quot; or a
     * comma &quot;,&quot; for the decimal fraction separator, depending on
     * the conventions in use on the device.  Similarly, the implementation
     * may display other device-specific characters as part of a decimal
     * string, such as spaces or commas for digit separators.  However, the
     * only characters allowed in the actual contents of the text object are
     * period &quot;.&quot;, minus sign &quot;-&quot;, and the decimal
     * digits.</p>
     *
     * <p>The actual contents of a <code>DECIMAL</code> text object may be
     * empty.  If the actual contents are not empty, they must conform to a
     * subset of the syntax for a <code>FloatingPointLiteral</code> as defined
     * by the <em>Java Language Specification</em>, section 3.10.2.  This
     * subset syntax is defined as follows: the actual contents
     * must consist of an optional minus sign
     * &quot;-&quot;, followed by one or more whole-number decimal digits,
     * followed by an optional fraction separator, followed by zero or more
     * decimal fraction digits.  The whole-number decimal digits may be
     * omitted if the fraction separator and one or more decimal fraction
     * digits are present.</p>
     *
     * <p>The syntax defined above is also enforced whenever the application
     * attempts to set or modify the contents of the text object by calling
     * a constructor or a method.</p>
     *
     * <p>Parsing this string value into a numeric value suitable for
     * computation is the responsibility of the application.  If the contents
     * are not empty, the result can be parsed successfully by
     * <code>Double.valueOf</code> and related methods if they are present
     * in the runtime environment. </p>
     *
     * <p>The sign and the fraction separator consume space in the text
     * object.  Applications should account for this when assigning a maximum
     * size for the text object.</p>
     *
     * <P>Constant <code>5</code> is assigned to <code>DECIMAL</code>.</P>
     *
     */
    public static final int DECIMAL = 5;

    /**
     * Indicates that the text entered is confidential data that should be
     * obscured whenever possible.  The contents may be visible while the
     * user is entering data.  However, the contents must never be divulged
     * to the user.  In particular, the existing contents must not be shown
     * when the user edits the contents.  The means by which the contents
     * are obscured is implementation-dependent.  For example, each
     * character of the data might be masked with a
     * &quot;<code>*</code>&quot; character.  The
     * <code>PASSWORD</code> modifier is useful for entering
     * confidential information
     * such as passwords or personal identification numbers (PINs).
     *
     * <p>Data entered into a <code>PASSWORD</code> field is treated
     * similarly to <code>SENSITIVE</code>
     * in that the implementation must never store the contents into a
     * dictionary or table for use in predictive, auto-completing, or other
     * accelerated input schemes.  If the <code>PASSWORD</code> bit is
     * set in a constraint
     * value, the <code>SENSITIVE</code> and
     * <code>NON_PREDICTIVE</code> bits are also considered to be
     * set, regardless of their actual values.  In addition, the
     * <code>INITIAL_CAPS_WORD</code> and
     * <code>INITIAL_CAPS_SENTENCE</code> flag bits should be ignored
     * even if they are set.</p>
     *
     * <p>The <code>PASSWORD</code> modifier can be combined with
     * other input constraints
     * by using the bit-wise <code>OR</code> operator (<code>|</code>).
     * The <code>PASSWORD</code> modifier is not
     * useful with some constraint values such as
     * <code>EMAILADDR</code>, <code>PHONENUMBER</code>,
     * and <code>URL</code>. These combinations are legal, however,
     * and no exception is
     * thrown if such a constraint is specified.</p>
     *
     * <p>Constant <code>0x10000</code> is assigned to
     * <code>PASSWORD</code>.</p>
     */
    public static final int PASSWORD = 0x10000;

    /**
     * Indicates that editing is currently disallowed.  When this flag is set,
     * the implementation must prevent the user from changing the text
     * contents of this object.  The implementation should also provide a
     * visual indication that the object's text cannot be edited.  The intent
     * of this flag is that this text object has the potential to be edited,
     * and that there are circumstances where the application will clear this
     * flag and allow the user to edit the contents.
     *
     * <p>The <code>UNEDITABLE</code> modifier can be combined with
     * other input constraints
     * by using the bit-wise <code>OR</code> operator (<code>|</code>).
     *
     * <p>Constant <code>0x20000</code> is assigned to <code>UNEDITABLE</code>.
     *
     */
    public static final int UNEDITABLE = 0x20000;

    /**
     * Indicates that the text entered is sensitive data that the
     * implementation must never store into a dictionary or table for use in
     * predictive, auto-completing, or other accelerated input schemes.  A
     * credit card number is an example of sensitive data.
     *
     * <p>The <code>SENSITIVE</code> modifier can be combined with other input
     * constraints by using the bit-wise <code>OR</code> operator
     * (<code>|</code>).</p>
     *
     * <p>Constant <code>0x40000</code> is assigned to
     * <code>SENSITIVE</code>.</p>
     *
     */
    public static final int SENSITIVE = 0x40000;

    /**
     * Indicates that the text entered does not consist of words that are
     * likely to be found in dictionaries typically used by predictive input
     * schemes.  If this bit is clear, the implementation is allowed to (but
     * is not required to) use predictive input facilities.  If this bit is
     * set, the implementation should not use any predictive input facilities,
     * but it instead should allow character-by-character text entry.
     *
     * <p>The <code>NON_PREDICTIVE</code> modifier can be combined
     * with other input
     * constraints by using the bit-wise <code>OR</code> operator
     * (<code>|</code>).
     *
     * <P>Constant <code>0x80000</code> is assigned to
     * <code>NON_PREDICTIVE</code>.</P>
     *
     */
    public static final int NON_PREDICTIVE = 0x80000;

    /**
     * This flag is a hint to the implementation that during text editing, the
     * initial letter of each word should be capitalized.  This hint should be
     * honored only on devices for which automatic capitalization is
     * appropriate and when the character set of the text being edited has the
     * notion of upper case and lower case letters.  The definition of
     * word boundaries is implementation-specific.
     *
     * <p>If the application specifies both the
     * <code>INITIAL_CAPS_WORD</code> and the
     * <code>INITIAL_CAPS_SENTENCE</code> flags,
     * <code>INITIAL_CAPS_WORD</code> behavior should be used.
     *
     * <p>The <code>INITIAL_CAPS_WORD</code> modifier can be combined
     * with other input
     * constraints by using the bit-wise <code>OR</code> operator
     * (<code>|</code>).
     *
     * <p>Constant <code>0x100000</code> is assigned to
     * <code>INITIAL_CAPS_WORD</code>.
     *
     */
    public static final int INITIAL_CAPS_WORD = 0x100000;

    /**
     * This flag is a hint to the implementation that during text editing, the
     * initial letter of each sentence should be capitalized.  This hint
     * should be honored only on devices for which automatic capitalization is
     * appropriate and when the character set of the text being edited has the
     * notion of upper case and lower case letters.  The definition of
     * sentence boundaries is implementation-specific.
     *
     * <p>If the application specifies both the
     * <code>INITIAL_CAPS_WORD</code> and the
     * <code>INITIAL_CAPS_SENTENCE</code> flags,
     * <code>INITIAL_CAPS_WORD</code> behavior should be used.
     *
     * <p>The <code>INITIAL_CAPS_SENTENCE</code> modifier can be
     * combined with other input
     * constraints by using the bit-wise <code>OR</code> operator
     * (<code>|</code>).
     *
     * <p>Constant <code>0x200000</code> is assigned to
     * <code>INITIAL_CAPS_SENTENCE</code>.
     *
     */
    public static final int INITIAL_CAPS_SENTENCE = 0x200000;

    /**
     * The mask value for determining the constraint mode. The application
     * should
     * use the bit-wise <code>AND</code> operation with a value returned by
     * <code>getConstraints()</code> and
     * <code>CONSTRAINT_MASK</code> in order to retrieve the current
     * constraint mode,
     * in order to remove any modifier flags such as the
     * <code>PASSWORD</code> flag.
     *
     * <P>Constant <code>0xFFFF</code> is assigned to
     * <code>CONSTRAINT_MASK</code>.</P>
     */
    public final static int CONSTRAINT_MASK = 0xFFFF;

    /**
     * Creates a new <code>TextField</code> object with the given label, initial
     * contents, maximum size in characters, and constraints.
     * If the text parameter is <code>null</code>, the
     * <code>TextField</code> is created empty.
     * The <code>maxSize</code> parameter must be greater than zero.
     * An <code>IllegalArgumentException</code> is thrown if the
     * length of the initial contents string exceeds <code>maxSize</code>.
     * However,
     * the implementation may assign a maximum size smaller than the 
     * application had requested.  If this occurs, and if the length of the 
     * contents exceeds the newly assigned maximum size, the contents are 
     * truncated from the end in order to fit, and no exception is thrown.
     *
     * @param label item label
     * @param text the initial contents, or <code>null</code> if the
     * <code>TextField</code> is to be empty
     * @param maxSize the maximum capacity in characters
     * @param constraints see <a href="#constraints">input constraints</a>
     *
     * @throws IllegalArgumentException if <code>maxSize</code> is zero or less
     * @throws IllegalArgumentException if the value of the constraints
     * parameter
     * is invalid
     * @throws IllegalArgumentException if <code>text</code> is illegal
     * for the specified constraints
     * @throws IllegalArgumentException if the length of the string exceeds
     * the requested maximum capacity 
     */
    public TextField(String label, String text, int maxSize, 
                     int constraints) {

        super(label);

        synchronized (Display.LCDUILock) {

            // IllegalArgumentException thrown here
            buffer = new DynamicCharacterArray(maxSize); 

	    // Constraint value is checked here. Since textFieldLF is not
	    // yet created, no LF notification will happen.
            setConstraintsImpl(constraints);

	    // Create a LF with empty content
            itemLF = textFieldLF = LFFactory.getFactory().getTextFieldLF(this);

            // this will use inputClient
	    // Right now setCharsImpl notifies LF a content change.
	    // If LF is created as an absolutely last thing then
            // setCharsImple here does not need the notification.
            if (text == null) {
		setCharsImpl(null, 0, 0);
            } else {
                setCharsImpl(text.toCharArray(), 0, text.length());
            }
        }
    }

    /**
     * Gets the contents of the <code>TextField</code> as a string value.
     *
     * @return the current contents
     * @see #setString
     */
    public String getString() {
        synchronized (Display.LCDUILock) {
	    textFieldLF.lUpdateContents();
            return buffer.toString();
        }
    }

    /**
     * Sets the contents of the <code>TextField</code> as a string
     * value, replacing the
     * previous contents.
     *
     * @param text the new value of the <code>TextField</code>, or
     * <code>null</code> if the TextField is to be made empty
     * @throws IllegalArgumentException if <code>text</code>
     * is illegal for the current
     * <a href="TextField.html#constraints">input constraints</a>
     * @throws IllegalArgumentException if the text would exceed the current
     * maximum capacity
     * @see #getString
     */
    public void setString(String text) {

        synchronized (Display.LCDUILock) {
            if (text == null || text.length() == 0) {
                setCharsImpl(null, 0, 0);
            } else {
                setCharsImpl(text.toCharArray(), 0, text.length());
            }
        }
    }

    /**
     * Copies the contents of the <code>TextField</code> into a
     * character array starting at
     * index zero. Array elements beyond the characters copied are left
     * unchanged.
     *
     * @param data the character array to receive the value
     * @return the number of characters copied
     * @throws ArrayIndexOutOfBoundsException if the array is too short for the
     * contents
     * @throws NullPointerException if <code>data</code> is <code>null</code>
     * @see #setChars
     */
    public int getChars(char[] data) {

        synchronized (Display.LCDUILock) {
	    
	    textFieldLF.lUpdateContents();

            try {
                buffer.getChars(0, buffer.length(), data, 0);
            } catch (IndexOutOfBoundsException e) {
                throw new ArrayIndexOutOfBoundsException(e.getMessage());
            }

            return buffer.length();
        }
    }

    /**
     * Sets the contents of the <code>TextField</code> from a
     * character array, replacing the
     * previous contents. Characters are copied from the region of the
     * <code>data</code> array
     * starting at array index <code>offset</code> and running for
     * <code>length</code> characters.
     * If the data array is <code>null</code>, the <code>TextField</code>
     * is set to be empty and the other parameters are ignored.
     *
     * <p>The <code>offset</code> and <code>length</code> parameters must
     * specify a valid range of characters within
     * the character array <code>data</code>.
     * The <code>offset</code> parameter must be within the
     * range <code>[0..(data.length)]</code>, inclusive.
     * The <code>length</code> parameter
     * must be a non-negative integer such that
     * <code>(offset + length) &lt;= data.length</code>.</p>
     * 
     * @param data the source of the character data
     * @param offset the beginning of the region of characters to copy
     * @param length the number of characters to copy
     * @throws ArrayIndexOutOfBoundsException if <code>offset</code>
     * and <code>length</code> do not specify
     * a valid range within the data array
     * @throws IllegalArgumentException if <code>data</code>
     * is illegal for the current
     * <a href="TextField.html#constraints">input constraints</a>
     * @throws IllegalArgumentException if the text would exceed the current
     * maximum capacity
     * @see #getChars
     */
    public void setChars(char[] data, int offset, int length) {

        synchronized (Display.LCDUILock) {
            setCharsImpl(data, offset, length);
        }
    }

    /**
     * Inserts a string into the contents of the
     * <code>TextField</code>.  The string is
     * inserted just prior to the character indicated by the
     * <code>position</code> parameter, where zero specifies the first
     * character of the contents of the <code>TextField</code>.  If
     * <code>position</code> is
     * less than or equal to zero, the insertion occurs at the beginning of
     * the contents, thus effecting a prepend operation.  If
     * <code>position</code> is greater than or equal to the current size of
     * the contents, the insertion occurs immediately after the end of the
     * contents, thus effecting an append operation.  For example,
     * <code>text.insert(s, text.size())</code> always appends the string
     * <code>s</code> to the current contents.
     *
     * <p>The current size of the contents is increased by the number of
     * inserted characters. The resulting string must fit within the current
     * maximum capacity. </p>
     *
     * <p>If the application needs to simulate typing of characters it can
     * determining the location of the current insertion point
     * (&quot;caret&quot;)
     * using the with {@link #getCaretPosition() getCaretPosition()} method.
     * For example,
     * <code>text.insert(s, text.getCaretPosition())</code> inserts the string
     * <code>s</code> at the current caret position.</p>
     *
     * @param src the <code>String</code> to be inserted
     * @param position the position at which insertion is to occur
     *
     * @throws IllegalArgumentException if the resulting contents
     * would be illegal for the current
     * <a href="TextField.html#constraints">input constraints</a>
     * @throws IllegalArgumentException if the insertion would exceed
     * the current
     * maximum capacity
     * @throws NullPointerException if <code>src</code> is <code>null</code>
     */
    public void insert(String src, int position) {
        synchronized (Display.LCDUILock) {
            // NullPointerException will be thrown by src.toCharArray
            insertImpl(src.toCharArray(), 0, src.length(), position);
        }
    }

    /**
     * Inserts a subrange of an array of characters into the contents of
     * the <code>TextField</code>.  The <code>offset</code> and
     * <code>length</code> parameters indicate the subrange
     * of the data array to be used for insertion. Behavior is otherwise
     * identical to {@link #insert(String, int) insert(String, int)}.
     *
     * <p>The <code>offset</code> and <code>length</code> parameters must
     * specify a valid range of characters within
     * the character array <code>data</code>.
     * The <code>offset</code> parameter must be within the
     * range <code>[0..(data.length)]</code>, inclusive.
     * The <code>length</code> parameter
     * must be a non-negative integer such that
     * <code>(offset + length) &lt;= data.length</code>.</p>
     * 
     * @param data the source of the character data
     * @param offset the beginning of the region of characters to copy
     * @param length the number of characters to copy
     * @param position the position at which insertion is to occur
     *
     * @throws ArrayIndexOutOfBoundsException if <code>offset</code>
     * and <code>length</code> do not specify
     * a valid range within the <code>data</code> array
     * @throws IllegalArgumentException if the resulting contents
     * would be illegal for the current
     * <a href="TextField.html#constraints">input constraints</a>
     * @throws IllegalArgumentException if the insertion would exceed
     * the current
     * maximum capacity
     * @throws NullPointerException if <code>data</code> is <code>null</code>
     */
    public void insert(char[] data, int offset, int length, int position)  {
        synchronized (Display.LCDUILock) {
            insertImpl(data, offset, length, position);
        }
    }

    /**
     * Deletes characters from the <code>TextField</code>.
     *
     * <p>The <code>offset</code> and <code>length</code> parameters must
     * specify a valid range of characters within
     * the contents of the <code>TextField</code>.
     * The <code>offset</code> parameter must be within the
     * range <code>[0..(size())]</code>, inclusive.
     * The <code>length</code> parameter
     * must be a non-negative integer such that
     * <code>(offset + length) &lt;= size()</code>.</p>
     * 
     * @param offset the beginning of the region to be deleted
     * @param length the number of characters to be deleted
     *
     * @throws IllegalArgumentException if the resulting contents
     * would be illegal for the current
     * <a href="TextField.html#constraints">input constraints</a>
     * @throws StringIndexOutOfBoundsException if <code>offset</code>
     * and <code>length</code> do not
     * specify a valid range within the contents of the <code>TextField</code>
     */
    public void delete(int offset, int length) {
        synchronized (Display.LCDUILock) {
	    deleteImpl(offset, length);
        }
    }

    /**
     * Returns the maximum size (number of characters) that can be
     * stored in this <code>TextField</code>.
     * @return the maximum size in characters
     * @see #setMaxSize
     */
    public int getMaxSize() {
        synchronized (Display.LCDUILock) {
            return buffer.capacity();
        }
    }

    /**
     * Sets the maximum size (number of characters) that can be contained
     * in this
     * <code>TextField</code>. If the current contents of the
     * <code>TextField</code> are larger than
     * <code>maxSize</code>, the contents are truncated to fit.
     *
     * @param maxSize the new maximum size
     *
     * @return assigned maximum capacity - may be smaller than requested.
     * @throws IllegalArgumentException if <code>maxSize</code> is zero or less.
     * @throws IllegalArgumentException if the contents
     * after truncation would be illegal for the current
     * <a href="TextField.html#constraints">input constraints</a>
     * @see #getMaxSize
     */
    public int setMaxSize(int maxSize) {
        synchronized (Display.LCDUILock) {

	    textFieldLF.lUpdateContents();

            int oldCapacity = buffer.capacity();

            if (oldCapacity == maxSize) {
		return maxSize;
	    }

            buffer.setCapacity(maxSize);
                
            if (!textFieldLF.lValidate(buffer, constraints)) {
                buffer.setCapacity(oldCapacity);
                throw new IllegalArgumentException();
            }
             
	    // Notify LF that contents has changed due to maxSize   
            textFieldLF.lSetMaxSize(maxSize);
                
            return buffer.capacity();
        }
    }

    /**
     * Gets the number of characters that are currently stored in this
     * <code>TextField</code>.
     * @return number of characters in the <code>TextField</code>
     */
    public int size() {
        synchronized (Display.LCDUILock) {
	    textFieldLF.lUpdateContents();
            return buffer.length();
        }
    }

    /**
     * Gets the current input position.  For some UIs this may block and ask
     * the user for the intended caret position, and on other UIs this may
     * simply return the current caret position.
     *
     * @return the current caret position, <code>0</code> if at the beginning
     */
    public int getCaretPosition() {
        synchronized (Display.LCDUILock) {
            return textFieldLF.lGetCaretPosition();
        }
    }

    /**
     * Sets the input constraints of the <code>TextField</code>. If
     * the the current contents
     * of the <code>TextField</code> do not match the new
     * <code>constraints</code>, the contents are
     * set to empty.
     *
     * @param constraints see <a href="#constraints">input constraints</a>
     *
     * @throws IllegalArgumentException if constraints is not any of the ones
     * specified in <a href="TextField.html#constraints">input constraints</a>
     * @see #getConstraints
     */
    public void setConstraints(int constraints)  {
        synchronized (Display.LCDUILock) {
            setConstraintsImpl(constraints);
        }
    }
  
    /**
     * Gets the current input constraints of the <code>TextField</code>.
     *
     * @return the current constraints value (see
     * <a href="#constraints">input constraints</a>)
     * @see #setConstraints
     */
    public int getConstraints() {
	return constraints;
    }

    /**
     * Sets a hint to the implementation as to the input mode that should be 
     * used when the user initiates editing of this <code>TextField</code>.  The
     * <code>characterSubset</code> parameter names a subset of Unicode 
     * characters that is used by the implementation to choose an initial 
     * input mode.  If <code>null</code> is passed, the implementation should 
     * choose a default input mode.
     *
     * <p>See <a href="#modes">Input Modes</a> for a full explanation of input 
     * modes. </p>
     *
     * @param characterSubset a string naming a Unicode character subset,
     * or <code>null</code>
     *
     */
    public void setInitialInputMode(String characterSubset) {
        synchronized (Display.LCDUILock) {
            initialInputMode = characterSubset;
            textFieldLF.lSetInitialInputMode(initialInputMode);            
        }
    }

    // ========================================================================
    // package private methods
    // ========================================================================

    /**
     * Creates a new <code>TextField</code> object with the given label, initial
     * contents, maximum size in characters, and constraints.  Behaves
     * the same as the public <code>TextField</code> constructor above except
     * for an additional argument <code>forTextBox</code> which signals 
     * this <code>TextField</code> will be used alone as a 
     * <code>TextBox</code> widget.
     * @param label item label
     * @param text the initial contents, or <code>null</code> if the
     * <code>TextField</code> is to be empty
     * @param maxSize the maximum capacity in characters
     * @param constraints see <a href="#constraints">input constraints</a>
     * @param forTextBox true if this textField will be used to implement
     *        a TextBox object.  when false, this method's results are
     *        identical to the public <code>TextField</code> constructor.
     * @throws IllegalArgumentException if <code>maxSize</code> is zero or less
     * @throws IllegalArgumentException if the value of the constraints
     * parameter
     * is invalid
     * @throws IllegalArgumentException if <code>text</code> is illegal
     * for the specified constraints
     * @throws IllegalArgumentException if the length of the string exceeds
     * the requested maximum capacity 
     */    
    TextField(String label, String text, int maxSize, 
	      int constraints, boolean forTextBox) {

        super(label);

        synchronized (Display.LCDUILock) {

            // IllegalArgumentException thrown here
            buffer = new DynamicCharacterArray(maxSize);

	    // Constraint value is checked here. Since textFieldLF is not
	    // yet created, no LF notification will happen.
            setConstraintsImpl(constraints);

	    if (forTextBox) {
		itemLF = textFieldLF = LFFactory.getFactory().getTextBoxLF(this);
	    } else {
		// Create a LF with empty content
		itemLF = textFieldLF = LFFactory.getFactory().getTextFieldLF(this);
	    }
            //
            // this will use inputClient
	    // Right now setCharsImpl notifies LF a content change.
	    // If LF is created as an absolutely last thing then
            // setCharsImple here does not need the notification.
            if (text == null) {
                setCharsImpl(null, 0, 0);
            } else {
                setCharsImpl(text.toCharArray(), 0, text.length());
            }
        }
    }
    /**
     * Deletes characters from the <code>TextField</code>.
     *
     * <p>The <code>offset</code> and <code>length</code> parameters must
     * specify a valid range of characters within
     * the contents of the <code>TextField</code>.
     * The <code>offset</code> parameter must be within the
     * range <code>[0..(size())]</code>, inclusive.
     * The <code>length</code> parameter
     * must be a non-negative integer such that
     * <code>(offset + length) &lt;= size()</code>.</p>
     * 
     * @param offset the beginning of the region to be deleted
     * @param length the number of characters to be deleted
     *
     * @throws IllegalArgumentException if the resulting contents
     * would be illegal for the current
     * <a href="TextField.html#constraints">input constraints</a>
     * @throws StringIndexOutOfBoundsException if <code>offset</code>
     * and <code>length</code> do not
     * specify a valid range within the contents of the <code>TextField</code>
     */
    void deleteImpl(int offset, int length) {

        if (length == 0) {
	    return;
	}

	// Update buffer with latest user input
	textFieldLF.lUpdateContents();

	// Keep old contents in case we need to restore below
	String oldContents = buffer.toString();

	// StringIndexOutOfBoundsException can be thrown here
        buffer.delete(offset, length);

        if (!textFieldLF.lValidate(buffer, constraints)) {
	    // Restore to old contents
	    buffer.delete(0, buffer.length()); 
	    buffer.insert(0, oldContents);
	    throw new IllegalArgumentException();
        }

	// Notify LF that contents has changed due to delete
	textFieldLF.lDelete(offset, length);
    }

    /**
     * Sets the contents of the <code>TextField</code> from a
     * character array, replacing the
     * previous contents.
     *
     * @param data the source of the character data
     * @param offset the beginning of the region of characters to copy
     * @param length the number of characters to copy
     * @throws ArrayIndexOutOfBoundsException if <code>offset</code>
     * and <code>length</code> do not specify
     * a valid range within the data array
     * @throws IllegalArgumentException if <code>data</code>
     * is illegal for the current
     * <a href="TextField.html#constraints">input constraints</a>
     * @throws IllegalArgumentException if the text would exceed the current
     * maximum capacity
     */
    void setCharsImpl(char[] data, int offset, int length) {

        if (data == null) {
            buffer.delete(0, buffer.length());
        } else {
            
            if (offset < 0 || offset > data.length
                || length < 0 || length > data.length
                || offset + length < 0
                || offset + length > data.length) {
                throw new ArrayIndexOutOfBoundsException();
            }
            
            if (length > buffer.capacity()) {
                throw new IllegalArgumentException();
            }
            
            if (length > 0) {
                DynamicCharacterArray dca = 
                    new DynamicCharacterArray(length);
                
                dca.set(data, offset, length);
                
                if (!textFieldLF.lValidate(dca, constraints)) {
                    throw new IllegalArgumentException();
                }
            }
            
            buffer.set(data, offset, length);
        }

	// Notify LF contents has changed due to setChars
	textFieldLF.lSetChars();
    }

    /**
     * Sets the input constraints of the <code>TextField</code>.
     * @param constraints see <a href="#constraints">input constraints</a>
     *
     * @throws IllegalArgumentException if constraints is not any of the ones
     * specified in <a href="TextField.html#constraints">input constraints</a>
     */
    void setConstraintsImpl(int constraints)  {

        if ((constraints & CONSTRAINT_MASK) < ANY || 
            (constraints & CONSTRAINT_MASK) > DECIMAL) { 
                throw new IllegalArgumentException();
        }
        
	this.constraints = constraints;

	// Since this function is called from Constructor before
	// LF is created, checking is necessary.
        if (textFieldLF == null) {
	    return;
	}

	textFieldLF.lSetConstraints();

	// If current contents doesn't satisfy new constraints,
	// set it to empty.
	textFieldLF.lUpdateContents();

	int curLen = buffer.length();
        if (curLen > 0 && !textFieldLF.lValidate(buffer, constraints)) {
            buffer.delete(0, curLen);
	    textFieldLF.lDelete(0, curLen);
        }
    }

    /**
     * Inserts data into the buffer.
     * 
     * @param data - data to be inserted
     * @param offset - <placeholder>
     * @param length - <placeholder>
     * @param position - <placeholder>
     */
    void insertImpl(char data[], int offset, int length, int position) {

	textFieldLF.lUpdateContents();

        int pos = buffer.insert(data, offset, length, position);

        if (!textFieldLF.lValidate(buffer, constraints)) {
            buffer.delete(pos, length); // reverse insertion
            throw new IllegalArgumentException();
        }

	// Notify LF contents has changed due a insertion
        textFieldLF.lInsert(data, offset, length, pos);
    }
    
    /**
     * Return whether the Item takes user input focus.
     *
     * @return Always return <code>true</code> so user can scroll
     * or highlight selection.
     */
    boolean acceptFocus() {
	return true;
    }
    
    /**
     * Notify the item to the effect that it has been recently deleted.
     * In addition to default action call TraverseOut for the TextField 
     */
     void itemDeleted() {
         textFieldLF.itemDeleted();
         super.itemDeleted();
     }

    /**
     * The look&feel associated with this TextField. 
     * Set in the constructor.
     */
    TextFieldLF textFieldLF; // = null

    /** buffer to store the text */
    DynamicCharacterArray buffer;
 
    /** Input constraints */
    int constraints;

    /** the initial input mode for when the text field gets focus */
    String initialInputMode = null; 
}
 
