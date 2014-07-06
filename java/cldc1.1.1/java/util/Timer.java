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

package java.util;
import java.util.Date;

import com.sun.cldchi.jvm.JVM;

/**
 * A facility for threads to schedule tasks for future execution in a
 * background thread.  Tasks may be scheduled for one-time execution, or for
 * repeated execution at regular intervals.
 *
 * <p>Corresponding to each <tt>Timer</tt> object is a single background
 * thread that is used to execute all of the timer's tasks, sequentially.
 * Timer tasks should complete quickly.  If a timer task takes excessive time
 * to complete, it "hogs" the timer's task execution thread.  This can, in
 * turn, delay the execution of subsequent tasks, which may "bunch up" and
 * execute in rapid succession when (and if) the offending task finally
 * completes.
 *
 * <p>After the last live reference to a <tt>Timer</tt> object goes away
 * <i>and</i> all outstanding tasks have completed execution, the timer's task
 * execution thread terminates gracefully (and becomes subject to garbage
 * collection).  However, this can take arbitrarily long to occur.  By
 * default, the task execution thread does not run as a <i>daemon thread</i>,
 * so it is capable of keeping an application from terminating.  If a caller
 * wants to terminate a timer's task execution thread rapidly, the caller
 * should invoke the timer's <tt>cancel</tt> method.
 *
 * <p>If the timer's task execution thread terminates unexpectedly, for
 * example, because its <tt>stop</tt> method is invoked, any further
 * attempt to schedule a task on the timer will result in an
 * <tt>IllegalStateException</tt>, as if the timer's <tt>cancel</tt>
 * method had been invoked.
 *
 * <p>This class is thread-safe: multiple threads can share a single
 * <tt>Timer</tt> object without the need for external synchronization.
 *
 * <p>This class does <i>not</i> offer real-time guarantees: it schedules
 * tasks using the <tt>Object.wait(long)</tt> method.
 * <p>
 * Timers function only within a single VM and are cancelled when the VM exits.
 * When the VM is started no timers exist, they are created only by
 * application request.
 *
 * @see     TimerTask
 * @see     Object#wait(long)
 * @since   1.3
 */

public class Timer {
    /**
     * The timer task queue.  This data structure is shared with the timer
     * thread.  The timer produces tasks, via its various schedule calls,
     * and the timer thread consumes, executing timer tasks as appropriate,
     * and removing them from the queue when they're obsolete.
     */
    private TaskQueue queue = new TaskQueue();

    /**
     * The timer thread.
     */
    private TimerThread thread;

    /**
     * Time of this class initialization in the monotonic clock.
     */
    private static final long monotonicClockOffset = 
        JVM.monotonicTimeMillis();

    /**
     * Time of this class initialization in the user clock.
     * Can change if the user clock changes.
     */
    private static long userClockOffset = System.currentTimeMillis();

    /**
     * Creates a new timer.  The associated thread does <i>not</i> run as
     * a daemon thread, which may prevent an application from terminating.
     *
     * @see Thread
     * @see #cancel()
     */
    public Timer() {
    }

    /**
     * Schedules the specified task for execution after the specified delay.
     *
     * @param task  task to be scheduled.
     * @param delay delay in milliseconds before task is to be executed.
     * @throws IllegalArgumentException if <tt>delay</tt> is negative, or
     *         <tt>delay + System.currentTimeMillis()</tt> is negative.
     * @throws IllegalStateException if task was already scheduled or
     *         cancelled, or timer was cancelled.
     */
    public void schedule(TimerTask task, long delay) {
        if (delay < 0)
            throw new IllegalArgumentException("Negative delay.");
        if (System.currentTimeMillis() + delay < 0) {
            throw new IllegalArgumentException("Illegal execution time.");
        }
        sched(task, null, delay, 0);
    }

    /**
     * Schedules the specified task for execution at the specified time.  If
     * the time is in the past, the task is scheduled for immediate execution.
     *
     * @param task task to be scheduled.
     * @param time time at which task is to be executed.
     * @throws IllegalArgumentException if <tt>time.getTime()</tt> is negative.
     * @throws IllegalStateException if task was already scheduled or
     *         cancelled, timer was cancelled, or timer thread terminated.
     */
    public void schedule(TimerTask task, Date time) {
        if (time == null) {
            throw new NullPointerException();
        }
        sched(task, time, 0, 0);
    }

