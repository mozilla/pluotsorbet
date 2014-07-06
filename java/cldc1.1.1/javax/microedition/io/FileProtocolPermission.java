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
import java.util.Vector;

/**
 * This class represents access rights to connections via the "file"
 * protocol.  A <code>FileProtocolPermission</code> consists of a
 * URI string indicating a pathname and a set of actions desired for that
 * pathname.
 * <p>
 * The URI string takes the following form:
 * <pre>
 * file://{pathname}
 * </pre>
 * A pathname that ends in "/*" indicates
 * all the files and directories contained in that directory.  A pathname
 * that ends with "/-" indicates (recursively) all files
 * and subdirectories contained in that directory.
 * <p>
 * The actions to be granted are passed to the constructor in a string
 * containing a list of one or more comma-separated keywords. The possible
 * keywords are "read" and "write".  The actions string is converted to
 * lowercase before processing.
 *
 * @see Connector#open
 * @see "javax.microedition.io.file.FileConnection" in <a href="http://www.jcp.org/en/jsr/detail?id=75">FileConnection Optional Package Specification</a>
 */
public final class FileProtocolPermission extends GCFPermission {
  /**
   * Read from a file
   */
  private final static int READ	= 0x1;

  /**
   * Write to a file
   */
  private final static int WRITE	= 0x2;

  /**
   * No actions
   */
  private final static int NONE		= 0x0;

  /**
   * All actions
   */ 
  private final static int ALL	= READ|WRITE;

  /**
   * Path normalizer
   */
  private static final PathNormalizer pathNormalizer = 
    new DefaultPathNormalizer();

  /**
   * Action mask
   */
  private int action_mask = NONE;

  /**
   * Creates a new <code>FileProtocolPermission</code> with the specified
   * actions.  The specified URI becomes the name of the
   * <code>FileProtocolPermission</code>.
   * The URI string must conform to the specification given above.
   *
   * @param uri the URI string
   * @param actions comma-separated list of desired actions
   *
   * @throws IllegalArgumentException if <code>uri</code> or
   * <code>actions</code> is malformed.
   * @throws NullPointerException if <code>uri</code> or
   * <code>actions</code> is <code>null</code>.
   *
   * @see #getName
   * @see #getActions
   */
  public FileProtocolPermission(String uri, String actions) {
    super(uri, false /*require authority*/, 
          null /*port range normalizer*/, 
          pathNormalizer, 
          true /*normalize authority*/);

    if (!"file".equals(getProtocol())) {
      throw new IllegalArgumentException("Expected file protocol: " + uri);
    }

    checkHostPortPathOnly();

    checkNoPortRange();

    String host = getHost();

    if (host != null && !"".equals(host) && !"localhost".equals(host)) {
      throw new IllegalArgumentException("Invalid host component: " + uri);
    }

    if (!uri.toLowerCase().startsWith("file:")) {
      throw new IllegalArgumentException(
        "Expected URI of the form file:{pathname} or file://{pathname}: " + 
        uri);
    }

    String path = getPath();

    if (path.charAt(0) != '/') {
      throw new IllegalArgumentException(
        "Path in the URI must be absolute: " + uri);
    }

    if (uri.equalsIgnoreCase("file:") || 
        uri.equalsIgnoreCase("file://") || 
        uri.equalsIgnoreCase("file://localhost")) {
      throw new IllegalArgumentException("No path specified: " + uri);
    }

    action_mask = getMask(actions);
  }

  int getActionMask() {
    return action_mask;
  }

  /**
   * Convert an action string to an integer actions mask. 
   *
   * @param action the action string
   * @return the action mask
   */
  private static int getMask(String action) {

    if (action == null) {
      throw new NullPointerException("action can't be null");
    }

    if (action.equals("")) {
      throw new IllegalArgumentException("action can't be empty");
    }

    int mask = NONE;
    
    char[] a = action.toCharArray();

    int i = a.length - 1;
    if (i < 0) {
      return mask;
    }

    while (i != -1) {
      // check for the known strings
      int matchlen;

      if (i >= 3 && 
          (a[i-3] == 'r' || a[i-3] == 'R') &&
          (a[i-2] == 'e' || a[i-2] == 'E') &&
          (a[i-1] == 'a' || a[i-1] == 'A') &&
          (a[i] == 'd' || a[i] == 'D')) {
        matchlen = 4;
        mask |= READ;

      } else if (i >= 4 && 
                 (a[i-4] == 'w' || a[i-4] == 'W') &&
                 (a[i-3] == 'r' || a[i-3] == 'R') &&
                 (a[i-2] == 'i' || a[i-2] == 'I') &&
                 (a[i-1] == 't' || a[i-1] == 'T') &&
                 (a[i] == 'e' || a[i] == 'E')) {
        matchlen = 5;
        mask |= WRITE;
        
      } else {
        // parse error
        throw new IllegalArgumentException(
          "invalid actions: " + action);
      }

      // make sure we didn't just match the tail of a word
      // like "ackbarfread".  Also, skip to the comma.
      if (i >= matchlen) {
        // don't match the comma at the beginning of the string
        if (i > matchlen && a[i-matchlen] == ',') {
          i--;
        } else {
          // parse error
          throw new IllegalArgumentException(
            "invalid actions: " + action);
        }
      }

      // point i at the location of the comma minus one (or -1).
      i -= matchlen;
    }

    return mask;
  }

