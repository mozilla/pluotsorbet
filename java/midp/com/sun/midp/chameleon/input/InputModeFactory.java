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
package com.sun.midp.chameleon.input;


public class InputModeFactory {
    public static final int KEYBOARD_INPUT_MODE = 1;
    public static final int NUMERIC_INPUT_MODE = 2;
    public static final int ALPHANUMERIC_INPUT_MODE = 3;
    public static final int PREDICTIVE_TEXT_INPUT_MODE = 4;
    public static final int SYMBOL_INPUT_MODE = 5;
    public static final int VIRTUAL_INPUT_MODE = 6;
    public static final int NATIVE_INPUT_MODE_START = 100;

    public static native int [] getInputModeIds();

    protected static int [] inputModeIds = getInputModeIds();

    public static InputMode createInputMode(int id) {
        if (id < NATIVE_INPUT_MODE_START) {
            InputMode im;
            switch(id) {
                case KEYBOARD_INPUT_MODE: im = new KeyboardInputMode(); break;
                case NUMERIC_INPUT_MODE: im = new NumericInputMode(); break;
                case ALPHANUMERIC_INPUT_MODE: im = new AlphaNumericInputMode(); break;
                case PREDICTIVE_TEXT_INPUT_MODE: im = new PredictiveTextInputMode(); break;
                case SYMBOL_INPUT_MODE: im = new SymbolInputMode(); break;
                case VIRTUAL_INPUT_MODE: im = new VirtualKeyboardInputMode(); break;
                default: throw new IllegalArgumentException("bad java input mode id: "+id);
            }
            return im;
        } else {
            NativeInputMode nim = new NativeInputMode();
            if (0 != nim.initialize(id)) {
                throw new IllegalArgumentException("bad native input mode id: "+id);
            }
            return nim;
        }
    }

    public static InputMode[] createInputModes() {
        final int nModes = inputModeIds.length;
        InputMode[] ims = new InputMode[nModes];
        for (int i=0; i<nModes; i++) {
            ims[i] = createInputMode(inputModeIds[i]);
        }
        return ims;
     }

}
