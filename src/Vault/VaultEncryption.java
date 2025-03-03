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

public static SecretKey getSecretKeyFromBytes(byte[] vaultKeyBytes) {
    if (vaultKeyBytes == null || vaultKeyBytes.length != KEY_SIZE) {
        throw new IllegalArgumentException("Invalid vault key bytes. Expected length: " + KEY_SIZE);
    }
    return new SecretKeySpec(vaultKeyBytes, "AES");
}

// Method to encrypt using AES-GCM
public static byte[] encryptAESGCM(byte[] data, SecretKey key, GCMParameterSpec spec) throws Exception {
    // Initialize the Cipher for AES/GCM/NoPadding
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
    
    // Initialize the cipher in ENCRYPT_MODE with the key and GCMParameterSpec (which includes the IV)
    cipher.init(Cipher.ENCRYPT_MODE, key, spec);
    
    // Perform the encryption and return the result
    return cipher.doFinal(data);
}



public static byte[] decryptAESGCMopener(byte[] encryptedData, SecretKey rootKey, GCMParameterSpec spec) {
    try {
        // Initialize the Cipher for AES/GCM/NoPadding
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        
        // Initialize the cipher in DECRYPT_MODE with the provided rootKey and GCMParameterSpec
        cipher.init(Cipher.DECRYPT_MODE, rootKey, spec);
        
        // Perform the decryption and return the result
        return cipher.doFinal(encryptedData);
    } catch (Exception e) {
        // Log the error (or use a logging framework like SLF4J for production code)
        System.err.println("❌ Decryption failed: " + e.getMessage());
        e.printStackTrace();
        return null; }
}




public static SecretKey reconstructKey(byte[] vaultKeyBytes) {
    if (vaultKeyBytes == null || (vaultKeyBytes.length != 16 && vaultKeyBytes.length != 24 && vaultKeyBytes.length != 32)) {
        throw new IllegalArgumentException("Invalid key length for AES.");
    }
    return new SecretKeySpec(vaultKeyBytes, "AES");
}

public static byte[] extractIV(byte[] encryptedVaultData) {
    // Define the length of the IV for AES-GCM (12 bytes is common)
    final int IV_LENGTH = 12;

    if (encryptedVaultData == null || encryptedVaultData.length < IV_LENGTH) {
        throw new IllegalArgumentException("Invalid encrypted data: Too short to contain an IV.");
    }

    // Extract the first 12 bytes (IV) from the encrypted data
    byte[] iv = new byte[IV_LENGTH];
    System.arraycopy(encryptedVaultData, 0, iv, 0, IV_LENGTH);

    return iv;
}




  // Decrypt vault data using AES/GCM/NoPadding
  public static byte[] decryptAESGCMvaultdata(byte[] encryptedVaultData, byte[] decryptedVaultKey, GCMParameterSpec spec) {
    try {
        // Create a SecretKey from the decryptedVaultKey byte array
        SecretKey secretKey = new SecretKeySpec(decryptedVaultKey, "AES");

        // Initialize the Cipher for AES/GCM/NoPadding
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        // Initialize the cipher in DECRYPT_MODE with the SecretKey and GCMParameterSpec (which contains the IV)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec);

        // Perform the decryption and return the result (decrypted vault data)
        return cipher.doFinal(encryptedVaultData);
    } catch (Exception e) {
        // Handle potential errors (e.g., invalid data, decryption failure)
        System.err.println("❌ Decryption failed: " + e.getMessage());
        throw new RuntimeException("Failed to decrypt vault data", e);
    }
}
}




