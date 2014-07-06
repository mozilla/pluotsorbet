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

import java.security.*;

/**
 * A <i>thread</i> is a thread of execution in a program. The Java
 * Virtual Machine allows an application to have multiple threads of
 * execution running concurrently.
 * <p>
 * Every thread has a priority. Threads with higher priority are
 * executed in preference to threads with lower priority.
 * <p>
 * There are two ways to create a new thread of execution. One is to
 * declare a class to be a subclass of <code>Thread</code>. This
 * subclass should override the <code>run</code> method of class
 * <code>Thread</code>. An instance of the subclass can then be
 * allocated and started. For example, a thread that computes primes
 * larger than a stated value could be written as follows:
 * <p><hr><blockquote><pre>
 *     class PrimeThread extends Thread {
 *         long minPrime;
 *         PrimeThread(long minPrime) {
 *             this.minPrime = minPrime;
 *         }
 *
 *         public void run() {
 *             // compute primes larger than minPrime
 *             &nbsp;.&nbsp;.&nbsp;.
 *         }
 *     }
 * </pre></blockquote><hr>
 * <p>
 * The following code would then create a thread and start it running:
 * <p><blockquote><pre>
 *     PrimeThread p = new PrimeThread(143);
 *     p.start();
 * </pre></blockquote>
 * <p>
 * The other way to create a thread is to declare a class that
 * implements the <code>Runnable</code> interface. That class then
 * implements the <code>run</code> method. An instance of the class can
 * then be allocated, passed as an argument when creating
 * <code>Thread</code>, and started. The same example in this other
 * style looks like the following:
 * <p><hr><blockquote><pre>
 *     class PrimeRun implements Runnable {
 *         long minPrime;
 *         PrimeRun(long minPrime) {
 *             this.minPrime = minPrime;
 *         }
 *
 *         public void run() {
 *             // compute primes larger than minPrime
 *             &nbsp;.&nbsp;.&nbsp;.
 *         }
 *     }
 * </pre></blockquote><hr>
 * <p>
 * The following code would then create a thread and start it running:
 * <p><blockquote><pre>
 *     PrimeRun p = new PrimeRun(143);
 *     new Thread(p).start();
 * </pre></blockquote>
 * <p>
 *
 *
 * @version 12/17/01 (CLDC 1.1)
 * @see     java.lang.Runnable
 * @see     java.lang.Runtime#exit(int)
 * @see     java.lang.Thread#run()
 * @since   JDK1.0, CLDC 1.0
 */
public
class Thread implements Runnable {

    /* Thread priority */
    private int         priority = NORM_PRIORITY;

    /* What will be run. */
    private Runnable    target;

    /*
     * This private variable is used by the VM.
     * Users never see it.
     */
    private Object vm_thread;
    private int    is_terminated;
    private int    is_stillborn;

    /* Thread name */
    private char   name[];

    /**
     * The minimum priority that a thread can have.
     */
    public final static int MIN_PRIORITY = 1;

    /**
     * The default priority that is assigned to a thread.
     */
    public final static int NORM_PRIORITY = 5;

    /**
     * The maximum priority that a thread can have.
     */
    public final static int MAX_PRIORITY = 10;

    /**
     * Returns a reference to the currently executing
     * <code>Thread</code> object.
     *
     * @return  the currently executing thread.
     */
    public static native Thread currentThread();

    /* For autonumbering anonymous threads. */
    private static int threadInitNumber;
    private static synchronized int nextThreadNum() {
        return ++threadInitNumber;
    }

    /**
     * Causes the currently executing thread object
     * to temporarily pause and allow other threads to execute.
     */
    public static native void yield();

    /**
     * Causes the currently executing thread to sleep (temporarily cease
     * execution) for the specified number of milliseconds. The thread
     * does not lose ownership of any monitors.
     *
     * @param      millis   the length of time to sleep in milliseconds.
     * @exception  InterruptedException if another thread has interrupted
     *             the current thread.  The <i>interrupted status</i> of the
     *             current thread is cleared when this exception is thrown.
     * @see        java.lang.Object#notify()
     */
    public static native void sleep(long millis) throws InterruptedException;

    /**
     * Initialize a Thread.
     *
     * @param target the object whose run() method gets called
     * @param name the name of the new thread
     */

    private void init(Runnable target, String name) {
        Thread parent = currentThread();
        this.target = target;
        this.name  = name.toCharArray();
        this.priority = parent.getPriority();
        setPriority(priority);
    }

   /**
     * Allocates a new <code>Thread</code> object.
     * <p>
     * Threads created this way must have overridden their
     * <code>run()</code> method to actually do anything.
     *
     * @see     java.lang.Runnable
     */
    public Thread() {
        init(null, "Thread-" + nextThreadNum());
    }

    /**
     * Allocates a new <code>Thread</code> object with the
     * given name.
     *
     * Threads created this way must have overridden their
     * <code>run()</code> method to actually do anything.
     *
     * @param   name   the name of the new thread.
     */
    public Thread(String name) {
        init(null, name);
    }

