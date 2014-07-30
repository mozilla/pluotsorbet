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
import javax.microedition.lcdui.*;
import com.sun.midp.chameleon.skins.*;
import com.sun.midp.util.ResourceHandler;
import com.sun.midp.configurator.Constants;
import com.sun.midp.lcdui.EventConstants;
import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
 * A special popup layer which implements a system
 * menu. The system menu is a collection of commands,
 * both screen (Back, Exit, etc) and item specific
 * commands. 
 */
public class MenuLayer extends ScrollablePopupLayer {
    
    /** The list of Commands to display in the menu. */
    protected Command[] menuCmds;
    
    /** The currently selected index in the menu. */
    protected int selI;
    
    /** 
     * The number of commands which have been scrolled off the
     * top of the menu, normally 0 unless there are more commands
     * than can fit on the menu.
     */
    protected int scrollIndex;
    
    /** 
     * The SoftButtonLayer maintains the overall set of
     * commands and their associated listeners.
     */
    protected SoftButtonLayer btnLayer;
    
    /**
     * A cascading menu which holds commands for a SubMenuCommand.
     */
    protected CascadeMenuLayer cascadeMenu;
    
    /**
     * A flag indicating if a cascading menu is visible.
     */
    protected boolean cascadeMenuUp;
    
    /** pointer pressed outside of the menuLayer's bounds */
    private final static int PRESS_OUT_OF_BOUNDS = -1; 
    
    /** pointer pressed on the menuLayer's title area */
    private final static int PRESS_ON_TITLE = -2; 
    
    /** variable used in pointerInput handling */
    private int itemIndexWhenPressed = PRESS_OUT_OF_BOUNDS; 
    
    /**
     * Construct a new system menu layer.
     */
    public MenuLayer() {
        super();
        setBackground(MenuSkin.IMAGE_BG, MenuSkin.COLOR_BG);
        cascadeMenu = new CascadeMenuLayer();
    }

    /**
     * Called typically by the SoftButtonLayer to establish the
     * set of Commands to display on this system menu. This method will
     * create a new copy of the array of commands passed in.
     *
     * @param cmdList the set of commands to display in the menu
     *                (the commands should already be sorted by priority)
     * @param btnLayer the SoftButtonLayer to notify of any command
     *                 selections
     * @param index the command index has to be highlighted. If index exceeds
     * the number of commands the 1st command has to be highlighted.
     */
    public void setMenuCommands(Command[] cmdList, SoftButtonLayer btnLayer,
                                int index) {
        if (cmdList.length == 1 && cmdList[0] instanceof SubMenuCommand) {
            cmdList = ((SubMenuCommand)cmdList[0]).getSubCommands();
        }
        this.menuCmds = new Command[cmdList.length];
        System.arraycopy(cmdList, 0, this.menuCmds, 0, cmdList.length);
        // If we have fewer commands than fill up the menu,
        // we shorten the menu's height
        if (menuCmds.length < MenuSkin.MAX_ITEMS) {
            bounds[H] = MenuSkin.HEIGHT - 
                ((MenuSkin.MAX_ITEMS - menuCmds.length) 
                    * MenuSkin.ITEM_HEIGHT);
        } else {
            bounds[H] = MenuSkin.HEIGHT;
        }
        alignMenu();           
        requestRepaint();

        this.btnLayer = btnLayer;
        
        selI = index < cmdList.length ? index : 0;
    }
    
    /**
     * Updates the scroll indicator.
     */
    public void updateScrollIndicator() {
    	if (scrollInd != null) {
            if (menuCmds.length > MenuSkin.MAX_ITEMS) {
                scrollInd.setVerticalScroll(
                  (scrollIndex * 100) / (menuCmds.length - MenuSkin.MAX_ITEMS),
                  (MenuSkin.MAX_ITEMS * 100) / menuCmds.length);
            } else {
                scrollInd.setVerticalScroll(0, 100);
            }
            super.updateScrollIndicator();
        }
    }
    
