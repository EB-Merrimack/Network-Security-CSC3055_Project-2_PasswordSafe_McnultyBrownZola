/*package Vault;

import java.io.File;
import java.nio.file.Files;
import java.security.PrivateKey;
import java.util.Base64;
import java.io.IOException;
import java.io.InvalidObjectException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

import Gui.LoginPanel;
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
    private byte[] encryptedData;
    private String vaultKeyValue;

    public VaultSealer(Vault vault, String userPassword) {
        System.out.println("🔍 Debug: Sealing Vault...");
        this.userpassword = userPassword;

        try {
            // Load vault data from the provided Vault object
            this.salt = vault.getSalt();
            this.passwords = vault.getPasswords();
            this.privKeys = vault.getPrivateKeys();
            this.vaultKeyIV = vault.getVaultKeyIV();
            this.vaultKeyValue = vault.getVaultKeyValue();

            byte[] saltBytes = Base64.getDecoder().decode(this.salt);

            // Derive the root key from the user password and salt
            SecretKey rootKey = VaultEncryption.deriveRootKey(userPassword, saltBytes);

            // Retrieve the vault key using the root key
            SecretKey vaultKey = VaultEncryption.getVaultKey(vault, rootKey);

            // Encrypt the in-memory vault data (including passwords and private keys)
            this.encryptedData = encryptVaultData(vaultKey);

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

        } catch (Exception e) {
            System.err.println("❌ Error sealing vault: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clear the password after use for security
            this.userpassword = null;
            // Exit the program
            System.exit(0);
        }
    }

    // Encrypt vault data, including passwords and private keys
    private byte[] encryptVaultData(SecretKey vaultKey) throws Exception {
        // Prepare data for encryption (include passwords and private keys)
        JSONArray vaultData = new JSONArray();

        // Encrypt private keys
        for (int i = 0; i < this.privKeys.size(); i++) {
            JSONObject privKey = (JSONObject) this.privKeys.get(i);
            String privKeyData = privKey.getString("privkey");
            String service = privKey.getString("service");
            String iv = privKey.getString("iv");

            byte[] encryptedPrivKey = encryptData(privKeyData, vaultKey, iv);
            if (encryptedPrivKey == null) {
                throw new Exception("Encryption of private key failed for service: " + service);
            }

            JSONObject encryptedPrivKeyObj = new JSONObject();
            encryptedPrivKeyObj.put("privkey", Base64.getEncoder().encodeToString(encryptedPrivKey));
            encryptedPrivKeyObj.put("service", service);
            encryptedPrivKeyObj.put("iv", iv);

            vaultData.add(encryptedPrivKeyObj);
        }

        // Encrypt passwords
        for (int i = 0; i < this.passwords.size(); i++) {
            JSONObject password = (JSONObject) this.passwords.get(i);
            String passData = password.getString("pass");
            String service = password.getString("service");
            String iv = password.getString("iv");

            byte[] encryptedPass = encryptData(passData, vaultKey, iv);
            if (encryptedPass == null) {
                throw new Exception("Encryption of password failed for service: " + service);
            }

            JSONObject encryptedPassObj = new JSONObject();
            encryptedPassObj.put("pass", Base64.getEncoder().encodeToString(encryptedPass));
            encryptedPassObj.put("service", service);
            encryptedPassObj.put("iv", iv);

            vaultData.add(encryptedPassObj);
        }

        // Return the encrypted vault data as byte array
        return vaultData.toString().getBytes();
    }

    // Helper method to encrypt data
    private byte[] encryptData(String data, SecretKey vaultKey, String iv) throws Exception {
        byte[] ivBytes = Base64.getDecoder().decode(iv);
        GCMParameterSpec spec = new GCMParameterSpec(128, ivBytes);
        return VaultEncryption.encryptAESGCM(data.getBytes(), vaultKey, spec);
    }

    // Encrypt vault key with the root key
    private byte[] encryptVaultKey(SecretKey rootKey, SecretKey vaultKey) throws Exception {
        byte[] iv = VaultEncryption.generateRandomIV();
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        return VaultEncryption.encryptAESGCM(vaultKey.getEncoded(), rootKey, spec);
    }

    // Create the vault JSON, including encrypted passwords and private keys
    private JSONObject createVaultJSON(byte[] encryptedData) {
        JSONObject json = new JSONObject();
        json.put("salt", this.salt);
        json.put("vaultKeyIV", this.vaultKeyIV);
        json.put("encryptedVaultKey", Base64.getEncoder().encodeToString(this.encryptedVaultKey));
        json.put("encryptedVaultData", Base64.getEncoder().encodeToString(encryptedData));
        json.put("rootPasswordHash", this.rootPasswordHash);
        return json;
    }

    // Write the encrypted vault data to a file
    private void writeToFile(JSONObject vaultData) throws Exception {
        File vaultFile = new File("src/json/vault.json");
        JsonIO.writeFormattedObject(this, vaultFile);
    }

    /**
     * Deserialize the VaultSealer from JSON data. This method is part of the
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
            throw new InvalidObjectException("Failed to deserialize VaultSealer: " + e.getMessage());
        }
    }

    /**
     * Serialize the VaultSealer to JSON data. This method is part of the
     * JSONSerializable interface.
     *
     * @return the JSON data representing the VaultSealer
     */
    /*@Override
    public JSONType toJSONType() {
        JSONObject json = new JSONObject();
        json.put("salt", this.salt);
        json.put("rootPasswordHash", this.rootPasswordHash);
        json.put("encryptedVaultData", Base64.getEncoder().encodeToString(this.encryptedData));
        JSONObject vaultKeyObject = new JSONObject();
        vaultKeyObject.put("iv", this.vaultKeyIV);
        vaultKeyObject.put("encryptedVaultKey", Base64.getEncoder().encodeToString(this.encryptedVaultKey));
        json.put("vaultkey", vaultKeyObject);
        return json;
    }
}*/
