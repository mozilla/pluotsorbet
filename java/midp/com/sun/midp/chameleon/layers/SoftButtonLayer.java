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

package com.sun.midp.chameleon.layers;

import com.sun.midp.chameleon.*;
import com.sun.midp.lcdui.Text;

import javax.microedition.lcdui.*;

import com.sun.midp.chameleon.skins.SoftButtonSkin;
import com.sun.midp.chameleon.skins.ScrollIndSkin;
import com.sun.midp.chameleon.skins.resources.MenuResources;

// EventConstants defines some constant values, such as
// key press, release, soft button codes, etc.
import com.sun.midp.lcdui.EventConstants;
import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

/**
 * Soft button layer.
 */
public class SoftButtonLayer extends CLayer implements CommandListener {

    /**
     * Labels for each of the softbuttons.
     */
    protected String[] labels;

    /**
     * A cached copy of the set of screen commands sent from
     * the Display.
     */
    protected Command[] scrCmds;
    /**
     * The CommandListener to notify for any screen commands executed.
     */
    protected CommandListener scrListener;
    /**
     * A cached copy of the set of item commands sent from
     * the Display.
     */
    protected Command[] itmCmds;
    /**
     * The ItemCommandListener to notify for any item commands executed.
     */
    protected ItemCommandListener itemListener;

    /**
     * The command associated with soft button #1.
     * null if there is no command associated with button #1.
     */
    protected Command soft1;

    /**
     * The set of commands associated with soft button #2.
     * null if there are no commands associated with button #2.
     * If there is only one element in this array, there is no need
     * for a menu and soft button #2 will behave similar to soft
     * button #1 and invoke the listener directly upon button press.
     */
    protected Command[] soft2;

    /**
     * When a sub menu is currently active, this reference is non-null.
     */
    protected SubMenuCommand subMenu;

    private int[] cached_button_anchor_x;

    /**
     * A set of weights assigned to each of the types of Commands.
     * The array is set up to return the weight of the Command type
     * for each index, ie. Command.BACK has a defined value of 2 from
     * the MIDP specification, cmdWieghts[Command.BACK] == 4, that is,
     * there are three other Command types which sort higher than BACK
     * (ITEM, SCREEN, and OK). The weighting is specified in the
     * MIDP Human Interface Specification (Sun Internal).
     */
    static protected final int[] cmdWeights = {
            -1, // 0 is no value
            2, // Screen
            4, // Back
            6, // Cancel
            3, // Ok
            8, // Help
            7, // Stop
            5, // Exit
            1, // Item
            9, // Virtual
    };

    /**
     * A System menu to popup and display when a set of commands
     * needs to be displayed.
     */
    protected MenuLayer menuLayer;

    /**
     * A flag indicating the system menu is up.
     */
    protected boolean menuUP;

    /**
     * A flag indicating the alert is up.
     */
    protected boolean alertUP;

    /**
     * A tunnel to utilize to notify of command invocation.
     */
    protected ChamDisplayTunnel tunnel;

    /**
     * An internal variable defined once to avoid costly heap thrashing.
     */
    protected Command swap;

    /**
     * Internal variables defined once to avoid costly heap thrashing.
     */
    protected int typeX, typeY;

    /**
     * Internal variables for the paint loop.
     */
    protected int buttonx, buttony, buttonw, buttonh;
    
	/**
	* True if user is interacting with the layer
	*/
	private boolean isInteractive ; // = false;

	/**
	* True if using native soft button layer
	*/
	private boolean isNativeLayer  = false;
    
    /**
     * Construct a SoftButtonLayer. The layer's background image and color
     * information is obtained directly from the SoftButtonSkin class,
     * such as background image, tile, and/or background color.
     *
     * @param tunnel    channel for command notifications
     */
    public SoftButtonLayer(ChamDisplayTunnel tunnel) {
        super(SoftButtonSkin.IMAGE_BG, SoftButtonSkin.COLOR_BG);
        super.setSupportsInput(true);
        super.setVisible(true);
        this.tunnel = tunnel;

        isNativeLayer = isNativeSoftButtonLayerSupported0();

        labels = new String[SoftButtonSkin.NUM_BUTTONS];
    }

