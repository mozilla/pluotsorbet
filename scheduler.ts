module J2ME {

  /** @const */ export var MAX_PRIORITY: number = 10;
  /** @const */ export var MIN_PRIORITY: number = 1;
  /** @const */ export var NORMAL_PRIORITY: number = 5;

  /** @const */ export var ISOLATE_MIN_PRIORITY: number = 1;
  /** @const */ export var ISOLATE_NORM_PRIORITY: number = 2;
  /** @const */ export var ISOLATE_MAX_PRIORITY: number = 3;

  /**
   * Maximum time in ms that all of the threads have to run before the event
   * loop is run.
   * NOTE: this number is somewhat arbitrarily chosen, but the thought is
   * we'd like the system to respond in under 100ms, so by using 80ms we then
   * have 20ms to get an event, start processing it, and render an update.
   * @const
   */
  var MAX_WINDOW_EXECUTION_TIME: number = 80;

  /**
   * Number of ms between preemption checks, chosen arbitrarily.
   * @const
   */
  var PREEMPTION_INTERVAL: number = 5;

  /**
   * Time when the last preemption check was allowed.
   */
  var lastPreemptionCheck: number = 0;

  /**
   * Number of preemption checks thus far.
   */
  export var preemptionCount: number = 0;

  /**
   * Time when the window began execution.
   * @type {number}
   */
  var windowStartTime: number = 0;

  /**
   * Time used to track thread execution time. This is updated when the thread starts
   * in the execution window and when `updateCurrentRuntime` is called.
   */
  var threadTrackingTime: number = 0;

  /**
   * Used to block preemptions from happening during code that can't handle them.
   */
  export var preemptionLockLevel: number = 0;

  /**
   * All of the currently runnable threads. Sorted in ascending order by virtualRuntime.
   * @type {Array}
   */
  var runningQueue: Context [] = [];

  /**
   * The smallest virtual runtime of all the currently executing threads. This number is
   * monotonically increasing.
   */
  var minVirtualRuntime: number = 0;

  /**
   * True when a setTimeout has been scheduled to run the threads.
   */
  var processQueueScheduled: boolean = false;

  /**
   * The currently executing context.
   */
  var current: Context = null;

  /**
   * Rate the virtual runtime increases.
   */
  var currentTimeScale: number = 1;


  /**
   * The scheduler tracks the amount of time(virtualRuntime) that each thread has had to execute
   * and tries to always execute the thread that has had least amount of time to run next.
   * For higher priority threads the virtual runtime is increased at a slower rate to give them
   * more time to be the the front of the queue and vice versa for low priority threads. To allow
   * the event loop a turn there is an overall MAX_WINDOW_EXECUTION_TIME that if reached will yield
   * all the threads and schedule them to resume on a setTimeout. This allows us to run up to
   * MAX_WINDOW_EXECUTION_TIME/PREEMPTION_INTERVAL threads per execution window.
   */
  export class Scheduler {

    static enqueue(ctx: Context, directExecution?: boolean) {
      if (ctx.virtualRuntime === 0) {
        // Ensure the new thread doesn't dominate.
        ctx.virtualRuntime = minVirtualRuntime;
      }
      runningQueue.unshift(ctx);
      runningQueue.sort(function(a: Context, b: Context) {
        return a.virtualRuntime - b.virtualRuntime;
      });
      Scheduler.updateMinVirtualRuntime();
      Scheduler.processRunningQueue(directExecution);
    }

    private static processRunningQueue(directExecution?: boolean) {
      function run() {
        processQueueScheduled = false;
        try {
          windowStartTime = performance.now();
          while (runningQueue.length) {
            var now = performance.now();
            if (now - windowStartTime >= MAX_WINDOW_EXECUTION_TIME) {
              break;
            }
            var ctx = runningQueue.shift();
            threadTrackingTime = lastPreemptionCheck = now;
            current = ctx;
            /*
             * The current scaling is a simple linear function where the scale goes from 1x to .1x for lowest
             * priority to highest priority.
             * NOTE: this should be tuned.
             * RUNTIME THREAD SCALE
             * low     low    1x
             * norm    norm   0.72x
             * high    high   .1x
             */
            currentTimeScale = -0.03103448276 * (ctx.priority * ctx.runtime.priority) + 1.031034483;
            ctx.execute();
            Scheduler.updateCurrentRuntime();
            current = null;
          }
        } finally {
          if (runningQueue.length) {
            Scheduler.processRunningQueue();
          }
        }
      }

      if (directExecution) {
        run();
        return;
      }
      if (processQueueScheduled) {
        return;
      }
      processQueueScheduled = true;
      (<any>window).setTimeout(run);
    }

    private static updateMinVirtualRuntime() {
      var virtualRuntime = minVirtualRuntime;

      if (current) {
        virtualRuntime = current.virtualRuntime;
      }

      if (runningQueue.length) {
        var nextContext = runningQueue[0];
        if (!current) {
          virtualRuntime = nextContext.virtualRuntime;
        } else {
          virtualRuntime = Math.min(virtualRuntime, nextContext.virtualRuntime);
        }
      }

      minVirtualRuntime = Math.max(minVirtualRuntime, virtualRuntime);
    }

    private static updateCurrentRuntime() {
      var now = performance.now();
      var ctx = current;
      var executionTime = now - threadTrackingTime;
      var weightedExecutionTime = executionTime * currentTimeScale;
      ctx.virtualRuntime += weightedExecutionTime;
      threadTrackingTime = now;
      Scheduler.updateMinVirtualRuntime()
    }

    static shouldPreempt(): boolean {
      if (preemptionLockLevel > 0) {
        return false;
      }
      var now = performance.now();
      var totalElapsed = now - windowStartTime;
      if (totalElapsed > MAX_WINDOW_EXECUTION_TIME) {
        preemptionCount++;
        threadWriter && threadWriter.writeLn("Execution window timeout: " + totalElapsed.toFixed(2) + " ms, samples: " + PS + ", count: " + preemptionCount);
        return true;
      }

      Scheduler.updateCurrentRuntime();

      var elapsed = now - lastPreemptionCheck;
      if (elapsed < PREEMPTION_INTERVAL) {
        return false;
      }

      lastPreemptionCheck = now;

      if (runningQueue.length === 0) {
        return false;
      }

      if ($.ctx.virtualRuntime > runningQueue[0].virtualRuntime) {
        preemptionCount++;
        threadWriter && threadWriter.writeLn("Preemption: " + elapsed.toFixed(2) + " ms, samples: " + PS + ", count: " + preemptionCount);
        return true;
      }

      return false;
    }
  }
}
