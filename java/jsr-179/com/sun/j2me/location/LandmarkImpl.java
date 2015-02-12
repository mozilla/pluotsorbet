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
package com.sun.j2me.location;

import javax.microedition.location.*;

/**
 * This class is an implementation of the <code>Landmark</code> class defined
 * by the JSR-179 specification.
 */ 
// JAVADOC COMMENT ELIDED
public class LandmarkImpl {
    // JAVADOC COMMENT ELIDED
    int recordId = -1;
    // JAVADOC COMMENT ELIDED
    String storeName = null;
    // JAVADOC COMMENT ELIDED
    String name;
    // JAVADOC COMMENT ELIDED
    String description;
    // JAVADOC COMMENT ELIDED
    boolean isCoordinates = false;
    // JAVADOC COMMENT ELIDED
    double latitude;
    // JAVADOC COMMENT ELIDED
    double longitude;
    // JAVADOC COMMENT ELIDED
    float altitude;
    // JAVADOC COMMENT ELIDED
    float horizontalAccuracy;
    // JAVADOC COMMENT ELIDED
    float verticalAccuracy;
    // JAVADOC COMMENT ELIDED
    boolean isAddressInfo = false;
    // JAVADOC COMMENT ELIDED
    int numAddressInfoFields = 0;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_EXTENSION = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_STREET = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_POSTAL_CODE = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_CITY = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_COUNTY = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_STATE = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_COUNTRY = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_COUNTRY_CODE = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_DISTRICT = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_BUILDING_NAME = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_BUILDING_FLOOR = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_BUILDING_ROOM = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_BUILDING_ZONE = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_CROSSING1 = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_CROSSING2 = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_URL = null;
    // JAVADOC COMMENT ELIDED
    String AddressInfo_PHONE_NUMBER = null;

    // JAVADOC COMMENT ELIDED
    public LandmarkImpl(String name, String description, 
		    QualifiedCoordinates coordinates,
		    AddressInfo addressInfo) {
        setName(name);
        this.description = description;
        setQualifiedCoordinates(coordinates);
        setAddressInfo(addressInfo);
    }

    // JAVADOC COMMENT ELIDED
    int getRecordId() {
        return recordId;
    }

    // JAVADOC COMMENT ELIDED
    void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    // JAVADOC COMMENT ELIDED
    String getStoreName() {
        return storeName;
    }

    // JAVADOC COMMENT ELIDED
    void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    // JAVADOC COMMENT ELIDED
    public String getName() {
        return name;
    }

    // JAVADOC COMMENT ELIDED
    public String getDescription() {
        return description;
    }

    // JAVADOC COMMENT ELIDED
    public QualifiedCoordinates getQualifiedCoordinates() {
        if (isCoordinates) {
            return new QualifiedCoordinates(latitude, longitude, altitude,
                        horizontalAccuracy,  verticalAccuracy);
        }
        return null;
    }

    // JAVADOC COMMENT ELIDED
    public AddressInfo getAddressInfo() {
        if(isAddressInfo) {
            AddressInfo address = new AddressInfo();
            address.setField(AddressInfo.EXTENSION, AddressInfo_EXTENSION);
            address.setField(AddressInfo.STREET, AddressInfo_STREET);
            address.setField(AddressInfo.POSTAL_CODE, AddressInfo_POSTAL_CODE);
            address.setField(AddressInfo.CITY, AddressInfo_CITY);
            address.setField(AddressInfo.COUNTY, AddressInfo_COUNTY);
            address.setField(AddressInfo.STATE, AddressInfo_STATE);
            address.setField(AddressInfo.COUNTRY, AddressInfo_COUNTRY);
            address.setField(AddressInfo.COUNTRY_CODE, 
                            AddressInfo_COUNTRY_CODE);
            address.setField(AddressInfo.DISTRICT, AddressInfo_DISTRICT);
            address.setField(AddressInfo.BUILDING_NAME, 
                            AddressInfo_BUILDING_NAME);
            address.setField(AddressInfo.BUILDING_FLOOR, 
                            AddressInfo_BUILDING_FLOOR);
            address.setField(AddressInfo.BUILDING_ROOM, 
                            AddressInfo_BUILDING_ROOM);
            address.setField(AddressInfo.BUILDING_ZONE, 
                            AddressInfo_BUILDING_ZONE);
            address.setField(AddressInfo.CROSSING1, AddressInfo_CROSSING1);
            address.setField(AddressInfo.CROSSING2, AddressInfo_CROSSING2);
            address.setField(AddressInfo.URL, AddressInfo_URL);
            address.setField(AddressInfo.PHONE_NUMBER, 
                            AddressInfo_PHONE_NUMBER);
            return address;
        }
        return null;
    }

    // JAVADOC COMMENT ELIDED
    public void setName(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        this.name = name;
    }

    // JAVADOC COMMENT ELIDED
    public void setDescription(String description) {
        this.description = description;
    }

    // JAVADOC COMMENT ELIDED
    public void setQualifiedCoordinates(QualifiedCoordinates coordinates) {
        if (coordinates != null) {
            latitude = coordinates.getLatitude();
            longitude = coordinates.getLongitude();
            altitude = coordinates.getAltitude();
            horizontalAccuracy = coordinates.getHorizontalAccuracy();
            verticalAccuracy = coordinates.getVerticalAccuracy();
            isCoordinates = true;
        } else {
            latitude = 0;
            longitude = 0;
            altitude = 0;
            horizontalAccuracy = 0;
            verticalAccuracy = 0;
            isCoordinates = false;
        }
    }

