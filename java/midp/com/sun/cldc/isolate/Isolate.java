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

package com.sun.cldc.isolate;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;
import java.io.IOException;
import com.sun.cldchi.jvm.JVM;

/**
 * <blockquote>
 * <hr>
 *     Last modified: 05/03/31 13:22:49. <p>
 *     Note: this document is still a draft. Details in the API are
 *     subject to change. <p>
 * <hr>
 * </blockquote>
 * The <code>Isolate</code> class provides the means of creating and managing
 * isolated computations and arranging for their communication with each
 * other.
 * <p>
 * <h3>Terminology</h3>
 *
 * Each isolated computation is called a <b>Task</b>. An <b>Isolate
 * object</b> is a Java representation of the task. Multiple
 * Isolate objects may be created to represent the same task. Where
 * the context is clear, the words "task", "isolate" and "isolate
 * object" may be used interchangeably. When a distinction needs to be
 * made, the word "task" is used to describe the underlying
 * computation, and the word "isolate" is used to describe the Java
 * object(s) that represent the computation. <p>
 *
 * When two Isolate objects represent the same task, they are said to
 * be <b>equivalent</b> to each other. Equivalent Isolate objects are
 * created to avoid the sharing of Isolate objects across task
 * boundaries. For example, in the following program, task A launches
 * task B using the object <code>b1</code>, and task B gets a
 * reference to itself using the object
 * <code>b2</code>. <code>b1</code> and <code>b2</code> are two
 * distinct objects: <p>
 *
 *<blockquote><pre>
 *class TaskA {
 *    void launchB() {
 *        Isolate <b>b1</b> = new Isolate("TaskB", ....);
 *        b1.start();
 *    }
 *}
 *class TaskB {
 *    public static void main(String args[]) {
 *        Isolate <b>b2</b> = Isolate.currentIsolate();
 *    }
 *}</pre></blockquote>
 *
 * <h3>Degree of Isolation</h3>
 *
 * Tasks in the CLDC environment are isolated in the following sense:
 * <ul>
 *     <li> Each task has a separate namespace for loading Java classes.
 *     <li> Each task has a separate set of static variables for Java classes
 *     <li> A <code>synchronized static</code> method uses a different 
 *          monitor object inside each task.
 *     <li> Typically no Java objects are shared across task boundaries.
 *          (see <a href=#classdoc_sharing>Object Sharing</a> below for
 *          exceptions).
 *     <li> Resource quotas are controlled by the runtime environment to
 *          prevent tasks to use excessive amount of resources. The
 *          implementation currently supports resource control for 
 *          memory or CPU cycles.
 * </ul>         
 *
 * <a name="classdoc_class_path"><h3>Class path</h3></a>
 *
 * <p>Part of the definition of an isolate is its
 * classpath, where the basic classes for the isolate are found.
 * The CLDC runtime searches for classes in three sets of
 * locations: 
 * <ul>
 *     <li>The romized system classes.
 *     <li>The isolate system class path.
 *     <li>The isolate application class path.
 * </ul>
 *
 * <p><i>System</i> and <i>application</i> class paths are specified separately
 * for an isolate when it is created. Classes on system and application class
 * paths have different access rights: 
 * <ul>
 *   <li>Only classes loaded from the system class path can access 
 *       hidden classes.
 *   <li>Only classes on the system class path can be loaded to restricted
 *       packages.
 * </ul>
 * For the definition of hidden and restricted packages, see
 * doc/misc/Romizer.html.
 *
 * <p>When an isolate requests a class, the class is first looked up the
 * romized system classes, then searched in the isolate system class path,
 * and then searched in the isolate application class path.
 *
 * <p>User application classes should be put on the application class path,
 * system class path can contain only trusted system classes.
 *
 * <p>WARNING: UNTRUSTED USER APPLICATION CLASSES MUST NEVER BE PUT ON THE
 * SYSTEM CLASS PATH, AS IT GRANTS THEM ACCESS TO SYSTEM INTERNALS AND BREAKS
 * SYSTEM SECURITY.
 *
 * <a name="classdoc_sharing"><h3>Object Sharing</h3></a>
 *
 * The Isolate API in CLDC does not support the sharing of arbitrary
 * Java object across Isolate boundaries. The only exception is String
 * objects: String objects may be passed as arguments from a parent
 * isolate to a child isolate's <code>main()</code> method. Such
 * Strings are passed by reference instead of by value in order to
 * conserve resource. Also, interned Strings (such as literal Strings
 * that appear inside Java source code) may be shared across Isolate
 * boundaries. <p>
 *
 * Even though String objects may be shared across isolates, different
 * isolates should not attempt to coordinate their activities by
 * synchronizing on these Strings. Specifically, an interned Strings cannot
 * be synchronized across isolate boundaries because it uses a different
 * monitor object in each Isolate. <p>
 *
 * <h3>Inter-Isolate Communication</h3>
 *
 * Isolates may need to communicate with each to coordinate their
 * activities. The recommended method of inter-isolate communication 
 * is a <b>native event queue</b>. On many CLDC/MIDP environments a native 
 * event queue already exists. Such event queues can be extended for
 * one isolate to send events to another isolate. <p>
 *
 * Some CLDC/MIDP implementors may be tempted to use native code to
 * pass shared objects from one isolate to another, and use
 * traditional Java object-level synchronization to perform
 * inter-isolate communication. In our experience this could easily
 * lead to inter-isolate deadlock that could be exploited by
 * downloaded malicious Midlets. For example, if a shared object is
 * used to synchronize screen painting, a malicious Midlet may stop
 * other Midlets from painting by not returning from its
 * <code>paint()</code> method. <p>
 *
 * In our experience, with shared objects, it would take significant
 * effort to design a system that can prevent such attacks. In
 * contrast, event queues are much more easily understood to design
 * a safe environment. Thus, we strongly recommend against using
 * shared objects for inter-isolate communication.<p>
 * 
 * The following code is an example of how an isolate can create other
 * isolates:
 *
 *<blockquote><pre>import com.sun.cldc.isolate.*;
 *
 *class HelloWorld {
 * 
 *    // Usage: cldc_vm -classpath <XX> HelloWorld HelloWorld2 <classpath>
 *    public static void main(String [] argv) {
 *        System.out.println("HelloWorld");
 *        for (int i = 0; i < 6; i++) {
 *            try {
 *                // pass i as an argument to the isolate just for fun 
 *               String[] isoArgs = {Integer.toString(i)};
 *                Isolate iso = new Isolate(argv[0], isoArgs);
 *                iso.start();
 *             } catch (Exception e) {
 *                System.out.println("caught exception " + e);
 *                e.printStackTrace();
 *            }
 *            System.out.println("HelloWorld: Iso " + i + " started.");
 *
 *        }
 *    }
 *}
 *
 *
 *class HelloWorld2 {
 *    static String st = "HelloWorld2[";
 *
 *    public static void main(String [] argv) {
 *        st = st.concat(argv[0]);
 *        st = st.concat("]");
 *        System.out.println("st is " + st);
 *        System.exit(42);
 *    }
 *} </pre></blockquote>
 *
 * @see javax.isolate.IsolateStartupException
 **/
