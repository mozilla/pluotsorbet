/*
 * @(#)AccessController.java	1.58 06/10/10
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
 
package java.security;

/** 
 * <p> The AccessController class is used for access control operations
 * and decisions.
 * 
 * <p> More specifically, the AccessController class is used for 
 * three purposes:
 * 
 * <ul>
 * <li> to decide whether an access to a critical system
 * resource is to be allowed or denied, based on the security policy
 * currently in effect,<p> 
 * <li>to mark code as being "privileged", thus affecting subsequent
 * access determinations, and<p>
 * <li>to obtain a "snapshot" of the current calling context so
 * access-control decisions from a different context can be made with
 * respect to the saved context. </ul>
 * 
 * <p> The {@link #checkPermission(Permission) checkPermission} method
 * determines whether the access request indicated by a specified
 * permission should be granted or denied. A sample call appears
 * below. In this example, <code>checkPermission</code> will determine 
 * whether or not to grant "read" access to the file named "testFile" in 
 * the "/temp" directory.
 * 
 * <pre>
 * 
 *    FilePermission perm = new FilePermission("/temp/testFile", "read");
 *    AccessController.checkPermission(perm);
 * 
 * </pre>
 *
 * <p> If a requested access is allowed, 
 * <code>checkPermission</code> returns quietly. If denied, an 
 * AccessControlException is
 * thrown. AccessControlException can also be thrown if the requested
 * permission is of an incorrect type or contains an invalid value.
 * Such information is given whenever possible.
 * 
 * Suppose the current thread traversed m callers, in the order of caller 1 
 * to caller 2 to caller m. Then caller m invoked the 
 * <code>checkPermission</code> method.
 * The <code>checkPermission </code>method determines whether access 
 * is granted or denied based on the following algorithm:
 * 
 * <pre>
 * i = m;
 * 
 * while (i > 0) {
 * 
 *      if (caller i's domain does not have the permission)
 *              throw AccessControlException
 * 
 *      else if (caller i is marked as privileged) {
 *              if (a context was specified in the call to doPrivileged) 
 *                 context.checkPermission(permission)
 *              return;
 *      }
 *      i = i - 1;
 * };
 *
 *    // Next, check the context inherited when
 *    // the thread was created. Whenever a new thread is created, the
 *    // AccessControlContext at that time is
 *    // stored and associated with the new thread, as the "inherited"
 *    // context.
 * 
 * inheritedContext.checkPermission(permission);
 * </pre>
 * 
 * <p> A caller can be marked as being "privileged" 
 * (see {@link #doPrivileged(PrivilegedAction) doPrivileged} and below). 
 * When making access control decisions, the <code>checkPermission</code>
 * method stops checking if it reaches a caller that 
 * was marked as "privileged" via a <code>doPrivileged</code> 
 * call without a context argument (see below for information about a
 * context argument). If that caller's domain has the
 * specified permission, no further checking is done and 
 * <code>checkPermission</code>
 * returns quietly, indicating that the requested access is allowed.
 * If that domain does not have the specified permission, an exception
 * is thrown, as usual.
 * 
 * <p> The normal use of the "privileged" feature is as follows. If you
 * don't need to return a value from within the "privileged" block, do 
 * the following:
 *
 * <pre>
 *   somemethod() {
 *        ...normal code here...
 *        AccessController.doPrivileged(new PrivilegedAction() {
 *            public Object run() {
 *                // privileged code goes here, for example:
 *                System.loadLibrary("awt");
 *                return null; // nothing to return
 *            }
 *        });
  *       ...normal code here...
 *  }
 * </pre>
 *
 * <p>
 * PrivilegedAction is an interface with a single method, named
 * <code>run</code>, that returns an Object.
 * The above example shows creation of an implementation
 * of that interface; a concrete implementation of the
 * <code>run</code> method is supplied.
 * When the call to <code>doPrivileged</code> is made, an 
 * instance of the PrivilegedAction implementation is passed
 * to it. The <code>doPrivileged</code> method calls the
 * <code>run</code> method from the PrivilegedAction 
 * implementation after enabling privileges, and returns the 
 * <code>run</code> method's return value as the 
 * <code>doPrivileged</code> return value (which is
 * ignored in this example).
 *
 * <p> If you need to return a value, you can do something like the following:
 *
 * <pre>
 *   somemethod() {
 *        ...normal code here...
 *        String user = (String) AccessController.doPrivileged(
 *          new PrivilegedAction() {
 *            public Object run() {
 *                return System.getProperty("user.name");
 *            }
 *          }
 *        );
 *        ...normal code here...
 *  }
 * </pre>
 *
 * <p>If the action performed in your <code>run</code> method could
 * throw a "checked" exception (those listed in the <code>throws</code> clause
 * of a method), then you need to use the 
 * <code>PrivilegedExceptionAction</code> interface instead of the
 * <code>PrivilegedAction</code> interface:
 * 
 * <pre>
 *   somemethod() throws FileNotFoundException {
 *        ...normal code here...
 *      try {
 *        FileInputStream fis = (FileInputStream) AccessController.doPrivileged(
 *          new PrivilegedExceptionAction() {
 *            public Object run() throws FileNotFoundException {
 *                return new FileInputStream("someFile");
 *            }
 *          }
 *        );
 *      } catch (PrivilegedActionException e) {
 *        // e.getException() should be an instance of FileNotFoundException,
 *        // as only "checked" exceptions will be "wrapped" in a
 *        // <code>PrivilegedActionException</code>.
 *        throw (FileNotFoundException) e.getException();
 *      }
 *        ...normal code here...
 *  }
 * </pre>
 * 
 * <p> Be *very* careful in your use of the "privileged" construct, and 
 * always remember to make the privileged code section as small as possible.
 * 
 * <p> Note that <code>checkPermission</code> always performs security checks
 * within the context of the currently executing thread.
 * Sometimes a security check that should be made within a given context
 * will actually need to be done from within a
 * <i>different</i> context (for example, from within a worker thread).
 * The {@link #getContext() getContext} method and 
 * AccessControlContext class are provided 
 * for this situation. The <code>getContext</code> method takes a "snapshot"
 * of the current calling context, and places
 * it in an AccessControlContext object, which it returns. A sample call is
 * the following:
 * 
 * <pre>
 * 
 *   AccessControlContext acc = AccessController.getContext()
 * 
 * </pre>
 * 
 * <p>
 * AccessControlContext itself has a <code>checkPermission</code> method
 * that makes access decisions based on the context <i>it</i> encapsulates,
 * rather than that of the current execution thread.
 * Code within a different context can thus call that method on the
 * previously-saved AccessControlContext object. A sample call is the
 * following:
 * 
 * <pre>
 * 
 *   acc.checkPermission(permission)
 * 
 * </pre> 
 *
 * <p> There are also times where you don't know a priori which permissions
 * to check the context against. In these cases you can use the
 * doPrivileged method that takes a context:
 * 
 * <pre>
 *   somemethod() {
 *         AccessController.doPrivileged(new PrivilegedAction() {
 *              public Object run() {
 *                 // Code goes here. Any permission checks within this
 *                 // run method will require that the intersection of the
 *                 // callers protection domain and the snapshot's
 *                 // context have the desired permission.
 *              }
 *         }, acc);
 *         ...normal code here...
 *   }
 * </pre>
 * 
 * @see AccessControlContext
 *
 * @version 1.48 00/05/03
 * @author Li Gong 
 * @author Roland Schemers
 */

