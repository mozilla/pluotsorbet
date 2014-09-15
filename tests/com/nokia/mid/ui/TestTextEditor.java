package com.nokia.mid.ui;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestTextEditor implements Testlet {
    public void test(TestHarness th) {
        TextEditor textEditor = new TextEditor("Hello, world!", 20, 0, 100, 24);

        th.check(textEditor.getContent(), "Hello, world!");
        th.check(textEditor.getMaxSize(), 20);

        try {
            textEditor.getConstraints();
            th.fail("TextEditor::getConstraints() not implemented");
        } catch(RuntimeException ex) {
            th.check(ex.getMessage(), "TextEditor::getConstraints() not implemented");
        }

        try {
            textEditor.setConstraints(0);
            th.fail("TextEditor::setConstraints(int) not implemented");
        } catch(RuntimeException ex) {
            th.check(ex.getMessage(), "TextEditor::setConstraints(int) not implemented");
        }

        th.check(textEditor.getWidth(), 100);
        th.check(textEditor.getHeight(), 24);

        textEditor.setContent("Helló, világ!");
        th.check(textEditor.getContent(), "Helló, világ!");

        th.check(textEditor.setMaxSize(22), 22);
        th.check(textEditor.getMaxSize(), 22);

        textEditor.setSize(120, 28);
        th.check(textEditor.getWidth(), 120);
        th.check(textEditor.getHeight(), 28);

        th.check(textEditor.isVisible(), false);
        textEditor.setVisible(true);
        th.check(textEditor.isVisible(), true);
        textEditor.setVisible(false);
        th.check(textEditor.isVisible(), false);
    }
}
