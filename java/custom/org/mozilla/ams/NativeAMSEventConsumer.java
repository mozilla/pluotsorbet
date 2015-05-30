package org.mozilla.ams;

/**
 * NativeAMSEventConsumer
 *
 */
public interface NativeAMSEventConsumer {
    /**
     * Processes NATIVE_MIDLET_EXECUTE_REQUEST
     *
     * @param suiteId the suiteId of the MIDlet to launch
     * @param className the className of the MIDlet to launch
     * @param displayName the name to display for the running MIDlet
     * @param arg0 the first arg to pass to the MIDlet
     * @param arg1 the second arg to pass to the MIDlet
     * @param arg2 the third arg to pass to the MIDlet
     */
    public void handleNativeMIDletExecuteRequest(
        int suiteId,
        String className,
        String displayName,
        String arg0,
        String arg1,
        String arg2);

    /**
     * Processes NATIVE_MIDLET_DESTROY_REQUEST
     *
     * @param suiteId the suiteId of the MIDlet to destroy
     * @param className the className of the MIDlet to destroy
     */
    public void handleNativeMIDletDestroyRequest(
        int suiteId,
        String className);


    /*
     * TODO: We can add these as we need them
     *
     * Processes NATIVE_MIDLET_RESUME_REQUEST
    public void handleNativeMIDletResumeRequest();

     * Processes NATIVE_MIDLET_PAUSE_REQUEST
    public void handleNativeMIDletPauseRequest();

     * Processes NATIVE_MIDLET_GETINFO_REQUEST
    public void handleNativeMIDletGetInfoRequest();
    */
}
