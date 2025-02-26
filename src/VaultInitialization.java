import javax.swing.*;

import encryption.CryptoUtils;

import java.io.File;
import java.io.IOException;

public class VaultInitialization {

    public static Vault initializeVault(File file) throws IOException {
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

        // Create a new Vault instance
        Vault newVault = new Vault();
        
        // Securely store the root password
        String salt = CryptoUtils.generateSalt();
        String hashedKey = CryptoUtils.hashPassword(rootPassword, salt);
        newVault.setSalt(salt);
        
        Vault.VaultKey vaultKey = new Vault.VaultKey();
        vaultKey.setKey(hashedKey);
        vaultKey.setIv(CryptoUtils.generateIV()); // Placeholder IV for encryption
        
        newVault.setVaultkey(vaultKey);

        // Save the newly created vault
        Main.writeFormattedObject(newVault, file);

        JOptionPane.showMessageDialog(null, "Vault successfully initialized!", 
                                      "Vault Setup Complete", JOptionPane.INFORMATION_MESSAGE);

        return newVault;
    }
}
