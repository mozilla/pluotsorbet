/*
 *   
 *
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
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

package javax.microedition.io;

import java.security.Permission;
import java.security.PermissionCollection;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/*
 * An abstract class that is the superclass of all permissions used with the 
 *  Generic Connection Framework. A GCFPermission  consists of a URI string 
 * representing access rights to a connection based on the specified protocol scheme.
 *
 * A specification that extends the Generic Connection Framework with a new protocol scheme 
 * should define a subclass of GCFPermission to guard access to the protocol. 
 */
public abstract class GCFPermission extends Permission {

  private URIParser parser;

  // A trick to create an URI parser and normalize the URI before passing it 
  // to the superclass constructor: we use kind of thread-local storage 
  // for URI parser. It works unless the superclass constructor somehow 
  // triggers construction of one more subclass instance.
  private static final Hashtable map = new Hashtable();

  private static String normalize(String uri, 
                                  PortRangeNormalizer portRangeNormalizer,
                                  PathNormalizer pathNormalizer,
                                  boolean normalizeAuthority) {
    URIParser p = new URIParser(uri, 
                                portRangeNormalizer,
                                pathNormalizer, 
                                normalizeAuthority);
    
    Thread t = Thread.currentThread();
    // NOTE: the mapping can be non-empty at this point if the previous
    // ctor invocation was terminated abruptly with an exception thrown
    // from superclass ctor. We override the existing mapping.
    map.put(t, p);

    return p.getURI();
  }

  private static URIParser getParser() {
    Thread t = Thread.currentThread();
    Object o = map.remove(t);
    return (URIParser)o;
  }

  /**
   * Constructs a <code>GCFPermission</code> with the specified URI.
   * The URI must begin with a string indicating the protocol
   * scheme, followed by a ':'.
   *
   * @param uri the URI string.
   *
   * @throws IllegalArgumentException if <code>uri</code> is malformed.
   * @throws NullPointerException if <code>uri</code> is <code>null</code>.
   *
   * @see #getURI
   */ 
  public GCFPermission(String uri) {
    this(uri, false, null, null, false);
  }

  GCFPermission(String uri, boolean requireAuthority) {
    this(uri, requireAuthority, null, null, false);
  }

  GCFPermission(String uri, boolean requireAuthority, 
                PortRangeNormalizer portRangeNormalizer) {
    this(uri, requireAuthority, portRangeNormalizer, null, false);
  }

  GCFPermission(String uri, boolean requireAuthority, 
                PortRangeNormalizer portRangeNormalizer, 
                PathNormalizer pathNormalizer) {
    this(uri, requireAuthority, portRangeNormalizer, pathNormalizer, false);
  }

  GCFPermission(String uri, boolean requireAuthority, 
                PortRangeNormalizer portRangeNormalizer, 
                PathNormalizer pathNormalizer, 
                boolean normalizeAuthority) {
    super(normalize(uri, 
                    portRangeNormalizer, 
                    pathNormalizer, 
                    normalizeAuthority));

    parser = getParser();

    String scheme = parser.getScheme();
    if (scheme == null || "".equals(scheme)) {
      throw new IllegalArgumentException("Expected protocol scheme: " + uri);
    }

    if (requireAuthority && !getURI().startsWith(scheme + "://")) {
      throw new IllegalArgumentException("Invalid URI: " + uri);
    }      
  }

  /**
   * Returns the URI of this GCFPermission.
   *
   * @return the URI string, identical to Permission.getName().
   */
  public String getURI() {
    return getName();
  }

  /**
   * Returns the protocol scheme of this GCFPermission. 
   * The protocol scheme is the string preceding the first ':' in the URI.
   *
   * @return the protocol scheme portion of the URI string.
   */
  public String getProtocol() {
    return parser.getScheme();
  }

  final int getMinPort() {
    return parser.getPortRange()[0];
  }

  final int getMaxPort() {
    return parser.getPortRange()[1];
  }

  final String getHost() {
    return parser.getHost();
  }

  final String getPath() {
    return parser.getPath();
  }

  final String getSchemeSpecificPart() {
    return parser.getSchemeSpecificPart();
  }

  final boolean impliesByHost(GCFPermission that) {
    String thisHost = this.parser.getHost();
    String thatHost = that.parser.getHost();

    boolean equal = thisHost.equals(thatHost);

    if (equal) {
      return true;
    }

    // Handle empty host names - server connections
    if ("".equals(thisHost) || "".equals(thatHost)) {
      return equal;
    }

    if (thisHost.startsWith("*")) {
      return thatHost.endsWith(thisHost.substring(1));
    }

    return false;    
  }

  final boolean impliesByPorts(GCFPermission that) {
    return (this.getMinPort() <= that.getMinPort()) && 
           (this.getMaxPort() >= that.getMaxPort());
  }

