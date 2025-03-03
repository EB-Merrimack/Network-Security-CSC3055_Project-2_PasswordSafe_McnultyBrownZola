package Vault;

import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;
import merrimackutil.json.JSONSerializable;
import merrimackutil.json.JsonIO;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InvalidObjectException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import Gui.GUIBuilder;


public class Vault implements JSONSerializable {
    //initalize seal for vault opening and clossing
   
    private String salt;
    private String rootPasswordHash;
    private JSONArray passwords;
    private JSONArray privKeys;
    private String vaultKeyIV;   // Stores IV (Base64)
    private String vaultKeyValue;
        private byte[] encryptedVaultKey;
    
        public Vault() {
            if (this.salt == null || this.salt.isEmpty()) {
                generateSalt();
            }     
            this.rootPasswordHash = null; // Set when a root password is created
            this.vaultKeyIV = "";
            this.vaultKeyValue = "";
            this.passwords = new JSONArray();
            this.privKeys = new JSONArray();
        }
        
    
        public String getRootPasswordHash() {
            return this.rootPasswordHash;
        }
    
        // ✅ Set Root Password (Hash & Save)
        public void setRootPassword(String password) {
            System.out.println("🔍 Debug: Storing Vault Password: " + password);
            
            this.rootPasswordHash = hashPassword(password);
        
            if (this.salt == null || this.salt.isEmpty()) {
                byte[] saltBytes = VaultEncryption.generateRandomIV();
                this.salt = Base64.getEncoder().encodeToString(saltBytes);
                System.out.println("✅ Debug: Generated New Salt: " + this.salt);
            } else {
                System.out.println("✅ Debug: Using Existing Salt: " + this.salt);
            }
        
            byte[] iv = VaultEncryption.generateRandomIV();
            this.vaultKeyIV = Base64.getEncoder().encodeToString(iv);
        
            try {
                byte[] vaultKey = VaultEncryption.generateRandomKey();
                SecretKey rootKey = VaultEncryption.deriveRootKey(password, Base64.getDecoder().decode(this.salt));
                byte[] encryptedVaultKey = VaultEncryption.encryptAESGCM(vaultKey, rootKey, iv);
        
                this.vaultKeyValue = Base64.getEncoder().encodeToString(encryptedVaultKey);
                System.out.println("✅ Debug: Vault Key Generated and Encrypted Successfully!");
        
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    
        // ✅ Verify Password
        public boolean verifyRootPassword(String password) {
            try {
                System.out.println("🔍 Debug: Verifying Root Password...");
                System.out.println("🔍 Debug: Stored Salt (Base64): " + this.salt);
        
                // Derive Root Key
                SecretKey rootKey = VaultEncryption.deriveRootKey(password, Base64.getDecoder().decode(this.salt));
                System.out.println("✅ Debug: Derived Root Key (Base64): " + Base64.getEncoder().encodeToString(rootKey.getEncoded()));
        
                // Decrypt Vault Key
                byte[] encryptedVaultKey = Base64.getDecoder().decode(this.vaultKeyValue);
                byte[] iv = Base64.getDecoder().decode(this.vaultKeyIV);
        
                System.out.println("🔍 Debug: Encrypted Vault Key (Base64): " + this.vaultKeyValue);
                System.out.println("🔍 Debug: Vault Key IV (Base64): " + this.vaultKeyIV);
        
                byte[] decryptedVaultKey = VaultEncryption.decryptAESGCM(encryptedVaultKey, rootKey, iv);
        
                System.out.println("✅ Debug: Vault Key Decryption Successful! Length: " + decryptedVaultKey.length);
        
                return decryptedVaultKey.length == 32; // ✅ Check if decryption was successful
            } catch (Exception e) {
                System.err.println("❌ Error: Vault Key Decryption Failed - " + e.getMessage());
                return false; // If decryption fails, the password is incorrect
            }
        }
    
        // ✅ Hash Password Using SHA-256
        public String hashPassword(String password) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                byte[] hashedBytes = digest.digest(password.getBytes());
                return Base64.getEncoder().encodeToString(hashedBytes);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException("Error hashing password", e);
            }
        }
    