public final class AccessController {

    /** 
     * Don't allow anyone to instantiate an AccessController
     */
    private AccessController() { }


    /**
     * Performs the specified <code>PrivilegedAction</code> with privileges
     * enabled and restricted by the specified <code>AccessControlContext</code>.
     * The action is performed with the intersection of the permissions
     * possessed by the caller's protection domain, and those possessed
     * by the domains represented by the specified
     * <code>AccessControlContext</code>.
     * <p>
     * If the action's <code>run</code> method throws an (unchecked) exception,
     * it will propagate through this method.
     *
     * @param action the action to be performed.
     * @param context an <i>access control context</i> representing the
     *		      restriction to be applied to the caller's domain's
     *		      privileges before performing the specified action.
     * @return the value returned by the action's <code>run</code> method.
     * @see #doPrivileged(PrivilegedAction)
     * @see #doPrivileged(PrivilegedExceptionAction,AccessControlContext)
     */
/*    public static Object doPrivileged(PrivilegedAction action,
				      AccessControlContext context)
    {
	return action.run();
    }
*/
    /**
     * Performs the specified <code>PrivilegedExceptionAction</code> with
     * privileges enabled.  The action is performed with <i>all</i> of the 
     * permissions possessed by the caller's protection domain.
     * <p>
     * If the action's <code>run</code> method throws an <i>unchecked</i>
     * exception, it will propagate through this method.
     *
     * @param action the action to be performed
     * @return the value returned by the action's <code>run</code> method
     * @throws PrivilegedActionException if the specified action's
     *         <code>run</code> method threw a <i>checked</i> exception
     * @see #doPrivileged(PrivilegedAction)
     * @see #doPrivileged(PrivilegedExceptionAction,AccessControlContext)
     */
/*
    public static Object doPrivileged(PrivilegedExceptionAction action)
	 throws PrivilegedActionException
    {
	return doPrivileged(action, null);
    }
*/

