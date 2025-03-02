package Gui;

import javax.swing.*;
import java.awt.*;
import Vault.Vault;
import Vault.VaultEncryption;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import java.util.Base64;
import javax.crypto.SecretKey;

public class LookupCredentialPanel extends JPanel {
    private JTextField serviceNameField;
    private JButton searchButton, backButton;
    private Vault vault;
    private GUIBuilder guiBuilder;

    public LookupCredentialPanel(Vault vault, GUIBuilder guiBuilder) {
        this.vault = vault;
        this.guiBuilder = guiBuilder;

        setLayout(new BorderLayout());

        serviceNameField = new JTextField(15);
        searchButton = new JButton("Lookup");
        backButton = new JButton("← Back");

        JPanel inputPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        inputPanel.add(new JLabel("Service Name:"));
        inputPanel.add(serviceNameField);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(searchButton);
        buttonPanel.add(backButton);

        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> searchVault());
        backButton.addActionListener(e -> guiBuilder.showPanel("Main"));
    }

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

    private String decryptPassword(JSONObject entry, String rootPassword) {
        try {
            String encryptedPass = entry.getString("pass");
            String iv = entry.getString("iv");

            SecretKey rootKey = VaultEncryption.deriveRootKey(rootPassword, Base64.getDecoder().decode(vault.getSalt()));
            SecretKey vaultKey = VaultEncryption.getVaultKey(vault, rootKey);

            byte[] decryptedBytes = VaultEncryption.decryptAESGCM(
                Base64.getDecoder().decode(encryptedPass),
                vaultKey,
                Base64.getDecoder().decode(iv)
            );

            return new String(decryptedBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return "[Error decrypting password]";
        }
    }
}
