package Vault;

import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;
import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InvalidObjectException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Vault implements JSONSerializable {
    private String salt;
    private String rootPasswordHash;
    private JSONObject vaultKey;
    private JSONArray passwords;
    private JSONArray privKeys;

    public Vault() {
        if (this.salt == null) {
            generateSalt();
        }        
        this.rootPasswordHash = null; // Set when a root password is created
        this.vaultKey = new JSONObject();
        this.passwords = new JSONArray();
        this.privKeys = new JSONArray();
    }

    public String getRootPasswordHash() {
        return this.rootPasswordHash;
    }

    // ✅ Set Root Password (Hash & Save)
    public void setRootPassword(String password) {
        this.rootPasswordHash = hashPassword(password);
    }

    // ✅ Verify Password
    public boolean verifyRootPassword(String password) {
        return hashPassword(password).equals(rootPasswordHash);
    }

    // ✅ Hash Password Using SHA-256
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hashedBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing password", e);
        }
    }

    // 🔹 Generate a random Base64-encoded salt
    private String generateSalt() {
        byte[] saltBytes = new byte[16];
        new java.security.SecureRandom().nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    // 🔹 Convert Vault to JSON
    @Override
    public JSONType toJSONType() {
        JSONObject json = new JSONObject();
        json.put("salt", this.salt);
        json.put("rootPasswordHash", this.rootPasswordHash);
        json.put("vaultkey", this.vaultKey);  // ✅ Store as JSONObject
        json.put("passwords", this.passwords);
        json.put("privkeys", this.privKeys);
        return json;
    }

    // 🔹 Deserialize Vault from JSON
    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        if (!(jsonType instanceof JSONObject)) {
            throw new InvalidObjectException("Invalid JSON format for Vault.");
        }
        JSONObject json = (JSONObject) jsonType;

        this.salt = json.getString("salt");
        this.rootPasswordHash = json.getString("rootPasswordHash");
        this.vaultKey = json.getObject("vaultkey");
        this.passwords = json.getArray("passwords");
        this.privKeys = json.getArray("privkeys");
    }

    // 🔹 Set Vault Key
    public void setVaultKey(String iv, String key) {
        vaultKey.put("iv", iv);
        vaultKey.put("key", key);
    }

    // 🔹 Get Vault Key (as an object)
    public JSONObject getVaultKey() {
        return vaultKey;
    }

    // 🔹 Add a new password entry
    public void addPassword(String service, String user, String encryptedPass, String iv) {
        JSONObject passEntry = new JSONObject();
        passEntry.put("iv", iv);
        passEntry.put("service", service);
        passEntry.put("user", user);
        passEntry.put("pass", encryptedPass);
        passwords.add(passEntry);
    }

    // 🔹 Add a new private key entry
    public void addPrivateKey(String service, String privKey, String iv) {
        JSONObject privKeyEntry = new JSONObject();
        privKeyEntry.put("iv", iv);
        privKeyEntry.put("service", service);
        privKeyEntry.put("privkey", privKey);
        privKeys.add(privKeyEntry);
    }

    // 🔹 Save Vault to File
    public void saveVault(File file) {
        try {
            JsonIO.writeSerializedObject(this, file);
            System.out.println("Vault saved successfully.");
        } catch (FileNotFoundException e) {
            System.err.println("Error saving vault: " + e.getMessage());
        }
    }

    // 🔹 Load Vault from File
    public static Vault loadVault(File file) {
        if (file.exists()) {
            try {
                JSONObject jsonType = JsonIO.readObject(file);  // ✅ FIXED: Read using `readObject`
                Vault vault = new Vault();
                vault.deserialize(jsonType);
                System.out.println("Vault loaded successfully.");
                return vault;
            } catch (FileNotFoundException e) {
                System.err.println("Vault file not found: " + e.getMessage());
            } catch (Exception e) {
                System.err.println("Error loading vault: " + e.getMessage());
            }
        }
        System.out.println("Creating a new vault...");
        return new Vault();
    }
}