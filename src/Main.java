/*import java.io.File;
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
import json.JSONFormatter; // Assuming the formatter class is in the json package/folder

public class Main {
    private static final String vaultfile = "src/json/vault.json";
    public static KeyBlockcreation keyBlock;

    public static void main(String[] args) throws Exception {
        GUIBuilder gui = new GUIBuilder();
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.setSize(800, 600);
        gui.setVisible(true);

        File vaultFile = new File(vaultfile);
        final Vault[] vault = new Vault[1]; // Use an array to hold the Vault object reference

        if (!vaultFile.exists()) {
            try {
                vault[0] = new Vault();
                writeFormattedObject(vault[0], vaultFile);
                gui.showPopupMessage("Vault created and saved to " + vaultfile);
            } catch (IOException e) {
                gui.showPopupMessage("Error creating vault: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Try loading the vault (whether newly created or pre-existing)
        try {
            vault[0] = readFormattedObject(vaultFile);
            gui.showPopupMessage("Vault loaded from " + vaultfile);
        } catch (IOException e) {
            gui.showPopupMessage("Error loading vault: " + e.getMessage());
            e.printStackTrace();
        }

        // Ensure vault data is saved when exiting
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

        // Retrieve data from KeyBlock
        JSONObject json = vault.getKeyBlock().getData();

        // Use the JSONFormatter to format the JSON string
        String formattedJson = JSONFormatter.formatJson(json.toString());

        // Write the formatted JSON to the file
        Files.write(Paths.get(file.toURI()), formattedJson.getBytes());
    }

    private static Vault readFormattedObject(File file) throws Exception {
        String content = new String(Files.readAllBytes(Paths.get(file.toURI()))).trim();

        if (content.trim().isEmpty() || content.trim().equals("{}") || isEmptyJsonObject(content)) {
            return VaultInitialization.initializeVault(file);
        }

        // Parse the JSON file content
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

        // Set the KeyBlock correctly with the parsed JSON data
        vault.setKeyBlock(new KeyBlockcreation(json));  // Correct way to set KeyBlock

        return vault;
    }

    // Helper method to check if a JSON object contains only whitespace or empty data between curly braces
    private static boolean isEmptyJsonObject(String content) {
        // Trim the content and remove curly braces
        String trimmedContent = content.trim();
        if (trimmedContent.startsWith("{") && trimmedContent.endsWith("}")) {
            String innerContent = trimmedContent.substring(1, trimmedContent.length() - 1).trim();
            // If the content between braces is empty or contains only whitespace, it's considered empty
            return innerContent.isEmpty();
        }
        return false;
    }
}*/




import javax.swing.JFrame;

import Gui.GUIBuilder;

public class Main {

    public static void main(String[] args) throws Exception {
        GUIBuilder gui = new GUIBuilder();
        gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui.setSize(800, 600);
        gui.setVisible(true);

        
     
    }
}