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
package javax.microedition.location;

import com.sun.j2me.location.LandmarkImpl;
import com.sun.j2me.location.LocationPersistentStorage;
import com.sun.j2me.location.Util;
import com.sun.j2me.main.Configuration;
import com.sun.j2me.security.LocationPermission;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class is defined by the JSR-179 specification
 * <em>Location API for J2ME for J2ME&trade;.</em>
 */
// JAVADOC COMMENT ELIDED
public class LandmarkStore {

    /** LandmarkStore Create property */
    private static final String CREATE_LANDMARKSTORE_SUPPORTED = 
        "com.sun.j2me.location.CreateLandmarkStoreSupported";
    
    /** LandmarkStore Delete property */
    private static final String DELETE_LANDMARKSTORE_SUPPORTED = 
        "com.sun.j2me.location.DeleteLandmarkStoreSupported";

    /** Category Create property */
    private static final String CREATE_CATEGORY_SUPPORTED = 
        "com.sun.j2me.location.CreateCategorySupported";

    /** Category Delete property */
    private static final String DELETE_CATEGORY_SUPPORTED = 
        "com.sun.j2me.location.DeleteCategorySupported";

    /** The default instance of a store */
    private static final LandmarkStore defaultStore = new LandmarkStore();

    /** The name of the record store */
    private String storeName;

    /**
     * Constructor is private to prevent user from instanciating this class.
     *
     * @param storeName name of landmark store
     */
    private LandmarkStore(String storeName) {
        this.storeName = storeName;
    }

    /**
     * Prevent the user from instanciating this class
     * creates default LandmarkStore if it is not exist
     */
    private LandmarkStore() {
        this.storeName = null;
    }

    // JAVADOC COMMENT ELIDED    
    public static synchronized LandmarkStore getInstance(String storeName) {
        Util.checkForPermission(LocationPermission.LANDMARK_STORE_READ);
        
        LandmarkStore current = null;
	try {
            if (storeName == null) {
                return defaultStore;
            } else {
                String[] storeNames = 
                        LocationPersistentStorage.listStoreNames();
                if (storeNames != null) {
                    for (int i = 0; i < storeNames.length; i++) {
                        if (storeNames[i].equals(storeName)) {
                            current = new LandmarkStore(storeName);
                            break;
                        }
                    }
                }
            }
	} catch (IOException e) { // return null
	}
        return current;
    }

    // JAVADOC COMMENT ELIDED
    public static void createLandmarkStore(String storeName)
	throws IOException, LandmarkException {
        if (Configuration.getProperty(CREATE_LANDMARKSTORE_SUPPORTED).
                equals("true")) {
            Util.checkForPermission(LocationPermission.LANDMARK_STORE_MANAGE);
            if (storeName == null) {
                throw new NullPointerException("storeName can not be null");
            }
            // verify that the name is correct and store does not exist
            int storeLen = storeName.length();
            if (storeLen == 0) { 
                throw new IllegalArgumentException("The store: name has " +
                                                   "incorrect length");
            }
            if (getInstance(storeName) != null) {
                throw new IllegalArgumentException("The store: " + storeName + 
                                                   " already exists");
            }
            LocationPersistentStorage.addStoreName(storeName);
        } else {
            throw new LandmarkException(
                    "Implementation does not support " + 
                    "creating new landmark stores");
        }
    }

    // JAVADOC COMMENT ELIDED
    public static void deleteLandmarkStore(String storeName)
                  throws IOException, LandmarkException {
        if (Configuration.getProperty(DELETE_LANDMARKSTORE_SUPPORTED).
                equals("true")) {
            Util.checkForPermission(LocationPermission.LANDMARK_STORE_MANAGE);
            if (storeName == null) {
                throw new NullPointerException();
            }
            LocationPersistentStorage.removeStoreName(storeName);
        } else {
            throw new LandmarkException(
                    "Implementation does not support " + 
                    "deleting landmark stores");
        }
    }

    // JAVADOC COMMENT ELIDED
    public static String[] listLandmarkStores() throws IOException {
        Util.checkForPermission(LocationPermission.LANDMARK_STORE_READ);
        return LocationPersistentStorage.listStoreNames();
    }

    // JAVADOC COMMENT ELIDED
    public void addLandmark(Landmark landmark, String category)
	throws IOException {
        Util.checkForPermission(LocationPermission.LANDMARK_STORE_WRITE);
        if (landmark == null) { // NullPointerException should be caused
	    throw new NullPointerException("Landmark is null");
	}
        LocationPersistentStorage.addLandmark(storeName,
                landmark.getInstance(), category);
    }

