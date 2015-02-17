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
 * Platform widget look and feel of <code>ImageItem</code>.
 */
class ImageItemLFImpl extends ItemLFImpl implements ImageItemLF {

    /**
     * Creates look &amp; feel for an <code>ImageItem</code>.
     *
     * @param imageItem the <code>ImageItem</code> associated with this 
     *                  look &amp; feel
     */
    ImageItemLFImpl(ImageItem imageItem) {
        super(imageItem);
        
        ImageData imageData = null;
        if (imageItem != null) {
          Image image = imageItem.immutableImg;
          if (image != null) {
            imageData = image.getImageData();
          }
        }

        this.imgItem = imageItem;

        this.itemImageData = imageData;

        // when no commands are added, the actual appearance
        // is PLAIN; the actual appearance will be the same
        // as appearance set in ImageItem if a command is added
        // to this ImageItem
        appearanceMode = Item.PLAIN;
    }

    // *****************************************************
    //  Public methods (ImageItemLF impl)
    // *****************************************************
    

    /**
     * Notifies L&amp;F of an image change in the corresponding 
     * <code>ImageItem</code>.
     *
     * @param img the new image set in the <code>ImageItem</code>
     */
    public void lSetImage(Image img) {
        // Only update native resource if it exists.
        if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
            ImageData imageData = null;
            if (img != null) {
              imageData = img.getImageData();
            }
            setContent0(nativeId, imageData, 
                        imgItem.altText, appearanceMode);
        }
        lRequestInvalidate(true, true);
    }

    /**
     * Notifies L&amp;F of an alternative text change
     * in the corresponding <code>ImageItem</code>.
     *
     * @param altText the new alternative text set in the 
     *                <code>ImageItem</code>
     */
    public void lSetAltText(String altText) {
        // Only update native resource if it exists.
        if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
            setContent0(nativeId, itemImageData, altText, 
                        appearanceMode);
        }
        lRequestInvalidate(true, true);
    }

    /**
     * Notifies L&amp;F of a command addition in the corresponding 
     * <code>ImageItem</code>.
     *
     * @param cmd the newly added command
     * @param i the index of the added command in the <code>ImageItem</code>'s
     *        commands[] array
     */
    public void lAddCommand(Command cmd, int i) {
        super.lAddCommand(cmd, i);

        if ((imgItem.numCommands >= 1) && (appearanceMode == Item.PLAIN)) {
            appearanceMode = imgItem.appearanceMode == Item.BUTTON ?
                             Item.BUTTON : Item.HYPERLINK;
            if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
                setContent0(nativeId, itemImageData, imgItem.altText, 
                            appearanceMode);
            }
            lRequestInvalidate(true, true);
        }
    }

    /**
     * Notifies L&amp;F of a command removal in the corresponding 
     * <code>ImageItem</code>.
     *
     * @param cmd the newly removed command
     * @param i the index of the removed command in the 
     *          <code>ImageItem</code>'s commands[] array
     */
    public void lRemoveCommand(Command cmd, int i) {
        super.lRemoveCommand(cmd, i);

        // restore the value of the original appearanceMode
        if (imgItem.numCommands < 1) {
            appearanceMode = Item.PLAIN;
            if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
                setContent0(nativeId, itemImageData, imgItem.altText, 
                            appearanceMode);
            }
            lRequestInvalidate(true, true);
        }
    }

    // *****************************************************
    //  Package private methods
    // *****************************************************
    /**
     * Create native resource for current <code>ImageItem</code>.
     * Override function in <code>ItemLFImpl</code>.
     *
     * @param ownerId Owner screen's native resource id
     */
    void createNativeResource(int ownerId) {
        nativeId = createNativeResource0(ownerId,
                                         imgItem.label, 
                                         imgItem.layout,
                                         itemImageData,
                                         imgItem.altText, 
                                         appearanceMode);
    }

    // *****************************************************
    //  Private methods
    // *****************************************************

    /**
     * Determine if this <code>Item</code> should have a newline before it.
     *
     * @return <code>true</code> if it should have a newline before
     */
    boolean equateNLB() {
        // MIDP1.0 already had an ability to set LAYOUT_NEWLINE_BEFORE.
        // Hence there is no need to check for LAYOUT_2 (as in StringItem)
        if (super.equateNLB()) {
            return true;
        }

        // LAYOUT_NEWLINE_BEFORE is not set but LAYOUT_2 is set
        // which means that items could be positioned side by side
        if ((imgItem.layout & Item.LAYOUT_2) == Item.LAYOUT_2) {
            return false;
        }
        
        // LAYOUT_2 was not set, hence we need to provide backward 
        // compatibility with MIDP1.0 where any ImageItem with a 
        // non-null label would go on a new line.
        return imgItem.label != null && imgItem.label.length() > 0;
    }

    /**
     * Called by event delivery to notify an ItemLF in current FormLF
     * of a change in its peer state.
     * Handle special gesture of default command.
     *
     * @param hint <code>-1</code> signals that user performed 
     *             the special gesture of default command
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

                defaultCommand  = imgItem.defaultCommand;
                commandListener = imgItem.commandListener;
            }

            if (defaultCommand != null && commandListener != null) {

                // Protect from any unexpected application exceptions
                try {
                    synchronized (Display.calloutLock) {
                        commandListener.commandAction(defaultCommand, imgItem);
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
     * KNI function that creates native resource for current 
     * <code>ImageItem</code>.
     *
     * @param ownerId Owner screen's native resource id 
     *                (<code>MidpDisplayable *</code>)
     * @param label label to be used for this <code>Item</code>
     * @param layout layout directive associated with this <code>Item</code>
     * @param imageData ImageData to be used for this <code>ImageItem</code>
     * @param altText alternative text to be used for this 
     *                <code>ImageItem</code>
     * @param appearanceMode should be <code>PLAIN</code>, 
     *                       <code>HYPERLINK</code> or <code>BUTTON</code>
     *
     * @return native resource id (<code>MidpItem *</code>) of this 
     *         <code>ImageItem</code>
     */
    private native int createNativeResource0(int ownerId,
                                             String label, 
                                             int layout,
                                             ImageData imageData,
                                             String altText,
                                             int appearanceMode);


    /**
     * KNI function that sets image on the native resource corresponding
     * to the current <code>ImageItem</code>.
     *
     * @param nativeId native resource id for this item
     * @param imgData <code>ImageData</code> instance associated with 
     *        a new <code>Image</code> set on the current 
     *        <code>ImageItem</code>
     * @param text new alternative text set on the current 
     *             <code>ImageItem</code>
     * @param appearanceMode the actual appearance mode to be used
     */
    private native void setContent0(int nativeId, 
                                    ImageData imageData, 
                                    String text,
                                    int appearanceMode);


    /** 
     * The <code>ImageItem</code> associated with this view.
     */
    private final ImageItem imgItem;

    /** 
     * The <code>ImageData</code> associated with the item.
     */
    private final ImageData itemImageData;

    /**
     * Appearance mode.
     * The actual appearance of an <code>ImageItem</code> could be different to
     * the one set in <code>ImageItem</code>. An <code>ImageItem</code> 
     * created with <code>PLAIN</code> appearance will look like a 
     * <code>HYPERLINK</code> if commands were added. 
     */
    private int appearanceMode;

} // ImageItemLFImpl
