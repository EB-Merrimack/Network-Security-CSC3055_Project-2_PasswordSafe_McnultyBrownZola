package Gui;

import javax.swing.*;
import Vault.Vault;
import Vault.VaultOpener;
import Vault.VaultSealer;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;
import java.awt.*;
import java.io.File;

public class GUIBuilder extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private Vault vault;
    private static final String VAULT_FILE = "src/json/vault.json";
    private String userPassword;

    public GUIBuilder() {
        setTitle("Secrets Vault");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Prevent window from closing immediately
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        File file = new File(VAULT_FILE);
        File encFile = new File("src/json/vault.enc");

        if (encFile.exists()) {
            // Prompt for password if the encrypted file exists
            promptForPasswordAndUnseal();
        } else if (file.exists()) {
            // If vault.json exists, load it
            try {
                JSONObject jsonVault = JsonIO.readObject(file);
                vault = new Vault();
                vault.deserialize(jsonVault);
                System.out.println("Vault loaded successfully.");
            } catch (Exception e) {
                System.err.println("Error loading vault: " + e.getMessage());
                vault = new Vault(); // Create a new vault if loading fails
            }
        } else {
            // If no vault file exists, create a new vault
            System.out.println("Creating a new vault...");
            vault = new Vault();
            saveVault(); // Save new vault immediately
        }

        // Add login & main menu panels
        mainPanel.add(new LoginPanel(this, vault), "Login");
        mainPanel.add(new MainPanel(this, vault), "Main");

        add(mainPanel);

        // Initially show the Login panel
        cardLayout.show(mainPanel, "Login");

        // Set custom close operation with a confirmation prompt if logged in
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (isUserLoggedIn()) {
                    int result = JOptionPane.showConfirmDialog(GUIBuilder.this,
                            "You are currently logged in. Closing the application will log you out. Are you sure you want to close and logout?",
                            "Confirm Logout", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (result == JOptionPane.YES_OPTION) {
                        try {
                            logoutAndSealVault();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        System.exit(0);  // Close the application
                    }
                    if (result == JOptionPane.NO_OPTION) {
                        ((JFrame) windowEvent.getWindow()).setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                    }
                } else {
                    System.exit(0);  // Close the application if not logged in
                }
            }
        });
    }

    private boolean isUserLoggedIn() {
        return LoginPanel.isUserLoggedIn;
    }

    // Method to log out and seal the vault
    private void logoutAndSealVault() throws Exception {
        // Create a new VaultSealer object
        VaultSealer vaultSealer = new VaultSealer(vault, userPassword);
        // Call the sealVault method to seal the vault
    }

    // Method to save the vault
    public void saveVault() {
        try {
            JsonIO.writeFormattedObject(vault, new File(VAULT_FILE));
            System.out.println("Vault saved successfully.");
        } catch (Exception e) {
            System.err.println("Error saving vault: " + e.getMessage());
        }
    }

    // Show the panel names
    public void showPanel(String panelName) {
        cardLayout.show(mainPanel, panelName); // Switches to an already added panel
    }

    public void showPanel(String panelName, JPanel panel) {
        mainPanel.add(panel, panelName);  // Dynamically adds a new panel if needed
        cardLayout.show(mainPanel, panelName);
    }

    // Show pop-up messages
    public void showPopupMessage(String message) {
        JOptionPane.showMessageDialog(this, message, "Message", JOptionPane.INFORMATION_MESSAGE);
    }

    // Getters and setters
    public Vault getVault() {
        return this.vault;
    }

    public void setUserPassword(String password) {
        this.userPassword = password;
    }

    public String getUserPassword() {
        return this.userPassword;
    }

    // Prompt user for password and try to unseal the vault
    private void promptForPasswordAndUnseal() {
        JPasswordField passwordField = new JPasswordField(20);
        int option = JOptionPane.showConfirmDialog(this, passwordField, 
                "Enter Password to Unseal Vault", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            userPassword = new String(passwordField.getPassword());
            VaultOpener vaultOpener = new VaultOpener(userPassword);  // Ensure userPassword is set
            try {
                if (vaultOpener.unseal(vault)) {
                    System.out.println("Vault successfully unsealed and loaded.");
                } else {
                    JOptionPane.showMessageDialog(this, "Incorrect password. Vault unsealing failed.", 
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error unsealing the vault: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Password input canceled. Exiting application.", 
                    "Exit", JOptionPane.WARNING_MESSAGE);
            System.exit(0);  // Exit the application if the user cancels
        }
    }
}