    static public native boolean isNativeSoftButtonLayerSupported0();
    private native void setNativeSoftButtonLabel0(String label, int softButtonIndex);


    /**
     * Returnes true if user is interacting with the layer,
     * false otherwise.
     *
     * used by MIDPWindow to check if the layer should become
     * visible.
     */
    public boolean isInteractive() {
        return isInteractive;
    }

    /**
     * Assigns new value to isInteractive and signals MIDPWindow.
     */
    private void setInteractive(boolean interactive) {
        if (isInteractive != interactive) {
            isInteractive = interactive;
            if (owner instanceof MIDPWindow) {
                ((MIDPWindow)owner).onSoftButtonInteractive(isInteractive);
            }
        }
    }

    /**
     * Commandlistener interface implementation. Handle softbuton commands.
     *
     * @param c command
     * @param d displayable
     */
    public void commandAction(Command c, Displayable d) {
        dismissMenu();
    }

    /**
     * Dismiss menu layer.
     */
    public void dismissMenu() {
        if (menuUP) {
            menuUP = false;
            menuLayer.dismiss();
            if (owner != null) {
                menuLayer.setScrollInd(null);
                owner.removeLayer(menuLayer);
            }
        }
        toggleMenu(menuUP);
    }

    /**
     * Returns true if system menu is currently up, false otherwise.
     *
     * @return true if system menu is up, false otherwise
     */
    public boolean systemMenuUp() {
        return menuUP;
    }

    /**
     * Called by the system to update the set of commands associated
     * with this button bar and its subsequent system menu.
     *
     * @param itemCmds     an array of item specific commands
     * @param numI         the number of item specific commands
     * @param itemListener the ItemCommandListener to notify if any
     *                     item commands are selected
     * @param screenCmds   an array of screen specific commands
     * @param numS         the number of screen specific commands
     * @param scrListener  the CommandListener to notify if any
     *                     screen commands are selected
     */
    public void updateCommandSet(Command[] itemCmds, int numI,
                                 ItemCommandListener itemListener,
                                 Command[] screenCmds, int numS,
                                 CommandListener scrListener) {
        // Cache the values for later
        this.itmCmds = new Command[numI];

        if (numI > 0) {
            System.arraycopy(itemCmds, 0, this.itmCmds, 0, numI);
        }
        this.itemListener = itemListener;

        this.scrCmds = new Command[numS];
        if (numS > 0) {
            System.arraycopy(screenCmds, 0, this.scrCmds, 0, numS);
        }
        this.scrListener = scrListener;

        // reset the commands
        soft1 = null;
        soft2 = null;

        if (numS > 0) {
            int index = -1;
            int type = -1;

            for (int i = 0; i < numS; i++) {
                if (!(this.scrCmds[i] instanceof SubMenuCommand)) {
                    switch (this.scrCmds[i].getCommandType()) {
                        case Command.BACK:
                            index = i;
                            type = Command.BACK;
                            break;
                        case Command.EXIT:
                            if (type != Command.BACK) {
                                index = i;
                                type = Command.EXIT;
                            }
                            break;
                        case Command.CANCEL:
                            if (type != Command.BACK && type != Command.EXIT) {
                                index = i;
                                type = Command.CANCEL;
                            }
                            break;
                        case Command.STOP:
                            if (type != Command.BACK && type != Command.EXIT &&
                                    type != Command.CANCEL) {
                                index = i;
                                type = Command.STOP;
                            }
                            break;
                        default:
                            break;
                    }
                } // if

                // We can short circuit the search if we find
                // a BACK command, because that is the highest weighted
                // Command for the left soft button
                if (type == Command.BACK) {
                    break;
                }
            } // for

            // If we have a command for the left button, we pop it out
            // of the array and decrement the number in the array - because
            // we'll sort them all together to form the right button (or menu)
            if (type > -1) {
                numS--;
                soft1 = this.scrCmds[index];
                System.arraycopy(screenCmds, index + 1,
                        scrCmds, index, numS - index);
            }
        }

        // Now fill in the 'right' soft button, possibly with a menu
        // of Commands
        switch (numI + numS) {
            case 0:
                soft2 = null;
                break;
            case 1:
                if (soft1 == null) {
                    soft1 = (numI > 0) ? this.itmCmds[0] : this.scrCmds[0];
                } else {
                    soft2 = new Command[1];
                    soft2[0] = (numI > 0) ? this.itmCmds[0] : this.scrCmds[0];
                }
                break;
            default:
                soft2 = new Command[numI + numS];

                if (setCommands(numI, 0, this.itmCmds)) {
                    numI--;
                }

                setCommands(numS, numI, this.scrCmds);

                break;
        }
        setButtonLabels();
    }

