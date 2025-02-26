import java.util.HashMap;
import java.util.List;
import java.util.Map;
import merrimackutil.json.types.JSONObject;

public class Vault {
    private KeyBlock keyBlock;

    public Vault() {
        this.keyBlock = new KeyBlock();
    }

    @Override
    public String toString() {
        return "Vault{" +
               "salt='" + getSalt() + '\'' +
               ", vaultkey=" + getVaultkey() +
               '}';
    }

    public String getSalt() {
        return (String) keyBlock.get("salt");
    }

    public void setSalt(String salt) {
        keyBlock.put("salt", salt);
    }

    public VaultKey getVaultkey() {
        return (VaultKey) keyBlock.get("vaultkey");
    }

    public void setVaultkey(VaultKey vaultkey) {
        keyBlock.put("vaultkey", vaultkey);
    }

    public List<PasswordAccount> getPasswords() {
        return (List<PasswordAccount>) keyBlock.get("passwords");
    }

    public void setPasswords(List<PasswordAccount> passwords) {
        keyBlock.put("passwords", passwords);
    }

    public List<PrivateKeyAccount> getPrivkeys() {
        return (List<PrivateKeyAccount>) keyBlock.get("privkeys");
    }

    public void setPrivkeys(List<PrivateKeyAccount> privkeys) {
        keyBlock.put("privkeys", privkeys);
    }

    public KeyBlock getKeyBlock() {
        return keyBlock;
    }

    public void setKeyBlock(KeyBlock keyBlock) {
        this.keyBlock = keyBlock;
    }

    public static class VaultKey {
        private String iv;
        private String key;

        public VaultKey() {}

        @Override
        public String toString() {
            return "VaultKey{" +
                   "key='" + key + '\'' +
                   ", iv='" + iv + '\'' +
                   '}';
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
    }

    public static class PasswordAccount {
        private String iv;
        private String service;
        private String user;
        private String pass;

        public PasswordAccount() {}

        @Override
        public String toString() {
            return "PasswordAccount{" +
                   "service='" + service + '\'' +
                   ", user='" + user + '\'' +
                   ", pass='" + pass + '\'' +
                   ", iv='" + iv + '\'' +
                   '}';
        }

        public String getIv() {
            return iv;
        }

        public void setIv(String iv) {
            this.iv = iv;
        }

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getPass() {
            return pass;
        }

        public void setPass(String pass) {
            this.pass = pass;
        }
    }

    public static class PrivateKeyAccount {
        private String iv;
        private String service;
        private String privkey;

        public PrivateKeyAccount() {}

        @Override
        public String toString() {
            return "PrivateKeyAccount{" +
                   "service='" + service + '\'' +
                   ", privkey='" + privkey + '\'' +
                   ", iv='" + iv + '\'' +
                   '}';
        }

        public String getIv() {
            return iv;
        }

        public void setIv(String iv) {
            this.iv = iv;
        }

        public String getService() {
            return service;
        }

        public void setService(String service) {
            this.service = service;
        }

        public String getPrivkey() {
            return privkey;
        }

        public void setPrivkey(String privkey) {
            this.privkey = privkey;
        }
    }

    public static class KeyBlock {
        private Map<String, Object> data;

        public KeyBlock() {
            this.data = new HashMap<>();
        }

        public void put(String key, Object value) {
            data.put(key, value);
        }

        public Object get(String key) {
            return data.get(key);
        }

        public JSONObject getData() {
            return new JSONObject(data); // Convert the map to a JSONObject
        }
    }
}
