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

package com.sun.j2me.pim.formats;

import com.sun.j2me.pim.ContactImpl;
import com.sun.j2me.pim.LineReader;
import com.sun.j2me.pim.PIMFormat;
import com.sun.j2me.pim.PIMHandler;
import com.sun.j2me.pim.UnsupportedPIMFormatException;
import com.sun.j2me.pim.AbstractPIMList;
import com.sun.j2me.pim.AbstractPIMItem;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import javax.microedition.pim.Contact;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.PIMList;
import com.sun.j2me.jsr75.StringUtil;

/**
 * Partial implementation of PIMEncoding for VCard/2.1 and VCard/3.0.
 *
 */
public abstract class VCardFormat extends EndMatcher implements PIMFormat {

    /**
     * Returns the version number of vCard implemented.
     * @return the VCard version number
     */
    protected abstract String getVersion();

    /**
     * Gets the vCard property name used to store categories.
     * @return the vCard category property name
     */
    protected abstract String getCategoryProperty();

    /**
     * Gets the vCard property name used to store classes.
     * @return the class property name
     */
    protected abstract String getClassProperty();

    /**
     * Gets the binary value describing all flags in a vCard line.
     * @param attributes fields to be parsed
     * @return binary coded settings
     */
    protected abstract int parseAttributes(String[] attributes);

    /**
     * Gets the name of the default binary encoding.
     * This is "BASE64" for vCard 2.1 and "B" for vCard 3.0.
     * @return the default binary encoding
     */
    protected abstract String getBinaryEncodingName();

    /**
     * VCard formatting class.
     */
    public VCardFormat() {
        super("VCARD");
    }

    /**
     * Gets the code name of this encoding (e.g. "VCARD/2.1").
     * @return the encoding name
     */
    public String getName() {
        return "VCARD/" + getVersion();
    }

    /**
     * Checks to see if a given PIM list type is supported by this encoding.
     * @param pimListType int representing the PIM list type to check
     * @return true if the type can be read and written by this encoding,
     * false otherwise
     */
    public boolean isTypeSupported(int pimListType) {
        return pimListType == PIM.CONTACT_LIST;
    }

    /**
     * Serializes a PIMItem.
     * @param out Stream to which serialized data is written
     * @param encoding Character encoding to use for serialized data
     * @param pimItem The item to write to the stream
     * @throws IOException if an error occurs while writing
     */
    public void encode(OutputStream out, String encoding, PIMItem pimItem)
        throws IOException {
        Writer w = new OutputStreamWriter(out, encoding);
        w.write("BEGIN:VCARD\r\n");
        w.write("VERSION:");
        w.write(getVersion());
        w.write("\r\n");
        // write all fields
        int[] fields = pimItem.getFields();
        FormatSupport.sort(fields);
        for (int i = 0; i < fields.length; i++) {
            int valueCount = pimItem.countValues(fields[i]);
            for (int j = 0; j < valueCount; j++) {
                writeValue(w, pimItem, fields[i], j);
            }
        }
        // write categories.
        String categories = StringUtil.join(pimItem.getCategories(), ",");
        if (categories.length() > 0) {
            w.write(getCategoryProperty());
            w.write(":");
            w.write(categories);
            w.write("\r\n");
        }
        w.write("END:VCARD\r\n");
        w.flush();
    }

