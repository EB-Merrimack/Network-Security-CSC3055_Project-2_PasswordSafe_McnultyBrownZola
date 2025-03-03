package Vault;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import merrimackutil.json.JsonIO;
import merrimackutil.json.types.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Base64;

public class VaultOpener {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_SIZE = 12;
    private static final int SALT_SIZE = 16;

    private final String userPassword;
    private final File encFile = new File("src/json/vault.enc");

    public VaultOpener(String userPassword) {
        this.userPassword = userPassword;
    }

    public boolean unseal(Vault vault) {
        try {
            System.out.println("🔍 Debug: Attempting to unseal vault...");
            byte[] encryptedData = readEncryptedFile();
            SecretKey rootKey = deriveRootKey(encryptedData);
            byte[] decryptedData = decryptData(encryptedData, rootKey);
            restoreVault(decryptedData);
            return true;
        } catch (Exception e) {
            System.out.println("❌ Vault decryption failed: " + e.getMessage());
            return false;
        }
    }

    private byte[] readEncryptedFile() throws IOException {
        if (!encFile.exists()) {
            throw new FileNotFoundException("Encrypted vault file 'vault.enc' not found.");
        }
        return Files.readAllBytes(encFile.toPath());
    }

    private SecretKey deriveRootKey(byte[] encryptedData) throws Exception {
        // Extract the salt from the first SALT_SIZE bytes of the encrypted file
        byte[] saltBytes = new byte[SALT_SIZE];
        System.arraycopy(encryptedData, 0, saltBytes, 0, SALT_SIZE);

        // Derive the root key using the extracted salt
        return VaultEncryption.deriveRootKey(userPassword, saltBytes);
    }

    private byte[] decryptData(byte[] encryptedData, SecretKey key) throws Exception {
        // Extract the IV and cipher text from the encrypted data
        byte[] iv = new byte[IV_SIZE];
        System.arraycopy(encryptedData, SALT_SIZE, iv, 0, IV_SIZE); // IV follows salt

        byte[] cipherText = new byte[encryptedData.length - (SALT_SIZE + IV_SIZE)];
        System.arraycopy(encryptedData, SALT_SIZE + IV_SIZE, cipherText, 0, cipherText.length);

        // Decrypt the data
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);
        return cipher.doFinal(cipherText);
    }

    private void restoreVault(byte[] decryptedData) throws IOException {
        // Create the decrypted vault JSON file
        File jsonFile = new File("src/json/vault.json");
        if (jsonFile.exists()) {
            jsonFile.delete();  // Ensure the file is deleted before saving
        }
        Files.write(jsonFile.toPath(), decryptedData);
        System.out.println("✅ Vault successfully restored as vault.json");
    }
}
