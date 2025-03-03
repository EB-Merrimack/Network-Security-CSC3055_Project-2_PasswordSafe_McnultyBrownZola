package Vault;

import java.io.File;
import java.io.InvalidObjectException;
import java.nio.file.Files;
import java.security.PrivateKey;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class VaultOpener implements JSONSerializable {
    private String salt;
    private JSONArray passwords;
    private JSONArray privKeys;
    private String rootPasswordHash;
    private String vaultKeyIV;
    private String vaultKeyValue;
    private byte[] decryptedData;
    private byte[] decryptedVaultKey;

    public VaultOpener(String userPassword) {
        try {
            // Load the encrypted vault file
            File vaultFile = new File("src/json/vault.json");
            String jsonString = new String(Files.readAllBytes(vaultFile.toPath()));
            JSONObject json = new JSONObject();

            // Deserialize the vault JSON
            this.salt = json.getString("salt");
            this.vaultKeyIV = json.getObject("vaultkey").getString("iv");
            this.vaultKeyValue = json.getObject("vaultkey").getString("key");
            this.rootPasswordHash = json.getString("rootPasswordHash");

            // Derive the root key using the user password and salt
            byte[] saltBytes = Base64.getDecoder().decode(this.salt);
            SecretKey rootKey = VaultEncryption.deriveRootKey(userPassword, saltBytes);

            // Decrypt the vault key using the root key
            this.decryptedVaultKey = decryptVaultKey(rootKey);

            // Decrypt the vault data using the decrypted vault key
            this.decryptedData = decryptVaultData(decryptedVaultKey);

            // Parse the decrypted data (we assume the format is the same as shown)
            JSONObject decryptedVault = new JSONObject();
            this.passwords = decryptedVault.getArray("passwords");
            this.privKeys = decryptedVault.getArray("privkeys");

            // Optionally log or return the decrypted vault information
            System.out.println("✅ Vault decrypted successfully.");

        } catch (Exception e) {
            System.err.println("❌ Error decrypting vault: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private byte[] decryptVaultKey(SecretKey rootKey) throws Exception {
        String ivBase64 = this.vaultKeyIV;
        byte[] iv = Base64.getDecoder().decode(ivBase64);
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);

        // Decrypt the vault key using the root key
        return VaultEncryption.decryptAESGCM(Base64.getDecoder().decode(this.vaultKeyValue), rootKey, spec);
    }

    private byte[] decryptVaultData(byte[] vaultKey) throws Exception {
        // Decrypt the vault data using the decrypted vault key
        String encryptedVaultDataBase64 = new String(Files.readAllBytes(new File("src/json/vault.json").toPath()));
        JSONObject json = new JSONObject();
        String encryptedVaultData = json.getString("encryptedVaultData");

        byte[] encryptedData = Base64.getDecoder().decode(encryptedVaultData);

        // Extract IV from the vault JSON
        String ivBase64 = json.getString("vaultKeyIV");
        byte[] iv = Base64.getDecoder().decode(ivBase64);
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);

        return VaultEncryption.decryptAESGCMopener(encryptedData, vaultKey, spec);
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        // Implementation of deserialization if necessary
        JSONObject json = (JSONObject) jsonType;
        try {
            this.salt = json.getString("salt");
            this.vaultKeyIV = json.getString("vaultKeyIV");
            this.rootPasswordHash = json.getString("rootPasswordHash");
        } catch (Exception e) {
            throw new InvalidObjectException("Failed to deserialize VaultDecryptor: " + e.getMessage());
        }
    }

    @Override
    public JSONType toJSONType() {
        // Return the decrypted vault data as JSON format
        JSONObject json = new JSONObject();
        json.put("salt", this.salt);
        json.put("rootPasswordHash", this.rootPasswordHash);
        json.put("passwords", this.passwords);
        json.put("privkeys", this.privKeys);
        JSONObject vaultKeyObject = new JSONObject();
        vaultKeyObject.put("iv", this.vaultKeyIV);
        vaultKeyObject.put("key", this.vaultKeyValue);
        json.put("vaultkey", vaultKeyObject);
        return json;
    }
}
