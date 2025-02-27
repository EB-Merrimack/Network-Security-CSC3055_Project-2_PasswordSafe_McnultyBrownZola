/*package encryption;

import org.bouncycastle.crypto.generators.SCrypt;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;

public class CryptoUtils {
    private static final int SCRYPT_COST = 2048;
    private static final int SCRYPT_BLOCK_SIZE = 8;
    private static final int SCRYPT_PARALLELIZATION = 1;
    private static final int AES_KEY_SIZE = 32; // 256-bit key
    private static final int IV_SIZE = 16;
    private static final SecureRandom secureRandom = new SecureRandom();

    // Generate a random 16-byte salt
    public static byte[] generateSalt() {
        byte[] salt = new byte[IV_SIZE];
        secureRandom.nextBytes(salt);
        return salt;
    }

    // Generate a random IV
    public static byte[] generateIV() {
        byte[] iv = new byte[IV_SIZE];
        secureRandom.nextBytes(iv);
        return iv;
    }

    // Derive the root key from the password using scrypt
    public static byte[] deriveRootKey(String password, byte[] salt) {
        return SCrypt.generate(password.getBytes(), salt, SCRYPT_COST, SCRYPT_BLOCK_SIZE, SCRYPT_PARALLELIZATION, AES_KEY_SIZE);
    }

    // Generate a new random AES key (vault key)
    public static byte[] generateRandomKey() {
        byte[] key = new byte[AES_KEY_SIZE];
        secureRandom.nextBytes(key);
        return key;
    }

    // Encrypt data using AES-GCM
     // Encrypt data using AES-GCM
    public static byte[] encryptAES(byte[] data, byte[] key, byte[] iv) throws Exception {
        SecretKey secretKey = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");

        // Use GCMParameterSpec with a 128-bit authentication tag length
        GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv); // 128-bit authentication tag

        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);
        return cipher.doFinal(data);
    }
}*/
