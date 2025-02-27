package Vault;

import merrimackutil.json.JsonIO;
import merrimackutil.json.parser.JSONParser;
import merrimackutil.json.parser.ast.SyntaxTree;
import merrimackutil.json.types.JSONType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InvalidObjectException;

public class VaultManager {
    private static final String VAULT_FILE = "src/json/vault.json";
    private Vault vault;

    public VaultManager() {
        this.vault = loadVault();
    }

    public Vault getVault() {
        return vault;
    }

    public Vault loadVault() {
        System.out.println("Loading vault..."); // ✅ Debugging Output

        File file = new File(VAULT_FILE);
        if (file.exists()) {
            System.out.println("Vault file found: " + file.getAbsolutePath()); // ✅ Debugging Output
            try {
                JSONParser parser = new JSONParser(file);
                SyntaxTree tree = parser.parse();
    
                if (tree.getRootNode() == null) { // ✅ Handle null SyntaxNode
                    System.err.println("Error: Vault JSON is invalid, root node is null!");
                    return new Vault(); // ✅ Return a new Vault instead of crashing
                }
    
                JSONType jsonObject = (JSONType) tree.getRootNode().evaluate();
    
                Vault loadedVault = new Vault();
                if (jsonObject != null) {
                    try {
                        loadedVault.deserialize(jsonObject);
                        System.out.println("Vault loaded successfully.");
                        return loadedVault;
                    } catch (InvalidObjectException e) {
                        System.err.println("Error: Invalid Vault JSON format - " + e.getMessage());
                    }
                } else {
                    System.err.println("Error: Loaded JSON is null!");
                }
            } catch (FileNotFoundException e) {
                System.err.println("Error loading vault: " + e.getMessage());
            }
        }
    
        System.out.println("Creating new vault...");
        Vault newVault = new Vault();
        saveVault(); // ✅ Ensure vault is saved on first-time creation
        return newVault;
    }

    public void saveVault() {
        File file = new File(VAULT_FILE);
        try {
            JsonIO.writeSerializedObject(vault, file);
            System.out.println("Vault saved successfully.");
        } catch (FileNotFoundException e) {
            System.err.println("Error saving vault: " + e.getMessage());
        }
    }
}