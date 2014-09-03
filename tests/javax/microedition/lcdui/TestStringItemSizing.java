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

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestStringItemSizing implements Testlet {
    TestHarness th;

    /**
     * Maximum width available for items.
     */
    private final int MAX_WIDTH = 220;

    // Parameter order for PLAIN_PRMS, HYPERLINK_PRMS, BUTTON_PRMS arrays
    private final int ITEM_LEFT_PAD   = 0;
    private final int ITEM_RIGHT_PAD  = 1;
    private final int ITEM_TOP_PAD    = 2;
    private final int ITEM_BOTTOM_PAD = 3;

    private final int CONTENT_LEFT_PAD   = 4;
    private final int CONTENT_RIGHT_PAD  = 5;
    private final int CONTENT_TOP_PAD    = 6;
    private final int CONTENT_BOTTOM_PAD = 7;

    private final int LABEL_BODY_HORIZ_PAD = 8;
    private final int LABEL_BODY_VERTICAL_PAD  = 9;

    private final int LABEL_I_WIDTH     = 10;
    private final int LABEL_FONT_HEIGHT = 11;
    private final int TEXT_I_WIDTH      = 12;
    private final int TEXT_FONT_HEIGHT  = 13;


    // Padding, 'i' width and height values for
    // different appearances of StringItem
    private final int PLAIN_PRMS[] = new int[] {4, 4, 4, 4,
						0, 0, 0, 0,
						4, 0,
						3, 13, 3, 15};
    private final int HYPERLINK_PRMS[] = new int[] {4, 4, 4, 4,
						    0, 0, 0, 0,
						    4, 0,
						    3, 13, 3, 15};
    private final int BUTTON_PRMS[] = new int[] {4, 4, 4, 4,
						 5, 5, 5, 5,
						 4, 0,
						 3, 13, 3, 15};

    // StringItem types (in terms of internal layout)
    private final int EMPTY       = 0;
    private final int EMPTY_LABEL = 1; // text can be multiline
    private final int EMPTY_TEXT  = 2; // label can be multiline
    private final int SAME_LINE   = 3; // both text and label present (no \n)
    private final int MULT_LINE   = 4; // both text and label present

    private final String[] appearanceModeNames = new String[] {
	                                          new String("Item.PLAIN"),
						  new String("Item.HYPERLINK"),
						  new String("Item.BUTTON")};

    // The following StringItems are empty or are so narrow that we
    // assume that they can be displayed with word wrapping
    StringItemInfo itemInfos[] = {
	//-------------Empty stringItems ----------------------------------
	new StringItemInfo(new StringItem(null, null), EMPTY,
			   "StringItem(null, null"),
	new StringItemInfo(new StringItem("", ""), EMPTY,
			   "StringItem(\"\", \"\""),
	new StringItemInfo(new StringItem(null, ""), EMPTY,
			   "StringItem(null, \"\""),
	new StringItemInfo(new StringItem("", null), EMPTY,
			   "StringItem(\"\", null"),
	//--------------End of line --------------------------------------
	new StringItemInfo(new StringItem("", "\n"), EMPTY_LABEL,
			   "StringItem(\"\", \"\\n\""),
	new StringItemInfo(new StringItem("\n", ""), EMPTY_TEXT,
			   "StringItem(\"\\n\", \"\""),
	//--------------Empty label - Single line text----------------------
	new StringItemInfo(new StringItem(null, "i"), EMPTY_LABEL,
			   "StringItem(null, \"i\""),
	//--------------Empty text - Single line label----------------------
	new StringItemInfo(new StringItem("i", null), EMPTY_TEXT,
			   "StringItem(\"i\", null"),
	//--------------Narrow label and text on the same line
	new StringItemInfo(new StringItem("i", "i"), SAME_LINE,
			   "StringItem(\"i\", \"i\""),
	//-------------Label or text is multiline---------
	new StringItemInfo(new StringItem("i\ni",""), EMPTY_TEXT,
			   "StringItem(\"i\\ni\", \"\""),
	new StringItemInfo(new StringItem("","i\ni"), EMPTY_LABEL,
			   "StringItem(\"\", \"i\\ni\""),
	//-------------Narrow label and text are multiline
	new StringItemInfo(new StringItem("ii\ni","i\nii"), MULT_LINE,
			   "StringItem(\"ii\\ni\", \"i\\nii\"")
    };

    // the following 2 arrays represent different appearances that
    // a StringItem could be created with (appearance is modified by
    // add/remove commands)
    // each strItem in the itemInfos[].strItem should be tested with
    // all appearances modes from modes[] with a command added or note
    // based on corresponding addCommands[] value
    int modes[] = new int[]{Item.PLAIN, Item.HYPERLINK, Item.BUTTON,
			    Item.PLAIN,Item.HYPERLINK, Item.BUTTON};

    boolean addCommands[] = new boolean[] {false, true, true,
					 true, false, false};

    // The sizing of the following StringItems is available width dependent
    StringItemInfo itemInfosWidthDependent[] = new StringItemInfo[] {
	// ------------- Label and text on the same line
	new StringItemInfo(new StringItem("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
					"iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
					  Item.PLAIN),
			   SAME_LINE,
			   "StringItem(34is, 35is, Item.PLAIN)"),
	new StringItemInfo(new StringItem("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
					"iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
					  Item.HYPERLINK),
			   SAME_LINE,
			   "StringItem(34is, 35is, Item.HYPERLINK)"),
	new StringItemInfo(new StringItem("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
					 "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
					  Item.BUTTON),
			   SAME_LINE,
			   "StringItem(33is, 33is, Item.BUTTON)"),
	new StringItemInfo(new StringItem("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
					 "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
					  Item.HYPERLINK),
			   SAME_LINE,
			   "StringItem(34is, 35is, Item.HYPERLINK) command added"),
	new StringItemInfo(new StringItem("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
					  "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
					  Item.BUTTON),
			   SAME_LINE,
			   "StringItem(33is, 33is, Item.BUTTON) command added"),
	new StringItemInfo(new StringItem("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
					"iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
					  Item.PLAIN),
			   SAME_LINE,
			   "StringItem(34is, 35is, Item.PLAIN) command added"),

	//--------------Label and text on multiple lines
	new StringItemInfo(new StringItem("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
				        "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
					  Item.PLAIN),
			   MULT_LINE,
			   "StringItem(35is, 35is, Item.PLAIN)"),
	new StringItemInfo(new StringItem("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
				        "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
					  Item.HYPERLINK),
			   MULT_LINE,
			   "StringItem(35is, 35is, Item.HYPERLINK)"),
	new StringItemInfo(new StringItem("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
				        "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
					  Item.BUTTON),
			   MULT_LINE,
			   "StringItem(35is, 35is, Item.BUTTON)"),
	new StringItemInfo(new StringItem("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
				        "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
					  Item.HYPERLINK),
			   MULT_LINE,
			   "StringItem(35is, 35is, Item.HYPERLINK) command added"),
	new StringItemInfo(new StringItem("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
				        "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
					  Item.BUTTON),
			   MULT_LINE,
			   "StringItem(35is, 35is, Item.BUTTON) command added"),
	new StringItemInfo(new StringItem("iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
				        "iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii",
					  Item.PLAIN),
			   MULT_LINE,
			   "StringItem(35is, 35is, Item.PLAIN) command added")
	    };

    boolean addCommandsToWidthDepInfos[] =
	new boolean[] {false, false, false, true, true, true,
	               false, false, false, true, true, true};

    // -----------------------------------------------------------------------
    // utilities

    StringItemInfo[] createTestArray() {

	StringItemInfo newItemInfos[] =
	    new StringItemInfo[itemInfos.length*modes.length +
			       itemInfosWidthDependent.length];

	Command c = new Command("", Command.ITEM, 1);

	for (int j, mode = 0;
	     mode < modes.length;
	     mode++) {

	    j = itemInfos.length * mode;

	    for (int i = 0; i < itemInfos.length; i++, j++) {
		newItemInfos[j] = new StringItemInfo(
				    new StringItem(itemInfos[i].strItem.label,
						   itemInfos[i].strItem.str,
						   modes[mode]),
				    itemInfos[i].type,
				    itemInfos[i].description + ", " +
				    appearanceModeNames[modes[mode]] +")" +
				    (addCommands[mode] ? " command added" : ""));

		if (addCommands[mode]) {
		    newItemInfos[j].strItem.addCommand(c);
		}
	    }
	}

	// Copy elements from itemInfosWidthDependent and add commands
	// if needed
	System.arraycopy(itemInfosWidthDependent, 0,
			 newItemInfos, itemInfos.length * modes.length,
			 itemInfosWidthDependent.length);

	for (int i=itemInfos.length*modes.length, j=0;
	     i < newItemInfos.length; i++, j++) {
	    if (addCommandsToWidthDepInfos[j]) {
		newItemInfos[i].strItem.addCommand(c);
	    }
	}

	return newItemInfos;
    }

    int calculateWidthEmptyLabel(int params[], int numOfIperLine[]) {
	int maxIs = 0;
	for (int i=0; i<numOfIperLine.length; i++) {
	    if (numOfIperLine[i] > maxIs) {
		maxIs = numOfIperLine[i];
	    }
	}
	return params[ITEM_LEFT_PAD] +
	       params[CONTENT_LEFT_PAD] +
	       maxIs*params[TEXT_I_WIDTH]+
	       params[CONTENT_RIGHT_PAD] +
	       params[ITEM_RIGHT_PAD];
    }

    int calculateHeightEmptyLabel(int params[], int numOfLines) {
	return params[ITEM_TOP_PAD] +
	       params[CONTENT_LEFT_PAD] +
	       params[TEXT_FONT_HEIGHT]*numOfLines +
	       params[CONTENT_RIGHT_PAD] +
	       params[ITEM_BOTTOM_PAD];
    }

    int calculateWidthEmptyText(int params[], int numOfIperLine[]) {
	int maxIs = 0;
	for (int i=0; i<numOfIperLine.length; i++) {
	    if (numOfIperLine[i] > maxIs) {
		maxIs = numOfIperLine[i];
	    }
	}
	return params[ITEM_LEFT_PAD] +
	       maxIs*params[LABEL_I_WIDTH]+
	       params[ITEM_RIGHT_PAD];
    }

    int calculateHeightEmptyText(int params[], int numOfLines) {
	return params[ITEM_TOP_PAD] +
	       params[LABEL_FONT_HEIGHT]*numOfLines +
	       params[ITEM_BOTTOM_PAD];
    }

    int calculateWidthSameLine(int params[],
			       int labelNumOfI, int textNumOfI) {
	return params[ITEM_LEFT_PAD] +
	       labelNumOfI*params[LABEL_I_WIDTH] +
	       params[LABEL_BODY_HORIZ_PAD] +
	       params[CONTENT_LEFT_PAD] +
	       textNumOfI*params[TEXT_I_WIDTH]+
	       params[CONTENT_RIGHT_PAD] +
	       params[ITEM_RIGHT_PAD];
    }

    int calculateHeightSameLine(int params[]) {
	int contentHeight = params[CONTENT_LEFT_PAD] +
	                    params[TEXT_FONT_HEIGHT] +
	                    params[CONTENT_RIGHT_PAD];
	return params[ITEM_TOP_PAD] + params[ITEM_BOTTOM_PAD] +
	       (params[LABEL_FONT_HEIGHT] > contentHeight ?
                params[LABEL_FONT_HEIGHT] : contentHeight);
    }

    int calculateWidthMultLines(int params[],
			       int labelNumOfIperLine[],
			       int textNumOfIperLine[]) {
	int labelMaxI = 0;
	for(int i=0; i<labelNumOfIperLine.length; i++) {
	    if (labelNumOfIperLine[i] > labelMaxI) {
		labelMaxI = labelNumOfIperLine[i];
	    }
	}
	labelMaxI *= params[LABEL_I_WIDTH];

	int textMaxI = 0;
	for(int i=0; i<textNumOfIperLine.length; i++) {
	    if (textNumOfIperLine[i] > textMaxI) {
		textMaxI = textNumOfIperLine[i];
	    }
	}
	textMaxI *= params[TEXT_I_WIDTH];
	textMaxI += params[CONTENT_LEFT_PAD] + params[CONTENT_RIGHT_PAD];

	return params[ITEM_LEFT_PAD] + params[ITEM_RIGHT_PAD] +
	    (labelMaxI > textMaxI ? labelMaxI : textMaxI);
    }

    int calculateHeightMultLines(int params[],
				int labelNumLines, int textNumLines) {
	return params[ITEM_TOP_PAD] +
	       labelNumLines*params[LABEL_FONT_HEIGHT] +
	       params[LABEL_BODY_VERTICAL_PAD] +
	       params[CONTENT_TOP_PAD] +
	       labelNumLines*params[TEXT_FONT_HEIGHT] +
	       params[CONTENT_BOTTOM_PAD] +
	       params[ITEM_BOTTOM_PAD];
    }

    // it is assumed in the following function that all 'i's following each
    // other can fit on one line. '\n' is the only line break
    // All characters other than '\n' should be 'i'
    int []getNumOfIsPerLine(String str) {
	if (str == null || str.length() == 0) {
	    return new int[]{0};
	}

	int lines = 1;
	for(int i=0; i < str.length(); i++) {
	    if (str.charAt(i) == '\n') {
		lines++;
	    }
	}

	int return_array[] = new int[lines];
	int j = 0, numOfIs = 0;
	for (int i = 0; i < str.length(); i++) {
	    if (str.charAt(i) == '\n') {
		return_array[j] = numOfIs;
		numOfIs = 0;
		j++;
	    } else {
		numOfIs++;
	    }
	}

	return_array[j] = numOfIs;


	return return_array;
    }

    int[] getParameters(StringItem strItem) {
	// Hyperlink and Button appearance settings are ignored
	// if there are no commands added

	// If a command is added to a StringItem with PLAIN appearance mode
	// it is changed to be HYPERLINK


	switch(strItem.getAppearanceMode()) {
	    case Item.PLAIN:
		return (strItem.numCommands == 0 ?
			PLAIN_PRMS : HYPERLINK_PRMS);

	    case Item.HYPERLINK:
		return (strItem.numCommands == 0 ?
			PLAIN_PRMS : HYPERLINK_PRMS);

	    case Item.BUTTON:
		return (strItem.numCommands == 0 ?
			PLAIN_PRMS : BUTTON_PRMS);
	}
	return null;
    }

    int calculateWidth(StringItem strItem, int type) {

	int params[] = getParameters(strItem);

	switch (type) {
	case EMPTY:
	    return 0;

	case EMPTY_LABEL:
	    return calculateWidthEmptyLabel(params,
					    getNumOfIsPerLine(strItem.str));

	case EMPTY_TEXT:
	    return calculateWidthEmptyText(params,
					   getNumOfIsPerLine(strItem.label));

	case SAME_LINE:
	    // we rely on the fact that both label and text are present
	    // and not empty
	    return calculateWidthSameLine(params,
					  getNumOfIsPerLine(strItem.label)[0],
					  getNumOfIsPerLine(strItem.str)[0]);

	case MULT_LINE:
	    return calculateWidthMultLines(params,
					   getNumOfIsPerLine(strItem.label),
					   getNumOfIsPerLine(strItem.str));

	default:
	    System.err.println("Incorrect type passing");
	    return 0;
	}
    }

    int calculateHeight(StringItem strItem, int type) {

	int params[] = getParameters(strItem);

	switch (type) {
	case EMPTY:
	    return 0;

	case EMPTY_LABEL:
	    return calculateHeightEmptyLabel(params,
					     getNumOfIsPerLine(strItem.str).length);

	case EMPTY_TEXT:
	    return calculateHeightEmptyText(params,
					    getNumOfIsPerLine(strItem.label).length);

	case SAME_LINE:
	    return calculateHeightSameLine(params);


	case MULT_LINE:
	    return calculateHeightMultLines(params,
					    getNumOfIsPerLine(strItem.label).length,
					    getNumOfIsPerLine(strItem.str).length);

	default:
	    System.err.println("Incorrect type passing");
	    return 0;
	}
    }

    // the tests

    public void testDefaultPreferredWidth(StringItemInfo strItemInfo) {
        int var1 = calculateWidth(strItemInfo.strItem, strItemInfo.type);
        int var2 = strItemInfo.strItem.stringItemLF.lGetPreferredWidth(-1);
        if (var1 == var2) {
  	        th.check(var1, var2);
        } else {
            th.todo(var1, var2);
        }
    }

    public void testDefaultPreferredHeight(StringItemInfo strItemInfo) {
        int var1 = calculateHeight(strItemInfo.strItem, strItemInfo.type);
        int var2 = strItemInfo.strItem.stringItemLF.lGetPreferredHeight(-1);
        if (var1 == var2) {
            th.check(var1, var2);
        } else {
            th.todo(var1, var2);
        }
    }


    public void test(TestHarness th) {
        this.th = th;

	StringItemInfo newItemInfos[] = createTestArray();

	for (int i = 0 ; i < newItemInfos.length; i++) {
	    testDefaultPreferredWidth(newItemInfos[i]);

	    testDefaultPreferredHeight(newItemInfos[i]);
	}
    }

     class StringItemInfo {
	 StringItemInfo(StringItem strItem, int type,
 		       String description) {
 	    this.strItem = strItem;
 	    this.type = type;
 	    this.description = description;
 	}

 	StringItem strItem;
 	int type;
 	String description;
     };
}
