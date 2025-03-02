package Gui;

import javax.swing.*;
import java.awt.*;
import java.security.*;
import java.util.Base64;
import Vault.Vault;
import Vault.VaultEncryption;
import javax.crypto.SecretKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class AddServiceAndKeyGenPanel extends JPanel {
    private JTextField serviceNameField;
    private JButton generateButton;
    private Vault vault;
    private GUIBuilder guiBuilder;

    public AddServiceAndKeyGenPanel(Vault vault, GUIBuilder guiBuilder) {
        this.vault = vault;
        this.guiBuilder = guiBuilder;

        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        serviceNameField = new JTextField(15);

        inputPanel.add(new JLabel("Service Name:"));
        inputPanel.add(serviceNameField);

        // Generate Button
        generateButton = new JButton("Generate & Store ElGamal Key");
        generateButton.addActionListener(e -> generateAndStoreElGamalKey());

        // Add components to panel
        add(inputPanel, BorderLayout.CENTER);
        add(generateButton, BorderLayout.SOUTH);
    }

    private void generateAndStoreElGamalKey() {
        String serviceName = serviceNameField.getText().trim();

        if (serviceName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a service name!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Retrieve the stored user password
        String rootPassword = guiBuilder.getUserPassword();
        if (rootPassword == null || rootPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Error: No stored password. Please restart and log in again.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!vault.verifyRootPassword(rootPassword)) {
            JOptionPane.showMessageDialog(this, "Incorrect root password!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // ✅ Generate a 512-bit ElGamal key pair
            KeyPair keyPair = generateElGamalKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            // ✅ Convert Public Key to Base64 and Show It
            String publicKeyBase64 = Base64.getEncoder().encodeToString(publicKey.getEncoded());
            System.out.println("✅ Public Key (Base64): " + publicKeyBase64);
            JOptionPane.showMessageDialog(this, "Public Key:\n" + publicKeyBase64, "Public Key Generated", JOptionPane.INFORMATION_MESSAGE);

            // ✅ Convert Private Key to Base64
            String privateKeyBase64 = Base64.getEncoder().encodeToString(privateKey.getEncoded());
            System.out.println("✅ Private Key (Base64 - Encrypted before storing): " + privateKeyBase64);

            // ✅ Encrypt the Private Key
            SecretKey rootKey = VaultEncryption.deriveRootKey(rootPassword, Base64.getDecoder().decode(vault.getSalt()));
            SecretKey vaultKey = VaultEncryption.getVaultKey(vault, rootKey);

            byte[] iv = VaultEncryption.generateRandomIV();
            String encodedIV = Base64.getEncoder().encodeToString(iv);

            byte[] encryptedPrivKeyBytes = VaultEncryption.encryptAESGCM(privateKeyBase64.getBytes(), vaultKey, iv);
            String encryptedPrivKey = Base64.getEncoder().encodeToString(encryptedPrivKeyBytes);

            System.out.println("✅ Debug: Private Key Encrypted Successfully!");

            // ✅ Store the encrypted private key in the vault
            vault.addPrivateKey(serviceName, encryptedPrivKey, encodedIV);
            guiBuilder.saveVault();

            JOptionPane.showMessageDialog(this, "Service and ElGamal private key added successfully!");

        } catch (Exception e) {
            System.err.println("❌ Error: Failed to encrypt and store ElGamal private key - " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: Failed to save ElGamal private key!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private KeyPair generateElGamalKeyPair() throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ElGamal", "BC");
        keyGen.initialize(512, new SecureRandom());

        return keyGen.generateKeyPair();
    }
}