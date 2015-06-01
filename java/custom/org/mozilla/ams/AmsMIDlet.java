package org.mozilla.ams;

import com.sun.cldc.isolate.Isolate;

import com.sun.midp.events.EventQueue;
import com.sun.midp.main.AmsUtil;
import com.sun.midp.main.MIDletProxy;
import com.sun.midp.main.MIDletProxyList;

import javax.microedition.midlet.MIDlet;

class MIDletLauncher implements Runnable {
    public void run() {
        init0();
    }

    private native void init0();
}

public class AmsMIDlet extends MIDlet
                       implements NativeAMSEventConsumer {
    public AmsMIDlet() {
    }

    public void startApp() {
        new NativeAMSEventListener(EventQueue.getEventQueue(), this);
        new Thread(new MIDletLauncher()).start();
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }

    public void handleNativeMIDletExecuteRequest(int suiteId, String className, String displayName, String arg0, String arg1, String arg2) {
            Isolate isolate = AmsUtil.startMidletInNewIsolate(suiteId,
                                                              className,
                                                              displayName,
                                                              arg0,
                                                              arg1,
                                                              arg2);
            if (!isBGMIDlet0(className)) {
                isolate.setPriority(Isolate.MAX_PRIORITY);
            }
    }

    public void handleNativeMIDletDestroyRequest(int suiteId, String className) {
        MIDletProxyList mpl = MIDletProxyList.getMIDletProxyList();
        if (null == mpl) {
            return;
        }

        MIDletProxy m = mpl.findMIDletProxy(suiteId, className);
        if (null == m) {
            System.out.println("Could not find MIDlet to destroy: " + suiteId + ", " + className);
            return;
        }

        m.destroyMidlet();
    }

    private native boolean isBGMIDlet0(String className);
}
