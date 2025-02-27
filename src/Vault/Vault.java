package Vault;

import merrimackutil.json.parser.ast.nodes.ClassNode;
import merrimackutil.json.parser.ast.nodes.KeyValueNode;
import merrimackutil.json.parser.ast.nodes.TokenNode;
import merrimackutil.json.types.JSONArray;
import merrimackutil.json.types.JSONObject;
import merrimackutil.json.types.JSONType;
import merrimackutil.json.JSONSerializable;
import merrimackutil.json.lexer.Token;
import merrimackutil.json.lexer.TokenType;
import merrimackutil.json.parser.JSONParser;
import merrimackutil.json.parser.ast.nodes.ArrayNode;
import merrimackutil.json.parser.ast.nodes.SyntaxNode;
import merrimackutil.util.Tuple;

import java.io.InvalidObjectException;
import java.lang.reflect.Field;
import java.util.Base64;
import java.util.List;

import org.bouncycastle.crypto.generators.SCrypt;

public class Vault implements JSONSerializable{
    private ClassNode vaultData;
    private String salt;
    private ClassNode vaultKey;
    private ArrayNode passwords;
    private ArrayNode privKeys;
    private String rootPasswordHash;
    

    public Vault() {
        this.vaultData = new ClassNode();
        this.passwords = new ArrayNode();  
        this.privKeys = new ArrayNode();
        this.vaultKey = new ClassNode();
        this.salt = generateSalt();
        this.rootPasswordHash = "";
        initVaultStructure();
    }

    // Initialize vault structure with empty key blocks
   private void initVaultStructure() {
    vaultData.addKVPair(new KeyValueNode(
        new TokenNode(new Token(TokenType.STRING, "salt")),
        new TokenNode(new Token(TokenType.STRING, ""))
    ));

    vaultData.addKVPair(new KeyValueNode(
        new TokenNode(new Token(TokenType.STRING, "vaultkey")),
        createVaultKeyBlock()
    ));

    vaultData.addKVPair(new KeyValueNode(
        new TokenNode(new Token(TokenType.STRING, "passwords")),
        new ArrayNode()
    ));

    vaultData.addKVPair(new KeyValueNode(
        new TokenNode(new Token(TokenType.STRING, "privkeys")),
        new ArrayNode()
    ));
}

   
    private ClassNode createVaultKeyBlock() {
        ClassNode vaultKeyBlock = new ClassNode();
    
        vaultKeyBlock.addKVPair(new KeyValueNode(
            new TokenNode(new Token(TokenType.STRING, "iv")),
            new TokenNode(new Token(TokenType.STRING, ""))
        ));
    
        vaultKeyBlock.addKVPair(new KeyValueNode(
            new TokenNode(new Token(TokenType.STRING, "key")),
            new TokenNode(new Token(TokenType.STRING, ""))
        ));
    
        return vaultKeyBlock;
    }

    private String hashPassword(String password) {
        int cost = 2048, blockSize = 8, parallelization = 1, keyLength = 32;
        byte[] derivedKey = SCrypt.generate(password.getBytes(), salt.getBytes(), cost, blockSize, parallelization, keyLength);
        return Base64.getEncoder().encodeToString(derivedKey);
    }

    public String getRootPasswordHash() {
        return rootPasswordHash;
    }

    public void setRootPassword(String password) {
        this.rootPasswordHash = hashPassword(password);
    }

    public boolean verifyRootPassword(String inputPassword) {
        return rootPasswordHash.equals(hashPassword(inputPassword));
    }


