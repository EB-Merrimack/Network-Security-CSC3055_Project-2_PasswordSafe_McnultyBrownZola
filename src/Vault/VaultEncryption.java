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
    public static SecretKey getVaultKey(Vault vault, SecretKey rootKey) {
        try {
            System.out.println("🛠 Debug: Retrieving Vault Key...");
    
            byte[] encryptedVaultKey = Base64.getDecoder().decode(vault.getVaultKeyValue());
            byte[] iv = Base64.getDecoder().decode(vault.getVaultKeyIV());
    
            System.out.println("🔍 Debug: Stored Encrypted Vault Key (Base64): " + vault.getVaultKeyValue());
            System.out.println("🔍 Debug: Vault Key IV (Base64): " + vault.getVaultKeyIV());
    
            byte[] decryptedVaultKey = decryptAESGCM(encryptedVaultKey, rootKey, iv);
            SecretKey vaultKey = new SecretKeySpec(decryptedVaultKey, "AES");
    
            // 🔍 Debug: Check if the decrypted vault key matches what should be used
            System.out.println("🔑 Debug: Decrypted Vault Key (Base64): " + Base64.getEncoder().encodeToString(vaultKey.getEncoded()));
    
            return vaultKey;
        } catch (Exception e) {
            throw new SecurityException("Vault Key Decryption Failed", e);
        }
    }

   public static JSONArray encryptJSONArray(JSONArray jsonArray, SecretKey vaultSecretKey) {
    JSONArray encryptedArray = new JSONArray();

    try {
        // Iterate over each entry in the JSONArray
        for (Object obj : jsonArray) {
            if (obj instanceof JSONObject) {  // Ensure we are processing a JSONObject
                JSONObject jsonEntry = (JSONObject) obj;

                // Generate a unique IV for each encryption (could also be random)
                byte[] iv = VaultEncryption.generateRandomIV();  // VaultEncryption.generateRandomIV() will generate a random 12-byte IV for GCM

                // Encrypt each value (specifically the "pass" or "privkey" field in this case)
                String dataToEncrypt = jsonEntry.getString("pass");  // Or you could use "privkey" depending on the entry type
                byte[] encryptedData = VaultEncryption.encryptAESGCM(dataToEncrypt.getBytes(), vaultSecretKey, iv);

                // Create a new JSONObject to store the encrypted data
                JSONObject encryptedEntry = new JSONObject();
                encryptedEntry.put("iv", Base64.getEncoder().encodeToString(iv));  // Store the IV in Base64
                encryptedEntry.put("encrypted_data", Base64.getEncoder().encodeToString(encryptedData));  // Store the encrypted data in Base64
                encryptedArray.add(encryptedEntry);  // Add this encrypted entry to the new array
            }
        }

    } catch (Exception e) {
        System.err.println("Error encrypting JSONArray: " + e.getMessage());
    }

    return encryptedArray;
}



}
