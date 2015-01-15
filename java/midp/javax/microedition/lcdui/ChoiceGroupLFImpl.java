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

import com.sun.midp.configurator.Constants;
import javax.microedition.lcdui.ChoiceGroup.CGElement;

/**
 *  This is the look and feel implementation for ChoiceGroup.
 */
class ChoiceGroupLFImpl extends ItemLFImpl implements ChoiceGroupLF {

    /**
     * Creates ChoiceLF for the passed in ChoiceGroup.
     * @param choiceGroup - the ChoiceGroup object associated with this view
     */
    ChoiceGroupLFImpl(ChoiceGroup choiceGroup) {
        super(choiceGroup);

        cg = choiceGroup;

        if (cg.numOfEls > 0 && cg.choiceType != Choice.MULTIPLE) {
            selectedIndex = 0;
            cg.cgElements[selectedIndex].setSelected(true);
        }
    }

    // *******************************************************
    // ChoiceGroupLF implementation
    // ********************************************************

    /**
     * Notifies Look &amps; Feel that an element was inserted into the 
     * <code>ChoiceGroup</code> at the the elementNum specified.
     *
     * @param elementNum the index of the element where insertion occurred
     * @param stringPart the string part of the element to be inserted
     * @param imagePart the image part of the element to be inserted,
     * or <code>null</code> if there is no image part
     */
    public void lInsert(int elementNum, String stringPart, Image imagePart) {
        // make sure that there is a default selection
        if (cg.choiceType != Choice.MULTIPLE) {
            if (selectedIndex == -1) {
                selectedIndex = 0;
                cg.cgElements[selectedIndex].setSelected(true);
            } else if (elementNum < selectedIndex &&
                       nativeId == DisplayableLFImpl.INVALID_NATIVE_ID) {
                // an element was inserted before selectedIndex and
                // selectedIndex has to be updated
                selectedIndex++;
            }
        }

        // Only update native resource if it exists.
        if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
            ImageData imagePartData = null;

            if (imagePart != null) {
              imagePartData = imagePart.getImageData();
            }

            insert0(nativeId, elementNum, 
                    stringPart, imagePartData, 
                    cg.cgElements[elementNum].selected);
        }

