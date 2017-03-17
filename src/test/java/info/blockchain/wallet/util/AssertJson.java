package info.blockchain.wallet.util;

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

        for (Object keyO : jsonA.keySet()) {
            String key = (String)keyO;
            if (!(jsonB.get(key) instanceof JSONObject) && !(jsonB.get(key) instanceof JSONArray) && !(jsonB.get(key) instanceof String)) {
                Assert.assertEquals(jsonA.toString(4)+ "\n"+ jsonB.toString(4),jsonB.get(key), jsonA.get(key));
            } else if (jsonB.get(key) instanceof String){
                Assert.assertEquals(jsonA.toString(4)+ "\n"+ jsonB.toString(4),jsonB.getString(key), jsonA.getString(key));
            } else {
                //Skip json element (Have separate test)
            }
        }
    }
}
