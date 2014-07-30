/*
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

package com.sun.midp.chameleon.skins.resources;

/**
 * Constants shared between various skin resources.
 */
public class SkinResourcesConstants {

    /** 
     * Skin file name 
     */
    public static final String SKIN_BINARY_FILE_NAME = "skin.bin";

    /** 
     * Magic sequence identifying skin file 
     */
    public static final short[] CHAM_BIN_MAGIC = { 0x89, 'C', 'H', 'A', 'M'};

    /** 
     * Expected (current) version of skin file format 
     */
    public static final int CHAM_BIN_FORMAT_VERSION = 0x00000003;

    /**
     * Constant for centering skin element horizontally
     * around the anchor point
     */
    public static final int HCENTER = 1;
  
    /**
     * Constant for centering skin element vertically 
     * around the anchor point.
     */
    public static final int VCENTER = 2;
  
    /**
     * Constant for positioning skin element to the left 
     * of the anchor point.
     */
    public static final int LEFT = 4;
  
    /**
     * Constant for positioning skin element to the right 
     * of the anchor point.
     */
    public static final int RIGHT = 8;
  
    /**
     * Constant for positioning skin element above 
     * the anchor point.
     */
    public static final int TOP = 16;
  
    /**
     * Constant for positioning skin elemen below 
     * the anchor point.
     */
    public static final int BOTTOM = 32;

    /**
     * Constant for the solid stroke style.
     */
    public static final int SOLID = 0;

    /**
     * Constant for the dotted stroke style.
     */
    public static final int DOTTED = 1;

    /** 
     * UTF8 string encoding 
     */
    public static final byte STRING_ENCODING_USASCII = 0;

    /** 
     * US ASCII string encoding 
     */
    public static final byte STRING_ENCODING_UTF8 = 1;
}
