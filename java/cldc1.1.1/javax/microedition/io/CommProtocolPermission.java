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

import java.security.PermissionCollection;
import java.security.Permission;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class represents access rights to connections via the "comm" protocol.
 * A <code>CommProtocolPermission</code> consists of a URI string but no
 * actions list.
 * <p>
 * The URI string specifies a logical serial port connection and optional
 * parameters.  It takes the following form:
 * <pre>
 * comm:{port identifier}[{optional parameters}]
 * </pre>
 * An asterisk may appear at the end of the URI string to indicate a
 * wildcard match in the <em>port identifer</em> field.
 * Valid examples include "comm:*" and "comm:port*".
 *
 * @see Connector#open
 * @see "javax.microedition.io.CommConnection" in <a href="http://www.jcp.org/en/jsr/detail?id=271">MIDP 3.0 Specification</a>
 */
public final class CommProtocolPermission extends GCFPermission {
  // A trick to create a path normalizer, pass it to superclass ctor and then
  // store it to an instance field. It works unless the superclass constructor
  // somehow triggers construction of one more subclass instance.
  private static final Hashtable map = new Hashtable();

  private static PathNormalizer createPathNormalizer() {
    CommOptParameterParser p = new CommOptParameterParser();
    
    Thread t = Thread.currentThread();
    // NOTE: the mapping can be non-empty at this point if the previous
    // ctor invocation was terminated abruptly with an exception thrown
    // from superclass ctor. We override the existing mapping.
    map.put(t, p);

    return p;
  }

  private static CommOptParameterParser getParser() {
    Thread t = Thread.currentThread();
    Object o = map.remove(t);
    return (CommOptParameterParser)o;
  }

  private final CommOptParameterParser parser;

  /**
   * Creates a new <code>CommProtocolPermission</code> with the specified
   * URI as its name.  The URI string must conform to the specification
   * given above.
   *
   * @param uri the URI string.
   *
   * @throws IllegalArgumentException if <code>uri</code> is malformed.
   * @throws NullPointerException if <code>uri</code> is <code>null</code>.
   *
   * @see #getName
   */
  public CommProtocolPermission(String uri) {
    super(uri, false /* requireAuthority */, 
          null /* portRangeNormalizer */, 
          CommProtocolPermission.createPathNormalizer());

    parser = CommProtocolPermission.getParser();

    if (!"comm".equals(getProtocol())) {
      throw new IllegalArgumentException("Expected comm protocol: " + uri);
    }

    String info = getSchemeSpecificPart();

    if (info == null || "".equals(info)) {
      throw new IllegalArgumentException(
        "Expected scheme-specific part: " + uri);
    }
  }

  /**
   * Checks if this <code>CommProtocolPermission</code> object "implies"
   * the specified permission.
   * <p>
   * More specifically, this method returns <code>true</code> if:
   * <p>
   * <ul>
   * <li> <i>p</i> is an instanceof <code>CommProtocolPermission</code>, and
   * <p>
   * <li> <i>p</i>'s URI string equals or (in the case of wildcards) is
   *   implied by this object's URI string.
   *   For example, "comm:*" implies "comm:port1;baudrate=300".
   * </ul>
   * @param p the permission to check against
   *
   * @return true if the specified permission is implied by this object,
   * false if not.
   */
  public boolean implies(Permission p) {
    if (!(p instanceof CommProtocolPermission)) {
      return false;
    } 
    CommProtocolPermission that = (CommProtocolPermission)p;

    String thisPart = this.getSchemeSpecificPart();
    String thatPart = that.getSchemeSpecificPart();

    if (thisPart.equals(thatPart)) {
      return true;
    }

    if (thisPart.endsWith("*")) {
      String s = thisPart.substring(0, thisPart.length() - 1);
      return thatPart.startsWith(s);
    }

    if (!this.parser.port.equals(that.parser.port)) {
      return false;
    }

    if (this.parser.baud != -1 && this.parser.baud != that.parser.baud) {
      return false;
    }

    int thisParams = 
      this.parser.bbc | this.parser.stop | this.parser.parity | 
      this.parser.rts | this.parser.cts | this.parser.blocking;
    int thatParams = 
      that.parser.bbc | that.parser.stop | that.parser.parity | 
      that.parser.rts | that.parser.cts | that.parser.blocking;

    return (thisParams & thatParams) == thatParams;
  }

  /**
   * Checks two <code>CommProtocolPermission</code> objects for equality.
   * 
   * @param obj the object we are testing for equality with this object.
   *
   * @return <code>true</code> if <code>obj</code> is a
   * <code>CommProtocolPermission</code> and has the same URI string as
   * this <code>CommProtocolPermission</code> object.
   */
  public boolean equals(Object obj) {
    if (!(obj instanceof CommProtocolPermission)) {
      return false;
    }
    CommProtocolPermission other = (CommProtocolPermission)obj;
    return other.getURI().equals(getURI());
  }

  /**
   * Returns the hash code value for this object.
   *
   * @return a hash code value for this object.
   */
  public int hashCode() {
    return getURI().hashCode();
  }

  /**
   * Returns the canonical string representation of the actions, which
   * currently is the empty string "", since there are no actions defined
   * for <code>CommProtocolPermission</code>.
   *
   * @return the empty string "".
   */
  public String getActions() {
    return "";
  }

  /**
   * Returns a new <code>PermissionCollection</code> for storing
   * <code>CommProtocolPermission</code> objects.
   * <p>
   * <code>CommProtocolPermission</code> objects must be stored in a
   * manner that allows
   * them to be inserted into the collection in any order, but that also
   * enables the <code>PermissionCollection</code> implies method to be
   * implemented in an efficient (and consistent) manner.
   *
   * @return a new <code>PermissionCollection</code> suitable for storing
   * <code>CommProtocolPermission</code> objects.
   */
  public PermissionCollection newPermissionCollection() {
    return new CommProtocolPermissionCollection();
  } 
}

/**
 * A GCFPermissionCollection stores a collection
 * of GCFPermission permissions. GCFPermission objects
 * must be stored in a manner that allows them to be inserted in any
 * order, but enable the implies function to evaluate the implies
 * method in an efficient (and consistent) manner.
 *
 * A GCFPermissionCollection handles comparing a permission like "a.b.c.d.e"
 * with a Permission such as "a.b.*", or "*".
 *
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.PermissionsImpl
 *
 * 
 *
 */

final class CommProtocolPermissionCollection extends PermissionCollection {
  private Vector permissions = new Vector(6);;

  /**
   * Create an empty GCFPermissionCollection object.
   *
   */
  public CommProtocolPermissionCollection() {}

  /**
   * Adds a permission to the GCFPermissions. The key for the hash is
   * permission.uri.
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
    if (!(permission instanceof CommProtocolPermission)) {
      throw new IllegalArgumentException("invalid permission: "+
                                         permission);
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
    if (!(permission instanceof CommProtocolPermission)) {
      return false;
    }

    Enumeration search = elements();
    while (search.hasMoreElements()) {
      CommProtocolPermission p = (CommProtocolPermission)search.nextElement();
      if (p.implies(permission)) {
        return true;
      }
    }  

    return false;          
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