    // Add a password entry
    public void addPassword(String service, String user, String encryptedPass, String iv) {
        ClassNode passBlock = new ClassNode();
    
        passBlock.addKVPair(new KeyValueNode(
            new TokenNode(new Token(TokenType.STRING, "iv")),
            new TokenNode(new Token(TokenType.STRING, iv))
        ));
    
        passBlock.addKVPair(new KeyValueNode(
            new TokenNode(new Token(TokenType.STRING, "service")),
            new TokenNode(new Token(TokenType.STRING, service))
        ));
    
        passBlock.addKVPair(new KeyValueNode(
            new TokenNode(new Token(TokenType.STRING, "user")),
            new TokenNode(new Token(TokenType.STRING, user))
        ));
    
        passBlock.addKVPair(new KeyValueNode(
            new TokenNode(new Token(TokenType.STRING, "pass")),
            new TokenNode(new Token(TokenType.STRING, encryptedPass))
        ));
    
        getPasswords().addValue(passBlock);
    }
    // Add a private key entry
    public void addPrivateKey(String service, String privKey, String iv) {
        ClassNode privKeyBlock = new ClassNode();
    
        privKeyBlock.addKVPair(new KeyValueNode(
            new TokenNode(new Token(TokenType.STRING, "iv")),
            new TokenNode(new Token(TokenType.STRING, iv))
        ));
    
        privKeyBlock.addKVPair(new KeyValueNode(
            new TokenNode(new Token(TokenType.STRING, "service")),
            new TokenNode(new Token(TokenType.STRING, service))
        ));
    
        privKeyBlock.addKVPair(new KeyValueNode(
            new TokenNode(new Token(TokenType.STRING, "privkey")),
            new TokenNode(new Token(TokenType.STRING, privKey))
        ));
    
        getPrivKeys().addValue(privKeyBlock);
    }

    // Getter Methods
    public String getSalt() {
        return (String) getKeyValue("salt");
    }

    public ClassNode getVaultKey() {
        return (ClassNode) getKeyValue("vaultkey");
    }

    public ArrayNode getPasswords() {
        return (ArrayNode) getKeyValue("passwords");
    }

    public ArrayNode getPrivKeys() {
        return (ArrayNode) getKeyValue("privkeys");
    }

    // Setter Methods
    public void setSalt(String salt) {
        setKeyValue("salt", salt);
    }

    public void setVaultKey(String iv, String key) {
        ClassNode vaultKeyBlock = createVaultKeyBlock();
    
        vaultKeyBlock.addKVPair(new KeyValueNode(
            new TokenNode(new Token(TokenType.STRING, "iv")),
            new TokenNode(new Token(TokenType.STRING, iv))
        ));
    
        vaultKeyBlock.addKVPair(new KeyValueNode(
            new TokenNode(new Token(TokenType.STRING, "key")),
            new TokenNode(new Token(TokenType.STRING, key))
        ));
    
        setKeyValue("vaultkey", vaultKeyBlock);
    }

