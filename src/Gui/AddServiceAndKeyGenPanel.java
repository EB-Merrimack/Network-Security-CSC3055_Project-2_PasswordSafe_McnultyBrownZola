package Gui;

import Vault.Vault;
import Vault.VaultEncryption;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AddServiceAndKeyGenPanel extends JPanel {
    private JTextField serviceNameField;
    private JTextArea generatedKeyArea;
    private JButton generateKeyButton, addToVaultButton;
    private Vault vault;
    private String rootPassword;
    private File vaultFile; 

    public AddServiceAndKeyGenPanel(File vaultFile, String rootPassword) {
        this.vaultFile = vaultFile;
        this.vault = Vault.loadVault(vaultFile);
        this.rootPassword = rootPassword;

        serviceNameField = new JTextField(15);
        generatedKeyArea = new JTextArea(5, 20);
        generatedKeyArea.setLineWrap(true);
        generatedKeyArea.setWrapStyleWord(true);
        generatedKeyArea.setEditable(false);

        generateKeyButton = new JButton("Generate Key");
        addToVaultButton = new JButton("Add to Vault");

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        inputPanel.add(new JLabel("Service Name:"));
        inputPanel.add(serviceNameField);
        inputPanel.add(generateKeyButton);
        inputPanel.add(new JScrollPane(generatedKeyArea));

        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.CENTER);
        add(addToVaultButton, BorderLayout.SOUTH);

        generateKeyButton.addActionListener(e -> generateKey());
        addToVaultButton.addActionListener(e -> addToVault());
    }

    private void generateKey() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();

            String privateKeyBase64 = Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
            generatedKeyArea.setText(privateKeyBase64);

            JOptionPane.showMessageDialog(this, "Key generated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(this, "Error generating key: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void addToVault() {
        String service = serviceNameField.getText().trim();
        String privateKey = generatedKeyArea.getText().trim();

        if (service.isEmpty() || privateKey.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Service and key must be provided.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // get vault key from root password
            SecretKey vaultKey = VaultEncryption.deriveRootKey(rootPassword, Base64.getDecoder().decode(vault.generateSalt()));

            // Generate IV and encrypt the private key
            byte[] iv = VaultEncryption.generateRandomIV();
            String ivBase64 = Base64.getEncoder().encodeToString(iv);
            byte[] encryptedKey = VaultEncryption.encryptAESGCM(privateKey.getBytes(), vaultKey, iv);
            String encryptedKeyBase64 = Base64.getEncoder().encodeToString(encryptedKey);

            // Add private key entry to vault
            vault.addPrivateKey(service, encryptedKeyBase64, ivBase64);

            // Save vault to file
            vault.saveVault(vaultFile);

            JOptionPane.showMessageDialog(this, "Service & key added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error encrypting key: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
