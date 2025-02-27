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

import javax.crypto.SecretKey;


public class Vault implements JSONSerializable {
    private String salt;
    private String rootPasswordHash;
    private JSONArray passwords;
    private JSONArray privKeys;
    private String vaultKeyIV;   // Stores IV (Base64)
    private String vaultKeyValue;

    public Vault() {
        if (this.salt == null || this.salt.isEmpty()) {
            generateSalt();
        }     
        this.rootPasswordHash = null; // Set when a root password is created
        this.vaultKeyIV = "";
        this.vaultKeyValue = "";
        this.passwords = new JSONArray();
        this.privKeys = new JSONArray();
    }
    

    public String getRootPasswordHash() {
        return this.rootPasswordHash;
    }

    // ✅ Set Root Password (Hash & Save)
    public void setRootPassword(String password) {
    this.rootPasswordHash = hashPassword(password);

    // ✅ Generate and store a salt if missing
    if (this.salt == null || this.salt.isEmpty()) {
        byte[] saltBytes = VaultEncryption.generateRandomIV();
        this.salt = Base64.getEncoder().encodeToString(saltBytes);
    }

    // ✅ Generate random IV for encryption
    byte[] iv = VaultEncryption.generateRandomIV();
    this.vaultKeyIV = Base64.getEncoder().encodeToString(iv);

    try {
        // ✅ Generate a new vault key and encrypt it
        byte[] vaultKey = VaultEncryption.generateRandomKey();
        SecretKey rootKey = VaultEncryption.deriveRootKey(password, Base64.getDecoder().decode(this.salt));
        byte[] encryptedVaultKey = VaultEncryption.encryptAESGCM(vaultKey, rootKey, iv);

        this.vaultKeyValue = Base64.getEncoder().encodeToString(encryptedVaultKey);
    } catch (Exception e) {
        e.printStackTrace();
    }
}

    // ✅ Verify Password
    public boolean verifyRootPassword(String password) {
        try {
            SecretKey rootKey = VaultEncryption.deriveRootKey(password, Base64.getDecoder().decode(this.salt));
            byte[] decryptedVaultKey = VaultEncryption.decryptAESGCM(
                Base64.getDecoder().decode(this.vaultKeyValue), rootKey, Base64.getDecoder().decode(this.vaultKeyIV)
            );
    
            return decryptedVaultKey.length == 32; // ✅ Check if decryption was successful
        } catch (Exception e) {
            return false; //  If decryption fails, the password is incorrect
        }
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
        
        JSONObject vaultKeyObject = new JSONObject();
        vaultKeyObject.put("iv", this.vaultKeyIV);
        vaultKeyObject.put("key", this.vaultKeyValue);
        json.put("vaultkey", vaultKeyObject);


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

        this.salt = json.containsKey("salt") ? json.getString("salt") : null;
    
    // ✅ If salt is missing, generate a new one
    if (this.salt == null || this.salt.isEmpty()) {
        generateSalt();
    }

        this.rootPasswordHash = json.getString("rootPasswordHash");
        JSONObject vaultKeyJSON = json.getObject("vaultkey");
        if (vaultKeyJSON != null) {
            this.vaultKeyIV = vaultKeyJSON.getString("iv");
            this.vaultKeyValue = vaultKeyJSON.getString("key");
        } else {
            this.vaultKeyIV = "";
            this.vaultKeyValue = "";
        }

        this.passwords = json.getArray("passwords");
        this.privKeys = json.getArray("privkeys");
    }

    public void setVaultKey(String iv, String key) {
        this.vaultKeyIV = iv;
        this.vaultKeyValue = key;
    }
    
    public String getVaultKeyIV() {
        return this.vaultKeyIV;
    }
    
    public String getVaultKeyValue() {
        return this.vaultKeyValue;
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


    public JSONArray getPasswords() {
        // Implement the method to return the passwords stored in the vault
        // This is a placeholder implementation
        return new JSONArray();
    }
}