    /**
     * Allocates a new <code>Thread</code> object with a
     * specific target object whose <code>run</code> method
     * is called.
     *
     * @param   target   the object whose <code>run</code> method is called.
     */
    public Thread(Runnable target) {
        init(target, "Thread-" + nextThreadNum());
    }

    /**
     * Allocates a new <code>Thread</code> object with the given
     * target and name.
     *
     * @param   target   the object whose <code>run</code> method is called.
     * @param   name     the name of the new thread.
     */
    public Thread(Runnable target, String name) {
        init(target, name);
    }

    /**
     * Causes this thread to begin execution; the Java Virtual Machine
     * calls the <code>run</code> method of this thread.
     * <p>
     * The result is that two threads are running concurrently: the
     * current thread (which returns from the call to the
     * <code>start</code> method) and the other thread (which executes its
     * <code>run</code> method).
     *
     * @exception  IllegalThreadStateException  if the thread was already
     *               started.
     * @see        java.lang.Thread#run()
     */
    public synchronized void start() {
        start(this);
    }

    private static synchronized void start(Thread thread) {
        thread.start0();
    }

    private native void start0();

    /**
     * If this thread was constructed using a separate
     * <code>Runnable</code> run object, then that
     * <code>Runnable</code> object's <code>run</code> method is called;
     * otherwise, this method does nothing and returns.
     * <p>
     * Subclasses of <code>Thread</code> should override this method.
     *
     * @see     java.lang.Thread#start()
     * @see     java.lang.Runnable#run()
     */
    public void run() {
        if (target != null) {
            target.run();
        }
    }

    /**
     * Interrupts this thread.  In an implementation conforming
     * to the CLDC Specification, this operation is not
     * required to cancel or clean up any pending I/O operations
     * that the thread may be waiting for.
     * @throws SecurityException - if the current thread cannot modify this thread.
     *
     * @since JDK 1.0, CLDC 1.1
     */
    public void interrupt() {
        checkAccess();
        interrupt0();
    }

    /**
     * Tests if this thread is alive. A thread is alive if it has
     * been started and has not yet died.
     *
     * @return  <code>true</code> if this thread is alive;
     *          <code>false</code> otherwise.
     */
    public final native boolean isAlive();

    /**
     * Changes the priority of this thread.
     *
     * @param newPriority priority to set this thread to
     * @throws SecurityException - if the current thread cannot modify this thread.
     * @exception  IllegalArgumentException  If the priority is not in the
     *             range <code>MIN_PRIORITY</code> to
     *             <code>MAX_PRIORITY</code>.
     * @see        java.lang.Thread#getPriority()
     * @see        java.lang.Thread#MAX_PRIORITY
     * @see        java.lang.Thread#MIN_PRIORITY
     */
    public final void setPriority(int newPriority) {
        if (newPriority > MAX_PRIORITY || newPriority < MIN_PRIORITY) {
            throw new IllegalArgumentException();
        }
        checkAccess();
        setPriority0(priority, newPriority);
        priority = newPriority;
    }

    /**
     * Returns this thread's priority.
     *
     * @return  this thread's priority.
     * @see     java.lang.Thread#setPriority(int)
     */
    public final int getPriority() {
        return priority;
    }

    /**
     * Returns this thread's name.  Note that in CLDC the name
     * of the thread can only be set when creating the thread.
     *
     * @return  this thread's name.
     */
    public final String getName() {
        return String.valueOf(name);
    }

    /**
     * Returns the current number of active threads in the virtual machine.
     *
     * @return  the current number of active threads.
     */
    public static native int activeCount();

    /**
     * Waits for this thread to die.
     *
     * @exception  InterruptedException if another thread has interrupted
     *             the current thread.  The <i>interrupted status</i> of the
     *             current thread is cleared when this exception is thrown.
     */
    public final synchronized void join() throws InterruptedException {
        while (isAlive()) {
            wait(1000);
        }
    }

    /**
     * Returns a string representation of this thread, including the
     * thread's name and priority.
     *
     * @return  a string representation of this thread.
     */
    public String toString() {
        return "Thread[" + getName() + "," + getPriority() + "]";
    }

    /* Some private helper methods */
    private native void setPriority0(int oldPriority, int newPriority);
    private native void interrupt0();
    private synchronized native void internalExit();

    /**
     * Determines if the currently running thread has permission to 
     * modify this thread. 
     * <p>
     * If the thread argument is a system thread, then this method calls checkPermission 
     * with the RuntimePermission("modifyThread") permission. If the thread argument is not a system thread, 
     * this method just returns silently.
     * <p>
     * Note: This method was mistakenly non-final in JDK 1.1.
     * It has been made final in the Java 2 Platform.
     *
     * @exception  SecurityException  if the current thread is not allowed to
     *               access this thread.
     * @see       AccessController.checkPermission(java.security.Permission), RuntimePermission  
     */
    public final void checkAccess() {
      Permission required = new RuntimePermission("modifyThread");
      AccessController.checkPermission(required);
    }
}
