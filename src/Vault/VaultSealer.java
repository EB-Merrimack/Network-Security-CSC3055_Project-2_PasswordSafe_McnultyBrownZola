package Vault;

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
    
                // Encrypt the in-memory vault data
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
            //exit the program
            System.exit(0);
        }
    }
// Encrypt the vault data
private byte[] encryptVaultData(SecretKey vaultKey) throws Exception {
    JSONArray encryptedPrivKeys = new JSONArray();
    JSONArray encryptedPasswords = new JSONArray();

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

        encryptedPrivKeys.add(encryptedPrivKeyObj);
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

        encryptedPasswords.add(encryptedPassObj);
    }



    // Convert encrypted data into JSON
    JSONObject vaultData = new JSONObject();
    vaultData.put("salt", this.salt);
    vaultData.put("vaultKeyIV", this.vaultKeyIV);
    vaultData.put("encryptedVaultKey", Base64.getEncoder().encodeToString(this.encryptedVaultKey));
    vaultData.put("privkeys", encryptedPrivKeys);
    vaultData.put("passwords", encryptedPasswords);
    vaultData.put("rootPasswordHash", this.rootPasswordHash);

    // Return the JSON data as bytes
    return vaultData.toString().getBytes();
}

// Check if encryptedVaultKey is valid before encoding it
private JSONObject createVaultJSON(byte[] encryptedData) throws Exception {
    if (this.encryptedVaultKey == null || encryptedData == null) {
        throw new Exception("Vault key or data is null. Cannot create JSON.");
    }

    JSONObject json = new JSONObject();
    json.put("salt", this.salt);
    json.put("vaultKeyIV", this.vaultKeyIV);
    json.put("encryptedVaultKey", Base64.getEncoder().encodeToString(this.encryptedVaultKey));
    json.put("encryptedVaultData", Base64.getEncoder().encodeToString(encryptedData));
    json.put("rootPasswordHash", this.rootPasswordHash);

    return json;
}

 private byte[] encryptData(String data, SecretKey key, String iv) throws Exception {
        GCMParameterSpec spec = new GCMParameterSpec(128, Base64.getDecoder().decode(iv));
        return VaultEncryption.encryptAESGCM(data.getBytes(), key, spec);
    }
    private byte[] encryptVaultKey(SecretKey rootKey, SecretKey vaultKey) throws Exception {
        byte[] iv = VaultEncryption.generateRandomIV();
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);
        return VaultEncryption.encryptAESGCM(vaultKey.getEncoded(), rootKey, spec);
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
        json.put("rootPasswordHash", this.rootPasswordHash);
        json.put("encryptedVaultData", Base64.getEncoder().encodeToString(this.encryptedData)); 
        JSONObject vaultKeyObject = new JSONObject();
        vaultKeyObject.put("iv", this.vaultKeyIV);
        vaultKeyObject.put("encryptedVaultKey", Base64.getEncoder().encodeToString(this.encryptedVaultKey));
        json.put("vaultkey", vaultKeyObject);
        return json;
    }

}