    /**
     * Helper function to determine the itemIndex at the x,y position
     *
     * @param x,y   pointer coordinates in menuLayer's space (0,0 means left-top
     *      corner) both value can be negative as menuLayer handles the pointer
     *      event outside its bounds
     * @return menuItem's index since 0, or PRESS_OUT_OF_BOUNDS, PRESS_ON_TITLE
     *
     */
    private int itemIndexAtPointerPosition(int x, int y) {
        int ret;
        if (!containsPoint(x + bounds[X], y + bounds[Y])) {
            ret = PRESS_OUT_OF_BOUNDS; 
        } else if (y < MenuSkin.ITEM_TOPOFFSET) {
            ret = PRESS_ON_TITLE;
        } else {
            ret = (y - MenuSkin.ITEM_TOPOFFSET) / MenuSkin.ITEM_HEIGHT;
        }
        return ret;
    }

    /**
     * Handle input from a pen tap. Parameters describe
     * the type of pen event and the x,y location in the
     * layer at which the event occurred. Important : the
     * x,y location of the pen tap will already be translated
     * into the coordinate space of the layer.
     *
     * @param type the type of pen event
     * @param x the x coordinate of the event
     * @param y the y coordinate of the event
     */
    public boolean pointerInput(int type, int x, int y) {
        switch (type) {
        case EventConstants.PRESSED:
            itemIndexWhenPressed =  itemIndexAtPointerPosition(x, y);

            if (itemIndexWhenPressed != PRESS_OUT_OF_BOUNDS && itemIndexWhenPressed >= 0) {
                // press on valid menu item
                selI = scrollIndex + itemIndexWhenPressed;
                requestRepaint();
                // if (btnLayer != null) btnLayer.serviceRepaints();
            }
            break;
        case EventConstants.RELEASED:
            int itemIndexWhenReleased = itemIndexAtPointerPosition(x, y);

            // dismiss the menu layer if the user pressed outside the menu
            if (itemIndexWhenReleased == PRESS_OUT_OF_BOUNDS) {
                if (btnLayer != null) {
                    btnLayer.dismissMenu();
                }
                break;
            }
            
            if (itemIndexWhenReleased == itemIndexWhenPressed) {
                if (itemIndexWhenPressed >= 0) {
                    if (btnLayer != null && !showSubMenu(selI)) {
                        if (selI >= 0 && selI < menuCmds.length) {
                            btnLayer.commandSelected(menuCmds[selI]);
                        }
                    }
                }
            }

            // remember to reset the variables
            itemIndexWhenPressed = PRESS_OUT_OF_BOUNDS;
            break;
        }
        // return true always as menuLayer will capture all of the pointer inputs
        return true;  
    }