    // Helper Methods for Key-Value Manipulation
    @SuppressWarnings("unchecked")
    private Object getKeyValue(String key) {
        try {
            // Use reflection to get the private field "kvPairs"
            Field kvPairsField = ClassNode.class.getDeclaredField("kvPairs");
            kvPairsField.setAccessible(true); // Allow access
    
            // Get the actual list of key-value pairs
            List<KeyValueNode> keyValuePairs = (List<KeyValueNode>) kvPairsField.get(vaultData);
    
            // Iterate through key-value pairs
            for (KeyValueNode kv : keyValuePairs) {
                Tuple<String, Object> pair = (Tuple<String, Object>) kv.evaluate();
                if (pair.getFirst().equals(key)) {
                    return pair.getSecond();
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace(); // Print error if reflection fails
        }
        return null;
    }

    @SuppressWarnings("unchecked") // Suppress unchecked warnings for reflection
    private void setKeyValue(String key, Object value) {
        try {
            // Use reflection to access the private "kvPairs" list
            Field kvPairsField = ClassNode.class.getDeclaredField("kvPairs");
            kvPairsField.setAccessible(true);

            // Get the actual list of key-value pairs
            List<KeyValueNode> keyValuePairs = (List<KeyValueNode>) kvPairsField.get(vaultData);

            // Search for an existing key and update it
            for (KeyValueNode kv : keyValuePairs) {
                Tuple<String, Object> pair = (Tuple<String, Object>) kv.evaluate();
                if (pair.getFirst().equals(key)) {
                    keyValuePairs.remove(kv); // Remove old entry
                    break;
                }
            }

            // Add the updated key-value pair
            keyValuePairs.add(new KeyValueNode(
                new TokenNode(new Token(TokenType.STRING, key)),
                new TokenNode(new Token(TokenType.STRING, value.toString()))
            ));

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace(); // Print error if reflection fails
        }
    }

    // Convert Vault to JSON-compatible structure
    public ClassNode toJSON() {
        return vaultData;
    }

    private String generateSalt() {
        byte[] saltBytes = new byte[16];
        new java.security.SecureRandom().nextBytes(saltBytes);
        return Base64.getEncoder().encodeToString(saltBytes);
    }

    @Override
    public void deserialize(JSONType arg0) throws InvalidObjectException {
        if (!(arg0 instanceof JSONObject)) {
            throw new InvalidObjectException("Invalid JSON format for Vault.");
        }
    
        JSONObject json = (JSONObject) arg0;
    
        // Load vault components
        this.salt = json.getString("salt");
        this.rootPasswordHash = json.getString("rootPasswordHash");
        JSONType vaultKeyJSON = json.getObject("vaultkey");
        if (vaultKeyJSON instanceof JSONObject) {
            this.vaultKey = (ClassNode) new JSONParser(vaultKeyJSON.toString()).parse().getRootNode();
        } else {
            this.vaultKey = new ClassNode(); // Default empty ClassNode if missing
        }
            this.vaultData = (ClassNode) new JSONParser(json.toString()).parse().getRootNode();
    
        // Extract passwords and private keys
        JSONArray jsonPasswords = json.getArray("passwords");
        this.passwords = (jsonPasswords != null) ? convertToArrayNode(jsonPasswords) : new ArrayNode();

        JSONArray jsonPrivKeys = json.getArray("privkeys");
        this.privKeys = (jsonPrivKeys != null) ? convertToArrayNode(jsonPrivKeys) : new ArrayNode();
    }


    @Override
    public JSONType toJSONType() {
        JSONObject json = new JSONObject();
    
        json.put("salt", (this.salt != null) ? this.salt : "");  
        json.put("rootPasswordHash", (this.rootPasswordHash != null) ? this.rootPasswordHash : "");
    
        // ✅ Convert vaultKey to a proper JSON object
        JSONObject vaultKeyObject = new JSONObject();
        if (this.vaultKey != null) {
            try {
                Field kvPairsField = ClassNode.class.getDeclaredField("kvPairs");
                kvPairsField.setAccessible(true);
    
                @SuppressWarnings("unchecked")
                List<KeyValueNode> keyValuePairs = (List<KeyValueNode>) kvPairsField.get(this.vaultKey);
    
                for (KeyValueNode kv : keyValuePairs) {
                    Tuple<String, Object> pair = (Tuple<String, Object>) kv.evaluate();
                    vaultKeyObject.put(pair.getFirst(), pair.getSecond().toString());
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        json.put("vaultkey", vaultKeyObject);

        // ✅ Convert ArrayNode manually since direct conversion isn't working
        json.put("passwords", convertArrayNodeToJSONArray(this.passwords));
        json.put("privkeys", convertArrayNodeToJSONArray(this.privKeys));
    
        return json;
    }

    private ArrayNode convertToArrayNode(JSONArray jsonArray) {
        ArrayNode arrayNode = new ArrayNode();
        for (Object obj : jsonArray) { 
            if (obj instanceof JSONType) { 
                JSONType jsonElement = (JSONType) obj;
    
               
                SyntaxNode node;
                if (jsonElement instanceof SyntaxNode) {
                    node = (SyntaxNode) jsonElement; 
                } else {
                    node = new JSONParser(jsonElement.toString()).parse().getRootNode(); // Parse if necessary
                }
    
                arrayNode.addValue(node); 
            }
        }
        return arrayNode;
    }

    private JSONArray convertArrayNodeToJSONArray(ArrayNode arrayNode) {
        JSONArray jsonArray = new JSONArray();
    
        if (arrayNode == null) { // ✅ Prevent null pointer exceptions
            return jsonArray;
        }
    
        try {
            Field valsField = ArrayNode.class.getDeclaredField("vals"); // 🔍 Find the field
            valsField.setAccessible(true); // ✅ Allow access to private field
    
            @SuppressWarnings("unchecked")
            List<SyntaxNode> values = (List<SyntaxNode>) valsField.get(arrayNode);
    
            for (SyntaxNode node : values) { 
                Object value = node.evaluate(); // ✅ Get evaluated value
                if (value instanceof JSONType) {
                    jsonArray.add(value); // ✅ Add as JSON object
                } else {
                    jsonArray.add(value.toString()); // ✅ Convert non-JSON types to String
                }
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Error accessing values of ArrayNode: " + e.getMessage());
        }
    
        return jsonArray;
    }
    
}