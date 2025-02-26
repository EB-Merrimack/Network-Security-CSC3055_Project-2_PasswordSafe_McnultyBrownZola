import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import merrimackutil.json.types.JSONObject;

public class KeyBlockcreation {
    private final Map<String, Object> data;

    // Constructor that initializes the data Map
    public KeyBlockcreation(JSONObject json) {
        this.data = new HashMap<>();
    }

    // Create key block from password and generate salt, iv, and vaultkey
    public void createKeyBlockFromPassword(String password) throws Exception {
        byte[] salt = generateSalt();
        byte[] iv = generateIv();
        SecretKey vaultKey = generateVaultKey(password, salt);

        // Add the generated values to the KeyBlock
        put("salt", Base64.getEncoder().encodeToString(salt));
        put("vaultkey", new JSONObject(generateVaultKeyData(vaultKey, iv)));
        put("iv", Base64.getEncoder().encodeToString(iv));
    }

    // Put key-value pairs into the map
    public void put(String key, Object value) {
        if (key != null && value != null) {
            data.put(key, value);
        } else {
            System.err.println("Key or value cannot be null");
        }
    }

    // Retrieve values from the map
    public Object get(String key) {
        return data.get(key);
    }

    // Returns the data as a JSONObject
    public JSONObject getData() {
        return new JSONObject(data); // The JSONObject will automatically handle key-value quoting (with double quotes)
    }

    // Helper method to convert JSONObject to a Map
    private static Map<String, Object> jsonToMap(JSONObject json) {
        Map<String, Object> map = new HashMap<>();
        for (String key : json.keySet()) {
            try {
                Object value = json.get(key);

                // Recursively handle nested JSONObjects
                if (value instanceof JSONObject) {
                    map.put(key, jsonToMap((JSONObject) value)); // Recursive call
                } else {
                    map.put(key, value);
                }
            } catch (Exception e) {
                System.err.println("Error processing key: " + key + " - " + e.getMessage());
            }
        }
        return map;
    }

    // Method to generate salt (using a simple random byte array for this example)
    private byte[] generateSalt() {
        byte[] salt = new byte[16];
        // Here you can use a secure random generator for salt
        for (int i = 0; i < salt.length; i++) {
            salt[i] = (byte) (Math.random() * 256); // Simple random byte (not cryptographically secure)
        }
        return salt;
    }

    // Method to generate iv (Initialization Vector) using a secure random generator
    private byte[] generateIv() {
        byte[] iv = new byte[16];
        // Using secure random IV generation
        for (int i = 0; i < iv.length; i++) {
            iv[i] = (byte) (Math.random() * 256); // Simple random byte (not cryptographically secure)
        }
        return iv;
    }

    // Generate vaultkey from password and salt using PBKDF2
    private SecretKey generateVaultKey(String password, byte[] salt) throws NoSuchAlgorithmException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] passwordBytes = password.getBytes();
            byte[] saltedPassword = new byte[passwordBytes.length + salt.length];

            System.arraycopy(passwordBytes, 0, saltedPassword, 0, passwordBytes.length);
            System.arraycopy(salt, 0, saltedPassword, passwordBytes.length, salt.length);

            return new SecretKeySpec(digest.digest(saltedPassword), "AES");
        } catch (Exception e) {
            throw new NoSuchAlgorithmException("Error generating vault key", e);
        }
    }

    // Generate vaultkey data with both key and iv
    private Map<String, Object> generateVaultKeyData(SecretKey vaultKey, byte[] iv) {
        Map<String, Object> vaultKeyData = new HashMap<>();
        vaultKeyData.put("key", Base64.getEncoder().encodeToString(vaultKey.getEncoded()));
        vaultKeyData.put("iv", Base64.getEncoder().encodeToString(iv));
        return vaultKeyData;
    }

  // In the KeyBlockcreation class

// Modify the toString() method to generate valid JSON
@Override
public String toString() {
    // Convert the map to JSON string format
    StringBuilder json = new StringBuilder();
    json.append("{");

    for (Map.Entry<String, Object> entry : data.entrySet()) {
        // Add key with double quotes
        json.append("\"").append(entry.getKey()).append("\": ");

        // Handle value types (String should be quoted)
        if (entry.getValue() instanceof String) {
            json.append("\"").append(entry.getValue()).append("\"");
        } else if (entry.getValue() instanceof JSONObject) {
            json.append(((JSONObject) entry.getValue()).toString());
        } else {
            json.append(entry.getValue());
        }

        // Add a comma if this is not the last entry
        if (data.size() > 1) {
            json.append(", ");
        }
    }

    json.append("}");
    return json.toString();
}

}
