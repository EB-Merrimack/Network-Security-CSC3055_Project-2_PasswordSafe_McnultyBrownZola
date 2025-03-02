package Vault;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Base64;
import javax.crypto.SecretKey;
import merrimackutil.json.types.JSONObject;
import java.security.SecureRandom;

public class VaultKeyGenerator {
    public static void main(String[] args) {
        try {
            // 🔹 User Password for Root Key Derivation
            String userPassword = "SuperSecurePassword123!";

            // 🔹 Generate a 16-byte Salt for SCrypt
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);

            // 🔹 Derive Root Key from Password
            SecretKey rootKey = VaultEncryption.deriveRootKey(userPassword, salt);
            System.out.println("Root Key (Base64): " + Base64.getEncoder().encodeToString(rootKey.getEncoded()));

            // 🔹 Generate a Random AES-256 Vault Key
            byte[] vaultKeyBytes = VaultEncryption.generateRandomKey();
            SecretKey vaultKey = new javax.crypto.spec.SecretKeySpec(vaultKeyBytes, "AES");
            System.out.println("Generated Vault Key (Base64): " + Base64.getEncoder().encodeToString(vaultKeyBytes));

            // 🔹 Encrypt the Vault Key using the Root Key
            byte[] iv = VaultEncryption.generateRandomIV(); // IV for AES-GCM
            byte[] encryptedVaultKey = VaultEncryption.encryptAESGCM(vaultKeyBytes, rootKey, iv);

            // 🔹 Store Encrypted Vault Key & IV in JSON
            JSONObject vaultKeyJson = new JSONObject();
            vaultKeyJson.put("salt", Base64.getEncoder().encodeToString(salt));
            vaultKeyJson.put("vaultKeyValue", Base64.getEncoder().encodeToString(encryptedVaultKey));
            vaultKeyJson.put("vaultKeyIV", Base64.getEncoder().encodeToString(iv));

            // 🔹 Save JSON to file
            try (FileWriter file = new FileWriter("vault_key.json")) {
                file.write(vaultKeyJson.toString());
                System.out.println("Vault Key JSON saved successfully.");
            }

        } catch (Exception e) {
            System.err.println("Error during key generation: " + e.getMessage());
        }
    }
}


