package com.nokia.mid.s40.codec;

import java.io.IOException;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestDataEncodeDecode implements Testlet {
    public int getExpectedPass() { return 92; }
    public int getExpectedFail() { return 0; }
    public int getExpectedKnownFail() { return 0; }
    void testString(TestHarness th) throws IOException {
        DataEncoder encoder = new DataEncoder("whatever");
        encoder.putStart(DataType.STRUCT, "event");
        encoder.put(DataType.STRING, "name", "value");
        encoder.putEnd(DataType.STRUCT, "event");

        byte[] data = encoder.getData();

        DataDecoder decoder = new DataDecoder("whatever", data, 0, data.length);
        th.check(decoder.getName(), "event");
        th.check(decoder.getType(), DataType.STRUCT);
        decoder.getStart(DataType.STRUCT);
        th.check(decoder.getName(), "name");
        th.check(decoder.getType(), DataType.STRING);
        th.check(decoder.getString(DataType.STRING), "value");
        decoder.getEnd(DataType.STRUCT);
    }

    void testMethod(TestHarness th) throws IOException {
        DataEncoder encoder = new DataEncoder("whatever");
        encoder.putStart(DataType.STRUCT, "event");
        encoder.put(DataType.METHOD, "name", "value");
        encoder.putEnd(DataType.STRUCT, "event");
        
        byte[] data = encoder.getData();
        
        DataDecoder decoder = new DataDecoder("whatever", data, 0, data.length);
        th.check(decoder.getName(), "event");
        th.check(decoder.getType(), DataType.STRUCT);
        decoder.getStart(DataType.STRUCT);
        th.check(decoder.getName(), "name");
        th.check(decoder.getType(), DataType.METHOD);
        th.check(decoder.getString(DataType.METHOD), "value");
        decoder.getEnd(DataType.STRUCT);
    }

    void testBoolean(TestHarness th) throws IOException {
        DataEncoder encoder = new DataEncoder("whatever");
        encoder.putStart(DataType.STRUCT, "event");
        encoder.put(DataType.BOOLEAN, "val1", true);
        encoder.put(DataType.BOOLEAN, "val2", false);
        encoder.putEnd(DataType.STRUCT, "event");

        byte[] data = encoder.getData();

        DataDecoder decoder = new DataDecoder("whatever", data, 0, data.length);
        th.check(decoder.getName(), "event");
        th.check(decoder.getType(), DataType.STRUCT);
        decoder.getStart(DataType.STRUCT);
        th.check(decoder.getName(), "val1");
        th.check(decoder.getType(), DataType.BOOLEAN);
        th.check(decoder.getBoolean());
        th.check(decoder.getName(), "val2");
        th.check(decoder.getType(), DataType.BOOLEAN);
        th.check(!decoder.getBoolean());
        decoder.getEnd(DataType.STRUCT);
    }

    void testLong(TestHarness th) throws IOException {
        DataEncoder encoder = new DataEncoder("whatever");
        encoder.putStart(DataType.STRUCT, "event");
        encoder.put(DataType.LONG, "l1", 42L);
        encoder.put(DataType.ULONG, "l2", 7L);
        encoder.putEnd(DataType.STRUCT, "event");
        
        byte[] data = encoder.getData();
        
        DataDecoder decoder = new DataDecoder("whatever", data, 0, data.length);
        th.check(decoder.getName(), "event");
        th.check(decoder.getType(), DataType.STRUCT);
        decoder.getStart(DataType.STRUCT);
        th.check(decoder.getName(), "l1");
        th.check(decoder.getType(), DataType.LONG);
        th.check(decoder.getInteger(DataType.LONG), 42L);
        th.check(decoder.getName(), "l2");
        th.check(decoder.getType(), DataType.ULONG);
        th.check(decoder.getInteger(DataType.ULONG), 7L);
        decoder.getEnd(DataType.STRUCT);
    }

    void testArray(TestHarness th) throws IOException {
        DataEncoder encoder = new DataEncoder("whatever");
        encoder.putStart(DataType.STRUCT, "event");
        encoder.putStart(DataType.ARRAY, "array");
        encoder.put(DataType.LONG, "l1", 42L);
        encoder.put(DataType.ULONG, "l2", 7L);
        encoder.putEnd(DataType.ARRAY, "array");
        encoder.putEnd(DataType.STRUCT, "event");

        byte[] data = encoder.getData();
        
        DataDecoder decoder = new DataDecoder("whatever", data, 0, data.length);
        th.check(decoder.getName(), "event");
        th.check(decoder.getType(), DataType.STRUCT);
        decoder.getStart(DataType.STRUCT);
        th.check(decoder.getName(), "array");
        th.check(decoder.getType(), DataType.ARRAY);
        decoder.getStart(DataType.ARRAY);
        th.check(decoder.getName(), "l1");
        th.check(decoder.getType(), DataType.LONG);
        th.check(decoder.getInteger(DataType.LONG), 42L);
        th.check(decoder.getName(), "l2");
        th.check(decoder.getType(), DataType.ULONG);
        th.check(decoder.getInteger(DataType.ULONG), 7L);
        decoder.getEnd(DataType.ARRAY);
        decoder.getEnd(DataType.STRUCT);
    }

    void testNested(TestHarness th) throws IOException {
        DataEncoder encoder = new DataEncoder("whatever");
        encoder.putStart(DataType.STRUCT, "struct1");
        encoder.putStart(DataType.STRUCT, "struct2");
        encoder.putStart(DataType.STRUCT, "struct3");
        encoder.put(DataType.LONG, "value", 42L);
        encoder.putEnd(DataType.STRUCT, "struct3");
        encoder.putEnd(DataType.STRUCT, "struct2");
        encoder.putEnd(DataType.STRUCT, "struct1");

        byte[] data = encoder.getData();

        DataDecoder decoder = new DataDecoder("whatever", data, 0, data.length);
        th.check(decoder.getName(), "struct1");
        th.check(decoder.getType(), DataType.STRUCT);
        decoder.getStart(DataType.STRUCT);

        th.check(decoder.getName(), "struct2");
        th.check(decoder.getType(), DataType.STRUCT);
        decoder.getStart(DataType.STRUCT);

        th.check(decoder.getName(), "struct3");
        th.check(decoder.getType(), DataType.STRUCT);
        decoder.getStart(DataType.STRUCT);

        th.check(decoder.getName(), "value");
        th.check(decoder.getType(), DataType.LONG);
        th.check(decoder.getInteger(DataType.LONG), 42L);

        decoder.getEnd(DataType.STRUCT);
        decoder.getEnd(DataType.STRUCT);
        decoder.getEnd(DataType.STRUCT);
    }

    void testList(TestHarness th) throws IOException {
        DataEncoder encoder = new DataEncoder("whatever");
        encoder.putStart(DataType.STRUCT, "event");
        encoder.putStart(DataType.LIST, "list");
        encoder.put(DataType.STRING, "str1", "val1");
        encoder.put(DataType.STRING, "str2", "val2");
        encoder.putEnd(DataType.LIST, "list");
        encoder.putEnd(DataType.STRUCT, "event");

        byte[] data = encoder.getData();

        DataDecoder decoder = new DataDecoder("whatever", data, 0, data.length);
        th.check(decoder.getName(), "event");
        th.check(decoder.getType(), DataType.STRUCT);
        decoder.getStart(DataType.STRUCT);
        th.check(decoder.getName(), "list");
        th.check(decoder.getType(), DataType.LIST);
        decoder.getStart(DataType.LIST);
        th.check(decoder.listHasMoreItems());
        th.check(decoder.listHasMoreItems());
        th.check(decoder.listHasMoreItems());
        th.check(decoder.getName(), "str1");
        th.check(decoder.getType(), DataType.STRING);
        th.check(decoder.getString(DataType.STRING), "val1");
        th.check(decoder.listHasMoreItems());
        th.check(decoder.getName(), "str2");
        th.check(decoder.getType(), DataType.STRING);
        th.check(decoder.getString(DataType.STRING), "val2");
        th.check(!decoder.listHasMoreItems());
        decoder.getEnd(DataType.LIST);
        decoder.getEnd(DataType.STRUCT);
    }

    void testMissingSimple(TestHarness th) throws IOException {
        DataEncoder encoder = new DataEncoder("whatever");
        encoder.putStart(DataType.STRUCT, "event");
        encoder.putStart(DataType.ARRAY, "arr");
        encoder.put(DataType.STRING, "str1", "val1");
        encoder.putEnd(DataType.ARRAY, "arr");
        encoder.putEnd(DataType.STRUCT, "event");

        byte[] data = encoder.getData();

        DataDecoder decoder = new DataDecoder("whatever", data, 0, data.length);
        th.check(decoder.getName(), "event");
        th.check(decoder.getType(), DataType.STRUCT);
        decoder.getStart(DataType.STRUCT);
        th.check(decoder.getName(), "arr");
        th.check(decoder.getType(), DataType.ARRAY);
        decoder.getStart(DataType.ARRAY);
        th.check(decoder.getName(), "str1");
        th.check(decoder.getType(), DataType.STRING);
        th.check(decoder.getString(DataType.STRING), "val1");
        try {
            decoder.getString(DataType.STRING);
            th.fail("Exception expected");
        } catch (IOException e) {
            th.check(e.getMessage(), "tag (" + DataType.STRING + ") invalid");
        }
        try {
            decoder.getInteger(DataType.ULONG);
            th.fail("Exception expected");
        } catch (IOException e) {
            th.check(e.getMessage(), "tag (" + DataType.ULONG + ") invalid");
        }
        decoder.getEnd(DataType.ARRAY);
        decoder.getEnd(DataType.STRUCT);
    }

    void testMissingStartEnd(TestHarness th) throws IOException {
        DataEncoder encoder = new DataEncoder("whatever");
        encoder.putStart(DataType.STRUCT, "event");
        encoder.put(DataType.STRING, "str1", "val1");
        encoder.putEnd(DataType.STRUCT, "event");

        byte[] data = encoder.getData();

        DataDecoder decoder = new DataDecoder("whatever", data, 0, data.length);
        th.check(decoder.getName(), "event");
        th.check(decoder.getType(), DataType.STRUCT);
        decoder.getStart(DataType.STRUCT);
        try {
            decoder.getStart(DataType.ARRAY);
            th.fail("Exception expected");
        } catch (IOException e) {
            th.check(e.getMessage(), "no start found " + DataType.ARRAY);
        }
        try {
            decoder.getStart(DataType.STRUCT);
            th.fail("Exception expected");
        } catch (IOException e) {
            th.check(e.getMessage(), "no start found " + DataType.STRUCT);
        }
        try {
            decoder.getEnd(DataType.ARRAY);
            th.fail("Exception expected");
        } catch (IOException e) {
            th.check(e.getMessage(), "no end found " + DataType.ARRAY);
        }
        th.check(decoder.getName(), "str1");
        th.check(decoder.getType(), DataType.STRING);
        th.check(decoder.getString(DataType.STRING), "val1");
        decoder.getEnd(DataType.STRUCT);
        try {
            decoder.getEnd(DataType.ARRAY);
            th.fail("Exception expected");
        } catch (IOException e) {
            th.check(e.getMessage(), "no end found " + DataType.ARRAY);
        }
    }

    void testEndTooLate(TestHarness th) throws IOException {
        DataEncoder encoder = new DataEncoder("whatever");
        encoder.putStart(DataType.STRUCT, "event");
        encoder.putStart(DataType.ARRAY, "array");
        encoder.put(DataType.STRING, "str1", "val1");
        encoder.putEnd(DataType.STRUCT, "event");
        encoder.putEnd(DataType.ARRAY, "array");

        byte[] data = encoder.getData();

        DataDecoder decoder = new DataDecoder("whatever", data, 0, data.length);
        th.check(decoder.getName(), "event");
        th.check(decoder.getType(), DataType.STRUCT);
        decoder.getStart(DataType.STRUCT);
        th.check(decoder.getName(), "array");
        th.check(decoder.getType(), DataType.ARRAY);
        decoder.getStart(DataType.ARRAY);
        try {
            decoder.getEnd(DataType.ARRAY);
            th.fail("Exception expected");
        } catch (IOException e) {
            th.check(e.getMessage(), "no end found " + DataType.ARRAY);
        }
        th.check(decoder.getName(), "str1");
        th.check(decoder.getType(), DataType.STRING);
        th.check(decoder.getString(DataType.STRING), "val1");
        try {
            decoder.getEnd(DataType.STRUCT);
        } catch (IOException e) {
            th.check(e.getMessage(), "no end found " + DataType.STRUCT);
        }
    }

    void testSkipOver(TestHarness th) throws IOException {
        DataEncoder encoder = new DataEncoder("whatever");
        encoder.putStart(DataType.STRUCT, "event");
        encoder.putStart(DataType.ARRAY, "array");
        encoder.put(DataType.STRING, "str1", "val1");
        encoder.put(DataType.STRING, "str2", "val2");
        encoder.put(DataType.STRING, "str3", "val3");
        encoder.putEnd(DataType.ARRAY, "array");
        encoder.put(DataType.STRING, "str4", "val4");
        encoder.putEnd(DataType.STRUCT, "event");

        byte[] data = encoder.getData();

        DataDecoder decoder = new DataDecoder("whatever", data, 0, data.length);
        th.check(decoder.getName(), "event");
        th.check(decoder.getType(), DataType.STRUCT);
        decoder.getStart(DataType.STRUCT);
        decoder.getStart(DataType.ARRAY);
        decoder.getEnd(DataType.ARRAY);
        th.check(decoder.getName(), "str4");
        th.check(decoder.getType(), DataType.STRING);
        th.check(decoder.getString(DataType.STRING), "val4");
        decoder.getEnd(DataType.STRUCT);
    }

    public void test(TestHarness th) {
        try {
            testString(th);
            testMethod(th);
            testBoolean(th);
            testLong(th);
            testArray(th);
            testNested(th);
            testList(th);
            testMissingSimple(th);
            testMissingStartEnd(th);
            testEndTooLate(th);
            testSkipOver(th);
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}
