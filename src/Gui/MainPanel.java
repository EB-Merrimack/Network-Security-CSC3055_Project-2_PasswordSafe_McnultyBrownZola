package Gui;

import javax.swing.*;
import java.awt.*;
import Vault.Vault;
import Vault.VaultSealer;
import java.io.File;

public class MainPanel extends JPanel {
    private Vault vault;
    private GUIBuilder parent;

    public MainPanel(GUIBuilder parent, Vault vault) {
        this.parent = parent;
        this.vault = vault;

        // Set layout for MainPanel
        setLayout(new BorderLayout());

        // Create a JPanel for the content and set it to the center
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        // Add components to contentPanel
        JLabel welcomeLabel = new JLabel("Welcome to the Secrets Vault");
        JButton addCredentialButton = new JButton("Add Service Credentials");
        JButton lookupCredentialButton = new JButton("Lookup Credentials");
        JButton generatePasswordButton = new JButton("Generate Password");
        JButton addPrivateKeyButton = new JButton("Add Private Key");
        JButton lookupPrivateKeyButton = new JButton("Lookup Private Key");
        JButton generateKeyPairButton = new JButton("Generate Key Pair");

        // Add action listeners
        addCredentialButton.addActionListener(e -> parent.showPanel("AddCredentialPanel", new AddCredentialPanel(parent)));
        lookupCredentialButton.addActionListener(e -> parent.showPanel("LookupCredentialPanel", new LookupCredentialPanel()));
        generatePasswordButton.addActionListener(e -> parent.showPanel("GenerateRandomPasswordPanel", new GenerateRandomPasswordPanel()));

        // Add buttons to content panel
        contentPanel.add(welcomeLabel);
        contentPanel.add(addCredentialButton);
        contentPanel.add(lookupCredentialButton);
        contentPanel.add(generatePasswordButton);
        contentPanel.add(addPrivateKeyButton);
        contentPanel.add(lookupPrivateKeyButton);
        contentPanel.add(generateKeyPairButton);

        // Create logout button and add action to it
        JButton logoutButton = new JButton("Logout");
        logoutButton.setPreferredSize(new Dimension(100, 30));
        logoutButton.addActionListener(e -> logoutAndSealVault());

        // Create a JPanel for logout button and add it to the top of the main panel
        JPanel logoutPanel = new JPanel();
        logoutPanel.add(logoutButton);

        // Add the logoutPanel to the top and contentPanel to the center
        add(logoutPanel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
    }

    // Method to logout and seal the vault
    private void logoutAndSealVault() {
        // Get the password entered in the password field (assuming it’s accessible here, adapt as necessary)
        String password = "yourPasswordHere";  // Replace with actual password retrieval logic

        // Create a File object for the vault.json file
        File vaultFile = new File("vault.json");

        // Seal the vault with the password and file
        VaultSealer.sealVault(vault, vaultFile, password);  // Pass the File object and password

        // Show a message and return to login panel
        JOptionPane.showMessageDialog(this, "You have logged out and the vault has been sealed.");
        parent.showPanel("Login");  // Show login panel after logout
    }
}
