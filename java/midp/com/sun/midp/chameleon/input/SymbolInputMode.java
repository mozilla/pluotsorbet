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
import javax.microedition.lcdui.TextField;

import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;
import com.sun.midp.lcdui.EventConstants;

/**
 * An InputMode instance which allows to select the particular symbol
 * from the table of predefined symbols.
 */
public class SymbolInputMode implements InputMode {

    /** A holder for the keyCode which was last processed */
    protected int lastKey = KEYCODE_NONE;

    /** The InputModeMediator for the current input session */
    protected InputModeMediator mediator;

    /** Symbol table */
    protected final SymbolTable st = new SymbolTable();

    /**
     * The number of symbol_table is designed to be 29 for 5x6 matrix,
     * starting the selection at 12.  But if you have more, the total
     * must be under 36 for 6x6 matrix.
     */
    protected final static char[] symbolTableChars;

    static {
        symbolTableChars =
            Resource.getString(ResourceConstants.LCDUI_TF_SYMBOLS_TABLE).
            toCharArray();
    }


    /**
     * This method is called to determine if this InputMode supports
     * the given text input constraints. The semantics of the constraints
     * value are defined in the javax.microedition.lcdui.TextField API.
     * If this InputMode returns false, this InputMode must not be used
     * to process key input for the selected text component.
     *
     * @param constraints text input constraints. The semantics of the
     * constraints value are defined in the TextField API.
     *
     * @return true if this InputMode supports the given text component
     *         constraints, as defined in the MIDP TextField API
     */
    public boolean supportsConstraints(int constraints) {
        switch (constraints & TextField.CONSTRAINT_MASK) {
            case TextField.NUMERIC:
            case TextField.DECIMAL:
            case TextField.PHONENUMBER:
                return false;
            default:
                return true;
        }
    }

    /**
     * Returns the display name which will represent this InputMode to
     * the user, such as in a selection list or the softbutton bar.
     *
     * @return the locale-appropriate name to represent this InputMode
     *         to the user
     */
    public String getName() {
        return Resource.getString(ResourceConstants.LCDUI_TF_SYMBOLS);
    }

    /**
     * Returns the command name which will represent this InputMode to
     * the user
     *
     * @return the locale-appropriate command name to represent this InputMode
     *         to the user
     */
    public String getCommandName() {
        return getName();
    }

    /**
     * This method will be called before any input keys are passed
     * to this InputMode to allow the InputMode to perform any needed
     * initialization. A reference to the InputModeMediator which is
     * currently managing the relationship between this InputMode and
     * the input session is passed in. This reference can be used
     * by this InputMode to commit text input as well as end the input
     * session with this InputMode. The reference is only valid until
     * this InputMode's endInput() method is called.
     *
     * @param constraints text input constraints. The semantics of the
     * constraints value are defined in the TextField API.
     *
     * @param mediator the InputModeMediator which is negotiating the
     *        relationship between this InputMode and the input session
     *
     * @param inputSubset current input subset
     */
    public void beginInput(InputModeMediator mediator, String inputSubset,
                           int constraints) {
        validateState(false);
        this.mediator = mediator;
    }

    /**
     * Symbol Input mode is represented by Symbol table implemented as canvas
     * @return SymbolTable
     */
    public Displayable getDisplayable() {
        return st;
    }

    /**
     * Process the given key code as input.
     *
     * This method will return true if the key was processed successfully,
     * false otherwise.
     *
     * @param keyCode the keycode of the key which was input
     * @param longPress return true if it's long key press otherwise false
     * @return true if the key was processed by this InputMode, false
     *         otherwise.
     */
    public int processKey(int keyCode, boolean longPress) {
        return KEYCODE_NONE;
    }

    /**
     * return the pending char
     * used to bypass the asynchronous commit mechanism
     * e.g. to immediately commit a char before moving the cursor
     * @return return the pending char
     */
    public char getPendingChar() {
        return lastKey == KEYCODE_NONE ? 0 : (char)lastKey;
    }

    /**
     * Return the next possible match for the key input processed thus
     * far by this InputMode. A call to this method should be preceeded
     * by a check of hasMoreMatches(). If the InputMode has more available
     * matches for the given input, this method will return them one by one.
     *
     * @return a String representing the next available match to the key
     *         input thus far
     */
    public String getNextMatch() {
        return null;
    }

    /**
     * True, if after processing a key, there is more than one possible
     * match to the input. If this method returns true, the getNextMatch()
     * method can be called to return the value.
     *
     * @return true if after processing a key, there is more than the one
     *         possible match to the given input
     */
    public boolean hasMoreMatches() {
        return false;
    }

    /**
     * Gets the possible string matches
     *
     * @return returns the set of options.
     */
    public String[] getMatchList() {
        return new String[0];
    }


