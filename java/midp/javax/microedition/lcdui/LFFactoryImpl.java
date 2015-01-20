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
* This is a factory class for the look &amp; feel classes.
*/
class LFFactoryImpl extends LFFactory {

    // *** Displayable look & feels *****

    /**
     * Returns an <code>Alert</code> look &amp; feel implementation.
     *
     * @param a The <code>Alert</code> associated with this look&amp;feel
     *
     * @return an <code>Alert</code> look &amp; feel implementation
     */
    AlertLF getAlertLF(Alert a) {
	return new AlertLFImpl(a);
    }

    /**
     * Returns a <code>Canvas</code> look &amp; feel implementation.
     *
     * @param c The <code>Canvas</code> associated with this look&amp;feel
     *
     * @return a <code>Canvas</code> look &amp; feel implementation
     */
    CanvasLF getCanvasLF(Canvas c) {
	return new CanvasLFImpl(c);
    }

    /**
     * Returns a <code>Form</code> look &amp; feel implementation.
     *
     * @param f The <code>Form</code> associated with this look&amp;feel
     *
     * @return a <code>Form</code> look &amp; feel implementation
     */
    FormLF getFormLF(Form f) {
	return new FormLFImpl(f, f.items, f.numOfItems);
    }

    /**
     * Returns a <code>List</code> look &amp; feel implementation.
     *
     * @param list The <code>List</code> associated with this look&amp;feel
     *
     * @return a <code>List</code> look &amp; feel implementation
     */
    FormLF getListLF(List list) {
	return new FormLFImpl(list, list.cg);
    }

    /**
     * Returns a <code>TextBox</code> look &amp; feel implementation.
     *
     * @param tb The <code>TextBox</code> associated with this look&amp;feel
     *
     * @return a <code>TextBox</code> look &amp; feel implementation
     */
    FormLF getTextBoxFormLF(TextBox tb) {
	return new FormLFImpl(tb, tb.textField);
    }

    // ** Item look & feels ******** */

    /**
     * Returns a <code>ChoiceGroup</code> look &amp; feel implementation.
     *
     * @param cg The <code>ChoiceGroup</code> associated with this 
     *           look&amp;feel
     *
     * @return a <code>ChoiceGroup</code> look &amp; feel implementation
     */
    ChoiceGroupLF getChoiceGroupLF(ChoiceGroup cg) {
	return new ChoiceGroupLFImpl(cg);
    }

    /**
     * Returns a <code>CustomItem</code> look &amp; feel implementation.
     *
     * @param ci The <code>CustomItem</code> associated with this 
     *             look&amp;feel
     *
     * @return a <code>CustomItem</code> look &amp; feel implementation
     */
    CustomItemLF getCustomItemLF(CustomItem ci) {
	return new CustomItemLFImpl(ci);
	
    }  

    /**
     * Returns a <code>DateField</code> look &amp; feel implementation.
     *
     * @param df The <code>DateField</code> associated with this look&amp;feel
     *
     * @return a <code>DateField</code> look &amp; feel implementation
     */  
    DateFieldLF getDateFieldLF(DateField df) {
	return new DateFieldLFImpl(df);
    }
    
    /**
     * Returns a <code>Gauge</code> look &amp; feel implementation.
     *
     * @param g The <code>Gauge</code> associated with this look&amp;feel
     *
     * @return a <code>Gauge</code> look &amp; feel implementation
     */
    GaugeLF getGaugeLF(Gauge g) {
	return new GaugeLFImpl(g);
    }

    /**
     * Returns an <code>ImageItem</code> look &amp; feel implementation.
     *
     * @param imgItem The <code>Alert</code> associated with this look&amp;feel
     *
     * @return an <code>ImageItem</code> look &amp; feel implementation
     */
    ImageItemLF getImageItemLF(ImageItem imgItem) {
	return new ImageItemLFImpl(imgItem);
    }

    /**
     * Returns a <code>StringItem</code> look &amp; feel implementation.
     *
     * @param strItem The <code>StringItem</code> associated with this 
     *                look&amp;feel
     *
     * @return a <code>StringItem</code> look &amp; feel implementation
     */
    StringItemLF getStringItemLF(StringItem strItem) {
	return new StringItemLFImpl(strItem);
    }

    /**
     * Returns a <code>Spacer</code> look &amp; feel implementation.
     *
     * @param spacer The <code>Spacer</code> associated with this look&amp;feel
     *
     * @return a <code>Spacer</code> look &amp; feel implementation
     */
    SpacerLF getSpacerLF(Spacer spacer) {
        return new SpacerLFImpl(spacer);
    }

    /**
     * Returns a <code>TextField</code> look &amp; feel implementation.
     *
     * @param tf The <code>TextField</code> associated with this look&amp;feel
     *
     * @return a <code>TextField</code> look &amp; feel implementation
     */
    TextFieldLF getTextFieldLF(TextField tf) {
	    return new TextFieldLFImpl(tf);
    }

    /**
     * Returns a TextField look & feel implementation 
     * for use in a TextBox screen.
     *
     * @param tf - <placeholder>
     * @return a TextField look & feel implementation
     */
    TextFieldLF getTextBoxLF(TextField tf) {
	return new TextFieldLFImpl(tf);
    }

    /**
     * Returns a <code>Ticker</code> look &amp; feel implementation.
     *
     * @param ticker The <code>Ticker</code> associated with this look&amp;feel
     *
     * @return a <code>Ticker</code> look &amp; feel implementation.
     */
    TickerLF getTickerLF(Ticker ticker) {
	return new TickerLFImpl(ticker);
    }
}