    /**
     * Handles key input from a keypad. Parameters describe
     * the type of key event and the platform-specific
     * code for the key. (Codes are translated using the
     * lcdui.Canvas)
     *
     * @param type the type of key event
     * @param keyCode the numeric code assigned to the key
     * @return true if the input has been processed by this
     * method, otherwise false (soft menu keys)
     */
    public boolean keyInput(int type, int keyCode) {
        // The system menu will absorb all key presses except
        // for the soft menu keys - that is, it will always
        // return 'true' indicating it has handled the key
        // event except for the soft button keys for which it
        // returns 'false'
    	if (keyCode == EventConstants.SOFT_BUTTON1 || 
            keyCode == EventConstants.SOFT_BUTTON2) {
            return false;
        }
        
        if (type != EventConstants.PRESSED && type != EventConstants.REPEATED) {
            return true;
        }
        
        if (keyCode == Constants.KEYCODE_UP) {
            if (selI > 0) {
                selI--;
                if (selI < scrollIndex && scrollIndex > 0) {
                    scrollIndex--;
                }
            } else {
            	selI = menuCmds.length - 1; 
            	scrollIndex = menuCmds.length - MenuSkin.MAX_ITEMS;
            	scrollIndex = (scrollIndex > 0) ? scrollIndex : 0;	
            }
            updateScrollIndicator();
            requestRepaint();
        } else if (keyCode == Constants.KEYCODE_DOWN) {
            if (selI < (menuCmds.length - 1)) {
                selI++;
                if (selI >= scrollIndex + MenuSkin.MAX_ITEMS &&
                    scrollIndex < (menuCmds.length - MenuSkin.MAX_ITEMS)) {
                    scrollIndex++;
                } 
            } else {
            	selI = 0;
            	scrollIndex = 0; 
            }
            updateScrollIndicator();
            requestRepaint();
        } else if (keyCode == Constants.KEYCODE_LEFT) {
            // IMPL_NOTE : Need to add support for a "right popping"
            // sub menu if the system menu is placed on the left
            // side of the screen instead of the right
            if (btnLayer != null) { 
                showSubMenu(selI);
            }
        } else if (keyCode == Constants.KEYCODE_SELECT) {
            if (btnLayer != null && !showSubMenu(selI)) {
                btnLayer.commandSelected(menuCmds[selI]);
            }
        } else {
            int max = 0;
            switch (keyCode) {
                case Canvas.KEY_NUM1:
                    max = 1;
                    break;
                case Canvas.KEY_NUM2:
                    max = 2;
                    break;
                case Canvas.KEY_NUM3:
                    max = 3;
                    break;
                case Canvas.KEY_NUM4:
                    max = 4;
                    break;
                case Canvas.KEY_NUM5:
                    max = 5;
                    break;
                case Canvas.KEY_NUM6:
                    max = 6;
                    break;
                case Canvas.KEY_NUM7:
                    max = 7;
                    break;
                case Canvas.KEY_NUM8:
                    max = 8;
                    break;
                case Canvas.KEY_NUM9:
                    max = 9;
                    break;
            }
            if (max > 0 && menuCmds.length >= max) {
                if (btnLayer != null && !showSubMenu(max - 1)) {
                    btnLayer.commandSelected(menuCmds[max - 1]);
                }
            }
        }
        return true;
    }

    /**
     * Cleans up the display when the cascaded menu is dismissed.
     * Removes the layer with the menu and requests the display to be
     * repainted.
     */
    public void dismissCascadeMenu() {
        if (owner != null && cascadeMenuUp) {
            cascadeMenuUp = false;
            cascadeMenu.dismiss();

            setScrollInd(ScrollIndLayer.getInstance(ScrollIndSkin.MODE));

            owner.removeLayer(cascadeMenu);
            requestRepaint();
        }
    }

    /**
     * Cleans up the display when the cascaded menu is dismissed.
     * Removes the layer with the menu and requests the display to be
     * repainted.
     */
    public void dismiss() {
        dismissCascadeMenu();
        selI = scrollIndex = 0;
    }

    /**
     * Notifies listener that a command has been selected.
     * Dismisses the cascaded menu and the button layer.
     * @param cmd the command that was selected
     */    
    public void subCommandSelected(Command cmd) {
	Command c = menuCmds[selI];
        if (c instanceof SubMenuCommand) {
            btnLayer.dismissMenu();
            ((SubMenuCommand)c).notifyListener(cmd);
        }
    }
    
    /**
     * Initializes the menu parameters.
     */
    protected void initialize() {
        super.initialize();
        bounds[X] = 0; // set in alignMenu()
        bounds[Y] = 0; // set in alignMenu()
        bounds[W] = MenuSkin.WIDTH;
        bounds[H] = MenuSkin.HEIGHT;
    }
       
