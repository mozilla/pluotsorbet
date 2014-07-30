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

package com.sun.midp.io;

/**
 * Parse path to file storage.
 * Can work like encoder: searching unsafe characters and
 * replace its on its US-ASCII hexadecimal representation.
 * Also doing back work: decodes encoded file path.
 * Based on RFC 1738. 
 * <p>
 * Checks file path for encoding unsafe characters 
 * and vice versa. 
 */
public class FileUrl {
    
    /** Array of unsafe characters. */
    private final static char[] unsafeCharacters = {
        ' ', '<', '>', '{', '}', '|',
        '\\', '^', '~', '[', ']', '`', '%'
    };
    
    /** Array of unsafe characters ASCII codes. */
    private final static String[] charactersASCIICode = {
        "%20", "%3C", "%3E", "%7B", "%7D", "%7C",
        "%5C", "%5E", "%7E", "%5B", "%5D", "%60", "%26"
    };
    
    /** Prevents anyone from instantiating this class */
    private FileUrl() {
        
    }
        
    /**
     * Decode file path: if it contains US-ASCII symbols,
     * than reverse its on its literal representation.
     * 
     * @param filenamepath  path to filename
     * @return  decoded string of file path
     */
    public static String decodeFilePath(String filenamepath) {
        if (filenamepath == null) {
            return null;
        } else {
            char[] fileChars  = new char[filenamepath.length()];
            StringBuffer buffer = new StringBuffer(filenamepath.length());
            char temp;
                
            filenamepath.getChars(0,filenamepath.length(),fileChars,0);
        
            for (int i = 0; i < fileChars.length ; i++) {
                
                if (fileChars[i] == '%' && i < fileChars.length-2) {                    
                    temp = decodeASCIISymbol(filenamepath.substring(i,i+3));
                    
                    if (temp != '0') {
                        buffer.append(temp);
                        i=i+2;   
                    } else
                        buffer.append(fileChars[i]);
                    
                } else {
                    buffer.append(fileChars[i]);
                }
             }
        
             return buffer.toString();
        }
    }
     
    /**
     * Encode file path: if it contains unsafe symbols,
     * according to RFC 1738.
     *  
     * @param filenamepath  path to filename
     * @return  encoded file path
     */
    public static String encodeFilePath(String filenamepath) {
        if (filenamepath == null) {
            return null;
        } else {
            char[] fileChars = new char[filenamepath.length()];
            StringBuffer buffer = new StringBuffer(filenamepath.length());
            String temp;
        
            filenamepath.getChars(0,filenamepath.length(),fileChars,0);
        
            for (int i = 0; i < fileChars.length; i++)
            {
              temp = encodeUnsafeCharacter(fileChars[i]);
              
              if (temp == null) 
                  buffer.append(fileChars[i]);
              else
                  buffer.append(temp);
            }
            
            return buffer.toString();
       }
    }
    
    /**
     * Encodes character to it US-ASCII code.
     * 
     * @param character
     * @return the code of unsafe character
     * or null if character is reserved.
     */
    private static String encodeUnsafeCharacter(char character) {
        for (int i = 0; i < unsafeCharacters.length; i++) {
            if (character == unsafeCharacters[i])
                return charactersASCIICode[i];
        }
        
        return null;
    }
    
    /**
     * Decodes ASCII code string to it
     * literal representation.
     * 
     * @param asciiCode string with ASCII code
     * @return specific character or '0', if 
     * we don`t need to decode this string
     */
    private static char decodeASCIISymbol(String asciiCode) {
        for (int i = 0; i < charactersASCIICode.length; i++) {
            if (asciiCode.equals(charactersASCIICode[i]))
                return unsafeCharacters[i];
        }
        
        return '0';
    }         
            
}
