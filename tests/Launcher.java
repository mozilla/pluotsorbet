import com.sun.midp.main.MIDletSuiteLoader;
import com.sun.midp.configurator.Constants;
import com.sun.midp.log.*;

public class Launcher {
    public static void main(final String args[]) {
        Logging.setReportLevel(Constants.LOG_INFORMATION);
        Logging.enableTrace(1);
        Logging.report(Logging.INFORMATION, LogChannels.LC_CORE, "launcher");

        // Start foreground midlet.
        Logging.report(Logging.INFORMATION, LogChannels.LC_CORE, "starting in foreground");
        try {
            MIDletSuiteLoader.main(args);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}