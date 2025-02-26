import merrimackutil.json.types.JSONObject;

public class Vault {
    private KeyBlockcreation keyBlock;

    public Vault() {
        this.keyBlock = new KeyBlockcreation(null);
    }

   

    public String getSalt() {
        return (String) keyBlock.get("salt");
    }

    public void setSalt(String salt) {
        keyBlock.put("salt", salt);
    }

    public VaultKey getVaultkey() {
        Object vaultKeyObj = keyBlock.get("vaultkey");
        
        // Check if the vaultKey is a JSONObject and convert to VaultKey
        if (vaultKeyObj instanceof JSONObject) {
            return new VaultKey((JSONObject) vaultKeyObj);
        }
        
        // If it's already a VaultKey instance, return it directly
        if (vaultKeyObj instanceof VaultKey) {
            return (VaultKey) vaultKeyObj;
        }
        
        return null;
    }

    public void setVaultkey(VaultKey vaultkey) {
        keyBlock.put("vaultkey", vaultkey.toJSON());
    }

    public KeyBlockcreation getKeyBlock() {
        return keyBlock;
    }

    public void setKeyBlock(KeyBlockcreation keyBlock) {
        this.keyBlock = keyBlock;
    }

    // Convert Vault to properly formatted JSON
    public JSONObject toJSON() {
        return keyBlock.getData();
    }

    // Nested VaultKey class
    public static class VaultKey {
        private String iv;
        private String key;

        public VaultKey() {}

        public VaultKey(JSONObject json) {
            this.iv = (String) json.get("iv");
            this.key = (String) json.get("key");
        }

      

        public String getIv() {
            return iv;
        }

        public void setIv(String iv) {
            this.iv = iv;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public JSONObject toJSON() {
            JSONObject json = new JSONObject();
            json.put("iv", iv);
            json.put("key", key);
            return json;
        }
    }
}