    /**
     * Mark the end of this InputMode's processing. The only possible call
     * to this InputMode after a call to endInput() is a call to beginInput()
     * to begin a new input session.
     */
    public void endInput() {
        validateState(true);
        mediator = null;
    }

    /**
     * Returns true if input mode is using its own displayable, false ifinput
     * mode does not require the speial displayable for its representation.
     * For Symbol mode is represented by Symbol table canvas, so it returns true
     * @return true if input mode is using its own displayable, otherwise false
     */
    public boolean hasDisplayable() {
        return true;
    }

    /**
     * This method will validate the state of this InputMode. If this
     * is a check for an "active" operation, the TextInputMediator must
     * be non-null or else this method will throw an IllegalStateException.
     * If this is a check for an "inactive" operation, then the
     * TextInputMediator should be null.
     * @param activeOperation true if any operation is active otherwise false.
     */
    protected void validateState(boolean activeOperation) {
        if (activeOperation && mediator == null) {
            throw new IllegalStateException(
            "Illegal operation on an input session already in progress");
        } else if (!activeOperation && mediator != null) {
            throw new IllegalStateException(
            "Illegal operation on an input session which is not in progress");
        }
    }

    /**
     * A special Canvas to display a symbol table.
     */
    protected class SymbolTable extends Canvas implements CommandListener {
        /** The margin size */
        private static final int MARGIN = 1;

        /** The margin size */
        private static final int DMARGIN = 2;

        /** Select commant to accept the symbol */
        private final Command okCmd = new Command(
             Resource.getString(ResourceConstants.OK), Command.OK, 1);

        /** Back commant to reject the symbol */
        private final Command backCmd = new Command(
             Resource.getString(ResourceConstants.BACK), Command.BACK, 0);

        /** Cell size */
        private int cellSize;

        /** Height margin */
        private int hmargin;

        /** Width margin */
        private int wmargin;

        /** Margin for the cursor */
        private int margin = 1;

        /** Window x position */
        private int wx;

        /** Window y position */
        private int wy;

        /** Window width */
        private int ww;

        /** Window height */
        private int wh;

        /** Number of columns */
        private int cols;

        /** Number of rows */
        private int rows;

        /** Current cursor position */
        private int pos;

        /** New cursor position */
        private int newpos;

        /** Font */
        private Font font;

        /** x cursor coordinate */
        int x_cursor_coord;

        /** y cursor coordinate */
        int y_cursor_coord;

        /** default location to start the cursor */
        protected int defaultSymbolCursorPos = 14;

        /** Default constructor for SymbolTable */
        protected SymbolTable() {
            init();
        }

        /**
         * Symbol accept is run in separate thread
         */
        private class Accept implements Runnable {
            /**
             * run for accept
             */
            public void run() {
                lastKey = symbolTableChars[pos];
                completeInputMode(true);
            }
        }

        /**
         * Symbol reject is run in separate thread
         */
        private class Reject implements Runnable {
            /**
             * run for reject
             */
            public void run() {
                completeInputMode(false);
            }
        }

        /**
         * Instance of accept object
         */
        private Runnable accept = new Accept();

        /**
         * Instance of reject object
         */
        private Runnable reject = new Reject();

        /**
         * Initialize the symbol table
         */
        void init() {
            calculateProportions();
            addCommand(backCmd);
            addCommand(okCmd);
            setCommandListener(this);
        }

        /**
         *   Calculate numbers of colomns and rows of symbol table.
         *   Calculate cell size of symbol table.
         */
        private void calculateProportions() {

            { // choose the table sizes
                rows = cols = (int) Math.sqrt(symbolTableChars.length);
                if (rows*cols < symbolTableChars.length) {
                    rows++;
                }
                if (rows*cols < symbolTableChars.length) {
                    cols++;
                }
            }

            int w = getWidth() / cols;
            int h = getHeight() / rows;

            cellSize = (w > h) ? h : w;

            int cw = 0, ch = 0;

            int temp_cols = getWidth() / cellSize;
            if ( temp_cols > cols) {
                cols = temp_cols;
                rows = (int)Math.ceil((double)symbolTableChars.length / cols);
            }

            int[] fs = { Font.SIZE_LARGE, Font.SIZE_MEDIUM, Font.SIZE_SMALL };
            for (int i = 0; i < fs.length; i++) {
                font = Font.getFont(Font.FACE_SYSTEM,
                                    Font.STYLE_BOLD,
                                    fs[i]);
                cw = font.charWidth('M');
                ch = font.getHeight();
                if (cw <= cellSize && ch <= cellSize) {
                    break;
                }
            }

            ww = cols * cellSize;
            wh = rows * cellSize;

            hmargin = (cellSize - ch) / 2;
            wmargin = cellSize / 2;

            wx = (getWidth() - ww) / 2;
            wy = (getHeight() - wh) / 2;
        }

