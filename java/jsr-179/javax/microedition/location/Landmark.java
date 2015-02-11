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

/**
 * This class is defined by the JSR-179 specification
 * <em>Location API for J2ME for J2ME&trade;.</em>
 */
// JAVADOC COMMENT ELIDED
public class Landmark {
    
    /** Landmark Implementation */
    private LandmarkImpl landmarkImpl;
    private String nameImpl;
    private String descriptionImpl;
    private QualifiedCoordinates coordinatesImpl;
    private AddressInfo addressInfoImpl;

    // JAVADOC COMMENT ELIDED
    public Landmark(String name, String description, 
		    QualifiedCoordinates coordinates,
		    AddressInfo addressInfo) {
        landmarkImpl = null;
        setName(name);
        descriptionImpl = description;
        coordinatesImpl = coordinates;
        addressInfoImpl = addressInfo;
    }

    // JAVADOC COMMENT ELIDED
    public String getName() {
        return nameImpl;
    }

    // JAVADOC COMMENT ELIDED
    public String getDescription() {
        return descriptionImpl;
    }

    // JAVADOC COMMENT ELIDED
    public QualifiedCoordinates getQualifiedCoordinates() {
        return coordinatesImpl;
    }

    // JAVADOC COMMENT ELIDED
    public AddressInfo getAddressInfo() {
        return addressInfoImpl;
    }

    // JAVADOC COMMENT ELIDED
    public void setName(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        nameImpl = name;
    }

    // JAVADOC COMMENT ELIDED
    public void setDescription(String description) {
        descriptionImpl = description;
    }

    // JAVADOC COMMENT ELIDED
    public void setQualifiedCoordinates(QualifiedCoordinates coordinates) {
        coordinatesImpl = coordinates;
    }

    // JAVADOC COMMENT ELIDED
    public void setAddressInfo(AddressInfo addressInfo) {
        addressInfoImpl = addressInfo;
    }
    
    // JAVADOC COMMENT ELIDED
    Landmark(LandmarkImpl landmark) {
        landmarkImpl = landmark;
        nameImpl = landmark.getName();
        descriptionImpl = landmark.getDescription();
        coordinatesImpl = landmark.getQualifiedCoordinates();
        addressInfoImpl = landmark.getAddressInfo();
    }

    // JAVADOC COMMENT ELIDED
    LandmarkImpl getInstance() {
        if (landmarkImpl == null) {
            landmarkImpl = new LandmarkImpl(nameImpl, descriptionImpl, 
                coordinatesImpl, addressInfoImpl);
        } else {
            landmarkImpl.setName(nameImpl);
            landmarkImpl.setDescription(descriptionImpl);
            landmarkImpl.setQualifiedCoordinates(coordinatesImpl);
            landmarkImpl.setAddressInfo(addressInfoImpl);
        }
        return landmarkImpl;
    }
}

