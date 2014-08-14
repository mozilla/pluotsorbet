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

package com.sun.j2me.pim;

import com.sun.j2me.app.AppPackage;
import com.sun.j2me.security.Permission;
import com.sun.j2me.security.PIMPermission;
import com.sun.j2me.pim.formats.VCalendar10Format;
import com.sun.j2me.pim.formats.VCard21Format;
import com.sun.j2me.pim.formats.VCard30Format;
import com.sun.j2me.i18n.Resource;
import com.sun.j2me.i18n.ResourceConstants;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import javax.microedition.pim.PIM;
import javax.microedition.pim.PIMException;
import javax.microedition.pim.PIMItem;
import javax.microedition.pim.PIMList;

/**
 * Implementation of PIM.
 *
 */
public final class PIMImpl extends PIM {
    /**
     * Supported serial formats.    
     */
    private static final PIMFormat[] formats = {
        new VCalendar10Format(),
        new VCard21Format(),
        new VCard30Format()
    };
    
    /** PIMImpl constructor, called from PIM.getInstance(). */
    public PIMImpl() {
    }
    
    /**
     * Gets an array of PIM items from an encoded input
     * stream.
     * @param is data input stream
     * @param enc character encoding of stream data
     * @return array of PIM items
     * @throws PIMException if any error reading the PIM data
     * @throws UnsupportedEncodingException if encoding is not supported
     */
    public PIMItem[] fromSerialFormat(InputStream is, String enc)
            throws PIMException, UnsupportedEncodingException {
        return fromSerialFormat(is, enc, null);
    }

    /**
     * Gets an array of PIM items from an encoded input
     * stream and list.
     * @param is data input stream
     * @param enc character encoding of stream data
     * @param list populate from list
     * @return array of PIM items
     * @throws PIMException if any error reading the PIM data
     * @throws UnsupportedEncodingException if encoding is not supported
     */
    private PIMItem[] fromSerialFormat(InputStream is, String enc, PIMList list)
        throws PIMException, UnsupportedEncodingException {
        
        if (enc == null) {
            enc = "UTF-8" /* NO I18N */;
        }
        InputStream in = new MarkableInputStream(is);
        in.mark(Integer.MAX_VALUE);
        try {
            for (int i = 0; i < formats.length; i++) {
                try {
                    PIMItem[] items = formats[i].decode(in, enc, list);
                    if (items == null) {
                        throw new PIMException(
                            "Empty stream or insufficient data");
                    }
                    return items;
                } catch (UnsupportedPIMFormatException e) {
                    in.reset();
                    in.mark(Integer.MAX_VALUE);
                }
            }
            throw new PIMException("Format is not recognized");
        } catch (UnsupportedEncodingException e) {
            throw e;
        } catch (IOException e) {
            throw new PIMException(e.getMessage());
        }
    }

    /**
     * Gets the current PIM lists.
     * @param pimListType type of list to return
     * @return array of list names
     */    
    public String[] listPIMLists(int pimListType) {
        checkPermissions(pimListType, PIM.READ_ONLY,
            Resource.getString(ResourceConstants.JSR75_ENUM_LISTS));
        validatePimListType(pimListType);
        return PIMHandler.getInstance().getListNames(pimListType);
    }

    /**
     * Gets the permission with detailed description.
     *
     * @param base one of the base permissions for contact, event, or to-do
     *        lists
     * @param action description of the operation being performed
     * @return the detailed permission
     */
    private PIMPermission getPermission(PIMPermission base, String action) {
        return new PIMPermission(base.getName(), base.getResource() + " (" +
            action + ").");
    }
        
    /**
     * Gets the permissions that need to be present to open a list
     *
     * @param listType CONTACT_LIST, EVENT_LIST or TODO_LIST
     * @param mode READ_ONLY, WRITE_ONLY or READ_WRITE
     * @return list of permissions to be checked
     * @throws IllegalArgumentException if one of the parameters
     * is out of bounds
     */
    private PIMPermission[] getPermissions(int listType, int mode,
        String action) {
        switch (listType) {
            case CONTACT_LIST:
                switch (mode) {
                    case READ_ONLY:
                        return new PIMPermission[] {
                            getPermission(PIMPermission.CONTACT_READ, action)
                            };
                    case WRITE_ONLY:
                        return new PIMPermission[] {
                            getPermission(PIMPermission.CONTACT_WRITE, action)
                            };
                    case READ_WRITE:
                        return new PIMPermission[] {
                            getPermission(PIMPermission.CONTACT_READ, action),
                            getPermission(PIMPermission.CONTACT_WRITE, action)
                            };
                    default:
                        throw new IllegalArgumentException("Not a valid mode: "
                            + mode);
                }
            case EVENT_LIST:
                switch (mode) {
                    case READ_ONLY:
                        return new PIMPermission[] {
                            getPermission(PIMPermission.EVENT_READ, action)
                            };
                    case WRITE_ONLY:
                        return new PIMPermission[] {
                            getPermission(PIMPermission.EVENT_WRITE, action)
                            };
                    case READ_WRITE:
                        return new PIMPermission[] {
                            getPermission(PIMPermission.EVENT_READ, action),
                            getPermission(PIMPermission.EVENT_WRITE, action)
                            };
                    default:
                        throw new IllegalArgumentException("Not a valid mode: "
                            + mode);
                }
            case TODO_LIST:
                switch (mode) {
                    case READ_ONLY:
                        return new PIMPermission[] {
                            getPermission(PIMPermission.TODO_READ, action)
                            };
                    case WRITE_ONLY:
                        return new PIMPermission[] {
                            getPermission(PIMPermission.TODO_WRITE, action)
                            };
                    case READ_WRITE:
                        return new PIMPermission[] {
                            getPermission(PIMPermission.TODO_READ, action),
                            getPermission(PIMPermission.TODO_WRITE, action)
                            };
                    default:
                        throw new IllegalArgumentException("Not a valid mode: "
                            + mode);
                }
            default:
                throw new IllegalArgumentException("Not a valid list type: " 
                    + listType);
        }
    }
    