    /**
     * Schedules the specified task for repeated <i>fixed-delay execution</i>,
     * beginning after the specified delay.  Subsequent executions take place
     * at approximately regular intervals separated by the specified period.
     *
     * <p>In fixed-delay execution, each execution is scheduled relative to
     * the actual execution time of the previous execution.  If an execution
     * is delayed for any reason (such as garbage collection or other
     * background activity), subsequent executions will be delayed as well.
     * In the long run, the frequency of execution will generally be slightly
     * lower than the reciprocal of the specified period (assuming the system
     * clock underlying <tt>Object.wait(long)</tt> is accurate).
     *
     * <p>Fixed-delay execution is appropriate for recurring activities
     * that require "smoothness."  In other words, it is appropriate for
     * activities where it is more important to keep the frequency accurate
     * in the short run than in the long run.  This includes most animation
     * tasks, such as blinking a cursor at regular intervals.  It also includes
     * tasks wherein regular activity is performed in response to human
     * input, such as automatically repeating a character as long as a key
     * is held down.
     *
     * @param task   task to be scheduled.
     * @param delay  delay in milliseconds before task is to be executed.
     * @param period time in milliseconds between successive task executions.
     * @throws IllegalArgumentException if <tt>delay</tt> is negative, or
     *         <tt>delay + System.currentTimeMillis()</tt> is negative.
     * @throws IllegalStateException if task was already scheduled or
     *         cancelled, timer was cancelled, or timer thread terminated.
     */
    public void schedule(TimerTask task, long delay, long period) {
        if (delay < 0)
            throw new IllegalArgumentException("Negative delay.");
        if (period <= 0)
            throw new IllegalArgumentException("Non-positive period.");
        if (System.currentTimeMillis() + delay < 0) {
            throw new IllegalArgumentException("Illegal execution time.");
        }
        sched(task, null, delay, -period);
    }

    /**
     * Schedules the specified task for repeated <i>fixed-delay execution</i>,
     * beginning at the specified time. Subsequent executions take place at
     * approximately regular intervals, separated by the specified period.
     *
     * <p>In fixed-delay execution, each execution is scheduled relative to
     * the actual execution time of the previous execution.  If an execution
     * is delayed for any reason (such as garbage collection or other
     * background activity), subsequent executions will be delayed as well.
     * In the long run, the frequency of execution will generally be slightly
     * lower than the reciprocal of the specified period (assuming the system
     * clock underlying <tt>Object.wait(long)</tt> is accurate).
     *
     * <p>Fixed-delay execution is appropriate for recurring activities
     * that require "smoothness."  In other words, it is appropriate for
     * activities where it is more important to keep the frequency accurate
     * in the short run than in the long run.  This includes most animation
     * tasks, such as blinking a cursor at regular intervals.  It also includes
     * tasks wherein regular activity is performed in response to human
     * input, such as automatically repeating a character as long as a key
     * is held down.
     *
     * @param task   task to be scheduled.
     * @param firstTime First time at which task is to be executed.
     * @param period time in milliseconds between successive task executions.
     * @throws IllegalArgumentException if <tt>time.getTime()</tt> is negative.
     * @throws IllegalStateException if task was already scheduled or
     *         cancelled, timer was cancelled, or timer thread terminated.
     */
    public void schedule(TimerTask task, Date firstTime, long period) {
        if (period <= 0)
            throw new IllegalArgumentException("Non-positive period.");
        if (firstTime == null) {
            throw new NullPointerException();
        }
        sched(task, firstTime, 0, -period);
    }

