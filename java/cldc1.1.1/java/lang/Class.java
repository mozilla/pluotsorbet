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

package java.lang;

/**
 * Instances of the class <code>Class</code> represent classes and interfaces
 * in a running Java application.  Every array also belongs to a class that is
 * reflected as a <code>Class</code> object that is shared by all arrays with
 * the same element type and number of dimensions.
 *
 * <p> <code>Class</code> has no public constructor. Instead <code>Class</code>
 * objects are constructed automatically by the Java Virtual Machine as classes
 * are loaded.
 *
 * <p> The following example uses a <code>Class</code> object to print the
 * class name of an object:
 *
 * <p> <blockquote><pre>
 *     void printClassName(Object obj) {
 *         System.out.println("The class of " + obj +
 *                            " is " + obj.getClass().getName());
 *     }
 * </pre></blockquote>
 *
 * @version 12/17/01 (CLDC 1.1)
 * @since   JDK1.0, CLDC 1.0
 */
public final
class Class {

    /*
     * Constructor. Only the Java Virtual Machine creates Class
     * objects.
     */
    private Class() {}

    /**
     * Converts the object to a string. The string representation is the
     * string "class" or "interface", followed by a space, and then by the
     * fully qualified name of the class in the format returned by
     * <code>getName</code>.  If this <code>Class</code> object represents a
     * primitive type, this method returns the name of the primitive type.  If
     * this <code>Class</code> object represents void this method returns
     * "void".
     *
     * @return a string representation of this class object.
     */
    public String toString() {
        return (isInterface() ? "interface " :  "class ") + getName();
    }

    /**
     * Returns the <code>Class</code> object associated with the class
     * with the given string name.  Given the fully-qualified name for
     * a class or interface, this method attempts to locate, load and
     * link the class.
     * <p>
     * For example, the following code fragment returns the runtime
     * <code>Class</code> descriptor for the class named
     * <code>java.lang.Thread</code>:
     * <ul><code>
     *   Class&nbsp;t&nbsp;= Class.forName("java.lang.Thread")
     * </code></ul>
     *
     * @param      className   the fully qualified name of the desired class.
     * @return     the <code>Class</code> object for the class with the
     *             specified name.
     * @exception  ClassNotFoundException  if the class could not be found.
     * @exception  Error  if the function fails for any other reason.
     * @since      JDK1.0
     */
    public static native Class forName(String className)
        throws ClassNotFoundException;

    /**
     * Creates a new instance of a class.
     *
     * @return     a newly allocated instance of the class represented by this
     *             object. This is done exactly as if by a <code>new</code>
     *             expression with an empty argument list.
     * @exception  IllegalAccessException  if the class or initializer is
     *               not accessible.
     * @exception  InstantiationException  if an application tries to
     *               instantiate an abstract class or an interface, or if the
     *               instantiation fails for some other reason.
     * @since     JDK1.0
     */
    public native Object newInstance()
        throws InstantiationException, IllegalAccessException;

    /**
     * Determines if the specified <code>Object</code> is assignment-compatible
     * with the object represented by this <code>Class</code>.  This method is
     * the dynamic equivalent of the Java language <code>instanceof</code>
     * operator. The method returns <code>true</code> if the specified
     * <code>Object</code> argument is non-null and can be cast to the
     * reference type represented by this <code>Class</code> object without
     * raising a <code>ClassCastException.</code> It returns <code>false</code>
     * otherwise.
     *
     * <p> Specifically, if this <code>Class</code> object represents a
     * declared class, this method returns <code>true</code> if the specified
     * <code>Object</code> argument is an instance of the represented class (or
     * of any of its subclasses); it returns <code>false</code> otherwise. If
     * this <code>Class</code> object represents an array class, this method
     * returns <code>true</code> if the specified <code>Object</code> argument
     * can be converted to an object of the array class by an identity
     * conversion or by a widening reference conversion; it returns
     * <code>false</code> otherwise. If this <code>Class</code> object
     * represents an interface, this method returns <code>true</code> if the
     * class or any superclass of the specified <code>Object</code> argument
     * implements this interface; it returns <code>false</code> otherwise. If
     * this <code>Class</code> object represents a primitive type, this method
     * returns <code>false</code>.
     *
     * @param   obj the object to check
     * @return  true if <code>obj</code> is an instance of this class
     *
     * @since JDK1.1
     */
    public native boolean isInstance(Object obj);

