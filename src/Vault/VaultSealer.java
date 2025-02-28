package Vault;

import merrimackutil.json.JsonIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Base64;
import javax.crypto.SecretKey;

public class VaultSealer {
    public static void sealVault(Vault vault, File file, String password) {
        try {
            // Derive root key from password
            SecretKey rootKey = VaultEncryption.deriveRootKey(password, Base64.getDecoder().decode(vault.getSalt()));

            // Generate a new random IV for encrypting the vault data
            byte[] iv = VaultEncryption.generateRandomIV();
            String ivBase64 = Base64.getEncoder().encodeToString(iv);

            // Convert Vault object to JSON
            String vaultJson = vault.toJSONType().toString();
            byte[] vaultData = vaultJson.getBytes();

            // Encrypt vault data using the vault key
            SecretKey vaultKey = VaultEncryption.getVaultKey(vault, rootKey);
            byte[] encryptedVaultData = VaultEncryption.encryptAESGCM(vaultData, vaultKey, iv);


            // Save the encrypted vault to a file
            JsonIO.writeSerializedObject(vault, file);
            System.out.println("Vault successfully sealed.");

        } catch (Exception e) {
            System.err.println("Error sealing vault: " + e.getMessage());
        }
    }
<<<<<<< Updated upstream
}
=======
} 
>>>>>>> Stashed changes
