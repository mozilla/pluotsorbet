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
 * Implements a graphical display, such as a bar graph, of an integer
 * value.  The <code>Gauge</code> contains a <em>current value</em>
 * that lies between zero and the <em>maximum value</em>, inclusive.
 * The application can control the current value and maximum value.
 * The range of values specified by the application may be larger than
 * the number of distinct visual states possible on the device, so
 * more than one value may have the same visual representation.
 *
 * <P>For example, consider a <code>Gauge</code> object that has a
 * range of values from zero to <code>99</code>, running on a device
 * that displays the <code>Gauge's</code> approximate value using a
 * set of one to ten bars. The device might show one bar for values
 * zero through nine, two bars for values ten through <code>19</code>,
 * three bars for values <code>20</code> through <code>29</code>, and
 * so forth. </p>
 *
 * <P>A <code>Gauge</code> may be interactive or
 * non-interactive. Applications may set or retrieve the
 * <code>Gauge's</code> value at any time regardless of the
 * interaction mode.  The implementation may change the visual
 * appearance of the bar graph depending on whether the object is
 * created in interactive mode. </p>
 *
 * <P>In interactive mode, the user is allowed to modify the
 * value. The user will always have the means to change the value up
 * or down by one and may also have the means to change the value in
 * greater increments.  The user is prohibited from moving the value
 * outside the established range. The expected behavior is that the
 * application sets the initial value and then allows the user to
 * modify the value thereafter. However, the application is not
 * prohibited from modifying the value even while the user is
 * interacting with it. </p>
 *
 * <p> In many cases the only means for the user to modify the value
 * will be to press a button to increase or decrease the value by one
 * unit at a time.  Therefore, applications should specify a range of
 * no more than a few dozen values. </p>
 *
 * <P>In non-interactive mode, the user is prohibited from modifying
 * the value.  Non-interactive mode is used to provide feedback to the
 * user on the state of a long-running operation. One expected use of
 * the non-interactive mode is as a &quot;progress indicator&quot; or
 * &quot;activity indicator&quot; to give the user some feedback
 * during a long-running operation. The application may update the
 * value periodically using the <code>setValue()</code> method. </P>
 *
 * <P>A non-interactive <code>Gauge</code> can have a definite or
 * indefinite range.  If a <code>Gauge</code> has definite range, it
 * will have an integer value between zero and the maximum value set
 * by the application, inclusive.  The implementation will provide a
 * graphical representation of this value such as described above.</p>
 *
 * <P>A non-interactive <code>Gauge</code> that has indefinite range
 * will exist in one of four states: continuous-idle,
 * incremental-idle, continuous-running, or incremental-updating.
 * These states are intended to indicate to the user that some level
 * of activity is occurring.  With incremental-updating, progress can
 * be indicated to the user even though there is no known endpoint to
 * the activity.  With continuous-running, there is no progress that
 * gets reported to the user and there is no known endpoint;
 * continuous-running is merely a busy state indicator. The
 * implementation should use a graphical display that shows this
 * appropriately.  The implementation may use different graphics for
 * indefinite continuous gauges and indefinite incremental gauges.
 * Because of this, separate idle states exist for each mode.  For
 * example, the implementation might show an hourglass or spinning
 * watch in the continuous-running state, but show an animation with
 * different states, like a beach ball or candy-striped bar, in the
 * incremental-updating state.</p>
 *
 * <p>In the continuous-idle or incremental-idle state, the
 * <code>Gauge</code> indicates that no activity is occurring. In the
 * incremental-updating state, the <code>Gauge</code> indicates
 * activity, but its graphical representation should be updated only
 * when the application requests an update with a call to
 * <code>setValue()</code>.  In the continuous-running state, the
 * <code>Gauge</code> indicates activity by showing an animation that
 * runs continuously, without update requests from the
 * application.</p>
 *
 * <p>The values <code>CONTINUOUS_IDLE</code>,
 * <code>INCREMENTAL_IDLE</code>, <code>CONTINUOUS_RUNNING</code>, and
 * <code>INCREMENTAL_UPDATING</code> have their special meaning only
 * when the <code>Gauge</code> is non-interactive and has been set to
 * have indefinite range.  They are treated as ordinary values if the
 * <code>Gauge</code> is interactive or if it has been set to have a
 * definite range.</p>
 *
 * <P>An application using the <code>Gauge</code> as a progress
 * indicator should typically also attach a {@link Command#STOP STOP}
 * command to the container containing the <code>Gauge</code> to allow
 * the user to halt the operation in progress.</p>
 *
 * <h3>Notes for Application Developers</h3>
 * 
 * <P>As mentioned above, a non-interactive <code>Gauge</code> may be
 * used to give user feedback during a long-running operation.  If the
 * application can observe the progress of the operation as it
 * proceeds to an endpoint known in advance, then the application
 * should use a non-interactive <code>Gauge</code> with a definite
 * range.  For example, consider an application that is downloading a
 * file known to be <code>20</code> kilobytes in size.  The
 * application could set the <code>Gauge's</code> maximum value to be
 * <code>20</code> and set its value to the number of kilobytes
 * downloaded so far.  The user will be presented with a
 * <code>Gauge</code> that shows the portion of the task completed at
 * any given time.</P>
 *
 * <P>If, on the other hand, the application is downloading a file of
 * unknown size, it should use a non-interactive <code>Gauge</code>
 * with indefinite range.  Ideally, the application should call
 * <CODE>setValue(INCREMENTAL_UPDATING)</CODE> periodically, perhaps
 * each time its input buffer has filled.  This will give the user an
 * indication of the rate at which progress is occurring.</P>
 *
 * <P>Finally, if the application is performing an operation but has
 * no means of detecting progress, it should set a non-interactive
 * <code>Gauge</code> to have indefinite range and set its value to
 * <CODE>CONTINUOUS_RUNNING</CODE> or <CODE>CONTINUOUS_IDLE</CODE> as
 * appropriate.  For example, if the application has issued a request
 * to a network server and is about to block waiting for the server to
 * respond, it should set the <code>Gauge's</code> state to
 * <CODE>CONTINUOUS_RUNNING</CODE> before awaiting the response, and it
 * should set the state to <CODE>CONTINUOUS_IDLE</CODE> after it has
 * received the response.</P>
 *
 * @since MIDP 1.0 
 */

public class Gauge extends Item {

    // public implementation

    /**
     * A special value used for the maximum value in order to indicate that 
     * the <code>Gauge</code> has indefinite range.  This value may be
     * used as the <code>maxValue</code>
     * parameter to the constructor, the parameter passed to
     * <code>setMaxValue()</code>, and
     * as the return value of <code>getMaxValue()</code>.
     * <P>
     * The value of <code>INDEFINITE</code> is <code>-1</code>.</P>
     *
     */
    public static final int INDEFINITE = -1;
    
    /**    
     * The value representing the continuous-idle state of a
     * non-interactive <code>Gauge</code> with indefinite range.  In
     * the continuous-idle state, the gauge shows a graphic
     * indicating that no work is in progress.
     *
     * <p>This value has special meaning only for non-interactive
     * gauges with indefinite range.  It is treated as an ordinary
     * value for interactive gauges and for non-interactive gauges
     * with definite range.</p>
     *
     * <p>The value of <code>CONTINUOUS_IDLE</code> is
     * <code>0</code>.</p>
     * 
     */
    public static final int CONTINUOUS_IDLE = 0;

    /** 
     * The value representing the incremental-idle state of a
     * non-interactive <code>Gauge</code> with indefinite range.  In
     * the incremental-idle state, the gauge shows a graphic
     * indicating that no work is in progress.
     *
     * <p>This value has special meaning only for non-interactive
     * gauges with indefinite range.  It is treated as an ordinary
     * value for interactive gauges and for non-interactive gauges
     * with definite range.</p>
     *
     * <p>The value of <code>INCREMENTAL_IDLE</code> is
     * <code>1</code>.</p>
     * 
     */
    public static final int INCREMENTAL_IDLE = 1;
    
    /** 
     * The value representing the continuous-running state of a
     * non-interactive <code>Gauge</code> with indefinite range.  In
     * the continuous-running state, the gauge shows a
     * continually-updating animation sequence that indicates that
     * work is in progress.  Once the application sets a gauge into
     * the continuous-running state, the animation should proceed
     * without further requests from the application.
     * 
     * <p>This value has special meaning only for non-interactive
     * gauges with indefinite range.  It is treated as an ordinary
     * value for interactive gauges and for non-interactive gauges
     * with definite range.</p>
     *
     * <p>The value of <code>CONTINUOUS_RUNNING</code> is
     * <code>2</code>.</p>
     *
     */
    public static final int CONTINUOUS_RUNNING = 2;
    
    /** 
     * The value representing the incremental-updating state of a
     * non-interactive <code>Gauge</code> with indefinite range.  In
     * the incremental-updating state, the gauge shows a graphic
     * indicating that work is in progress, typically one frame of an
     * animation sequence.  The graphic should be updated to the next
     * frame in the sequence only when the application calls
     * <code>setValue(INCREMENTAL_UPDATING)</code>.
     * 
     * <p>This value has special meaning only for non-interactive
     * gauges with indefinite range.  It is treated as an ordinary
     * value for interactive gauges and for non-interactive gauges
     * with definite range.</p> 
     *
     * <p> The value of <code>INCREMENTAL_UPDATING</code> is
     * <code>3</code>.</p>
     *
     */
    public static final int INCREMENTAL_UPDATING = 3;

    /**
     * Creates a new <code>Gauge</code> object with the given
     * label, in interactive or non-interactive mode, with the given
     * maximum and initial values.  In interactive mode (where
     * <code>interactive</code> is <code>true</code>) the maximum
     * value must be greater than zero, otherwise an exception is
     * thrown.  In non-interactive mode (where
     * <code>interactive</code> is <code>false</code>) the maximum
     * value must be greater than zero or equal to the special value
     * <code>INDEFINITE</code>, otherwise an exception is thrown.
     *
     * <p>If the maximum value is greater than zero, the gauge has
     * definite range.  In this case the initial value must be within
     * the range zero to <code>maxValue</code>, inclusive.  If the
     * initial value is less than zero, the value is set to zero.  If
     * the initial value is greater than <code>maxValue</code>, it is
     * set to <code>maxValue</code>.</p>
     *
     * <p>If <code>interactive</code> is <code>false</code> and the
     * maximum value is <code>INDEFINITE</code>, this creates a
     * non-interactive gauge with indefinite range. The initial value
     * must be one of <code>CONTINUOUS_IDLE</code>,
     * <code>INCREMENTAL_IDLE</code>, <code>CONTINUOUS_RUNNING</code>,
     * or <code>INCREMENTAL_UPDATING</code>.</p>
     *
     * @see #INDEFINITE
     * @see #CONTINUOUS_IDLE
     * @see #INCREMENTAL_IDLE
     * @see #CONTINUOUS_RUNNING
     * @see #INCREMENTAL_UPDATING
     *
     * @param label the <code>Gauge's</code> label
     * @param interactive tells whether the user can change the value
     * @param maxValue the maximum value, or <code>INDEFINITE</code>
     * @param initialValue the initial value in the range
     * <code>[0..maxValue]</code>, or one of <code>CONTINUOUS_IDLE</code>,
     * <code>INCREMENTAL_IDLE</code>, <code>CONTINUOUS_RUNNING</code>,
     * or <code>INCREMENTAL_UPDATING</code> if <code>maxValue</code> is
     * <code>INDEFINITE</code>.
     *
     * @throws IllegalArgumentException if <code>maxValue</code>
     * is not positive for interactive gauges
     * @throws IllegalArgumentException if <code>maxValue</code> is
     * neither positive nor
     * <code>INDEFINITE</code> for non-interactive gauges
     * @throws IllegalArgumentException if initialValue is not one of
     * <code>CONTINUOUS_IDLE</code>, <code>INCREMENTAL_IDLE</code>,
     * <code>CONTINUOUS_RUNNING</code>, or <code>INCREMENTAL_UPDATING</code>
     * for a non-interactive gauge with indefinite range
     */
    public Gauge(String label, boolean interactive, int maxValue,
                 int initialValue)
    {
        super(label);

        if (maxValue == INDEFINITE && (initialValue < CONTINUOUS_IDLE || 
                                   initialValue > INCREMENTAL_UPDATING)) {
           throw new IllegalArgumentException();
        }

        synchronized (Display.LCDUILock) {

            this.interactive = interactive;

            itemLF = gaugeLF = LFFactory.getFactory().getGaugeLF(this);
            /**
            * IllegalArgumentException may be thrown by
            * setMaxValueImpl and setValue
            */
            setMaxValueImpl(maxValue);
            setValueImpl(initialValue);
        }
    }

    /**
     * Sets the label of the <code>Item</code>. If <code>label</code>
     * is <code>null</code>, specifies that this item has no label.
     * 
     * <p>It is illegal to call this method if this <code>Item</code>
     * is contained within  an <code>Alert</code>.</p>
     * 
     * @param label the label string
     * @throws IllegalStateException if this <code>Item</code> is contained 
     * within an <code>Alert</code>
     * @see #getLabel
     */
    public void setLabel(String label) {
        if (this.owner instanceof Alert) {
            throw new IllegalStateException("Gauge contained within an Alert");
        }
        super.setLabel(label);
    }

    /**
     * Sets the layout directives for this item.
     *
     * <p>It is illegal to call this method if this <code>Item</code> 
     * is contained within an <code>Alert</code>.</p>
     * 
     * @param layout a combination of layout directive values for this item
     * @throws IllegalArgumentException if the value of layout is not a valid
     * combination of layout directives
     * @throws IllegalStateException if this <code>Item</code> is
     * contained within an <code>Alert</code>
     * @see #getLayout
     */
    public void setLayout(int layout) {
        if (this.owner instanceof Alert) {
            throw new IllegalStateException("Gauge contained within an Alert");
        }
        super.setLayout(layout);
    }

    /**
     * Adds a context sensitive <code>Command</code> to the item. 
     * The semantic type of
     * <code>Command</code> should be <code>ITEM</code>. The implementation 
     * will present the command
     * only when the the item is active, for example, highlighted.
     * <p>
     * If the added command is already in the item (tested by comparing the
     * object references), the method has no effect. If the item is
     * actually visible on the display, and this call affects the set of
     * visible commands, the implementation should update the display as soon
     * as it is feasible to do so.
     *
     * <p>It is illegal to call this method if this <code>Item</code>
     * is contained within an <code>Alert</code>.</p>
     * 
     * @param cmd the command to be added
     * @throws IllegalStateException if this <code>Item</code> is contained
     * within an <code>Alert</code>
     * @throws NullPointerException if cmd is <code>null</code>
     */
    public void addCommand(Command cmd) {
        if (this.owner instanceof Alert) {
            throw new IllegalStateException("Gauge contained within an Alert");
        }
        super.addCommand(cmd);
    }

    /**
     * Sets a listener for <code>Commands</code> to this Item, 
     * replacing any previous
     * <code>ItemCommandListener</code>. A <code>null</code> reference 
     * is allowed and has the effect of
     * removing any existing listener.
     *
     * <p>It is illegal to call this method if this <code>Item</code> 
     * is contained within an <code>Alert</code>.</p>
     * 
     * @param l the new listener, or <code>null</code>.
     * @throws IllegalStateException if this <code>Item</code> is contained
     * within an <code>Alert</code>
     */
    public void setItemCommandListener(ItemCommandListener l) {
        if (this.owner instanceof Alert) {
            throw new IllegalStateException("Gauge contained within an Alert");
        }
        super.setItemCommandListener(l);
    }

    /**
     * Sets the preferred width and height for this <code>Item</code>.
     * Values for width and height less than <code>-1</code> are illegal.
     * If the width is between zero and the minimum width, inclusive,
     * the minimum width is used instead.
     * If the height is between zero and the minimum height, inclusive,
     * the minimum height is used instead.
     *
     * <p>Supplying a width or height value greater than the minimum width or 
     * height <em>locks</em> that dimension to the supplied
     * value.  The implementation may silently enforce a maximum dimension for 
     * an <code>Item</code> based on factors such as the screen size. 
     * Supplying a value of
     * <code>-1</code> for the width or height unlocks that dimension.
     * See <a href="#sizes">Item Sizes</a> for a complete discussion.</p>
     * 
     * <p>It is illegal to call this method if this <code>Item</code> 
     * is contained within  an <code>Alert</code>.</p>
     * 
     * @param width the value to which the width should be locked, or
     * <code>-1</code> to unlock
     * @param height the value to which the height should be locked, or 
     * <code>-1</code> to unlock
     * @throws IllegalArgumentException if width or height is less than 
     * <code>-1</code>
     * @throws IllegalStateException if this <code>Item</code> is contained
     * within an <code>Alert</code>
     * @see #getPreferredHeight
     * @see #getPreferredWidth
     */
    public void setPreferredSize(int width, int height) {
        if (this.owner instanceof Alert) {
            throw new IllegalStateException("Gauge contained within an Alert");
        }
        super.setPreferredSize(width, height);
    }

    /**
     * Sets default <code>Command</code> for this <code>Item</code>.  
     * If the <code>Item</code> previously had a
     * default <code>Command</code>, that <code>Command</code> 
     * is no longer the default, but it
     * remains present on the <code>Item</code>.
     *
     * <p>If not <code>null</code>, the <code>Command</code> object
     * passed becomes the default <code>Command</code>
     * for this <code>Item</code>.  If the <code>Command</code> object
     * passed is not currently present
     * on this <code>Item</code>, it is added as if {@link #addCommand}
     * had been called
     * before it is made the default <code>Command</code>.</p>
     *
     * <p>If <code>null</code> is passed, the <code>Item</code> is set to
     * have no default <code>Command</code>.
     * The previous default <code>Command</code>, if any, remains present
     * on the <code>Item</code>.
     * </p>
     *
     * <p>It is illegal to call this method if this <code>Item</code>
     * is contained within  an <code>Alert</code>.</p>
     * 
     * @param cmd the command to be used as this <code>Item's</code> default
     * <code>Command</code>, or <code>null</code> if there is to 
     * be no default command
     *
     * @throws IllegalStateException if this <code>Item</code> is contained
     * within an <code>Alert</code>
     */
    public void setDefaultCommand(Command cmd) {
        if (this.owner instanceof Alert) {
            throw new IllegalStateException("Gauge contained within an Alert");
        }
        super.setDefaultCommand(cmd);
    }
    
    /**
     * Sets the current value of this <code>Gauge</code> object.
     *
     * <p>If the gauge is interactive, or if it is non-interactive with
     * definite range, the following rules apply.  If the value is less than
     * zero, zero is used. If the current value is greater than the maximum
     * value, the current value is set to be equal to the maximum value. </p>
     *
     * <p> If this <code>Gauge</code> object is a non-interactive
     * gauge with indefinite
     * range, then value must be one of <code>CONTINUOUS_IDLE</code>,
     * <code>INCREMENTAL_IDLE</code>, <code>CONTINUOUS_RUNNING</code>, or
     * <code>INCREMENTAL_UPDATING</code>.
     * Other values will cause an exception to be thrown.</p>
     * 
     * @see #CONTINUOUS_IDLE
     * @see #INCREMENTAL_IDLE
     * @see #CONTINUOUS_RUNNING
     * @see #INCREMENTAL_UPDATING
     * 
     * @param value the new value
     * @throws IllegalArgumentException if value is not one of
     * <code>CONTINUOUS_IDLE</code>,  <code>INCREMENTAL_IDLE</code>,
     * <code>CONTINUOUS_RUNNING</code>, or <code>INCREMENTAL_UPDATING</code>
     * for non-interactive gauges with indefinite range
     * @see #getValue
     */
    public void setValue(int value) {
        synchronized (Display.LCDUILock) {
           setValueImpl(value);
        }
    }
    
    /**
     * Gets the current value of this <code>Gauge</code> object.
     *
     * <p> If this <code>Gauge</code> object is a non-interactive
     * gauge with indefinite
     * range, the value returned will be one of <code>CONTINUOUS_IDLE</code>,
     * <code>INCREMENTAL_IDLE</code>, <code>CONTINUOUS_RUNNING</code>, or
     * <code>INCREMENTAL_UPDATING</code>.  Otherwise, it will be an integer
     * between zero and the gauge's maximum value, inclusive.</p>
     *
     * @see #CONTINUOUS_IDLE
     * @see #INCREMENTAL_IDLE
     * @see #CONTINUOUS_RUNNING
     * @see #INCREMENTAL_UPDATING
     * 
     * @return current value of the <code>Gauge</code>
     * @see #setValue
     */
    public int getValue() {
	synchronized (Display.LCDUILock) {
	    return gaugeLF.lGetValue();
	}
    }

    /**
     * Sets the maximum value of this <code>Gauge</code> object.
     * 
     * <p>For interactive gauges, the new maximum value must be greater than
     * zero, otherwise an exception is thrown.  For non-interactive gauges,
     * the new maximum value must be greater than zero or equal to the special
     * value <code>INDEFINITE</code>, otherwise an exception is thrown.  </p>
     *
     * <p>If the new maximum value is greater than zero, this provides the
     * gauge with a definite range.  If the gauge previously had a definite
     * range, and if the current value is greater than new maximum value, the
     * current value is set to be equal to the new maximum value.  If the 
     * gauge previously had a definite range, and if the current value is less 
     * than or equal to the new maximum value, the current value is left 
     * unchanged. </p>
     * 
     * <p>If the new maximum value is greater than zero, and if the gauge had
     * previously had indefinite range, this new maximum value provides it
     * with a definite range.  Its graphical representation must change
     * accordingly, the previous state of <code>CONTINUOUS_IDLE</code>,
     * <code>INCREMENTAL_IDLE</code>, <code>CONTINUOUS_RUNNING</code>, or 
     * <code>INCREMENTAL_UPDATING</code> is ignored, and the current value 
     * is set to zero. </p>
     *
     * <p>If this gauge is non-interactive and the new maximum value is
     * <code>INDEFINITE</code>, this gives the gauge indefinite range.
     * If the gauge
     * previously had a definite range, its graphical representation must
     * change accordingly, the previous value is ignored, and the current
     * state is set to <code>CONTINUOUS_IDLE</code>.  If the gauge previously 
     * had an indefinite range, setting the maximum value to 
     * <code>INDEFINITE</code> will have no effect. </p>
     *
     * @see #INDEFINITE
     * 
     * @param maxValue the new maximum value
     *
     * @throws IllegalArgumentException if <code>maxValue</code> is invalid
     * @see #getMaxValue
     */
    public void setMaxValue(int maxValue) {
        synchronized (Display.LCDUILock) {
            setMaxValueImpl(maxValue);
        }
    }

    /**
     * Gets the maximum value of this <code>Gauge</code> object.
     *
     * <p>If this gauge is interactive, the maximum value will be a positive 
     * integer.  If this gauge is non-interactive, the maximum value will be a 
     * positive integer (indicating that the gauge has definite range)
     * or the special value <code>INDEFINITE</code> (indicating that
     * the gauge has
     * indefinite range).</p>
     * 
     * @see #INDEFINITE
     * 
     * @return the maximum value of the <code>Gauge</code>, or
     * <code>INDEFINITE</code>
     * @see #setMaxValue
     */
    public int getMaxValue() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return maxValue;
    }

    /**
     * Tells whether the user is allowed to change the value of the
     * <code>Gauge</code>.
     * 
     * @return a boolean indicating whether the <code>Gauge</code> is
     * interactive
     */
    public boolean isInteractive() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return interactive;
    }

    // package private implementation

    /**
     * Sets the current value of this Gauge object.
     *
     * @param value the new value
     * @throws IllegalArgumentException if value is not one of
     * <code>CONTINUOUS_IDLE</code>,  <code>INCREMENTAL_IDLE</code>,
     * <code>CONTINUOUS_RUNNING</code>, or <code>INCREMENTAL_UPDATING</code>
     * for non-interactive gauges with indefinite range
     */
    void setValueImpl(int value) {
        if (!interactive && maxValue == INDEFINITE) {
            switch (value) {
            case CONTINUOUS_IDLE:
            case INCREMENTAL_IDLE:
            case INCREMENTAL_UPDATING:
            case CONTINUOUS_RUNNING:
                break;
            default:
                throw new IllegalArgumentException();
            }
        }

	if ((this.value != value) ||
	    (this.maxValue == INDEFINITE && value == INCREMENTAL_UPDATING)) {
	    int oldValue = this.value;
	    this.value = value;
	    checkValue();
	    gaugeLF.lSetValue(oldValue, this.value);
	}
    }

    /**
     * Return whether the Item takes user input focus.
     *
     * @return return <code>true</code> if contents is interactive or have
     * abstract commands.
     */
    boolean acceptFocus() {
	return super.acceptFocus() || interactive;
    }

    // private implementation


    /**
     * Utility method to ensure the value of the Gauge is always
     * in a range of 0 to maxValue, or if maxValue is INDEFINITE 
     * that value is CONTINUOUS_IDLE, INCREMENTAL_IDLE, 
     * INCREMENTAL_UPDATING, or CONTINUOUS_RUNNING.  In the case
     * where maxValue is INDEFINITE and value is not one of the
     * three defined here it will be set to CONTINUOUS_IDLE. (0)
     *
     * private instance variable value will be within parameter
     * after this call
     */
    private void checkValue() {
        if (maxValue == INDEFINITE) {
            if (value < CONTINUOUS_IDLE || value > INCREMENTAL_UPDATING) {
                value = CONTINUOUS_IDLE;
            }
        } else {
            if (value < 0) {
                value = 0;
            } else if (value > maxValue) {
                value = maxValue;
            }
        }
    }

    /**
     * Set the max value of this Gauge.  
     *
     * @param maxValue The maximum value to set for this Gauge
     * 
     * @throws IllegalArgumentException if maxValue is not positive for 
     * interactive gauges
     * @throws IllegalArgumentException if maxValue is neither positive nor
     * INDEFINITE for non-interactive gauges
     */
    private void setMaxValueImpl(int maxValue) {
        if (maxValue <= 0) {
            if (!(interactive == false && maxValue == INDEFINITE)) {
                throw new IllegalArgumentException();
            }
        }

        int oldMaxValue = this.maxValue;
        this.maxValue = maxValue;
	
        if (oldMaxValue == INDEFINITE) { // oldMaxValue
            if (maxValue > INDEFINITE) {
                value = 0;
            }
        } else if (maxValue == INDEFINITE) {
           value = CONTINUOUS_IDLE;
        }
	
        checkValue();

         if (oldMaxValue != maxValue) {
         gaugeLF.lSetMaxValue(oldMaxValue, maxValue);
	
        }
    }

    /**
     * The look&feel associated with this item. 
     * Set in the constructor. 
     */
    GaugeLF gaugeLF; // = null

    /** The current value of this gauge */
    int value;    // = 0

    /** The maximum possible value of this gauge */
    int maxValue; // = 0

    /** Whether this gauge is interactive or not */
    boolean interactive; // = false

}