  /**
   * Checks if this <code>FileProtocolPermission</code> object "implies"
   * the specified permission.
   * <p>
   * More specifically, this method returns <code>true</code> if:
   * <p>
   * <ul>
   * <li> <i>p</i> is an instanceof <code>FileProtocolPermission</code>,
   * <p>
   * <li> <i>p</i>'s actions are a proper subset of this
   * object's actions, and
   * <p>
   * <li> <i>p</i>'s pathname is implied by this object's
   *      pathname. For example, "/tmp/*" implies "/tmp/foo", since
   *      "/tmp/*" encompasses the "/tmp" directory and all files in that
   *      directory, including the one named "foo".
   * </ul>
   * <p>
   * @param p the permission to check against
   *
   * @return true if the specified permission is implied by this object,
   * false if not.
   */
  public boolean implies(Permission p) {
    if (!(p instanceof FileProtocolPermission)) {
      return false;
    } 

    FileProtocolPermission that = (FileProtocolPermission)p;

    if ((this.action_mask & that.action_mask) != that.action_mask) {
      return false;
    }

    return impliesByPath(that);
  }

  boolean impliesByPath(FileProtocolPermission that) {
    String thisPath = this.getPath();
    String thatPath = that.getPath();

    if (thisPath.equals(thatPath)) {
      return true;
    }

    // A pathname that ends in "/*" indicates all the files and directories
    // contained in that directory.
    if (thisPath.endsWith("/*")) {
      int len = thisPath.length();
      String s = thisPath.substring(0, len - 1);
      return (thatPath.startsWith(s) && 
              !thatPath.endsWith("/-") &&
              thatPath.indexOf('/', len - 1) == -1);
    }

    // A pathname that ends with "/-" indicates (recursively) all files
    // and subdirectories contained in that directory.    
    if (thisPath.endsWith("/-")) {
      int len = thisPath.length();
      String s = thisPath.substring(0, len - 1);
      return thatPath.startsWith(s);
    }

    return false;
  }

  /**
   * Checks two <code>FileProtocolPermission</code> objects for equality.
   * 
   * @param obj the object we are testing for equality with this object.
   *
   * @return <code>true</code> if <code>obj</code> is a
   * <code>FileProtocolPermission</code>,
   * and has the same URI string and actions as
   * this <code>FileProtocolPermission</code> object.
   */
  public boolean equals(Object obj) {
    if (!(obj instanceof FileProtocolPermission)) {
      return false;
    }
    FileProtocolPermission other = (FileProtocolPermission)obj;
    return other.getURI().equals(getURI()) 
           && action_mask == other.action_mask;
  }

  /**
   * Returns the hash code value for this object.
   *
   * @return a hash code value for this object.
   */
  public int hashCode() {
    return getURI().hashCode() ^ action_mask;
  }

  /**
   * Returns the canonical string representation of the actions.
   * If both read and write actions are allowed, this method returns
   * the string <code>"read,write"</code>.
   *
   * @return the canonical string representation of the actions.
   */
  public String getActions() {
    switch (action_mask) {
    case ALL:    return "read,write";
    case READ:   return "read";
    case WRITE:  return "write";
    default:     return "";
    }
  }

  /**
   * Returns a new <code>PermissionCollection</code> for storing
   * <code>FileProtocolPermission</code> objects.
   * <p>
   * <code>FileProtocolPermission</code> objects must be stored in a
   * manner that allows
   * them to be inserted into the collection in any order, but that also
   * enables the <code>PermissionCollection</code> implies method to be
   * implemented in an efficient (and consistent) manner.
   *
   * @return a new <code>PermissionCollection</code> suitable for storing
   * <code>FileProtocolPermission</code> objects.
   */
  public PermissionCollection newPermissionCollection() {
    return new FileProtocolPermissionCollection();
  } 

}

/**
 * A FileProtocolPermissionCollection stores a collection
 * of FileProtocol permissions. FileProtocolPermission objects
 * must be stored in a manner that allows them to be inserted in any
 * order, but enable the implies function to evaluate the implies
 * method in an efficient (and consistent) manner.
 *
 *
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.PermissionsImpl
 *
 * 
 *
 */

final class FileProtocolPermissionCollection extends PermissionCollection {
  private final Vector permissions = new Vector(6);

  /**
   * Create an empty FileProtocolPermissionCollection object.
   *
   */
  public FileProtocolPermissionCollection() {}

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
    if (! (permission instanceof FileProtocolPermission))
      throw new IllegalArgumentException("invalid permission: "+
                                         permission);
    if (isReadOnly()) {
      throw new SecurityException(
        "Cannot add a Permission to a readonly PermissionCollection");
    }

    FileProtocolPermission bp = (FileProtocolPermission) permission;
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
    if (! (permission instanceof FileProtocolPermission)) {
      return false;
    }

    FileProtocolPermission fp = (FileProtocolPermission) permission;

    int desired = fp.getActionMask();
    int effective = 0;
    int needed = desired;

    Enumeration search = permissions.elements();
    while (search.hasMoreElements()) {
      FileProtocolPermission p = (FileProtocolPermission)search.nextElement();

      int actions = p.getActionMask();
      if ((actions & needed) != 0 && p.impliesByPath(fp)) {
        effective |= actions;
        if ((effective & desired) == desired) {
          return true;
        }
        needed = (desired ^ effective);
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