    /**
     * Assigns the commands to soft1 and soft2 buttons .
     *
     * @param cmdNum Number of commands
     * @param start  Start index for soft2 commands array
     * @param cmds   Source commands array
     * @return if any commnad has been assign to soft1 returns true,
     *         false otherwise
     */
    private boolean setCommands(int cmdNum, int start, Command[] cmds) {
        int setSoft1 = 0;
        if (cmdNum > 0) {
            sortCommands(cmds, cmdNum);
            for (int i = 0; i < cmdNum; i++) {
                if (soft1 == null && !(cmds[i] instanceof SubMenuCommand)) {
                    soft1 = cmds[i];
                    setSoft1++;
                } else {
                    soft2[start + i - setSoft1] = cmds[i];
                }
            }
            if (setSoft1 > 0) {
                // decrement the soft2 array length
                Command[] soft2temp = new Command[soft2.length - 1];
                System.arraycopy(soft2, 0, soft2temp, 0, soft2temp.length);
                soft2 = soft2temp;
            }
        }
        return setSoft1 > 0;
    }

    /**
     * Handle key input from a keypad. Parameters describe
     * the type of key event and the platform-specific
     * code for the key. (Codes are translated using the
     * lcdui.Canvas)
     *
     * @param type    the type of key event
     * @param keyCode the numeric code assigned to the key
     * @return true if this method processed the input,
     *         otherwise false.
     */
    public boolean keyInput(int type, int keyCode) {
        // SoftButtonLayer absorbs soft button 
        // event only if corresponding soft button is "active".
        // For further clarification please refer to
        // isSoft1Active() and isSoft2Active() methods.

        boolean ret = false;
        if (keyCode == EventConstants.SOFT_BUTTON1) {
            if (isSoft1Active()) {
                if (type == EventConstants.PRESSED) {
                    setInteractive(true);
                    ret = true;
                } else if (type == EventConstants.RELEASED) {
                    soft1();
                    ret = true;
                }
            }
        } else if (keyCode == EventConstants.SOFT_BUTTON2) {
            if (isSoft2Active()) {
                if (type == EventConstants.PRESSED) {
                    setInteractive(true);
                    ret = true;
                } else if (type == EventConstants.RELEASED) {
                    soft2();
                    ret = true;
                }
            }
        }
        return ret;
    }

    /**
     * Handles pointer input events.
     *
     * @param type the event type for this input event
     * @param x    the x coordinate of the input event
     * @param y    the y coordinate of the input event
     * @return true always
     */
    public boolean pointerInput(int type, int x, int y) {

        if (type != EventConstants.PRESSED) {
            return true;
        }

        for (int i = 0; i < SoftButtonSkin.NUM_BUTTONS; i++) {
            switch (SoftButtonSkin.BUTTON_ALIGN_X[i]) {
                case Graphics.LEFT:
                    if (x < cached_button_anchor_x[i] ||
                            (x > cached_button_anchor_x[i] +
                                    SoftButtonSkin.BUTTON_MAX_WIDTH[i])) {
                        continue;
                    }
                    break;
                case Graphics.RIGHT:
                    if (x > cached_button_anchor_x[i] ||
                            (x < cached_button_anchor_x[i] -
                                    SoftButtonSkin.BUTTON_MAX_WIDTH[i])) {
                        continue;
                    }
                    break;
                default:
                    continue;
            }
            if (y >= SoftButtonSkin.BUTTON_ANCHOR_Y[i]
                    && y <= bounds[H]) {
                softPress(i);
            }
        }

        // SoftButtonLayer always swallows any pointer input, as there is no
        // need to forward the press on to any other layer.
        return true;
    }