    /**
     * Schedules the specified task for repeated <i>fixed-rate execution</i>,
     * beginning after the specified delay.  Subsequent executions take place
     * at approximately regular intervals, separated by the specified period.
     *
     * <p>In fixed-rate execution, each execution is scheduled relative to the
     * scheduled execution time of the initial execution.  If an execution is
     * delayed for any reason (such as garbage collection or other background
     * activity), two or more executions will occur in rapid succession to
     * "catch up."  In the long run, the frequency of execution will be
     * exactly the reciprocal of the specified period (assuming the system
     * clock underlying <tt>Object.wait(long)</tt> is accurate).
     *
     * <p>Fixed-rate execution is appropriate for recurring activities that
     * are sensitive to <i>absolute</i> time, such as ringing a chime every
     * hour on the hour, or running scheduled maintenance every day at a
     * particular time.  It is also appropriate for for recurring activities
     * where the total time to perform a fixed number of executions is
     * important, such as a countdown timer that ticks once every second for
     * ten seconds.  Finally, fixed-rate execution is appropriate for
     * scheduling multiple repeating timer tasks that must remain synchronized
     * with respect to one another.
     *
     * @param task   task to be scheduled.
     * @param delay  delay in milliseconds before task is to be executed.
     * @param period time in milliseconds between successive task executions.
     * @throws IllegalArgumentException if <tt>delay</tt> is negative, or
     *         <tt>delay + System.currentTimeMillis()</tt> is negative.
     * @throws IllegalStateException if task was already scheduled or
     *         cancelled, timer was cancelled, or timer thread terminated.
     */
    public void scheduleAtFixedRate(TimerTask task, long delay, long period) {
        if (delay < 0)
            throw new IllegalArgumentException("Negative delay.");
        if (period <= 0)
            throw new IllegalArgumentException("Non-positive period.");
        if (System.currentTimeMillis() + delay < 0) {
            throw new IllegalArgumentException("Illegal execution time.");
        }
        sched(task, null, delay, period);
    }

    /**
     * Schedules the specified task for repeated <i>fixed-rate execution</i>,
     * beginning at the specified time. Subsequent executions take place at
     * approximately regular intervals, separated by the specified period.
     *
     * <p>In fixed-rate execution, each execution is scheduled relative to the
     * scheduled execution time of the initial execution.  If an execution is
     * delayed for any reason (such as garbage collection or other background
     * activity), two or more executions will occur in rapid succession to
     * "catch up."  In the long run, the frequency of execution will be
     * exactly the reciprocal of the specified period (assuming the system
     * clock underlying <tt>Object.wait(long)</tt> is accurate).
     *
     * <p>Fixed-rate execution is appropriate for recurring activities that
     * are sensitive to <i>absolute</i> time, such as ringing a chime every
     * hour on the hour, or running scheduled maintenance every day at a
     * particular time.  It is also appropriate for for recurring activities
     * where the total time to perform a fixed number of executions is
     * important, such as a countdown timer that ticks once every second for
     * ten seconds.  Finally, fixed-rate execution is appropriate for
     * scheduling multiple repeating timer tasks that must remain synchronized
     * with respect to one another.
     *
     * @param task   task to be scheduled.
     * @param firstTime First time at which task is to be executed.
     * @param period time in milliseconds between successive task executions.
     * @throws IllegalArgumentException if <tt>time.getTime()</tt> is negative.
     * @throws IllegalStateException if task was already scheduled or
     *         cancelled, timer was cancelled, or timer thread terminated.
     */
    public void scheduleAtFixedRate(TimerTask task, Date firstTime,
                                    long period) {
        if (period <= 0)
            throw new IllegalArgumentException("Non-positive period.");
        if (firstTime == null) {
            throw new NullPointerException();
        }
        sched(task, firstTime, 0, period);
    }

    /**
     * Schedule the specified timer task for execution at the specified
     * time with the specified period, in milliseconds.  If period is
     * positive, the task is scheduled for repeated execution; if period is
     * zero, the task is scheduled for one-time execution. Time is specified
     * in Date.getTime() format.  This method checks timer state, task state,
     * and initial execution time, but not period.
     *
     * @param task   task to be scheduled.
     * @param userTime the user time at which task is to be executed or 
     *        <tt>null</tt> if the delay is specified
     * @param delay the delay in milliseconds before the task execution
     * @param period time in milliseconds between successive task executions.
     * @param isUserClock true if the time is bound to user clock
     * @throws IllegalArgumentException if <tt>time()</tt> is negative.
     * @throws IllegalStateException if task was already scheduled or
     *         cancelled, timer was cancelled, or timer thread terminated.
     */
    private void sched(TimerTask task, Date userTime, long delay, long period) {
        final boolean isUserClock = userTime != null;

        long time;

        if (isUserClock) {
            long t = userTime.getTime();
            if (t < 0) {
                throw new IllegalArgumentException("Illegal execution time.");
            }
            time = Timer.userTimeFromStart(t);
        } else {
            time = Timer.monotonicTimeFromStart(JVM.monotonicTimeMillis() + delay);
        }
        
        synchronized (queue) {
            if (!queue.newTasksMayBeScheduled) {
                throw new IllegalStateException("Timer already cancelled.");
            }

	    /*
	     * If the TimerThread has exited without an error
	     * it is restarted. See the commentary in TimerThread.run.
	     */
	    if (thread == null || !thread.isAlive()) {
		thread = new TimerThread(queue);
		thread.start();
	    }

            synchronized (task.lock) {
                if (task.state != TimerTask.VIRGIN) {
                    throw new IllegalStateException(
                        "Task already scheduled or cancelled");
                }
                task.nextExecutionTime = time;
                task.period = period;
                task.state = TimerTask.SCHEDULED;
                task.isUserClock = isUserClock;
            }

            queue.add(task);
            if (queue.getMin() == task)
                queue.notify();
        }
    }

