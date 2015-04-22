package com.sun.midp.i18n;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestResourceConstants implements Testlet {
    public int getExpectedPass() { return 67; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }

    native static void setLanguage(String language);

    public void test(TestHarness th) {
        setLanguage("en-EN");
        th.check(Resource.getString(ResourceConstants.DONE), "Done");
        th.check(Resource.getString(ResourceConstants.ABSTRACTIONS_PIM_TODO), "PIM to-do list");

        int first = ResourceConstants.DONE;
        int last = ResourceConstants.ABSTRACTIONS_PIM_TODO;

        while (first <= last) {
            th.check(Resource.getString(first).length() > 0);
            first++;
        }

        // Test loading a string in a different language
        setLanguage("it-IT");
        th.check(Resource.getString(ResourceConstants.DONE), "Fatto");

        // Test loading a string from an unsupported language (should fallback to English)
        setLanguage("doesntexist");
        th.check(Resource.getString(ResourceConstants.DONE), "Done");
    }

}
