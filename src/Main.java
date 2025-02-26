import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import Gui.GUIBuilder;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.parser.JSONParser;
import merrimackutil.json.parser.ast.SyntaxTree;

import java.util.Map;

public class Main {
    private static final String vaultfile = "src/json/vault.json";

    public static void main(String[] args) throws Exception {
        GUIBuilder gui = new GUIBuilder();
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.setSize(800, 600);
        gui.setVisible(true);

        File vaultFile = new File(vaultfile);
        final Vault[] vault = new Vault[1]; // Use a single-element array to hold the Vault object

        if (!vaultFile.exists()) {
            try {
                vault[0] = new Vault();
                writeFormattedObject(vault[0], vaultFile);
                gui.showPopupMessage("Vault created and saved to " + vaultfile);
            } catch (IOException e) {
                gui.showPopupMessage("Error creating vault: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            try {
                vault[0] = readFormattedObject(vaultFile);
                gui.showPopupMessage("Vault loaded from " + vaultfile);
            } catch (IOException e) {
                gui.showPopupMessage("Error loading vault: " + e.getMessage());
                e.printStackTrace();
            }
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                writeFormattedObject(vault[0], vaultFile);
                gui.showPopupMessage("Vault saved to " + vaultfile);
            } catch (IOException e) {
                gui.showPopupMessage("Error saving vault: " + e.getMessage());
                e.printStackTrace();
            }
        }));
    }

    static void writeFormattedObject(Vault vault, File file) throws IOException {
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            boolean created = parentDir.mkdirs();
            if (!created) {
                throw new IOException("Failed to create directory: " + parentDir.getAbsolutePath());
            }
        }

        if (!file.exists()) {
            boolean created = file.createNewFile();
            if (!created) {
                throw new IOException("Failed to create file: " + file.getAbsolutePath());
            }
        }

        JSONObject json = vault.getKeyBlock().getData();  // Retrieve data from KeyBlock
        Files.write(Paths.get(file.toURI()), json.toString().getBytes());
    }

    private static Vault readFormattedObject(File file) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(file.toURI()))).trim();

        if (content.isEmpty() || content.equals("{}")) {
            return VaultInitialization.initializeVault(file);
        }

        JSONParser parser = new JSONParser(content);
        SyntaxTree syntaxTree = parser.parse();

        if (syntaxTree == null) {
            throw new IOException("JSON parsing failed: Syntax tree is null.");
        }

        Object evaluatedData = syntaxTree.evaluate();
        if (!(evaluatedData instanceof Map)) {
            throw new IOException("Invalid JSON structure in vault file");
        }

        JSONObject json = new JSONObject((Map<String, Object>) evaluatedData);
        Vault vault = new Vault();
        vault.setKeyBlock(new KeyBlock(json));  // Pass in the non-nested KeyBlock class
        
        return vault;
    }

    private static int promptUserForVaultCreation() {
        String message = "The vault file is empty. Would you like to create a new vault?";
        String title = "Vault Creation";
        int choice = JOptionPane.showConfirmDialog(null, message, title, JOptionPane.YES_NO_OPTION);
        return choice; // Returns 0 for Yes, 1 for No
    }
}