public final class Isolate {
    /**
     * Controls access to each public API entry point. The
     * isolate creator can grant API access to the child.
     * Note this is a static so it is private to each Isolate
     */
    private static int  _API_access_ok;

    /**
     * Priority level of this Isolate that was set using setPriority before
     * the isolate has started.
     */
    private int             _priority;

    /**
     * Links to the next Isolate in a task's _seen_isolates list. See
     * Task.cpp for more information.
     */
    private Isolate         _next;

    /**
     * A number that uniquely identifies the task represented by this
     * Isolate object.
     */
    private long            _uniqueId;

    /**
     * Called by native code when the task corresponding to this Isolate
     * has terminated
     */
    private int             _terminated;

    /**
     * If this isolate has terminated, this variable saves the exit code --
     * Normally the exitCode() method would retrieve the exit code from
     * the Task. _saved_exit_code is used only if this Isolate object 
     * has been dis-associated from the Task (i.e., the Task has
     * exited).
     */
    private int             _saved_exit_code;

    /**
     * Saves the mainClass parameter passed to Isolate() constructor.
     */
    private String          _mainClass;

    /**
     * Saves the mainArgs parameter passed to Isolate() constructor.
     */
    private String[]        _mainArgs;

    /**
     * Saves app_classpath[] parameter passed to Isolate() constructor.
     */
    private String[]        _app_classpath;

    /**
     * Saves sys_classpath[] parameter passed to Isolate() constructor.
     */
    private String[]        _sys_classpath;

    /**
     * Packages we want to be hidden in this Isolate. See definition of hidden package in 
     * doc/misc/Romizer.html
     */
    private String[]        _hidden_packages;
    /**
     * Packages we want to be restricted in this Isolate. See definition of restricted package in 
     * doc/misc/Romizer.html
     */
    private String[]        _restricted_packages;