    // JAVADOC COMMENT ELIDED
    public void setAddressInfo(AddressInfo address) {
        if (address != null) {
            numAddressInfoFields = 0;
            AddressInfo_EXTENSION = address.getField(AddressInfo.EXTENSION);
            if(AddressInfo_EXTENSION != null) {
                numAddressInfoFields++;
            }
            AddressInfo_STREET = address.getField(AddressInfo.STREET);
            if(AddressInfo_STREET != null) {
                numAddressInfoFields++;
            }
            AddressInfo_POSTAL_CODE = address.getField(AddressInfo.POSTAL_CODE);
            if(AddressInfo_POSTAL_CODE != null) {
                numAddressInfoFields++;
            }
            AddressInfo_CITY = address.getField(AddressInfo.CITY);
            if(AddressInfo_CITY != null) {
                numAddressInfoFields++;
            }
            AddressInfo_COUNTY = address.getField(AddressInfo.COUNTY);
            if(AddressInfo_COUNTY != null) {
                numAddressInfoFields++;
            }
            AddressInfo_STATE = address.getField(AddressInfo.STATE);
            if(AddressInfo_STATE != null) {
                numAddressInfoFields++;
            }
            AddressInfo_COUNTRY = address.getField(AddressInfo.COUNTRY);
            if(AddressInfo_COUNTRY != null) {
                numAddressInfoFields++;
            }
            AddressInfo_COUNTRY_CODE = 
                    address.getField(AddressInfo.COUNTRY_CODE);
            if(AddressInfo_COUNTRY_CODE != null) {
                numAddressInfoFields++;
            }
            AddressInfo_DISTRICT = address.getField(AddressInfo.DISTRICT);
            if(AddressInfo_DISTRICT != null) {
                numAddressInfoFields++;
            }
            AddressInfo_BUILDING_NAME = 
                    address.getField(AddressInfo.BUILDING_NAME);
            if(AddressInfo_BUILDING_NAME != null) {
                numAddressInfoFields++;
            }
            AddressInfo_BUILDING_FLOOR = 
                    address.getField(AddressInfo.BUILDING_FLOOR);
            if(AddressInfo_BUILDING_FLOOR != null) {
                numAddressInfoFields++;
            }
            AddressInfo_BUILDING_ROOM = 
                    address.getField(AddressInfo.BUILDING_ROOM);
            if(AddressInfo_BUILDING_ROOM != null) {
                numAddressInfoFields++;
            }
            AddressInfo_BUILDING_ZONE = 
                    address.getField(AddressInfo.BUILDING_ZONE);
            if(AddressInfo_BUILDING_ZONE != null) {
                numAddressInfoFields++;
            }
            AddressInfo_CROSSING1 = address.getField(AddressInfo.CROSSING1);
            if(AddressInfo_CROSSING1 != null) {
                numAddressInfoFields++;
            }
            AddressInfo_CROSSING2 = address.getField(AddressInfo.CROSSING2);
            if(AddressInfo_CROSSING2 != null) {
                numAddressInfoFields++;
            }
            AddressInfo_URL = address.getField(AddressInfo.URL);
            if(AddressInfo_URL != null) {
                numAddressInfoFields++;
            }
            AddressInfo_PHONE_NUMBER = 
                    address.getField(AddressInfo.PHONE_NUMBER);
            if(AddressInfo_PHONE_NUMBER != null) {
                numAddressInfoFields++;
            }
            isAddressInfo = true;
        } else {
            AddressInfo_EXTENSION = null;
            AddressInfo_STREET = null;
            AddressInfo_POSTAL_CODE = null;
            AddressInfo_CITY = null;
            AddressInfo_COUNTY = null;
            AddressInfo_STATE = null;
            AddressInfo_COUNTRY = null;
            AddressInfo_COUNTRY_CODE = null;
            AddressInfo_DISTRICT = null;
            AddressInfo_BUILDING_NAME = null;
            AddressInfo_BUILDING_FLOOR = null;
            AddressInfo_BUILDING_ROOM = null;
            AddressInfo_BUILDING_ZONE = null;
            AddressInfo_CROSSING1 = null;
            AddressInfo_CROSSING2 = null;
            AddressInfo_URL = null;
            AddressInfo_PHONE_NUMBER = null;
            numAddressInfoFields = 0;
            isAddressInfo = false;
        }
    }

    // JAVADOC COMMENT ELIDED
    String asString() {
        String coordinates;
        if (isCoordinates) {
            coordinates = "Lat: " + latitude +
		" Lon: " + longitude;
        } else {
            coordinates = "null";
        }
        return "Landmark: { storeName = " + storeName +
	    " recordId = " + recordId +
	    " name = " + name +
	    " description = " + description +
	    " coordinates = " + coordinates +
	    " addressInfo = " + getAddressInfo() + " }";
    }

    // JAVADOC COMMENT ELIDED
    public boolean equals(Object o) {
        if (!(o instanceof LandmarkImpl)) {
            return false;
        }
        if (this == o) {
            return true;
        }
        LandmarkImpl lm = (LandmarkImpl)o;
        boolean idEquals = recordId == lm.recordId;
        boolean storeEquals = ((storeName == null) && (lm.storeName == null)) ||
	    ((storeName != null) && storeName.equals(lm.storeName));
        return idEquals && storeEquals;
    }

    /** 
     * Returns a proper hash code for using in Hashtable.
     * @return hash code value for this instance
     */
    public int hashCode() {
        return recordId;
    }

    // JAVADOC COMMENT ELIDED
    static {
        initNativeClass();
    }

    // JAVADOC COMMENT ELIDED
    private native static void initNativeClass();
    
}

