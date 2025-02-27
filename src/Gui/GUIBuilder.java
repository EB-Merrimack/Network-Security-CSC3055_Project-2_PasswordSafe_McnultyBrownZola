package Gui;

import javax.swing.*;
import java.awt.*;
import java.io.File;

public class GUIBuilder extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;

    public GUIBuilder() {
        setTitle("Secrets Vault");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(new LoginPanel(), "Login");
        mainPanel.add(new MainPanel(), "Main");
        mainPanel.add(new VaultInitializationPanel(), "VaultInitialization"); // Add VaultInitialization panel
        
        add(mainPanel);

        // Check if vault.json exists and show the appropriate panel
        if (isVaultInitialized()) {
            // Vault exists, show Login panel
            cardLayout.show(mainPanel, "Login");
        } else {
            // Vault doesn't exist, show Vault Initialization panel
            cardLayout.show(mainPanel, "VaultInitialization");
        }
    }

    // Method to check if vault.json exists
    private boolean isVaultInitialized() {
        File vaultFile = new File("vault.json");
        return vaultFile.exists();
    }

    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUIBuilder::new);
    }

    public void showPopupMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Message", JOptionPane.INFORMATION_MESSAGE);
    }
}