    /**
     * Determines if the class or interface represented by this
     * <code>Class</code> object is either the same as, or is a superclass or
     * superinterface of, the class or interface represented by the specified
     * <code>Class</code> parameter. It returns <code>true</code> if so;
     * otherwise it returns <code>false</code>. If this <code>Class</code>
     * object represents a primitive type, this method returns
     * <code>true</code> if the specified <code>Class</code> parameter is
     * exactly this <code>Class</code> object; otherwise it returns
     * <code>false</code>.
     *
     * <p> Specifically, this method tests whether the type represented by the
     * specified <code>Class</code> parameter can be converted to the type
     * represented by this <code>Class</code> object via an identity conversion
     * or via a widening reference conversion. See <em>The Java Language
     * Specification</em>, sections 5.1.1 and 5.1.4 , for details.
     *
     * @param cls the <code>Class</code> object to be checked
     * @return the <code>boolean</code> value indicating whether objects of the
     * type <code>cls</code> can be assigned to objects of this class
     * @exception NullPointerException if the specified Class parameter is
     *            null.
     * @since JDK1.1
     */
    public native boolean isAssignableFrom(Class cls);

    /**
     * Determines if the specified <code>Class</code> object represents an
     * interface type.
     *
     * @return  <code>true</code> if this object represents an interface;
     *          <code>false</code> otherwise.
     */
    public native boolean isInterface();

    /**
     * Determines if this <code>Class</code> object represents an array class.
     *
     * @return  <code>true</code> if this object represents an array class;
     *          <code>false</code> otherwise.
     * @since   JDK1.1
     */
    public native boolean isArray();

    /**
     * Returns the fully-qualified name of the entity (class, interface, array
     * class, primitive type, or void) represented by this <code>Class</code>
     * object, as a <code>String</code>.
     *
     * <p> If this <code>Class</code> object represents a class of arrays, then
     * the internal form of the name consists of the name of the element type
     * in Java signature format, preceded by one or more "<tt>[</tt>"
     * characters representing the depth of array nesting. Thus:
     *
     * <blockquote><pre>
     * (new Object[3]).getClass().getName()
     * </pre></blockquote>
     *
     * returns "<code>[Ljava.lang.Object;</code>" and:
     *
     * <blockquote><pre>
     * (new int[3][4][5][6][7][8][9]).getClass().getName()
     * </pre></blockquote>
     *
     * returns "<code>[[[[[[[I</code>". The encoding of element type names
     * is as follows:
     *
     * <blockquote><pre>
     * B            byte
     * C            char
     * D            double
     * F            float
     * I            int
     * J            long
     * L<i>classname;</i>  class or interface
     * S            short
     * Z            boolean
     * </pre></blockquote>
     *
     * The class or interface name <tt><i>classname</i></tt> is given in fully
     * qualified form as shown in the example above.
     *
     * @return  the fully qualified name of the class or interface
     *          represented by this object.
     */
    public native String getName();

    /**
     * Finds a resource with a given name in the application's
     * JAR file. This method returns
     * <code>null</code> if no resource with this name is found
     * in the application's JAR file.
     * <p>
     * The resource names can be represented in two
     * different formats: absolute or relative.
     * <p>
     * Absolute format:
     * <ul><code>/packagePathName/resourceName</code></ul>
     * <p>
     * Relative format:
     * <ul><code>resourceName</code></ul>
     * <p>
     * In the absolute format, the programmer provides a fully
     * qualified name that includes both the full path and the
     * name of the resource inside the JAR file.  In the path names,
     * the character "/" is used as the separator.
     * <p>
     * In the relative format, the programmer provides only
     * the name of the actual resource.  Relative names are
     * converted to absolute names by the system by prepending
     * the resource name with the fully qualified package name
     * of class upon which the <code>getResourceAsStream</code>
     * method was called.
     *
     * @param name  name of the desired resource
     * @return      a <code>java.io.InputStream</code> object.
     */
    public java.io.InputStream getResourceAsStream(String name) {
        try {
            if (name.length() > 0 && name.charAt(0) == '/') {
                /* Absolute format */
                name = name.substring(1);
            } else {
                /* Relative format */
                String className = this.getName();
                int dotIndex = className.lastIndexOf('.');
                if (dotIndex >= 0) {
                    name = className.substring(0, dotIndex + 1).replace('.', '/')
                           + name;
                }
            }
            return new com.sun.cldc.io.ResourceInputStream(name);
        } catch (java.io.IOException x) {
            return null;
        }
    }

