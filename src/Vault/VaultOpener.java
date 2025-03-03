package Vault;

import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONArray;
import java.io.File;
import java.io.FileNotFoundException;

public class VaultOpener {
    public VaultOpener(String password2) {
        try {
            // Read the vault JSON file into a JSONObject
            File vaultFile = new File("src/json/vault.json");
            JSONObject vaultData = JsonIO.readObject(vaultFile);

            // Extract individual JSON elements (salt, rootPasswordHash, etc.)
            String salt = vaultData.getString("salt");
            String rootPasswordHash = vaultData.getString("rootPasswordHash");
            JSONObject vaultKeyObj = vaultData.getObject("vaultkey");
            String vaultKeyIV = vaultKeyObj.getString("iv");
            String encryptedVaultKey = vaultKeyObj.getString("encryptedVaultKey");
            String encryptedVaultData = vaultData.getString("encryptedVaultData");

            // Decrypt vault data and the vault key (implement decryption logic here)
            System.out.println("Vault Data Decrypted:");
            System.out.println("Salt: " + salt);
            System.out.println("Root Password Hash: " + rootPasswordHash);
            System.out.println("Encrypted Vault Key: " + encryptedVaultKey);
            System.out.println("Encrypted Vault Data: " + encryptedVaultData);

            // Extract private keys and passwords (if any) and handle them
            JSONArray privKeys = vaultData.getArray("privkeys");
            JSONArray passwords = vaultData.getArray("passwords");

            // Process private keys
            for (int i = 0; i < privKeys.size(); i++) { // Use .size() instead of .length()
                JSONObject privKey = (JSONObject) privKeys.get(i);  // Use get(i) to access each element
                String service = privKey.getString("service");
                String iv = privKey.getString("iv");
                String encryptedPrivKey = privKey.getString("privkey");
                System.out.println("Service: " + service);
                System.out.println("IV: " + iv);
                System.out.println("Encrypted Private Key: " + encryptedPrivKey);
            }

            // Process passwords
            for (int i = 0; i < passwords.size(); i++) { // Use .size() instead of .length()
                JSONObject password = (JSONObject) passwords.get(i);  // Use get(i) to access each element
                String service = password.getString("service");
                String user = password.getString("user");
                String pass = password.getString("pass");
                String iv = password.getString("iv");
                System.out.println("Service: " + service);
                System.out.println("User: " + user);
                System.out.println("Encrypted Password: " + pass);
            }

        } catch (FileNotFoundException e) {
            System.err.println("Vault file not found: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error processing vault data: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
