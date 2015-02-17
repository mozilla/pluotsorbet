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
 * Look and feel implementation for <code>StringItem</code> using 
 * platform widget.
 */
class StringItemLFImpl extends ItemLFImpl implements StringItemLF {
        
    /**
     * Creates a look&amp;feel for a <code>StringItem</code>.
     *
     * @param strItem The <code>StringItem</code> associated with this 
     *                look&amp;feel
     */
    StringItemLFImpl(StringItem strItem) {
        
        super(strItem);
        
        this.strItem = strItem;

        // when no commands are added, the actual appearance
        // is PLAIN; the actual appearance will be the same
        // as appearance set in StringItem if a command is added
        // to this StringItem
        appearanceMode = Item.PLAIN;
    }


    // *****************************************************
    //  Public methods (StringItemLF interface impl)
    // *****************************************************

    /**
     * Notifies L&amp;F of a string change in the corresponding 
     * <code>StringItem</code>.
     *
     * @param str the new string set in the <code>StringItem</code>
     */
    public void lSetText(String str) {
        // Only update native resource if it exists.
        if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
            setContent0(nativeId, str, appearanceMode);
        }

        lRequestInvalidate(true, true);
    }


    /**
     * Notifies L&amp;F of a font change in the corresponding 
     * <code>StringItem</code>.
     *
     * @param font the new font set in the <code>StringItem</code>
     */
    public void lSetFont(Font font) {
        // Only update native resource if it exists.
        if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
            setFont0(nativeId, 
                     font.getFace(), font.getStyle(), font.getSize());
        }

        lRequestInvalidate(true, true);
    }

    /**
     * Gets default font to render text in StringItem if it was not
     * set by the application.
     * @return - the font to render text if it was not set by the app
     */
    public Font getDefaultFont() {
        return Theme.curContentFont;
    }

    /**
     * Notifies L&amp;F of a command addition in the corresponding 
     * <code>StringItem</code>.
     *
     * @param cmd the newly added command
     * @param i the index of the added command in the <code>StringItem</code>'s
     *          commands[] array
     */
    public void lAddCommand(Command cmd, int i) {
        super.lAddCommand(cmd, i);

        if ((strItem.numCommands >= 1) && (appearanceMode == Item.PLAIN)) {
            appearanceMode = strItem.appearanceMode == Item.BUTTON ?
                             Item.BUTTON : Item.HYPERLINK;
            if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
                setContent0(nativeId, strItem.str, appearanceMode);
            }
            lRequestInvalidate(true, true);
        }
    }

    /**
     * Notifies L&amp;F of a command removal in the corresponding 
     * <code>StringItem</code>.
     *
     * @param cmd the newly removed command
     * @param i the index of the removed command in the 
     *          <code>StringItem</code>'s commands[] array
     */
    public void lRemoveCommand(Command cmd, int i) {
        super.lRemoveCommand(cmd, i);

        // restore the value of the original appearanceMode
        if (strItem.numCommands < 1) {
            appearanceMode = Item.PLAIN;
            if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
                setContent0(nativeId, strItem.str, appearanceMode);
            }
            lRequestInvalidate(true, true);
        }
    }

    // *****************************************************
    //  Package private methods
    // *****************************************************

    /**
     * Determine if this <code>Item</code> should have a newline before it.
     *
     * @return <code>true</code> if it should have a newline before
     */
    boolean equateNLB() {
        String label = strItem.label;
        String str   = strItem.str;

        // If label starts with a \n, put this StringItem on a newline
        if (label != null && label.length() > 0) {
            if (label.charAt(0) == '\n') {
                return true;
            }
        } else if (str != null && str.length() > 0) {
            // If there is no label and our content starts with a \n,
            // this StringItem starts on a newline
            if (str.charAt(0) == '\n') {
                return true;
            }
        } else {
            // empty StringItem
            return false;
        }
        
        if ((strItem.layout & Item.LAYOUT_2) == Item.LAYOUT_2) {
            return ((strItem.layout & Item.LAYOUT_NEWLINE_BEFORE)
                    == Item.LAYOUT_NEWLINE_BEFORE);
        }
        
        // LAYOUT_2 was not set, hence we need to provide backward 
        // compatibility with MIDP1.0 where any StringItem with a 
        // non-null label would go on a new line.
        return label != null && label.length() > 0;
    }
    
    /**
     * Determine if this <code>Item</code> should have a newline after it.
     *
     * @return <code>true</code> if it should have a newline after
     */
    boolean equateNLA() {
        
        String label = item.label;
        String str = strItem.str;

        // If content ends with a \n, there is a newline after 
        // this StringItem
        if (str != null && str.length() > 0) {
            if (str.charAt(str.length() - 1) == '\n') {
                return true;
            }
        } else if (label != null && label.length() > 0) {
            // If there is no content and our label ends with a \n, 
            // there is a newline after this StringItem
            if (label.charAt(label.length() - 1) == '\n') {
                return true;
            }
        } else {
            // empty StringItem
            return false;
        }
        
        if ((strItem.layout & Item.LAYOUT_2) == Item.LAYOUT_2) {
            return ((item.layout & Item.LAYOUT_NEWLINE_AFTER)
                    == Item.LAYOUT_NEWLINE_AFTER);
        }
        return false;
    }

    /**
     * Called by event delivery to notify an <code>ItemLF</code> in current 
     * <code>FormLF</code> of a change in its peer state.
     * Handle special gesture of default command.
     *
     * @param hint <code>-1</code> signals that user performed the 
     *             special gesture of default command
     *
     * @return always <code>false</code> so <code>ItemStateListener</code> 
     *         will not be notified
     */
    boolean uCallPeerStateChanged(int hint) { 
        // activate default command if hint is -1
        if (hint == -1) {

            Command defaultCommand;
            ItemCommandListener commandListener;

            synchronized (Display.LCDUILock) {

                defaultCommand  = strItem.defaultCommand;
                commandListener = strItem.commandListener;
            }

            if (defaultCommand != null && commandListener != null) {

                // Protect from any unexpected application exceptions
                try {
                    synchronized (Display.calloutLock) {
                        commandListener.commandAction(defaultCommand, strItem);
                    }
                } catch (Throwable thr) {
                    Display.handleThrowable(thr);
                }
            }
        }

        // Indicate to Form to not notify ItemStateListener
        return false;
    }
    
    /**
     * Create native resource for current <code>StringItem</code>.
     * Override function in <code>ItemLFImpl</code>.
     *
     * @param ownerId Owner screen's native resource id
     */
    void createNativeResource(int ownerId) {
        nativeId = createNativeResource0(ownerId,
                                         strItem.label, strItem.layout,
                                         strItem.str, 
                                         appearanceMode, strItem.font);
    }

    /**
     * KNI function that create native resource for current 
     * <code>StringItem</code>.
     *
     * @param ownerId Owner screen's native resource id 
     *                (<code>MidpDisplayable *</code>)
     * @param label label to be used for this <code>Item</code>
     * @param layout layout directive associated with this <code>Item</code>
     * @param text text to be used for this <code>StringItem</code>
     * @param appearanceMode should be <code>PLAIN</code>, 
     *                       <code>HYPERLINK</code> or <code>BUTTON</code>
     * @param font font face to be used for rendering <code>StringItem</code> 
     *             content
     * 
     * @return native resource id (<code>MidpItem *</code>) of this 
     *         <code>StringItem</code>
     */
    private native int createNativeResource0(int ownerId,
                                             String label, 
                                             int layout,
                                             String text,
                                             int appearanceMode, 
                                             Font font);


    /**
     * KNI function that sets text on the native resource corresponding
     * to the current <code>StringItem</code>.
     *
     * @param nativeId native resource id for this item
     * @param text new text set on the current <code>StringItem</code>
     * @param appearanceMode the appearance mode of the text passed in
     */
    private native void setContent0(int nativeId, String text, 
                                    int appearanceMode);
    

    /**
     * KNI function that sets font on the native resource corresponding
     * to the current <code>StringItem</code>.
     *
     * @param nativeId native resource id for this item
     * @param face face of the new font
     * @param style style of the new font
     * @param size size of the new font
     */
    private native void setFont0(int nativeId, int face, int style, int size);


    /** 
     * The <code>StringItem</code> associated with this view.
     */
    private StringItem strItem;

    /**
     * The appearance mode.
     * The actual appearance of a <code>StringItem</code> could be different to
     * the one set in <code>StringItem</code>. A <code>StringItem</code> 
     * created with <code>PLAIN</code> appearance will look like a 
     * <code>HYPERLINK</code> if commands were added. 
     */
    private int appearanceMode;
}
