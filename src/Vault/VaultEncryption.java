package Vault;

import org.bouncycastle.crypto.generators.SCrypt;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.Base64;

public class VaultEncryption {
    static {
        Security.addProvider(new BouncyCastleProvider()); // ✅ Register Bouncy Castle
    }

   // private static final int SCRYPT_COST = 2048;
   // private static final int SCRYPT_BLOCK_SIZE = 8;
   // private static final int SCRYPT_PARALLELISM = 1;
    private static final int KEY_SIZE = 32; // AES-256
    private static final int IV_SIZE = 12; // GCM Standard IV

    // ✅ Derives AES key from password using scrypt
    public static SecretKey deriveRootKey(String password, byte[] salt) {
        System.out.println("🔍 Debug: Using Salt for Root Key Derivation (Base64): " + Base64.getEncoder().encodeToString(salt));
        System.out.println("🔍 Debug: Using Password for Root Key Derivation: " + password);
    
        // Ensure scrypt parameters remain consistent
        int cost = 2048;
        int blockSize = 8;
        int parallelization = 1;
        int keyLength = 32; // AES-256
    
        byte[] keyBytes = SCrypt.generate(password.getBytes(), salt, cost, blockSize, parallelization, keyLength);
        
        System.out.println("✅ Debug: Derived Root Key (Base64): " + Base64.getEncoder().encodeToString(keyBytes));
        
        return new SecretKeySpec(keyBytes, "AES");
    }

    // ✅ Generates a random AES-GCM IV
    public static byte[] generateRandomIV() {
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    // ✅ Generates a random AES key
    public static byte[] generateRandomKey() {
        byte[] key = new byte[KEY_SIZE];
        new SecureRandom().nextBytes(key);
        return key;
    }

    // ✅ Encrypts data using AES-GCM
    public static byte[] encryptAESGCM(byte[] plaintext, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        return cipher.doFinal(plaintext);
    }

    // ✅ Decrypts data using AES-GCM
    public static byte[] decryptAESGCM(byte[] ciphertext, SecretKey key, byte[] iv) throws Exception {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
            GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
            return cipher.doFinal(ciphertext);
        } catch (GeneralSecurityException e) {
            // Handle GCM MAC failure here (authentication tag mismatch)
            throw new SecurityException("GCM decryption failed: " + e.getMessage(), e);
        }
    }

    // ✅ Gets the Vault Key by decrypting it with the root key
    public static SecretKey getVaultKey(Vault vault, SecretKey rootKey) throws Exception {
        if (vault.getVaultKeyValue().isEmpty() || vault.getVaultKeyIV().isEmpty()) {
            throw new IllegalStateException("Vault key or IV is missing.");
        }
    
        byte[] encryptedVaultKey = Base64.getDecoder().decode(vault.getVaultKeyValue());
        byte[] iv = Base64.getDecoder().decode(vault.getVaultKeyIV());
    
        System.out.println("🔍 Debug: Encrypted Vault Key (Base64): " + vault.getVaultKeyValue());
        System.out.println("🔍 Debug: Vault Key IV (Base64): " + vault.getVaultKeyIV());
    
        try {
            // Decrypt the vault key
            byte[] decryptedVaultKey = decryptAESGCM(encryptedVaultKey, rootKey, iv);
            
            System.out.println("✅ Debug: Vault Key Decryption Successful! Length: " + decryptedVaultKey.length);
            return new SecretKeySpec(decryptedVaultKey, "AES");
    
        } catch (Exception e) {
            System.err.println("❌ Error: Vault Key Decryption Failed - " + e.getMessage());
            System.err.println("⚠️ Possible Causes: Incorrect Root Key, IV mismatch, or corrupted vault file.");
            throw new SecurityException("Vault Key Decryption Failed", e);
        }
    }
}
