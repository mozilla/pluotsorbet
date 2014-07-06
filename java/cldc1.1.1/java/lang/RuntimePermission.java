/*
 * @(#)RuntimePermission.java	1.46 06/10/10
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
 *
 */

package java.lang;

import java.security.*;
import java.util.Enumeration;
import java.util.Hashtable;

/**
 * This class is for runtime permissions. A RuntimePermission
 * contains a name (also referred to as a "target name") but
 * no actions list; you either have the named permission
 * or you don't.
 *
 * <P>
 * The target name is the name of the runtime permission (see below). The
 * naming convention follows the  hierarchical property naming convention.
 * Also, an asterisk
 * may appear at the end of the name, following a ".", or by itself, to
 * signify a wildcard match. For example: "loadLibrary.*" or "*" is valid,
 * "*loadLibrary" or "a*b" is not valid.
 * <P>
 * The following table lists all the possible RuntimePermission target names,
 * and for each provides a description of what the permission allows
 * and a discussion of the risks of granting code the permission.
 * <P>
 *
 * <table border=1 cellpadding=5 summary="permission target name, 
 *  what the target allows,and associated risks">
 * <tr>
 * <th>Permission Target Name</th>
 * <th>What the Permission Allows</th>
 * <th>Risks of Allowing this Permission</th>
 * </tr>
 *
 * <tr>
 *   <td>createClassLoader</td>
 *   <td>Creation of a class loader</td>
 *   <td>This is an extremely dangerous permission to grant.
 * Malicious applications that can instantiate their own class
 * loaders could then load their own rogue classes into the system.
 * These newly loaded classes could be placed into any protection
 * domain by the class loader, thereby automatically granting the
 * classes the permissions for that domain.</td>
 * </tr>
 *
 * <tr>
 *   <td>getClassLoader</td>
 *   <td>Retrieval of a class loader (e.g., the class loader for the calling
 * class)</td>
 *   <td>This would grant an attacker permission to get the
 * class loader for a particular class. This is dangerous because
 * having access to a class's class loader allows the attacker to
 * load other classes available to that class loader. The attacker
 * would typically otherwise not have access to those classes.</td>
 * </tr>
 *
 * <tr>
 *   <td>setContextClassLoader</td>
 *   <td>Setting of the context class loader used by a thread</td>
 *   <td>The context class loader is used by system code and extensions
 * when they need to lookup resources that might not exist in the system
 * class loader. Granting setContextClassLoader permission would allow
 * code to change which context class loader is used
 * for a particular thread, including system threads.</td>
 * </tr>
 *
 * <tr>
 *   <td>setSecurityManager</td>
 *   <td>Setting of the security manager (possibly replacing an existing one)
 * </td>
 *   <td>The security manager is a class that allows 
 * applications to implement a security policy. Granting the setSecurityManager
 * permission would allow code to change which security manager is used by
 * installing a different, possibly less restrictive security manager,
 * thereby bypassing checks that would have been enforced by the original
 * security manager.</td>
 * </tr>
 *
 * <tr>
 *   <td>createSecurityManager</td>
 *   <td>Creation of a new security manager</td>
 *   <td>This gives code access to protected, sensitive methods that may
 * disclose information about other classes or the execution stack.</td>
 * </tr>
 *
 * <tr>
 *   <td>exitVM</td>
 *   <td>Halting of the Java Virtual Machine</td>
 *   <td>This allows an attacker to mount a denial-of-service attack
 * by automatically forcing the virtual machine to halt.
 * Note: The "exitVM" permission is automatically granted to all code
 * loaded from the application class path, thus enabling applications
 * to terminate themselves.</td>
 * </tr>
 *
 * <tr>
 *   <td>shutdownHooks</td>
 *   <td>Registration and cancellation of virtual-machine shutdown hooks</td>
 *   <td>This allows an attacker to register a malicious shutdown
 * hook that interferes with the clean shutdown of the virtual machine.</td>
 * </tr>
 *
 * <tr>
 *   <td>setFactory</td>
 *   <td>Setting of the socket factory used by ServerSocket or Socket,
 * or of the stream handler factory used by URL</td>
 *   <td>This allows code to set the actual implementation
 * for the socket, server socket, stream handler, or RMI socket factory.
 * NOTE: <B>java.net.ServerSocket, java.net.Socket</B> are found in J2ME 
 * CDC profiles such as J2ME Foundation Profile.
 * An attacker may set a faulty implementation which mangles the data
 * stream.</td>
 * </tr>
 *
 * <tr>
 *   <td>setIO</td>
 *   <td>Setting of System.out, System.in, and System.err</td>
 *   <td>This allows changing the value of the standard system streams.
 * An attacker may change System.in to monitor and
 * steal user input, or may set System.err to a "null" OutputSteam,
 * which would hide any error messages sent to System.err. </td>
 * </tr>
 *
 * <tr>
 *   <td>modifyThread</td>
 *   <td>Modification of threads, e.g., via calls to Thread <code>stop</code>,
 * <code>suspend</code>, <code>resume</code>, <code>setPriority</code>,
 * and <code>setName</code> methods</td>
 *   <td>This allows an attacker to start or suspend any thread
 * in the system.</td>
 * </tr>
 *
 * <tr>
 *   <td>stopThread</td>
 *   <td>Stopping of threads via calls to the Thread <code>stop</code>
 * method</td>
 *   <td>This allows code to stop any thread in the system provided that it is
 * already granted permission to access that thread.
 * This poses as a threat, because that code may corrupt the system by
 * killing existing threads.</td>
 * </tr>
 *
 * <tr>
 *   <td>modifyThreadGroup</td>
 *   <td>modification of thread groups, e.g., via calls to ThreadGroup
 * <code>destroy</code>, <code>getParent</code>, <code>resume</code>, 
 * <code>setDaemon</code>, <code>setMaxPriority</code>, <code>stop</code>, 
 * and <code>suspend</code> methods</td>
 *   <td>This allows an attacker to create thread groups and
 * set their run priority.</td>
 * </tr>
 *
 * <tr>
 *   <td>getProtectionDomain</td>
 *   <td>Retrieval of the ProtectionDomain for a class</td>
 *   <td>This allows code to obtain policy information
 * for a particular code source. While obtaining policy information
 * does not compromise the security of the system, it does give
 * attackers additional information, such as local file names for
 * example, to better aim an attack.</td>
 * </tr>
 *
 * <tr>
 *   <td>readFileDescriptor</td>
 *   <td>Reading of file descriptors</td>
 *   <td>This would allow code to read the particular file associated
 *       with the file descriptor read. This is dangerous if the file
 *       contains confidential data.</td>
 * </tr>
 *
 * <tr>
 *   <td>writeFileDescriptor</td>
 *   <td>Writing to file descriptors</td>
 *   <td>This allows code to write to a particular file associated
 *       with the descriptor. This is dangerous because it may allow
 *       malicious code to plant viruses or at the very least, fill up
 *       your entire disk.</td>
 * </tr>
 *
 * <tr>
 *   <td>loadLibrary.{library name}</td>
 *   <td>Dynamic linking of the specified library</td>
 *   <td>It is dangerous to allow an applet permission to load native code
 * libraries, because the Java security architecture is not designed to and
 * does not prevent malicious behavior at the level of native code.</td>
 * </tr>
 *
 * <tr>
 *   <td>accessClassInPackage.{package name}</td>
 *   <td>Access to the specified package via a class loader's
 * <code>loadClass</code> method when that class loader calls
 * the SecurityManager <code>checkPackageAcesss</code> method</td>
 *   <td>This gives code access to classes in packages
 * to which it normally does not have access. Malicious code
 * may use these classes to help in its attempt to compromise
 * security in the system.</td>
 * </tr>
 *
 * <tr>
 *   <td>defineClassInPackage.{package name}</td>
 *   <td>Definition of classes in the specified package, via a class
 * loader's <code>defineClass</code> method when that class loader calls
 * the SecurityManager <code>checkPackageDefinition</code> method.</td>
 *   <td>This grants code permission to define a class
 * in a particular package. This is dangerous because malicious
 * code with this permission may define rogue classes in
 * trusted packages like <code>java.security</code> or <code>java.lang</code>,
 * for example.</td>
 * </tr>
 *
 * <tr>
 *   <td>accessDeclaredMembers</td>
 *   <td>Access to the declared members of a class</td>
 *   <td>This grants code permission to query a class for its public,
 * protected, default (package) access, and private fields and/or
 * methods. Although the code would have
 * access to the private and protected field and method names, it would not
 * have access to the private/protected field data and would not be able
 * to invoke any private methods. Nevertheless, malicious code
 * may use this information to better aim an attack.
 * Additionally, it may invoke any public methods and/or access public fields
 * in the class.  This could be dangerous if
 * the code would normally not be able to invoke those methods and/or
 * access the fields  because
 * it can't cast the object to the class/interface with those methods
 * and fields.
</td>
 * </tr>
 * <tr>
 *   <td>queuePrintJob</td>
 *   <td>Initiation of a print job request</td>
 *   <td>This could print sensitive information to a printer,
 * or simply waste paper.</td>
 * </tr>
 *
 * </table>
 *
 * @see java.security.BasicPermission
 * @see java.security.Permission
 * @see java.security.Permissions
 * @see java.security.PermissionCollection
 * @see java.lang.SecurityManager
 *
 * @version 1.37 00/02/02
 *
 * @author Marianne Mueller
 * @author Roland Schemers
 */

public final class RuntimePermission extends BasicPermission {

    /**
     * Creates a new RuntimePermission with the specified name.
     * The name is the symbolic name of the RuntimePermission, such as
     * "exit", "setFactory", etc. An asterisk
     * may appear at the end of the name, following a ".", or by itself, to
     * signify a wildcard match.
     *
     * @param name the name of the RuntimePermission.
     */

    public RuntimePermission(String name)
    {
	super(name);
    }

    /**
     * Creates a new RuntimePermission object with the specified name.
     * The name is the symbolic name of the RuntimePermission, and the
     * actions String is currently unused and should be null.
     *
     * @param name the name of the RuntimePermission.
     * @param actions should be null.
     */

    public RuntimePermission(String name, String actions)
    {
	super(name, actions);
    }
}