    /**
     * Writes a single vCard line.
     * @param w output stream target
     * @param item the data to to written
     * @param field the attribute to be processed
     * @param index the offset of the data to be processed
     * @throws IOException if an error occurs while writing
     */
    protected void writeValue(Writer w, PIMItem item, int field, int index)
        throws IOException {

        String label = VCardSupport.getFieldLabel(field);
        switch (field) {
            case Contact.FORMATTED_NAME:
            case Contact.FORMATTED_ADDR:
            case Contact.PHOTO_URL:
            case Contact.TEL:
            case Contact.EMAIL:
            case Contact.TITLE:
            case Contact.ORG:
            case Contact.NICKNAME:
            case Contact.NOTE:
            case Contact.UID:
            case Contact.URL:
            case Contact.PUBLIC_KEY_STRING: {
                String sValue = item.getString(field, index);
                if (sValue != null) {
                    w.write(label);
                    writeAttributes(w, item.getAttributes(field, index));
                    w.write(":");
                    w.write(sValue);
                    w.write("\r\n");
                }
                break;
            }
            case Contact.NAME:
            case Contact.ADDR: {
                String[] aValue = item.getStringArray(field, index);
                if (aValue != null) {
                    w.write(label);
                    writeAttributes(w, item.getAttributes(field, index));
                    w.write(":");
                    writeStringArray(w, aValue);
                    w.write("\r\n");
                }
                break;
            }
            case Contact.PHOTO: {
                String beeValue = new String(item.getBinary(field, index));
                if ( beeValue != null ){  
                    w.write(label);
                    w.write(";ENCODING=");
                    w.write(getBinaryEncodingName());
                    writeAttributes(w, item.getAttributes(field, index));
                    
                    byte[] rawData = Base64Encoding.fromBase64( beeValue );
                    if (rawData.length > 0) {
                        String imageType = getImageType( rawData );
                        if( imageType != null ) {
                            w.write(";TYPE=");
                            w.write(imageType);
                        }
                    }
                    w.write(":\r\n");
                    
                    //Re-encode the image data to make sure that each line 
                    //of the Base64 format data has at most 76 + 4 characters                   
                    w.write(Base64Encoding.toBase64( rawData, 76, 4 ));
                    w.write("\r\n");
                }
                break;
            }

            case Contact.PUBLIC_KEY: {
                String beeValue = new String(item.getBinary(field, index));
                if (beeValue != null) {
                    w.write(label);
                    w.write(";ENCODING=");
                    w.write(getBinaryEncodingName());
                    writeAttributes(w, item.getAttributes(field, index));
                    w.write(":\r\n");

                    //Force to re-encode the binary data
                    w.write(Base64Encoding.toBase64( 
                            Base64Encoding.fromBase64( beeValue ),
                            76, 4 ));
                    w.write("\r\n");
                }
                break;
            }
            case Contact.BIRTHDAY:
                w.write(label);
                writeAttributes(w, item.getAttributes(field, index));
                w.write(":");
                writeDate(w, item.getDate(field, index));
                w.write("\r\n");
                break;
            case Contact.REVISION:
                w.write(label);
                writeAttributes(w, item.getAttributes(field, index));
                w.write(":");
                writeDateTime(w, item.getDate(field, index));
                w.write("\r\n");
                break;
            case Contact.CLASS: {
                int iValue = item.getInt(field, index);
                String sValue = VCardSupport.getClassType(iValue);
                if (sValue != null) {
                    w.write(getClassProperty());
                    writeAttributes(w, item.getAttributes(field, index));
                    w.write(":");
                    w.write(sValue);
                    w.write("\r\n");
                }
                break;
            }
            default:
                // field cannot be written. ignore it.
        }
    }

    /**
     * Writes a vCard field with multiple elements, such as ADR.
     * @param w output stream target
     * @param data the strings to write
     * @throws IOException if an error occurs while writing
     */
    protected void writeStringArray(Writer w, String[] data)
        throws IOException {
        for (int i = 0; i < data.length; i++) {
            if (data[i] != null) {
                w.write(data[i]);
            }
            if (i != data.length -1) {
                w.write(';');
            }
        }
    }

    /**
     * Writes a vCard date field.
     * @param w output stream target
     * @param date data to be written
     * @throws IOException if an error occurs while writing
     */
    protected void writeDate(Writer w, long date) throws IOException {
        w.write(PIMHandler.getInstance().composeDate(date));
    }

    /**
     * Writes a vCard date time field.
     * @param w output stream target
     * @param date data to be written
     * @throws IOException if an error occurs while writing
     */
    protected void writeDateTime(Writer w, long date) throws IOException {
        w.write(PIMHandler.getInstance().composeDateTime(date));
    }
    
    /**
     * Writes the attributes for a field.
     * @param w output stream target
     * @param attributes data to be written
     * @throws IOException if an error occurs while writing
     */
    protected abstract void writeAttributes(Writer w, int attributes)
        throws IOException;

    /**
     * Constructs one or more PIMItems from serialized data.
     * @param in Stream containing serialized data
     * @param encoding Character encoding of the stream
     * @param list PIMList to which items should be added, or null if the items
     * should not be part of a list
     * @throws UnsupportedPIMFormatException if the serialized data cannot be
     * interpreted by this encoding.
     * @return a non-empty array of PIMItems containing the objects described in
     * the serialized data, or null if no items are available
     * @throws IOException if an error occurs while reading
     */
    public PIMItem[] decode(InputStream in, String encoding, PIMList list)
        throws IOException {

        LineReader r = new LineReader(in, encoding, this);
        ContactImpl contact = decode(r, list);
        if (contact == null) {
            return null;
        } else {
            return new ContactImpl[] { contact };
        }
    }