    /**
     * Terminates this timer, discarding any currently scheduled tasks.
     * Does not interfere with a currently executing task (if it exists).
     * Once a timer has been terminated, its execution thread terminates
     * gracefully, and no more tasks may be scheduled on it.
     *
     * <p>Note that calling this method from within the run method of a
     * timer task that was invoked by this timer absolutely guarantees that
     * the ongoing task execution is the last task execution that will ever
     * be performed by this timer.
     *
     * <p>This method may be called repeatedly; the second and subsequent 
     * calls have no effect.
     */
    public void cancel() {
        synchronized (queue) {
            queue.newTasksMayBeScheduled = false;
            queue.clear();
            queue.notify();  // In case queue was already empty.
        }
    }

    private static long monotonicTimeFromStart(long monotonicTime) {
        return monotonicTime - monotonicClockOffset;
    }

    private static long userTimeFromStart(long userTime) {
        return userTime - userClockOffset;
    }

    static long relativeTimeToUserTime(long relativeTime) {
        return relativeTime + userClockOffset;
    }

    static long monotonicTimeMillis() {
        return monotonicTimeFromStart(JVM.monotonicTimeMillis());
    }

    /**
     * The millisecond threshold to signal user clock skew.
     * User clock deviations below the threshold are accumulated and reported
     * only when they exceed the threshold.
     */
    private static final int SKEW_THRESHOLD = 100;

    /**
     * The millisecond period to check for user clock skew.
     * Since many platforms do not notify applications about the user clock change,
     * we periodically check for the skew.
     */
    static final int USER_CLOCK_CHECK_PERIOD = 1000;

    /**
     * Returns the value of the user clock skew in milliseconds. 
     * Returned is the deviation of the user clock from the monotonic clock 
     * accumulated since the previous invocation of this method in this task.
     * Deviations below the <code>SKEW_THRESHOLD</code> are not reported.
     * They are accumulated until they exceed the threshold.
     *
     * @return the user clock skew
     */
    static long userClockSkew() {
        long newDelta = 
            System.currentTimeMillis() - JVM.monotonicTimeMillis();
        long oldDelta = 
            userClockOffset - monotonicClockOffset;
        long skew = newDelta - oldDelta;
        if (Math.abs(skew) < SKEW_THRESHOLD) {
            skew = 0;
        }

        userClockOffset += skew;
        return skew;
    }
}

/**
 * This "helper class" implements the timer's task execution thread, which
 * waits for tasks on the timer queue, executions them when they fire,
 * reschedules repeating tasks, and removes cancelled tasks and spent
 * non-repeating tasks from the queue.
 * <p>
 * The thread will timeout if no TimerTasks are scheduled for it within
 * a timeout period.  When it times out the thread exits leaving
 * the newTasksMayBeScheduled
 * boolean true.  If true and the thread is not alive it should be restarted as
 * in the Timer.sched method above.
 */
class TimerThread extends Thread {
    /**
     * Our Timer's queue.  We store this reference in preference to
     * a reference to the Timer so the reference graph remains acyclic.
     * Otherwise, the Timer would never be garbage-collected and this
     * thread would never go away.
     */
    private TaskQueue queue;

