package com.sentrysoftware.matrix.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityManager {

	private static final char[] KEY_STORE_PASSWORD = new char[] { 'S', 'e', 'n', 't', 'r', 'y' };
	private static final String MASTER_KEY_ALIAS = "masterKey";
	public static final String HWS_KEY_STORE_FILE_NAME = "hws-keystore.p12";

	/**
	 * Encrypt the given password
	 * 
	 * @param passwd password to encrypt
	 * @param keyStoreFile the key store holding the secret information
	 * @return char array
	 * @throws HardwareSecurityException
	 */
	public static char[] encrypt(final char[] passwd, @NonNull final File keyStoreFile) throws HardwareSecurityException {

		if (passwd == null) {
			return null;
		}

		return CryptoCipher.encrypt(passwd, getSecretKey(keyStoreFile));
	}

	/**
	 * Decrypt the password
	 * 
	 * @param crypted the crypted text
	 * @param keyStoreFile the key store holding the secret information
	 * @return char array
	 * @throws HardwareSecurityException
	 */
	public static char[] decrypt(final char[] crypted, final File keyStoreFile) throws HardwareSecurityException {

		if (crypted == null) {
			return null;
		}

		if (keyStoreFile!= null && keyStoreFile.exists()) {

			return CryptoCipher.decrypt(crypted, getSecretKey(keyStoreFile));
		}

		return crypted;
	}

	/**
	 * Get the secret key from the KeyStore
	 * @param keyStoreFile The key store file
	 * 
	 * @return {@link SecretKey} instance
	 * @throws HardwareSecurityException
	 */
	private static SecretKey getSecretKey(@NonNull final File keyStoreFile) throws HardwareSecurityException {

		// Load the keyStore
		final KeyStore ks = loadKeyStore(keyStoreFile);

		// Get the secretKey entry by its alias
		KeyStore.SecretKeyEntry entry = null; 
		try {
			entry = (KeyStore.SecretKeyEntry) ks.getEntry(MASTER_KEY_ALIAS, new PasswordProtection(KEY_STORE_PASSWORD));
		} catch (Exception e) {
			throw new HardwareSecurityException("Error detected when getting the secret key entry", e);
		}

		// No entry, means the master key is not generated yet, let's generate a new one
		if (entry == null) {
			return generateMasterKey(ks, KEY_STORE_PASSWORD, keyStoreFile);
		}

		return entry.getSecretKey();
	}

	/**
	 * Load the hwsKeyStore.pkcs12 file, if the keyStore file doesn't exist then it is created
	 * 
	 * @throws HardwareSecurityException
	 */
	public static KeyStore loadKeyStore(@NonNull final File keyStoreFile) throws HardwareSecurityException {

		try {
			final KeyStore ks = KeyStore.getInstance("PKCS12");
			if (keyStoreFile.exists()) {
				// if exists, load
				ks.load(new FileInputStream(keyStoreFile), KEY_STORE_PASSWORD);
			} else {
				// if not exists, create
				ks.load(null, null);

				// Store the key store password
				ks.store(new FileOutputStream(keyStoreFile), KEY_STORE_PASSWORD);

			}

			return ks;
		} catch (Exception e) {
			throw new HardwareSecurityException("Error detected when loading " + HWS_KEY_STORE_FILE_NAME, e);
		}
	}

	/**
	 * Generate and save a new master key in the given {@link KeyStore}
	 * 
	 * @param ks       The keyStore holding the secret key
	 * @param password The password used to protect the {@link KeyStore}
	 * 
	 * @throws Exception
	 */
	public static SecretKey generateMasterKey(@NonNull final KeyStore ks, @NonNull final char[] password,
			@NonNull final File keyStoreFile) throws HardwareSecurityException {

		try {
			// Random master key
			char[] masterKey = CryptoCipher.generateRandomMasterKey();

			// Create the KeySpec for password based encryption (PBE)
			final PBEKeySpec spec = new PBEKeySpec(masterKey, CryptoCipher.getSalt(), 2333, 32 * 8);

			// Get the secrete key factory
			final SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

			// Generate a secret key
			final SecretKey secretKey = skf.generateSecret(spec);

			// Create a secret key entry to be stored in the KeyStore
			KeyStore.SecretKeyEntry skEntry = new KeyStore.SecretKeyEntry(secretKey);

			// Set the secret key entry
			ks.setEntry(MASTER_KEY_ALIAS, skEntry, new PasswordProtection(password));

			// Finally commit
			ks.store(new FileOutputStream(keyStoreFile), password);

			return secretKey;
		} catch (Exception e) {
			throw new HardwareSecurityException("Error detected when generating the master key", e);
		}

	}
}