        // 🔹 Generate a random Base64-encoded salt
        public String generateSalt() {
            byte[] saltBytes = new byte[16];
            new java.security.SecureRandom().nextBytes(saltBytes);
            return Base64.getEncoder().encodeToString(saltBytes);
        }
    
        // 🔹 Convert Vault to JSON
        @Override
        public JSONType toJSONType() {
            JSONObject json = new JSONObject();
            json.put("salt", this.salt);
            json.put("rootPasswordHash", this.rootPasswordHash);
            
            JSONObject vaultKeyObject = new JSONObject();
            vaultKeyObject.put("iv", this.vaultKeyIV);
            vaultKeyObject.put("key", this.vaultKeyValue);
            json.put("vaultkey", vaultKeyObject);
    
    
            json.put("passwords", this.passwords);
            json.put("privkeys", this.privKeys);
            return json;
        }
    
        // 🔹 Deserialize Vault from JSON
        @Override
        public void deserialize(JSONType jsonType) throws InvalidObjectException {
            if (!(jsonType instanceof JSONObject)) {
                throw new InvalidObjectException("Invalid JSON format for Vault.");
            }
            JSONObject json = (JSONObject) jsonType;
    
            if (json.containsKey("salt")) {
                Object saltObj = json.get("salt");
                if (saltObj instanceof String) {
                    this.salt = (String) saltObj;
                } else if (saltObj instanceof byte[]) {
                    // Convert byte array to Base64 string if necessary
                    this.salt = Base64.getEncoder().encodeToString((byte[]) saltObj);
                } else {
                    // Fallback: force to string
                    this.salt = saltObj.toString();
                }
                System.out.println("✅ Debug: Loaded Salt from JSON: " + this.salt);
            } else {
                System.out.println("⚠ Warning: No salt found in JSON.");
                this.salt = "";
            }
        
            // If salt is still empty, generate a new one and assign it!
            if (this.salt == null || this.salt.isEmpty()) {
                this.salt = generateSalt();
                System.out.println("✅ Debug: Generated New Salt in deserialize: " + this.salt);
            }
    
            this.rootPasswordHash = json.getString("rootPasswordHash");
            JSONObject vaultKeyJSON = json.getObject("vaultkey");
            if (vaultKeyJSON != null) {
                this.vaultKeyIV = vaultKeyJSON.getString("iv");
                this.vaultKeyValue = vaultKeyJSON.getString("key");
            } else {
                this.vaultKeyIV = "";
                this.vaultKeyValue = "";
            }
    
            this.passwords = json.getArray("passwords");
            this.privKeys = json.getArray("privkeys");
        }
    
        /**
         * Sets the vault key IV and value from given parameters.
         * Both parameters should be Base64 strings.
         * @param iv the initialization vector for AES-GCM encryption
         * @param key the encrypted vault key
         */
        public void setVaultKey(String iv, String key) {
            this.vaultKeyIV = iv;
            this.vaultKeyValue = key;
        }
        
        /**
         * Returns the initialization vector (IV) used for encrypting the Vault key.
         * The IV is a Base64-encoded string.
         * @return the IV used for encrypting the Vault key
         */
        public String getVaultKeyIV() {
            return this.vaultKeyIV;
        }
        
/**
 * Returns the encrypted Vault key as a Base64-encoded string.
 * 
 * @return the Base64-encoded encrypted Vault key
 */

        public String getVaultKeyValue() {
            return this.vaultKeyValue;
        }
    
        // 🔹 Add a new password entry
        public void addPassword(String service, String user, String encryptedPass, String iv) {
            JSONObject passEntry = new JSONObject();
            passEntry.put("iv", iv);
            passEntry.put("service", service);
            passEntry.put("user", user);
            passEntry.put("pass", encryptedPass);
            passwords.add(passEntry);
        }
    
