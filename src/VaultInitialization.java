import javax.swing.*;
import encryption.CryptoUtils;
import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

public class VaultInitialization {

    public static Vault initializeVault(File file) throws Exception {
        // Create a password input field
        JPasswordField passwordField = new JPasswordField();
        int option = JOptionPane.showConfirmDialog(
            null, passwordField, "Set a root password for the vault", 
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE
        );

        if (option != JOptionPane.OK_OPTION) {
            throw new IOException("Vault initialization canceled. Exiting.");
        }

        // Get the password securely
        char[] passwordChars = passwordField.getPassword();
        String rootPassword = new String(passwordChars); // Convert char array to string
        
        // Clear password from memory
        java.util.Arrays.fill(passwordChars, ' ');

        if (rootPassword.isEmpty()) {
            throw new IOException("Root password cannot be empty. Exiting.");
        }

        // Generate a new Vault instance
        Vault newVault = new Vault();

        // Generate a random salt (IV) for key derivation
        byte[] salt = CryptoUtils.generateSalt();
        newVault.setSalt(Base64.getEncoder().encodeToString(salt));

        // Derive the root key using scrypt
        byte[] rootKey = CryptoUtils.deriveRootKey(rootPassword, salt);

        // Generate a random vault key
        byte[] vaultKeyBytes = CryptoUtils.generateRandomKey();

        // Encrypt the vault key using the derived root key
        byte[] iv = CryptoUtils.generateIV();
        byte[] encryptedVaultKey = CryptoUtils.encryptAES(vaultKeyBytes, rootKey, iv);

        // Store the encrypted vault key and IV in VaultKey
        Vault.VaultKey vaultKey = new Vault.VaultKey();
        vaultKey.setKey(Base64.getEncoder().encodeToString(encryptedVaultKey));
        vaultKey.setIv(Base64.getEncoder().encodeToString(iv));

        newVault.setVaultkey(vaultKey);

        // Save the newly created vault
        Main.writeFormattedObject(newVault, file);

        JOptionPane.showMessageDialog(null, "Vault successfully initialized!", 
                                      "Vault Setup Complete", JOptionPane.INFORMATION_MESSAGE);

        return newVault;
    }
}
