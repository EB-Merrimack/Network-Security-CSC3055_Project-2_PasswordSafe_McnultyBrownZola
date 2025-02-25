import java.util.Base64;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class Vault {
    private KeyBlock keyBlock;

    public Vault() {
        this.keyBlock = new KeyBlock();
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

        public VaultKey() {
            // Default constructor
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

        public PasswordAccount() {
            // Default constructor
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

        public PrivateKeyAccount() {
            // Default constructor
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
}