        // 🔹 Add a new private key entry
        public void addPrivateKey(String service, String privKey, String iv) {
            JSONObject privKeyEntry = new JSONObject();
            privKeyEntry.put("iv", iv);
            privKeyEntry.put("service", service);
            privKeyEntry.put("privkey", privKey);
            privKeys.add(privKeyEntry);
        }
    
        // 🔹 Save Vault to File
        public void saveVault(File file) {
            try {
                JsonIO.writeSerializedObject(this, file);
                System.out.println("Vault saved successfully.");
            } catch (FileNotFoundException e) {
                System.err.println("Error saving vault: " + e.getMessage());
            }
        }
    
        // 🔹 Load Vault from File
        public static Vault loadVault(File file) {
            if (file.exists()) {
                try {
                    JSONObject jsonType = JsonIO.readObject(file);  // ✅ FIXED: Read using `readObject`
                    Vault vault = new Vault();
                    vault.deserialize(jsonType);
                    System.out.println("Vault loaded successfully.");
                    return vault;
                } catch (FileNotFoundException e) {
                    System.err.println("Vault file not found: " + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Error loading vault: " + e.getMessage());
                }
            }
            System.out.println("Creating a new vault...");
            return new Vault();
        }
    
    
        /**
         * Returns the salt used for hashing the root password as a Base64 string.
         * The salt is trimmed of any whitespace before being returned.
         * @return the salt used for hashing the root password
         */
        public String getSalt() {
            return this.salt.trim();
        }
    
    
        /**
         * Returns the list of stored passwords as a JSONArray.
         * The JSONArray stores objects of the form
         * {service: <service name>, user: <user name>, pass: <password>}
         * @return the list of stored passwords
         */
        public JSONArray getPasswords() {
           return this.passwords;     }
    
    
/**
 * Returns the list of stored private keys as a JSONArray.
 * The JSONArray stores objects of the form
 * {service: <service name>, privkey: <private key>, iv: <initialization vector>}
 * @return the list of stored private keys
 */

        public JSONArray getPrivateKeys() {
            return this.privKeys;
        }
    
        /**
         * Returns the decrypted password for the given service and user.
         * The password is decrypted using the given Vault key.
         * If the password is not found, "Not found" is returned.
         * If decryption fails, "Decryption error!" is returned.
         * @param service the service name
         * @param user the user name
         * @param vaultKey the Vault key used for decryption
         * @return the decrypted password
         */
        public String getDecryptedPassword(String service, String user, SecretKey vaultKey) {
            for (Object obj : passwords) {
                if (obj instanceof JSONObject) {  // Ensure it's a JSON object
                    JSONObject passEntry = (JSONObject) obj;
        
                    if (passEntry.getString("service").equals(service) && passEntry.getString("user").equals(user)) {
                        try {
                            byte[] encryptedPass = Base64.getDecoder().decode(passEntry.getString("pass"));
                            byte[] iv = Base64.getDecoder().decode(passEntry.getString("iv"));
        
                            System.out.println("🔍 Debug: Found Encrypted Password Entry!");
                            System.out.println("🔍 Debug: Encrypted Password (Base64): " + passEntry.getString("pass"));
                            System.out.println("🔍 Debug: IV (Base64): " + passEntry.getString("iv"));
        
                            // Decrypt password
                            byte[] decryptedPass = VaultEncryption.decryptAESGCM(encryptedPass, vaultKey, iv);
                            System.out.println("✅ Debug: Decryption Successful! Password: " + new String(decryptedPass));
        
                            return new String(decryptedPass);
                        } catch (Exception e) {
                            System.err.println("Error: Password Decryption Failed - " + e.getMessage());
                            return "Decryption error!";
                        }
                    }
                }
            }
            return "Not found";
        }



     
        
        }
        
        
        
    
    
    

    