    /**
     * Amount of memory reserved for this isolate
     * The isolate cannot get OutOfMemory exception until it
     * allocates at least memoryReserve bytes.
     * If the system cannot reserve the requested amount,
     * the isolate will not start.
     */
    private int             _memoryReserve = 0;

    /**
     * Memory allocation limit for this isolate
     * OutOfMemoryError exception is thrown if
     * - the isolate exceeds its memory allocation limit
     * - excess over the reserved amount for this isolate
     *   cannot be allocated in the heap or
     *   conflicts with reserves of other isolates
     */
    private int             _memoryLimit = Integer.MAX_VALUE;

    /**
     * Used during bootstrap of new Isolate to store API access value
     */
    private int             _APIAccess = 0;


    private int             _ConnectDebugger = 0;


    private int             _UseVerifier = 1;

    private int             _profileId = DEFAULT_PROFILE_ID;
    
    private int             _UseProfiler = 1;

     /**
      * ID of default profile.
      */
    private final static int DEFAULT_PROFILE_ID = -1;
     /**
     * A special priority level to indicate that an Isolate is suspended.
     */
    private final static int SUSPEND = 0;

    /**
     * The minimum priority that an Isolate can have.
     */
    public final static int MIN_PRIORITY = 1;

    /**
     * The default priority that is assigned to an Isolate.
     */
    public final static int NORM_PRIORITY = 2;

    /**
     * The maximum priority that an Isolate can have.
     */
    public final static int MAX_PRIORITY = 3;

    /**
     * Creates a new isolated java application with a default configuration.
     *
     * <p>This constructor has the same effect as invoking
     * {@link #Isolate(String,String[],String[])}
     *  and passing <code>null</code> for the <code>app_classpath</code> 
     * and <code>sys_classpath</code> parameters.
     * See the long constructor documentation for more details.
     *
     * @param mainClass fully qualified name of the main method class
     * @param mainArgs the arguments of the main method in the new isolate
     * @throws IsolateStartupException if an error occurs in the configuration
     * or startup of the new isolate before any application code is invoked
     **/
    public Isolate(String mainClass, String[] mainArgs)
        throws IsolateStartupException {
        this(mainClass, mainArgs, (String[])null);
    }

    /**
     * Creates a new isolated java application with a default configuration.
     *
     * <p>This constructor has the same effect as invoking
     * {@link #Isolate(String,String[],String[], String[])}
     * and passing <code>null</code> for the <code>sys_classpath</code> 
     * parameter.
     * See the long constructor documentation for more details.
     *
     * @param mainClass fully qualified name of the main method class
     * @param mainArgs the arguments of the main method in the new isolate
     * @param app_classpath the application classpath(s) for the isolate
     *        (see <a href=#classdoc_class_path>Class path</a>)
     * @throws IsolateStartupException if an error occurs in the configuration
     * or startup of the new isolate before any application code is invoked
     **/
    public Isolate(String mainClass, String[] mainArgs, String[] app_classpath)
        throws IsolateStartupException {
        this(mainClass, mainArgs, app_classpath, (String[])null);
    }


