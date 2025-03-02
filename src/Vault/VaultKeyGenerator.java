package Vault;

import java.util.Base64;
import javax.crypto.SecretKey;

public class VaultKeyGenerator {
    public static void main(String[] args) {
        try {
            // Define User Password and Generate Salt
            String userPassword = "SuperSecurePassword123!";
            byte[] salt = VaultEncryption.generateRandomKey(); // 32-byte salt for scrypt

            // Derive Root Key from Password
            SecretKey rootKey = VaultEncryption.deriveRootKey(userPassword, salt);
            System.out.println("Root Key (Base64): " + Base64.getEncoder().encodeToString(rootKey.getEncoded()));

            // Generate a Random AES-256 Vault Key
            byte[] vaultKeyBytes = VaultEncryption.generateRandomKey();
            SecretKey vaultKey = new javax.crypto.spec.SecretKeySpec(vaultKeyBytes, "AES");
            System.out.println("Generated Vault Key (Base64): " + Base64.getEncoder().encodeToString(vaultKeyBytes));

            //Encrypt the Vault Key using the Root Key
            byte[] iv = VaultEncryption.generateRandomIV(); // IV for AES-GCM
            byte[] encryptedVaultKey = VaultEncryption.encryptAESGCM(vaultKeyBytes, rootKey, iv);
            
            // Store Encrypted Vault Key & IV
            String encryptedVaultKeyBase64 = Base64.getEncoder().encodeToString(encryptedVaultKey);
            String ivBase64 = Base64.getEncoder().encodeToString(iv);
            
            System.out.println(" Encrypted Vault Key (Base64): " + encryptedVaultKeyBase64);
            System.out.println(" IV for Vault Key (Base64): " + ivBase64);
            
            
        } catch (Exception e) {
            System.err.println("Error during key generation: " + e.getMessage());
        }
    }
}