    /**
     * The number of milliseconds to wait after the timer queue is empty
     * before the thread exits. It will be restarted when the next TimerTask
     * is inserted.
     */
    private static final long THREAD_TIMEOUT = 30*1000L;
    /**
     * initialize the timer thread with a task queue.
     * @param queue queue of tasks for this timer thread.
     */
    TimerThread(TaskQueue queue) {
        this.queue = queue;
    }
    /** start the main processing loop.  */
    public void run() {
        try {
            mainLoop();
	    /*
	     * If mainLoop returns then thread timed out with no events
	     * in the queue.  The thread will quietly be restarted in sched()
	     * when the next TimerTask is queued.
	     */
        } catch (Throwable t) {
            // Someone killed this Thread, behave as if Timer cancelled
            synchronized (queue) {
                queue.newTasksMayBeScheduled = false;
                queue.clear();  // Eliminate obsolete references
            }
        }
    }

    /**
     * The main timer loop.  (See class comment.)
     */
    private void mainLoop() {
        while (true) {
            try {
                TimerTask task;
                boolean taskFired;
                synchronized (queue) {
                    // Wait for queue to become non-empty
                    // But no more than timeout value.
                    while (queue.isEmpty() && queue.newTasksMayBeScheduled) {
			queue.wait(THREAD_TIMEOUT);
			if (queue.isEmpty()) {
			    break;
			}
                    }
                    if (queue.isEmpty())
                        break; // Queue is empty and will forever remain; die

                    // Handle a possible change of the user clock
                    queue.checkUserClockChange();

                    // Queue nonempty; look at first evt and do the right thing
                    long currentTime, executionTime;
                    task = queue.getMin();
                    synchronized (task.lock) {
                        if (task.state == TimerTask.CANCELLED) {
                            queue.removeMin();
                            continue;  // No action required, poll queue again
                        }
                        currentTime = Timer.monotonicTimeMillis();
                        executionTime = task.nextExecutionTime;

                        if (taskFired = (executionTime <= currentTime)) {
                            if (task.period == 0) { // Non-repeating, remove
                                queue.removeMin();
                                task.state = TimerTask.EXECUTED;
                            } else { // Repeating task, reschedule
                                queue.rescheduleMin(
                                  task.period < 0 ? currentTime   - task.period
                                                : executionTime + task.period);
                            }
                        }
                    }
                    if (!taskFired) { // Task hasn't yet fired; wait
                        long timeout = executionTime - currentTime;
                        if (queue.hasUserClockTasks() && 
                            timeout > Timer.USER_CLOCK_CHECK_PERIOD) {
                            timeout = Timer.USER_CLOCK_CHECK_PERIOD;
                        }
                        queue.wait(timeout);
		    }
                }
                if (taskFired) { // Task fired; run it, holding no locks
		    try {
			task.run();
		    } catch (Exception e) {
			// Cancel tasks that cause exceptions
			task.cancel();
		    }
		}
            } catch (InterruptedException e) {
            }
        }
    }
}

/**
 * This class represents a timer task queue: a priority queue of TimerTasks,
 * ordered on nextExecutionTime.  Each Timer object has one of these, which it
 * shares with its TimerThread.  Internally this class uses a heap, which
 * offers log(n) performance for the add, removeMin and rescheduleMin
 * operations, and constant time performance for the getMin operation.
 */
class TaskQueue {
    /**
     * Priority queue represented as a balanced binary heap: the two children
     * of queue[n] are queue[2*n] and queue[2*n+1].  The priority queue is
     * ordered on the nextExecutionTime field: The TimerTask with the lowest
     * nextExecutionTime is in queue[1] (assuming the queue is nonempty).  For
     * each node n in the heap, and each descendant of n, d,
     * n.nextExecutionTime <= d.nextExecutionTime. 
     */
    private TimerTask[] queue = new TimerTask[4];

    /**
     * The number of tasks in the priority queue.  (The tasks are stored in
     * queue[1] up to queue[size]).
     */
    private int size = 0;

    /**
     * This flag is set to false by the reaper to inform us that there
     * are no more live references to our Timer object.  Once this flag
     * is true and there are no more tasks in our queue, there is no
     * work left for us to do, so we terminate gracefully.
     */
    boolean newTasksMayBeScheduled = true;

    /**
     * Adds a new task to the priority queue.
     * @param task to add to the current queue
     */
    void add(TimerTask task) {
        // Grow backing store if necessary
        if (++size == queue.length) {
            TimerTask[] newQueue = new TimerTask[2*queue.length];
            System.arraycopy(queue, 0, newQueue, 0, size);
            queue = newQueue;
        }

        queue[size] = task;
        fixUp(size);

        if (task.isUserClock) {
            userClockTaskAdded();
        }
    }

