import java.util.List;

public class Vault {
    private KeyBlock keyBlock;

    public Vault() {
        this.keyBlock = new KeyBlock();
    }

    /**
     * Returns the salt value used to hash the vault key.
     *
     * @return the salt value
     */
    public String getSalt() {
        return (String) keyBlock.get("salt");
    }

    /**
     * Sets the salt value used to hash the vault key.
     *
     * @param salt the salt value
     */
    public void setSalt(String salt) {
        keyBlock.put("salt", salt);
    }

    /**
     * Returns the VaultKey object containing the vault's key.
     *
     * @return the VaultKey object
     */
    public VaultKey getVaultkey() {
        return (VaultKey) keyBlock.get("vaultkey");
    }

    /**
     * Sets the VaultKey object containing the vault's key.
     *
     * @param vaultkey the VaultKey object
     */
    public void setVaultkey(VaultKey vaultkey) {
        keyBlock.put("vaultkey", vaultkey);
    }

    /**
     * Returns the list of password accounts stored in the vault.
     * 
     * @return the list of password accounts
     */
    @SuppressWarnings("unchecked")
    public List<PasswordAccount> getPasswords() {
        return (List<PasswordAccount>) keyBlock.get("passwords");
    }

    /**
     * Sets the list of password accounts stored in the vault.
     *
     * @param passwords the list of password accounts
     */
    public void setPasswords(List<PasswordAccount> passwords) {
        keyBlock.put("passwords", passwords);
    }



/**
 * Returns the list of private key accounts stored in the vault.
 * 
 * This method retrieves the list of private key accounts from the internal
 * keyBlock data structure. The list contains objects of type PrivateKeyAccount,
 * representing individual private key accounts.
 * 
 * @return the list of private key accounts
 */

    @SuppressWarnings("unchecked")
    public List<PrivateKeyAccount> getPrivkeys() {
        return (List<PrivateKeyAccount>) keyBlock.get("privkeys");
    }

  
    /**
     * Sets the list of private key accounts stored in the vault.
     * 
     * @param privkeys the list of private key accounts
     */
public void setPrivkeys(List<PrivateKeyAccount> privkeys) {
        keyBlock.put("privkeys", privkeys);
    }

/**
 * Retrieves the KeyBlock object associated with the vault.
 *
 * @return the KeyBlock object
 */

    public KeyBlock getKeyBlock() {
        return keyBlock;
    }

    /**
     * Sets the KeyBlock object associated with the vault.
     * 
     * This method assigns a new KeyBlock object to the vault, overwriting the
     * existing object.
     * 
     * @param keyBlock the new KeyBlock object
     */
    public void setKeyBlock(KeyBlock keyBlock) {
        this.keyBlock = keyBlock;
    }

    public static class VaultKey {
        private String iv;
        private String key;

        public VaultKey() {
            // Default constructor
        }

/**
 * Retrieves the initialization vector (IV) associated with the vault key.
 *
 * @return the IV as a String
 */

        public String getIv() {
            return iv;
        }

        /**
         * Sets the initialization vector (IV) associated with the vault key.
         * 
         * @param iv the IV as a String
         */
        public void setIv(String iv) {
            this.iv = iv;
        }

        /**
         * Retrieves the vault key as a String.
         * 
         * @return the vault key
         */
        public String getKey() {
            return key;
        }

        /**
         * Sets the vault key as a String.
         * 
         * @param key the vault key
         */
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

        /**
         * Retrieves the initialization vector (IV) associated with the
         * password entry.
         * 
         * @return the IV as a String
         */
        public String getIv() {
            return iv;
        }

/**
 * Sets the initialization vector (IV) for the password account.
 *
 * @param iv the IV as a String
 */

        public void setIv(String iv) {
            this.iv = iv;
        }

        /**
         * Retrieves the service associated with the password account.
         * 
         * @return the service
         */
        public String getService() {
            return service;
        }

/**
 * Sets the service associated with the password account.
 *
 * @param service the service name as a String
 */

        /**
         * Sets the service associated with the password account.
         *
         * @param service the service name as a String
         */
        public void setService(String service) {
            this.service = service;
        }

/**
 * Retrieves the username associated with the password account.
 *
 * @return the username as a String
 */

        public String getUser() {
            return user;
        }

        /**
         * Sets the username associated with the password account.
         * 
         * @param user the username as a String
         */
        public void setUser(String user) {
            this.user = user;
        }

        /**
         * Retrieves the password associated with the password account.
         * 
         * @return the password as a String
         */
        public String getPass() {
            return pass;
        }

/**
 * Sets the password for the password account.
 *
 * @param pass the password as a String
 */

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

        /**
         * Retrieves the initialization vector (IV) associated with the
         * private key entry.
         * 
         * @return the IV as a String
         */
        public String getIv() {
            return iv;
        }

        /**
         * Sets the initialization vector (IV) associated with the
         * private key entry.
         * 
         * @param iv the IV as a String
         */
        public void setIv(String iv) {
            this.iv = iv;
        }

        /**
         * Retrieves the service associated with the private key account.
         * 
         * @return the service name as a String
         */
        public String getService() {
            return service;
        }

        /**
         * Sets the service associated with the private key account.
         * 
         * @param service the service name as a String
         */
        public void setService(String service) {
            this.service = service;
        }

/**
 * Retrieves the private key associated with the private key account.
 * 
 * @return the private key as a String
 */

        /**
         * Retrieves the private key associated with the private key account.
         * 
         * @return the private key as a String
         */
        public String getPrivkey() {
            return privkey;
        }


        /**
         * Sets the private key associated with the private key account.
         * 
         * @param privkey the private key as a String
         */
        public void setPrivkey(String privkey) {
            this.privkey = privkey;
        }
    }
}
