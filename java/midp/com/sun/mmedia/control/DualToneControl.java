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

package com.sun.mmedia.control;

/**
 * <code>DualToneControl</code> is the interface to enable playback of a 
 * user-defined monotonic tone sequence with dual tone capabilities.
 * <p>
 * A tone sequence is specified as a list of tone-duration pairs and
 * user-defined sequence blocks.  The list is packaged as an 
 * array of bytes.  The <code>setSequence</code> method is used to 
 * input the sequence to the <code>ToneControl</code>.  In addition,
 * the tone sequence format specified below can also be used as a 
 * file format to define tone sequences.  A file containing a
 * tone sequence as specified must use ".jts" as the file extension.  
 * <code>"audio/x-tone-seq"</code> designates the MIME type for this
 * format.
 * <p>
 * <a name="tone_sequence_format"></a>
 * The syntax of a tone sequence is described in 
 * <a href="http://www.ietf.org/rfc/rfc2234">Augmented BNF</a> notations:
 * <blockquote>
 * <pre>
 * sequence              = version *1tempo_definition *1resolution_definition 
 *		             *block_definition 1*sequence_event *dualtone_definition
 *
 * version               = VERSION version_number
 * VERSION               = byte-value
 * version_number        = 1	; version # 1
 *
 * tempo_definition      = TEMPO tempo_modifier
 * TEMPO                 = byte-value
 * tempo_modifier        = byte-value 
 *              ; multiply by 4 to get the tempo (in bpm) used 
 *              ; in the sequence.
 * 
 * resolution_definition = RESOLUTION resolution_unit
 * RESOLUTION            = byte-value
 * resolution_unit       = byte-value
 *
 * block_definition      = BLOCK_START block_number 
 *                            1*sequence_event 
 *                         BLOCK_END block_number
 * BLOCK_START           = byte-value
 * BLOCK_END             = byte-value
 * block_number          = byte-value 
 *              ; block_number specified in BLOCK_END has to be the 
 *              ; same as the one in BLOCK_START 
 *
 * sequence_event        = tone_event / block_event / 
 *                           volume_event / repeat_event
 * 
 * tone_event            = note duration
 * note                  = byte-value ; note to be played
 * duration              = byte-value ; duration of the note
 *
 * block_event           = PLAY_BLOCK block_number
 * PLAY_BLOCK            = byte-value
 * block_number          = byte-value 
 *              ; block_number must be previously defined 
 *              ; by a full block_definition
 *
 * volume_event          = SET_VOLUME volume
 * SET_VOLUME            = byte-value
 * volume                = byte-value ; new volume
 *
 * repeat_event          = REPEAT multiplier tone_event
 * REPEAT                = byte-value
 * multiplier            = byte-value  
 *              ; number of times to repeat a tone
 *
 * dualtone_definition   = DUALTONE duration note note
 * DUALTONE              = byte-value
 *
 * byte-value            = -128 - 127
 *              ; the value of each constant and additional
 *              ; constraints on each parameter are specified below.
 * </pre>
 * </blockquote>
 * 
 * <A HREF="#DUALTONE"><code>DUALTONE</code></A>
 * is pre-defined constant.
 * <p>
 *
 */
public interface DualToneControl extends javax.microedition.media.control.ToneControl {

    /** 
     * The DUALTONE attribute tag.
     * <p>
     * Value -50 is assigned to <code>DUALTONE</code>.
     */
    byte DUALTONE = -50;
}