    // JAVADOC COMMENT ELIDED
    public Enumeration getLandmarks(String category, String name)
	throws IOException {
        Enumeration en = LocationPersistentStorage.
                             getLandmarksEnumeration(storeName, category, name, 
						 -90, 90, -180, 180);
        if (en == null) {
            return null;
        }
        Vector vecLandmarks = new Vector();
        while (en.hasMoreElements()) {
            vecLandmarks.addElement(
                    new Landmark((LandmarkImpl)en.nextElement()));
        }
        return vecLandmarks.elements();
    }

    // JAVADOC COMMENT ELIDED
    public Enumeration getLandmarks() throws IOException {
        return getLandmarks(null, null);
    }

    // JAVADOC COMMENT ELIDED
    public Enumeration getLandmarks(String category, double minLatitude,
            double maxLatitude, double minLongitude, double maxLongitude) 
            throws IOException {
        if ((minLongitude == 180D) || 
            (maxLongitude == 180D)) {
            throw new IllegalArgumentException("Longtitude out of range " +
					       "must not equal 180");
        }
        if (minLatitude > maxLatitude) {
            throw new IllegalArgumentException("Minimum latitude cannot be " +
	        "larger than the maximum latitude");
        }
        Util.checkRange(minLatitude, -90, 90,
			"Latitude out of range [-90.0, 90]: ");
        Util.checkRange(maxLatitude, -90, 90,
			"Latitude out of range [-90.0, 90]: ");
        Util.checkRange(maxLongitude, -180, 180,
			"Longitude out of range [-180.0, 180]: ");
        Util.checkRange(minLongitude, -180, 180,
			"Longitude out of range [-180.0, 180]: ");
        
        Enumeration en = LocationPersistentStorage.
                             getLandmarksEnumeration(storeName, category, null, 
						 minLatitude, maxLatitude,
						 minLongitude, maxLongitude);
        if (en == null) {
            return null;
        }
        Vector vecLandmarks = new Vector();
        while (en.hasMoreElements()) {
            vecLandmarks.addElement(
                    new Landmark((LandmarkImpl)en.nextElement()));
        }
        return vecLandmarks.elements();
    }

    // JAVADOC COMMENT ELIDED
    public void removeLandmarkFromCategory(Landmark lm, String category)
	throws IOException  {
	Util.checkForPermission(LocationPermission.LANDMARK_STORE_WRITE);
        if (lm == null || category == null) {
            throw new NullPointerException();
        }
        LocationPersistentStorage.removeLandmarkFromCategory(
                storeName, lm.getInstance(), category);
    }

    // JAVADOC COMMENT ELIDED
    public void updateLandmark(Landmark lm)
	throws IOException, LandmarkException {
        Util.checkForPermission(LocationPermission.LANDMARK_STORE_WRITE);
        if (lm == null) {
            throw new NullPointerException();
        }
        LocationPersistentStorage.updateLandmark(
                storeName, lm.getInstance());
    }

    // JAVADOC COMMENT ELIDED
    public void deleteLandmark(Landmark lm) 
	throws IOException, LandmarkException {
        Util.checkForPermission(LocationPermission.LANDMARK_STORE_WRITE);
        if (lm == null) {
            throw new NullPointerException();
        }

        LocationPersistentStorage.deleteLandmark(
		storeName, lm.getInstance());
    }

    // JAVADOC COMMENT ELIDED
    public Enumeration getCategories() {
	try {
	    return getCategoriesVector().elements();
	} catch (IOException e) {
	    return new Vector().elements();
	}
    }

    // JAVADOC COMMENT ELIDED
    private Vector getCategoriesVector() throws IOException {
        return LocationPersistentStorage.
                getCategories(storeName);
    }

    // JAVADOC COMMENT ELIDED
    public void addCategory(String categoryName)
	throws LandmarkException, IOException {
        if (Configuration.getProperty(CREATE_CATEGORY_SUPPORTED).
                equals("true")) {
            Util.checkForPermission(LocationPermission.LANDMARK_STORE_CATEGORY);
            if (categoryName == null) {
                throw new NullPointerException("Category name is null");
            }
            // save categories into persistent storage
            LocationPersistentStorage.
                    addCategory(categoryName, storeName);
        } else {
            throw new LandmarkException(
                    "Implementation does not support " + 
                    "creating categories");
        }
    }

    // JAVADOC COMMENT ELIDED
    public void deleteCategory(String categoryName)
	throws LandmarkException, IOException {
        if (Configuration.getProperty(DELETE_CATEGORY_SUPPORTED).
                equals("true")) {
            Util.checkForPermission(LocationPermission.LANDMARK_STORE_CATEGORY);
            if (categoryName == null) {
                throw new NullPointerException();
            }
            LocationPersistentStorage.
                    deleteCategory(categoryName, storeName);
        } else {
            throw new LandmarkException(
                    "Implementation does not support " + 
                    "deleting categories");
        }
    }

}
