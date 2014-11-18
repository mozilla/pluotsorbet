/*
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

package javax.microedition.media.control;


/**
 * <code>GUIControl</code> extends <code>Control</code> and is defined
 * for controls that provide GUI functionalities.
 * <p>
 * <code>Control</code>s that support a GUI component
 * should implement this interface.
 */
public interface GUIControl extends javax.microedition.media.Control {

    /**
     * This defines a mode on how the GUI is displayed.
     * It is used in conjunction with 
     * <a href="#initDisplayMode(int, java.lang.Object)">
     * <code>initDisplayMode</code></a>.
     * <p>
     * When <code>USE_GUI_PRIMITIVE</code> is specified for
     * <code>initDisplayMode</code>, a GUI primitive will be
     * returned.  This object is where the GUI
     * of this control will be displayed. 
     * It can be used
     * in conjunction with other GUI objects, and conforms
     * to the GUI behaviors as specified by
     * the platform.
     * <p> 
     * For a given platform, the object returned
     * must implement or extend from the appropriate GUI primitive
     * of the platform.  For platforms that support only AWT such as 
     * some CDC implementations, the object must
     * extend from <code>java.awt.Component</code>; for MIDP 
     * implementations with only LCDUI support, it must extend from
     * <code>javax.microedition.lcdui.Item</code>.
     * <p>
     * In these cases, the <code>arg</code> argument must be 
     * <code>null</code> or a <code>String</code> that specifies
     * the fully-qualified classname of the GUI primitive.
     * <p>
     * On some platforms that support multiple types of GUI primitives,
     * the <code>arg</code> argument must be used to arbitrate among the
     * options.  The <code>arg</code> argument must be a
     * <code>String</code> that specifies the fully-qualified
     * classname of the GUI primitive to be returned by the method.
     * <p>
     * For example, a platform that supports both AWT and LCDUI
     * must use either <code>"java.awt.Component"</code> or
     * <code>"javax.microedition.lcdui.Item"</code> as the
     * <code>arg</code> argument.  The object returned will be
     * of either type according to what's specified.
     * <p>
     * Here are some sample usage scenarios:
     * <p>
     * For CDC implementations with only AWT support: 
     * <pre>
     * <code>
     *   try {
     *       Player p = Manager.createPlayer("http://abc.mpg");
     *       p.realize();
     *       GUIControl gc;
     *       if ((gc = (GUIControl)p.getControl("GUIControl")) != null)
     *           add((Component)gc.initDisplayMode(GUIControl.USE_GUI_PRIMITIVE, null));
     *       p.start();
     *   } catch (MediaException pe) {
     *   } catch (IOException ioe) {
     *   }
     * </code>
     * </pre>
     * <p>
     * For MIDP implementations with only LCDUI support:
     * <pre>
     * <code>
     *   try {
     *       Player p = Manager.createPlayer("http://abc.mpg");
     *       p.realize();
     *       GUIControl gc;
     *       if ((gc = (GUIControl)p.getControl("GUIControl")) != null) {
     *           Form form = new Form("My GUI");
     *           form.append((Item)gc.initDisplayMode(GUIControl.USE_GUI_PRIMITIVE, null));
     *           Display.getDisplay().setCurrent(form);
     *       }
     *       p.start();
     *   } catch (MediaException pe) {
     *   } catch (IOException ioe) {
     *   }
     * </code>
     * </pre>
     * <p>
     * For implementations with both AWT and LCDUI support: 
     * <pre>
     * <code>
     *   try {
     *       Player p = Manager.createPlayer("http://abc.mpg");
     *       p.realize();
     *       GUIControl gc;
     *       if ((gc = (GUIControl)p.getControl("GUIControl")) != null)
     *           add((Component)gc.initDisplayMode(GUIControl.USE_GUI_PRIMITIVE,
     *                                   "java.awt.Component");
     *       p.start();
     *   } catch (MediaException pe) {
     *   } catch (IOException ioe) {
     *   }
     * </code>
     * </pre>
     * <p>
     * Value 0 is assigned to <code>USE_GUI_PRIMITIVE</code>.
     */
    int USE_GUI_PRIMITIVE = 0;

    /**
     * Initialize the mode on how the GUI is displayed.
     *
     * @param mode The mode that determines how the GUI is
     * displayed.  <code>GUIControl</code> defines only
     * one mode:
     * <a href="#USE_GUI_PRIMITIVE"><code>USE_GUI_PRIMITIVE</code></a>.
     * Subclasses of this may introduce more modes.
     *
     * @param arg The exact semantics of this argument is
     * specified in the respective mode definitions.
     *
     * @return The exact semantics and type of the object returned
     * are specified in the respective mode definitions.
     * 
     * @exception IllegalStateException Thrown if 
     * <code>initDisplayMode</code> is called again after it has 
     * previously been called successfully.
     *
     * @exception IllegalArgumentException Thrown if
     * the <code>mode</code> or <code>arg</code> 
     * argument is invalid.   <code>mode</code> must be
     * defined by GUIControl or its subclasses; or a custom mode
     * supported by this implementation.
     * <code>arg</code> must conform to the 
     * constraints defined by the 
     * respective mode definitions.
     * Refer to the mode definitions for the required type
     * of <code>arg</code>.
     */
    Object initDisplayMode(int mode, Object arg);
}
