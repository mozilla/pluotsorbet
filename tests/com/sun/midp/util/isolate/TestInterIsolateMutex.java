package com.sun.midp.util.isolate;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;
import com.sun.cldc.isolate.Isolate;
import com.sun.midp.security.*;

class SecurityTokenProvider {
    static private class SecurityTrusted implements ImplicitlyTrustedClass {};
    
    private static SecurityToken token = SecurityInitializer.requestToken(new SecurityTrusted());
    
    static SecurityToken getToken() {
        return token;
    }
}

class UnlockerIsolate {
    public static void main(String args[]) {
        SecurityToken t = SecurityTokenProvider.getToken();
        InterIsolateMutex m = InterIsolateMutex.getInstance(t, "mutex");
        try {
            m.unlock();
        } catch (RuntimeException e) {
            // Exception expected!
        }
    }
}

class LockerIsolate1 {
    public static void main(String args[]) {
        SecurityToken t = SecurityTokenProvider.getToken();
        InterIsolateMutex m = InterIsolateMutex.getInstance(t, "mutex");
        m.lock();
        m.unlock();
    }
}

class LockerIsolate2 {
    public static void main(String args[]) {
        SecurityToken t = SecurityTokenProvider.getToken();
        InterIsolateMutex m = InterIsolateMutex.getInstance(t, "mutex");
        m.lock();
        try {
            // Give the test some time to test waiting for the lock
            Thread.sleep(1000);
        } catch (Exception e) {}
        m.unlock();
    }
}

class LockerThread extends Thread {
    public void run() {
        SecurityToken t = SecurityTokenProvider.getToken();
        InterIsolateMutex m = InterIsolateMutex.getInstance(t, "mutex");

        m.lock();
        try {
            // Give the test some time to test waiting for the lock
            Thread.sleep(1000);
        } catch (Exception e) {}
        m.unlock();
    }
}

public class TestInterIsolateMutex implements Testlet {
    public int getExpectedPass() { return 3; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    public void test(TestHarness th) {
        SecurityToken t = SecurityTokenProvider.getToken();
        InterIsolateMutex m = InterIsolateMutex.getInstance(t, "mutex");

        // Unlock when mutex is unlocked
        try {
            m.unlock();
            th.fail("Exception expected");
        } catch (RuntimeException e) {
            th.check(e.getMessage(), "Mutex is not locked");
        }

        // Lock mutex
        m.lock();

        // Lock mutex again
        try {
            m.lock();
            th.fail("Exception expected");
        } catch (RuntimeException e) {
            th.check(e.getMessage(), "Attempting to lock mutex twice within the same Isolate");
        }

        // Unlock mutex
        m.unlock();

        // Lock mutex
        m.lock();

        try {
            // Start isolate that tries to unlock the mutex
            Isolate iso = new Isolate("com.sun.midp.util.isolate.UnlockerIsolate", new String[] {});
            iso.start();
            iso.waitForExit();

            // Unlock the mutex
            m.unlock();
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
        }

        try {
            // Start isolate that locks the mutex and terminates right away
            Isolate iso = new Isolate("com.sun.midp.util.isolate.LockerIsolate1", new String[] {});
            iso.start();
            iso.waitForExit();

            m.lock();
            m.unlock();
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
        }

        try {
            // Start isolate that locks the mutex for some time
            Isolate iso = new Isolate("com.sun.midp.util.isolate.LockerIsolate2", new String[] {});
            iso.start();

            while (true) {
                try {
                    m.unlock();
                    th.fail("Exception expected");
                } catch (RuntimeException e) {
                    if (e.getMessage().equals("Mutex is locked by different Isolate")) {
                        th.check(true, "Mutex is locked by different Isolate");
                        break;
                    }
                    Thread.yield();
                }
            }

            // Lock while locked by another isolate
            m.lock();

            iso.waitForExit();
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
        }

        m.unlock();

        try {
            Isolate iso1 = new Isolate("com.sun.midp.util.isolate.LockerIsolate1", new String[] {});
            Isolate iso2 = new Isolate("com.sun.midp.util.isolate.LockerIsolate2", new String[] {});

            iso2.start();
            iso1.start();

            m.lock();
            m.unlock();

            iso2.waitForExit();
            iso1.waitForExit();
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
        }

        try {
            Isolate iso = new Isolate("com.sun.midp.util.isolate.LockerIsolate2", new String[] {});
            LockerThread lockerThread = new LockerThread();

            lockerThread.start();

            try {
                Thread.yield();
            } catch (Exception e) {}

            iso.start();

            iso.waitForExit();
            lockerThread.join();
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
        }

        try {
            Isolate iso = new Isolate("com.sun.midp.util.isolate.LockerIsolate2", new String[] {});
            LockerThread lockerThread = new LockerThread();

            iso.start();

            try {
                Thread.yield();
            } catch (Exception e) {}

            lockerThread.start();

            iso.waitForExit();
            lockerThread.join();
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
        }
    }
}