    /*
     * This private function is used during virtual machine initialization.
     * The user does not normally see this function.
     */
//    private static void runCustomCode() {}

    /* The code below is specific to this VM */

    /**
     * Returns the <code>Class</code> representing the superclass of the entity
     * (class, interface, primitive type or void) represented by this
     * <code>Class</code>.  If this <code>Class</code> represents either the
     * <code>Object</code> class, an interface, a primitive type, or void, then
     * null is returned.  If this object represents an array class then the
     * <code>Class</code> object representing the <code>Object</code> class is
     * returned.
     *
     * Note that this method is not supported by CLDC.
     * We have made the method private, since it is
     * needed by our implementation.
     *
     * @return the superclass of the class represented by this object.
     */
    private native Class getSuperclass();

    /*
     * This private variable is used by the VM.
     * Users never see it.
     */
    private transient Object vmClass;

    private int    status;
    private Thread thread;

    private static final int IN_PROGRESS = 1;
    private static final int VERIFIED    = 2;
    private static final int INITIALIZED = 4;
    private static final int ERROR       = 8;

    // Native for invoking <clinit>
    private native void invoke_clinit();

    /**
     * Initialization at step 9:
     * If ENABLE_ISOLATES == false
     * Remove the <clinit> method after the class is initialized.
     * If ENABLE_ISOLATES == true, clear class initialization
     * barrier.
     */
    private native void init9();

    private native void invoke_verify();

    /*
     * Implements the 11 step program detailed in Java Language Specification
     * 12.4.2
     */
    void initialize() throws Throwable {
        // Step 1
        synchronized (this) {
            //  Step 2
            while ((status & IN_PROGRESS) != 0 && thread != Thread.currentThread()) {
                try{
                    wait();
                } catch (InterruptedException e) {
                }
            }

            // Step 3
            if ((status & IN_PROGRESS) != 0 && thread == Thread.currentThread()) {
                return;
            }

            // Step 4
            if ((status & INITIALIZED) != 0) {
                return;
            }

            // Step 5
            if (status == ERROR) {
                throw new NoClassDefFoundError(getName());
            }
            /* Note: CLDC 1.0 does not have NoClassDefFoundError class */

            // Step 6
            status |= IN_PROGRESS;
            thread = Thread.currentThread();
        }

        try {
            // Step 7
            invoke_verify();
            Class s = getSuperclass();
            if (s != null && (s.status & INITIALIZED) == 0) {
                // The test of s.status is not part of the spec, but
                // it saves us doing a lot of work in the most common
                // case.
                s.initialize();
            }

            // Step 8
            invoke_clinit();

            // Step 9
            synchronized (this) {
                status &= ~IN_PROGRESS;
                status |= INITIALIZED;
                thread = null;
                init9();
                notifyAll();
            }
        } catch(Throwable e) {
            // Step 10 and 11
            // CR 6224346, The cldc_vm threading mechanism is such that
            // we can just jam these values in without fear of another
            // thread doing the same since only this thread can be
            // executing the initialize() method and the scheduler is
            // non-preemptive.  We do this here in case the monitorenter
            // fails due to OOME because some other thread holds the lock,
            // memory is low and we need to allocate a ConditionDesc to
            // wait for the lock.
            status = ERROR;
            thread = null;
            synchronized (this) {
                notifyAll();
                throwError(e);
            }
        }
    }

    private Error throwError(Throwable e) throws Error {
        throw (e instanceof Error) ? (Error)e
            : new Error("Static initializer: " + e.getClass().getName() + 
                        ", " + e.getMessage());
    }
}
