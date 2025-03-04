package Gui;

import javax.swing.*;
import java.awt.*;
import Vault.Vault;
import Vault.VaultEncryption;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import java.util.Base64;
import javax.crypto.SecretKey;

// Lookup the credentials in the vault
public class LookupCredentialPanel extends JPanel {
    private JTextField serviceNameField;
    private JButton searchButton, backButton;
    private Vault vault;
    private GUIBuilder guiBuilder;

    public LookupCredentialPanel(Vault vault, GUIBuilder guiBuilder) {
        this.vault = vault;
        this.guiBuilder = guiBuilder;

        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        serviceNameField = new JTextField(15);
        searchButton = new JButton("Lookup");
        backButton = new JButton("← Back");



        // Label Service Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.LINE_END;
        inputPanel.add(new JLabel("Service Name:"), gbc);

        // Service Name Field
        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.LINE_START;
        inputPanel.add(serviceNameField, gbc);

        // Button Panel- south buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(searchButton);
        buttonPanel.add(backButton);
        // Add components to panel
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);


        // Button Action
        searchButton.addActionListener(e -> searchVault());
        backButton.addActionListener(e -> guiBuilder.showPanel("Main"));


    }

    // Search the vault for the service name
    private void searchVault() {
        String serviceName = serviceNameField.getText().trim();

        if (serviceName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a service name!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String rootPassword = guiBuilder.getUserPassword();

        if (rootPassword == null || rootPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Error: No stored password. Please restart and log in again.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!vault.verifyRootPassword(rootPassword)) {
            JOptionPane.showMessageDialog(this, "Incorrect root password!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String credential = getCredential(serviceName, rootPassword);

        if (credential != null) {
            JOptionPane.showMessageDialog(this, credential, "Credential Found", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "No credentials found for: " + serviceName, "Not Found", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Retrieves the credential for the given service name from the vault.
     *
     * @param serviceName The service name to look up.
     * @param rootPassword The root password to decrypt the credential with.
     * @return The credential string, or null if no credential was found.
     */
    private String getCredential(String serviceName, String rootPassword) {
        JSONArray passwords = vault.getPasswords();


        for (int i = 0; i < passwords.size(); i++) {
            JSONObject entry = passwords.getObject(i);
            String storedServiceName = entry.getString("service");

            if (storedServiceName.equalsIgnoreCase(serviceName)) {
                return "User: " + entry.getString("user") + "\nPassword: " + decryptPassword(entry, rootPassword);
            }
        }

        return null;
    }

    // Decrypt the password
    private String decryptPassword(JSONObject entry, String rootPassword) {
        try {
            String encryptedPass = entry.getString("pass");
            String iv = entry.getString("iv");

            SecretKey rootKey = VaultEncryption.deriveRootKey(rootPassword, Base64.getDecoder().decode(vault.getSalt()));
            SecretKey vaultKey = VaultEncryption.getVaultKey(vault, rootKey);  // Use the stored vault key


            byte[] decryptedBytes = VaultEncryption.decryptAESGCM(
                Base64.getDecoder().decode(encryptedPass),
                vaultKey,
                Base64.getDecoder().decode(iv)
            );


            System.out.println("Decryption successful!"); // Debug log

            return new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return "[Error decrypting password]";
        }
    }
}
