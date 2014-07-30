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

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;
import com.sun.midp.configurator.Constants;

/**
 * Layout management class for <code>Form</code>.
 * See DisplayableLF.java for naming convention.
 */
class LayoutManager {

    // Required test: multiple/simultaneous forms with different layout

    /**
     * Singleton design pattern. Obtain access using instance() method.
     */
    LayoutManager() {
    	sizingBox = new int[3]; // x,y,width
    }

    /**
     * Do layout.
     * SYNC NOTE: caller must hold LCDUILock around a call to this method
     *
     * @param layoutMode one of <code>FULL_LAYOUT</code> or 
     *                   <code>UPDATE_LAYOUT</code>
     * @param numOfLFs number of elements in the calling form
     * @param itemLFs reference to the items array of the calling form
     * @param inp_viewportWidth width of the screen area available for the form
     * @param inp_viewportHeight height of the screen area available 
     * for the form
     * @param viewable area needed for the content of the form
     */
    void lLayout(int layoutMode, 
                 ItemLFImpl[] itemLFs, 
                 int numOfLFs,
                 int inp_viewportWidth,
                 int inp_viewportHeight,
                 int[] viewable) {

        viewportWidth = inp_viewportWidth;
        viewportHeight = inp_viewportHeight;

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "\n<<<<<<<<<< Doing " +
                           (layoutMode == FULL_LAYOUT ?
                            "FULL_LAYOUT" :
                            "UPDATE_LAYOUT") +
                           "... >>>>>>>>>>");
        }
        
        if (layoutMode == FULL_LAYOUT) {
            // first Layout
            updateBlock(0, 0, true, itemLFs, numOfLFs, viewable); 
        } else { 
            // UPDATE_LAYOUT
            // most of the time only one or two items are updated at a time.
            // here we find the minimum items that needs a re-layout, and
            // calling layout for them
            
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION,
                               LogChannels.LC_HIGHUI_FORM_LAYOUT,
                               "UPDATE_LAYOUT - START");            
            }
            // loop on all the items, and find which item needs an update
            // If more than one Item needs update, they both will update,
            // in their layout order.
                                    
            // * we keep a "moving anchor" to use when we identify
            // an invalid Item. This anchor is always at the beginning 
            // of the row above the current Item checked, or at the 
            // beginning of the row of the current Item, in case there
            // was an explicit line break.

            int anchorIndex = 0; 
            
            // this index is needed to identify new lines to be set as 
            // anchors later.
            int newLineIndex = 0;
            
            
            // find where to start the layout. It should be at the beginning
            // of the line above the invalid item, or of the same line
            // in case the line break is explicit.

            // We loop on all the Items to find the first invalid, while
            // keeping the anchorIndex up do date. When calling "updateBlock",
            // the index will jump directly to the next block, because we laid
            // out all the Items in that block.

            for (int index = 0; index < numOfLFs; index++) {

                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, 
                                   LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                   "\n["+itemLFs[index]+"]" +
                                   "BEFORE: index: " +index +
                                   "\t[" + itemLFs[index].bounds[X] +
                                   "," + itemLFs[index].bounds[Y] +
                                   "," + itemLFs[index].bounds[WIDTH] +
                                   "," + itemLFs[index].bounds[HEIGHT] +
                                   "]\t newLine?" + itemLFs[index].isNewLine +
                                   " lineHeight=" + itemLFs[index].rowHeight +
                                   "\t actualBoundsInvalid[" + 
                                   itemLFs[index].actualBoundsInvalid[X] + "," 
                                   + itemLFs[index].actualBoundsInvalid[Y] +
                                   "," + 
                                   itemLFs[index].actualBoundsInvalid[WIDTH] +
                                   "," + 
                                   itemLFs[index].actualBoundsInvalid[HEIGHT] +
                                   "]\t ** viewable: " + index +
                                   "\t[" + viewportWidth + "," +
                                   viewable[HEIGHT] +"]");
                }
                
                if (itemLFs[index].actualBoundsInvalid[WIDTH] || 
                    itemLFs[index].actualBoundsInvalid[X]) {
                    
                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION, 
                                       LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                       "> WIDTH or X is invalid!"); 
                    }
                    
                    // if width is changed, than we have to do a layout two 
                    // a block of Items. So it covers the height as well call
                    // layout block. The index will jump to the first item on
                    // the next block.
                    
                    // return the last item on the block:
                    index = updateBlock(anchorIndex, index, false,
                                        itemLFs, numOfLFs, viewable); 
                    
                    // set the anchor on the next item, if there is one.
                    // if (i+1) is larger than the length of itemLFs, the for
                    // loop will end anyway so we don't have to check it here.

                    anchorIndex = index + 1;

                } else if (itemLFs[index].actualBoundsInvalid[HEIGHT]) {
                    
                    // item current height
                    int h = itemLFs[index].bounds[HEIGHT];

                    // item preferred height
                    int ph = itemLFs[index].lGetAdornedPreferredHeight(
                                                itemLFs[index].bounds[WIDTH]);

                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION, 
                                       LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                       "> HEIGHT is invalid  from:" + h +
                                       " to:" + ph);
                    }
                    if (h != ph) {
                        itemLFs[index].lSetSize(itemLFs[index].bounds[WIDTH], 
                                               ph);
                        itemLFs[index].rowHeight += (ph-h);

                        // NOTE: We should check whole row height, 
                        // instead of just this item.

                        itemLFs[index].actualBoundsInvalid[HEIGHT] = false;

                        if (numOfLFs > index+1) {

                            itemLFs[index+1].actualBoundsInvalid[Y] = true;
                            
                            // NOTE: We should calculate new LineHeight,
                            // instead of just Item Height

                            updateVertically(index+1, 
                                             itemLFs,
                                             numOfLFs,
                                             viewable);
                        } else {
                            // only need to update the viewable
                            viewable[HEIGHT] += (ph - h);
                        }
                    }
                    
                } else if (itemLFs[index].actualBoundsInvalid[Y]) {
                    
                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION, 
                                       LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                       "> *only* Y is invalid for #" +
                                       index);
                    }

                    updateVertically(index, itemLFs, numOfLFs, viewable);

                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION, 
                                       LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                       "> Y - done");
                    }
                    
                } else {
                    
                    // current item is valid.
                    
                    // check if i can be a new anchor point 
                    // (has an explicit line break before it, 
                    // or after the previous Item).
                    if (itemLFs[index].isNewLine) {
                        
                        if (itemLFs[index].equateNLB() ||
                            ((index > 0) && (itemLFs[index-1].equateNLA()))) {
                            // explicit newline
                            anchorIndex = index;
                            newLineIndex = index;
                        } else {
                            // implicit newline
                            
                            // we can move the anchor to the next line:
                            // set the anchorIndex to be the old newLineIndex
                            // (which is the first item on the row above the
                            // current item).
                            anchorIndex = newLineIndex;
                            
                            // set i as the first in its line
                            newLineIndex = index;
                        }
                        
                    }
                    
                }

                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, 
                                   LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                   "AFTER: index: " + index +
                                   "\t[" + itemLFs[index].bounds[X] +
                                   "," + itemLFs[index].bounds[Y] +
                                   "," + itemLFs[index].bounds[WIDTH] +
                                   "," + itemLFs[index].bounds[HEIGHT] +
                                   "]\t newLine?" + itemLFs[index].isNewLine +
                                   " lineHeight=" + 
                                   itemLFs[index].rowHeight +
                                   "\t actualBoundsInvalid[" + 
                                   itemLFs[index].actualBoundsInvalid[X] +
                                   "," +
                                   itemLFs[index].actualBoundsInvalid[Y] +
                                   "," +
                                   itemLFs[index].actualBoundsInvalid[WIDTH] +
                                   "," +
                                   itemLFs[index].actualBoundsInvalid[HEIGHT] +
                                   "]\t ** viewable: " +index +
                                   "\t[" + viewportWidth + "," + 
                                   viewable[HEIGHT] + "]");
                }                                   
            } // for loop    
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION,
                               LogChannels.LC_HIGHUI_FORM_LAYOUT,
                               "UPDATE_LAYOUT - DONE");
            }
        }

        // correct viewable area if required

        // if there are no items in the form just reset viewable[HEIGHT]
        if (numOfLFs == 0) {
            viewable[HEIGHT] = 0;            
        }
    }

    /**
     * Used both to do a full layout or just update a layout.
     *
     * assumptions: startIndex<=invalidIndex
     * 
     * @param startIndex The index to start the layout. Should start a row.
     * @param invalidIndex The index causing the re-layout, should
     *        be equal or greater than startIndex
     * @param fullLayout if <code>true</code>, does a full layout and ignores
     *                   the rest of the parameters sent to this method.
     * @param itemLFs reference to the items array of the calling form
     * @param numOfLFs number of elements in the calling form
     * @param viewable area needed for the content of the form
     *
     * @return the index of the last <code>Item</code> laid out
     */
    private int updateBlock(int startIndex,
                            int invalidIndex, 
                            boolean fullLayout,
                            ItemLFImpl[] itemLFs, 
                            int numOfLFs,
                            int[] viewable) {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "\n - updateBlock(START="+startIndex
                           +", INVALID="+invalidIndex
                           +", Full Layout="+fullLayout+") {");
        }

        // SYNC NOTE: layout() is always called from within a hold
        // on LCDUILock

        int oldWidth = viewable[WIDTH];
        int oldHeight = viewable[HEIGHT];

        // If we don't have any Items, just return
        if (numOfLFs == 0) {

            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION,
                               LogChannels.LC_HIGHUI_FORM_LAYOUT,
                               " we don't have any Items, just return }");
            }

            return 0;
        }

        int rowStart;

        if (fullLayout) {
            // The index of the first Item in the horizontal row
            rowStart = 0;
        } else {
            rowStart = startIndex;
        }

        // The sizingBox starts out life with the size of the viewport, 
        // but gets whittled down as each Item gets laid out and occupies 
        // space in it. It effectively keeps a running total of what space
        // is available due to the Items which have already been laid out


        // We only allow space for the traversal indicator if we
        // have more than one item - because we only draw the traversal
        // indicator if we have more than one item to traverse to.
        // LF's width is set to the maximum allowable width,
        // while view's height is initialized with initial padding and
        // and grows when new row is added.
        sizingBox[X] = 0;
        sizingBox[Y] = 0;
        sizingBox[WIDTH] = viewportWidth;
        viewable[WIDTH] = viewportWidth;

        if (fullLayout) {
            viewable[HEIGHT] = 0;
        } else if (numOfLFs > 1 && startIndex > 0) {
            sizingBox[Y] = itemLFs[startIndex-1].bounds[Y]
                         + itemLFs[startIndex-1].rowHeight;
                    
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, 
                                LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                "sizingBox[Y]=" + sizingBox[Y]);
            }
        }

        // A running variable which maintains the height of the
        // tallest item on a line
        int lineHeight = 0;
        int pW, pH;

        String locale = System.getProperty("microedition.locale");

        if (locale != null && locale.equals("he-IL")) {
            rl_direction = true;
        } else {
            rl_direction = false;
        }
        int curAlignment = (rl_direction) ? Item.LAYOUT_RIGHT : Item.LAYOUT_LEFT;

        // We loop through the Items starting in startIndex, until we reach
        // the end of the block, and return the index of the next block,
        // or just finishing the for loop if this is the last block.
        for (int index = startIndex; index < numOfLFs; index++) {

            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION,
                               LogChannels.LC_HIGHUI_FORM_LAYOUT,
                               "..\n\tFOR LOOP: startIndex=" + startIndex +
                               " index=[" + index +
                               "] invalidIndex=" + invalidIndex);
            }

            // If the Item can be shrunken, get its minimum width,
            // and its preferred if it is not
            if (itemLFs[index].shouldHShrink()) {
                pW = itemLFs[index].lGetAdornedMinimumWidth();
            } else {
                if (itemLFs[index].lGetLockedWidth() != -1) {
                    pW = itemLFs[index].lGetLockedWidth();
                } else {
                    // if height is locked default preferred width
                    // will be used, otherwise width will be calculated
                    // based on lockedHeight
                    pW = itemLFs[index].lGetAdornedPreferredWidth(
                            itemLFs[index].lGetLockedHeight());

                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION,
                                       LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                       " no shrink - locked w - pW=" + pW +
                                       " viewable[width]=" + viewable[WIDTH]);
                    }
                }
            }

            // We have a notion of the maximum allowable width an Item can
            // have, so we enforce it first here:
            
            if (!Constants.SCROLLS_HORIZONTAL && (pW > viewable[WIDTH])) {
                pW = viewable[WIDTH];
            }

            // We use a separate boolean here to check for each case of
            // requiring a new line (rather than the if() from hell)
            boolean newLine = (index > 0 && itemLFs[index - 1].equateNLA() ||
                               itemLFs[index].equateNLB() || 
                               // no room for this item on the same row
                               pW > sizingBox[WIDTH]);

            if (isImplicitLineBreak(curAlignment, index, itemLFs)) {
                curAlignment = itemLFs[index].getLayout() & LAYOUT_HMASK;
                newLine = true;
            }

            // We linebreak if there is an existing row;
            // possible only when there is more than 1 item
            if (newLine && (lineHeight > 0)) {
                // index > 0, as the calculation of newLine guarantee

                //
                // ** NEW LINE **
                //
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, 
                                   LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                   "   --new line--");
                }

                // First, handle current row's layout directives

                try {

                    // used for layout update
                    // int oldRowHeight = itemLFs[index-1].rowHeight; 
                    boolean wasNewLine = itemLFs[index].isNewLine;

                    // now it's certainly first in line
                    itemLFs[index].isNewLine = true;

                    // layout items in previous row
                    lineHeight = layoutRowHorizontal(rowStart, index - 1, 
                                                     sizingBox[WIDTH],
                                                     lineHeight,
                                                     itemLFs);
                    layoutRowVertical(rowStart, index - 1, lineHeight, 
                                      itemLFs, numOfLFs);

                    if (fullLayout) {

                        if (numOfLFs > 1) {
                            viewable[HEIGHT] += lineHeight;
                        } else {
                            viewable[HEIGHT] += lineHeight + 1;
                        }

                    } else { // UPDATE_LAYOUT

                        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                            Logging.report(Logging.INFORMATION, 
                                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                           "** 1 ** row height=" +
                                           lineHeight);
                        }

                        // cases: 
                        // 1. this item was first in row already
                        //  > than update layout wouldn't be called for the
                        //  > row above it unless there was a change in that
                        //  > row.
                        //  > Therefore we can finish the loop, and just update
                        //  > the Y coordinates for the rest of the Items.

                        if (wasNewLine && index > invalidIndex) {
                            // we can call updateVertically by returning
                            // index-1 and marking next Y as invalid.
                            itemLFs[index].actualBoundsInvalid[Y] = true;


                            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                                Logging.report(
                                             Logging.INFORMATION,
                                             LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                             " returning index-1 and "+
                                             "marking next Y as invalid. }");
                            }


                            return (index-1);
                            // (some Items after it may still be invalid, 
                            // so the calling method will continue the layout).

                        } 

                        // 2. this item wasn't first on its row
                        //  > then we should continue a regular layout.

                        if (!wasNewLine) {
                            itemLFs[index].actualBoundsInvalid[X] = true;
                        }
                    }

                } catch (Throwable t) {
                    Display.handleThrowable(t);
                }

                // Then, reset the sizingBox, lineHeight, and rowStart
                sizingBox[X] = 0;
                if (fullLayout) {
                    sizingBox[Y] = viewable[HEIGHT];
                } else {
                    sizingBox[Y] = itemLFs[index-1].bounds[Y] + 
                        itemLFs[index-1].rowHeight;
                    if (numOfLFs <= 1) {
                        sizingBox[Y] += 1;
                    }
                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION, 
                                       LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                       "** 2 **   sizingBox[Y]=" +
                                       sizingBox[Y]);
                    }
                }

                sizingBox[WIDTH] = viewportWidth;

                lineHeight = 0;
                rowStart = index;
                
                itemLFs[index].isNewLine = true;

                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, 
                                   LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                   "  (new line end)");
                }
                //
                // ** NEW LINE - END **
                //
                                
            } else {
                
                // keep isNewLine flag up to date

                if (index == 0 || newLine) {
                    itemLFs[index].isNewLine = true; 
                } else {
                    
                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION, 
                                       LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                       "** " +index +" is not a new line **");
                    }

                    // this is not a new line
                    itemLFs[index].isNewLine = false;
                }

            }

            pH = getItemHeight(index, pW, itemLFs);


            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, 
                               LogChannels.LC_HIGHUI_FORM_LAYOUT,
                               " updateBlock.. pH = " +pH);
            }



            // If the Item is changing size, set the flag so that callPaint()
            // will call the Item's sizeChanged() method before painting

            if (oldWidth != viewportWidth || oldHeight != viewportHeight ||
                itemLFs[index].bounds[WIDTH] != pW || itemLFs[index].bounds[HEIGHT] != pH) { 
                itemLFs[index].sizeChanged = true;
            }
            
            if (!fullLayout && (index > invalidIndex)) {
                
                // if we've reached the end of the block (explicit linebreak)
                // than we can safely return
                if (itemLFs[index].equateNLB() || 
                    ((index > 0) && (itemLFs[index-1].equateNLA()))) {
                    
                    // we can stop the loop, returning the last Item laid out
                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION, 
                                       LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                       "** stop layout, explicit lb **\n}");
                    }
                    itemLFs[index].actualBoundsInvalid[Y] = true;
                    return (index - 1);

                    // identify more occasions where only Y will change
                } else if (itemLFs[index].bounds[X] == sizingBox[X] &&
                           // itemLFs[index].bounds[Y] == sizingBox[Y] &&
                           itemLFs[index].bounds[WIDTH] == pW &&
                           itemLFs[index].bounds[HEIGHT] == pH) {
                    
                    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                        Logging.report(Logging.INFORMATION, 
                                       LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                       "\n** no need to layout **\n}");
                    }

                    itemLFs[index].actualBoundsInvalid[X] = false; 
                    // notice the "true": (only the Y coordinate is invalid)
                    itemLFs[index].actualBoundsInvalid[Y] = true; 
                    itemLFs[index].actualBoundsInvalid[WIDTH] = false; 
                    itemLFs[index].actualBoundsInvalid[HEIGHT] = false;
                    
                    return (index - 1);   
                }                
            }

            // We assign bounds to the item, which is its pixel location,
            // width, and height in coordinates which represent offsets
            // of the viewport origin (that is, are in the viewport
            // coordinate space)
            itemLFs[index].lSetSize(pW, pH);
            itemLFs[index].lSetLocation(sizingBox[X], sizingBox[Y]);
            
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, 
                               LogChannels.LC_HIGHUI_FORM_LAYOUT,
                               "[F] index ("+ index +
                               " lineHeight == "+ lineHeight +
                               ") set height to:" + pH);
            }

            itemLFs[index].actualBoundsInvalid[X] = false;
            itemLFs[index].actualBoundsInvalid[Y] = false;
            itemLFs[index].actualBoundsInvalid[WIDTH] = false;
            itemLFs[index].actualBoundsInvalid[HEIGHT] = false;


            // If this Item is currently the tallest on the line, its
            // height becomes our prevailing lineheight
            if (pH > lineHeight) {
                lineHeight = pH;
            }

            // We whittle down the sizingBox by the Item's dimensions,
            // effectively maintaining how much space is left for the
            // remaining Items, if the item has some positive width
            if (pW > 0) {
                sizingBox[WIDTH] -= pW;
                // we know that item fits on this row but padding
                // might not fit
                if (sizingBox[WIDTH] < 0) {
                    sizingBox[WIDTH] = 0;
                }
                sizingBox[X] += pW;
            }

            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION,
                               LogChannels.LC_HIGHUI_FORM_LAYOUT,
                               "\t\tindex: " +index +
                               "\t[" + itemLFs[index].bounds[X] +
                               "," + itemLFs[index].bounds[Y] +
                               "," + itemLFs[index].bounds[WIDTH] +
                               "," + itemLFs[index].bounds[HEIGHT] + "]");
            }
        } // for
        
        // Before we quit, layout the last row we did in the loop
        try {
            
            int oldRowHeight = itemLFs[rowStart].rowHeight;

            lineHeight = layoutRowHorizontal(rowStart, numOfLFs - 1, 
                                             sizingBox[WIDTH], lineHeight,
                                             itemLFs);
            
            int rowY = itemLFs[rowStart].bounds[Y];

            layoutRowVertical(rowStart, numOfLFs - 1, lineHeight,
                              itemLFs, numOfLFs);

            viewable[HEIGHT] = rowY + lineHeight;

        } catch (Throwable t) {
            Display.handleThrowable(t);
        }

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION,
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           " returning invalidIndex:"+invalidIndex+" }");
        }

        return invalidIndex;
    }

    /**
     * Calculating how many pixels should the <pre>startIndex<> item move up
     * or down, and loop from this item until the end, adding the delta
     * to all these items.
     * We know where this startIndex should be, we know where it is
     * now, so we can know how much to move everything.
     *
     * The viewable height is updated accordingly.
     *
     * @param startIndex the index of the first item that should move
     *                   up or down. It should be first in its row, 
     *                   and the Item before it should be laid out 
     *                   correctly, with rowHeight set up.
     * @param itemLFs reference to the items array of the calling form
     * @param numOfLFs number of elements in the calling form
     * @param viewable area needed for the content of the form
     */
    private void updateVertically(int startIndex, 
                                  ItemLFImpl[] itemLFs, 
                                  int numOfLFs,
                                  int[] viewable) {
        
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "### in updateVertically for #" + startIndex +
                           ".\t");
        }        

        int deltaY = 0;
        int newY = 0;
        // loop on all the items, starting with this one, 
        // updating their Y, and unmark their flag
        if (startIndex == 0) {
            newY = 0;
            
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION,
                               LogChannels.LC_HIGHUI_FORM_LAYOUT,
                               "newY=" + newY);
            }

        } else {
            newY = itemLFs[startIndex-1].bounds[Y] +
                itemLFs[startIndex-1].rowHeight;
            
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION,
                               LogChannels.LC_HIGHUI_FORM_LAYOUT,
                               " itemLFs[si-1].bounds[Y]=" +
                               itemLFs[startIndex-1].bounds[Y] +
                               " itemLFs[si-1].rowHeight=" +
                               itemLFs[startIndex-1].rowHeight);
            }
        }
        
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           ">>> CustomItemLFImpl -- lRepaint()" + 
                           " itemLFs[si].bounds[Y]=" +
                           itemLFs[startIndex].bounds[Y] +
                           " newY=" + newY);
        }
        
        deltaY = newY - itemLFs[startIndex].bounds[Y];
        
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
            LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           " delta= " + deltaY);
        }
        
        if (deltaY == 0) {
            itemLFs[startIndex].actualBoundsInvalid[Y] = false;
            return;
        }
        
        for (int i = startIndex; i < numOfLFs; i++) {
            itemLFs[i].lMove(0, deltaY);
        }
        
        itemLFs[startIndex].actualBoundsInvalid[Y] = false;
        
        // update viewable height        
        viewable[HEIGHT] += deltaY;
    }

    /**
     * After the contents of a row have been determined, layout the
     * items on that row, taking into account the individual items'
     * horizontally oriented layout directives.
     *
     * @param rowStart the index of the first row element
     * @param rowEnd the index of the last row element
     * @param hSpace the amount of empty space in pixels in this row before 
     *               inflation
     * @param rowHeight the old row height
     * @param itemLFs reference to the items array of the calling form
     *
     * @return the new rowHeight for this row after all of the inflations
     */
    private int layoutRowHorizontal(int rowStart, int rowEnd,
                                    int hSpace, int rowHeight,
                                    ItemLFImpl[] itemLFs) {
        // SYNC NOTE: protected by the lock around calls to layout()
        
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "[F] layoutRowHorizontal -- rowStart=" + rowStart 
                           + " rowEnd="+rowEnd + " hSpace=" + hSpace +
                           " rowHeight="+rowHeight);
        }
        
        hSpace = inflateHShrinkables(rowStart, rowEnd, hSpace, itemLFs);
        hSpace = inflateHExpandables(rowStart, rowEnd, hSpace, itemLFs);
        
        
        // if any of the items were inflated we have to recalculate
        // the new row height for this row
        rowHeight = 0;
        for (int i = rowStart; i <= rowEnd; i++) {
            if (rowHeight < itemLFs[i].bounds[HEIGHT]) {
                rowHeight = itemLFs[i].bounds[HEIGHT];
            }
        }
        
        if (hSpace == 0) {
        
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, 
                               LogChannels.LC_HIGHUI_FORM_LAYOUT,
                               "[F] layoutRowHorizontal -- done -- " +
                               "(hSpace == 0) -- returning "+rowHeight);
            }
            return rowHeight;
        }
        
        int curAlignment = getCurHorAlignment(itemLFs, rowStart);

        switch (curAlignment) {
              case Item.LAYOUT_CENTER:
                hSpace = hSpace / 2;
                /* fall through */
        case Item.LAYOUT_RIGHT:
                    for (; rowStart <= rowEnd; rowStart++) {
                        itemLFs[rowStart].lMove(hSpace, 0);
                    }
                break;
        case Item.LAYOUT_LEFT:
        default:
            break;
        }


        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "[F] layoutRowHorizontal -- done " +
                           "-- returning "+rowHeight);
        }

        return rowHeight;

    }
 
    /**
     * Gets the current horizontal alignment of the item. If Item's
     * horizontal layout bits are not set its current horizontal
     * alignment is the same as of the previous Item.
     * 
     * @param itemLFs reference to the items array of the calling form
     * @param index the index of an item in the itemLFs array which 
     *        current horizontal alignment needs to be found out
     * @return currentl horizontal alignment of an Item with passed in 
     *         index
     */
    private int  getCurHorAlignment(ItemLFImpl[] itemLFs, int index) {
        for (int hAlign, i = index; i >= 0; i--) {
            hAlign = itemLFs[i].getLayout() & LAYOUT_HMASK;

            if (hAlign != Item.LAYOUT_DEFAULT)
                return hAlign;
        }
        // default layout
        if (rl_direction) {
            return Item.LAYOUT_RIGHT;
        } else {
            return Item.LAYOUT_LEFT;
        }
    }

    /**
     * Inflate all the horizontally 'shrinkable' items on a row.
     *
     * @param rowStart the index of the first row element
     * @param rowEnd the index of the last row element
     * @param space the amount of empty space left in pixels in this row
     * @param itemLFs reference to the items array of the calling form
     *
     * @return the amount of empty space on this row after shinkage
     */
    private int inflateHShrinkables(int rowStart, int rowEnd, int space,
                                    ItemLFImpl[] itemLFs) {
        // SYNC NOTE: protected by the lock around calls to layout()
     
         if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "[F] inflateHShrinkables -- rowStart=" +
                           rowStart + " rowEnd=" + rowEnd +
                           " space=" + space);
        }
  
        if (space == 0) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION,
                               LogChannels.LC_HIGHUI_FORM_LAYOUT,
                               "[F] inflateHShrinkables -- returning " +
                               "(space == 0)");
            }
            return 0;
        }
        
        // To inflate shrinkables, we make a first pass gathering
        // the smallest proportion (the baseline)
        int baseline = Integer.MAX_VALUE;
        int pW, prop = 0;
        
        for (int i = rowStart; i <= rowEnd; i++) {
            if (itemLFs[i].shouldHShrink()) {
                pW = itemLFs[i].lGetLockedWidth();
                if (pW == -1) {
                    pW = itemLFs[i].lGetAdornedPreferredWidth(
                            itemLFs[i].lGetLockedHeight());
                }
                prop = pW - itemLFs[i].lGetAdornedMinimumWidth();
                if (prop > 0 && prop < baseline) {
                    baseline = prop;
                }
            }
        }
        
        // If there are no shrinkables, return
        if (baseline == Integer.MAX_VALUE) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION,
                               LogChannels.LC_HIGHUI_FORM_LAYOUT,
                               "[F] inflateHShrinkables -- returning " +
                               "(baseline == Integer.MAX_VALUE) space == " +
                               space);
            }
            return space;
        }
            
        prop = 0;
        
        // Now we loop again, adding up all the proportions so
        // we can find the adder
        for (int i = rowStart; i <= rowEnd; i++) {
            if (itemLFs[i].shouldHShrink()) {
                pW = itemLFs[i].lGetLockedWidth();
                if (pW == -1) {
                    pW = itemLFs[i].lGetAdornedPreferredWidth(
                            itemLFs[i].lGetLockedHeight());
                }
                prop += ((pW - itemLFs[i].lGetAdornedMinimumWidth()) /
                         baseline);
            }
        }
        
           if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "[F] inflateHShrinkables -- prop == "+prop);
        }

     
        // Now we compute the adder, and add it to each of the
        // shrinkables, times its proportion
        int adder = space / prop;
        
        for (int i = rowStart; i <= rowEnd; i++) {
            if (itemLFs[i].shouldHShrink()) {
                
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, 
                                   LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                   "### item "+i+" before shrinking is:" +
                                   itemLFs[i].bounds[WIDTH]);
                }

                pW = itemLFs[i].lGetLockedWidth();
                if (pW == -1) {
                    pW = itemLFs[i].lGetAdornedPreferredWidth(
                            itemLFs[i].lGetLockedHeight());
                }
                space = pW - itemLFs[i].lGetAdornedMinimumWidth();
                
                // The proportionate amount of space to add
                prop = adder * (space / baseline);
                
                // We only enlarge the item to its preferred width at
                // a maximum
                if (space > prop) {
                    space = prop;
                }
                itemLFs[i].lSetSize(itemLFs[i].bounds[WIDTH] + space,
                                    getItemHeight(i, 
                                                  itemLFs[i].bounds[WIDTH] + 
                                                  space,
                                                  itemLFs));
                
                // Now we have to shift the rest of the elements on the line
                for (int j = i + 1; j <= rowEnd; j++) {
                    itemLFs[j].lMove(space, 0);
                }
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, 
                                   LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                   "### item " + i + " shrank to:" +
                                   itemLFs[i].bounds[WIDTH]);
                }
            }
        }
        
        space = viewportWidth -
            (itemLFs[rowEnd].bounds[X] + itemLFs[rowEnd].bounds[WIDTH]);

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "[F] inflateHShrinkables -- " +
                           "returning (end). space == " + space);
        }
        
        return space;
    }
    
    /**
     * Inflate all the horizontally 'expandable' items on a row.
     *
     * @param rowStart the index of the first row element
     * @param rowEnd the index of the last row element
     * @param space the amount of empty space on this row
     * @param itemLFs reference to the items array of the calling form
     *
     * @return the amount of empty space after expansion
     */
    private int inflateHExpandables(int rowStart, int rowEnd, int space,
                                    ItemLFImpl[] itemLFs) {
        // SYNC NOTE: protected by the lock around calls to layout()

        if (space == 0) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, 
                               LogChannels.LC_HIGHUI_FORM_LAYOUT,
                               "[F] inflateHExpandables -- " +
                               "returning (space == 0)");
            }
            return 0;
        }
        
        int numExp = 0;
        // We first loop through and count the expandables
        for (int i = rowStart; i <= rowEnd; i++) {
            if (itemLFs[i].shouldHExpand()) {
                numExp++;
            }
        }
        
        if (numExp == 0 || space < numExp) {
            if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                Logging.report(Logging.INFORMATION, 
                               LogChannels.LC_HIGHUI_FORM_LAYOUT,
                               "[F] inflateHExpandables -- returning " +
                               "(numExp == 0 || space < numExp) space = " +
                               space);
            }
            return space;
        }
        
        space = space / numExp;

        // We then add the same amount to each Expandable
        for (int i = rowStart; i <= rowEnd; i++) {
            if (itemLFs[i].shouldHExpand()) {
                itemLFs[i].lSetSize(itemLFs[i].bounds[WIDTH] + space,
                                   getItemHeight(i,
                                                 itemLFs[i].bounds[WIDTH] +
                                                 space,
                                                 itemLFs));

                itemLFs[i].lGetContentSize(itemLFs[i].lGetContentBounds(),itemLFs[i].bounds[WIDTH] + space);



                // We right shift each subsequent item on the row
                for (int j = i + 1; j <= rowEnd; j++) {
                    itemLFs[j].lMove(space, 0);
                }
            }
        }
        
        space = viewportWidth -
            (itemLFs[rowEnd].bounds[X] + itemLFs[rowEnd].bounds[WIDTH]);

               if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "[F] inflateHExpandables -- " +
                           "returning (end) space = " + space);
        }
 
        return space;
    }
    
    /**
     * After the contents of a row have been determined, layout the
     * items on that row, taking into account the individual items'
     * vertically oriented layout directives.
     *
     * @param rowStart the index of the first row element
     * @param rowEnd the index of the last row element
     * @param itemLFs reference to the items array of the calling form
     * @param lineHeight the overall height in pixels of the line
     * @param numOfLFs number of elements in the calling form
     */
    private void layoutRowVertical(int rowStart, int rowEnd, 
                                   int lineHeight,
                                   ItemLFImpl[] itemLFs, 
                                   int numOfLFs) {
        // SYNC NOTE: protected by the lock around calls to layout()
        
        int space = 0;
        int pH = 0;
        
        for (int i = rowStart; i <= rowEnd; i++) {

            // set the row height
            itemLFs[i].rowHeight = lineHeight;
            
            // Items that have the LAYOUT_VSHRINK  directive are expanded 
            // to their preferred height or to the height of the row, 
            // whichever is smaller. Items that are still shorter than 
            // the row height and that have the LAYOUT_VEXPAND directive 
            // will expand to the height of the row.
            if (itemLFs[i].shouldVExpand()) {
                itemLFs[i].lSetSize(itemLFs[i].bounds[WIDTH], lineHeight);
                
            } else if (itemLFs[i].shouldVShrink()) {
                pH = itemLFs[i].lGetLockedHeight();
                if (pH == -1) {
                    pH = itemLFs[i].
                        lGetAdornedPreferredHeight(itemLFs[i].bounds[WIDTH]);
                }
                if (pH > lineHeight) {
                    pH = lineHeight;
                }
                itemLFs[i].lSetSize(itemLFs[i].bounds[WIDTH], pH);
            }
            
            // initially the items are aligned at the top so we simply
            // add on to the height
            switch (itemLFs[i].getLayout() & LAYOUT_VMASK) {
            case Item.LAYOUT_VCENTER:
                space = lineHeight - itemLFs[i].bounds[HEIGHT];
                if (space > 0) {
                    itemLFs[i].lMove(0, space / 2);
                }
                break;
            case Item.LAYOUT_TOP:
                // it's already there...
                break;
            case Item.LAYOUT_BOTTOM:
                // the layout algorithm must align the Items along the bottom
                // if there is no vertical directive specified.
            default:
                space = lineHeight - itemLFs[i].bounds[HEIGHT];
                if (space > 0) {
                    itemLFs[i].lMove(0, space);
                }                
                if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, 
                                   LogChannels.LC_HIGHUI_FORM_LAYOUT,
                                   "Default V layout -- space = " +
                                   space +
                                   " itemLFs[i].Y = " +
                                   itemLFs[i].bounds[Y]);
                }
            }
        }

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           "[F] layoutRowVertical -- done");
        }
        
    }

    /**
     * This method checks if we need a new line due to change in 
     * horizontal alignment. If horizontal alignment is not set on 
     * <code>Item</code> with index thisItem then current horizontal 
     * alignment is not changed and no row break is needed.
     *
     * @param curAlignment current horizontal alignment until this Item
     * @param thisItem index of the <code>Item</code> from which to start 
     *                 the scan
     * @param itemLFs reference to the items array of the calling form
     *
     * @return <code>true</code> if a new line is needed
     */
    private boolean isImplicitLineBreak(int curAlignment, int thisItem, 
                                        ItemLFImpl[] itemLFs) {

        if (thisItem == 0) {
            return false;
        }

        int hAlign = itemLFs[thisItem].getLayout() & LAYOUT_HMASK;

        // alignment is not changed
        if (hAlign == 0) {
            return false;
        }

        return (hAlign != curAlignment);
    }

    /**
     * Get item's height based on the width.
     *
     * @param index the index of the item which height is being calculated 
     * @param pW the width set for the item
     * @param itemLFs reference to the items array of the calling form
     *
     * @return the height of the item
     */
    private int getItemHeight(int index, int pW, ItemLFImpl[] itemLFs) {
        // SYNC NOTE: protected by the lock around calls to layout()
        
        int pH;
        
        // If the Item can be shrunken, we use its minimum height,
        // and its preferred height if it is not
        if (itemLFs[index].shouldVShrink()) {
            pH = itemLFs[index].lGetAdornedMinimumHeight();
        } else {
            pH = itemLFs[index].lGetLockedHeight();
            if (pH == -1) {
                pH = itemLFs[index].lGetAdornedPreferredHeight(pW);
            }
        }

        // If we can't scroll vertically, clip the item's height
        // if it can't fit in the view. We would also enforce a
        // notion of a "maximum vertical height" here if we had one
        if (!Constants.SCROLLS_VERTICAL &&
            pH > viewportHeight) {
            pH = viewportHeight;
        }

        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, 
                           LogChannels.LC_HIGHUI_FORM_LAYOUT,
                           " LayoutManager- getItemHeight("+index+","+pW+
                           ") returns "+pH);
        }

        return pH;        
    }

    /**
     * Singleton design pattern: obtain access to the single instance of
     * this class using this method.
     *
     * @return Single instance of LayoutManager
     */
    static LayoutManager instance() {
        return singleInstance;
    }

    /** 
     * A bit mask to capture the horizontal layout directive of an item.
     */
    static final int LAYOUT_HMASK = 0x03;

    /** 
     * A bit mask to capture the vertical layout directive of an item. 
     */
    static final int LAYOUT_VMASK = 0x30;

    /**
     * 'sizingBox' is a [x,y,w,h] array used for dynamic sizing of 
     * <code>Item</code>s during the layout. It starts with the size of the 
     * viewport, but can shrink or grow according to the <code>Item</code> 
     * it tries to lay out.
     *
     * It is used by layoutBlock and layoutRow.
     */
    private int[] sizingBox;

    /** Do a full layout. */
    final static int FULL_LAYOUT = -1;

    /** Only update layout. */
    final static int UPDATE_LAYOUT = -2;
    
    /**
     * Single instance of the LayoutManager class.
     */
    static LayoutManager singleInstance = new LayoutManager();

    /** layout derection depend on the language conventions in use */
    private boolean rl_direction;


    /** Used as an index into the viewport[], for the x origin. */
    final static int X      = DisplayableLFImpl.X;
        
    /** Used as an index into the viewport[], for the y origin. */
    final static int Y      = DisplayableLFImpl.Y;
    
    /** Used as an index into the viewport[], for the width. */
    final static int WIDTH  = DisplayableLFImpl.WIDTH;
    
    /** Used as an index into the viewport[], for the height. */
    final static int HEIGHT = DisplayableLFImpl.HEIGHT;

    /** Width of viewport, as passed to layout(). */
    int viewportWidth = 0;

    /** Height of viewport, as passed to layout(). */
    int viewportHeight = 0;

}