    /**
     * Creates a new Isolate with the specified arguments and
     * classpath. <p>
     *
     * The new isolate will execute the <code>main</code> method of
     * class <code>mainClass</code> with arguments
     * <code>mainArgs</code>. The <code>mainClass</code> parameter
     * must reference a class present in the romized system classes,
     * or in one of the classpath elements specified by the
     * <code>sys_classpath</code> and <code>app_classpath</code> parameters.
     *
     * <p>When the constructor returns, the new isolate is not yet
     * running. The new isolate does not start execution until the
     * {@link #start start} method is invoked. The {@link #halt halt}
     * and {@link #exit exit} methods will fail if before 
     * {@link #start start} is invoked.
     *
     * <p>Class resolution and loading are performed in the new task
     * represented by this new isolate. Any loading exceptions (such as
     * <code>ClassNotFoundException</code>), including the loading
     * exception of the specified <code>mainClass</code>, will occur
     * inside the new task when it is started, not the creator task.
     *
     * <p>Changes made to any of the constructor's parameters after
     * control returns from this constructor will have no effect on the
     * newly created isolate.
     *
     * <p>If <code>mainArgs</code> is <code>null</code>, a zero-length
     * <code>String</code> array will be provided to the main method
     * of <code>mainClass</code>.
     * 
     * <p>User application classes should be put on <code>app_classpath</code>,
     * while <code>sys_classpath</code> can contain only trusted system
     * classes.
     *
     * <p>WARNING: UNTRUSTED USER APPLICATION CLASSES MUST NEVER BE PUT ON THE
     * SYSTEM CLASS PATH, AS IT GRANTS THEM ACCESS TO SYSTEM INTERNALS AND
     * BREAKS SYSTEM SECURITY.
     *
     * @param mainClass fully qualified name of the main method class
     * @param mainArgs the arguments of the main method in the new isolate
     * @param app_classpath the application classpath(s) for the isolate
     * @param sys_classpath the system classpath(s) for the isolate
     *        (see <a href=#classdoc_class_path>Class path</a>)
     * @throws IsolateStartupException if an error occurs in the configuration
     *         of the new isolate before any application code is invoked
     * @throws IllegalArgumentException if any parameters are found to be
     *         invalid.
     **/
    public Isolate(String mainClass, String[] mainArgs, 
                   String[] app_classpath, String[] sys_classpath)
        throws IsolateStartupException
    {
        securityCheck();
        if (mainClass == null) {
            throw new IllegalArgumentException("specified class name is null");
        }
        registerNewIsolate();
        _priority = NORM_PRIORITY;
        _mainClass = mainClass;
        _mainArgs = argCopy(mainArgs);
        _app_classpath = argCopy(app_classpath);
        _sys_classpath = argCopy(sys_classpath);

        /*
         * <p>WARNING: DO NOT REMOVE THIS MESSAGE UNLESS YOU HAVE READ AND
         * UNDERSTOOD THE SECURITY IMPLICATIONS: HAVING UNTRUSTED USER
         * APPLICATION CLASSES ON THE SYSTEM CLASS PATH GRANTS THEM ACCESS TO
         * SYSTEM INTERNALS AND BREAKS SYSTEM SECURITY.
         */
        if (_sys_classpath.length != 0) {
          System.err.println();
          System.err.println("****warning****");
          System.err.println("****Untrusted user classes must never be put");
          System.err.println("****on the system class path");
          System.err.println("****warning****");
          System.err.println();
        }
    }

    /**
     * Start execution of this <code>Isolate</code>. Any code that belongs
     * to this <code>Isolate</code> (including static initializers)
     * is executed only after this method is called.
     * <p>
     * Control will return from this method when the new isolate's
     * first user level thread starts executing, or if an error occurs
     * during the initialization of the new isolate.
     *
     * <p>If any exception is thrown by this method, no code in the
     * <code>Isolate</code> will have executed.
     *
     * <p>Errors such as the main class being invalid or not visible in
     * the classpath will occur handled within the new isolate.
     *
     * @throws IsolateStartupException if an error occurs in the
     *         initialization or configuration of the new isolate
     *         before any application code is invoked, or if this
     *         Isolate was already started or is terminated.
     * @throws IsolateResourceError if systems exceeds maximum Isolate count
     * @throws OutOfMemoryError if the reserved memory cannot be allocated
     */
    public synchronized void start() throws IsolateStartupException
    {
        if (getStatus() > NEW) {
            throw new IsolateStartupException("Isolate has already started");
        }

        try {
            nativeStart();
        } catch (IsolateResourceError e) {
            throw e;
        } catch (OutOfMemoryError e) {
            throw e;
        } catch (Throwable t) {
            // To be somewhat compilant to JSR-121, we do not pass any
            // other errors back to the caller of start().  Instead,
            // the caller can use Isolate.exitCode() to discover that
            // the isolate has exited. See CR 6270554.
        }

        // Wait till the fate of the started isolate is known
        // (STARTED or STOPPED...)
        while (getStatus() <= NEW) {
            try {
                // Note: do NOT use wait(). See comments inside waitForExit().
                waitStatus(NEW);
            } catch (InterruptedException e) {
                throw new IsolateStartupException(
                      "Exception was thrown while Isolate was starting");
            }
        }
    }

    /**
     * Requests normal termination of this <code>Isolate</code>.
     * Invocation of this method is equivalent to causing the isolate
     * to invoke {@link java.lang.Runtime#exit(int)}.  If this method
     * invocation is, in fact, the cause of the isolate's termination,
     * the <code>status</code> supplied will be the isolate's
     * termination status.
     *
     * <p>No exception is thrown if this isolate is already
     * terminated.  Even if {@link #isTerminated()} returns false prior
     * to invoking <code>exit</code>, an invocation of <code>exit</code> may
     * occur after the isolate exits on its own or is terminated by
     * another isolate. In these cases, the actual exit code reported by
     * the isolate may be different from <code>status</code>.
     *
     * <p>If this isolate is not yet started, it will be marked as
     * already terminated. A subsequent invocation to {@link #start()} would
     * result in an IsolateStartupException.
     *
     * <p>If this isolate is suspended, it will be terminated without
     * being resumed.
     *
     * @param status Termination status. By convention, a nonzero status 
     *               code indicates abnormal termination.
     **/
    public void exit(int status) {
        try {
            stop(status, this == currentIsolate() ?
                 EXIT_REASON_SELF_EXIT :
                 EXIT_REASON_OTHER_EXIT);
        } catch (SecurityException se) {
            stop(status, EXIT_REASON_SELF_EXIT);
        }
    }