        /**
         * Notify this symbol table its being shown on the screen.
         * Overrides Canvas.showNotify.
         */
        protected void showNotify() {
            pos = newpos = defaultSymbolCursorPos;
        }

        /**
         * Called when the drawable area of the <code>Canvas</code> has
         * been changed.  This
         * method has augmented semantics compared to {@link
         * Displayable#sizeChanged(int,int) Displayable.sizeChanged}.
         *
         * @param w the new width in pixels of the drawable area of the
         * <code>Canvas</code>
         * @param h the new height in pixels of the drawable area of
         * the <code>Canvas</code>
         */
        protected void sizeChanged(int w, int h) {
            calculateProportions();
        }

        /**
         * Paint this symbol table.
         * Overrides Canvas.paint.
         *
         * @param g The Graphics object to paint to
         */
        protected void paint(Graphics g) {

            paintPanel(g);
        }

        /**
         * Paint the symbol table panel
         *
         * @param g The Graphics object to paint to
         */
        void paintPanel(Graphics g) {

            int clipX = g.getClipX();
            int clipY = g.getClipY();
            int clipW = g.getClipWidth();
            int clipH = g.getClipHeight();

            Font old_font = g.getFont();
            g.setFont(font);

            g.setGrayScale(255);
            g.fillRect(clipX, clipY, clipW, clipH);

            int sr = (clipY - wy) / cellSize;
            if (sr < 0) sr = 0;

            int sc = (clipX - wx) / cellSize;
            if (sc < 0) sc = 0;

            int er = (clipH + clipY + cellSize - wy) / cellSize;
            if (er > rows - 1) er = rows - 1;

            int ec = (clipW + clipX + cellSize - wx) / cellSize;
            if (ec > cols - 1) ec = cols - 1;

            g.setGrayScale(0);
            g.drawRect(wx, wy, ww, wh);

            for (int r = sr; r <= er; r++) {
                for (int c = sc; c <= ec; c++) {
                    int i = r * cols + c;
                    if (i >= symbolTableChars.length) {
                        break;
                    }
                    if (i == newpos) {
                        g.setGrayScale(0);
                        setCursorCoord(newpos);
                        g.fillRect(x_cursor_coord, y_cursor_coord,
                                cellSize - margin, cellSize - margin);
                        drawChar(g, symbolTableChars[i], r, c, true);
                        g.setGrayScale(255);
                        pos = newpos;
                    } else {
                        drawChar(g, symbolTableChars[i], r, c, false);
                    }

                }
            }
            g.setFont(old_font);
        }

        /**
         * Draw a character
         *
         * @param g The Graphics object to paint to
         * @param c The character to draw
         * @param row The row the character is in
         * @param col The column the character is in
         * @param reverse A flag to draw the character in inverse
         */
        void drawChar(Graphics g, char c, int row, int col, boolean reverse) {
            int y = wy + row * cellSize + hmargin;
            int x = wx + col * cellSize + wmargin;

            Font old_font = g.getFont();
            g.setFont(font);
            g.setGrayScale(reverse ? 255 : 0);
            g.drawChar(c, x, y, Graphics.HCENTER | Graphics.TOP);
            g.setFont(old_font);
        }

        /**
         * Move focus to the symbol
         * @param x x coordinate of the pointer
         * @param y y coordinate of the pointer
         */
        protected void pointerPressed(int x, int y) {
            int pressedId;
            pressedId = getIdAtPointerPosition(x, y);
            // move focus to the clicked position
            if (pressedId < symbolTableChars.length && pressedId >= 0) {
                newpos = pressedId;
                repaint();
            }
        }

        /**
         * Select the symbol by one click
         * @param x x coordinate of the pointer
         * @param y y coordinate of the pointer
         */
        protected void pointerReleased(int x, int y) {
            int pressedId;
            // select character
            pressedId = getIdAtPointerPosition(x, y);
            if (pos == pressedId) {
                new Thread(accept).start();
            }
        }

        /**
         * Helper function to determine the cell index at the x,y position
         *
         * @param x, y  pointer coordinates in this popup
         * layer's space (0,0 means left-top corner)
         *
         * @return cell id at the pointer position
         */
        private int getIdAtPointerPosition(int x, int y) {
            int ret = -1;

            int col = (x - wx) / cellSize;
            int row = (y - wy) / cellSize;

            if (col >= 0 && col < cols && row >= 0 && row < rows) {
                ret = cols * row + col;
            }
            return ret;
        }

