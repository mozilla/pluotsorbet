package com.nokia.mid.impl.jms.core;

public class MIDletRegistry {
    private static MIDletRegistry instance = new MIDletRegistry();

    private MIDletRegistry() { }

    public static MIDletRegistry getMIDletRegistry() {
        return instance;
    }

    public Object getMIDletLocation(int id) {
        System.out.println("MIDletRegistry::getMIDletLocation not implemented: " + id);
        return null;
    }

    public MIDletSuite findMIDletSuite(String param1, String param2) {
        System.out.println("MIDletRegistry::findMIDletSuite not implemented.");
        return null;
    }
}