    /**
     * Forces termination of this <code>Isolate</code>.
     *
     * If this method invocation is in fact the cause of the isolate's
     * termination, the <code>status</code> supplied will be the
     * isolate's termination status.
     *
     * <p>No exception is thrown if this isolate is already
     * terminated.  Even if {@link #isTerminated()} returns false prior
     * to invoking <code>halt</code>, an invocation of <code>halt</code> may
     * occur after the isolate exits on its own or is terminated by
     * another isolate.  In these cases, the actual exit code reported by
     * the isolate may be different from <code>status</code>.
     *
     * <p>If this isolate is not yet started, it will be marked as
     * already terminated. A subsequent invocation to {@link #start()} would
     * result in an IsolateStartupException.
     *
     * <p>If this isolate is suspended, it will be terminated without
     * being resumed.
     *
     * <h3>Implementation Note</h3>
     *
     * Implementations should strive to implement "quick" termination
     * with as little coordination with the target isolate as possible.
     * The only information required of a terminated isolate is the exit
     * code it was terminated with.
     *
     * @param status Termination status. By convention, a nonzero status code
     *               indicates abnormal termination.
     **/
    public void halt(int status) {
        try {
            stop(status, this == currentIsolate() ?
                 EXIT_REASON_SELF_HALT :
                 EXIT_REASON_OTHER_HALT);
        } catch (SecurityException se) {
            stop(status, EXIT_REASON_SELF_HALT);
        }
    }

    /**
     * Returns true if this <code>Isolate</code> is terminated.
     */
    public boolean isTerminated() {
        int state = getStatus();
        return (state >= STOPPED);
    }

    /**
     * Returns the Isolate object corresponding to the currently executing
     * task.
     *
     * <p>This method never returns <code>null</code>.
     *
     * @return the <code>Isolate</code> object for the current task
     **/
    public static Isolate currentIsolate() {
        securityCheck();
        return currentIsolate0();
    }

    private native static Isolate currentIsolate0();

    /**
     * Returns an array of <code>Isolate</code> objects representing
     * all tasks that have been started but have not terminated.
     * New tasks may have been constructed or existing ones
     * terminated by the time this method returns.
     *
     * @return the active <code>Isolate</code> objects at the time
     * of the call
     **/
    public static Isolate[] getIsolates() {
        securityCheck();
        return getIsolates0();
    }
    private native static Isolate[] getIsolates0();


    ///////////////////////////////////////////////////////////////////////////
    //  Valid state transitions are:
    //  NEW                 -> { STARTED, STOPPED }
    //  STARTED             -> { STOPPING, STOPPED }
    //  STOPPING            -> { STOPPED }
    //  { STOPPED }           : final states.
    //  { NEW}                : initial states.
    //
    //
    // Note: only Isolate created by the current isolate can be in the NEW
    // state.
    // Hence, knowing if an isolate is started only consists of testing if
    // its state is > NEW.

    static final int INVALID_TASK_ID = -1;  // invalid task id.

    static final int NEW             = 1;   // created by the current isolate
    static final int STARTED         = 2;   // start() method has been called.
    static final int STOPPING        = 3;   // isolate is stopping --
                                            // see IsolateEvent.STOPPING
    static final int STOPPED         = 4;   // isolate was terminated --
                                            // see IsolateEvent.TERMINATED

    /**
     * Returns a small integer ID that uniquely identifies this
     * Isolate among the current set of active Isolates. The returned
     * ID will remain unchanged and reserved for this Isolate during its
     * entire lifetime. However, after this Isolate is terminated, the ID may
     * be resumed for a new Isolate.
     *
     * @return -1 if the task has not been started or it has been terminated,
     *         
     */
    public int id() {
        return id0();
    }
    private native int id0();