        lRequestInvalidate(true, true);
    }

    /**
     * Notifies Look &amps; Feel that an element referenced by
     * <code>elementNum</code> was deleted in the corresponding
     * ChoiceGroup.
     *
     * @param elementNum the index of the deleted element
     */
    public void lDelete(int elementNum) {

        // adjust selected index
        if (cg.numOfEls == 0) {
            selectedIndex = -1;
        } else if (cg.choiceType != ChoiceGroup.MULTIPLE) {
            if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
                if (selectedIndex != -1 && selectedIndex < cg.numOfEls) {
                    cg.cgElements[selectedIndex].setSelected(false);
                }
                selectedIndex = getSelectedIndex0(nativeId);
            }

            if (elementNum < selectedIndex) {
                selectedIndex--;
            } else if (elementNum == selectedIndex &&
                       selectedIndex == cg.numOfEls) {
                // last element is selected and deleted - 
                // new last should be selected
                selectedIndex = cg.numOfEls - 1;
            }
            cg.cgElements[selectedIndex].setSelected(true);
        }

        if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
            delete0(nativeId, elementNum, selectedIndex);
        }

        lRequestInvalidate(true, true);
    }

    /**
     * Notifies Look &amps; Feel that all elements 
     * were deleted in the corresponding ChoiceGroup.
     */
    public void lDeleteAll() {
        // Only update native resource if it exists.
        if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
            deleteAll0(nativeId);
        }
        selectedIndex = -1;
        lRequestInvalidate(true, true);
    }

    /**
     * Notifies Look &amps; Fell that the <code>String</code> and 
     * <code>Image</code> parts of the
     * element referenced by <code>elementNum</code> were set in
     * the corresponding ChoiceGroup,
     * replacing the previous contents of the element.
     *
     * @param elementNum the index of the element set
     * @param stringPart the string part of the new element
     * @param imagePart the image part of the element, or <code>null</code>
     * if there is no image part
     */
    public void lSet(int elementNum, String stringPart, Image imagePart) {
        // Only update native resource if it exists.
        if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
            ImageData imagePartData = null;
            if (imagePart != null) {
              imagePartData = imagePart.getImageData();
            }

            // for selected value to be passed correctly to the 
            // newly created element we have to do the sync first
            // (alternatively we could rely on native to maintain
            // the selected state correctly)
            syncSelectedIndex();
            syncSelectedFlags();
            set0(nativeId, elementNum, 
                 stringPart, imagePartData, 
                 cg.cgElements[elementNum].selected);
        }
        lRequestInvalidate(true, true);
    }

    /**
     * Notifies Look &amps; Feel that an element was selected (or
     * deselected) in the corresponding ChoiceGroup.
     *
     * @param elementNum the number of the element. Indexing of the
     * elements is zero-based
     * @param selected the new state of the element <code>true=selected</code>,
     * <code>false=not</code> selected
     */
    public void lSetSelectedIndex(int elementNum, boolean selected) {
        // Only update native resource if it exists.
        if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
            setSelectedIndex0(nativeId, elementNum, selected);
        } else {
            if (cg.choiceType == Choice.MULTIPLE) {
                cg.cgElements[elementNum].setSelected(selected);
            } else {
                // selected item cannot be deselected in 
                // EXCLUSIVE, IMPLICIT, POPUP ChoiceGroup
                if (!selected ||
                    (/* choiceType != Choice.IMPLICIT && */
                     selectedIndex == elementNum)) {
                    return;
                }
                
                cg.cgElements[selectedIndex].setSelected(false);
                selectedIndex = elementNum;
                cg.cgElements[selectedIndex].setSelected(true);
            }
        }
    }

    /**
     * Notifies Look &amps; Feel that selected state was changed on
     * several elements in the corresponding MULTIPLE ChoiceGroup
     * (cannot be null).
     * @param selectedArray an array in which the method collect the
     * selection status
     */
    public void lSetSelectedFlags(boolean[] selectedArray) {
        // Only update native resource if it exists.
        if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
            setSelectedFlags0(nativeId, selectedArray, 
                              selectedArray.length);
        }
    }

    /**
     * Notifies Look &amps; Feel that a new text fit policy was set
     * in the corresponding ChoiceGroup.
     * @param fitPolicy preferred content fit policy for choice elements
     */
    public void lSetFitPolicy(int fitPolicy) {
        // Only update native resource if it exists.
        if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
            setFitPolicy0(nativeId, fitPolicy);
	    lRequestInvalidate(true, true);
        }
    }

    /**
     * Notifies Look &amps; Feel that a new font was set for an
     * element with the  specified elementNum in the 
     * corresponding ChoiceGroup.
     * @param elementNum the index of the element, starting from zero
     * @param font the preferred font to use to render the element
     */
    public void lSetFont(int elementNum, Font font) {
        // Only update native resource if it exists.
        if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
            setFont0(nativeId, elementNum, 
                     font.getFace(), font.getStyle(), font.getSize());
	    lRequestInvalidate(true, true);
        }
    }

    /**
     * Gets default font to render ChoiceGroup element if it was not
     * set by the application
     * @return - the font to render ChoiceGroup element if it was not 
     *           set by the app
     */
    public Font getDefaultFont() {
	return Theme.curContentFont;
    }

    /**
     * Gets currently selected index 
     * @return currently selected index
     */
    public int lGetSelectedIndex() {
        if (nativeId == DisplayableLFImpl.INVALID_NATIVE_ID) {
            return selectedIndex;
        } else {
            // sync with native
            syncSelectedIndex();
            return selectedIndex;
        }
    }


    /**
     * Gets selected flags.(only elements corresponding to the 
     * elements are expected to be filled). ChoiceGroup sets the rest to
     * false
     * @param selectedArray_return to contain the results
     * @return the number of selected elements
     */
    public int lGetSelectedFlags(boolean[] selectedArray_return) {
        int countSelected = 0;
        if (nativeId == DisplayableLFImpl.INVALID_NATIVE_ID) {
            for (int i = 0; i < cg.numOfEls; i++) {
                selectedArray_return[i] = cg.cgElements[i].selected;
                if (selectedArray_return[i]) {
                    countSelected++;
                }
            }
            
        } else {
            countSelected = getSelectedFlags0(nativeId, selectedArray_return,
                                              cg.numOfEls);
            
            // sync with native
            for (int i = 0; i < cg.numOfEls; i++) {
                cg.cgElements[i].setSelected(selectedArray_return[i]);
            }
        }
        return countSelected;
    }


    /**
     * Determines if an element with a passed in index
     * is selected or not.
     * @param elementNum the index of an element in question
     * @return true if the element is selected, false - otherwise
     */
    public boolean lIsSelected(int elementNum) {
        if (nativeId == DisplayableLFImpl.INVALID_NATIVE_ID) {
            return cg.cgElements[elementNum].selected;
        }
        
        return isSelected0(nativeId, elementNum);
    }

    // *****************************************************
    //  Package private methods
    // *****************************************************

    /**
     * Called by event delivery to notify an ItemLF in current FormLF
     * of a change in its peer state.
     *
     * @param hint index of the element whose selection status has changed
     * @return always true so ItemStateListener should be notified
     */
    boolean uCallPeerStateChanged(int hint) {
	// Any hint means selection has change
	// For types other than IMPLICIT List, notify itemStateListener.
	if (cg.choiceType != Choice.IMPLICIT) {
	    return true; // notify itemStateListener
	}

	// For IMPLICIT List, notify commandListener
	List list;
	CommandListener cl;
	Command cmd;

	synchronized (Display.LCDUILock) {
	    list = (List)cg.owner;

	    if (list.listener == null ||
		list.selectCommand == null ||
		cg.numOfEls == 0) {
		return false; // No itemStateListener to notify
	    }

	    cl = list.listener;
	    cmd = list.selectCommand;
	}
	
	try {
	    synchronized (Display.calloutLock) {
		cl.commandAction(cmd, list);
	    }
	} catch (Throwable thr) {
	    Display.handleThrowable(thr);
	}

	return false; // No itemStateListener to notify
    }

    /**
     * Determine if this Item should have a newline after it
     *
     * @return true if it should have a newline after
     */
    boolean equateNLA() {
        if (super.equateNLA()) {
            return true;
        }
        return ((cg.layout & Item.LAYOUT_2) != Item.LAYOUT_2);
    }


    /**
     * Determine if this Item should have a newline before it
     *
     * @return true if it should have a newline before
     */
    boolean equateNLB() {
        if (super.equateNLB()) {
            return true;
        }

        return ((cg.layout & Item.LAYOUT_2) != Item.LAYOUT_2);
    }

    /**
    /**
     * Override <code>ItemLFImpl</code> method to sync with native resource
     * before hiding the native resource. Selection of native resource will 
     * be preserved before the resource is hidden.
     */
    void lHideNativeResource() {
	// sync selected flags and selectedIndex
	// before any visible native resource is deleted.
	if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
	    syncSelectedIndex();
	    syncSelectedFlags();
	}

	// Hide native resource
        super.lHideNativeResource();
    }

    /**
     * Creates and sets  native resource for current ChoiceGroup.
     * Override function in ItemLFImpl.
     * @param ownerId Owner screen's native resource id
     */
    void createNativeResource(int ownerId) {
        nativeId = createNativeResource0(ownerId, cg.label, 
					 (cg.owner instanceof List ? 
					  -1 : cg.layout),
                                         cg.choiceType, cg.fitPolicy, 
                                         cg.cgElements, cg.numOfEls, 
                                         selectedIndex);
    }

    // *****************************************************
    //  Private methods
    // *****************************************************

    /**
     * Read and save user selection from native resource.
     */
    private void syncSelectedIndex() {
        if (cg.choiceType != Choice.MULTIPLE) {
            int newSelectedIndex = getSelectedIndex0(nativeId);
            if (selectedIndex != newSelectedIndex) {
                if (selectedIndex != -1) {
                    cg.cgElements[selectedIndex].setSelected(false);
                }
                selectedIndex = newSelectedIndex;
                if (selectedIndex != -1) {
                    cg.cgElements[selectedIndex].setSelected(true);
                }
            }
        }
    }

    /**
     * Read and save user selection from native resource.
     */
    private void syncSelectedFlags() {
        if (cg.numOfEls > 0 && cg.choiceType == Choice.MULTIPLE) {
            boolean[] selectedArray_return = new boolean[cg.numOfEls];
            
            getSelectedFlags0(nativeId, selectedArray_return,
                              cg.numOfEls);
            
            for (int i = 0; i < cg.numOfEls; i++) {
                cg.cgElements[i].setSelected(selectedArray_return[i]);
            }
        }
    }

    /**
     * KNI function that creates native resource for current ChoiceGroup.
     * @param ownerId Owner screen's native resource id (MidpDisplayable *)
     * @param label string to be used as label for this ChoiceGroup
     * @param layout layout directive associated with this <code>Item</code>
     * @param choiceType should be EXCLUSIVE, MULTIPLE, IMPLICIT, POPUP
     * @param fitPolicy should be TEXT_WRAP_DEFAULT, TEXT_WRAP_ON, or
     *                    TEXT_WRAP_OFF
     * @param cgElements array of CGElement that stores such data as
     *                   image, text, font, selection state per element
     * @param numChoices number of valid elements in cgElements array
     * @param selectedIndex index of a currently selected element
     *                      (has no meaning for MULTIPLE ChoiceGroup)
     * @return native resource id (MidpItem *) of this ChoiceGroup
     */
    private native int createNativeResource0(int ownerId, String label, 
					     int layout,
                                             int choiceType, int fitPolicy,
                                             CGElement []cgElements,
                                             int numChoices,
                                             int selectedIndex);


    /**
     * KNI function that notifies native resource of a new element
     * inserted prior to the element specified
     * 
     * @param nativeId native resource id (MidpItem *) of this ChoiceGroup
     * @param elementNum the index of an element where insertion is to occur
     * @param stringPart the string part of the element to be inserted
     * @param imagePart the image part of the element to be inserted
     * @param selected the selected state of the element to be inserted
     */
    private native void insert0(int nativeId, int elementNum, 
                                String stringPart, ImageData imagePart,
                                boolean selected);

    /**
     * KNI function that notifies native resource of a specified element
     * being deleted in the corresponding ChoiceGroup.
     *
     * @param nativeId native resource id (MidpItem *) of this ChoiceGroup
     * @param elementNum the index of an element to be deleted
     * @param selectedIndex the index of an element to be selected after the
     *        deletion is done (has no meaning for MULTIPLE ChoiceGroup)
     */
    private native void delete0(int nativeId, int elementNum, 
                                int selectedIndex); 

    /**
     * KNI function that notifies native resource that all elements were
     * deleted in the corresponding ChoiceGroup.
     *
     * @param nativeId native resource id (MidpItem *) of this ChoiceGroup
     */
    private native void deleteAll0(int nativeId);

    /**
     * KNI function that notifies native resource of a specified element
     * being set in the corresponding ChoiceGroup.
     *
     * @param nativeId native resource id (MidpItem *) of this ChoiceGroup
     * @param elementNum the index of an element to be set
     * @param stringPart the string part of the element to be set
     * @param imagePart the image part of the element to be set
     * @param selected the selected state of the element to be set
     */
    private native void set0(int nativeId, int elementNum, 
                             String stringPart, ImageData imagePart, 
                             boolean selected);

    /**
     * KNI function that notifies native resource of an element's new selected
     * state in the corresponding ChoiceGroup.
     *
     * @param nativeId native resource id (MidpItem *) of this ChoiceGroup
     * @param elementNum the index of an element which selected state changed
     * @param selected the new selected state
     */
    private native void setSelectedIndex0(int nativeId, int elementNum,
                                          boolean selected);
    /**
     * KNI function that notifies native resource of new selected
     * states in the corresponding ChoiceGroup.
     *
     * @param nativeId native resource id (MidpItem *) of this ChoiceGroup
     * @param selectedArray array with new selected states
     * @param numSelectedArray number of elements in selectedArray to be used
     */
    private native void setSelectedFlags0(int nativeId, 
                                          boolean []selectedArray,
                                          int numSelectedArray);
    /**
     * KNI function that notifies native resource of fit policy change
     * in the corresponding ChoiceGroup.
     *
     * @param nativeId native resource id (MidpItem *) of this ChoiceGroup
     * @param fitPolicy new fit policy (can be TEXT_WRAP_OFF, TEXT_WRAP_ON,
     *                  or TEXT_WRAP_DEFAULT)
     */
    private native void setFitPolicy0(int nativeId, int fitPolicy);

    /**
     * KNI function that notifies native resource of an element's new font
     * setting in the corresponding ChoiceGroup.
     *
     * @param nativeId native resource id (MidpItem *) of this ChoiceGroup
     * @param elementNum the index of an element which font has changed
     * @param face of the newly set font
     * @param style of the newly set font 
     * @param size of newly set font
     */
    private native void setFont0(int nativeId, int elementNum,
                                 int face, int style, int size);

    /**
     * KNI function that gets index of a currently selected index from 
     * the ChoiceGroup's native resource.
     *
     * @param nativeId native resource id (MidpItem *) of this ChoiceGroup
     * @return index of the currently selected element
     */
    private native int getSelectedIndex0(int nativeId);

    /**
     * KNI function that queries the state of all elements in the native
     * resource and returns it in the passed in selectedArray array.
     *
     * @param nativeId native resource id (MidpItem *) of this ChoiceGroup
     * @param selectedArray to contain the results
     * @param numOfEls number of elements in selectedArray
     * @return the number of elements selected in the native resource
     */
    private native int getSelectedFlags0(int nativeId, 
                                         boolean[] selectedArray,
                                         int numOfEls);
    /**
     * KNI function that queries the state of an element in the native
     * resource
     *
     * @param nativeId native resource id (MidpItem *) of this ChoiceGroup
     * @param elementNum the index of an element which state is queried
     * @return the current state of an element in the nativer resource
     */
    private native boolean isSelected0(int nativeId, int elementNum);

    /** ChoiceGroup associated with this ChoiceGroupLF. */
    ChoiceGroup cg;

    /**
     * The currently selected index of this ChoiceGroup (-1 by default).
     */
    int selectedIndex = -1;
}