    /**
     * Aligns the menu to the current screen.
     */ 
    protected void alignMenu() {
	if (owner == null) 
	    return;
        bounds[W] = MenuSkin.WIDTH;

        switch (MenuSkin.ALIGN_X) {
            case Graphics.LEFT:
                bounds[X] = 0;
                break;
            case Graphics.HCENTER:
		bounds[X] = (owner.bounds[W] - bounds[W]) / 2;
		break;
            case Graphics.RIGHT:
            default:
		bounds[X] = owner.bounds[W] - bounds[W];
		break;
        }
        switch (MenuSkin.ALIGN_Y) {
            case Graphics.TOP:
                bounds[Y] = 0;
                break;
            case Graphics.VCENTER:
		bounds[Y] = (owner.bounds[H] - SoftButtonSkin.HEIGHT -
				 bounds[H]) / 2;
		break;
            case Graphics.BOTTOM:
            default:
                bounds[Y] = owner.bounds[H] - SoftButtonSkin.HEIGHT -
                    bounds[H];
		break;
        }
        updateBoundsByScrollInd();
    }
    /**
     * Renders the body of the menu.
     * @param g the graphics context to be updated
     */
    protected void paintBody(Graphics g) {        
        if (MenuSkin.TEXT_TITLE != null) {
            // IMPL_NOTE enforce MenuSkin.TITLE_MAXWIDTH based on
            // title value and font, add '...' to titles which
            // are too long to show
            g.setFont(MenuSkin.FONT_TITLE);
            g.setColor(MenuSkin.COLOR_TITLE);
            g.drawString(MenuSkin.TEXT_TITLE,
                         MenuSkin.TITLE_X,
                         MenuSkin.TITLE_Y,
                         Graphics.TOP | MenuSkin.TITLE_ALIGN);
        }
        
        if (menuCmds != null) {
                       
            int y = MenuSkin.ITEM_TOPOFFSET;
            int x = 0;
            Image arrow = null;
            
            for (int cmdIndex = scrollIndex; 
                (cmdIndex < menuCmds.length) 
                    && (cmdIndex - scrollIndex < MenuSkin.MAX_ITEMS);
                cmdIndex++)
            {
                
                if (menuCmds[cmdIndex] instanceof SubMenuCommand) {
                    arrow = MenuSkin.IMAGE_SUBMENU_ARROW;
                    if (cmdIndex == selI && !cascadeMenuUp) {
                        arrow = MenuSkin.IMAGE_SUBMENU_ARROW_HL;
                    }
                    if (arrow != null) {
                        x = arrow.getWidth() + 2;
                    }
                }

                int itemOffset;
                if (cmdIndex == selI && !cascadeMenuUp) {
                    if (MenuSkin.IMAGE_ITEM_SEL_BG != null) {
                        // We want to draw the selected item background

                        CGraphicsUtil.draw3pcsBackground(g, 3,
                            ((selI - scrollIndex) * MenuSkin.ITEM_HEIGHT) +
                                MenuSkin.IMAGE_BG[0].getHeight(),
                            bounds[W] - 3,
                            MenuSkin.IMAGE_ITEM_SEL_BG);
                    } else {
                        if (ScreenSkin.RL_DIRECTION) {
                            itemOffset = bounds[W] - MenuSkin.ITEM_ANCHOR_X + 2 -
                                MenuSkin.FONT_ITEM_SEL.stringWidth(
                                menuCmds[cmdIndex].getLabel()) - 4 - x;
                        } else {
                            itemOffset = MenuSkin.ITEM_ANCHOR_X - 2;
                        }
                        g.setColor(MenuSkin.COLOR_BG_SEL);
                        g.fillRoundRect(itemOffset,
                            ((selI - scrollIndex) * MenuSkin.ITEM_HEIGHT) +
                                MenuSkin.ITEM_TOPOFFSET,
                            MenuSkin.FONT_ITEM_SEL.stringWidth(
                                menuCmds[cmdIndex].getLabel()) + 4 + x,
                            MenuSkin.ITEM_HEIGHT,
                            3, 3);
                    }
                }

                if (cmdIndex < 9) {
                    g.setFont((selI == cmdIndex) ?
                               MenuSkin.FONT_ITEM_SEL :
                               MenuSkin.FONT_ITEM);
                    g.setColor((selI == cmdIndex) ? 
                               MenuSkin.COLOR_INDEX_SEL :
                               MenuSkin.COLOR_INDEX);

                     if (ScreenSkin.RL_DIRECTION) {
                         itemOffset = bounds[W] - MenuSkin.ITEM_INDEX_ANCHOR_X;                                                     
                     } else {
                         itemOffset = MenuSkin.ITEM_INDEX_ANCHOR_X;
                     }

                     g.drawString("" + (cmdIndex + 1), itemOffset,
                                 y, Graphics.TOP | ScreenSkin.TEXT_ORIENT);
                }
                
                g.setFont(MenuSkin.FONT_ITEM);                
                g.setColor((selI == cmdIndex) ? MenuSkin.COLOR_ITEM_SEL :
                           MenuSkin.COLOR_ITEM);

                if (ScreenSkin.RL_DIRECTION) {
                         itemOffset = bounds[W] - MenuSkin.ITEM_ANCHOR_X;
                     } else {
                         itemOffset = MenuSkin.ITEM_ANCHOR_X;
                     }
                if (arrow != null) {
                    g.drawImage(arrow, itemOffset, y + 2,
                                Graphics.TOP | ScreenSkin.TEXT_ORIENT);
                    arrow = null;
                }
                if (ScreenSkin.RL_DIRECTION) {
                         itemOffset = bounds[W] - MenuSkin.ITEM_ANCHOR_X - x;                                                     
                     } else {
                         itemOffset = MenuSkin.ITEM_ANCHOR_X;
                     }
                g.drawString(menuCmds[cmdIndex].getLabel(),
                             itemOffset,
                             y, Graphics.TOP | ScreenSkin.TEXT_ORIENT);
                            
                x = 0;
                y += MenuSkin.ITEM_HEIGHT;                 
            }
        }       
    }

