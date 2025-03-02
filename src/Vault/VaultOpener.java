package Vault;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Base64;
import javax.crypto.SecretKey;
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
    private boolean isSealed;

    public VaultOpener(String userPassword) {
        System.out.println("🔑 Opening Vault...");
        try {
            // Load encrypted vault data
            File vaultFile = new File("src/json/vault.json"); // Adjust to your actual file path
            if (!vaultFile.exists()) {
                throw new IllegalStateException("Vault file not found!");
            }

            // Read encrypted vault file
            byte[] encryptedData = Files.readAllBytes(vaultFile.toPath());

            // Read the JSON object from the file
            JSONObject vaultData = JsonIO.readObject(new String(encryptedData, StandardCharsets.UTF_8));

            // Extract the salt and vault key IV
            this.salt = vaultData.getString("salt");
            this.vaultKeyIV = vaultData.getString("vaultKeyIV");

            // Decode the salt and derive the root key
            byte[] saltBytes = Base64.getDecoder().decode(this.salt);
            SecretKey rootKey = VaultEncryption.deriveRootKey(userPassword, saltBytes);

            // Decrypt the encrypted vault key using the root key
            this.encryptedVaultKey = Base64.getDecoder().decode(vaultData.getString("encryptedVaultKey"));
            SecretKey vaultKey = decryptVaultKey(this.encryptedVaultKey, rootKey);
            if (vaultKey == null) {
                throw new IllegalStateException("Failed to decrypt vault key!");
            }

            // Decrypt vault data using the vault key
            byte[] encryptedVaultData = Base64.getDecoder().decode(vaultData.getString("encryptedVaultData"));
            JSONObject decryptedVaultData = decryptVaultData(encryptedVaultData, vaultKey);

            System.out.println("🔍 Debug: Vault data successfully decrypted.");

            // Deserialize the decrypted vault data
            deserialize(decryptedVaultData);
            System.out.println("✅ Vault successfully opened.");

            // Mark the vault as unsealed
            this.isSealed = false; // Set isSealed to false once the vault is opened

        } catch (Exception e) {
            System.err.println("❌ Error opening vault: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private SecretKey decryptVaultKey(byte[] encryptedVaultKey, SecretKey rootKey) throws Exception {
        byte[] iv = Base64.getDecoder().decode(this.vaultKeyIV); // Use vaultKeyIV for AES decryption
        // Decrypt the encrypted vault key using the root key and the IV
        byte[] decryptedVaultKeyBytes = VaultEncryption.decryptAESGCM(encryptedVaultKey, rootKey, iv);
        return VaultEncryption.getSecretKeyFromBytes(decryptedVaultKeyBytes); // Convert bytes to SecretKey
    }

    private JSONObject decryptVaultData(byte[] encryptedData, SecretKey vaultKey) throws Exception {
        byte[] iv = Base64.getDecoder().decode(this.vaultKeyIV); // Use vaultKeyIV for AES decryption
        // Decrypt the vault data using AES-GCM
        byte[] decryptedData = VaultEncryption.decryptAESGCM(encryptedData, vaultKey, iv);
        // Convert decrypted data into a JSONObject using JsonIO
        return JsonIO.readObject(new String(decryptedData, StandardCharsets.UTF_8));
    }

    @Override
    public void deserialize(JSONType json) {
        if (!(json instanceof JSONObject)) {
            throw new IllegalArgumentException("Invalid JSON format for VaultOpener");
        }
        JSONObject jsonObject = (JSONObject) json;
        this.salt = jsonObject.getString("salt");
        this.passwords = jsonObject.getArray("passwords");
        this.privKeys = jsonObject.getArray("privKeys");
        this.vaultKeyIV = jsonObject.getString("vaultKeyIV");
        this.encryptedVaultKey = Base64.getDecoder().decode(jsonObject.getString("encryptedVaultKey"));
    }

    @Override
    public JSONType toJSONType() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("salt", this.salt);
        jsonObject.put("passwords", this.passwords);
        jsonObject.put("privKeys", this.privKeys);
        jsonObject.put("vaultKeyIV", this.vaultKeyIV);
        jsonObject.put("encryptedVaultKey", Base64.getEncoder().encodeToString(this.encryptedVaultKey));
        return jsonObject;
    }

    public boolean isVaultSealed() {
        return this.isSealed;
    }
}