    /**
     * Constructs a single PIMItem from serialized data.
     * @param in LineReader containing serialized data
     * @param list PIM list to which the item belongs
     * @throws UnsupportedPIMFormatException if the serialized data cannot be
     * interpreted by this encoding.
     * @return an unserialized Contact, or null if no data was available
     */
    private ContactImpl decode(LineReader in, PIMList list)
    throws IOException {
        String line = in.readLine();
        if (line == null) {
            return null;
        }
        if (!line.toUpperCase().equals("BEGIN:VCARD")) {
            throw new UnsupportedPIMFormatException("Not a vCard :'"
                + line + "'");
        }
        String categoryProperty = getCategoryProperty();
        ContactImpl contact = new ContactImpl((AbstractPIMList)list);
        while ((line = in.readLine()) != null) {
            FormatSupport.DataElement element =
                FormatSupport.parseObjectLine(line);
            if ( element.propertyName.equals( "PHOTO" ) ) {
                element = parsePhoto(in, element, contact );            
            } else if ( element.propertyName.equals( "KEY" ) ) {
                element = parsePublicKey(in, element, contact );
            }

            if (element.propertyName.equals("END")) {
                return contact;
            } else if (element.propertyName.equals("VERSION")) {
                if (!element.data.equals(getVersion())) {
                    throw new UnsupportedPIMFormatException("Version "
                        + element.data + " not supported");
                }
            } else if (element.propertyName.equals(categoryProperty)) {
                String[] categories = StringUtil.split(element.data, ',', 0);
                for (int j = 0; j < categories.length; j++) {
                    try {
                        contact.addToCategory(categories[j]);
                    } catch (PIMException e) {
                        // cannot add to category
                    }
                }
            } else {
                importData(contact,
                           element.propertyName,
                           element.attributes,
                           element.data,
                           contact.getPIMListHandle());
            }
        }
        throw new IOException("Unterminated vCard");
    }

    /**
     * Parses a single vCard line.
     * @param contact element to populate
     * @param prefix filter for selecting fields
     * @param attributes fields to be processed
     * @param data input to be filtered
     * @param listHandle handle of the list containing the contact being parsed
     */
    private void importData(Contact contact,
    String prefix, String[] attributes, String data, Object listHandle) {

        int attr = parseAttributes(attributes);
        int field = VCardSupport.getFieldCode(prefix);
        if (field == -1 && prefix.equals(getClassProperty())) {
            field = Contact.CLASS;
        }
        if (!PIMHandler.getInstance().isSupportedField(listHandle, field)) {
            return;
        }
        switch (field) {
            case Contact.FORMATTED_NAME:
            case Contact.FORMATTED_ADDR:
            case Contact.TEL:
            case Contact.EMAIL:
            case Contact.TITLE:
            case Contact.ORG:
            case Contact.NICKNAME:
            case Contact.NOTE:
            case Contact.UID:
            case Contact.URL: {
                String sdata = FormatSupport.parseString(attributes, data);
                contact.addString(field, attr, sdata);
                break;
            }
            case Contact.NAME:
            case Contact.ADDR: {
                String[] elements =
                FormatSupport.parseStringArray(attributes, data);
                int elementCount = PIMHandler.getInstance()
                    .getStringArraySize(listHandle, field);
                if (elements.length != elementCount) {
                    String[] a = new String[elementCount];
                    System.arraycopy(elements, 0, a, 0,
                        Math.min(elements.length, elementCount));
                    elements = a;
                }
                contact.addStringArray(field, attr, elements);
                break;
            }
            case Contact.BIRTHDAY:
            case Contact.REVISION: {
                int dateLen = data.length();
                // end date is either date in yyyyMMdd format or
                // date/time in yyyymmddThhmmss(Z).
                long date = (dateLen < 15) ?
                    PIMHandler.getInstance().parseDate(data) :
                    PIMHandler.getInstance().parseDateTime(data);
                contact.addDate(field, attr, date);
                break;
            }
            case Contact.PHOTO: {
                String valueType =
                FormatSupport.getAttributeValue(attributes, "VALUE=", null);
                if (valueType == null) {
                    // binary data represented in "B" encoded format
                    
                    byte[] bData = data.getBytes();
                    if (bData.length != 0) {
                        contact.addBinary(Contact.PHOTO, attr, bData, 0,
                                          bData.length);
                    }
                } else if (valueType.equals("URL")) {
                    String sdata = FormatSupport.parseString(attributes, data);
                    contact.addString(Contact.PHOTO_URL, attr, sdata);
                } else {
                    // ignore; value type not recognized
                }
                break;
            }
            case Contact.PUBLIC_KEY: {
                byte[] beeData = data.getBytes();
                if (beeData.length != 0) {
                    contact.addBinary(Contact.PUBLIC_KEY, attr, beeData, 0,
                                      beeData.length);
                }
                break;
            }
            case Contact.CLASS: {
                int i = VCardSupport.getClassCode(data);
                if (i != -1) {
                    contact.addInt(Contact.CLASS, Contact.ATTR_NONE, i);
                }
                break;
            }
            default:
                // System.err.println("No match for field prefix '"
                // + prefix + "' (REMOVE THIS MESSAGE)");
        }
    }

