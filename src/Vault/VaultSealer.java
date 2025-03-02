package Vault;

import java.io.File;
import java.nio.file.Files;
import java.util.Base64;
import java.io.IOException;
import java.io.InvalidObjectException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;

public class VaultSealer implements JSONSerializable {
    private String salt;
    private JSONArray passwords;
    private JSONArray privKeys;
    private byte[] encryptedVaultKey;
    private String vaultKeyIV;
    private String rootPasswordHash;
    private boolean isSealed;
    private String userpassword;
    
    public VaultSealer(Vault vault, String userPassword) {
        System.out.println("🔍 Debug: Sealing Vault...");
        this.userpassword = userPassword;
        
        try {
            // Load vault data from the provided Vault object
            this.salt = vault.getSalt();
            this.passwords = vault.getPasswords();
            this.privKeys = vault.getPrivateKeys();
            this.vaultKeyIV = vault.getVaultKeyIV();

            byte[] saltBytes = Base64.getDecoder().decode(this.salt);

            // Derive the root key from the user password and salt
            SecretKey rootKey = VaultEncryption.deriveRootKey(userPassword, saltBytes);

            // Retrieve the vault key using the root key
            SecretKey vaultKey = VaultEncryption.getVaultKey(vault, rootKey);

            // Encrypt the in-memory vault data
            byte[] encryptedData = encryptVaultData(vaultKey);

            // Encrypt the vault key using the root key
            this.encryptedVaultKey = encryptVaultKey(rootKey, vaultKey);

            // Save the password hash to persist
            this.rootPasswordHash = vault.hashPassword(userPassword);

            // Convert the vault data to JSON
            JSONObject vaultData = createVaultJSON(encryptedData);

            // Delete the old vault.json file if it exists
            File oldVaultFile = new File("src/json/vault.json");
            if (oldVaultFile.exists()) {
                oldVaultFile.delete();
                System.out.println("🗑️ Old vault.json file deleted.");
            }

            // Write encrypted vault data to the new vault.json file
            writeToFile(vaultData);

            // Optionally return or log success
            System.out.println("✅ Vault sealed successfully.");

            this.isSealed = true;

        } catch (Exception e) {
            System.err.println("❌ Error sealing vault: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clear the password after use for security
            this.userpassword = null;
            //exit the program
            System.exit(0);
        }
    }

    private byte[] encryptVaultData(SecretKey vaultKey) throws Exception {
        byte[] vaultData = (new JSONObject().toString()).getBytes();
        byte[] iv = VaultEncryption.generateRandomIV();
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        return VaultEncryption.encryptAESGCM(vaultData, vaultKey, spec);
    }

    private byte[] encryptVaultKey(SecretKey rootKey, SecretKey vaultKey) throws Exception {
        byte[] iv = VaultEncryption.generateRandomIV();
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        return VaultEncryption.encryptAESGCM(vaultKey.getEncoded(), rootKey, spec);
    }

    private JSONObject createVaultJSON(byte[] encryptedData) {
        JSONObject json = new JSONObject();
        json.put("salt", this.salt);
        json.put("vaultKeyIV", this.vaultKeyIV);
        json.put("encryptedVaultKey", Base64.getEncoder().encodeToString(this.encryptedVaultKey));
        json.put("encryptedVaultData", Base64.getEncoder().encodeToString(encryptedData));
        json.put("rootPasswordHash", this.rootPasswordHash);
        return json;
    }

    private void writeToFile(JSONObject vaultData) throws Exception {
        File vaultFile = new File("src/json/vault.json");
        JsonIO.writeFormattedObject(this, vaultFile);
    }

    @Override
    public void deserialize(JSONType jsonType) throws InvalidObjectException {
        JSONObject json = (JSONObject) jsonType;
        try {
            this.salt = json.getString("salt");
            this.vaultKeyIV = json.getString("vaultKeyIV");
            this.encryptedVaultKey = Base64.getDecoder().decode(json.getString("encryptedVaultKey"));
            this.rootPasswordHash = json.getString("rootPasswordHash");
        } catch (Exception e) {
            throw new InvalidObjectException("Failed to deserialize VaultSealer: " + e.getMessage());
        }
    }

    @Override
    public JSONType toJSONType() {
        JSONObject json = new JSONObject();
        json.put("salt", this.salt);
        json.put("vaultKeyIV", this.vaultKeyIV);
        json.put("encryptedVaultKey", Base64.getEncoder().encodeToString(this.encryptedVaultKey));
        json.put("rootPasswordHash", this.rootPasswordHash);
        return json;
    }

    public boolean isVaultSealed() {
        return this.isSealed;
    }
}
