package Gui;

import javax.swing.*;

public class MainPanel extends JPanel{
    public MainPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JLabel welcomeLabel = new JLabel("Welcome to the Secrets Vault");
        JButton addCredentialButton = new JButton("Add Service Credentials");
        JButton lookupCredentialButton = new JButton("Lookup Credentials");
        JButton generatePasswordButton = new JButton("Generate Password");
        JButton addPrivateKeyButton = new JButton("Add Private Key");
        JButton lookupPrivateKeyButton = new JButton("Lookup Private Key");
        JButton generateKeyPairButton = new JButton("Generate Key Pair");
        
        addCredentialButton.addActionListener(e -> new AddCredentialPanel());
        lookupCredentialButton.addActionListener(e -> new LookupCredentialPanel());
        generatePasswordButton.addActionListener(e -> new GenerateRandomPasswordPanel());
        addPrivateKeyButton.addActionListener(e -> new AddServiceAndPrivateKeyPanel());
        lookupPrivateKeyButton.addActionListener(e -> new LookupPrivateKeyPanel());
        generateKeyPairButton.addActionListener(e -> new AddServiceAndKeyGenPanel());
        
        add(welcomeLabel);
        add(addCredentialButton);
        add(lookupCredentialButton);
        add(generatePasswordButton);
        add(addPrivateKeyButton);
        add(lookupPrivateKeyButton);
        add(generateKeyPairButton);
    }
    
}