    /**
     * Returns a 64-bit ID that uniquely identifies this Isolate.
     * The ID is assigned when the Isolate is created and will remain 
     * unchanged and reserved for this Isolate during the entire 
     * lifetime of the VM. 
     */
    public long uniqueId() {
      return _uniqueId;
    }

    /**
     * @return the amount of object heap memory reserved for this Isolate.
     */
    public int reservedMemory() {
        return _memoryReserve;
    }

    /**
     * @return the maximum amount of object heap memory that can be
     * allocated by this Isolate.
     */
    public int totalMemory() {
        return _memoryLimit;
    }

    /**
     * This function returns the approximate amount of object heap
     * memory currently used by this Isolate. The approximate value
     * may not be accurate: it may not include recent allocations
     * made by the Isolate, and it may count objects allocated by the
     * Isolate that have since become unreachable. <p>
     *
     * @return the approximate amount of object heap memory currently 
     * used by this Isolate.
     */
    public int usedMemory() {
        return usedMemory0();
    }
    private native int usedMemory0();

    /**
     * Sets the object heap memory reserved and maximum limits to the
     * same value. Note that if the system does not have sufficient
     * resources to guaranteed the reserved amount, the start() method
     * of this Isolate would fail.  This method should only be called
     * before the Isolate is started.  Calling it after the isolate
     * has started will cause undetermined behavior. <p>
     *
     * @param reserved The minimum amount of memory guaranteed to be
     *                 available to the isolate at any time. Also the total
     *                 amount of memory that the isolate can reserve.
     */
    public void setMemoryQuota(int reserved) {
        setMemoryQuota(reserved, reserved);
    }

    /**
     * Sets the object heap memory quota for this Isolate. Note that
     * if the system does not have sufficient resources to guaranteed
     * the reserved amount, the start() method of this Isolate would
     * fail.
     * This method should only be called before the Isolate is
     * started.  Calling it after the isolate has started will cause
     * undetermined behavior. <p>
     *
     * @param reserved The minimum amount of memory guaranteed to be
     *                 available to the isolate at any time.
     * @param total    The total amount of memory that the isolate can
     *                 reserve.
     */
    public void setMemoryQuota(int reserved, int total) {
        if (reserved < 0 || reserved > total) {
            throw new IllegalArgumentException();
        }
        _memoryReserve = reserved;
        _memoryLimit   = total; 
    }

    /* Return true if isolate has been started.
     */
    synchronized boolean isStarted() {
        return getStatus() <= NEW;
    }

    private String[] argCopy(String[] args) {
        if (args == null) {
            return new String[0];
        }
        String[] result = new String[args.length];
        JVM.unchecked_obj_arraycopy(args, 0, result, 0, args.length);
        return result;
    }

    /**
     * Add this Isolate to the TaskDesc::_seen_isolates list of the
     * current task and return the globally unique isolate identifier.
     */
    private native void registerNewIsolate();

    /**
     * Stopping execution of an Isolate. Used by implementation of exit
     * and halt.
     *
     * <p>If this isolate is not yet started, it will be marked as
     * already terminated. A subsequent invocation to {@link #start()} would
     * result in an IsolateStartupException.
     *
     * <p>If this isolate is suspended, it will be terminated without
     * being resumed.
     */
    private native void stop(int exit_code, int exit_reason);

    /**
     * Adjust the priority of this Isolate. The priority controls the
     * amount of CPU time that VM allocates to execute threads in this
     * Isolate.
     *
     * Note: thread scheduling and task scheduling use separate mechanisms.
     * In the current imeplentation, each task is guaranteed execution time
     * relative to its priority.
     *
     *
     * @param new_priority must be between <code>MIN_PRIORITY</code>
     * and <code>MAX_PRIORITY</code>, or else this method call will
     * have no effect.
     */
    public void setPriority(int new_priority) {
        if (new_priority >= MIN_PRIORITY && new_priority <= MAX_PRIORITY) {
            _priority = new_priority;
            setPriority0(new_priority);
        }
    }

    private native void setPriority0(int new_priority);

    /**
     * Returns the priority of this isolate.
     *
     * @return the priority of this isolate. If the isolate has already
     *         terminated, the returned value is undefined.
     */
    public int getPriority() {
        return _priority;
    }

    /**
     * Returns if this isolate has been suspended.
     * @return true iff the isolate has been suspended.
     */
    public boolean isSuspended() {
        return (isSuspended0() != 0 ? true : false);
    }

    private native int isSuspended0();

