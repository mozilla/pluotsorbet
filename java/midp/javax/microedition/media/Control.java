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

package javax.microedition.media;


/**
 * A <code>Control</code> object is used to control some media 
 * processing functions.  The set of
 * operations are usually functionally related.  Thus a <code>Control</code>
 * object provides a logical grouping of media processing functions.
 * <p>
 * <code>Control</code>s are obtained from <code>Controllable</code>.
 * The <code>Player</code> interface extends <code>Controllable</code>.  
 * Therefore a <code>Player</code> implementation can use the 
 * <code>Control</code> interface
 * to extend its media processing functions.  For example,
 * a <code>Player</code> can expose a <code>VolumeControl</code> to allow
 * the volume level to be set. 
 * <p>
 * Multiple <code>Control</code>s can be implemented by the same object.
 * For example, an object can implement both <code>VolumeControl</code>
 * and <code>ToneControl</code>.  In this case, the object can be
 * used for controlling both the volume and tone generation.
 * <p>
 * The <code>javax.microedition.media.control</code> package specifies 
 * a set of pre-defined <code>Control</code>s.
 * 
 * @see Controllable
 * @see Player
 */
public interface Control {
}
