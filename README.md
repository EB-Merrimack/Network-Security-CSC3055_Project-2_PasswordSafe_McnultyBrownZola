# Network-Security-CSC3055_Project-2_PasswordSafe_McnultyBrownZola
# Secrets Vault Application - MainPanel

This project is part of the **Secrets Vault** application, which securely manages user credentials and private keys. The `MainPanel` class is a core part of the user interface, providing options for managing service credentials, private keys, key pairs, and logging out while sealing the vault.

## Overview

The `MainPanel` class is a **Swing panel** that serves as the main interface for interacting with the vault. It provides buttons for the user to:

- Add service credentials
- Lookup credentials
- Add private keys
- Lookup private keys
- Generate key pairs
- Log out and seal the vault

## Features

- **Add Service Credentials**: Add credentials for a service to the vault.
- **Lookup Credentials**: Look up stored service credentials in the vault.
- **Add Private Key**: Add private key entries to the vault.
- **Lookup Private Key**: Retrieve private key entries from the vault.
- **Generate Key Pair**: Generate a new public-private key pair for use with the vault.
- **Logout**: Securely log out of the vault and seal it, preventing unauthorized access.

## Components

- **JPanel (MainPanel)**: The main panel of the application that contains all the UI components.
- **GUIBuilder**: A helper class for switching between different panels.
- **Vault**: The vault that stores and manages credentials and private keys.
- **VaultEncryption**: Handles the encryption and decryption of vault data.
- **Action Listeners**: Define actions for buttons like adding credentials, looking up private keys, logging out, etc.

## UI Layout

- **MainPanel**:
  - `BorderLayout` is used to structure the panel.
  - A welcome message and buttons for each action (e.g., adding credentials, looking up credentials, logging out) are arranged using `BoxLayout`.
  - The logout button is displayed at the bottom of the panel, and the main content is displayed in the center.

## Instructions

1. **Running the Application**:
   - Ensure you have Java 8 or later installed.
   - Compile and run the application via your IDE or command line.

2. **Interacting with the Vault**:
   - After logging in, use the options provided in the `MainPanel` to manage your credentials and keys.
   - Click **Logout** when you're done to seal the vault and securely log out.

## Dependencies

- **Java 8+**: The application uses Java Swing for the GUI and Java’s `javax.crypto` package for encryption.
- **Vault**: A custom class for managing encrypted vault data (credentials, keys, etc.).

## File Structure

```plaintext
- Gui/
  - MainPanel.java          # Main panel of the application
  - GUIBuilder.java         # Class for managing different panels
- Vault/
  - Vault.java              # Class that handles vault encryption and decryption
  - VaultEncryption.java    # Handles encryption logic
- AddCredentialPanel.java   # Panel for adding service credentials
- LookupCredentialPanel.java  # Panel for looking up credentials
- AddPrivateKeyPanel.java   # Panel for adding private keys
- LookupPrivateKeyPanel.java  # Panel for looking up private keys
- AddServiceAndKeyGenPanel.java  # Panel for generating new key pairs
