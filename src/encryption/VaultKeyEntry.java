package encryption;

import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;
import java.io.InvalidObjectException;

/**
 * Represents an entry storing the encrypted vault key and metadata.
 */
public class VaultKeyEntry {
    private String encryptedVaultKey; // Base64-encoded encrypted vault key
    private String salt; // Base64-encoded salt
    private String iv;   // Base64-encoded IV

    /**
     * Default constructor initializes fields to null.
     */
    public VaultKeyEntry() {
        this.encryptedVaultKey = null;
        this.salt = null;
        this.iv = null;
    }

    /**
     * Constructs a new VaultKeyEntry.
     * @param encryptedVaultKey Base64-encoded encrypted vault key.
     * @param salt Base64-encoded salt used for key derivation.
     * @param iv Base64-encoded IV for encryption.
     */
    public VaultKeyEntry(String encryptedVaultKey, String salt, String iv) {
        this.encryptedVaultKey = encryptedVaultKey;
        this.salt = salt;
        this.iv = iv;
    }

    /**
     * Constructs a VaultKeyEntry from a serialized JSON object.
     * @param obj the JSONObject to deserialize.
     * @throws InvalidObjectException if the JSON structure is invalid.
     */
    public VaultKeyEntry(JSONObject obj) throws InvalidObjectException {
        deserialize(obj);
    }

    public String getEncryptedVaultKey() {
        return encryptedVaultKey;
    }

    public String getSalt() {
        return salt;
    }

    public String getIv() {
        return iv;
    }

    public void setEncryptedVaultKey(String encryptedVaultKey) {
        this.encryptedVaultKey = encryptedVaultKey;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    /**
     * Serializes the object into a JSON formatted string.
     * @return JSON string representation of the object.
     */
    public String serialize() {
        return toJSONType().getFormattedJSON();
    }

    /**
     * Converts JSON data to a VaultKeyEntry object.
     * @param obj a JSON type to deserialize.
     * @throws InvalidObjectException if the JSON structure is invalid.
     */
    public void deserialize(JSONType obj) throws InvalidObjectException {
        JSONObject tmp;
        if (obj instanceof JSONObject) {
            tmp = (JSONObject) obj;
            if (tmp.containsKey("encryptedVaultKey"))
                encryptedVaultKey = tmp.getString("encryptedVaultKey");
            else
                throw new InvalidObjectException("Missing field: encryptedVaultKey");

            if (tmp.containsKey("salt"))
                salt = tmp.getString("salt");
            else
                throw new InvalidObjectException("Missing field: salt");

            if (tmp.containsKey("iv"))
                iv = tmp.getString("iv");
            else
                throw new InvalidObjectException("Missing field: iv");
        } else {
            throw new InvalidObjectException("Invalid JSON structure for VaultKeyEntry");
        }
    }

    /**
     * Converts the object to a JSON type.
     * @return a JSON object containing vault key data.
     */
    public JSONType toJSONType() {
        JSONObject obj = new JSONObject();
        obj.put("encryptedVaultKey", encryptedVaultKey);
        obj.put("salt", salt);
        obj.put("iv", iv);
        return obj;
    }
}
