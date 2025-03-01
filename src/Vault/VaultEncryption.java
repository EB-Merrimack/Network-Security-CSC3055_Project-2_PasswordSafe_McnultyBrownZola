package Vault;

import org.bouncycastle.crypto.generators.SCrypt;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;

import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.util.Base64;

public class VaultEncryption {
    static {
        Security.addProvider(new BouncyCastleProvider()); // ✅ Register Bouncy Castle
    }

    private static final int SCRYPT_COST = 2048;
    private static final int SCRYPT_BLOCK_SIZE = 8;
    private static final int SCRYPT_PARALLELISM = 1;
    private static final int KEY_SIZE = 32; // AES-256
    private static final int IV_SIZE = 12; // GCM Standard IV

    // ✅ Derives AES key from password using scrypt
    public static SecretKey deriveRootKey(String password, byte[] salt) {
        byte[] keyBytes = SCrypt.generate(password.getBytes(), salt, SCRYPT_COST, SCRYPT_BLOCK_SIZE, SCRYPT_PARALLELISM, KEY_SIZE);
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
    
        byte[] decryptedVaultKey = decryptAESGCM(encryptedVaultKey, rootKey, iv);
    
        if (decryptedVaultKey.length != 32) {
            throw new SecurityException("Invalid vault key length.");
        }
    
        return new SecretKeySpec(decryptedVaultKey, "AES");
    }



  public static JSONArray encryptJSONArray(JSONArray data, SecretKey vaultKey) {
    JSONArray encryptedData = new JSONArray();

    try {
        // Generate a random IV for AES-GCM
        byte[] iv = new byte[12];  // AES-GCM requires a 12-byte IV
        new java.security.SecureRandom().nextBytes(iv);

        // Prepare AES-GCM cipher
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(128, iv);  // 128-bit authentication tag
        cipher.init(Cipher.ENCRYPT_MODE, vaultKey, spec);

        // Encrypt each element in the array
        for (Object obj : data) {
            if (obj instanceof String) {
                String str = (String) obj;
                byte[] encryptedBytes = cipher.doFinal(str.getBytes());
                String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes);
                encryptedData.add(encryptedBase64);
            } else if (obj instanceof JSONObject) {
                // If the element is a JSON object, you can recursively encrypt fields or serialize the object
                String jsonString = obj.toString(); // Convert to JSON string
                byte[] encryptedBytes = cipher.doFinal(jsonString.getBytes());
                String encryptedBase64 = Base64.getEncoder().encodeToString(encryptedBytes);
                encryptedData.add(encryptedBase64);
            } else {
                // Handle other types if needed
                encryptedData.add(obj); // Add non-encrypted data as-is
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
    
    return encryptedData;
}

public static SecretKey convertBytesToKey(byte[] keyBytes) {
    // Ensure the key length is valid for AES (typically 16, 24, or 32 bytes for AES-128, AES-192, or AES-256)
    if (keyBytes.length != 32) {
        throw new IllegalArgumentException("Invalid key length. Expected 32 bytes for AES-256.");
    }

    // Create and return a SecretKeySpec from the byte array
    return new SecretKeySpec(keyBytes, "AES");
}
}
