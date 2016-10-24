package info.blockchain.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;

public class AssertJson {

    public static void assertEqual(String a, String b) {

        JSONObject jsonA = new JSONObject(a);
        JSONObject jsonB = new JSONObject(b);

        if (!jsonB.keySet().containsAll(jsonA.keySet())) {
            Assert.assertTrue("json keys missing", false);
        }

        for (String key : jsonA.keySet()) {
            if (!(jsonB.get(key) instanceof JSONObject) && !(jsonB.get(key) instanceof JSONArray) && !(jsonB.get(key) instanceof String)) {
                System.out.println("Not String - "+key);
                Assert.assertEquals(jsonB.get(key), jsonA.get(key));
            } else if (jsonB.get(key) instanceof String){
                System.out.println("String - "+key);
                Assert.assertEquals(jsonB.getString(key), jsonA.getString(key));
            } else {
                //Skip json element (Have separate test)
                System.out.println("skipping - "+key);
            }
        }
    }
}
