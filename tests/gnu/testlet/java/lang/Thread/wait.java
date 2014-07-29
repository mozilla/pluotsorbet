package gnu.testlet.java.lang.Thread;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class wait implements Testlet
{
    TestHarness th;
    Object o;
    Object done;

    class WaitForInterrupt extends Thread {
	public void run() {
	    synchronized (done) {
		done.notifyAll();
	    }
	    try {
		synchronized (o) {
		    o.wait();
		}
	    } catch (InterruptedException e) {
		th.check(true, "got interrupt");
		synchronized (done) {
		    done.notifyAll();
		}
		return;
	    }
	    th.check(false, "didn't get interrupt");
	    synchronized (done) {
		done.notifyAll();
	    }
	}
    }

    public void test (TestHarness th)
    {
	this.th = th;
	this.o = new Object();
	this.done = new Object();

	try {
	    synchronized (o) {
		synchronized (o) {
		    o.wait(1);
		}
	    }
	    th.check(true, "wait with timeout nested into monitors");

	    Thread t = new WaitForInterrupt();

	    synchronized (o) {
		t.start();
		synchronized (done) {
		    done.wait();
		}
	    }

	    //t.interrupt();

	    synchronized (done) {
		//done.wait();
	    }
	} catch (InterruptedException e) {
	    th.fail("unexpected InterruptedException");
	}
    }
}