    /**
     * Performs the specified <code>PrivilegedExceptionAction</code> with 
     * privileges enabled and restricted by the specified
     * <code>AccessControlContext</code>.  The action is performed with the
     * intersection of the the permissions possessed by the caller's
     * protection domain, and those possessed by the domains represented by the
     * specified <code>AccessControlContext</code>.
     * <p>
     * If the action's <code>run</code> method throws an <i>unchecked</i>
     * exception, it will propagate through this method.
     *
     * @param action the action to be performed
     * @param context an <i>access control context</i> representing the
     *		      restriction to be applied to the caller's domain's
     *		      privileges before performing the specified action
     * @return the value returned by the action's <code>run</code> method
     * @throws PrivilegedActionException if the specified action's
     *         <code>run</code> method
     *	       threw a <i>checked</i> exception
     * @see #doPrivileged(PrivilegedAction)
     * @see #doPrivileged(PrivilegedExceptionAction,AccessControlContext)
     */
/*    public static Object doPrivileged(PrivilegedExceptionAction action,
				      AccessControlContext context)
    {
	try {
	    return action.run();
	} catch (RuntimeException e) {
	    // mimic JDK behavior
	    throw e;
	} catch (Exception e) {
	    // slight deviation from JDK behavior, but not spec
	    // They always wrap, for some reason (bug?)
	    throw new PrivilegedActionException(e);
	} catch (Throwable e) {
	    throw CVM.throwLocalException(e);
	}
    }*/

    /**
     * Returns the AccessControl context. i.e., it gets 
     * the protection domains of all the callers on the stack,
     * starting at the first class with a non-null 
     * ProtectionDomain. 
     *
     * @return the access control context based on the current stack or
     *         null if there was only privileged system code.
     * 
     * 
     */
/*
    private static AccessControlContext sysACC =
	new AccessControlContext(null, false, null);

    private static AccessControlContext privACC =
	new AccessControlContext(null, true, null);

    private static native void fillInContext(ProtectionDomain[] ctx, int n);
    private static native int computeContext(boolean[] isPrivilegedRef,
					     AccessControlContext[] ctxRef);
*/
/*    private static AccessControlContext getStackAccessControlContext() {
	AccessControlContext[] privilegedContextRef =
	    new AccessControlContext[1];
	boolean[] isPrivilegedRef = new boolean[1];

	int count = computeContext(isPrivilegedRef, privilegedContextRef);

	boolean isPrivileged = isPrivilegedRef[0];
	AccessControlContext privilegedContext = privilegedContextRef[0];

        // either all the domains on the stack were system domains, or
        // we had a privileged system domain
        if (count == 0) {
            if (isPrivileged && privilegedContext == null) {
		return null;
            } else if (privilegedContext != null) {
		return new AccessControlContext(null, isPrivileged,
		    privilegedContext);
	    } else if (!isPrivileged) {
		return sysACC;
	    } else {
		throw new InternalError();
	    }
        }

	ProtectionDomain[] ctx = new ProtectionDomain[count];

	fillInContext(ctx, count);

	return new AccessControlContext(ctx, isPrivileged, privilegedContext);
    }*/

    /**
     * Returns the "inherited" AccessControl context. This is the context
     * that existed when the thread was created. Package private so 
     * AccessControlContext can use it.
     */

//    static native AccessControlContext getInheritedAccessControlContext();

    /** 
     * This method takes a "snapshot" of the current calling context, which
     * includes the current Thread's inherited AccessControlContext,
     * and places it in an AccessControlContext object. This context may then
     * be checked at a later point, possibly in another thread.
     *
     * @see AccessControlContext
     *
     * @return the AccessControlContext based on the current context.
     */

  /*  public static AccessControlContext getContext()
    {

	AccessControlContext acc = getStackAccessControlContext();
	if (acc == null) {
	    // all we had was privileged system code. We don't want
	    // to return null though, so we construct a real ACC.
	    return privACC;
	} else {
	    return acc.optimize();
	}
    }    */

    /** 
     * Determines whether the access request indicated by the
     * specified permission should be allowed or denied, based on
     * the security policy currently in effect. 
     * This method quietly returns if the access request
     * is permitted, or throws a suitable AccessControlException otherwise. 
     *
     * @param perm the requested permission.
     * 
     * @exception AccessControlException if the specified permission
     * is not permitted, based on the current security policy.
     */

    public static void checkPermission(Permission perm)
		 throws AccessControlException 
    {
/*
	//System.err.println("checkPermission "+perm);
	//Thread.currentThread().dumpStack();

	AccessControlContext stack = getStackAccessControlContext();
	// if context is null, we had privileged system code on the stack.
	if (stack == null) {
	    Debug debug = AccessControlContext.getDebug();
	    if (debug != null) {
		if (Debug.isOn("stack"))
		    Thread.currentThread().dumpStack();
		if (Debug.isOn("domain")) {
		    debug.println("domain (context is null)");
		}
		debug.println("access allowed "+perm);
	    }
	    return;
	}

	AccessControlContext acc = stack.optimize();
	acc.checkPermission(perm);
*/
    }
}