    /**
     * Suspends all threads in this isolate from execution. This
     * method should be used carefully if objects shared between isolates
     * (passed via native methods) are used for synchornization. A
     * suspended isolate holding a lock on such an object will stop other
     * tasks from ever receiving that lock.
     * See introduction for better ways of communicating between isolates.
     *
     * This method will suspend the isolate only if the isolate is currently
     * started, not suspended and not terminated. Otherwise this method
     * has no effect.
     */
    public void suspend() {
        suspend0();
    }

    private native void suspend0();

    /**
     * The opposite of the <code>suspend</code> method.
     *
     * This method will resume the isolate only if the isolate is
     * currently started, suspended and not terminated. Otherwise this
     * method has no effect.
     */
    public void resume() {
        resume0();
    }

    private native void resume0();

    /**
     * Returns the exit code of the isolate. If this Isolate has terminated,
     * this method returns the exit code parameter to the first invocation of 
     * System.exit(), Isolate.exit() or Isolate.halt() that caused the Isolate
     * to terminate. If this Isolate has terminated without calling 
     * System.exit(), Isolate.exit() or Isolate.halt(), then 0 is returned.
     *
     * If this Isolate has not started or has not terminated, 0 is returned.
     *
     * @return the exit code of the isolate.
     */
    public int exitCode() {
        return exitCode0();
    }

    private native int exitCode0();

    /**
     * Blocks the execution of the calling thread until this Isolate
     * has exited. If <code>waitForExit()</code> is called on the
     * current Isolate, the result is undefined.
     *
     * @throws InterruptedException (unimplemented yet): if CLDC 
     *         Specification 1.1 is enabled, when a thread is blocked
     *         inside this method, it may be interrupted by an
     *         invocation of Thread.interrupt, in which case an
     *         InterruptedException is thrown regardless of the
     *         termination status of this Isolate.
     */
    public synchronized void waitForExit() /* throws InterruptedException */ {
        while (getStatus() <= STOPPING) {
            try {
                // Note: do NOT use wait(): When notifyStatus() is
                // called, the calling thread may not hold the monitor
                // of this object, so if we wrote the code like this
                // we may get into a race condition
                //     while (getStatus() <= STOPPING) {
                //         <thread switch/race condition may happen here>
                //         wait();
                //     }
                // waitStatus() performs the getStatus() <= STOPPING check in
                // native code again, where thread switch is guaranteed to
                // not happen. Hence we won't have a race condition.
                waitStatus(STOPPING);
            } catch (InterruptedException e) {
                // IMPL_NOTE: this method should throw InterruptedException!
                throw new Error();
            }
        }
    }

    /**
     * Returns the classpath the Isolate was started with.
     *
     * @return String[] that is equal to classpath argument passed to
     *         Isolate constructor
     */
    public String[] getClassPath() {
        return argCopy(_app_classpath);
    }

    /**
     * Determine if this isolate has permission to access the API
     * If not, throw runtime exception
     */
    private static void securityCheck() {
        if (_API_access_ok == 0) {
            throw new SecurityException("Access to Isolate API not allowed");
        }
    }

    /** 
     * Sets the access to Isolate API for this Isolate. This method
     * should be used by the AMS, before the Isolate is started, to
     * control whether or not a created Isolate is able to call the
     * Isolate API. The default for all but the first Isolate is
     * <code>false</code>. If the AMS calls this method after the Isolate
     * has started, it has no effect.<p>
     *
     * In additional, after an Isolate has started, if it has access
     * to the Isolate API, it can call this method to disable
     * it. However, once it loses the access, attempts to call this
     * method would result in a SecurityException.
     */
    public void setAPIAccess(boolean access) {
        _APIAccess = (access == true ? 1 : 0);

        // Only allow access to be degraded after starting.
        if (!access && equals(currentIsolate())) {
            _API_access_ok = 0;
        }
    }

    public void setDebug(boolean mode) {
        _ConnectDebugger = (mode == true ? 1 : 0);
    }

    public void attachDebugger() {
        securityCheck();
        attachDebugger0(this);
    }

    /**
     * Indicates if debugger connection is established with the VM.
     *
     * @return true if debugger is connected, otherwise returns false.
     */
    public native boolean isDebuggerConnected();

    /**
     * Controls whether or not classes for this isolate need to be
     * verified. When creating a new Isolate, the AMS may waive
     * verification for classes that have already been verified. The
     * default is <code>false</code>.  This method should be called
     * before the Isolate is started.
     */
    public void setUseVerifier(boolean verify) {
        _UseVerifier = (verify == true ? 1 : 0);
    }

    /**
     * Returns the current status of the task represented by this Isolate.
     *
     * @return one of NEW, STARTED, STOPPING or STOPPED
     */
    private native int getStatus();

