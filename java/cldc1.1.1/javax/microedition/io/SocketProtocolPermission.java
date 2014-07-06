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
 * This class represents access rights to connections via the "socket"
 * protocol.  A <code>SocketProtocolPermission</code> consists of a
 * URI string but no actions list.
 * <p>
 * The URI string specifies a socket stream connection.
 * It takes the following form:
 * <pre>
 * socket://[{host}][:{portrange}]
 * </pre>
 * If the <code>{host}</code> string is a DNS name, an asterisk may
 * appear in the leftmost position to indicate a wildcard match
 * (e.g., "*.sun.com").  The URI string "socket://*" matches all
 * "client mode" connections; the string "socket://" specifies a
 * server socket connection.
 * <p>
 * The <code>{portrange}</code> string takes the following form:
 * <pre>
 * portnumber | -portnumber | portnumber-[portnumber]
 * </pre>
 * A <code>{portrange}</code> specification of the form "N-"
 * (where N is a port number)
 * signifies all ports numbered N and above, while a specification of the
 * form "-N" indicates all ports numbered N and below.
 *
 * @see Connector#open
 * @see "javax.microedition.io.SocketConnection" in <a href="http://www.jcp.org/en/jsr/detail?id=271">MIDP 3.0 Specification</a>
 */

public final class SocketProtocolPermission extends GCFPermission {

  private static final PortRangeNormalizer normalizer = 
    new ServerModePortRangeNormalizer();

  /**
   * Creates a new <code>SocketProtocolPermission</code> with the
   * specified URI as its name. The URI string must conform to the
   * specification given above.
   *
   * @param uri the URI string.
   *
   * @throws IllegalArgumentException if <code>uri</code> is malformed.
   * @throws NullPointerException if <code>uri</code> is <code>null</code>.
   *
   * @see #getName
   */
  public SocketProtocolPermission(String uri) {
    super(uri, true, normalizer);

    if (!"socket".equals(getProtocol())) {
      throw new IllegalArgumentException("Expected socket protocol: " + uri);
    }

    checkHostPortOnly();
  }

  /**
   * Checks if this <code>SocketProtocolPermission</code> object "implies"
   * the specified permission.
   * <p>
   * More specifically, this method first ensures that all of the following
   * are true (and returns false if any of them are not):
   * <p>
   * <ul>
   * <li> <i>p</i> is an instanceof SocketProtocolPermission, and
   * <p>
   * <li> <i>p</i>'s port range is included in this port range.
   * </ul>
   * <p>
   * Then <code>implies</code> checks each of the following, in order,
   * and for each returns true if the stated condition is true:
   * <p>
   * <ul>
   * <li> If this object was initialized with a single IP address and
   * one of <i>p</i>'s IP addresses is equal to this object's IP address.
   * <p>
   * <li>If this object is a wildcard domain (such as *.sun.com), and
   * <i>p</i>'s canonical name (the name without any preceding *)
   * ends with this object's canonical host name. For example, *.sun.com
   * implies *.eng.sun.com..
   * <p>
   * <li>If this object was not initialized with a single IP address,
   * and one of this object's IP addresses equals one of <i>p</i>'s IP
   * addresses.
   * <p>
   * <li>If this canonical name equals <i>p</i>'s canonical name.
   * </ul>
   * <p>
   * If none of the above are true, <code>implies</code> returns false.
   * 
   * @param p the permission to check against.
   *
   * @return true if the specified permission is implied by this object,
   * false if not.
   */
  public boolean implies(Permission p) {
    if (!(p instanceof SocketProtocolPermission)) {
      return false;
    } 

    SocketProtocolPermission perm = (SocketProtocolPermission)p;

    return impliesByHost(perm) && impliesByPorts(perm);
  }

  /**
   * Checks two <code>SocketProtocolPermission</code> objects for equality.
   * 
   * @param obj the object we are testing for equality with this object.
   *
   * @return <code>true</code> if <code>obj</code> is a
   * <code>SocketProtocolPermission</code> and has the same URI string as
   * this <code>SocketProtocolPermission</code> object.
   */
  public boolean equals(Object obj) {
    if (!(obj instanceof SocketProtocolPermission)) {
      return false;
    }
    SocketProtocolPermission other = (SocketProtocolPermission)obj;
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
   * for <code>SocketProtocolPermission</code>.
   *
   * @return the empty string "".
   */
  public String getActions() {
    return "";
  }

  /**
   * Returns a new <code>PermissionCollection</code> for storing
   * <code>SocketProtocolPermission</code> objects.
   * <p>
   * <code>SocketProtocolPermission</code> objects must be stored in a
   * manner that allows
   * them to be inserted into the collection in any order, but that also
   * enables the <code>PermissionCollection</code> implies method to be
   * implemented in an efficient (and consistent) manner.
   *
   * @return a new <code>PermissionCollection</code> suitable for storing
   * <code>SocketProtocolPermission</code> objects.
   */
  public PermissionCollection newPermissionCollection() {
    return new GCFPermissionCollection(this.getClass());
  } 
}
