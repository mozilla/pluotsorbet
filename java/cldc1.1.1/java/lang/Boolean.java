/*
 *   
 *
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
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

package java.lang;

/**
 * The Boolean class wraps a value of the primitive type
 * <code>boolean</code> in an object. An object of type
 * <code>Boolean</code> contains a single field whose type is
 * <code>boolean</code>.
 *
 * @version 12/17/01 (CLDC 1.1)
 * @since   JDK1.0, CLDC 1.0
 */
public final
class Boolean {

    /** 
     * The <code>Boolean</code> object corresponding to the primitive 
     * value <code>true</code>. 
     */
    public static final Boolean TRUE = new Boolean(true);

    /** 
     * The <code>Boolean</code> object corresponding to the primitive 
     * value <code>false</code>. 
     */
    public static final Boolean FALSE = new Boolean(false);

    /**
     * The value of the Boolean.
     */
    private boolean value;

    /**
     * Allocates a <code>Boolean</code> object representing the
     * <code>value</code> argument.
     *
     * @param   value   the value of the <code>Boolean</code>.
     */
    public Boolean(boolean value) {
        this.value = value;
    }

    /**
     * Returns the value of this <tt>Boolean</tt> object as a boolean
     * primitive.
     *
     * @return  the primitive <code>boolean</code> value of this object.
     */
    public boolean booleanValue() {
        return value;
    }

    /**
     * Returns a String object representing this Boolean's value.
     * If this object represents the value <code>true</code>, a string equal
     * to <code>"true"</code> is returned. Otherwise, a string equal to
     * <code>"false"</code> is returned.
     *
     * @return  a string representation of this object.
     */
    public String toString() {
      return value ? "true" : "false";
    }

    /**
     * Returns a hash code for this <tt>Boolean</tt> object.
     *
     * @return  the integer <tt>1231</tt> if this object represents
     * <tt>true</tt>; returns the integer <tt>1237</tt> if this
     * object represents <tt>false</tt>.
     */
    public int hashCode() {
        return value ? 1231 : 1237;
    }

    /**
     * Returns <code>true</code> if and only if the argument is not
     * <code>null</code> and is a <code>Boolean </code>object that
     * represents the same <code>boolean</code> value as this object.
     *
     * @param   obj   the object to compare with.
     * @return  <code>true</code> if the Boolean objects represent the
     *          same value; <code>false</code> otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Boolean) {
            return value == ((Boolean)obj).booleanValue();
        }
        return false;
    }

}