    /**
     * Notify all threads that are waiting on the status of any Isolate object
     * that represent the same task as this Isolate object.
     *
     * To simplify VM design, this method does NOT need to be called
     * while holding a lock of such Isolate objects. To avert race conditions,
     * the waiting threads must be blocked using waitStatus() instead
     * of wait(). See comments inside waitForExit() for details.
     */
    private native void notifyStatus();

    /**
     * Blocks the current thread until getStatus() would return a value
     * greater than maxStatus, or (CLDC Spec 1.1 only) until this
     * thread is interrupted. <p>
     *
     * See comments inside waitForExit() to see why this method method
     * must be used instead of wait() to avert race conditions.
     */
    private native void waitStatus(int maxStatus) throws InterruptedException;

    /* For now, ignore the links argument.
     * Native method will use the JNI invocation API to start a new in-process
     * JVM to execute the new Isolate.
     */
    private native void nativeStart() throws IsolateStartupException;

    /**
     * The last non-daemon thread returned from main.
     */
    private static final int EXIT_REASON_IMPLICIT_EXIT  = 1;

    /**
     * The last non-daemon thread exited due to an uncaught exception.
     *
     * <p>Note that if a daemon thread dies with an uncaught exception,
     * that will not cause the containing isolate to die.  Additionally,
     * only if the <emph>last</emph> non-daemon thread dies with
     * an uncaught exception will this reason be noted.  Uncaught exceptions
     * in shutdown hooks do not count, either.
     */
    private static final int EXIT_REASON_UNCAUGHT_EXCEPT = 6;

    /**
     * The isolate invoked {@link System#exit System.exit},
     * {@link Runtime#exit Runtime.exit}, or
     * {@link Isolate#exit Isolate.exit} on itself.
     */
    private static final int EXIT_REASON_SELF_EXIT  = 2;

    /**
     * The isolate invoked
     * {@link Runtime#halt Runtime.halt} or
     * {@link Isolate#halt Isolate.halt} on itself.
     */
    private static final int EXIT_REASON_SELF_HALT = 3;

    /**
     * Some other isolate invoked {@link Isolate#exit Isolate.exit}
     * on the isolate.
     */
    private static final int EXIT_REASON_OTHER_EXIT = 4;

    /**
     * Some other isolate invoked {@link Isolate#halt Isolate.halt}
     * on the isolate.
     */
    private static final int EXIT_REASON_OTHER_HALT = 5;

    /**
     * Sets active profile name for isolate. This method must be 
     * called before the isolate is started. 
     *
     * If isolate is already started the method throws an 
     * <code>IllegalIsolateStateException</code>.
     *
     * The method also determines if <code>profile</code> 
     * is a name of existing profile which is defined in ROM 
     * configuration file. If not, throws runtime 
     * <code>IllegalArgumentException</code>.
     *
     * @param profile  The new active profile name.
     */
    public native void setProfile(String profile) throws 
      IllegalArgumentException,
      IllegalIsolateStateException;

    /**
     * Sets the packages which will be hidden. See definition of hidden package in 
     * doc/misc/Romizer.html. Note, that this function call overrides previous settings.
     *
     * If isolate is already started the method throws an 
     * <code>IllegalIsolateStateException</code>.
     *
     * @param package_name.  The name of package for marking.
     */
    public void setHiddenPackages(String[] package_names) throws 
      IllegalIsolateStateException {
        if (getStatus() > NEW) {
            throw new IllegalIsolateStateException("Can only set hidden packages before Isolate starts");
        }
      _hidden_packages = package_names;
    }

    /**
     * Sets the packages which will be restricted. See definition of restricted package in 
     * doc/misc/Romizer.html. Note, that this function call overrides previous settings.
     *
     * If isolate is already started the method throws an 
     * <code>IllegalIsolateStateException</code>.
     *
     * @param package_name  The name of package for marking.
     */
    public void setRestrictedPackages(String[] package_names) throws 
      IllegalIsolateStateException {
        if (getStatus() > NEW) {
            throw new IllegalIsolateStateException("Can only set restricted packages before Isolate starts");
        }
      _restricted_packages = package_names;
    }
    
    /**
     * Sets whether isolate should be profiled or not. By default all isolates are profiled.
     * Should be set before task for isolate is created.
     *
     */
    public void setUseProfiler(boolean useProfiler) {
      _UseProfiler = (useProfiler == true ? 1 : 0);
    }

    private native void attachDebugger0(Isolate obj);
}