    /**
     * Checks for all the permissions that need to be present to open a list
     *
     * @param pimListType CONTACT_LIST, EVENT_LIST or TODO_LIST
     * @param mode READ_ONLY, WRITE_ONLY or READ_WRITE
     * @throws IllegalArgumentException if one of the parameters is out of
     * bounds
     * @throws SecurityException if the application does not have the required
     * permissions
     */
    private void checkPermissions(int pimListType, int mode, String action) {
        PIMPermission[] permissions = getPermissions(pimListType, mode, action);
        AppPackage appPackage = AppPackage.getInstance();
        /*
         * Do a first pass on the permissions to make sure that none is
         * automatically denied. This is for the case when both read permission
         * and write permission are required. It is possible that, for example,
         * read permission is granted only after asking the user, but write
         * permission is automatically denied. In this case, the user should
         * not be asked a question at all.
         */
        for (int i = 0; i < permissions.length; i++) {
            int status = appPackage.checkPermission(permissions[i]);
            if (status == 0) {
                // throw an exception
                appPackage.checkIfPermissionAllowed(permissions[i]);
            } else if (status == 1) {
                // don't check this permission again
                permissions[i] = null;
            }
        }
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i] != null) {
                try {
                    appPackage.checkForPermission(permissions[i]);
                } catch (InterruptedException e) {
                    throw new SecurityException("Security check interrupted: "
                        + e.getMessage());
                }
            }
        }
    }

    /**
     * Opens the PIM list.
     *
     * @param pimListType CONTACT_LIST, EVENT_LIST or TODO_LIST
     * @param mode READ_ONLY, WRITE_ONLY or READ_WRITE
     * @return handle to opened PIM list
     * @throws PIMException if the list is not found
     */
    public PIMList openPIMList(int pimListType, int mode) throws PIMException {
        validatePimListType(pimListType);
        validateMode(mode);
        checkPermissions(pimListType, mode,
            Resource.getString(ResourceConstants.JSR75_OPEN_DEFAULT_LIST));
        String listName = PIMHandler.getInstance()
            .getDefaultListName(pimListType);
        if (listName == null) {
            throw new PIMException("List not available");
        }
        return openPIMListImpl(pimListType, mode, listName);
    }
    
    /**
     * Opens the PIM list.
     *
     * @param pimListType CONTACT_LIST, EVENT_LIST or TODO_LIST
     * @param mode READ_ONLY, WRITE_ONLY or READ_WRITE
     * @param name name of the list
     * @return handle to opened PIM list
     * @throws PIMException if the list is not found
     */
    public PIMList openPIMList(int pimListType, int mode, String name)
            throws PIMException {
        if (name == null) {
            throw new NullPointerException("PIM list name cannot be null");
        }
        validatePimListType(pimListType);
        validateMode(mode);
        checkPermissions(pimListType, mode, Resource.getString(
            ResourceConstants.JSR75_OPEN_LIST) + ": '" + name + "'");
        validateName(pimListType, name);
        return openPIMListImpl(pimListType, mode, name);
    }
    
    /**
     * Does the same as openPIMList, without any validation
     * 
     * @param pimListType CONTACT_LIST, EVENT_LIST or TODO_LIST
     * @param mode READ_ONLY, WRITE_ONLY or READ_WRITE
     * @param name name of the list
     * @return handle to opened PIM list
     * @throws PIMException if the list is not found
     */
    private PIMList openPIMListImpl(int pimListType, int mode, String name)
            throws PIMException {
    
        AbstractPIMList list;
        PIMHandler handler = PIMHandler.getInstance();
        Object listHandle = handler.openList(pimListType, name, mode);
        
        switch (pimListType) {
            case PIM.CONTACT_LIST:
                list = new ContactListImpl(name, mode, listHandle);
                break;
            case PIM.EVENT_LIST:
                list = new EventListImpl(name, mode, listHandle);
                break;
            case PIM.TODO_LIST:
                list = new ToDoListImpl(name, mode, listHandle);
                break;
            default:
                // pimListType has been verified
                throw new Error("Unreachable code");
        }
        Object[] keys = handler.getListKeys(listHandle);
        for (int i = 0; i < keys.length; i++) {
            byte[] data = handler.getListElement(listHandle, keys[i]);
            String [] categories = handler.getListElementCategories(listHandle,
                keys[i]);
            try {
                PIMItem[] items =
                    fromSerialFormat(new ByteArrayInputStream(data), 
                        "UTF-8", list);
                for (int j = 0; j < items.length; j++) {
                    AbstractPIMItem item = (AbstractPIMItem) items[j];
                    item.setKey(keys[i]);
                    list.addItem(item);
                    item.setDefaultValues();
                    for (int index = 0; index < categories.length; index ++) {
                        item.addToCategory(categories[index]);
                    }
                    item.setModified(false);
                }
            } catch (UnsupportedEncodingException e) {
                throw new Error("UTF-8 not supported");
            } catch (PIMException e) {
                // skip element
            }
        }
        return list;
    }
    
    /**
     * Gets the list of supported serial formats.
     * 
     * @param pimListType CONTACT_LIST, EVENT_LIST or TODO_LIST
     * @return array of format names
     */
    public String[] supportedSerialFormats(int pimListType) {
        validatePimListType(pimListType);
        int supportedFormatCount = 0;
        for (int i = 0; i < formats.length; i++) {
            if (formats[i].isTypeSupported(pimListType)) {
                supportedFormatCount ++;
            }
        }
        String[] supportedFormats = new String[supportedFormatCount];
        for (int i = 0; i < formats.length; i++) {
            if (formats[i].isTypeSupported(pimListType)) {
                supportedFormats[--supportedFormatCount] = formats[i].getName();
            }
        }
        return supportedFormats;
    }

    /**
     * Converts to serial format.
     * @param item the PIM item to be processed
     * @param os the target output stream
     * @param enc the character encoding for output strings
     * @param dataFormat the serialized format
     * @throws PIMException if any error writing the PIM data
     * @throws UnsupportedEncodingException if encoding is not supported
     */    
    public void toSerialFormat(PIMItem item,
        OutputStream os,
        String enc,
        String dataFormat) throws PIMException, UnsupportedEncodingException {
        
        if (enc == null) {
            enc = "UTF-8" /* NO I18N */;
        }
        if (dataFormat == null) {
            throw new NullPointerException("Null data format");
        }
        if (dataFormat.trim().length() == 0) {
            throw new IllegalArgumentException("Empty data format");
        }
        if (item == null) {
            throw new NullPointerException("Null PIM item");
        }

        try {
            for (int i = 0; i < formats.length; i++) {
                if (formats[i].getName().equals(dataFormat)) {
                    formats[i].encode(os, enc, item);
                    return;
                }
            }
            throw new IllegalArgumentException("Data format '" + dataFormat
                + "' is not supported");
        } catch (UnsupportedEncodingException e) {
            throw e;
        } catch (IOException e) {
            throw new PIMException(e.getMessage());
        }
    }
    
    /**
     * Ensures that the given PIM list type is valid.
     * @param pimListType a PIM list type
     * @throws IllegalArgumentException if the list type is not valid.
     */    
    private void validatePimListType(int pimListType) {
        switch (pimListType) {
            case PIM.CONTACT_LIST:
            case PIM.EVENT_LIST:
            case PIM.TODO_LIST:
                // ok
                break;
            default:
                throw new IllegalArgumentException("Not a valid PIM list type: "
                    + pimListType);
        }
    }
    
    /**
     * Ensures that the given PIM list mode is valid.
     * @param mode READ_ONLY, WRITE_ONLY or READ_WRITE
     */
    private void validateMode(int mode) {
        switch (mode) {
            case READ_ONLY:
            case WRITE_ONLY:
            case READ_WRITE:
                break;
            default:
                throw new IllegalArgumentException(
                    "Invalid PIM list mode: " + mode);
        }
    }

    /**
     * Ensures that the given PIM list name is valid.
     *
     * @param pimListType CONTACT_LIST, EVENT_LIST or TODO_LIST
     * @param name name of the list
     * @throws PIMException if list with the specified name is not found
     */
    private void validateName(int pimListType, String name) 
            throws PIMException {
        String[] names = PIMHandler.getInstance().getListNames(pimListType);
        for (int i = 0; i < names.length; i++) {
            if (name.equals(names[i])) {
                return;
            }
        }
        throw new PIMException("PIM list does not exist: '" + name + "'");
    }

}
