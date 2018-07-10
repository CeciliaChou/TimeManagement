package org.zx_xjr.timemanagement;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        try {
            JSONObject object = new JSONObject("{ \"a\" : 19 }");
            int a = object.getInt("a");
            assertEquals(a, 19);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}