  final void checkNoHostPort() {
    parser.checkNoHost();
    parser.checkNoPortRange();
  }

  final void checkHostPortPathOnly() {
    parser.checkNoFragment();
    parser.checkNoUserInfo();
    parser.checkNoQuery();
  }

  final void checkHostPortOnly() {
    parser.checkNoFragment();
    parser.checkNoUserInfo();
    parser.checkNoPath();
    parser.checkNoQuery();
  }

  final void checkPortRange() {
    parser.checkPortRange();
  }

  final void checkNoPortRange() {
    parser.checkNoPortRange();
  }
}

/**
 * A GCFPermissionCollection stores a collection
 * of GCF permissions. GCFPermission objects
 * must be stored in a manner that allows them to be inserted in any
 * order, but enable the implies function to evaluate the implies
 * method in an efficient (and consistent) manner.
 *
 * Subclasses should override <code>add</code> method to 
 *
 * @see java.security.Permission
 */

final class GCFPermissionCollection extends PermissionCollection {
  private static final Class gcfPermissionClass;
  private final Class permissionClass;
  private final Vector permissions = new Vector(6);

  static {
    try {
      gcfPermissionClass = 
        Class.forName("javax.microedition.io.GCFPermission");
    } catch (ClassNotFoundException e) {
      throw new Error(e.toString());
    }
  }

  /**
   * Create an empty GCFPermissionCollection object.
   *
   */
  public GCFPermissionCollection(Class clazz) {
    if (!gcfPermissionClass.isAssignableFrom(clazz)) {
      throw new IllegalArgumentException();
    }
    permissionClass = clazz;
  }

  /**
   * Adds a permission to the collection.
   *
   * @param permission the Permission object to add.
   *
   * @exception IllegalArgumentException - if the permission is not a
   *                                       GCFPermission, or if
   *					     the permission is not of the
   *					     same Class as the other
   *					     permissions in this collection.
   *
   * @exception SecurityException - if this GCFPermissionCollection object
   *                                has been marked readonly
   */
  public void add(Permission permission) {
    if (!permissionClass.isInstance(permission)) {
      throw new IllegalArgumentException(
        "Invalid permission class: " + permission);
    }
	
    if (isReadOnly()) {
      throw new SecurityException(
        "Cannot add a Permission to a readonly PermissionCollection");
    }

    permissions.addElement(permission);
  }

  /**
   * Check and see if this set of permissions implies the permissions
   * expressed in "permission".
   *
   * @param p the Permission object to compare
   *
   * @return true if "permission" is a proper subset of a permission in
   * the set, false if not.
   */
  public boolean implies(Permission permission) { 
    if (!permissionClass.isInstance(permission)) {
      return false;
    }

    GCFPermission perm = (GCFPermission) permission;

    int perm_low  = perm.getMinPort();
    int perm_high = perm.getMaxPort();

    Enumeration search = permissions.elements();
    int count = permissions.size();
    int port_low[] = new int[count];
    int port_high[] = new int[count];
    int port_range_count = 0;
    while (search.hasMoreElements()) {
      GCFPermission cur_perm = 
        (GCFPermission)search.nextElement();
      if (cur_perm.impliesByHost(perm)) {
        if (cur_perm.impliesByPorts(perm)) {
          return true;
        }
        
        port_low[port_range_count] = cur_perm.getMinPort();
        port_high[port_range_count] = cur_perm.getMaxPort();
        port_range_count++;
      }
    }

    // now we need to determine if found port ranges cover perm port range.
    // we will sort the port ranges by the low border and will try to found
    // the longest continues range covered by these ranges
    // we will use simple x^2 sort here, cause it is rare situation when we
    // have many port ranges.

    for (int i = 0; i < port_range_count; i++) {
      for (int j = 0; j < port_range_count - 1; j++) {
        if (port_low[j] > port_low[j+1]) {
          int tmp = port_low[j];
          port_low[j] = port_low[j+1];
          port_low[j+1] = tmp;
          tmp = port_high[j];
          port_high[j] = port_high[j+1];
          port_high[j+1] = tmp;
        }
      }
    }

    int current_low = port_low[0];
    int current_high = port_high[0];
    for (int i = 1; i < port_range_count; i++) {
      if (port_low[i] > current_high + 1) { //end of continious range
        if (current_low <= perm_low && current_high >= perm_high) {
          return true;
        } 
        if (perm_low <= current_high) {
          return false;
        }
        current_low = port_low[i];
        current_high = port_high[i];
      } else {
        if (current_high < port_high[i]) {
          current_high = port_high[i];
        }
      }
    }
    return (current_low <= perm_low && current_high >= perm_high);
  }

  /**
   * Returns an enumeration of all the GCFPermission objects in the
   * container.
   *
   * @return an enumeration of all the GCFPermission objects.
   */
  public Enumeration elements() {
    return permissions.elements();
  }
}
