package midlets;

import javax.microedition.midlet.MIDlet;
import javax.microedition.content.ContentHandlerServer;
import javax.microedition.content.Invocation;
import javax.microedition.content.Registry;

public class ContentHandlerMIDlet extends MIDlet {
    public void startApp() {
        try {
            // Register MIDlet as a content handler
            Registry.getRegistry(getClass().getName())
                    .register(getClass().getName(),
                              new String[] { "image/jpeg", "image/png", "image/gif", "audio/amr", "audio/mp3", "video/3gpp", "video/mp4" },
                              null,
                              new String[] { "share" },
                              null,
                              null,
                              null);

            ContentHandlerServer chServer = Registry.getServer(getClass().getName());

            // Check if the MIDlet has been invoked
            Invocation invoc = chServer.getRequest(false);
            if (invoc == null) {
              System.out.println("Invocation is null");
              return;
            }

            String shareAction = invoc.getAction();
            System.out.println("Invocation action: " + shareAction);

            String[] shareArgs = invoc.getArgs();
            for (int i = 0; i < shareArgs.length; i++) {
              System.out.println("Invocation args[" + i + "]: " + shareArgs[i]);
            }

            invoc.setArgs(null);
            chServer.finish(invoc, Invocation.INITIATED);
        } catch (Exception e) {
            System.out.println("Unexpected exception: " + e);
        }
    }

    public void pauseApp() {
    }

    public void destroyApp(boolean unconditional) {
    }
};
