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

public class VaultSealer {
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_SIZE = 12;

    private final String userPassword;
    private final File jsonFile = new File("src/json/vault.json");
    private final File encFile = new File("src/json/vault.enc");

    public VaultSealer(Vault vault, String userPassword) throws Exception {
        System.out.println("🔒 Debug: Sealing Vault...");
        this.userPassword = userPassword;

        byte[] jsonData = readVaultFile();  // Read vault.json
        byte[] saltBytes = extractSaltFromJson(jsonData);  // Extract salt from JSON
        SecretKey rootKey = VaultEncryption.deriveRootKey(userPassword, saltBytes);

        byte[] encryptedData = encryptData(jsonData, rootKey);
        saveEncryptedFile(encryptedData);
        deleteUnencryptedVaultFile();
    }

    private byte[] readVaultFile() throws IOException {
        if (!jsonFile.exists()) {
            throw new FileNotFoundException("❌ Error: vault.json not found.");
        }
        return Files.readAllBytes(jsonFile.toPath());
    }

    private byte[] extractSaltFromJson(byte[] jsonData) throws Exception {
        // Use JsonIO to read the JSON object
        JSONObject jsonObject = JsonIO.readObject(new String(jsonData));


        String saltBase64 = jsonObject.getString("salt");
        return Base64.getDecoder().decode(saltBase64);
    }

    private byte[] encryptData(byte[] data, SecretKey key) throws Exception {
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);
        byte[] encryptedData = cipher.doFinal(data);

        byte[] result = new byte[IV_SIZE + encryptedData.length];
        System.arraycopy(iv, 0, result, 0, IV_SIZE);  // Store IV first
        System.arraycopy(encryptedData, 0, result, IV_SIZE, encryptedData.length);  // Then cipher text

        return result;
    }

    private void saveEncryptedFile(byte[] encryptedData) throws IOException {
        if (encFile.exists()) {
            encFile.delete();
        }
        Files.write(encFile.toPath(), encryptedData);
        System.out.println("✅ Vault successfully sealed as vault.enc");
     
    }

    private void deleteUnencryptedVaultFile() {
        if (jsonFile.exists() && jsonFile.delete()) {
            System.out.println("✅ Unencrypted vault.json deleted successfully.");
            System.exit(0);
        } else {
            System.err.println("⚠️ Warning: Could not delete vault.json (file may not exist).");
            System.exit(0);
        }
    }
    
}
