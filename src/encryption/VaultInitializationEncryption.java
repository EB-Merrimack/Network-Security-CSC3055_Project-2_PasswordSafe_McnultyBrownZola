package encryption;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import merrimackutil.json.lexer.Token;
import merrimackutil.json.lexer.TokenType;
import merrimackutil.json.parser.ast.nodes.KeyValueNode;
import merrimackutil.json.parser.ast.nodes.SyntaxNode;
import merrimackutil.json.parser.ast.nodes.TokenNode;

public class VaultInitializationEncryption {

    // Method to check password strength
    public static String checkPasswordStrength(String password) {
        if (password.length() < 8) {
            return "Password must be at least 8 characters long.";
        }

        // Regex to check for at least one uppercase letter, one lowercase letter, one number, and one special character
        Pattern uppercasePattern = Pattern.compile("[A-Z]");
        Pattern lowercasePattern = Pattern.compile("[a-z]");
        Pattern digitPattern = Pattern.compile("[0-9]");
        Pattern specialCharPattern = Pattern.compile("[^a-zA-Z0-9]");

        Matcher uppercaseMatcher = uppercasePattern.matcher(password);
        Matcher lowercaseMatcher = lowercasePattern.matcher(password);
        Matcher digitMatcher = digitPattern.matcher(password);
        Matcher specialCharMatcher = specialCharPattern.matcher(password);

        if (!uppercaseMatcher.find()) {
            return "Password must contain at least one uppercase letter.";
        }
        if (!lowercaseMatcher.find()) {
            return "Password must contain at least one lowercase letter.";
        }
        if (!digitMatcher.find()) {
            return "Password must contain at least one number.";
        }
        if (!specialCharMatcher.find()) {
            return "Password must contain at least one special character.";
        }

        // If all checks pass, password is strong
        return "Strong";
    }

    // Method to generate a random vault key (example using AES)
    private static SecretKey generateVaultKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128); // AES key size (128 bits)
        return keyGenerator.generateKey();
    }

    // Method to derive the root key from the password (simulating password-based key derivation)
    private static byte[] deriveRootKeyFromPassword(String password) throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        return sha256.digest(password.getBytes());
    }

    // Method to encrypt vault data (using AES, just for illustration)
    private static String encryptData(String data, SecretKey vaultKey) throws Exception {
        // A real encryption method should be used here. We're just simulating encryption for now.
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    public static void saveVaultToFile(SecretKey vaultKey, String encryptedVaultData, byte[] salt) {
        try {
            // Encrypt the vault key before storing it in JSON (simulating encryption)
            String encryptedVaultKey = Base64.getEncoder().encodeToString(vaultKey.getEncoded());
    
            // Create a list of KeyValueNodes to simulate a JSON structure
            List<KeyValueNode> keyValueNodes = new ArrayList<>();
    
            // Create the key-value pairs using TokenNode for values
            SyntaxNode keyNode = new TokenNode(new Token(TokenType.STRING, "encryptedVaultKey"));
            SyntaxNode valueNode = new TokenNode(new Token(TokenType.STRING, encryptedVaultKey));
            KeyValueNode encryptedVaultKeyNode = new KeyValueNode(keyNode, valueNode);
    
            keyNode = new TokenNode(new Token(TokenType.STRING, "encryptedVaultData"));
            valueNode = new TokenNode(new Token(TokenType.STRING, encryptedVaultData));
            KeyValueNode encryptedVaultDataNode = new KeyValueNode(keyNode, valueNode);
    
            keyNode = new TokenNode(new Token(TokenType.STRING, "salt"));
            valueNode = new TokenNode(new Token(TokenType.STRING, Base64.getEncoder().encodeToString(salt)));
            KeyValueNode saltNode = new KeyValueNode(keyNode, valueNode);
    
            // Add the key-value pairs to the list
            keyValueNodes.add(encryptedVaultKeyNode);
            keyValueNodes.add(encryptedVaultDataNode);
            keyValueNodes.add(saltNode);
    
            // Convert the list of KeyValueNodes to a JSON-like string representation
            StringBuilder jsonStringBuilder = new StringBuilder("{\n");
            for (KeyValueNode node : keyValueNodes) {
                // Safely handle the KeyNode and ValueNode as TokenNode
                String key = "";
                String value = "";
    
                // Check if the key node is a TokenNode
                if (node.getKey() instanceof TokenNode) {
                    key = ((TokenNode) node.getKey()).getValue();
                }
    
                // Check if the value node is a TokenNode
                if (node.getValue() instanceof TokenNode) {
                    value = ((TokenNode) node.getValue()).getValue();
                }
    
                jsonStringBuilder.append("  \"").append(key)
                        .append("\": \"").append(value).append("\",\n");
            }
    
            // Remove the last comma and add closing brace
            jsonStringBuilder.setLength(jsonStringBuilder.length() - 2);
            jsonStringBuilder.append("\n}");
    
            // Write the JSON string to file
            Files.write(Paths.get("vault.json"), jsonStringBuilder.toString().getBytes());
    
            System.out.println("Vault data saved to vault.json");
    
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to save vault data to vault.json");
        }
    }
    
    // Method to simulate the process of initializing and saving vault data
    public static boolean initializeVault(String password) {
        try {
            // Generate a salt for this example (should be a random salt)
            byte[] salt = new byte[16];
            new java.security.SecureRandom().nextBytes(salt);

            // Derive the root key from the password
            byte[] rootKey = deriveRootKeyFromPassword(password);

            // Generate a vault key (for this example, use AES key generation)
            SecretKey vaultKey = generateVaultKey();

            // Encrypt the vault data (example: just use Base64 encoding for this simulation)
            String vaultData = "This is the secret vault data.";  // Example vault data
            String encryptedVaultData = encryptData(vaultData, vaultKey);

            // Save the encrypted vault data and vault key to vault.json
            saveVaultToFile(vaultKey, encryptedVaultData, salt);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }    
}

       