    /**
     * Shows the sub menu.
     * @param index the offset in the array of menu commands
     * @return true if submenu is shown,  false - otherwise 
     */
    private boolean showSubMenu(int index) {
        boolean ret = false;
        if (menuCmds[index] instanceof SubMenuCommand) {
            SubMenuCommand subMenu = (SubMenuCommand)menuCmds[index];

            owner.addLayer(cascadeMenu);
            cascadeMenu.setMenuCommands(subMenu.getSubCommands(), this);
            cascadeMenu.setAnchorPoint(bounds[X],
                                       bounds[Y] + MenuSkin.ITEM_TOPOFFSET + 
                                       ((index - scrollIndex) *
                                        MenuSkin.ITEM_HEIGHT));
            cascadeMenuUp = true;
            cascadeMenu.requestRepaint();
            setScrollInd(ScrollIndLayer.getInstance(ScrollIndSkin.MODE));
            // IMPL_NOTE: fix layer inrteraction in removeLayer
            btnLayer.requestRepaint();

            selI = index;
            addDirtyRegion();
            ret = true;
        }
        return ret;
    }

    /**
     * Gets index of highlighted command.
     * @return highlighted index
     */
    public int getIndex() {
        return selI;
    }

    /**
     * Update bounds of layer
     * @param layers - current layer can be dependant on this parameter
     */
    public void update(CLayer[] layers) {
        alignMenu();
        if (owner != null && cascadeMenuUp) {
            cascadeMenu.update(layers);
            if (btnLayer != null) {
                showSubMenu(selI);
            }
        }
        super.update(layers);
    }

