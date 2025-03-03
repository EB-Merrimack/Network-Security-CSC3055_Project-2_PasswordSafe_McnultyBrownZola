/*package Vault;

import java.io.File;
import java.io.InvalidObjectException;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;


import java.io.File;
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
    private byte[] encryptedVaultKey;
    private String vaultKeyIV;
    private String rootPasswordHash;
    private byte[] decryptedVaultKey;
    private byte[] decryptedData;

    public VaultOpener(String userPassword) {
        try {
            System.out.println("🔍 Debug: Opening Vault...");

            // Load the vault.json file
            File vaultFile = new File("src/json/vault.json");
            JSONObject vaultData = JsonIO.readObject(vaultFile);

            // Get the salt and other data from the JSON
            this.salt = vaultData.getString("salt");
            this.vaultKeyIV = vaultData.getString("vaultKeyIV");
            this.encryptedVaultKey = Base64.getDecoder().decode(vaultData.getString("encryptedVaultKey"));
            this.rootPasswordHash = vaultData.getString("rootPasswordHash");

            // Derive the root key from the user password
            byte[] saltBytes = Base64.getDecoder().decode(this.salt);
            SecretKey rootKey = VaultEncryption.deriveRootKey(userPassword, saltBytes);

            // Decrypt the vault key using the root key
            this.decryptedVaultKey = decryptVaultKey(rootKey);

            // Decrypt the vault data using the decrypted vault key
            this.decryptedData = decryptVaultData(this.decryptedVaultKey);

            // Deserialize the vault data (passwords and private keys)
            JSONObject decryptedJson = new JSONObject();
            this.passwords = decryptedJson.getArray("passwords");
            this.privKeys = decryptedJson.getArray("privKeys");

            System.out.println("✅ Vault opened successfully.");

        } catch (Exception e) {
            System.err.println("❌ Error opening vault: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Decrypt the vault key using the root key
    private byte[] decryptVaultKey(SecretKey rootKey) throws Exception {
        GCMParameterSpec spec = new GCMParameterSpec(128, Base64.getDecoder().decode(this.vaultKeyIV));
        return VaultEncryption.decryptAESGCMopener(this.encryptedVaultKey, rootKey, spec);
    }

    // Decrypt the vault data using the decrypted vault key
    private byte[] decryptVaultData(byte[] decryptedVaultKey) throws Exception {
        // Prepare the GCM spec for vault data decryption
        GCMParameterSpec spec = new GCMParameterSpec(128, Base64.getDecoder().decode(this.vaultKeyIV));
        return VaultEncryption.decryptAESGCMvaultdata(this.encryptedVaultKey, decryptedVaultKey, spec);
    }
    /**
     * Deserialize the VaultOpener from JSON data. This method is part of the
     * JSONSerializable interface.
     *
     * @param jsonType the JSON data to deserialize
     * @throws InvalidObjectException if the JSON data is invalid
     */
    /*@Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        JSONObject json = (JSONObject) jsonType;
        try {
            this.salt = json.getString("salt");
            this.vaultKeyIV = json.getString("vaultKeyIV");
            this.encryptedVaultKey = Base64.getDecoder().decode(json.getString("encryptedVaultKey"));
            this.rootPasswordHash = json.getString("rootPasswordHash");
        } catch (Exception e) {
            throw new InvalidObjectException("Failed to deserialize VaultOpener: " + e.getMessage());
        }
    }

    /**
     * Serialize the VaultOpener to JSON data. This method is part of the
     * JSONSerializable interface.
     *
     * @return the JSON data representing the VaultOpener
     */
    /*@Override
    public JSONType toJSONType() {
        JSONObject json = new JSONObject();
        json.put("salt", this.salt);
        json.put("rootPasswordHash", this.rootPasswordHash);
        json.put("encryptedVaultData", Base64.getEncoder().encodeToString(this.decryptedData));
        JSONObject vaultKeyObject = new JSONObject();
        vaultKeyObject.put("iv", this.vaultKeyIV);
        vaultKeyObject.put("encryptedVaultKey", Base64.getEncoder().encodeToString(this.encryptedVaultKey));
        json.put("vaultkey", vaultKeyObject);
        return json;
    }
}*/
