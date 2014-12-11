package com.nokia.mid.ui;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.TextField;

public class TestTextEditor extends Canvas implements Testlet {
    public void testConstraints(TestHarness th, int constraints) {
        TextEditor textEditor = new TextEditor("Hello, world!", 20, 0, 100, 24);

        th.check(textEditor.getContent(), "Hello, world!");
        th.check(textEditor.getMaxSize(), 20);
        th.check(textEditor.getCaretPosition(), 13);

        textEditor.setConstraints(constraints);
        th.check(textEditor.getConstraints(), constraints);

        th.check(textEditor.getWidth(), 100);
        th.check(textEditor.getHeight(), 24);

        textEditor.setContent("Helló, világ!");
        th.check(textEditor.getContent(), "Helló, világ!");

        th.check(textEditor.setMaxSize(22), 22);
        th.check(textEditor.getMaxSize(), 22);
        th.check(textEditor.getContent(), "Helló, világ!");
        th.check(textEditor.setMaxSize(5), 5);
        th.check(textEditor.getContent(), "Helló");
        th.check(textEditor.getCaretPosition(), 5);

        textEditor.delete(2, 2);
        th.check(textEditor.getContent(), "Heó");
        th.check(textEditor.getCaretPosition(), 2);

        textEditor.insert("ll", 2);
        th.check(textEditor.getContent(), "Helló");
        th.check(textEditor.getCaretPosition(), 4);

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

        th.check(textEditor.getPositionX(), 0);
        th.check(textEditor.getPositionY(), 0);
        textEditor.setPosition(10, 20);
        th.check(textEditor.getPositionX(), 10);
        th.check(textEditor.getPositionY(), 20);
        textEditor.setPosition(-10, -20);
        th.check(textEditor.getPositionX(), -10);
        th.check(textEditor.getPositionY(), -20);

        textEditor.setVisible(true);
        textEditor.setContent("XYZYZ");
        th.check(textEditor.getCaretPosition(), 5);
        textEditor.setCaret(3);
        th.check(textEditor.getCaretPosition(), 3);
        textEditor.setCaret(6);
        th.check(textEditor.getCaretPosition(), 5);
        textEditor.setCaret(0);
        th.check(textEditor.getCaretPosition(), 0);
        textEditor.setCaret(-1);
        th.check(textEditor.getCaretPosition(), 0);

        textEditor.setParent(null);
    }

    private native boolean isTextEditorReallyFocused();

    public void test(TestHarness th) {
        testConstraints(th, TextField.ANY);
        testConstraints(th, TextField.PASSWORD);
    }

    protected void paint(Graphics graphics) {}
}
