package Vault;

import org.bouncycastle.crypto.generators.SCrypt;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class VaultEncryption {

    private static final int AES_KEY_SIZE = 128;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int SCRYPT_COST = 2048;
    private static final int SCRYPT_BLOCK_SIZE = 8;
    private static final int SCRYPT_PARALLELIZATION = 1;

    // Generate a random 128-bit AES key
    public static String generateRandomAESKey() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(AES_KEY_SIZE);
            byte[] key = keyGen.generateKey().getEncoded();
            return Base64.getEncoder().encodeToString(key);
        } catch (Exception e) {
            throw new RuntimeException("Error generating AES key", e);
        }
    }

    // 🔑 Derive root key from password using scrypt
    public static byte[] deriveRootKey(String password, String salt) {
        return SCrypt.generate(password.getBytes(), Base64.getDecoder().decode(salt),
                SCRYPT_COST, SCRYPT_BLOCK_SIZE, SCRYPT_PARALLELIZATION, AES_KEY_SIZE / 8);
    }

    // Encrypt data using AES/GCM/NoPadding
    public static String encryptAES(String plaintext, byte[] key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            byte[] iv = new byte[12]; // 12-byte IV
            new SecureRandom().nextBytes(iv);

            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            byte[] ciphertext = cipher.doFinal(plaintext.getBytes());

            return Base64.getEncoder().encodeToString(iv) + ":" + Base64.getEncoder().encodeToString(ciphertext);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting", e);
        }
    }

    // Decrypt data using AES/GCM/NoPadding
    public static String decryptAES(String ciphertext, byte[] key) {
        try {
            String[] parts = ciphertext.split(":");
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] encryptedData = Base64.getDecoder().decode(parts[1]);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(GCM_TAG_LENGTH, iv));

            return new String(cipher.doFinal(encryptedData));
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting", e);
        }
    }
}