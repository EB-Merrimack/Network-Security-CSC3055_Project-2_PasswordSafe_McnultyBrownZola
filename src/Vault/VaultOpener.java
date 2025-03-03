package Vault;

import java.io.File;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;

public class VaultOpener {

    public VaultOpener(String userPassword) {
        try {
            // Load encrypted vault data
            File vaultFile = new File("src/json/vault.json");
            JSONObject vaultData = JsonIO.readObject(vaultFile);

            // Extract required fields
            String salt = vaultData.getString("salt");
            JSONObject vaultKeyObject = (JSONObject) vaultData.get("vaultkey");
            String vaultKeyIV = vaultKeyObject.getString("iv");
            String encryptedVaultKeyBase64 = vaultKeyObject.getString("encryptedVaultKey");
            String encryptedVaultDataBase64 = vaultData.getString("encryptedVaultData");

            if (salt == null || vaultKeyIV == null || encryptedVaultKeyBase64 == null || encryptedVaultDataBase64 == null) {
                throw new IllegalArgumentException("Missing required fields in JSON.");
            }

            byte[] encryptedVaultKey = Base64.getDecoder().decode(encryptedVaultKeyBase64);
            byte[] encryptedVaultData = Base64.getDecoder().decode(encryptedVaultDataBase64);

            // Derive root key
            byte[] saltBytes = Base64.getDecoder().decode(salt);
            SecretKey rootKey = VaultEncryption.deriveRootKey(userPassword, saltBytes);
            System.out.println("Debug: Derived Root Key (Base64): " + Base64.getEncoder().encodeToString(rootKey.getEncoded()));

            // Decrypt vault key
            byte[] iv = Base64.getDecoder().decode(vaultKeyIV);
            GCMParameterSpec spec = new GCMParameterSpec(128, iv);
            byte[] vaultKeyBytes = VaultEncryption.decryptAESGCMopener(encryptedVaultKey, rootKey, spec);

            if (vaultKeyBytes == null) {
                throw new IllegalArgumentException("Decryption failed: vaultKeyBytes is null.");
            }

            // Validate vault key length
            if (vaultKeyBytes.length != 16 && vaultKeyBytes.length != 24 && vaultKeyBytes.length != 32) {
                throw new IllegalArgumentException("Invalid AES key length: " + vaultKeyBytes.length);
            }
            SecretKey vaultKey = VaultEncryption.reconstructKey(vaultKeyBytes);

            // Decrypt vault data
            byte[] vaultIV = VaultEncryption.extractIV(encryptedVaultData);
            GCMParameterSpec vaultSpec = new GCMParameterSpec(128, vaultIV);
            byte[] decryptedVaultData = VaultEncryption.decryptAESGCMopener(encryptedVaultData, vaultKey, vaultSpec);

            // Output decrypted vault data
            String decryptedJson = new String(decryptedVaultData);
            System.out.println("Decrypted Vault Data: " + decryptedJson);

        } catch (Exception e) {
            System.err.println("❌ Error opening vault: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
