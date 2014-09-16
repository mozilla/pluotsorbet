package com.nokia.mid.ui;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

public class TestTextEditor extends Canvas implements Testlet {
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
        th.check(textEditor.getContent(), "Helló, világ!");
        th.check(textEditor.setMaxSize(5), 5);
        th.check(textEditor.getContent(), "Helló");

        textEditor.setSize(120, 28);
        th.check(textEditor.getWidth(), 120);
        th.check(textEditor.getHeight(), 28);

        th.check(textEditor.isVisible(), false);
        textEditor.setVisible(true);
        th.check(textEditor.isVisible(), true);
        textEditor.setVisible(false);
        th.check(textEditor.isVisible(), false);

        textEditor.setParent(this);
        th.check(textEditor.getParent(), this);

        th.check(textEditor.isMultiline(), false);
        textEditor.setMultiline(true);
        th.check(textEditor.isMultiline(), true);
        textEditor.setMultiline(false);
        th.check(textEditor.isMultiline(), false);

        th.check(textEditor.getBackgroundColor(), 0xFFFFFFFF);
        textEditor.setBackgroundColor(0xBBBBBBBB);
        th.check(textEditor.getBackgroundColor(), 0xBBBBBBBB);

        th.check(textEditor.getForegroundColor(), 0xFF000000);
        textEditor.setForegroundColor(0x33333333);
        th.check(textEditor.getForegroundColor(), 0x33333333);

        th.check(textEditor.getLineMarginHeight(), 0);
        th.check(textEditor.getContentHeight(), Font.getDefaultFont().getHeight());
        textEditor.setMultiline(true);
        th.check(textEditor.getContentHeight(), Font.getDefaultFont().getHeight());
        textEditor.setContent("A\nB");
        th.check(textEditor.getContentHeight(), Font.getDefaultFont().getHeight() * 2);
        textEditor.setContent("A\r\nB");
        th.check(textEditor.getContentHeight(), Font.getDefaultFont().getHeight() * 2);
        textEditor.setContent("A\nB\nC");
        th.check(textEditor.getContentHeight(), Font.getDefaultFont().getHeight() * 3);
    }

    protected void paint(Graphics graphics) {}
}
