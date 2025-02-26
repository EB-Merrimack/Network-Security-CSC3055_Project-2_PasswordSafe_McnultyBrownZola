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

    // Returns the data as a JSONObject
    public JSONObject getData() {
        return new JSONObject(this);
    }

    // Helper method to convert JSONObject to a Map
    private static Map<String, Object> jsonToMap(JSONObject json) {
        Map<String, Object> map = new HashMap<>();
        for (String key : json.keySet()) {
            try {
                map.put(key, json.get(key)); // Safe conversion of JSON to Map
            } catch (Exception e) {
                System.err.println("Error processing key: " + key + " - " + e.getMessage());
            }
        }
        return map;
    }
}