    /**
     * Selects a command.
     *
     * @param cmd the command selected
     */
    public void commandSelected(Command cmd) {
        if (cmd == null) {
            return;
        }
        dismissMenu();
        processCommand(cmd);
    }

    /**
     * Toggles the alert. Grabs the background and requests a
     * repaint.
     *
     * @param alertUp flag indicating the alaert has expired
     */
    public void toggleAlert(boolean alertUp) {
        alertUP = alertUp;
        setBackground();
        requestRepaint();
    }

    /**
     * Sets the background based on current menu and alert
     * settings.
     */
    private void setBackground() {
        if (menuUP) {
            setBackground(SoftButtonSkin.IMAGE_MU_BG,
                    SoftButtonSkin.COLOR_MU_BG);
        } else if (alertUP) {
            setBackground(SoftButtonSkin.IMAGE_AU_BG,
                    SoftButtonSkin.COLOR_AU_BG);
        } else {
            setBackground(SoftButtonSkin.IMAGE_BG,
                    SoftButtonSkin.COLOR_BG);
        }
    }

    /**
     * Toggles the current menu selection.
     * Grabs the background and requests a repaint.
     *
     * @param menuUp the flag indicating the menu selection
     */
    public void toggleMenu(boolean menuUp) {
        if (owner instanceof MIDPWindow) {
            ((MIDPWindow) owner).paintWash(menuUp);
        }
        setBackground();
        requestRepaint();
    }

    /**
     * Sets the anchor constraints for rendering operation.
     */
    public void setAnchor() {

        int anchor_x;

        if (owner == null)
            return;

        bounds[X] = 0;

        for (int i = 0; i < SoftButtonSkin.NUM_BUTTONS; i++) {
            anchor_x = SoftButtonSkin.BUTTON_ANCHOR_X[i];
            if (anchor_x < 0) {
                anchor_x += owner.bounds[W];
            }
            cached_button_anchor_x[i] = anchor_x;
        }


        bounds[Y] = owner.bounds[H] - SoftButtonSkin.HEIGHT;
        bounds[W] = owner.bounds[W];
        bounds[H] = SoftButtonSkin.HEIGHT;
    }

    /**
     * Switch based on soft button pressed.
     *
     * @param buttonID the button pushed
     */
    protected void softPress(int buttonID) {
        switch (buttonID) {
            case 0:
                soft1();
                break;
            case 1:
                soft2();
                break;
        }
    }

    /**
     * Soft button 1 handler.
     */
    protected void soft1() {
        if (menuUP) {
            dismissMenu();
            subMenu = null;
            requestRepaint();
            setButtonLabels();
            setInteractive(false);
        } else {
            processCommand(soft1);
        }
    }

    /**
     * Initializes the menu from menu resources.
     */
    protected void initMenu() {
        MenuResources.load();
        menuLayer = new MenuLayer();
        Command menuClose = new Command(SoftButtonSkin.TEXT_BACKCMD,
                Command.BACK, 1);
        menuLayer.setCommands(new Command[]{menuClose});
        menuLayer.setCommandListener(this);
    }

    /**
     * Soft button 2 handler.
     */
    protected void soft2() {
        if (soft2 != null) {
            if (soft2.length == 1 &&
                soft2[0] instanceof SubMenuCommand) {
                subMenu = (SubMenuCommand) soft2[0];
            }
            
            if (soft2.length > 1 ||
                subMenu != null) {
                if (menuLayer == null) {
                    initMenu();
                }
                menuLayer.setMenuCommands(soft2, this, 0);
                
                menuUP = true;
                toggleMenu(menuUP);
                
                // Show the menu
                if (owner != null) {
                    owner.addLayer(menuLayer);
		    menuLayer.alignMenu();           
		    menuLayer.requestRepaint();
                    menuLayer.setScrollInd(ScrollIndLayer.getInstance(ScrollIndSkin.MODE));
                }
                
                requestRepaint();

            } else if (soft2.length == 1) {
                // command action
                processCommand(soft2[0]);
            }
        } else {
            setInteractive(false);
        }
    }

