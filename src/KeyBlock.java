import java.util.HashMap;
import java.util.Map;
import merrimackutil.json.types.JSONObject;

public class KeyBlock extends HashMap<String, Object> {
    public KeyBlock() {
        super();
    }

    public KeyBlock(JSONObject json) {
        super(jsonToMap(json)); // Convert JSONObject manually
    }

    public JSONObject getData() {
        return new JSONObject(this);
    }

    // Helper method to convert JSONObject to a Map
    private static Map<String, Object> jsonToMap(JSONObject json) {
        Map<String, Object> map = new HashMap<>();
        for (String key : json.keySet()) {
            map.put(key, json.get(key)); // Assuming json.get() returns valid Java types
        }
        return map;
    }
}
