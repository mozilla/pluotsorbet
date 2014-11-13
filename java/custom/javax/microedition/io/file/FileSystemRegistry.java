package javax.microedition.io.file;

import java.util.Enumeration;
import java.util.Vector;

public class FileSystemRegistry {
    private FileSystemRegistry() {
    }

    public static boolean addFileSystemListener(FileSystemListener listener) {
        System.out.println("FileSystemRegistry::addFileSystemListener(FileSystemListener) not implemented.");
        return false;
    }

    public static boolean removeFileSystemListener(FileSystemListener listener) {
        System.out.println("FileSystemRegistry::removeFileSystemListener(FileSystemListener) not implemented.");
        return false;
    }

    public static Enumeration listRoots() {
        Vector roots = new Vector();
        roots.addElement("");
        return roots.elements();
    }
}

