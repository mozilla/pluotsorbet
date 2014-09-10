package com.nokia.mid.s40.codec;

import java.io.IOException;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

public class TestDataEncodeDecode implements Testlet {
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
        encoder.putEnd(DataType.STRUCT, "struct1");
        encoder.putEnd(DataType.STRUCT, "struct2");
        encoder.putEnd(DataType.STRUCT, "struct3");

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

    public void test(TestHarness th) {
        try {
            testString(th);
            testMethod(th);
            testLong(th);
            testArray(th);
            testNested(th);
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}