        /**
         * Handle a key press event on this symbol table.
         * Overrides Canvas.keyPressed.
         *
         * @param keyCode The key that was pressed
         */
        protected void keyPressed(int keyCode) {

            validateState(true);
            if (mediator != null && mediator.isClearKey(keyCode)) {
                new Thread(reject).start();
            } else {
                switch (getGameAction(keyCode)) {
                case Canvas.RIGHT:
                    if ((pos + 1) < symbolTableChars.length) {
                        newpos = pos + 1;
                        setCursorCoord(pos);
                        repaint(x_cursor_coord,y_cursor_coord,cellSize - margin,cellSize - margin);
                        setCursorCoord(newpos);
                        repaint(x_cursor_coord,y_cursor_coord,cellSize - margin,cellSize - margin);
                    }
                    break;
                case Canvas.LEFT:
                    if (pos > 0) {
                        newpos = pos - 1;
                        setCursorCoord(pos);
                        repaint(x_cursor_coord,y_cursor_coord,cellSize - margin,cellSize - margin);
                        setCursorCoord(newpos);
                        repaint(x_cursor_coord,y_cursor_coord,cellSize - margin,cellSize - margin);
                    }
                    break;
                case Canvas.UP: {
                    int p = pos - cols;
                    if (p >= 0) {
                        newpos = p;
                        setCursorCoord(pos);
                        repaint(x_cursor_coord,y_cursor_coord,cellSize - margin,cellSize - margin);
                        setCursorCoord(newpos);
                        repaint(x_cursor_coord,y_cursor_coord,cellSize - margin,cellSize - margin);
                    }
                    break;
                }
                case Canvas.DOWN: {
                    int p = pos + cols;
                    if (p < symbolTableChars.length) {
                        newpos = p;
                        setCursorCoord(pos);
                        repaint(x_cursor_coord,y_cursor_coord,cellSize - margin,cellSize - margin);
                        setCursorCoord(newpos);
                        repaint(x_cursor_coord,y_cursor_coord,cellSize - margin,cellSize - margin);
                    }
                    break;
                }
                case Canvas.FIRE:
                    new Thread(accept).start();
                    break;
                }
            }
         }

        /**
         * Set bounds of repaint area
         * @param curr_pos - position of cursor
         */
        private void setCursorCoord(int curr_pos) {
            int row = curr_pos / cols;
            int col = curr_pos % cols;
            y_cursor_coord = wy + row * cellSize;
            x_cursor_coord = wx + col * cellSize;
        }

        /**
         * This method is used to immediately commit the given
         * string and then call the TextInputMediator's inputModeCompleted()
         * method
         * @param c command
         * @param d displayable
         */
        public void commandAction(Command c, Displayable d) {
            validateState(true);
            if (c == backCmd) {
                new Thread(reject).start();
            } else if (c == okCmd) {
                new Thread(accept).start();
            }
        }
    }

    /**
     * Complete current input mode
     * @param commit true if the symbol has to be committed otherwise false
     */
    protected void completeInputMode(boolean commit) {
        if (commit) {
            commitPendingChar();
        }
        mediator.inputModeCompleted();
    }

    /**
     * Commit pending char
     * @return true if the char has been committed otherwise false
     */
    protected boolean commitPendingChar() {
        boolean committed = false;
        if (lastKey != KEYCODE_NONE) {
            committed = true;
            mediator.commit(String.valueOf((char)lastKey));
        }
        lastKey = KEYCODE_NONE;
        return committed;
    }

    /**
     * Check if the char is the symbol from the symbol table
     * @param c char
     * @return true if this char exists in the symbol table otherwise false.
     */
    public static boolean isSymbol(char c) {
        for (int i = 0; i < symbolTableChars.length; i++) {
            if (symbolTableChars[i] == c) {
                return true;
            }
        }
        return false;
    }

    /** this mode is not set as default. So the map is initialized by false */
    private static final boolean[][] isMap =
        new boolean[TextInputSession.INPUT_SUBSETS.length + 1]
        [TextInputSession.MAX_CONSTRAINTS];

    /**
     * Returns the map specifying this input mode is proper one for the
     * particular pair of input subset and constraint. The form of the map is
     *
     *                       |ANY|EMAILADDR|NUMERIC|PHONENUMBER|URL|DECIMAL|
     * ---------------------------------------------------------------------
     * IS_FULLWIDTH_DIGITS   |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_FULLWIDTH_LATIN    |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_HALFWIDTH_KATAKANA |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_HANJA              |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_KANJI              |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_LATIN              |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_LATIN_DIGITS       |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_SIMPLIFIED_HANZI   |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * IS_TRADITIONAL_HANZI  |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * MIDP_UPPERCASE_LATIN  |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * MIDP_LOWERCASE_LATIN  |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     * NULL                  |t|f|   t|f   |  t|f  |    t|f    |t|f|  t|f  |
     *
     * @return input subset x constraint map
     */
    public boolean[][] getIsConstraintsMap() {
        return isMap;
    }
}

