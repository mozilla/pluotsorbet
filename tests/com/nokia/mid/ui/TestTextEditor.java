package com.nokia.mid.ui;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestTextEditor implements Testlet {
    public void test(TestHarness th) {
        TextEditor textEditor = new TextEditor("", "", 5, 0, 5, 5);
        th.check(textEditor.isVisible(), false);
        textEditor.setVisible(true);
        th.check(textEditor.isVisible(), true);
        textEditor.setVisible(false);
        th.check(textEditor.isVisible(), false);
    }
}
