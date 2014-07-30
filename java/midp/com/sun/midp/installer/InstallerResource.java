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


package com.sun.midp.installer;

import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

import com.sun.midp.configurator.Constants;



/**
 * InstallerResource class help to preserve the same logic
 * of GraphicalInstaller for all Installers.
 * The main goal of InstallerResource class is to give correct text for
 * UI components during installation depending on type of installer.
 */
public class InstallerResource {    
    /** How specific installer initializate suite (connecting to web source or 
     * searching in file system). */
    public static final int CONNECTING_GAUGE_LABEL = 0;
    /** What specific installer do before connecting or searching needed 
     * suite to install. */
    public static final int PREPARE_INSTALLATION_LIST_LABEL = 1;
    /** Label to show during installer read data from source storage. */
    public static final int TRANSFER_DATA_LABEL = 2;
    /** Shows type of storage, e.g. website or external storage. */
    public static final int TYPE_OF_SOURCE = 3;
    /** Label to show during loading description file. */
    public static final int LOAD_JAD_GAUGE_LABEL = 4;
    /** Label to show during loading jar file. */
    public static final int LOAD_JAR_GAUGE_LABEL = 5;
    /** Type of message if IOException occur. */
    public static final int IO_EXCEPTION_MESSAGE = 6;
    
    /** Schema with which file URI must begin. */
    final static String DEFAULT_FILE_SCHEMA = "file:///";
    /** Installation from web source. */
    final static int HTTP_INSTALL = 0;
    /** Installation from external storage source. */
    final static int FILE_INSTALL = 1;  
    
    /** Array of specific messages for different installer.
     * First index of array corresponds to one of the InstallerResource 
     * constant. Second index is specific for different installers.
     */
    private static int[][] installerMessages = {
        { 
            ResourceConstants.AMS_GRA_INTLR_CONN_GAUGE_LABEL,
            ResourceConstants.AMS_GRA_INTLR_CONN_STORAGE_GAUGE_LABEL        
        },
        { 
            ResourceConstants.AMS_DISC_APP_GET_INSTALL_LIST,
            ResourceConstants.AMS_DISC_APP_PREPARE_INSTALL_STORAGE  
        },
        {
            ResourceConstants.AMS_DISC_APP_GAUGE_LABEL_DOWNLOAD,
            ResourceConstants.AMS_DISC_APP_GAUGE_LABEL_STORAGE        
        },
        {
            ResourceConstants.AMS_WEBSITE,
            ResourceConstants.AMS_FILE_PATH        
        },
        {
            ResourceConstants.AMS_GRA_INTLR_DOWNLOADING_JAD_GAUGE_LABEL,
            ResourceConstants.AMS_GRA_INTLR_DOWNLOADING_JAD_GAUGE_LABEL_STORAGE        
        },
        {
            ResourceConstants.AMS_GRA_INTLR_DOWNLOADING_JAR_GAUGE_LABEL,
            ResourceConstants.AMS_GRA_INTLR_DOWNLOADING_JAR_GAUGE_LABEL_STORAGE
        },
        {
            ResourceConstants.AMS_GRA_INTLR_CONN_DROPPED,
            ResourceConstants.AMS_GRA_INTLR_FILE_NOT_FOUND
        }
    };
    
    /**
     * Constructor of InstallerResource
     */
    public InstallerResource() {
        
    }
    
    /**
     * Return the specific text for UI component.
     * This function is almost called from GraphicalInstaller.
     * 
     * @param installer instance of installer
     * @param key key code
     * @return specific for each installer installation text
     */   
    public static String getString(Installer installer, int key) {
        int typeOfInstall = installer instanceof FileInstaller ? 
            InstallerResource.FILE_INSTALL : InstallerResource.HTTP_INSTALL;
        
        return getString(typeOfInstall,key);
    }
    
    /**
     * Return the specific text for UI component.
     * This function is almost called from DiscoveryApp.
     * 
     * @param typeOfInstall type of installation
     * @param key key code
     * @return specific for each installer installation text
     */
    public static String getString(int typeOfInstall, int key) {
               
        return Resource.getString(installerMessages[key][typeOfInstall]);
    }
         
    /**
     * Returns necessary installer for GraphicalInstaller class.
     * 
     * @param url path to suite
     * @return necessary installer
     */
    public static Installer getInstaller(String url) {
        if (url.startsWith(InstallerResource.DEFAULT_FILE_SCHEMA)) 
            return new FileInstaller();
        else
            return new HttpInstaller();        
    }
    
 }
