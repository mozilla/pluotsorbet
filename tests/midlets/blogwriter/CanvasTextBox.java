/*
 * Copyright © 2013 Nokia Corporation. All rights reserved. Nokia and Nokia
 * Connecting People are registered trademarks of Nokia Corporation. Oracle and
 * Java are trademarks or registered trademarks of Oracle and/or its affiliates.
 * Other product and company names mentioned herein may be trademarks or trade
 * names of their respective owners. See LICENSE.TXT for license information.
 */

package com.nokia.example;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import com.nokia.mid.ui.CanvasGraphicsItem;
import com.nokia.mid.ui.TextEditor;
import com.nokia.mid.ui.TextEditorListener;

/**
 * This class implements CanvasTextBox control with label, text editing area
 * decorations and a keyboard indicator.
 *
 * CanvasTextBox is based on CanvasGraphicsItem, on which label and text editor
 * borders are drawn. On Symbian platform, the keyboard indicator will be
 * relocated from its default position. Also a very simple scrollbar is drawn to
 * indicate position in case CanvasTextBox is constructed as multiline.
 *
 * CanvasTextBox can have normal, focused or dimmed (disabled) state. State is
 * encapsulated in TextBoxState objects (class TextBoxState is implemented in
 * this file).
 */
public class CanvasTextBox extends CanvasGraphicsItem implements
TextEditorListener {

	private Font labelFont = null;
	private String label;
	private TextEditor textEditor;
	private TextBoxState normalState;
	private TextBoxState focusedState;
	private TextBoxState dimmedState;
	private TextBoxState currentState;
	private boolean enabled = true;
	private boolean focused = false;
	private TextEditorListener listener;
	private int controlWidth = 200;
	private int textLimit;
	private boolean multiline;
	private Scrollbar scrollbar;
	private Controls controls;

	// Enable indicator on JRT version 2.1 for symbian devices by uncommenting the following code. 
	// JRT versions 2.2 and newer already contain the indicator in the virtual keyboard.
	// private boolean showIndicator;


	// Default control's margin
	private final int margin = 3;
	// Margin applied around text editor and its border
	private final int textEditorBorderMargin = 9;
	// Margin applied on text editor it self
	private final int textEditorMargin = 12;
        private final int editorYPadding = 10;

	public CanvasTextBox(Canvas parent, String label, int type, int textLimit,
			boolean multiline) {
		super(1, 1);
		this.setParent(parent);
		this.label = label;
		this.textLimit = textLimit;
		this.multiline = multiline;

		this.labelFont = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN,
				Font.SIZE_LARGE);
		this.initializeTextEditor(parent, type);
		this.createStates();              
		this.setAutomaticSize();
	}

	public CanvasTextBox(Canvas parent, String label, int type, int textLimit) {
		// Construct single line text box with default width
		this(parent, label, type, textLimit, false);
	}

	public String getText() {
		return textEditor.getContent();
	}

	public boolean isEmpty() {
		return textEditor.size() == 0;
	}

	public void clearChar() {
		int caret = textEditor.getCaretPosition() - 1;
		if (caret >= 0) {
			textEditor.delete(textEditor.getCaretPosition() - 1, 1);
			repaint();
		}
	}

	public void setVisible(boolean visible) {
		super.setVisible(visible);
		textEditor.setVisible(visible);
	}

	/**
	 * Sets the control position.
	 *
	 * Repositions are also nested TextEditor and indicator (if supported).
	 */
	public void setPosition(int x, int y) {
		// Set position of the underlying CanvasGraphicsItem
		super.setPosition(x, y);
		// Calculate and sed position of TextEditor
		int editorX = x + (this.controlWidth - this.editorWidth()) / 2;
		int editorY = y + labelFont.getHeight() + 2 * this.margin
				+ (this.currentState.height - this.textEditor.getHeight()) / 2
                                + ((BlogWriter.isAshaPlatform()) ? editorYPadding : 0);
		this.textEditor.setPosition(editorX, editorY);

		// Where supported, re-position also keyboard indicator
		/*
           	//Enable indicator on JRT version 2.1 for symbian devices by uncommenting the following code. 
         	//JRT versions 2.2 and newer already contain the indicator in the virtual keyboard.

        if (this.textEditor instanceof com.nokia.mid.ui.S60TextEditor) {
            com.nokia.mid.ui.S60TextEditor s60Editor = (com.nokia.mid.ui.S60TextEditor) this.textEditor;
            s60Editor.setIndicatorLocation(editorX, y + labelFont.getHeight()
                    + this.currentState.getHeight() + 3 * this.margin);
            s60Editor.setIndicatorVisibility(this.focused && this.showIndicator);
        }*/
	}


	/**
	 * Sets size of the CanvasTextBox.
	 *
	 * This method re-layouts nested TextEditor and indicator (if supported).
	 */
	public void setSize(int w, int h) {
		// Set size of underlying CanvasGraphicsItem
		super.setSize(w, h);
		// Calculate the size of  TextEditor.
		this.controlWidth = w;
                if(BlogWriter.isAshaPlatform())
                    h += 2 * editorYPadding - 4;
		int editorHeight = (h - labelFont.getHeight() - 3 * this.margin - 2 * this.textEditorBorderMargin) + (BlogWriter.isAshaPlatform() ? margin : 0);
		this.textEditor.setSize(this.controlWidth - 2 * this.textEditorMargin
				- (this.scrollbar != null ? Scrollbar.width : 0), editorHeight);
		// States need to be re-created to reflect change in editor's size
		this.createStates();
		// This call updates indicator position
		this.setPosition(this.getPositionX(), this.getPositionY());
	}
	
	public void setTextEditorListener(TextEditorListener listener) {
		this.listener = listener;
	}

	/**
	 * Enables or disables control.
	 */
	public void setEnabled(boolean enabled) {
		if (this.enabled == enabled) {
			return;
		}

		this.enabled = enabled;
		this.updateState();
		this.repaint();
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	/**
	 * Sets focus of the CanvasTextBox. Focus is forwarded to TextEditor, and
	 * when control loses focus, it hides indicator (where it is supported).
	 */
	public void setFocused(boolean focused) {
		if (this.focused == focused) {
			//return;
		}

		this.focused = focused;
		this.updateState();
		if (this.textEditor.hasFocus() != this.focused) {
			this.textEditor.setFocus(this.focused);
		}

		/*           	
         	//Enable indicator on JRT version 2.1 for symbian devices by uncommenting the following code. 
         	//JRT versions 2.2 and newer already contain the indicator in the virtual keyboard.

        if (this.textEditor instanceof com.nokia.mid.ui.S60TextEditor) {
            com.nokia.mid.ui.S60TextEditor s60Editor = (com.nokia.mid.ui.S60TextEditor) this.textEditor;
            s60Editor.setIndicatorVisibility(this.focused && this.showIndicator);
        }*/
		this.repaint();
	}

	public boolean isFocused() {
		return this.focused;
	}

	/**
	 * This is very basic pointer event handling. It expects pointer pressed
	 * events.
	 *
	 * CanvasGraphicsItem does not receive any pointer events, they are
	 * delivered to parent Canvas, so this method needs to be called from
	 * Canvas.pointerPressed() override.
	 */
	public void handlePointerPressed(int x, int y) {
		if (this.isVisible() && this.enabled) {
			if (this.hitTest(x, y)) {
				this.setFocused(true);
				if (this.scrollbar != null) {
					this.scrollbar.handlePointerPressed(x, y);
					this.repaint();
				}
				if (this.controls != null) {
					this.controls.handlePointerPressed(x - this.getPositionX(), y - this.getPositionY());
					this.repaint();
				}
			} else {
				this.setFocused(false);
			}
		}
	}

	/**
	 * This is very basic pointer event handling for pointer released events.
	 */
	public void handlePointerReleased(int x, int y) {
		if (this.isVisible() && this.controls != null) {
			this.controls.handlePointerReleased(x - this.getPositionX(), y - this.getPositionY());
			this.repaint();
		}
	}

	/**
	 * Checks whether given point belongs to the control. Coordinates are
	 * relative to parent Canvas.
	 */
	public boolean hitTest(int x, int y) {
		return x >= this.getPositionX()
				&& x < (this.getPositionX() + this.getWidth())
				&& y >= this.getPositionY()
				&& y < (this.getPositionY() + this.getHeight());
	}

	/**
	 * Before exiting MIDlet, it is necessary to set parents of both TextEditor
	 * and the base CanvasGraphics item to null.
	 */
	public void dispose() {
		this.textEditor.setParent(null);
		this.setParent(null);
	}

    /**
     * Paints the label, currentState and scrollbar.
     */
    public void paint(Graphics gfx) {
        if (BlogWriter.isAshaPlatform()) {
            gfx.setColor(0x8D8C8C);
            gfx.fillRect(0, 0, getWidth(), getHeight());
        }

		gfx.setColor(this.currentState.labelColor);
		gfx.drawString(this.label, this.margin, this.margin, Graphics.TOP | Graphics.LEFT);

		int textEditorY = this.labelFont.getHeight() + 2 * this.margin;
		this.currentState.paint(gfx,(this.controlWidth - this.currentState.width) / 2, textEditorY);

		if (this.scrollbar != null) {
			this.scrollbar.paint(gfx, this.textEditor.getWidth()
					+ this.textEditorMargin, textEditorY
					+ this.textEditorBorderMargin);
		}
		if (this.controls != null && this.isFocused()) {
			this.controls.paint(gfx, (this.controlWidth - this.currentState.width) / 2 + this.currentState.width, textEditorY-1);
		}
	}

	/**
	 * Handles some of the TextEditor events to support scrollbar and forwards
	 * events to external listener (if there is any).
	 */
	public void inputAction(TextEditor source, int type) {
		if (source != this.textEditor) {
			return;
		}

		if ((type & (TextEditorListener.ACTION_SCROLLBAR_CHANGED | TextEditorListener.ACTION_CARET_MOVE)) != 0) {
			if (this.scrollbar != null) {
				this.repaint();
			}
		}

		if (this.listener != null) {
			this.listener.inputAction(source, type);
		}
	}

	/**
	 * Creates TextEditor instance and sets its properties.
	 */
	private void initializeTextEditor(Canvas parent, int type) {
		this.textEditor = TextEditor.createTextEditor("", this.textLimit, type,
				this.controlWidth - 2 * this.textEditorMargin,
				(this.multiline ? 2 : 1) * (labelFont.getHeight()));

		this.textEditor.setParent(parent);
		if (this.multiline) {
			this.textEditor.setMultiline(true);

			/*
           		//Enable indicator on JRT version 2.1 for symbian devices by uncommenting the following code. 
         		//JRT versions 2.2 and newer already contain the indicator in the virtual keyboard.

            if (this.textEditor instanceof com.nokia.mid.ui.S60TextEditor) {
                this.textEditor.setSize(this.textEditor.getWidth() - Scrollbar.width, this.textEditor.getHeight());
                //this.scrollbar = new Scrollbar(this.textEditor, 0xaaaaaa, 0x101010);
            }*/

			if(BlogWriter.isS60Platform())
				this.scrollbar = new Scrollbar(this.textEditor, 0xaaaaaa, 0x101010);
		}

		if(!BlogWriter.isS60Platform())
		{
			this.controls = new Controls(this.textEditor, 0x101010, 0xaaaaaa, 0xffffff);
		}
		this.textEditor.setTextEditorListener(this);
		this.setZPosition(1);
		this.textEditor.setZPosition(2);
	}

	/**
	 * Sets CanvasTextBox initial size.
	 */
	private void setAutomaticSize() {
		// Calculate height so all part of the CanvasTextBox fit
		int height = labelFont.getHeight() + this.normalState.getHeight() + 3
				* this.margin;
		/*
           	//Enable indicator on JRT version 2.1 for symbian devices by uncommenting the following code. 
         	//JRT versions 2.2 and newer already contain the indicator in the virtual keyboard.

        if (this.textEditor instanceof com.nokia.mid.ui.S60TextEditor) {
            com.nokia.mid.ui.S60TextEditor s60Editor = (com.nokia.mid.ui.S60TextEditor) this.textEditor;
            height += s60Editor.getIndicatorSize()[1] + this.margin;
        }*/
		super.setSize(this.controlWidth + 2 * this.margin, height);
	}

	/**
	 * Updates currentState based on state flags.
	 */
	private void updateState() {
		if (this.enabled) {
			if (this.focused) {
				this.currentState = this.focusedState;
			} else {
				this.currentState = this.normalState;
			}
		} else {
			this.currentState = this.dimmedState;
		}
		this.textEditor.setForegroundColor(this.currentState.textColor);
	}

	   /**
     * Creates CanvasTextBox states.
     */
    private void createStates() {
        int width = this.editorWidth() + 2 * this.textEditorBorderMargin;
        int height;
        if (BlogWriter.isAshaPlatform()) {
            height = this.textEditor.getHeight() - 2;// + 2 * this.textEditorBorderMargin;
        } else {
            height = this.textEditor.getHeight() + 2 * this.textEditorBorderMargin;
        }

        this.normalState = new TextBoxState(width, height, 0x000000, 0xff000000,
                0xe0e0e0);
        this.focusedState = new TextBoxState(width, height, 0x000000, 0xff000000,
                0xffffff);
        this.dimmedState = new TextBoxState(width, height, 0xc5c5c5, 0xff313131,
                0xa0a0a0);
        this.updateState();
    }

	/**
	 * Calculates space needed for TextEditor. Scrollbar width is taken into
	 * account.
	 */
	private int editorWidth() {
		return this.textEditor.getWidth()
				+ (this.scrollbar != null ? Scrollbar.width : 0);
	}

	/**
	 * Encapsulates CanvasTextBox visual appearance.
	 *
	 * TextBoxState allows to specify label, text and background colors.
	 *
	 * When the size of the owning CanvasTextEditor changes, TextBoxStates need
	 * to be recreated since their width and height can only be set in the
	 * constructor.
	 */
	class TextBoxState {

		public int backgroundColor;
		public int labelColor;
		public int textColor;
		private int width;
		private int height;
		private final int cornersDiameter = 10;
		private final int borderColor = 0x000000;

		public TextBoxState(int width, int height, int labelColor,
				int textColor, int backgroundColor) {
			this.width = width;
			this.height = height;
			this.labelColor = labelColor;
			this.textColor = textColor;
			this.backgroundColor = backgroundColor;
		}

		public int getHeight() {
			return this.height;
		}

		public int getWidth() {
			return this.width;
		}

		/**
		 * Draws background and border. This method should be called from
		 * CanvasTextBox paint method.
		 */
		public void paint(Graphics gfx, int x, int y) {
			gfx.setColor(this.backgroundColor);
			gfx.fillRoundRect(x, y, this.width, this.height,
					this.cornersDiameter, this.cornersDiameter);
			gfx.setColor(this.borderColor);
			gfx.drawRoundRect(x, y, this.width, this.height,
					this.cornersDiameter, this.cornersDiameter);
		}
	}
}
