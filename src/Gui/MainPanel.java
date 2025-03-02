package Gui;

import javax.crypto.SecretKey;
import javax.swing.*;
import java.awt.*;
import Vault.Vault;
import Vault.VaultEncryption;

//import Vault.VaultSealer;
import java.io.File;
import java.util.Base64;

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
        lookupCredentialButton.addActionListener(e -> parent.showPanel("LookupCredentialPanel", new LookupCredentialPanel(vault, parent)));
        generatePasswordButton.addActionListener(e -> parent.showPanel("GenerateRandomPasswordPanel", new GenerateRandomPasswordPanel()));
        addPrivateKeyButton.addActionListener(e -> parent.showPanel("AddServiceAndPrivateKeyPanel", new AddServiceAndPrivateKeyPanel(vault, parent)));
        lookupPrivateKeyButton.addActionListener(e -> parent.showPanel("LookupPrivateKeyPanel", new LookupPrivateKeyPanel(vault, parent)));



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
        //logoutButton.addActionListener(e -> logoutAndSealVault());

        // Create a JPanel for logout button and add it to the top of the main panel
        JPanel logoutPanel = new JPanel();
        logoutPanel.add(logoutButton);

        // Add the logoutPanel to the bottom and contentPanel to the center
       
        add(contentPanel, BorderLayout.CENTER);
        add(logoutPanel, BorderLayout.SOUTH);
    }

    // Method to logout and seal the vault
    /*private void logoutAndSealVault() {
        
        // Call the sealVault method from the Vault class
        Vault vault2 = new Vault();
        vault2.sealVault();

    }*/
}
