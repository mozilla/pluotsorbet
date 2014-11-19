package javax.microedition.io.file;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */
 
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Vector;

/**
 * The FileSystemRegistry is a central registry for file system listeners
 * interested in the adding and removing (or mounting and unmounting) of
 * file systems on a device.
 *
 * @see FileConnection
 * @see FileSystemListener
 * @since FileConnection 1.0
 */
public class FileSystemRegistry {
	
	// the registered listeners
	private static Vector listeners = new Vector();
	
	// whether the natives have been initialized
	private static boolean isInitialized = false;
	
	// used to synchronize init operation
	private static Object initLock = new Object();
	
	static {
		com.ibm.oti.connection.file.Util.init();
	}

    /* Prevent JavaDoc from generating default constructor */
    FileSystemRegistry() {
    }

    /**
     * This method is used to register a FileSystemListener that is
     * notified in case of adding and removing a new file system root.
     * Multiple file system listeners can be added.  If file
     * systems are not supported on a device, false is returned from
     * the method (this check is performed prior to security checks).
     *
     * @param   listener The new FileSystemListener to be registered in
     *          order to handle adding/removing file system roots.
     * @return  boolean indicating if file system listener was successfully
     *          added or not
     * @throws  SecurityException if application is not given permission
     *          to read files.
     * @throws  NullPointerException if listener is <code>null</code>.
     *
     * @see FileSystemListener
     * @see FileConnection
     */
    public static boolean addFileSystemListener(FileSystemListener listener) {
        if (listener==null)
        	throw new NullPointerException();
        if (listeners.contains(listener))
        	return false;
        
        listeners.addElement(listener);
        
        synchronized(initLock) {
	    	if (!isInitialized) {
	    		initImpl();
	    		isInitialized = true;
	    	}
        }
        
        return true;
    }


    /**
     * This method is used to remove a registered FileSystemListener.  If file
     * systems are not supported on a device, false is returned from
     * the method.
     *
     * @param listener The FileSystemListener to be removed.
     * @return  boolean indicating if file system listener was successfully
     *          removed or not
     * @throws  NullPointerException if listener is <code>null</code>.
     *
     * @see FileSystemListener
     * @see FileConnection
     */
    public static boolean removeFileSystemListener(FileSystemListener listener) {
		if (listener==null)
			throw new NullPointerException();
		
		int idxElement = listeners.indexOf(listener);
		if (idxElement==-1)
			return false;
        
		listeners.removeElementAt(idxElement);
        
		return true;
    }


    /**
     * This method returns the currently mounted root file systems on a device
	 * as String objects in an Enumeration.
     * If there are no roots available on the device, a zero length Enumeration
     * is returned.  If file systems are not supported on a device, a zero
     * length Enumeration is also returned (this check is performed prior to
     * security checks).
	 * <P>
	 * The first directory in the file URI is referred to as the <I>root</I>, 
	 * which corresponds to a logical mount point for a particular storage 
	 * unit or memory.  Root strings are defined by the platform or 
	 * implementation and can be a string of zero or more characters ("" can be
	 * a valid root string on some systems) followed by a trailing "/" to denote
	 * that the root is a directory. Each root string is guaranteed to uniquely 
	 * refer to a root. Root names are 
	 * device specific and are not required to adhere to any standard.  
	 * Examples of possible root strings and how to open them include:
	 * <table border=1>
	 * <tr><th>Possible Root Value</th><th>Opening the Root</th></tr>
	 * <tr><td>CFCard/</td><td>Connector.open("file:///CFCard/");</td></tr>
	 * <tr><td>SDCard/</td><td>Connector.open("file:///SDCard/");</td></tr>
	 * <tr><td>MemoryStick/</td><td>Connector.open("file:///MemoryStick/");</td></tr>
	 * <tr><td>C:/</td><td>Connector.open("file:///C:/");</td></tr>
	 * <tr><td>/</td><td>Connector.open("file:////");</td></tr>
	 * </table>
	 * <P>
	 * The following is a sample showing the use of listRoots to retrieve
	 * the available size of all roots on a device:
	 * <pre>
	 *   Enumeration rootEnum = FileSystemRegistry.listRoots();
	 *   while (e.hasMoreElements()) {
	 *      String root = (String) e.nextElement();
	 *      FileConnection fc = Connector.open("file:///" + root);
	 *      System.out.println(fc.availableSize());
	 *   } 
	 * </pre>
	 *
     * @return  an Eumeration of mounted file systems as String objects.
     * @throws  SecurityException if application is not given permission
     *          to read files.
     * @see     FileConnection
     */
    public static Enumeration listRoots() {
    	final String[] roots = getRootsImpl();
    	
        return new Enumeration() {
        	int i = 0;
			public boolean hasMoreElements() {
				return roots!=null && i<roots.length;
			}

			public Object nextElement() {
				if (!hasMoreElements()) throw new NoSuchElementException();
				return roots[i++];
			}
		};
    }
    
	/**
	 * Notifies all registered listeners that the given file system change
	 * has occured.
	 * 
	 * This method is called from a native.
	 */
    private static void notifyRootChanged(int state, String rootName) {
		for(int i=0; i<listeners.size(); i++) {
			FileSystemListener listener = (FileSystemListener)listeners.elementAt(i);
			try {
				listener.rootChanged(state, rootName);
			} catch (Exception e) {}
		}
    }
    
    /**
     * Initializes the natives and starts providing callbacks to notifyRootChanged() when
     * file system roots are added or removed.
     */
	private static native void initImpl();
	
	/**
	 * Returns the string representing the file system roots, or null if there are none.
	 */
    private static native String[] getRootsImpl();
    
}
