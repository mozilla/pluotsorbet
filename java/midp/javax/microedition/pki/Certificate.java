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

package javax.microedition.pki;

import java.lang.String;

/**
 * Interface common to certificates.
 * The features abstracted of <CODE>Certificates</CODE> include subject,
 * issuer, type, version, serial number, signing algorithm, dates of valid use,
 * and serial number.
 * <p>
 * <b>Printable Representation for Binary Values</b></p>
 * <p>
 * A non-string values in a certificate are represented as strings with each
 * byte as two hex digits (capital letters for A-F) separated by ":" (Unicode
 * U+003A).</p>
 * <p>
 * For example: <tt>0C:56:FA:80</tt></p>
 * <p>
 * <b>Printable Representation for X.509 Distinguished Names</b></p>
 * <p>
 * For a X.509 certificate the value returned is the printable version of
 * the distinguished name (DN) from the certificate.</p>
 * <p>
 * An X.509 distinguished name of is set of attributes, each attribute is a
 * sequence of an object ID and a value. For string comparison purposes, the
 * following rules define a strict printable representation.</p>
 * <p>
 * <ol>
 * <li>There is no added white space around separators.</li>
 *
 * <li>The attributes are in the same order as in the certificate;
 * attributes are not reordered.</li>
 *
 * <li>If an object ID is in the table below, the label from the table
 * will be substituted for the object ID, else the ID is formatted as
 * a string using the binary printable representation above.</li>
 *
 * <li>Each object ID or label and value within an attribute will be
 * separated by a "=" (Unicode U+003D), even if the value is empty.</li>
 *
 * <li>If value is not a string, then it is formatted as a string using the
 * binary printable representation above.</li>
 *
 * <li>Attributes will be separated by a ";" (Unicode U+003B)</li>
 * </ol>
 * </p>
 * <br><b>Labels for X.500 Distinguished Name Attributes</b>
 * <table border="1" cellpadding=4 cellspacing=0 width="100%">
 *
 *   <tr>
 *     <th bgcolor="#CCCCFF">Object ID</th>
 *     <th bgcolor="#CCCCFF">Binary</th>
 *     <th bgcolor="#CCCCFF">Label</th>
 *   <tr>
 *     <td>id-at-commonName</td>
 *     <td><tt>55:04:03</tt></td>
 *     <td>CN</td>
 *   </tr>
 *   <tr>
 *     <td>id-at-surname</td>
 *     <td><tt>55:04:04</tt></td>
 *     <td>SN</td>
 *   </tr>
 *   <tr>
 *     <td>id-at-countryName</td>
 *     <td><tt>55:04:06</tt></td>
 *     <td>C</td>
 *   </tr>
 *   <tr>
 *     <td>id-at-localityName</td>
 *     <td><tt>55:04:07</tt></td>
 *     <td>L</td>
 *   </tr>
 *   <tr>
 *     <td>id-at-stateOrProvinceName</td>
 *     <td><tt>55:04:08</tt></td>
 *     <td>ST</td>
 *   </tr>
 *   <tr>
 *     <td>id-at-streetAddress</td>
 *     <td><tt>55:04:09</tt></td>
 *     <td>STREET</td>
 *   </tr>
 *   <tr>
 *     <td>id-at-organizationName</td>
 *     <td><tt>55:04:0A</tt></td>
 *     <td>O</td>
 *   </tr>
 *   <tr>
 *     <td>id-at-organizationUnitName</td>
 *     <td><tt>55:04:0B</tt></td>
 *     <td>OU</td>
 *   </tr>
 *   <tr>
 *     <td>emailAddress</td>
 *     <td><tt>2A:86:48:86:F7:0D:01:09:01</tt></td>
 *     <td>EmailAddress</td>
 *   </tr>
 * </table>
 * <p>
 * Example of a printable distinguished name:</p>
 * <blockquote>
 * <tt>C=US;O=Any Company, Inc.;CN=www.anycompany.com</tt></blockquote>
 *
 */

public interface Certificate {
   
    /**
     * Gets the name of this certificate's subject.
     * @return The subject of this <CODE>Certificate</CODE>;
     * the value MUST NOT be <CODE>null</CODE>.
     */
    public String getSubject();

    /**
     * Gets the name of this certificate's issuer.
     * @return The issuer of the <CODE>Certificate</CODE>;
     * the value MUST NOT be <CODE>null</CODE>.
     */
    public String getIssuer();

    /**
     * Get the type of the <CODE>Certificate</CODE>.
     * For X.509 Certificates the value returned is "X.509".
     * 
     * @return The type of the <CODE>Certificate</CODE>;
     * the value MUST NOT be <CODE>null</CODE>.
     */
    public String getType();

    /**
     * Gets the version number of this <CODE>Certificate</CODE>.
     * The format of the version number depends on the specific
     * type and specification.
     * For a X.509 certificate per RFC 2459 it would be "2".
     * @return The version number of the <CODE>Certificate</CODE>;
     * the value MUST NOT be <CODE>null</CODE>.
     */
    public String getVersion();

    /**
     * Gets the name of the algorithm used to sign the
     * <CODE>Certificate</CODE>. 
     * The algorithm names returned should be the labels
     * defined in RFC2459 Section 7.2.
     * @return The name of signature algorithm;
     * the value MUST NOT be <CODE>null</CODE>.
     */
    public String getSigAlgName();

    /**
     * Gets the time before which this <CODE>Certificate</CODE> may not be used
     * from the validity period. 
     *
     * @return The time in milliseconds before which the
     *  <CODE>Certificate</CODE> is not valid; it MUST be positive,
     *  <CODE>0</CODE> is returned if the certificate does not
     *  have its validity restricted based on the time.
     */
    public long getNotBefore();

    /**
     * Gets the time after which this <CODE>Certificate</CODE> may not be used
     * from the validity period. 
     * @return The time in milliseconds after which the
     *  <CODE>Certificate</CODE> is not valid (expiration date);
     *  it MUST be positive; <CODE>Long.MAX_VALUE</CODE> is returned if
     *  the certificate does not have its validity restricted based on the
     *  time.
     */
    public long getNotAfter();

    /**
     * Gets the printable form of the serial number of this
     * <CODE>Certificate</CODE>. 
     * If the serial number within the <CODE>certificate</CODE>
     * is binary it should be formatted as a string using the binary printable
     * representation in class description.
     * For example,  0C:56:FA:80.
     * @return A string containing the serial number
     * in user-friendly form; <CODE>null</CODE> is returned
     * if there is no serial number.
     */
    public String getSerialNumber();

}
