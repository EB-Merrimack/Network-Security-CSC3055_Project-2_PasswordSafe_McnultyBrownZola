package Vault;

import merrimackutil.json.JsonIO;
import merrimackutil.json.parser.JSONParser;
import merrimackutil.json.parser.ast.SyntaxTree;
import merrimackutil.json.types.JSONObject;
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
        File file = new File(VAULT_FILE);

        if (file.exists()) {
            System.out.println("Vault file found: " + file.getAbsolutePath());
            try {
                JSONParser parser = new JSONParser(file);
                SyntaxTree tree = parser.parse();

                if (tree == null || tree.getRootNode() == null) {
                    System.err.println("Error: Vault JSON is invalid, root node is null!");
                    return createNewVault();
                }

                JSONType jsonObject = (JSONType) tree.getRootNode().evaluate();
                if (jsonObject == null) {
                    System.err.println("Error: Parsed JSON is null!");
                    return createNewVault();
                }

                Vault loadedVault = new Vault();
                loadedVault.deserialize(jsonObject);
                System.out.println("Vault loaded successfully.");
                return loadedVault;
            } catch (Exception e) {
                System.err.println("Error parsing vault: " + e.getMessage());
            }
        }

        return createNewVault(); // ✅ Ensure a fresh vault is created if the file is missing or invalid
    }

    private Vault createNewVault() {
        System.out.println("Creating a new vault...");
        Vault newVault = new Vault();
        this.vault = newVault; // ✅ Ensure `vault` is assigned
        saveVault();  // ✅ Save the newly created vault
        return newVault;
    }

    public void saveVault() {
        File file = new File(VAULT_FILE);
        try {
            JSONObject jsonVault = (JSONObject) vault.toJSONType();
            
            if (jsonVault == null || jsonVault.isEmpty()) { // ✅ Prevent saving empty JSON
                System.err.println("Error: Vault JSON is empty, not saving.");
                return;
            }
    
            // ✅ Manually format JSON for readability
            String formattedJson = jsonVault.toString();
            formattedJson = formattedJson.replace(",", ",\n"); // ✅ Add line breaks
            
            JsonIO.writeSerializedObject(vault, file);
            System.out.println("Vault saved successfully.");
        } catch (FileNotFoundException e) {
            System.err.println("Error saving vault: " + e.getMessage());
        }
    }
}