    /**
     * Return the "head task" of the priority queue.  (The head task is an
     * task with the lowest nextExecutionTime.)
     * @return the minimum head task of the queue.
     */
    TimerTask getMin() {
        return queue[1];
    }

    /**
     * Remove the head task from the priority queue.
     */
    void removeMin() {
        final TimerTask task = queue[1];

        queue[1] = queue[size];
        queue[size--] = null;  // Drop extra reference to prevent memory leak
        fixDown(1);

        if (task.isUserClock) {
            userClockTaskRemoved();
        }
    }

    /**
     * Sets the nextExecutionTime associated with the head task to the 
     * specified value, and adjusts priority queue accordingly.
     * @param newTime new time to apply to head task execution.
     */
    void rescheduleMin(long newTime) {
        final TimerTask task = queue[1];

        task.nextExecutionTime = newTime;
        fixDown(1);

        if (task.isUserClock) {
            // Only the first execution is scheduled against the user clock. 
            // Subsequent executions are scheduled based on delays.
            task.isUserClock = false;
            userClockTaskRemoved();
        }
    }

    /**
     * Returns true if the priority queue contains no elements.
     * @return true if queue is empty.
     */
    boolean isEmpty() {
        return size == 0;
    }

    /**
     * Removes all elements from the priority queue.
     */
    void clear() {
        // Null out task references to prevent memory leak
        for (int i = 1; i <= size; i++)
            queue[i] = null;

        size = 0;
        userClockTaskCount = 0;
    }

    /**
     * Establishes the heap invariant (described above) assuming the heap
     * satisfies the invariant except possibly for the leaf-node indexed by k
     * (which may have a nextExecutionTime less than its parent's).
     *
     * This method functions by "promoting" queue[k] up the hierarchy
     * (by swapping it with its parent) repeatedly until queue[k]'s
     * nextExecutionTime is greater than or equal to that of its parent.
     * @param k index of queued task to be promoted up in the queue.
     */
    private void fixUp(int k) {
        while (k > 1) {
            int j = k >> 1;
            if (queue[j].nextExecutionTime <= queue[k].nextExecutionTime)
                break;
            TimerTask tmp = queue[j];  queue[j] = queue[k]; queue[k] = tmp;
            k = j;
        }
    }

    /**
     * Establishes the heap invariant (described above) in the subtree
     * rooted at k, which is assumed to satisfy the heap invariant except
     * possibly for node k itself (which may have a nextExecutionTime greater
     * than its children's).
     *
     * This method functions by "demoting" queue[k] down the hierarchy
     * (by swapping it with its smaller child) repeatedly until queue[k]'s
     * nextExecutionTime is less than or equal to those of its children.
     * @param k index of queued task to be demoted in the queue.
     */
    private void fixDown(int k) {
        int j;
        while ((j = k << 1) <= size) {
            if (j < size &&
                queue[j].nextExecutionTime > queue[j+1].nextExecutionTime)
                j++; // j indexes smallest kid
            if (queue[k].nextExecutionTime <= queue[j].nextExecutionTime)
                break;
            TimerTask tmp = queue[j];  queue[j] = queue[k]; queue[k] = tmp;
            k = j;
        }
    }

    /**
     * The amount of active tasks scheduled against the user 
     * clock in this queue.
     */
    private int userClockTaskCount = 0;

    private void userClockTaskAdded() {
        userClockTaskCount++;
    }

    private void userClockTaskRemoved() {
        userClockTaskCount--;
    }

    boolean hasUserClockTasks() {
        return userClockTaskCount > 0;
    }

    void checkUserClockChange() {
        if (!hasUserClockTasks()) {
            return;
        }

        final long userClockSkew = Timer.userClockSkew();
        if (userClockSkew == 0) {
            return;
        }

        if (userClockSkew < 0) {
            for (int i = 1; i <= size; i++) {
                TimerTask task = queue[i];
                if (task != null && task.isUserClock) {
                    task.nextExecutionTime -= userClockSkew;
                    fixUp(i);
                }
            }
        } else {
            for (int i = size; i >= 1; i--) {
                TimerTask task = queue[i];
                if (task != null && task.isUserClock) {
                    task.nextExecutionTime -= userClockSkew;
                    fixDown(i);
                }
            }
        }
    }
}
