
/**
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
 * 
 * These are constant defines both in native and Java layers.
 * NOTE: DO NOT EDIT. THIS FILE IS GENERATED. If you want to 
 * edit it, you need to modify the corresponding XML files.
 *
 * Patent pending.
 */
package com.sun.midp.chameleon.skins;

/** Indentifiers for Chameleon skin properties */public class SkinPropertiesIDs {

    /**
     * The orientation of text. The value indicates the directionality of 
     * various text on the display. Valid values are Graphics.LEFT and 
     * Graphics.RIGHT. A value of Graphics.RIGHT, for example, would indicate 
     * text which reads from left to right. NOTE: This property currently has 
     * no effect.
     */
    public final static int SCREEN_TEXT_ORIENT = 0;

    /**
     * The amount of padding (in pixels) between items in a Form.
     */
    public final static int SCREEN_PAD_FORM_ITEMS = 1;

    /**
     * The amount of padding (in pixels) around the top/bottom of a label.
     */
    public final static int SCREEN_PAD_LABEL_VERT = 2;

    /**
     * The amount of padding (in pixels) around the left/right of a label.
     */
    public final static int SCREEN_PAD_LABEL_HORIZ = 3;

    /**
     * The background color for a screen. In cases where there is either no 
     * image background or the images could not be loaded, this color is used 
     * as a solid fill for screen backgrounds.
     */
    public final static int SCREEN_COLOR_BG = 4;

    /**
     * The background color for the 'home' screen. In cases where there no 
     * image background or the images could not be loaded, this color is used 
     * as a solid fill for screen backgrounds.
     */
    public final static int SCREEN_COLOR_HS_BG = 5;

    /**
     * The foreground color for a screen. This is the basic color that all 
     * foreground text is painted.
     */
    public final static int SCREEN_COLOR_FG = 6;

    /**
     * The background highlight color for highlighted elements. This is often 
     * the solid background color for selected text and things of that nature.
     */
    public final static int SCREEN_COLOR_BG_HL = 7;

    /**
     * The foreground highlight color for highlighted elements. This is often 
     * the foreground color for selected text and things of that nature. It 
     * should be of a contrasting nature to the background highlight color.
     */
    public final static int SCREEN_COLOR_FG_HL = 8;

    /**
     * The color to use for a border around a custom item.
     */
    public final static int SCREEN_COLOR_BORDER = 9;

    /**
     * The color to use for a border around a custom item which is currently 
     * traversed to (or highlighted).
     */
    public final static int SCREEN_COLOR_BORDER_HL = 10;

    /**
     * The color with which to draw a traversal indicator for elements which 
     * must do so (such as custom items).
     */
    public final static int SCREEN_COLOR_TRAVERSE_IND = 11;

    /**
     * The style of border for those visual elements which draw a border, 
     * either Graphics.SOLID or Graphics.DOTTED.
     */
    public final static int SCREEN_BORDER_STYLE = 12;

    /**
     * The amount of pixels to scroll the contents of the Screen at one time.
     */
    public final static int SCREEN_SCROLL_AMOUNT = 13;

    /**
     * The Font to use when rendering Item labels in a Form.
     */
    public final static int SCREEN_FONT_LABEL = 14;

    /**
     * The Font to use as the predefined Font.INPUT_TEXT font.
     */
    public final static int SCREEN_FONT_INPUT_TEXT = 15;

    /**
     * The Font to use as the predefined Font.STATIC_TEXT font.
     */
    public final static int SCREEN_FONT_STATIC_TEXT = 16;

    /**
     * The Image to use as a tile to paint a 'wash' over the rest of the 
     * screen contents. An example of this is when displaying an Alert on top 
     * of other screen elements, the background is covered with a 
     * semi-transparent 'wash' to give it a grayish-out effect. If isn't 
     * specified or cannot be loaded, no wash will be painted.
     */
    public final static int SCREEN_IMAGE_WASH = 17;

    /**
     * The Image to use as a tile for the typical screen background. This 
     * image could be either fullsize, or if smaller than fullsize, it will 
     * be tiled both horizontally and vertically to fill the entire screen 
     * background.
     */
    public final static int SCREEN_IMAGE_BG = 18;

    /**
     * The 9 pc Image background to use for the typical screen background 
     * with a title. The images correspond to: Piece 0: Top left corner of 
     * the top-bar. Piece 1: Middle tile for the top-bar. Piece 2: Top right 
     * corner of the top-bar. Piece 3: Left edge of the middle-bar. Piece 4: 
     * Middle tile for the middle-bar. Piece 5: Right edge of the middle-bar. 
     * Piece 6: Bottom left corner of the bottom-bar. Piece 7: Middle tile 
     * for the bottom-bar. Piece 8: Bottom right corner of the bottom-bar. 
     * The top-bar is the area which serves to visually cap the top of the 
     * background. The middle-bar is the background 'tile' for a single 
     * horizontal row of the background. It will be repeatedly drawn 
     * vertically to fill the background area between the top-bar and the 
     * bottom-bar. The bottom-bar is the bottom border of the screen 
     * background and serves to visually cap the bottom of the background. 
     * All three bars are divided into three pieces, a left, a middle, and a 
     * right. If the combined widths of the left, middle, and right images is 
     * less than the overall width of the background, the middle image will 
     * be tiled horizontally to make up the difference. If isn't specified or 
     * cannot be loaded, solid fill color is used instead of background image.
     */
    public final static int SCREEN_IMAGE_BG_W_TITLE = 19;

    /**
     * The 9 pc Image background to use for the typical screen background 
     * without a title. The images correspond to: Piece 0: Top left corner of 
     * the top-bar. Piece 1: Middle tile for the top-bar. Piece 2: Top right 
     * corner of the top-bar. Piece 3: Left edge of the middle-bar. Piece 4: 
     * Middle tile for the middle-bar. Piece 5: Right edge of the middle-bar. 
     * Piece 6: Bottom left corner of the bottom-bar. Piece 7: Middle tile 
     * for the bottom-bar. Piece 8: Bottom right corner of the bottom-bar. 
     * The top-bar is the area which serves to visually cap the top of the 
     * background. The middle-bar is the background 'tile' for a single 
     * horizontal row of the background. It will be repeatedly drawn 
     * vertically to fill the background area between the top-bar and the 
     * bottom-bar. The bottom-bar is the bottom border of the screen 
     * background and serves to visually cap the bottom of the background. 
     * All three bars are divided into three pieces, a left, a middle, and a 
     * right. If the combined widths of the left, middle, and right images is 
     * less than the overall width of the background, the middle image will 
     * be tiled horizontally to make up the difference. If isn't specified or 
     * cannot be loaded, solid fill color is used instead of background image.
     */
    public final static int SCREEN_IMAGE_BG_WO_TITLE = 20;

    /**
     * The Image to use as a tile for the 'home' screen background. This 
     * image could be either fullsize, or if smaller than fullsize, it will 
     * be tiled both horizontally and vertically to fill the entire screen 
     * background.
     */
    public final static int SCREEN_IMAGE_HS_BG_TILE = 21;

    /**
     * The 9 pc Image background to use for the 'home' screen background with 
     * a title. The images correspond to: Piece 0: Top left corner of the 
     * top-bar. Piece 1: Middle tile for the top-bar. Piece 2: Top right 
     * corner of the top-bar. Piece 3: Left edge of the middle-bar. Piece 4: 
     * Middle tile for the middle-bar. Piece 5: Right edge of the middle-bar. 
     * Piece 6: Bottom left corner of the bottom-bar. Piece 7: Middle tile 
     * for the bottom-bar. Piece 8: Bottom right corner of the bottom-bar. 
     * The top-bar is the area which serves to visually cap the top of the 
     * background. The middle-bar is the background 'tile' for a single 
     * horizontal row of the background. It will be repeatedly drawn 
     * vertically to fill the background area between the top-bar and the 
     * bottom-bar. The bottom-bar is the bottom border of the screen 
     * background and serves to visually cap the bottom of the background. 
     * All three bars are divided into three pieces, a left, a middle, and a 
     * right. If the combined widths of the left, middle, and right images is 
     * less than the overall width of the background, the middle image will 
     * be tiled horizontally to make up the difference. If isn't specified or 
     * cannot be loaded, solid fill color is used instead of background image.
     */
    public final static int SCREEN_IMAGE_HS_BG_W_TITLE = 22;

    /**
     * The 9 pc Image background to use for the 'home' screen background 
     * without a title. The images correspond to: Piece 0: Top left corner of 
     * the top-bar. Piece 1: Middle tile for the top-bar. Piece 2: Top right 
     * corner of the top-bar. Piece 3: Left edge of the middle-bar. Piece 4: 
     * Middle tile for the middle-bar. Piece 5: Right edge of the middle-bar. 
     * Piece 6: Bottom left corner of the bottom-bar. Piece 7: Middle tile 
     * for the bottom-bar. Piece 8: Bottom right corner of the bottom-bar. 
     * The top-bar is the area which serves to visually cap the top of the 
     * background. The middle-bar is the background 'tile' for a single 
     * horizontal row of the background. It will be repeatedly drawn 
     * vertically to fill the background area between the top-bar and the 
     * bottom-bar. The bottom-bar is the bottom border of the screen 
     * background and serves to visually cap the bottom of the background. 
     * All three bars are divided into three pieces, a left, a middle, and a 
     * right. If the combined widths of the left, middle, and right images is 
     * less than the overall width of the background, the middle image will 
     * be tiled horizontally to make up the difference. If isn't specified pr 
     * cannot be loaded, solid fill color is used instead of background image.
     */
    public final static int SCREEN_IMAGE_HS_BG_WO_TITLE = 23;

    /**
     * The 'mode' of the scroll indicator. Valid values are either 
     * ScrollIndSkin.MODE_ARROWS or ScrollIndSkin.MODE_BAR.
     */
    public final static int SCROLL_MODE = 24;

    /**
     * The width of the scroll indicator. This value is only used when no 
     * images can be loaded to determine the indicator's width.
     */
    public final static int SCROLL_WIDTH = 25;

    /**
     * The background fill color for the scroll indicator (in bar mode). If 
     * no images can be loaded, this color is used to render the background 
     * of a scrollbar.
     */
    public final static int SCROLL_COLOR_BG = 26;

    /**
     * The foreground fill color for the scroll indicator (in bar mode). If 
     * no images can be loaded, this color is used to render the foreground 
     * of a scrollbar.
     */
    public final static int SCROLL_COLOR_FG = 27;

    /**
     * The foreground fill color for the scroll bar frame.
     */
    public final static int SCROLL_COLOR_FRAME = 28;

    /**
     * The foreground fill color for the pressed scrollbar arrow
     */
    public final static int SCROLL_COLOR_UP_ARROW = 29;

    /**
     * The foreground fill color for the released scrollbar arrow
     */
    public final static int SCROLL_COLOR_DN_ARROW = 30;

    /**
     * A 3 piece background image to use to render the background of the 
     * scroll indicator (in bar mode). Piece 0: Top cap of the bar. Piece 1: 
     * Middle tile of the bar. Piece 2: Bottom cap of the bar. These images 
     * are used to render a background for the scrollbar. If the combined 
     * heights of the top, middle, and bottom images is less than the overall 
     * height of the scrollbar, the middle image will be tiled vertically to 
     * make up the difference. If isn't specified or cannot be loaded, solid 
     * fill color is used instead of background image.
     */
    public final static int SCROLL_IMAGE_BG = 31;

    /**
     * A 3 piece foreground image to use to render the 'thumb' of the scroll 
     * indicator (in bar mode). Piece 0: Top cap of the thumb. Piece 1: 
     * Middle tile of the thumb. Piece 2: Bottom cap of the thumb. These 
     * images are used to render a foreground for the scrollbar. If the 
     * combined heights of the top, middle, and bottom images is less than 
     * the overall height of the thumb, the middle image will be tiled 
     * vertically to make up the difference. If isn't specified or cannot be 
     * loaded, solid fill color is used instead of foreground image.
     */
    public final static int SCROLL_IMAGE_FG = 32;

    /**
     * The 'up' arrow for a scroll indicator in arrow mode.
     */
    public final static int SCROLL_IMAGE_UP = 33;

    /**
     * The 'down' arrow for a scroll indicator in arrow mode.
     */
    public final static int SCROLL_IMAGE_DN = 34;

    /**
     * The background fill color for the scroll indicator (in bar mode) when 
     * used for an Alert. If no images can be loaded, this color is used to 
     * render the background of a scrollbar.
     */
    public final static int SCROLL_COLOR_AU_BG = 35;

    /**
     * The foreground fill color for the scroll indicator (in bar mode) when 
     * used for an Alert. If no images can be loaded, this color is used to 
     * render the foreground of a scrollbar.
     */
    public final static int SCROLL_COLOR_AU_FG = 36;

    /**
     * A 3 piece background image to use to render the background of the 
     * scroll indicator (in bar mode) when used for an Alert. Piece 0: Top 
     * cap of the bar. Piece 1: Middle tile of the bar. Piece 2: Bottom cap 
     * of the bar. These images are used to render a background for the 
     * scrollbar. If the combined heights of the top, middle, and bottom 
     * images is less than the overall height of the scrollbar, the middle 
     * image will be tiled vertically to make up the difference. If isn't 
     * specified or cannot be loaded, solid fill color is used instead of 
     * background image.
     */
    public final static int SCROLL_IMAGE_AU_BG = 37;

    /**
     * A 3 piece foreground image to use to render the 'thumb' of the scroll 
     * indicator (in bar mode) when used for an Alert. Piece 0: Top cap of 
     * the thumb. Piece 1: Middle tile of the thumb. Piece 2: Bottom cap of 
     * the thumb. These images are used to render a foreground for the 
     * scrollbar. If the combined heights of the top, middle, and bottom 
     * images is less than the overall height of the thumb, the middle image 
     * will be tiled vertically to make up the difference. If isn't specified 
     * or cannot be loaded, solid fill color is used instead of foreground 
     * image.
     */
    public final static int SCROLL_IMAGE_AU_FG = 38;

    /**
     * The 'up' arrow for a scroll indicator in arrow mode for an Alert.
     */
    public final static int SCROLL_IMAGE_AU_UP = 39;

    /**
     * The 'down' arrow for a scroll indicator in arrow mode for an Alert.
     */
    public final static int SCROLL_IMAGE_AU_DN = 40;

    /**
     * The overall height of the soft button bar.
     */
    public final static int SOFTBTN_HEIGHT = 41;

    /**
     * The number of 'soft buttons' to represent on the soft button bar, 
     * typically 2.
     */
    public final static int SOFTBTN_NUM_BUTTONS = 42;

    /**
     * Specifies the alignment property of the drop shadow on the button 
     * text. This should be a combination value such as Graphics.TOP | 
     * Graphics.LEFT or Graphics.RIGHT | Graphics.BOTTOM.
     */
    public final static int SOFTBTN_BUTTON_SHD_ALIGN = 43;

    /**
     * The text foreground color for buttons.
     */
    public final static int SOFTBTN_COLOR_FG = 44;

    /**
     * The text drop-shadow color for buttons.
     */
    public final static int SOFTBTN_COLOR_FG_SHD = 45;

    /**
     * The solid fill-color for the button bar. If the background image isn't 
     * specified or cannot be loaded, this fill color will be used.
     */
    public final static int SOFTBTN_COLOR_BG = 46;

    /**
     * The text foreground color for buttons on the button bar when the 
     * system menu is up.
     */
    public final static int SOFTBTN_COLOR_MU_FG = 47;

    /**
     * The text drop-shadow color for buttons on the button bar when the 
     * system menu is up.
     */
    public final static int SOFTBTN_COLOR_MU_FG_SHD = 48;

    /**
     * The solid fill-color for the button bar when the system menu is up. If 
     * the background image isn't specified or cannot be loaded, this fill 
     * color will be used.
     */
    public final static int SOFTBTN_COLOR_MU_BG = 49;

    /**
     * The text foreground color for buttons on the button bar when a system 
     * alert is up.
     */
    public final static int SOFTBTN_COLOR_AU_FG = 50;

    /**
     * The text drop-shadow color for buttons on the button bar when a system 
     * alert is up.
     */
    public final static int SOFTBTN_COLOR_AU_FG_SHD = 51;

    /**
     * The solid fill-color for the button bar when a system alert is up. If 
     * the background image isn't specified or cannot be loaded, this fill 
     * color will be used.
     */
    public final static int SOFTBTN_COLOR_AU_BG = 52;

    /**
     * The font to use to render the softbutton text.
     */
    public final static int SOFTBTN_FONT = 53;

    /**
     * The soft button label to use to represent that pressing that soft 
     * button will bring up the system menu, typically 'Menu'.
     */
    public final static int SOFTBTN_TEXT_MENUCMD = 54;

    /**
     * The soft button label to use for the command which closes the system 
     * menu once it is opened, typically 'Back'.
     */
    public final static int SOFTBTN_TEXT_BACKCMD = 55;

    /**
     * The 3 piece background image for the button bar. Piece 0: Left edge of 
     * the button bar. Piece 1: Middle tile of the button bar. Piece 2: Right 
     * edge of the button bar. These images are used to render a background 
     * for the button bar. If the combined widths of the left, middle, and 
     * right images is less than the overall width of the button bar, the 
     * middle image will be tiled horizontally to make up the difference. If 
     * isn't specified or cannot be loaded, solid fill color is used instead 
     * of background image.
     */
    public final static int SOFTBTN_IMAGE_BG = 56;

    /**
     * The 3 piece background image for the button bar when the system menu 
     * is up. Piece 0: Left edge of the button bar. Piece 1: Middle tile of 
     * the button bar. Piece 2: Right edge of the button bar. These images 
     * are used to render a background for the button bar. If the combined 
     * widths of the left, middle, and right images is less than the overall 
     * width of the button bar, the middle image will be tiled horizontally 
     * to make up the difference. If isn't specified or cannot be loaded, 
     * solid fill color is used instead of background iamge.
     */
    public final static int SOFTBTN_IMAGE_MU_BG = 57;

    /**
     * The 3 piece background image for the button bar when a system alert is 
     * up. Piece 0: Left edge of the button bar. Piece 1: Middle tile of the 
     * button bar. Piece 2: Right edge of the button bar. These images are 
     * used to render a background for the button bar. If the combined widths 
     * of the left, middle, and right images is less than the overall width 
     * of the button bar, the middle image will be tiled horizontally to make 
     * up the difference. If isn't specified or cannot be loaded, solid fill 
     * color is used instead of background image.
     */
    public final static int SOFTBTN_IMAGE_AU_BG = 58;

    /**
     * The overall height of this ticker (in pixels).
     */
    public final static int TICKER_HEIGHT = 59;

    /**
     * The vertical alignment of this ticker, can be either Graphics.TOP or 
     * Graphics.BOTTOM.
     */
    public final static int TICKER_ALIGN = 60;

    /**
     * The direction of motion of the text in this ticker. Valid values are 
     * either Graphics.RIGHT or Graphics.LEFT, meaning text moves to the 
     * right or to the left, respectively.
     */
    public final static int TICKER_DIRECTION = 61;

    /**
     * The amount of time (in milliseconds) between subsequent ticker text 
     * updates. This is the rate of animation for the ticker text. For 
     * example, a value of 500 would move the text across the ticker two 
     * times per second. The distance the text would move per update is 
     * defined by TICKER_SPEED.
     */
    public final static int TICKER_RATE = 62;

    /**
     * The rate of movement per cycle for the ticker text (measured in 
     * pixels). This is the amount of travel (in pixels) the text will move 
     * for each update of the ticker.
     */
    public final static int TICKER_SPEED = 63;

    /**
     * The y-coordinate of the anchor point for the ticker text. This value 
     * represents the TOP of the ticker text.
     */
    public final static int TICKER_TEXT_ANCHOR_Y = 64;

    /**
     * The alignment directive for the drop shadow of the ticker text. If 
     * this ticker has a drop shadow, this directive specified where to align 
     * the drop shadow. Valid values are: Graphics.TOP | Graphics.LEFT, 
     * Graphics.TOP | Graphics.RIGHT, Graphics.BOTTOM | Graphics.LEFT, 
     * Graphics.BOTTOM | Graphics.RIGHT.
     */
    public final static int TICKER_TEXT_SHD_ALIGN = 65;

    /**
     * The background fill color for this ticker if its background image 
     * isn't specified or cannot be loaded.
     */
    public final static int TICKER_COLOR_BG = 66;

    /**
     * The foreground color for rendering the text of this ticker.
     */
    public final static int TICKER_COLOR_FG = 67;

    /**
     * The drop-shadow color of the text of this ticker.
     */
    public final static int TICKER_COLOR_FG_SHD = 68;

    /**
     * The font to render the text of this ticker.
     */
    public final static int TICKER_FONT = 69;

    /**
     * The 3 piece background image to be used for this ticker. Piece 0: Left 
     * edge of the ticker. Piece 1: Middle tile of the ticker. Piece 2: Right 
     * edge of the ticker. These images are used to render a background for 
     * the ticker. If the combined widths of the left, middle, and right 
     * images is less than the overall width of the ticker, the middle image 
     * will be tiled horizontally to make up the difference. If isn't 
     * specified or cannot be loaded, solid fill color is used instead of 
     * background image.
     */
    public final static int TICKER_IMAGE_BG = 70;

    /**
     * The 3 piece background image to be used for this ticker when a system 
     * alert is visible. Piece 0: Left edge of the ticker. Piece 1: Middle 
     * tile of the ticker. Piece 2: Right edge of the ticker. These images 
     * are used to render a background for the ticker.If the combined widths 
     * of the left, middle, and right images is less than the overall width 
     * of the ticker, the middle image will be tiled horizontally to make up 
     * the difference. If isn't specified or cannot be loaded, solid fill 
     * color is used instead of background image.
     */
    public final static int TICKER_IMAGE_AU_BG = 71;

    /**
     * The overall height of this ticker (in pixels).
     */
    public final static int PTI_HEIGHT = 72;

    /**
     * The text margin in PTI bar (in pixels).
     */
    public final static int PTI_MARGIN = 73;

    /**
     * The background fill color for this pti bar if its background image 
     * isn't specified or cannot be loaded.
     */
    public final static int PTI_COLOR_BG = 74;

    /**
     * The foreground color for rendering the text of this pti bar.
     */
    public final static int PTI_COLOR_FG = 75;

    /**
     * The highlighted color of the text of this pti bar.
     */
    public final static int PTI_COLOR_FG_HL = 76;

    /**
     * The highlighted color of the background of this pti bar.
     */
    public final static int PTI_COLOR_BG_HL = 77;

    /**
     * The color of the background of this pti bar.
     */
    public final static int PTI_COLOR_BDR = 78;

    /**
     * The font to render the text of this ticker.
     */
    public final static int PTI_FONT = 79;

    /**
     * These images are used to render a background for the pti bar. If isn't 
     * specified or cannot be loaded, solid fill color is used instead of 
     * background image.
     */
    public final static int PTI_IMAGE_BG = 80;

    /**
     * Left scrolling indicator for PTI bar
     */
    public final static int PTI_LEFT_ARROW = 81;

    /**
     * Right scrolling indicator for PTI bar
     */
    public final static int PTI_RIGHT_ARROW = 82;

    /**
     * The text margin in Input Mode Indicator (in pixels).
     */
    public final static int INPUT_MODE_MARGIN = 83;

    /**
     * The background fill color for this pti bar if its background image 
     * isn't specified or cannot be loaded.
     */
    public final static int INPUT_MODE_COLOR_BG = 84;

    /**
     * The foreground color for rendering the text of this Input Mode 
     * Indicator.
     */
    public final static int INPUT_MODE_COLOR_FG = 85;

    /**
     * The color of the background of this Input Mode Indicator.
     */
    public final static int INPUT_MODE_COLOR_BDR = 86;

    /**
     * The font to render the text of this Input Mode Indicator.
     */
    public final static int INPUT_MODE_FONT = 87;

    /**
     * These images are used to render a background for the Input Mode 
     * Indicator. If isn't specified or cannot be loaded, solid fill color is 
     * used instead of background image.
     */
    public final static int INPUT_MODE_IMAGE_BG = 88;

    /**
     * The overall height of the title component, including its background, 
     * title text, etc.
     */
    public final static int TITLE_HEIGHT = 89;

    /**
     * The margin (in pixels) to leave on either side of the layer when 
     * rendering its text.
     */
    public final static int TITLE_MARGIN = 90;

    /**
     * Specifies the horizontal alignment of the text, can be Graphics.LEFT, 
     * RIGHT, or HCENTER.
     */
    public final static int TITLE_TEXT_ALIGN_X = 91;

    /**
     * Specifies the alignment property of the drop shadow on the title text. 
     * This should be a combination value such as Graphics.TOP | 
     * Graphics.LEFT or Graphics.RIGHT | Graphics.BOTTOM
     */
    public final static int TITLE_TEXT_SHD_ALIGN = 92;

    /**
     * The foreground color to use when rendering the title text.
     */
    public final static int TITLE_COLOR_FG = 93;

    /**
     * The 'shadow' color to use when rendering the title text. The title's 
     * shadow is a drop shadow of the text appearing behind the foreground 
     * color text. If this color is equal to that of the foreground color, no 
     * drop shadow will be drawn.
     */
    public final static int TITLE_COLOR_FG_SHD = 94;

    /**
     * A background fill color to use if there is no background image.
     */
    public final static int TITLE_COLOR_BG = 95;

    /**
     * The Font to use when rendering the title text.
     */
    public final static int TITLE_FONT = 96;

    /**
     * The 3 piece background image to be used in the 'title bar'. Piece 0: 
     * Left edge of the titlebar. Piece 1: Middle tile of the titlebar. Piece 
     * 2: Right edge of the titlebar. These images are used to render a 
     * background for the titlebar. If the combined widths of the left, 
     * middle, and right images is less than the overall width of the 
     * titlebar, the middle image will be tiled horizontally to make up the 
     * difference. If isn't specified or cannot be loaded, either the tile 
     * image or solid fill color is used instead of background image.
     */
    public final static int TITLE_IMAGE_BG = 97;

    /**
     * The overall width of an Alert.
     */
    public final static int ALERT_WIDTH = 98;

    /**
     * The height for an Alert. Alerts taller than this height will require 
     * scrolling.
     */
    public final static int ALERT_HEIGHT = 99;

    /**
     * The horizontal alignment of an Alert on the screen. Valid values are 
     * Graphics.LEFT, Graphics.HCENTER, and Graphics.RIGHT.
     */
    public final static int ALERT_ALIGN_X = 100;

    /**
     * The vertical alignment of an Alert on the screen. Valid values are 
     * Graphics.TOP, Graphics.VCENTER, and Graphics.BOTTOM.
     */
    public final static int ALERT_ALIGN_Y = 101;

    /**
     * The width (in pixels) for the horizontal margins for an alert.
     */
    public final static int ALERT_MARGIN_H = 102;

    /**
     * The height (in pixels) for the vertical margins for an alert.
     */
    public final static int ALERT_MARGIN_V = 103;

    /**
     * The alignment directive for the title in the title bar. Valid values 
     * are Graphics.LEFT, Graphics.HCENTER, or Graphics.RIGHT. This value 
     * will be combined with Graphics.TOP to locate the title in the Alert's 
     * title bar, centered vertically.
     */
    public final static int ALERT_TITLE_ALIGN = 104;

    /**
     * The overall height of the 'title bar' of the Alert. This height 
     * accounts for the area at the top of the Alert where the icon and the 
     * title are located. The rest of the alert constitutes the 'body' of the 
     * Alert.
     */
    public final static int ALERT_TITLE_HEIGHT = 105;

    /**
     * The width of the margin (in pixels) on either side of the title in an 
     * Alert. If the title exceeds the available width, it will be clipped at 
     * the margins.
     */
    public final static int ALERT_TITLE_MARGIN = 106;

    /**
     * The default title for an 'info' type alert.
     */
    public final static int ALERT_TEXT_TITLE_INFO = 107;

    /**
     * The default title for a 'warning' type alert.
     */
    public final static int ALERT_TEXT_TITLE_WARN = 108;

    /**
     * The default title for an 'error' type alert.
     */
    public final static int ALERT_TEXT_TITLE_ERRR = 109;

    /**
     * The default title for an 'alarm' type alert.
     */
    public final static int ALERT_TEXT_TITLE_ALRM = 110;

    /**
     * The default title for a 'confirm' type alert.
     */
    public final static int ALERT_TEXT_TITLE_CNFM = 111;

    /**
     * The amount of padding (in pixels) between some elements on a row (such 
     * as the icon and the title).
     */
    public final static int ALERT_PAD_HORIZ = 112;

    /**
     * The amount of padding (in pixels) between two rows of elements.
     */
    public final static int ALERT_PAD_VERT = 113;

    /**
     * The amount of pixels to scroll the contents of the Alert at one time.
     */
    public final static int ALERT_SCROLL_AMOUNT = 114;

    /**
     * The timeout (in milliseconds) for an Alert.
     */
    public final static int ALERT_TIMEOUT = 115;

    /**
     * The background fill color to be used for Alerts when the background 
     * image cannot be loaded or does not exist.
     */
    public final static int ALERT_COLOR_BG = 116;

    /**
     * The foreground color for the Alert's title text.
     */
    public final static int ALERT_COLOR_TITLE = 117;

    /**
     * The foreground color for the Alert's contents.
     */
    public final static int ALERT_COLOR_FG = 118;

    /**
     * The font to use to render the Alert's title text.
     */
    public final static int ALERT_FONT_TITLE = 119;

    /**
     * The font to use to render the Alert's contents.
     */
    public final static int ALERT_FONT_TEXT = 120;

    /**
     * A 9-piece background to use for an Alert. Piece 0: Top left corner of 
     * the top-bar. Piece 1: Middle tile for the top-bar. Piece 2: Top right 
     * corner of the top-bar. Piece 3: Left edge of the middle-bar. Piece 4: 
     * Middle tile for the middle-bar. Piece 5: Right edge of the middle-bar. 
     * Piece 6: Bottom left corner of the bottom-bar. Piece 7: Middle tile 
     * for the bottom-bar. Piece 8: Bottom right corner of the bottom-bar. 
     * The top-bar is the area where the Alert icon and title are drawn. If 
     * there is no Alert title, the top-bar may be very short, and serves to 
     * visually cap the top of the Alert. The middle-bar is the background 
     * 'tile' for a row of the middle of an Alert. It will be repeatedly 
     * drawn vertically to accommodate the height of the Alert. The 
     * bottom-bar is the bottom border of the Alert and serves to visually 
     * cap the bottom of the Alert. All three bars are divided into three 
     * pieces, a left, a middle, and a right. If the combined widths of the 
     * left, middle, and right images is less than the overall width of the 
     * Alert, the middle image will be tiled horizontally to make up the 
     * difference. If isn't specified or cannot be loaded, solid fill color 
     * is used instead of image background.
     */
    public final static int ALERT_IMAGE_BG = 121;

    /**
     * The icon for an 'Information' type Alert.
     */
    public final static int ALERT_IMAGE_ICON_INFO = 122;

    /**
     * The icon for an 'Warning' type Alert.
     */
    public final static int ALERT_IMAGE_ICON_WARN = 123;

    /**
     * The icon for an 'Error' type Alert.
     */
    public final static int ALERT_IMAGE_ICON_ERRR = 124;

    /**
     * The icon for an 'Alarm' type Alert.
     */
    public final static int ALERT_IMAGE_ICON_ALRM = 125;

    /**
     * The icon for an 'Confirmation' type Alert.
     */
    public final static int ALERT_IMAGE_ICON_CNFM = 126;

    /**
     * The width of the continuous gauge. This is the width of the UI design. 
     * It does not include label support which may be provided automatically 
     * in addition to this measurement.
     */
    public final static int BUSYCRSR_WIDTH = 127;

    /**
     * The height of the continuous gauge. This is the height of the UI 
     * design. It does not include label support which may be provided 
     * automatically in addition to this measurement.
     */
    public final static int BUSYCRSR_HEIGHT = 128;

    /**
     * The number of frames that contribute to the animation of this 
     * continuous gauge.
     */
    public final static int BUSYCRSR_NUM_FRAMES = 129;

    /**
     * The 'x' coordinate of the animation region of the continuous gauge as 
     * measured in the gauge's own coordinate space, where the frames get 
     * painted. If the animation is small in size, the frames could be of 
     * that small size and given this 'x' co-ordinate, the frames would get 
     * painted there. The frames will be placed with their upper left corner 
     * at this coordinate.
     */
    public final static int BUSYCRSR_FRAME_X = 130;

    /**
     * The 'y' coordinate of the animation region of the continuous gauge as 
     * measured in the gauge's own coordinate space, where the frames get 
     * painted. If the animation is small in size, the frames could be of 
     * that small size and given this 'y' co-ordinate, the frames would get 
     * painted there. The frames will be placed with their upper left corner 
     * at this coordinate.
     */
    public final static int BUSYCRSR_FRAME_Y = 131;

    /**
     * The sequence in which the frames should be animated in this continuous 
     * gauge.
     */
    public final static int BUSYCRSR_FRAME_SEQU = 132;

    /**
     * The image to be used for the background body of the continuous gauge, 
     * without any animation related images.
     */
    public final static int BUSYCRSR_IMAGE_BG = 133;

    /**
     * All the images (frames), total BUSYCRSR_NUM_FRAMES images, that 
     * contribute to the animation of this continuous gauge.
     */
    public final static int BUSYCRSR_IMAGE_FRAME = 134;

    /**
     * The maximum width for an application-supplied image for a choice 
     * element.
     */
    public final static int CHOICE_WIDTH_IMAGE = 135;

    /**
     * The maximum height for an application-supplied image for a choice 
     * element.
     */
    public final static int CHOICE_HEIGHT_IMAGE = 136;

    /**
     * In a popup choice, the width of the scrollbar.
     */
    public final static int CHOICE_WIDTH_SCROLL = 137;

    /**
     * In a popup choice, the width of the thumb of the scrollbar.
     */
    public final static int CHOICE_WIDTH_THUMB = 138;

    /**
     * In a popup choice, the height of the thumb of the scrollbar.
     */
    public final static int CHOICE_HEIGHT_THUMB = 139;

    /**
     * The horizontal padding (in pixels) to use between elements on the same 
     * line.
     */
    public final static int CHOICE_PAD_H = 140;

    /**
     * The vertical padding (in pixels) to use between elements on the same 
     * line.
     */
    public final static int CHOICE_PAD_V = 141;

    /**
     * The foreground color for this popup choice.
     */
    public final static int CHOICE_COLOR_FG = 142;

    /**
     * The background color for this popup choice.
     */
    public final static int CHOICE_COLOR_BG = 143;

    /**
     * The border color for this popup choice.
     */
    public final static int CHOICE_COLOR_BRDR = 144;

    /**
     * The border shadow color for this popup choice.
     */
    public final static int CHOICE_COLOR_BRDR_SHD = 145;

    /**
     * The color of the scrollbar on a popup choice.
     */
    public final static int CHOICE_COLOR_SCROLL = 146;

    /**
     * The color of the scrollbar thumb on a popup choice.
     */
    public final static int CHOICE_COLOR_THUMB = 147;

    /**
     * The font to use for choice elements.
     */
    public final static int CHOICE_FONT = 148;

    /**
     * The font to use for the currently focused choice element.
     */
    public final static int CHOICE_FONT_FOCUS = 149;

    /**
     * The pair of images for radio buttons. Piece 0: The image for the 'off' 
     * radio button. Piece 1: The image for the 'on' radio button. Both 
     * images should be of the same dimensions.
     */
    public final static int CHOICE_IMAGE_RADIO = 150;

    /**
     * The pair of images for checkboxes. Piece 0: The image for the 
     * 'unchecked' checkbox. Piece 1: The image for the 'checked' checkbox. 
     * Both images should be of the same dimensions.
     */
    public final static int CHOICE_IMAGE_CHKBX = 151;

    /**
     * A 9 piece image background for a popup choicegroup. Piece 0: Top left 
     * corner of the top-bar. Piece 1: Middle tile for the top-bar. Piece 2: 
     * Top right corner of the top-bar. Piece 3: Left edge of the middle-bar. 
     * Piece 4: Middle tile for the middle-bar. Piece 5: Right edge of the 
     * middle-bar. Piece 6: Bottom left corner of the bottom-bar. Piece 7: 
     * Middle tile for the bottom-bar. Piece 8: Bottom right corner of the 
     * bottom-bar. The top-bar is the top border for the popup. The 
     * middle-bar is the left/right borders as well as the center tile for 
     * the popup. The bottom-bar is the bottom border of the popup. All three 
     * bars are divided into three pieces, a left, a middle, and a right. If 
     * the combined widths of the left, middle, and right images is less than 
     * the overall width of the datefield, the middle image will be tiled 
     * horizontally to make up the difference. If isn't specified or cannot 
     * be loaded, solid fill color is used instead of background image.
     */
    public final static int CHOICE_IMAGE_BG = 152;

    /**
     * A 9 piece image background to use to draw the 'popup' button for popup 
     * choicegroup. Once the button background is drawn, the icon will be 
     * placed in its center. Piece 0: Top left corner of the top-bar. Piece 
     * 1: Middle tile for the top-bar. Piece 2: Top right corner of the 
     * top-bar. Piece 3: Left edge of the middle-bar. Piece 4: Middle tile 
     * for the middle-bar. Piece 5: Right edge of the middle-bar. Piece 6: 
     * Bottom left corner of the bottom-bar. Piece 7: Middle tile for the 
     * bottom-bar. Piece 8: Bottom right corner of the bottom-bar. All three 
     * bars are divided into three pieces, a left, a middle, and a right. If 
     * the combined widths of the left, middle, and right images is less than 
     * the overall width of the background, the middle image will be tiled 
     * horizontally to make up the difference. If isn't specified or cannot 
     * be loaded, solid fill color is used instead of background image.
     */
    public final static int CHOICE_IMAGE_BTN_BG = 153;

    /**
     * The image to use to indicate the drop-down nature of the popup choice, 
     * typically a 'down arrow'.
     */
    public final static int CHOICE_IMAGE_BTN_ICON = 154;

    /**
     * The 9 piece image background to use for the background of a popup 
     * choice window. Piece 0: Top left corner of the top-bar. Piece 1: 
     * Middle tile for the top-bar. Piece 2: Top right corner of the top-bar. 
     * Piece 3: Left edge of the middle-bar. Piece 4: Middle tile for the 
     * middle-bar. Piece 5: Right edge of the middle-bar. Piece 6: Bottom 
     * left corner of the bottom-bar. Piece 7: Middle tile for the 
     * bottom-bar. Piece 8: Bottom right corner of the bottom-bar. All three 
     * bars are divided into three pieces, a left, a middle, and a right. If 
     * the combined widths of the left, middle, and right images is less than 
     * the overall width of the background, the middle image will be tiled 
     * horizontally to make up the difference. If isn't specified or cannot 
     * be loaded, solid fill color is used instead of background image.
     */
    public final static int CHOICE_IMAGE_POPUP_BG = 155;

    /**
     * The height (in pixels) of the popup date editor.
     */
    public final static int DATEEDITOR_HEIGHT = 156;

    /**
     * The maximum height allowed for internal popups, like month, year, 
     * hours, etc.
     */
    public final static int DATEEDITOR_HEIGHT_POPUPS = 157;

    /**
     * The width (in pixels) of the popup date editor in 'date' mode.
     */
    public final static int DATEEDITOR_WIDTH_D = 158;

    /**
     * The width (in pixels) of the popup date editor in 'time' mode.
     */
    public final static int DATEEDITOR_WIDTH_T = 159;

    /**
     * The width (in pixels) of the popup date editor in 'date/time' mode.
     */
    public final static int DATEEDITOR_WIDTH_DT = 160;

    /**
     * The background color of the popup editor (if the background image 
     * either can't be found or loaded).
     */
    public final static int DATEEDITOR_COLOR_BG = 161;

    /**
     * The background color for the popups, such as hours, minutes, month, 
     * etc.
     */
    public final static int DATEEDITOR_COLOR_POPUPS_BG = 162;

    /**
     * The border color of the popup editor (if the background image either 
     * can't be found or loaded).
     */
    public final static int DATEEDITOR_COLOR_BRDR = 163;

    /**
     * The color of the traverse indicator within the editor popup.
     */
    public final static int DATEEDITOR_COLOR_TRAV_IND = 164;

    /**
     * The color to draw the hands of the clock.
     */
    public final static int DATEEDITOR_COLOR_CLK_LT = 165;

    /**
     * The shadow color to draw the hands of the clock.
     */
    public final static int DATEEDITOR_COLOR_CLK_DK = 166;

    /**
     * The font to use for rendering options inside the popups, like hours, 
     * minutes, month, etc.
     */
    public final static int DATEEDITOR_FONT_POPUPS = 167;

    /**
     * The 9 piece image background to use to draw the background of the 
     * popup editor.
     */
    public final static int DATEEDITOR_IMAGE_BG = 168;

    /**
     * The background image for the drop-down month selector.
     */
    public final static int DATEEDITOR_IMAGE_MON_BG = 169;

    /**
     * The background image for the drop-down year selector.
     */
    public final static int DATEEDITOR_IMAGE_YR_BG = 170;

    /**
     * The background image for rendering the calendar.
     */
    public final static int DATEEDITOR_IMAGE_CAL_BG = 171;

    /**
     * This is a single image which holds all the dates as graphics. The 
     * image is arranged vertically, starting with 1 and going to 31. Each 
     * digit is equal height and centered in the image.
     */
    public final static int DATEEDITOR_IMAGE_DATES = 172;

    /**
     * This is a single image which holds all the days of week as graphics. 
     * The image is arranged horizontally, starting with 1 and going to 7. 
     * Each letter is equal height and centered in the image.
     */
    public final static int DATEEDITOR_IMAGE_DAYS = 173;

    /**
     * The background image for the drop-down hour/minute selected.
     */
    public final static int DATEEDITOR_IMAGE_TIME_BG = 174;

    /**
     * Two radio button images for the am/pm setting. The first image is the 
     * unselected radio button and the second image is the selected radio 
     * image.
     */
    public final static int DATEEDITOR_IMAGE_RADIO = 175;

    /**
     * This is a single image which holds the graphics for the am/pm text. 
     * The image contains the am and pm graphic arranged horizontally such 
     * that the left half of the image is the am graphic and the right half 
     * of the image is the pm graphic.
     */
    public final static int DATEEDITOR_IMAGE_AMPM = 176;

    /**
     * The background image for the clock.
     */
    public final static int DATEEDITOR_IMAGE_CLOCK_BG = 177;

    /**
     * The horizontal padding (in pixels) between the left and right borders 
     * of a datefield and its contents.
     */
    public final static int DATEFIELD_PAD_H = 178;

    /**
     * The vertical padding (in pixels) between the top and bottom borders of 
     * a text component and its contents.
     */
    public final static int DATEFIELD_PAD_V = 179;

    /**
     * For the 'popup' button (with a non-existing image background), the 
     * width in pixels of the line border to draw around the button.
     */
    public final static int DATEFIELD_BTN_BRDR_W = 180;

    /**
     * The font to use for the text in this datefield.
     */
    public final static int DATEFIELD_FONT = 181;

    /**
     * The foreground color for this datefield.
     */
    public final static int DATEFIELD_COLOR_FG = 182;

    /**
     * The background color for this datefield.
     */
    public final static int DATEFIELD_COLOR_BG = 183;

    /**
     * The border color for this datefield.
     */
    public final static int DATEFIELD_COLOR_BRDR = 184;

    /**
     * The 'light' line border color for the 'popup' button (with a 
     * non-existing image background).
     */
    public final static int DATEFIELD_COLOR_BRDR_LT = 185;

    /**
     * The 'dark' line border color for the 'popup' button (with a 
     * non-existing image background).
     */
    public final static int DATEFIELD_COLOR_BRDR_DK = 186;

    /**
     * The border shadow color for this datefield.
     */
    public final static int DATEFIELD_COLOR_BRDR_SHD = 187;

    /**
     * A 9 piece image background for a datefield. Piece 0: Top left corner 
     * of the top-bar. Piece 1: Middle tile for the top-bar. Piece 2: Top 
     * right corner of the top-bar. Piece 3: Left edge of the middle-bar. 
     * Piece 4: Middle tile for the middle-bar. Piece 5: Right edge of the 
     * middle-bar. Piece 6: Bottom left corner of the bottom-bar. Piece 7: 
     * Middle tile for the bottom-bar. Piece 8: Bottom right corner of the 
     * bottom-bar. The top-bar is the top border for the datefield. The 
     * middle-bar is the left/right borders as well as the center tile for 
     * the datefield. The bottom-bar is the bottom border of the datefield. 
     * All three bars are divided into three pieces, a left, a middle, and a 
     * right. If the combined widths of the left, middle, and right images is 
     * less than the overall width of the datefield, the middle image will be 
     * tiled horizontally to make up the difference. If isn't specified or 
     * cannot be loaded, solid fill color is used instead of background image.
     */
    public final static int DATEFIELD_IMAGE_BG = 188;

    /**
     * A 9 piece image background for the 'popup' button which holds the 
     * specific icon for this datefield. Piece 0: Top left corner of the 
     * top-bar. Piece 1: Middle tile for the top-bar. Piece 2: Top right 
     * corner of the top-bar. Piece 3: Left edge of the middle-bar. Piece 4: 
     * Middle tile for the middle-bar Piece 5: Right edge of the middle-bar. 
     * Piece 6: Bottom left corner of the bottom-bar. Piece 7: Middle tile 
     * for the bottom-bar. Piece 8: Bottom right corner of the bottom-bar. 
     * All three bars are divided into three pieces, a left, a middle, and a 
     * right. If the combined widths of the left, middle, and right images is 
     * less than the overall width needed for the button, the middle image 
     * will be tiled horizontally to make up the difference. If isn't 
     * specified or cannot be loaded, solid fill color is used instead of 
     * background image.
     */
    public final static int DATEFIELD_IMAGE_BTN_BG = 189;

    /**
     * An icon for the popup button for a date only datefield.
     */
    public final static int DATEFIELD_IMAGE_ICON_DATE = 190;

    /**
     * An icon for the popup button for a time only datefield.
     */
    public final static int DATEFIELD_IMAGE_ICON_TIME = 191;

    /**
     * An icon for the popup button for a date and time datefield.
     */
    public final static int DATEFIELD_IMAGE_ICON_DATETIME = 192;

    /**
     * The orientation of the interactive gauge. The value indicates the 
     * directionality of the gauge as it goes from 0 to its max value. Valid 
     * values are Skin.LEFT, Skin.RIGHT, Skin.UP, and Skin.DOWN. A value of 
     * Skin.RIGHT, for example, would indicate a gauge which increased in 
     * value from left to right.
     */
    public final static int GAUGE_ORIENT = 193;

    /**
     * The width of the interactive gauge. This is the width of the UI 
     * design. It does not include label support which may be provided 
     * automatically in addition to this measurement.
     */
    public final static int GAUGE_WIDTH = 194;

    /**
     * The height of the interactive gauge. This is the height of the UI 
     * design. It does not include label support which may be provided 
     * automatically in addition to this measurement.
     */
    public final static int GAUGE_HEIGHT = 195;

    /**
     * The 'x' coordinate of the meter image for this interactive gauge, as 
     * measured in the gauge's own coordinate space. The meter image will be 
     * placed with its upper left corner at this coordinate.
     */
    public final static int GAUGE_METER_X = 196;

    /**
     * The 'y' coordinate of the meter image for this interactive gauge, as 
     * measured in the gauge's own coordinate space. The meter image will be 
     * placed with its upper left corner at this coordinate.
     */
    public final static int GAUGE_METER_Y = 197;

    /**
     * The 'x' coordinate of the increase button for this interactive gauge, 
     * as measured in the gauge's own coordinate space. The button will be 
     * placed with its upper left corner at this coordinate.
     */
    public final static int GAUGE_INC_BTN_X = 198;

    /**
     * The 'y' coordinate of the increase button for this interactive gauge, 
     * as measured in the gauge's own coordinate space. The button will be 
     * placed with its upper left corner at this coordinate.
     */
    public final static int GAUGE_INC_BTN_Y = 199;

    /**
     * The 'x' coordinate of the decrease button for this interactive gauge, 
     * as measured in the gauge's own coordinate space. The button will be 
     * placed with its upper left corner at this coordinate.
     */
    public final static int GAUGE_DEC_BTN_X = 200;

    /**
     * The 'y' coordinate of the decrease button for this interactive gauge, 
     * as measured in the gauge's own coordinate space. The button will be 
     * placed with its upper left corner at this coordinate.
     */
    public final static int GAUGE_DEC_BTN_Y = 201;

    /**
     * The 'x' coordinate defining the upper left corner of the bounding box 
     * which contains the interactive gauge's value.
     */
    public final static int GAUGE_VALUE_X = 202;

    /**
     * The 'y' coordinate defining the upper left corner of the bounding box 
     * which contains the interactive gauge's value.
     */
    public final static int GAUGE_VALUE_Y = 203;

    /**
     * The maximum allowable width of the space available to render the value 
     * of the interactive gauge. For example, a gauge may be able to easily 
     * render the value of 45, but it may not be large enough to render 
     * 4500000. The height of the gauge value is determined by the image used 
     * to provide the numbers.
     */
    public final static int GAUGE_VALUE_WIDTH = 204;

    /**
     * The image to be used for the background body of the interactive gauge, 
     * without the meter images, value, or control buttons.
     */
    public final static int GAUGE_IMAGE_BG = 205;

    /**
     * The image to be used for the actual meter of the interactive gauge, in 
     * an 'empty' (ie value==0) state.
     */
    public final static int GAUGE_IMAGE_MTR_EMPTY = 206;

    /**
     * The image to be used for the actual meter of the interactive gauge, in 
     * a 'full' (ie value==max) state.
     */
    public final static int GAUGE_IMAGE_MTR_FULL = 207;

    /**
     * The image to be used for the 'increase' button for this interactive 
     * gauge.
     */
    public final static int GAUGE_IMAGE_INC_BTN = 208;

    /**
     * The image to be used for the 'decrease' button for this interactive 
     * gauge.
     */
    public final static int GAUGE_IMAGE_DEC_BTN = 209;

    /**
     * The image to be used to paint the value of this interactive gauge in a 
     * numerical form. The image should consist of the highlighted/normal 
     * values 0-9, and a trailing non-highlighted/grayish 0, that could be 
     * used to display a value of 0. The digits should be equally spaced in a 
     * horizontal layout. Each digit should be the same width, equal to the 
     * width of the overall image divided by 11 (the total number of digits 
     * in the image, 0-9,0).
     */
    public final static int GAUGE_IMAGE_VALUES = 210;

    /**
     * The background fill color of a hyperlink which has input focus.
     */
    public final static int IMAGEITEM_COLOR_BG_LNK_FOC = 211;

    /**
     * The background fill color of a button.
     */
    public final static int IMAGEITEM_COLOR_BG_BTN = 212;

    /**
     * The 'light' line border color of a button which does not have an image 
     * background.
     */
    public final static int IMAGEITEM_COLOR_BORDER_LT = 213;

    /**
     * The 'dark' line border color of a button which does not have an image 
     * background.
     */
    public final static int IMAGEITEM_COLOR_BORDER_DK = 214;

    /**
     * The padding between the sides of an image and its outer border when it 
     * is a hyperlink.
     */
    public final static int IMAGEITEM_PAD_LNK_H = 215;

    /**
     * The padding between the top and bottom of an image and its outer 
     * border when it is a hyperlink.
     */
    public final static int IMAGEITEM_PAD_LNK_V = 216;

    /**
     * The padding between the sides of an image and its outer border when it 
     * is a button.
     */
    public final static int IMAGEITEM_PAD_BTN_H = 217;

    /**
     * The padding between the top and bottom of an image and its outer 
     * border when it is a button.
     */
    public final static int IMAGEITEM_PAD_BTN_V = 218;

    /**
     * The thickness of the line border drawn around the image when it is a 
     * button (and its background image is not available).
     */
    public final static int IMAGEITEM_BTN_BORDER_W = 219;

    /**
     * The 'horizontal' portion of the border graphic to draw around an image 
     * when it is a hyperlink.
     */
    public final static int IMAGEITEM_IMAGE_LNK_H = 220;

    /**
     * The 'vertical' portion of the border graphic to draw around an image 
     * when it is a hyperlink.
     */
    public final static int IMAGEITEM_IMAGE_LNK_V = 221;

    /**
     * The 9 piece image background for an image which is a button. Piece 0: 
     * Top left corner of the button. Piece 1: Middle tile for the top of the 
     * button. Piece 2: Top right corner of the button. Piece 3: Left edge of 
     * the button. Piece 4: Middle tile for the button. Piece 5: Right edge 
     * of the button. Piece 6: Bottom left corner of the button. Piece 7: 
     * Middle tile for the bottom of the button. Piece 8: Bottom right corner 
     * of the button. All three rows are divided into three pieces, a left, a 
     * middle, and a right. If the combined widths of the left, middle, and 
     * right images is less than the overall width of the button, the middle 
     * image will be tiled horizontally to make up the difference. If isn't 
     * specified or cannot be loaded, solid fill color and line border are 
     * used instead of background image.
     */
    public final static int IMAGEITEM_IMAGE_BUTTON = 222;

    /**
     * Height of virtual keyboard
     */
    public final static int KEYBOARD_HEIGHT = 223;

    /**
     * The persent of the board which virtual keyboard fill
     */
    public final static int KEYBOARD_COEFFICIENT = 224;

    /**
     * Image for virtual keyboard key
     */
    public final static int KEYBOARD_KEY = 225;

    /**
     * Image for virtual keyboard key
     */
    public final static int KEYBOARD_BTN_BACKSPACE = 226;

    /**
     * Image for virtual keyboard key
     */
    public final static int KEYBOARD_BTN_CAPS = 227;

    /**
     * Image for virtual keyboard key
     */
    public final static int KEYBOARD_BTN_ENTER = 228;

    /**
     * Image for virtual keyboard key
     */
    public final static int KEYBOARD_BTN_ALPHA_MODE = 229;

    /**
     * Image for virtual keyboard key
     */
    public final static int KEYBOARD_BTN_SYMBOL_MODE = 230;

    /**
     * Image for virtual keyboard key
     */
    public final static int KEYBOARD_BTN_NUMERIC_MODE = 231;

    /**
     * Background for virtual keyboard
     */
    public final static int KEYBOARD_BG = 232;

    /**
     * Background color for virtual keyboard
     */
    public final static int KEYBOARD_COLOR_BG = 233;

    /**
     * Image for virtual keyboard key with selected up arrow
     */
    public final static int KEYBOARD_BTN_UP_SEL = 234;

    /**
     * Image for virtual keyboard key with unselected up arrow
     */
    public final static int KEYBOARD_BTN_UP_UN = 235;

    /**
     * Image for virtual keyboard key with selected left arrow
     */
    public final static int KEYBOARD_BTN_LEFT_SEL = 236;

    /**
     * Image for virtual keyboard key with unselected left arrow
     */
    public final static int KEYBOARD_BTN_LEFT_UN = 237;

    /**
     * Image for virtual keyboard key with selected middle icon
     */
    public final static int KEYBOARD_BTN_MID_SEL = 238;

    /**
     * Image for virtual keyboard key with unselected middle icon
     */
    public final static int KEYBOARD_BTN_MID_UN = 239;

    /**
     * Image for virtual keyboard key with selected right arrow
     */
    public final static int KEYBOARD_BTN_RIGHT_SEL = 240;

    /**
     * Image for virtual keyboard key with unselected right arrow
     */
    public final static int KEYBOARD_BTN_RIGHT_UN = 241;

    /**
     * Image for virtual keyboard key with selected down arrow
     */
    public final static int KEYBOARD_BTN_DOWN_SEL = 242;

    /**
     * Image for virtual keyboard key with unselected down arrow
     */
    public final static int KEYBOARD_BTN_DOWN_UN = 243;

    /**
     * Virtual keyboard font
     */
    public final static int KEYBOARD_FONT = 244;

    /**
     * Radius of finger if finger support is on, if finger support is off 
     * than this constant should be 0
     */
    public final static int TOUCH_RADIUS = 245;

    /**
     * The maximum overall width of the system menu component.
     */
    public final static int MENU_WIDTH = 246;

    /**
     * The maximum overall height of the system menu component.
     */
    public final static int MENU_HEIGHT = 247;

    /**
     * The alignment property of the system menu on the 'x' axis. Valid 
     * values are Graphics.LEFT, Graphics.RIGHT, or Graphics.HCENTER. A value 
     * of LEFT, for example, would align the system menu on the left edge of 
     * the screen. A value of RIGHT would align the menu along the right edge 
     * of the screen, and a value of HCENTER would horizontally center the 
     * system menu on the screen.
     */
    public final static int MENU_ALIGN_X = 248;

    /**
     * The alignment property of the system menu on the 'y' axis. Valid 
     * values are Graphics.TOP, Graphics.BOTTOM, or Graphics.VCENTER. A value 
     * of TOP, for example, would align the system menu along the top edge of 
     * the screen. A value of BOTTOM would align the menu along the bottom 
     * edge of the screen, and a value of VCENTER would vertically center the 
     * system menu on the screen.
     */
    public final static int MENU_ALIGN_Y = 249;

    /**
     * The 'x' coordinate for the anchor point of the system menu title.
     */
    public final static int MENU_TITLE_X = 250;

    /**
     * The 'y' coordinate for the anchor point of the system menu title.
     */
    public final static int MENU_TITLE_Y = 251;

    /**
     * The maximum width allowed for hte system menu title.
     */
    public final static int MENU_TITLE_MAXWIDTH = 252;

    /**
     * The horizontal alignment property of the system menu title around its 
     * anchor point. Valid values are Graphics.LEFT, Graphics.RIGHT, and 
     * Graphics.HCENTER.
     */
    public final static int MENU_TITLE_ALIGN = 253;

    /**
     * The max items this system menu can display at once.
     */
    public final static int MENU_MAX_ITEMS = 254;

    /**
     * The height (in pixels) of each option in the system menu. This height 
     * includes any padding, and is the total height occupied by a single 
     * 'row' which makes up an item. The next item in the menu would have its 
     * origin y-value at ITEM_HEIGHT + 1.
     */
    public final static int MENU_ITEM_HEIGHT = 255;

    /**
     * The offset (in pixels) from the top of the menu to the origin y-value 
     * of the first menu item.
     */
    public final static int MENU_ITEM_TOPOFFSET = 256;

    /**
     * The 'x' coordinate of the starting location for the index of each menu 
     * item. That is, menu items are made up of an index and a string, ie, '5 
     * Help Menu'. This coordinate is for the numeric index of the items. All 
     * item indexes will have the same x coordinate.
     */
    public final static int MENU_ITEM_INDEX_ANCHOR_X = 257;

    /**
     * The 'x' coordinate of the starting location for the string value of 
     * each menu item. That is, menu items are made up of an index and a 
     * string, ie, '5 Help Menu'. This coordinate is for the string value of 
     * the items. All item strings will have the same x coordinate.
     */
    public final static int MENU_ITEM_ANCHOR_X = 258;

    /**
     * A background fill color to use in lieu of using a background image.
     */
    public final static int MENU_COLOR_BG = 259;

    /**
     * A background fill color to use for the currently highlighted menu item 
     * and index in lieu of using a background image.
     */
    public final static int MENU_COLOR_BG_SEL = 260;

    /**
     * The foreground color to use to render the system menu title if one is 
     * used.
     */
    public final static int MENU_COLOR_TITLE = 261;

    /**
     * The non-selected foreground color to use to render the index of each 
     * item on the menu.
     */
    public final static int MENU_COLOR_INDEX = 262;

    /**
     * The foreground color to use to render the index of the selected item 
     * on the menu.
     */
    public final static int MENU_COLOR_INDEX_SEL = 263;

    /**
     * The non-selected foreground color to use to render items on the menu.
     */
    public final static int MENU_COLOR_ITEM = 264;

    /**
     * The foreground color to use to render the selected item on the menu.
     */
    public final static int MENU_COLOR_ITEM_SEL = 265;

    /**
     * The actual title of the system menu, typically 'Menu'. If isn't 
     * specified, then this system menu does not have a title.
     */
    public final static int MENU_TEXT_TITLE = 266;

    /**
     * The Font to use to render the system menu title.
     */
    public final static int MENU_FONT_TITLE = 267;

    /**
     * The Font to use to render all non-selected items and their indexes on 
     * the menu.
     */
    public final static int MENU_FONT_ITEM = 268;

    /**
     * The Font to use to render the currently selected item and its index on 
     * the menu.
     */
    public final static int MENU_FONT_ITEM_SEL = 269;

    /**
     * The 9 piece background image to be used for the system menu. Piece 0: 
     * Top left corner of the top-bar. Piece 1: Middle tile for the top-bar. 
     * Piece 2: Top right corner of the top-bar. Piece 3: Left edge of the 
     * item-bar. Piece 4: Middle tile for the item-bar. Piece 5: Right edge 
     * of the item-bar. Piece 6: Bottom left corner of the bottom-bar. Piece 
     * 7: Middle tile for the bottom-bar. Piece 8: Bottom right corner of the 
     * bottom-bar. The top-bar is the area where the menu title is drawn. If 
     * there is no menu title, the top-bar may be very short, and serves to 
     * visually cap the top of the menu. The item-bar is the background 
     * 'tile' for a single menu item. It will be repeatedly drawn vertically 
     * to accommodate each line for each menu item. The bottom-bar is the 
     * bottom border of the system menu and serves to visually cap the bottom 
     * of the menu. All three bars are divided into three pieces, a left, a 
     * middle, and a right. If the combined widths of the left, middle, and 
     * right images is less than the overall width of the system menu, the 
     * middle image will be tiled horizontally to make up the difference. If 
     * isn't specified or cannot be loaded, solid fill color is used instead 
     * of background image.
     */
    public final static int MENU_IMAGE_BG = 270;

    /**
     * The 3 piece background image for a selected menu item. Piece 0: Left 
     * edge of the selected item-bar. Piece 1: Middle tile of the selected 
     * item-bar. Piece 2: Right edge of the selected item-bar. These images 
     * are used to render a special background for the currently selected 
     * menu item. They should match in height to those images used for the 
     * non-selected item-bar. If the combined widths of the left, middle, and 
     * right images is less than the overall width of the system menu, the 
     * middle image will be tiled horizontally to make up the difference. If 
     * isn't specified or cannot be loaded, solid fill color is used instead 
     * of background image.
     */
    public final static int MENU_IMAGE_ITEM_SEL_BG = 271;

    /**
     * Some menu items may trigger a cascading submenu. In this case the menu 
     * item may be additionally displayed with a directional arrow indicating 
     * the availability of a submenu. This image is the arrow to be displayed 
     * when a menu item is not currently selected.
     */
    public final static int MENU_IMAGE_SUBMENU = 272;

    /**
     * Some menu items may trigger a cascading submenu. In this case the menu 
     * item may be additionally displayed with a directional arrow indicating 
     * the availability of a submenu. This image is the arrow to be displayed 
     * when a menu item is currently selected.
     */
    public final static int MENU_IMAGE_SUBMENU_HL = 273;

    /**
     * The orientation of the progressbar. The value indicates the 
     * directionality of the progressbar as it goes from 0 to its max value. 
     * Valid values are Skin.LEFT, Skin.RIGHT, Skin.UP, and Skin.DOWN. A 
     * value of Skin.RIGHT, for example, would indicate a gauge which 
     * increased in value from left to right.
     */
    public final static int PBAR_ORIENT = 274;

    /**
     * The width of the progressbar. This is the width of the UI design. It 
     * does not include label support which may be provided automatically in 
     * addition to this measurement.
     */
    public final static int PBAR_WIDTH = 275;

    /**
     * The height of the progressbar. This is the height of the UI design. It 
     * does not include label support which may be provided automatically in 
     * addition to this measurement.
     */
    public final static int PBAR_HEIGHT = 276;

    /**
     * The 'x' coordinate of the meter image for this progressbar, as 
     * measured in the gauge's own coordinate space. The meter image will be 
     * placed with its upper left corner at this coordinate.
     */
    public final static int PBAR_METER_X = 277;

    /**
     * The 'y' coordinate of the meter image for this progressbar, as 
     * measured in the gauge's own coordinate space. The meter image will be 
     * placed with its upper left corner at this coordinate.
     */
    public final static int PBAR_METER_Y = 278;

    /**
     * The 'x' coordinate defining the upper left corner of the bounding box 
     * which contains the progressbar's value.
     */
    public final static int PBAR_VALUE_X = 279;

    /**
     * The 'y' coordinate defining the upper left corner of the bounding box 
     * which contains the progressbar's value.
     */
    public final static int PBAR_VALUE_Y = 280;

    /**
     * The maximum allowable width of the space available to render the value 
     * of the progressbar as a percentage. For example, it would range from a 
     * 0% to a 100%, which is the maximum. The actual value of the gauge is 
     * scaled according to the maximum value of the gauge as a percentage. 
     * The height of the gauge value is determined by the image used to 
     * provide the numbers.
     */
    public final static int PBAR_VALUE_WIDTH = 281;

    /**
     * The image to be used for the background body of the progressbar, 
     * without the meter images, value and percentage.
     */
    public final static int PBAR_IMAGE_BG = 282;

    /**
     * The image to be used for the actual meter of this progressbar, in an 
     * 'empty' (ie value==0) state.
     */
    public final static int PBAR_IMAGE_MTR_EMPTY = 283;

    /**
     * The image to be used for the actual meter of this progressbar, in a 
     * 'full' (ie value==max) state.
     */
    public final static int PBAR_IMAGE_MTR_FULL = 284;

    /**
     * The image to be used to paint the value of this progressbar in a 
     * numerical form. The image should consist of the highlighted/normal 
     * values 0-9, and a trailing non-highlighted/grayish 0, that could be 
     * used to display a value of 0. The digits should be equally spaced in a 
     * horizontal layout. Each digit should be the same width, equal to the 
     * width of the overall image divided by 11 (the total number of digits 
     * in the image, 0-9,0).
     */
    public final static int PBAR_IMAGE_VALUES = 285;

    /**
     * The image to be used to clip the percentage sign from. The percentage 
     * sign is displayed right next to the (percentage equivalent) current 
     * value of the progressbar. It consists of 2 percentage signs, one 
     * normal/highlighted and the other showing non-highlighted/grayish 
     * rendering, that could be used when displaying a value of 0%. The 2 
     * signs should be equally spaced in a horizontal layout and be the same 
     * width, equal to the width of the overall image divided by 2 (the total 
     * number of signs in the image).
     */
    public final static int PBAR_IMAGE_PERCENTS = 286;

    /**
     * In button mode, the number of pixels between the sides of the button 
     * and its text.
     */
    public final static int STRINGITEM_PAD_BUTTON_H = 287;

    /**
     * In button mode, the number of pixels between the top and bottom of the 
     * button and its text.
     */
    public final static int STRINGITEM_PAD_BUTTON_V = 288;

    /**
     * In button mode (with a non-existing image background), the width in 
     * pixels of the line border to draw around the button.
     */
    public final static int STRINGITEM_BUTTON_BORDER_W = 289;

    /**
     * The foreground text color of a hyperlink.
     */
    public final static int STRINGITEM_COLOR_FG_LNK = 290;

    /**
     * The foreground text color of a hyperlink which has input focus.
     */
    public final static int STRINGITEM_COLOR_FG_LNK_FOC = 291;

    /**
     * The background fill color of a hyperlink which has input focus. If 
     * this value is -1, there will be no background fill.
     */
    public final static int STRINGITEM_COLOR_BG_LNK_FOC = 292;

    /**
     * The foreground text color of a button.
     */
    public final static int STRINGITEM_COLOR_FG_BTN = 293;

    /**
     * The background fill color of a button (with a non-existing image 
     * background).
     */
    public final static int STRINGITEM_COLOR_BG_BTN = 294;

    /**
     * The 'light' line border color for a button (with a non-existing image 
     * background).
     */
    public final static int STRINGITEM_COLOR_BORDER_LT = 295;

    /**
     * The 'dark' line border color for a button (with a non-existing image 
     * background).
     */
    public final static int STRINGITEM_COLOR_BORDER_DK = 296;

    /**
     * The font to use for a plain StringItem.
     */
    public final static int STRINGITEM_FONT = 297;

    /**
     * The font to use for a hyperlink.
     */
    public final static int STRINGITEM_FONT_LNK = 298;

    /**
     * The font to use for a button.
     */
    public final static int STRINGITEM_FONT_BTN = 299;

    /**
     * The image to use to draw the link underline below a hyperlink.
     */
    public final static int STRINGITEM_IMAGE_LNK = 300;

    /**
     * The 9 piece image background to use for a button. Piece 0: Top left 
     * corner of the button. Piece 1: Middle tile for the top of the button. 
     * Piece 2: Top right corner of the button. Piece 3: Left edge of the 
     * button. Piece 4: Middle tile for the button. Piece 5: Right edge of 
     * the button. Piece 6: Bottom left corner of the button. Piece 7: Middle 
     * tile for the bottom of the button. Piece 8: Bottom right corner of the 
     * button. All three rows are divided into three pieces, a left, a 
     * middle, and a right. If the combined widths of the left, middle, and 
     * right images is less than the overall width of the button, the middle 
     * image will be tiled horizontally to make up the difference. If isn't 
     * specified or cannot be loaded, solid fill color and line border are 
     * used instead of background image.
     */
    public final static int STRINGITEM_IMAGE_BTN = 301;

    /**
     * The horizontal padding (in pixels) between the left and right borders 
     * of a text component and its contents.
     */
    public final static int TEXTFIELD_PAD_H = 302;

    /**
     * The vertical padding (in pixels) between the top and bottom borders of 
     * a text component and its contents.
     */
    public final static int TEXTFIELD_PAD_V = 303;

    /**
     * The size of the inset margin for a full-screen textbox component. This 
     * will create an inset margin on the screen, literally creating a 'box' 
     * onscreen which will contain the text input.
     */
    public final static int TEXTFIELD_BOX_MARGIN = 304;

    /**
     * The width (in pixels) for the caret (input cursor) in a text input 
     * component.
     */
    public final static int TEXTFIELD_WIDTH_CARET = 305;

    /**
     * For auto-scrolling text, this is the rate (in milliseconds) between 
     * updates of the scrolling text. For example, a value of 100 will move 
     * the text ten times in one second.
     */
    public final static int TEXTFIELD_SCRL_RATE = 306;

    /**
     * For auto-scrolling text, the number of pixels to shift the text in a 
     * single update. This value combines with TEXTFIELD_SCRL_RATE to define 
     * auto-scrolling behavior. The text will move a certain number of pixels 
     * every so many milliseconds.
     */
    public final static int TEXTFIELD_SCRL_SPD = 307;

    /**
     * The foreground color for this text component.
     */
    public final static int TEXTFIELD_COLOR_FG = 308;

    /**
     * The background color for this text component.
     */
    public final static int TEXTFIELD_COLOR_BG = 309;

    /**
     * The border color for this text component.
     */
    public final static int TEXTFIELD_COLOR_BRDR = 310;

    /**
     * The border shadow color for this text component.
     */
    public final static int TEXTFIELD_COLOR_BRDR_SHD = 311;

    /**
     * The foreground color for an uneditable text component.
     */
    public final static int TEXTFIELD_COLOR_FG_UE = 312;

    /**
     * The background color for an uneditable text component.
     */
    public final static int TEXTFIELD_COLOR_BG_UE = 313;

    /**
     * The border color for an uneditable text component.
     */
    public final static int TEXTFIELD_COLOR_BRDR_UE = 314;

    /**
     * The border shadow color for an uneditable text component.
     */
    public final static int TEXTFIELD_COLOR_BRDR_SHD_UE = 315;

    /**
     * A 9 piece image background for a text component. Piece 0: Top left 
     * corner of the top-bar. Piece 1: Middle tile for the top-bar. Piece 2: 
     * Top right corner of the top-bar. Piece 3: Left edge of the middle-bar. 
     * Piece 4: Middle tile for the middle-bar. Piece 5: Right edge of the 
     * middle-bar. Piece 6: Bottom left corner of the bottom-bar. Piece 7: 
     * Middle tile for the bottom-bar. Piece 8: Bottom right corner of the 
     * bottom-bar. The top-bar is the top border for the text component. The 
     * middle-bar is the left/right borders as well as the center tile for 
     * the text component. The bottom-bar is the bottom border of the text 
     * component. All three bars are divided into three pieces, a left, a 
     * middle, and a right. If the combined widths of the left, middle, and 
     * right images is less than the overall width of the text component, the 
     * middle image will be tiled horizontally to make up the difference. If 
     * isn't specified or cannot be loaded, solid fill color is used instead 
     * of background image.
     */
    public final static int TEXTFIELD_IMAGE_BG = 316;

    /**
     * A 9 piece image background for an uneditable text component. Piece 0: 
     * Top left corner of the top-bar. Piece 1: Middle tile for the top-bar. 
     * Piece 2: Top right corner of the top-bar. Piece 3: Left edge of the 
     * middle-bar. Piece 4: Middle tile for the middle-bar. Piece 5: Right 
     * edge of the middle-bar. Piece 6: Bottom left corner of the bottom-bar. 
     * Piece 7: Middle tile for the bottom-bar. Piece 8: Bottom right corner 
     * of the bottom-bar. The top-bar is the top border for the text 
     * component. The middle-bar is the left/right borders as well as the 
     * center tile for the text component. The bottom-bar is the bottom 
     * border of the text component. All three bars are divided into three 
     * pieces, a left, a middle, and a right. If the combined widths of the 
     * left, middle, and right images is less than the overall width of the 
     * text component, the middle image will be tiled horizontally to make up 
     * the difference. If isn't specified or cannot be loaded, solid fill 
     * color is used instead of background image.
     */
    public final static int TEXTFIELD_IMAGE_BG_UE = 317;

    /**
     * The width of the incremental updating gauge. This is the width of the 
     * UI design. It does not include label support which may be provided 
     * automatically in addition to this measurement.
     */
    public final static int UPDATEBAR_WIDTH = 318;

    /**
     * The height of the incremental updating gauge. This is the height of 
     * the UI design. It does not include label support which may be provided 
     * automatically in addition to this measurement.
     */
    public final static int UPDATEBAR_HEIGHT = 319;

    /**
     * The number of frames that contribute to the animation of this 
     * incremental updating gauge.
     */
    public final static int UPDATEBAR_NUM_FRAMES = 320;

    /**
     * The 'x' coordinate of the animation region of the incremental updating 
     * gauge as measured in the gauge's own coordinate space, where the 
     * frames get painted. If the animation is small in size, the frames 
     * could be of that small size and given this 'x' co-ordinate, the frames 
     * would get painted there. The frames will be placed with their upper 
     * left corner at this coordinate.
     */
    public final static int UPDATEBAR_FRAME_X = 321;

    /**
     * The 'y' coordinate of the animation region of the incremental updating 
     * gauge as measured in the gauge's own coordinate space, where the 
     * frames get painted. If the animation is small in size, the frames 
     * could be of that small size and given this 'y' co-ordinate, the frames 
     * would get painted there. The frames will be placed with their upper 
     * left corner at this coordinate.
     */
    public final static int UPDATEBAR_FRAME_Y = 322;

    /**
     * The sequence in which the frames should be animated in this 
     * incremental updating gauge.
     */
    public final static int UPDATEBAR_FRAME_SEQU = 323;

    /**
     * The image to be used for the background body of the incremental 
     * updating gauge, without any animation related images.
     */
    public final static int UPDATEBAR_IMAGE_BG = 324;

    /**
     * All the images (frames), total UPDATEBAR_NUM_FRAMES images, that 
     * contribute to the animation of this incremental updating gauge.
     */
    public final static int UPDATEBAR_IMAGE_FRAME = 325;

    /**
     * The alignment property of a button around its anchor point. Its a 
     * sequence of SOFTBTN_NUM_BUTTONS integer numbers. Valid values for this 
     * property would be Graphics.LEFT, Graphics.RIGHT, or Graphics.HCENTER. 
     * This value will be combined with Graphics.TOP to orient the label 
     * around the anchor.
     */
    public final static int SOFTBTN_BUTTON_ALIGN_X = 326;

    /**
     * The maximum width (in pixels) to allow for a button. Its a sequence of 
     * SOFTBTN_NUM_BUTTONS integer numbers.
     */
    public final static int SOFTBTN_BUTTON_MAX_WIDTH = 327;

    /**
     * The x-coordinate for the anchor point of a button. Its a sequence of 
     * SOFTBTN_NUM_BUTTONS integer numbers.
     */
    public final static int SOFTBTN_BUTTON_ANCHOR_X = 328;

    /**
     * The y-coordinate for the anchor point of a button. Its a sequence of 
     * SOFTBTN_NUM_BUTTONS integer numbers.
     */
    public final static int SOFTBTN_BUTTON_ANCHOR_Y = 329;

    /**
     * The background image for the drop-down month selector if 
     * microedition.locale is set to a country where RTL text orientation is 
     * used
     */
    public final static int DATEEDITOR_IMAGE_MON_HE_BG = 330;

    /**
     * The background image for the drop-down day selector if 
     * microedition.locale is set to a country where uses RIGHT-TO-LEFT note 
     * (Hebrew)
     */
    public final static int DATEEDITOR_IMAGE_DAY_HE_BG = 331;

    /**
     * The background image for the drop-down year selector if 
     * microedition.locale is set to a country where uses RIGHT-TO-LEFT note 
     * (Hebrew)
     */
    public final static int DATEEDITOR_IMAGE_YR_HE_BG = 332;

    /**
     * The background image for the drop-down time selector if 
     * microedition.locale is set to a country where uses RIGHT-TO-LEFT note 
     * (Hebrew)
     */
    public final static int DATEEDITOR_IMAGE_TIME_HE_BG = 333;

}
