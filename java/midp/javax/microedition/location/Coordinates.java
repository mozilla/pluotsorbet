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

import com.sun.j2me.location.*;

/**
 * This class is defined by the JSR-179 specification
 * <em>Location API for J2ME for J2ME&trade;.</em>
 */
// JAVADOC COMMENT ELIDED
public class Coordinates {
    // JAVADOC COMMENT ELIDED
    public static final int DD_MM_SS = 1;
    // JAVADOC COMMENT ELIDED
    public static final int DD_MM = 2;
    // JAVADOC COMMENT ELIDED
    private double latitude;
    // JAVADOC COMMENT ELIDED
    private double longitude;
    // JAVADOC COMMENT ELIDED
    private float altitude;

    // JAVADOC COMMENT ELIDED
    static final double EARTH_RADIUS = 6378137D;
    // JAVADOC COMMENT ELIDED
    static final double FLATTENING = 298.257223563D;
    // JAVADOC COMMENT ELIDED
    static final double DEG2RAD = 0.01745329252D;
    // JAVADOC COMMENT ELIDED
    private float azimuth;
    // JAVADOC COMMENT ELIDED
    private float distance;
    
    // JAVADOC COMMENT ELIDED
    public Coordinates(double latitude, double longitude, float altitude) {
        setLatitude(latitude);
        setLongitude(longitude);
        this.altitude = altitude;
    }

    // JAVADOC COMMENT ELIDED
    public double getLatitude() {
        return latitude;
    }

    // JAVADOC COMMENT ELIDED
    public double getLongitude() {
        return longitude;
    }

    // JAVADOC COMMENT ELIDED
    public float getAltitude() {
        return altitude;
    }

    // JAVADOC COMMENT ELIDED
    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    // JAVADOC COMMENT ELIDED
    public void setLatitude(double latitude) {
        Util.checkRange(latitude, -90, 90,
			"Latitude out of range [-90.0, 90]: ");
        this.latitude = latitude;
    }

    // JAVADOC COMMENT ELIDED
    public void setLongitude(double longitude) {
        Util.checkRange(longitude, -180, 180,
			"Longitude out of range [-180.0, 180): ");
        if (longitude == 180D) {
            throw new IllegalArgumentException(
	        "Longitude out of range [-180.0, 180): " + longitude);
        }
        this.longitude = longitude;
    }

