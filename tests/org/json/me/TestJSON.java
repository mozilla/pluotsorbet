package org.json.me;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

import java.util.Vector;

public class TestJSON implements Testlet {
    public void test(TestHarness th) {
        try {
            String jsonStr = "{\"team\":\"Bursaspor\",\"manager\":\"Ertuğrul Sağlam\",\"year\":\"2010\"}";
            JSONObject obj = new JSONObject(jsonStr);
            th.check(obj.getString("team"), "Bursaspor");

            jsonStr = "[0,{\"1\":{\"2\":{\"3\":{\"4\":[5,{\"6\":7}]}}}}]";
            JSONArray array = new JSONArray(jsonStr);
            th.check("{\"1\":{\"2\":{\"3\":{\"4\":[5,{\"6\":7}]}}}}", array.get(1).toString());

            JSONObject obj2 = (JSONObject)array.get(1);
            th.check("{\"2\":{\"3\":{\"4\":[5,{\"6\":7}]}}}", obj2.get("1").toString());

            jsonStr = "{}";
            obj = new JSONObject(jsonStr);
            th.check("{}", obj.toString());

            jsonStr = "[5,]";
            array = new JSONArray(jsonStr);
            th.check("[5]", array.toString());

            jsonStr = "[5,,2]";
            array = new JSONArray(jsonStr);
            th.check("[5,null,2]", array.toString());

            jsonStr = "[\"hello\\bworld\\\"abc\\tdef\\\\ghi\\rjkl\\n123\\u4e2d\"]";
            array = new JSONArray(jsonStr);
            th.check("hello\bworld\"abc\tdef\\ghi\rjkl\n123中", array.get(0).toString());

            jsonStr = "{\"name\":";
            try {
                obj = new JSONObject(jsonStr);
                th.fail("Exception expected");
            } catch (JSONException e) {
                th.check(e.getMessage(), "Missing value. at character 7 of {\"name\":");
            }

            jsonStr = "{\"name\":}";
            try {
                obj = new JSONObject(jsonStr);
                th.fail("Exception expected");
            } catch (JSONException e) {
                th.check(e.getMessage(), "Missing value. at character 8 of {\"name\":}");
            }


            jsonStr = "{\"name";
            try {
                obj = new JSONObject(jsonStr);
                th.fail("Exception expected");
            } catch (JSONException e) {
                th.check(e.getMessage(), "Unterminated string at character 6 of {\"name");
            }


            jsonStr = "[[null, 123.45, \"a\\\tb c\"}, true]";
            try{
                array = new JSONArray(jsonStr);
                th.fail("Exception expected");
            } catch (JSONException e) {
                th.check(e.getMessage(), "Expected a ',' or ']' at character 25 of [[null, 123.45, \"a\\	b c\"}, true]");
            }

            array = new JSONArray();
            th.check("[]", array.toString());

            Vector testList = new Vector();
            testList.addElement("First item");
            testList.addElement("Second item");
            array = new JSONArray(testList);
            th.check("[\"First item\",\"Second item\"]", array.toString());
        } catch (Exception e) {
            th.fail("Unexpected exception: " + e);
            e.printStackTrace();
        }
    }
}
