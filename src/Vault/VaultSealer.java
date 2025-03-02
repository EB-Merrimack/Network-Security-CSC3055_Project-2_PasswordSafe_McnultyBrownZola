package Vault;

import java.io.File;
import java.util.Base64;

import javax.crypto.SecretKey;

import Gui.GUIBuilder;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONArray;

public class VaultSealer {
public VaultSealer(Vault vault) {
        //TODO Auto-generated constructor stub
    }

public void sealVault() {
            try {
                // Retrieve the stored user password from GUIBuilder
                String rootPassword = GUIBuilder.getUserPassword();
                if (rootPassword == null || rootPassword.isEmpty()) {
                    throw new IllegalStateException("Root password is null or empty!");
                }
        
                System.out.println("🔍 Debug: Retrieved user password.");
        
                // Ensure salt is generated and properly stored
                if (this.salt == null || this.salt.isEmpty()) {
                    System.out.println("🔍 Debug: Salt is missing, attempting to load from JSON...");
        
                    // Try to load the salt from the saved JSON file (vault.json)
                    File vaultFile = new File("src\\json\\vault.json");
                    if (vaultFile.exists()) {
                        Vault loadedVault = Vault.loadVault(vaultFile);
                        this.salt = loadedVault.getSalt();  // Retrieve the salt from the loaded Vault
                        System.out.println("🔍 Debug: Salt loaded from JSON: " + this.salt);
                    } else {
                        throw new IllegalStateException("Vault file does not exist and salt is missing!");
                    }
                }
        
                byte[] saltBytes = Base64.getDecoder().decode(this.salt);  // Use this.salt instead of getSalt()
                System.out.println("🔍 Debug: Salt (Base64): " + this.salt);
        
                // Derive root key from the user password and retrieved salt
                SecretKey rootKey = VaultEncryption.deriveRootKey(rootPassword, saltBytes);
                System.out.println("🔍 Debug: Root key successfully derived.");
        
                // Retrieve the vault key using the derived root key
                SecretKey vaultKey = VaultEncryption.getVaultKey(this, rootKey);
                if (vaultKey == null) {
                    throw new IllegalStateException("Vault key is not initialized properly!");
                }
        
                System.out.println("🔍 Debug: Vault key successfully retrieved using root key.");
        
                // Encrypt the in-memory vault data (passwords and private keys)
                JSONArray encryptedPasswords = VaultEncryption.encryptJSONArray(this.passwords, vaultKey);
                JSONArray encryptedPrivKeys = VaultEncryption.encryptJSONArray(this.privKeys, vaultKey);
                System.out.println("🔍 Debug: Vault data encrypted.");
        
                // Encrypt the vault key with the root key
                byte[] vaultKeyIV = Base64.getDecoder().decode(this.getVaultKeyIV());
                this.encryptedVaultKey = VaultEncryption.encryptAESGCM(vaultKey.getEncoded(), rootKey, vaultKeyIV);
                System.out.println("🔍 Debug: Vault key encrypted with root key.");
        
                // Write sealed vault data using JSONSerializable
                JsonIO.writeSerializedObject(this, new File("./json/vault.json"));
                System.out.println("✅ Vault sealed successfully!");
        
            } catch (Exception e) {
                System.err.println("❌ Error sealing vault: " + e.getMessage());
                e.printStackTrace();
            }
}
}