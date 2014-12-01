package javax.microedition.io.file;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */
 
/**
 * This class is used for receiving status notification when
 * adding or removing a file system root. This can be achieved by inserting or
 * removing a card from a device or by mounting or unmounting file systems to
 * a device.
 *
 * @see FileConnection
 * @since FileConnection 1.0
 */
public interface FileSystemListener {

    /**
     * Constant indicating that a file system root has been added to the device.
     */
    public static final int ROOT_ADDED = 0;

    /**
     * Constant indicating that a file system root has been removed from the
     * device.
     */
    public static final int ROOT_REMOVED = 1;

    /**
     * This method is invoked when a root on the device has changed state.
     *
     * @param   state int representing the state change that has happened to
     *          the root.
     * @param   rootName the String name of the root, following the root naming
     *          conventions detailed in FileConnection.
     * @throws  IllegalArgumentException if <code>state</code> has a negative
     *          value or is not one of the legal acceptable constants.
     * @throws  NullPointerException if <code>rootName</code> is
     *          <code>null</code>.
     *
     * @see     FileConnection
     */
    public abstract void rootChanged(int state, String rootName);
}
