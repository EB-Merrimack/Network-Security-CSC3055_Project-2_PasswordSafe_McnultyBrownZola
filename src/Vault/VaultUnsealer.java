package Vault;

import merrimackutil.json.JsonIO;
import merrimackutil.json.parser.JSONParser;
import merrimackutil.json.types.JSONType;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Base64;
import javax.crypto.SecretKey;


public class VaultUnsealer {
    public static Vault unsealVault(File file, String password) {
        try {
            // Load the vault file
            Vault vault = Vault.loadVault(file);

            // Derive root key from password
            SecretKey rootKey = VaultEncryption.deriveRootKey(password, Base64.getDecoder().decode(vault.getSalt()));

            // Decrypt the vault key
            byte[] decryptedVaultKey = VaultEncryption.decryptAESGCM(
                Base64.getDecoder().decode(vault.getVaultKeyValue()),
                rootKey,
                Base64.getDecoder().decode(vault.getVaultKeyIV())
            );

            // Verify that the vault key is correct
            if (decryptedVaultKey.length != 32) {
                throw new SecurityException("Invalid password. Unable to unseal vault.");
            }

            // Use the decrypted vault key to decrypt vault contents
            byte[] decryptedVaultData = VaultEncryption.decryptAESGCM(
                Base64.getDecoder().decode(vault.getVaultKeyValue()),
                VaultEncryption.getVaultKey(vault, rootKey),
                Base64.getDecoder().decode(vault.getVaultKeyIV())
            );

            // Convert decrypted JSON data back into a Vault object
            String vaultJson = new String(decryptedVaultData);
           // Parse JSON using JSONParser correctly
           JSONParser parser = new JSONParser(vaultJson); // Instantiate parser
           vault.deserialize((JSONType) parser.parse()); // Parse and deserialize

            System.out.println("Vault successfully unsealed.");
            return vault;

        } catch (Exception e) {
            System.err.println("Error unsealing vault: " + e.getMessage());
            return null;
        }
    }
} 