    // JAVADOC COMMENT ELIDED
    public static double convert(String coordinate) {
	if (coordinate == null) {
	    throw new NullPointerException("Null string specified");
	}
        // tokenize the coordianates to 2 or 3 elements
        if (coordinate.startsWith("0") && (!coordinate.startsWith("0:"))) {
            throw new IllegalArgumentException(
                "A coordinate cannot start with a 0 with two digits");
        }
        double[] coordinates = new double[] { Double.NaN, Double.NaN, 0 };
        int next = -1;
        int current = 0;
        do {
            if (current > 2) {
                throw new
                    IllegalArgumentException(
                        "Invalid coordinate format");
            }
            int position = next + 1;
            next = coordinate.indexOf(':', position);
            String currentText;
            if (next > -1) {
                currentText = coordinate.substring(position, next);
                try {
                    coordinates[current] = Double.parseDouble(currentText);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                        "Invalid coordinate format: " + e.getMessage());
                }

                // only the last coordinate may be a fracture
                if ((long)coordinates[current] != coordinates[current]) {
                    throw new
                        IllegalArgumentException(
                            "Only the last coordinate may be a fracture: "
                            + coordinate);
                }
            } else {
                currentText = coordinate.substring(position,
                                                   coordinate.length());
                try {
                    coordinates[current] = Double.parseDouble(currentText);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                        "Invalid coordinate format: " + e.getMessage());
                }
            }
            if (currentText.startsWith("+")) {
                throw new
                    IllegalArgumentException(
                        "Coordinate should not use 'plus' sign :"
                        + currentText);
            }
            if (current > 0) {
                int pos = currentText.indexOf('.');
                if (pos > -1) {
                    if (pos != 2 || currentText.length() < 4) {
                        throw new IllegalArgumentException(
                            "Invalid coordinate format");
                    }
                    if (current != 2) {
                        if (currentText.length() - pos > 6) {
                            throw new IllegalArgumentException(
                                "Invalid coordinate format");
                        }
                    } else {
                        if (currentText.length() - pos > 4) {
                            throw new IllegalArgumentException(
                                "Invalid coordinate format");
                        }
                    }
                } else {
                    if (currentText.length() != 2) {
                        throw new IllegalArgumentException(
                            "Invalid coordinate format");
                    }
                }
            }
            if (currentText.endsWith(".")) {
                throw new IllegalArgumentException(
                    "Invalid coordinate format");

            }
            current++;
        } while (next > -1);

        // special case for 180 when the degrees is -180 and the
        // minutes, seconds and decimal fractions are 0
        if (coordinates[0] != -180D) {
            Util.checkRange(coordinates[0], -179, 179,
                            "Degrees out of range [-179.0, 179]: ");
            Util.checkRange(coordinates[1], 0, 60,
                            "Minutes out of range [0, 59]: ");
            Util.checkRange(coordinates[2], 0, 60,
                            "Seconds out of range [0, 59]: ");
            if (coordinates[1] == 60D) {
                throw new IllegalArgumentException(
                    "Minutes out of range [0, 59]: 60");
            }
            if (coordinates[2] == 60D) {
                throw new IllegalArgumentException(
                    "Seconds out of range [0, 59]: 60");
            }
            if (Double.isNaN(coordinates[1])) {
                throw new IllegalArgumentException(
                    "Invalid coordinate format");
            }
        } else {
            if (coordinates[1] != 0D || coordinates[2] != 0D) {
                throw new IllegalArgumentException(
                    "Invalid coordinate format");
            }
        }

        // convert the integer array to a numeric representation:
        double value = coordinates[0];
        if (!coordinate.startsWith("-")) {
            value += coordinates[1] / 60 + coordinates[2] / 3600;
        } else {
            value -= coordinates[1] / 60 + coordinates[2] / 3600;
        }
        return value;
    }

    // JAVADOC COMMENT ELIDED
    public static String convert(double coordinate, int outputType) {
        if (coordinate == 180D || Double.isNaN(coordinate)) {
            throw new IllegalArgumentException("Coordinate out of range");
        }
        Util.checkRange(coordinate, -180, 180,
	    "Coordinate out of range [-180.0, 180): ");
        StringBuffer buffer = new StringBuffer();
        if (coordinate < 0) {
            buffer.append("-");
        }
        coordinate = Math.abs(coordinate);
        int deg = (int)coordinate;
        buffer.append(deg);
        buffer.append(":");
        double dMin = (coordinate - deg) * 60D;
        if (outputType == DD_MM_SS) {
            int min1 = (int)dMin;
	    if (min1 == 60) min1 = 59;
            if (min1 < 10) {
                buffer.append("0");
            }
            buffer.append(min1);
            buffer.append(":");
            double dSec = (dMin - min1) * 60D;
            double sec1 = (double)(int)Math.floor(1000D * dSec + 0.5D) / 1000D;
	    if (sec1 >= 60) sec1 = 59.999;
            if (sec1 < 10) {
                buffer.append("0");
            }
            buffer.append(sec1);
        } else {
            if (outputType != DD_MM) {
                throw new 
		    IllegalArgumentException(
			"outputType must be either DD_MM or DD_MM_SS, " +
			"instead we got: " + outputType);
            }
            double min2 = (double)(int)Math.floor(100000D * dMin + 0.5D)
		/ 100000D;
	    if (min2 >= 60) min2 = 59.99999;
            if (min2 < 10) {
                buffer.append("0");
            }
            buffer.append(min2);
        }
        return buffer.toString();
    }

    // JAVADOC COMMENT ELIDED
    public float azimuthTo(Coordinates to) {
        if (to == null) {
            throw new NullPointerException("Null coordinates specified");
        }
        computeAzimuthAndDistance(latitude, longitude,
				  to.latitude, to.longitude);
        return azimuth;
    }

    // JAVADOC COMMENT ELIDED
    public float distance(Coordinates to) {
        if (to == null) {
            throw new NullPointerException("Null coordinates specified");
        }
        computeAzimuthAndDistance(latitude, longitude,
				  to.latitude, to.longitude);
        return distance;
    }

    // JAVADOC COMMENT ELIDED
    private void computeAzimuthAndDistance(double lat1, double long1,
					   double lat2, double long2) {
        if ((lat1 == lat2)  && (long1 == long2)) {
	    azimuth = 0;
	    distance = 0;
            return;
        }
        double c = 0.0;
        double d = 0.0;
        double e = 0.0;
        double y = 0.0;
        double sa = 0.0;
        double sx = 0.0;
        double sy = 0.0;
        double cx = 0.0;
        double cy = 0.0;
        double cz = 0.0;
        double c2a = 0.0;

        double f = 1.0D / FLATTENING; // Flattening factor

        // Initial values
        double eps = 0.5E-13; // Tolerence
        double glon1 = long1 * DEG2RAD;
        double glat1 = lat1 * DEG2RAD;
        double glon2 = long2 * DEG2RAD;
        double glat2 = lat2 * DEG2RAD;

        double r = 1.0D - f;
        double tu1 = r * Math.sin(glat1) / Math.cos(glat1);
        double tu2 = r * Math.sin(glat2) / Math.cos(glat2);
        double cu1 = 1 / Math.sqrt(1 + tu1 * tu1);
        double su1 = cu1 * tu1;
        double cu2 = 1 / Math.sqrt(1 + tu2 * tu2);
        double s = cu1 * cu2;
        double baz = s * tu2;
        double faz = baz * tu1;
        double x = glon2 - glon1;
	
        // Iterate
        do {
            sx = Math.sin(x);
            cx = Math.cos(x);
            tu1 = cu2 * sx;
            tu2 = baz - su1 * cu2 * cx;
            sy = Math.sqrt(tu1 * tu1 + tu2 * tu2);
            cy = s * cx + faz;
            y = LocationMath.atan2(sy, cy);
            sa = s * sx / sy;
            c2a = -sa * sa + 1;
            cz = faz + faz;
            if (c2a > 0) {
                cz = -cz / c2a + cy;
            }
            e = cz * cz * 2 - 1;
            c = ((-3 * c2a + 4) * f + 4) * c2a * f / 16;
            d = x;
            x = ((e * cy * c + cz) * sy * c + y) * sa;
            x = (1 - c) * x * f + glon2 - glon1;
        } while (Math.abs(d - x) > eps);

        // Finish up
        faz = LocationMath.atan2(tu1, tu2);
        azimuth = (float)(faz / DEG2RAD);
        if (azimuth < 0) {
            azimuth += 360;
        }
        if (lat1 == 90D) {
            azimuth = 180F;
        } else if (lat1 == -90D) {
            azimuth = 0.0F;
        }
        x = Math.sqrt((1 / r / r - 1) * c2a + 1) + 1;
        x = (x - 2) / x;
        c = 1 - x;
        c = (x * x / 4 + 1) / c;
        d = (0.375 * x * x - 1) * x;
        x = e * cy;
        s = 1 - 2 * e;
        distance = (float)(((((sy * sy * 4 - 3) * s * cz * d / 6 - x) * d / 4 +
			     cz) * sy * d + y) * c * EARTH_RADIUS * r);
    }
}
