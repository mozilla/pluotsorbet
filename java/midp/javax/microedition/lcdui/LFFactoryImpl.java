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
* This is a factory class for the look & feel classes.
*/
class LFFactoryImpl extends LFFactory {

    // *** Displayable look & feels *****

    /**
     * Returns an Alert look & feel implementation.
     *
     * @param a - <placeholder>
     * @return an Alert look & feel implementation
     */
    AlertLF getAlertLF(Alert a) {
        return new AlertLFImpl(a);
    }

    /**
     * Returns a Canvas look & feel implementation.
     *
     * @param c - <placeholder>
     * @return a Canvas look & feel implementation
     */
    CanvasLF getCanvasLF(Canvas c) {
        return new CanvasLFImpl(c);
    }

    /**
     * Returns a Form look & feel implementation.
     *
     * @param f - <placeholder>
     * @return a Form look & feel implementation
     */
    FormLF getFormLF(Form f) {
        return new FormLFImpl(f, f.items, f.numOfItems);
    }

    /**
     * Returns a List look & feel implementation.
     *
     * @param list - <placeholder>
     * @return a List look & feel implementation
     */
    FormLF getListLF(List list) {
        return new FormLFImpl(list, list.cg);
    }

    /**
     * Returns a TextBox look & feel implementation.
     *
     * @param tb - <placeholder>
     * @return a TextBox look & feel implementation
     */
    FormLF getTextBoxFormLF(TextBox tb) {
        return new FormLFImpl(tb, tb.textField);
    }

    // ** Item look & feels ******** */

    /**
     * Returns a ChoiceGroup look & feel implementation.
     *
     * @param item - <placeholder>
     * @return a ChoiceGroup look & feel implementation
     */
    ChoiceGroupLF getChoiceGroupLF(ChoiceGroup item) {       
        ChoiceGroup cg = item;
        if (cg.choiceType == Choice.POPUP) {
            return new ChoiceGroupPopupLFImpl(cg);
        } else {
            return new ChoiceGroupLFImpl(cg);
        }
    }

    /**
     * Returns a CustomItem look & feel implementation.
     *
     * @param item - <placeholder>
     * @return a CustomItem look & feel implementation
     */
    CustomItemLF getCustomItemLF(CustomItem item) {
        return new CustomItemLFImpl(item);
    }  

    /**
     * Returns a DateField look & feel implementation.
     *
     * @param item - <placeholder>
     * @return a DateField look & feel implementation
     */  
    DateFieldLF getDateFieldLF(DateField item) {
        return new DateFieldLFImpl(item);
    }
    
    /**
     * Returns a Gauge look & feel implementation.
     *
     * @param item - <placeholder>
     * @return a Gauge look & feel implementation
     */
    GaugeLF getGaugeLF(Gauge item) {
        return new GaugeLFImpl(item);
    }

    /**
     * Returns an ImageItem look & feel implementation.
     *
     * @param item - <placeholder>
     * @return an ImageItem look & feel implementation
     */
    ImageItemLF getImageItemLF(ImageItem item) {
        return new ImageItemLFImpl(item);
    }

    /**
     * Returns a StringItem look & feel implementation.
     *
     * @param item - <placeholder>
     * @return a StringItem look & feel implementation
     */
    StringItemLF getStringItemLF(StringItem item) {
        return new StringItemLFImpl(item);
    }

    /**
     * Returns a Spacer look & feel implementation.
     *
     * @param spacer <placeholder>
     * @return a Spacer look & feel implementation
     */
    SpacerLF getSpacerLF(Spacer spacer) {
        return new SpacerLFImpl(spacer);
    }

    /**
     * Returns a TextField look & feel implementation.
     *
     * @param tf - <placeholder>
     * @return a TextField look & feel implementation
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
        return new TextBoxLFImpl(tf);
    }

    /**
     * Returns a Ticker look and feel implementation.
     *
     * @param ticker - <placeholder>
     * @return a Ticker look and feel implementation.
     */
    TickerLF getTickerLF(Ticker ticker) {
        return new TickerLFImpl(ticker);
    }
}
