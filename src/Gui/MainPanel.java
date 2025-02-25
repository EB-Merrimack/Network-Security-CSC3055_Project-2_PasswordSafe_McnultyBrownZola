package Gui;

import javax.swing.*;

public class MainPanel extends JPanel{
    public MainPanel() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        
        JLabel welcomeLabel = new JLabel("Welcome to the Secrets Vault");
        JButton addSecretButton = new JButton("Add New Secret");
        JButton lookupSecretButton = new JButton("Lookup Secret");
        
        add(welcomeLabel);
        add(addSecretButton);
        add(lookupSecretButton);
    }
    
}
