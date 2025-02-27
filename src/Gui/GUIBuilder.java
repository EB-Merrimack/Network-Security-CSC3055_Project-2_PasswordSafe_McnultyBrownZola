package Gui;

import javax.swing.*;

import Vault.VaultManager;

import java.awt.*;

public class GUIBuilder extends JFrame{
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private VaultManager vaultManager;


    public GUIBuilder() {

        setTitle("Secrets Vault");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        vaultManager = new VaultManager(); // ✅ Initialize VaultManager
        System.out.println("VaultManager initialized in GUI.");

        mainPanel.add(new LoginPanel(this, vaultManager), "Login");
        mainPanel.add(new MainPanel(this, vaultManager), "Main");
        
        add(mainPanel);
    
        // Initially show the Login panel
        cardLayout.show(mainPanel, "Login");
    }
    
    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName); // ✅ Switches to an already added panel
    }
    
    public void showPanel(String panelName, JPanel panel) {
        mainPanel.add(panel, panelName);  // ✅ Dynamically adds a new panel if needed
        cardLayout.show(mainPanel, panelName);
    }
    
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUIBuilder::new);
    }
    public void showPopupMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Message", JOptionPane.INFORMATION_MESSAGE);
    }
}
