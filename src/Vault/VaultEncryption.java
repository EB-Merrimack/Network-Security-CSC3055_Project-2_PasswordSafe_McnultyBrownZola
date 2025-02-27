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
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "BC");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
        return cipher.doFinal(ciphertext);
    }
}