    private String getImageType(byte[] rawData){        
        if ( rawData[0] == (byte)0x42 && rawData[1] == (byte)0x4d ) {
            return "BMP";
        }
        if ( rawData[0] == (byte)0xff && rawData[1] == (byte)0xd8
             && rawData[2] == (byte)0xff && rawData[3] == (byte)0xe0) {
             return "JPEG";
        }
        if ( rawData[0] == (byte)0x49 && rawData[1] == (byte)0x49
             && rawData[2] == (byte)0x2a ) {
             return "TIFF";
        }
        if ( rawData[0] == (byte)0x47 && rawData[1] == (byte)0x49
             && rawData[2] == (byte)0x46 ) {
             return "GIF";
        }
        if ( rawData[0] == (byte)0x89 && rawData[1] == (byte)0x50
             && rawData[2] == (byte)0x4e && rawData[3] == (byte)0x47
             && rawData[4] == (byte)0x0d && rawData[5] == (byte)0x0a
             && rawData[6] == (byte)0x1a && rawData[7] == (byte)0x0a
             && rawData[8] == (byte)0x00 && rawData[9] == (byte)0x00
             && rawData[10] == (byte)0x00 && rawData[11] == (byte)0x0d
             && rawData[12] == (byte)0x49 && rawData[13] == (byte)0x48
             && rawData[14] == (byte)0x44 && rawData[15] == (byte)0x52 ) {
             return "PNG";
        }
        return null;
    }

    private FormatSupport.DataElement parsePhoto( LineReader in, FormatSupport.DataElement element, 
                                ContactImpl contact ) 
                                throws IOException {   
        String encodingName = FormatSupport.getAttributeValue(
                element.attributes, "ENCODING=", null);
        if ( encodingName == null ) {//Use URL, only 1 line for PHOTO field            
            return element;
        }

        do {
            String line = in.readLine();
            if ( line.indexOf( ':' ) != -1 || line.indexOf( ';' ) != -1 ){
                if ( encodingName.equals(FormatSupport.QUOTED_PRINTABLE)){
                    byte[] rawBinaryData = 
                        QuotedPrintableEncoding.fromQuotedPrintable( element.data );
                    element.data = Base64Encoding.toBase64(rawBinaryData, 76, 4);
                }else if ( encodingName.equals(FormatSupport.BASE64) == false
                           && encodingName.equals(FormatSupport.BEE) == false){
                    element = FormatSupport.parseObjectLine(line);
                    break;
                }

                importData(contact,
                   element.propertyName,
                   element.attributes,
                   element.data,
                   contact.getPIMListHandle());
                element =
                    FormatSupport.parseObjectLine(line);
                break;
            }
            element.data += line;
        }while (true);

        if ( element.propertyName.equals("KEY")) {
            return parsePublicKey( in, element, contact );
        }
        return element;
    }

    private FormatSupport.DataElement parsePublicKey( LineReader in, FormatSupport.DataElement element, 
                                    ContactImpl contact ) 
                                    throws IOException {
        String encodingName = FormatSupport.getAttributeValue(
                element.attributes, "ENCODING=", null);

        do {
            String line = in.readLine();
            if ( line.indexOf( ':' ) != -1 || line.indexOf( ';' ) != -1 ){
                if ( encodingName.equals(FormatSupport.QUOTED_PRINTABLE)){
                    //Re-encode binary data from QUOTED_PRINTABLE format to "B" format
                    byte[] rawBinaryData = 
                        QuotedPrintableEncoding.fromQuotedPrintable( element.data );
                    element.data = Base64Encoding.toBase64(rawBinaryData, 76, 4);
                }else if ( encodingName.equals(FormatSupport.PLAIN_TEXT )){
                    //Re-encode binary data from PLAIN_TEXT format to "B" format
                    byte[] rawBinaryData = element.data.getBytes();
                    element.data = Base64Encoding.toBase64(rawBinaryData, 76, 4);
                }else if ( encodingName.equals(FormatSupport.BASE64) ==false 
                           && encodingName.equals(FormatSupport.BEE) == false){
                    //The encoding type not supported, decod the next line and break
                    element = FormatSupport.parseObjectLine(line);
                    break;
                }

                importData(contact,
                   element.propertyName,
                   element.attributes,
                   element.data,
                   contact.getPIMListHandle());
                element =
                    FormatSupport.parseObjectLine(line);
                break;
            }
            element.data += line;
        }while (true);
        if ( element.propertyName.equals("PHOTO")) {
            return parsePhoto( in, element, contact );
        }
        return element;     
    }
}