    /**
     * Scroll content inside of the Menu.
     * @param scrollType scrollType. Scroll type can be one of the following
     * @see ScrollBarLayer.SCROLL_NONE
     * @see ScrollBarLayer.SCROLL_PAGEUP
     * @see ScrollBarLayer.SCROLL_PAGEDOWN
     * @see ScrollBarLayer.SCROLL_LINEUP
     * @see ScrollBarLayer.SCROLL_LINEDOWN or
     * @see ScrollBarLayer.SCROLL_THUMBTRACK
     * @param thumbPosition
     */
    public void scrollContent(int scrollType, int thumbPosition) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI,
                           "MenuLayer.scrollContent scrollType=" + scrollType + 
                           " thumbPosition=" + thumbPosition); 
        }
        // keep old scrollIndex
        int oldScrollIndex = scrollIndex;
        
        switch (scrollType) {
        case ScrollBarLayer.SCROLL_PAGEUP:
            uScrollViewport(Canvas.UP);
            break;
        case ScrollBarLayer.SCROLL_PAGEDOWN:
            uScrollViewport(Canvas.DOWN);
            break;
        case ScrollBarLayer.SCROLL_LINEUP:
            uScrollByLine(Canvas.UP);
            break;
        case ScrollBarLayer.SCROLL_LINEDOWN:
            uScrollByLine(Canvas.DOWN);
            break;
        case ScrollBarLayer.SCROLL_THUMBTRACK:
            uScrollAt(thumbPosition);
            break;
        default:
            break;
        }
        // only if scroll index has been changed do update
        if (oldScrollIndex != scrollIndex && scrollIndex >= 0) {

            // correct selI if required.
            // The selected item always should be on the screen
            if (selI < scrollIndex) {
                selI = scrollIndex;
            } else if (selI >= (scrollIndex + MenuSkin.MAX_ITEMS)) {
                selI = scrollIndex + MenuSkin.MAX_ITEMS - 1;
            }

            if (cascadeMenuUp) {
                dismissCascadeMenu();
            }

            updateScrollIndicator();
            requestRepaint();
        }
    }

    /**
     * Update bounds of layer depend on visability of scroll indicator layer
     */
    public void updateBoundsByScrollInd() {
        bounds[W] = MenuSkin.WIDTH;
        if (owner != null) {
            switch (MenuSkin.ALIGN_X) {
                case Graphics.LEFT:
                    bounds[X] = 0;
                    break;
                case Graphics.HCENTER:
                    bounds[X] = (owner.bounds[W] - bounds[W]) / 2;
                    break;
                case Graphics.RIGHT:
                default:
                    bounds[X] = owner.bounds[W] - bounds[W];
                    break;
            }
        }
        super.updateBoundsByScrollInd();
    }


    /**
     * Perform a line scrolling in the given direction. This method will
     * attempt to scroll the view to show next/previous line.
     *
     * @param dir the direction of the flip, either DOWN or UP
     */
    private void uScrollByLine(int dir) {
        switch(dir) {
        case Canvas.UP:
            if (scrollIndex > 0) {
                scrollIndex--;
            }
            break;
        case Canvas.DOWN:
            if (scrollIndex < (menuCmds.length - MenuSkin.MAX_ITEMS)) {
                scrollIndex++;
            }
            break;
        }
    }
    
    /**
     * Perform a page flip in the given direction. This method will
     * attempt to scroll the view to show as much of the next page
     * as possible. It uses the locations and bounds of the items on
     * the page to best determine a new location - taking into account
     * items which may lie on page boundaries as well as items which
     * may span several pages.
     *
     * @param dir the direction of the flip, either DOWN or UP
     */
    private void uScrollViewport(int dir) {
        switch (dir) {
        case Canvas.UP:
            scrollIndex -= MenuSkin.MAX_ITEMS - 1;
            if (scrollIndex < 0) {
                scrollIndex = 0;
            }
            break;
        case Canvas.DOWN:
            scrollIndex += MenuSkin.MAX_ITEMS - 1;
            if (scrollIndex > menuCmds.length - MenuSkin.MAX_ITEMS) {
                scrollIndex = menuCmds.length - MenuSkin.MAX_ITEMS;
            }
            break;
        }
    }
    
    /**
     * Perform a scrolling at the given position.
     * @param context position
     */
    void uScrollAt(int position) {
        int viewableH =  MenuSkin.ITEM_HEIGHT * menuCmds.length;
        int viewportH =  MenuSkin.ITEM_HEIGHT * MenuSkin.MAX_ITEMS;
        
        int newY = (viewableH - viewportH) * position / 100;
        if (newY < 0) {
            newY = 0;
        } else if (newY > viewableH - viewportH) {
            newY = viewableH - viewportH;
        }
        scrollIndex = newY / MenuSkin.ITEM_HEIGHT;
    }

}

