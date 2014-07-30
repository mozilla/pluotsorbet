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

package com.sun.midp.chameleon;

/** The element of the layer list with forward and backward links */
class CLayerElement {
   /** The layer instance */
   protected CLayer layer;
   /** The next upper layer in the layers list */
   protected CLayerElement upper;
   /** The previous lower layer in the layers list */
   protected CLayerElement lower;

   /**
    * Construct layer element with specified content and links
    *
    * @param layer element conten
    * @param lower reference to the previous lower layer element
    * @param upper reference to the next upper layer element
    */
   CLayerElement(CLayer layer,
           CLayerElement lower, CLayerElement upper) {
       this.layer = layer;
       this.upper = upper;
       this.lower = lower;
   }

   /**
    * Get list element with next uppper layer
    * @return list element with upper layer inside, null if none
    */
   CLayerElement getUpper() {
       return upper;
   }

   /**
    * Get list element with previous lower layer
    * @return list element with lower layer inside, null if none
    */
   CLayerElement getLower() {
       return lower;
   }

   /**
    * Get layer instance stored by list element
    * @return
    */
   CLayer getLayer() {
       return layer;
   }
}