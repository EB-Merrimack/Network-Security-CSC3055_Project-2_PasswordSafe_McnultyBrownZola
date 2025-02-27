package encryption;

import org.bouncycastle.crypto.generators.SCrypt;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ScryptKeyDerivation {
    public static byte[] deriveRootKey(String password, byte[] salt) {
        // scrypt parameters
        int cost = 2048;
        int blockSize = 8;
        int parallelization = 1;
        int keyLength = 32; // AES-256

        return SCrypt.generate(
            password.getBytes(StandardCharsets.UTF_8),
            salt,
            cost,
            blockSize,
            parallelization,
            keyLength
        );
    }

    public static void main(String[] args) {
        String password = "your-secure-password";
        byte[] salt = new byte[16]; // Use your previously generated salt (IV)
        
        byte[] rootKey = deriveRootKey(password, salt);
        System.out.println("Derived Root Key (Base64): " + Base64.getEncoder().encodeToString(rootKey));
    }
}
