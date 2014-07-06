/*
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

package javax.microedition.io;

/**
 * Implementors of this interface encapsulate scheme-specific normalization of
 * port range specification for an URI.
 */
interface PortRangeNormalizer {
  /**
   * Given the host and the original port range specification string from 
   * an URI, returns the port range and the string representing port range
   * normalized as defined in RFC 3986 and the defining specification for the
   * scheme.
   * <p>
   * If <code>host</code>, <code>portspec</code> or <code>portRange</code> is
   * <code>null</code>, the behavior is undefined. If <code>portRange</code>
   * is not an array of two elements, the behavior is undefined.
   *
   * @param host the host specification from an URI
   * @param portspec the port range specification from an URI
   * @param portRange array of two elements to store the port range
   *
   * @throws IllegalArgumentException if <code>portspec</code> is malformed.
   *
   * @return the normalized port range specification string or 
   * <code>null</code> if no scheme-specific normalization is applicable
   */
  String normalize(String host, String portspec, int[] portRange);

  /**
   * Given the port range parsed from an URI, returns a string representation 
   * of the port range normalized for this protocol.
   *
   * @param portRange array of length two specifying port range
   *
   * @return the normalized port range specification string or 
   * <code>null</code> if no scheme-specific normalization is applicable
   */
  String normalize(int[] portRange);
}
