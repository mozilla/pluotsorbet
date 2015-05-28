// TODO: License info?

package com.sun.midp.main;

/**
 * TODO: Interface description
 */
public interface MIDletControllerNativeEventConsumer {
    /**
     * Processes NATIVE_MIDLET_EXECUTE_REQUEST
     *
     * TODO: Param list
     */
    public void handleNativeMidletExecuteEvent(
        int externalAppId,
        int id,
        String midlet,
        String displayName,
        String arg0,
        String arg1,
        String arg2,
        int memoryReserved,
        int memoryTotal,
        int priority,
        String profileName);

    /**
     * Processes NATIVE_MIDLET_DESTROY_REQUEST
     *
     * TODO: Param list
     */
    public void handleNativeMidletDestroyEvent(
        int suiteId,
        String midlet
    );


    /*
     * TODO: We can add these as we need them
     *
     * Processes NATIVE_MIDLET_RESUME_REQUEST
    public void handleNativeMidletResumeEvent();

     * Processes NATIVE_MIDLET_PAUSE_REQUEST
    public void handleNativeMidletPauseEvent();

     * Processes NATIVE_MIDLET_GETINFO_REQUEST
    public void handleNativeMidletGetInfoEvent();
    */
}
