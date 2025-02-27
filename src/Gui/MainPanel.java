package Gui;


import javax.swing.*;

import Vault.VaultManager;

public class MainPanel extends JPanel{
    public MainPanel(GUIBuilder parent, VaultManager manager) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JLabel welcomeLabel = new JLabel("Welcome to the Secrets Vault");
        JButton addCredentialButton = new JButton("Add Service Credentials");
        JButton lookupCredentialButton = new JButton("Lookup Credentials");
        JButton generatePasswordButton = new JButton("Generate Password");
        JButton addPrivateKeyButton = new JButton("Add Private Key");
        JButton lookupPrivateKeyButton = new JButton("Lookup Private Key");
        JButton generateKeyPairButton = new JButton("Generate Key Pair");
        
        addCredentialButton.addActionListener(e -> parent.showPanel("AddCredentialPanel", new AddCredentialPanel()));
        lookupCredentialButton.addActionListener(e -> parent.showPanel("LookupCredentialPanel", new LookupCredentialPanel()));
        generatePasswordButton.addActionListener(e -> parent.showPanel("GenerateRandomPasswordPanel", new GenerateRandomPasswordPanel()));
        //addPrivateKeyButton.addActionListener(e -> parent.showPanel("AddServiceAndPrivateKeyPanel", new AddServiceAndPrivateKeyPanel()));
        //lookupPrivateKeyButton.addActionListener(e -> parent.showPanel("LookupPrivateKeyPanel", new LookupPrivateKeyPanel()));
        //generateKeyPairButton.addActionListener(e -> parent.showPanel("AddServiceAndKeyGenPanel", new AddServiceAndKeyGenPanel()));


        add(welcomeLabel);
        add(addCredentialButton);
        add(lookupCredentialButton);
        add(generatePasswordButton);
        add(addPrivateKeyButton);
        add(lookupPrivateKeyButton);
        add(generateKeyPairButton);
    }
    
}
