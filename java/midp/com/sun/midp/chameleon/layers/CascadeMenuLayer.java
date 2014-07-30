/**
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
import com.sun.midp.chameleon.skins.resources.*;
import com.sun.midp.util.ResourceHandler;
import com.sun.midp.configurator.Constants;
import com.sun.midp.lcdui.EventConstants;


public class CascadeMenuLayer extends ScrollablePopupLayer {
    
    /** The list of Commands to display in the menu */
    protected Command[] menuCmds;
    
    /** The currently selected index in the menu */
    protected int selI;
    
    /** 
     * The number of commands which have been scrolled off the
     * top of the menu, normally 0 unless there are more commands
     * than can fit on the menu.
     */
    protected int scrollIndex;
    
    /** 
     * The ManuLayer to which this cascading menu belongs
     */
    protected MenuLayer menuLayer;
    
    /** pointer pressed outside of the menuLayer's bounds */
    private final static int PRESS_OUT_OF_BOUNDS = -1; 
    
    /** variable used in pointerInput handling */
    private int itemIndexWhenPressed = PRESS_OUT_OF_BOUNDS; 

    public CascadeMenuLayer() {
        super();
        setBackground(null, MenuSkin.COLOR_BG);
    }
    
    public void setMenuCommands(Command[] cmdList, MenuLayer menuLayer) 
    {
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
        bounds[H] -= MenuSkin.ITEM_TOPOFFSET;
        alignMenu();           
        requestRepaint();

        this.menuLayer = menuLayer;
        selI = 0;
    }
    
    public void setAnchorPoint(int x, int y) {
        bounds[Y] = y - bounds[H] + MenuSkin.ITEM_HEIGHT + 3;
        if (bounds[Y] < 0) {
            bounds[Y] = 0;
        }
    }
    
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
     * @return menuItem's index since 0, or PRESS_OUT_OF_BOUNDS
     *
     */
    private int itemIndexAtPointerPosition(int x, int y) {
        int ret;
        if (!containsPoint(x + bounds[X], y + bounds[Y])) {
            // IMPL_NOTE: nothing happened. Need to be handled another way 
            ret = PRESS_OUT_OF_BOUNDS; 
        } else {
            ret = y / MenuSkin.ITEM_HEIGHT;
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
        boolean consume = true;
        switch (type) {
        case EventConstants.PRESSED:
            itemIndexWhenPressed =  itemIndexAtPointerPosition(x, y);

            // dismiss the menu layer if the user pressed outside the menu
            if (itemIndexWhenPressed == PRESS_OUT_OF_BOUNDS) {
                if (menuLayer != null) {
                    menuLayer.dismissCascadeMenu();
                }
                consume = false;
            } else if (itemIndexWhenPressed >= 0) { // press on valid menu item
                selI = scrollIndex + itemIndexWhenPressed;
                requestRepaint();
                // if (btnLayer != null) btnLayer.serviceRepaints();
            }
            break;
        case EventConstants.RELEASED:
            int itemIndexWhenReleased = itemIndexAtPointerPosition(x, y);
            
            if (itemIndexWhenReleased == itemIndexWhenPressed) {
                if (itemIndexWhenPressed >= 0) {
                    if (menuLayer != null) {
                        if (selI >= 0 && selI < menuCmds.length) {
                            menuLayer.subCommandSelected(menuCmds[selI]);
                        }
                    }
                }
            }
            
            if (itemIndexWhenReleased == PRESS_OUT_OF_BOUNDS) {
                consume = false;
            }

            // remember to reset the variables
            itemIndexWhenPressed = PRESS_OUT_OF_BOUNDS;
            break;
        }
        // return true always as menuLayer will capture all of the pointer inputs
        return consume;  
    }
   
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
                if (selI >= MenuSkin.MAX_ITEMS &&
                    scrollIndex < (menuCmds.length - MenuSkin.MAX_ITEMS))
                {
                    scrollIndex++;
                }
                
            } else {
                selI = 0;
                scrollIndex = 0;
            }
            updateScrollIndicator();
            requestRepaint();
        } else if (keyCode == Constants.KEYCODE_RIGHT) {
            menuLayer.dismissCascadeMenu();
        } else if (keyCode == Constants.KEYCODE_SELECT) {
            menuLayer.subCommandSelected(menuCmds[selI]);
        } else if (menuCmds.length < 10) {
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
                menuLayer.subCommandSelected(menuCmds[max - 1]);
            }
        }
        return true;
    }

    /**
     * Cleans up the display when the cascaded menu is dismissed.
     * Removes the layer with the menu and requests the display to be
     * repainted.
     */
    public void dismiss() {
        selI = scrollIndex = 0;
        
        if (owner != null &&
            scrollInd != null &&
            scrollInd.scrollable == this) {
            owner.removeLayer(scrollInd);
        }
    }

    public void setScrollInd(ScrollIndLayer scrollInd) {
        if (ScrollIndSkin.MODE != ScrollIndResourcesConstants.MODE_BAR) {
            super.setScrollInd(scrollInd);
        }
    }
    
    protected void initialize() {
        super.initialize();
        bounds[X] = 0; // set in alignMenu()
        bounds[Y] = 0; // set in alignMenu()
        bounds[W] = MenuSkin.WIDTH / 2;
        bounds[H] = MenuSkin.HEIGHT - MenuSkin.ITEM_TOPOFFSET;
    }

    protected void alignMenu() {
        if (owner == null)
            return;
        switch (MenuSkin.ALIGN_X) {
            case Graphics.LEFT:
                bounds[X] = 0;
                break;
            case Graphics.HCENTER:
                bounds[X] = (owner.bounds[W] - bounds[W]) / 2;
                break;
            case Graphics.RIGHT:
            default:
                bounds[X] = owner.bounds[W] - bounds[W] - MenuSkin.WIDTH + 5;
                break;
        }
        if (bounds[X] < 0) {
            bounds[X] = 0;
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
        if (bounds[Y] < 0) {
            bounds[Y] = 0;
        }
        updateBoundsByScrollInd();
    }

    protected void paintBody(Graphics g) {        
        
        if (menuCmds == null) {
            return;
        }
                       
        int y = 0;
        
        for (int cmdIndex = scrollIndex; 
            (cmdIndex < menuCmds.length) 
                && (cmdIndex - scrollIndex < MenuSkin.MAX_ITEMS);
            cmdIndex++)
        {

            int itemOffset = 0;
            if (cmdIndex == selI) {
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
                                menuCmds[cmdIndex].getLabel()) - 4;
                        } else {
                            itemOffset = MenuSkin.ITEM_ANCHOR_X - 2;
                        }
                    g.setColor(MenuSkin.COLOR_BG_SEL);
                    g.fillRoundRect(itemOffset,
                        ((selI - scrollIndex) * MenuSkin.ITEM_HEIGHT),
                        MenuSkin.FONT_ITEM_SEL.stringWidth(
                            menuCmds[cmdIndex].getLabel()) + 4,
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

             g.drawString(menuCmds[cmdIndex].getLabel(), itemOffset,
                         y, Graphics.TOP | ScreenSkin.TEXT_ORIENT);
                         
            y += MenuSkin.ITEM_HEIGHT;                 
        }
        g.setColor(0);
        g.drawRect(0, 0, bounds[W] - 1, bounds[H] - 1);
    }

    /**
     * Update bounds of layer
     * @param layers - current layer can be dependant on this parameter
     */
    public void update(CLayer[] layers) {
        alignMenu();
    }

    /**
     * Update bounds of layer depend on visability of scroll indicator layer
     */
    public void updateBoundsByScrollInd() {
        bounds[W] = MenuSkin.WIDTH / 2;
        super.updateBoundsByScrollInd();
    }


}

