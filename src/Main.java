import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.swing.JFrame;
import Gui.GUIBuilder;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.parser.JSONParser;
import merrimackutil.json.parser.ast.SyntaxTree;

import java.util.Base64;
import java.util.Map;

public class Main {
    private static final String vaultfile = "src/json/vault.json";

    public static void main(String[] args) {
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

    private static void writeFormattedObject(Vault vault, File file) throws IOException {
        // Ensure the directory exists
        File parentDir = file.getParentFile();
        if (!parentDir.exists()) {
            boolean created = parentDir.mkdirs(); // Creates the directory if it doesn't exist
            if (!created) {
                throw new IOException("Failed to create directory: " + parentDir.getAbsolutePath());
            }
        }
    
        // Create the file if it doesn't exist
        if (!file.exists()) {
            boolean created = file.createNewFile(); // Creates the file if it doesn't exist
            if (!created) {
                throw new IOException("Failed to create file: " + file.getAbsolutePath());
            }
        }
    
        // Get JSON representation and write to file
        JSONObject json = vault.getKeyBlock().getData();
        Files.write(Paths.get(file.toURI()), json.toString().getBytes());
    }
    
    private static Vault readFormattedObject(File file) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(file.toURI())));
    
        // Parse the JSON file
        JSONParser parser = new JSONParser(content);
        SyntaxTree syntaxTree = parser.parse(); 
    
        // Evaluate the syntax tree
        Object evaluatedData = syntaxTree.evaluate();
    
        if (!(evaluatedData instanceof Map)) {
            throw new IOException("Invalid JSON structure in vault file");
        }
    
        JSONObject json = new JSONObject((Map<String, Object>) evaluatedData);
    
        Vault vault = new Vault();
        vault.setKeyBlock(new KeyBlock(json)); // Ensure KeyBlock constructor supports JSONObject
        return vault;
    }
}