    /**
     * Determines if soft button 2 will be processed by the layer.
     * Called by keyInput to determine if the corresponding key event 
     * should be absorbed by SoftButtonLayer.
     *
     * @return true if soft2 command can be processed, false otherwise
     */
    protected boolean isSoft2Active() {
        // when MIDPWindow is not in full screen mode, we absorb
        // all key events reserved for the delivery of commands
        if (!((MIDPWindow)owner).isInFullScreenMode()) {
            return true;
        }

        // for full screen mode we should check if soft key is useful
        if (menuUP) {
            return true;
        } else if (soft2 != null) {
            if (soft2.length == 1 &&
                soft2[0] instanceof SubMenuCommand) {
                return true;
            }
            // search for at least one active command
            for (int i = 0; i < soft2.length; i++) {
                if (isCommandActive(soft2[i])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if soft button 1 will be processed by the layer.
     * Called by keyInput to determine if the corresponding key event 
     * should be absorbed by SoftButtonLayer.
     *
     * @return true if soft1 command can be processed, false otherwise
     */
    protected boolean isSoft1Active() {
        // when MIDPWindow is not in full screen mode, we absorb
        // all key events reserved for the delivery of commands
        if (!((MIDPWindow)owner).isInFullScreenMode()) {
            return true;
        }

        // for full screen mode we should check if soft key is useful
        if (menuUP) {
            return true;
        } else {
            return isCommandActive(soft1);
        }

    }

    /**
     * Determines if it is possible to process the command.
     *
     * @param cmd the command to check
     * @return true if command can be processed, false otherwise
     */
    protected boolean isCommandActive(Command cmd) {

        if (tunnel == null || cmd == null) {
            return false;
        }

        if ((isItemCommand(cmd) && (itemListener != null)) 
            || (scrListener != null)) {
            return true;
        }

        return false;
    }

    /**
     * Processes commands.
     *
     * @param cmd the selected command
     */
    protected void processCommand(Command cmd) {

        setInteractive(false);

        if (tunnel == null || cmd == null) {
            return;
        }

        if (subMenu != null) {
            subMenu.notifyListener(cmd);
            subMenu = null;
        } else if (isItemCommand(cmd)) {
            tunnel.callItemListener(cmd, itemListener);
        } else {
            tunnel.callScreenListener(cmd, scrListener);
        }
    }

    /**
     * Sets the button labels.
     */
    protected void setButtonLabels() {
        // reset all the labels
        for (int i = 0; i < SoftButtonSkin.NUM_BUTTONS; i++) {
            labels[i] = null;
        }

        // Port me : If a port had more than 2 soft buttons, adjust
        // the behavior here for extra buttons
        labels[0] = (soft1 == null) ? null : soft1.getLabel();
        if (soft2 == null) {
            labels[1] = null;
        } else if (soft2.length == 1) {
            labels[1] = soft2[0].getLabel();
        } else {
            labels[1] = SoftButtonSkin.TEXT_MENUCMD;
        }

        if (isNativeLayer) {
            // paint buttons on native layer instead of java's SFBLayer
            setNativeSoftButtonLabel0 (labels[0],0);
            setNativeSoftButtonLabel0 (labels[1],1);
        } else {
            addDirtyRegion();
            requestRepaint();
        }
    }

    /**
     * Rearranges the commands based on weights and priority.
     *
     * @param cmds the commands to be sorted
     * @param num  the number of commands to check
     */
    protected void sortCommands(Command[] cmds, int num) {

        // The number of commands is small, so we use a simple
        // Insertion sort that requires little heap        
        for (int i = 1; i < num; i++) {
            for (int j = i; j > 0; j--) {
               if (compare(cmds[j], cmds[j - 1]) < 0) {
                    swap = cmds[j];
                    cmds[j] = cmds[j - 1];
                    cmds[j - 1] = swap;
                } else break;
            }
        }
    }

    /**
     * Compares two commands.
     *
     * @param a first command for comparison
     * @param b second command for comparison
     * @return 0 if commands are the same; negative
     *         if the first object is lower; positive is first
     *         command is higher.
     */
    protected int compare(Command a, Command b) {
        if (a == null || b == null) {
            return 0;
        }

        typeX = a.getCommandType();
        typeY = b.getCommandType();
        if (typeX != typeY) {
            return cmdWeights[typeX] - cmdWeights[typeY];
        } else {
            return a.getPriority() - b.getPriority();
        }
    }

    /**
     * Checks if the item is a command.
     *
     * @param cmd the item to be checked
     * @return true if the command is found in the
     *         list of item commands
     */
    protected boolean isItemCommand(Command cmd) {
        if (itmCmds.length == 0) {
            return false;
        }

        for (int i = 0; i < itmCmds.length; i++) {
            if (itmCmds[i] == cmd) {
                return true;
            }
        }
        return false;
    }

/**
* Initializes the soft button layer.
*/
    protected void initialize() {
        super.initialize();
        cached_button_anchor_x = new int[SoftButtonSkin.NUM_BUTTONS];
        setAnchor();
    }

    /**
     * Renders the soft button layer.
     *
     * @param g the graphics context to be updated
     */
    protected void paintBody(Graphics g) {

        //paint nothing when using native layer
        if (isNativeLayer) {
            return;
        }

        g.setFont(SoftButtonSkin.FONT);


        for (int i = 0; i < SoftButtonSkin.NUM_BUTTONS; i++) {
            if (labels[i] == null) {
                continue;
            }

            buttonw = SoftButtonSkin.FONT.stringWidth(labels[i]);
            if (buttonw > SoftButtonSkin.BUTTON_MAX_WIDTH[i]) {
                buttonw = SoftButtonSkin.BUTTON_MAX_WIDTH[i];
            }

            switch (SoftButtonSkin.BUTTON_ALIGN_X[i]) {
                case Graphics.HCENTER:
                    buttonx =
                            cached_button_anchor_x[i] - (buttonw / 2);
                    break;
                case Graphics.RIGHT:
                    buttonx = cached_button_anchor_x[i] - buttonw;
                    break;
                case Graphics.LEFT:
                default:
                    buttonx = cached_button_anchor_x[i];
                    break;
            }
            buttony = SoftButtonSkin.BUTTON_ANCHOR_Y[i];

            g.translate(buttonx, buttony);

            Text.drawTruncStringShadowed(g, labels[i], SoftButtonSkin.FONT,
                    SoftButtonSkin.COLOR_FG, SoftButtonSkin.COLOR_FG_SHD,
                    SoftButtonSkin.BUTTON_SHD_ALIGN, buttonw);
            g.translate(-buttonx, -buttony);
        }
    }

    /**
     * Returns the left soft button (one).
     *
     * @return the command that's tied to the left soft button
     */
    public Command getSoftOne() {
        return soft1;
    }

    /**
     * Returns the command array tied to the right soft button (two).
     *
     * @return the command array that's tied to the right soft button
     */
    public Command[] getSoftTwo() {
        return soft2;
    }

    /**
     * Returns true if the point lies in the bounds of commnad layers
     * subset like buttons, menu, submenu 
     * @param x the "x" coordinate of the point
     * @param y the "y" coordinate of the point
     * @return true if the point lies in the bounds of commnad layers
     * subset
     */
    public boolean belongToCmdLayers(int x, int y) {
        return containsPoint(x,y) ||
            (menuLayer != null && 
             (menuLayer.containsPoint(x,y) ||
              (menuLayer.cascadeMenu != null && menuLayer.cascadeMenuUp &&
               menuLayer.cascadeMenu.containsPoint(x,y))
              )
             );
    }
        
    /**
     * Update bounds of layer
     *
     * @param layers - current layer can be dependant on this parameter
     */
    public void update(CLayer[] layers) {
        super.update(layers);
        setAnchor();
        if (null != menuLayer) {
            menuLayer.update(layers);
        }
    }
}

