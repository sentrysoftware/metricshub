package com.sentrysoftware.matrix.security;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import com.sentrysoftware.matrix.common.helpers.ResourceHelper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityManager {

	private static final char[] KEY_STORE_PASSWORD = new char[] { 'S', 'e', 'n', 't', 'r', 'y' };
	private static final String MASTER_KEY_ALIAS = "masterKey";
	private static final String HWS_KEY_STORE_FILE_NAME = "hwsKeyStore.pkcs12";

	/**
	 * Encrypt the given password
	 * 
	 * @param passwd password to encrypt
	 * @return char array
	 * @throws HardwareSecurityException
	 */
	public static char[] encrypt(final char[] passwd) throws HardwareSecurityException {

		// Get the keyStoreFile with mkdir option set to true so that we are sure the security directory is created
		final File keyStoreFile = getKeyStoreFile(true);

		return CryptoCipher.encrypt(passwd, getSecretKey(keyStoreFile));
	}

	/**
	 * Decrypt the password
	 * 
	 * @param crypted the crypted text
	 * @return char array
	 * @throws HardwareSecurityException
	 */
	public static char[] decrypt(final char[] crypted) throws HardwareSecurityException {

		final File keyStoreFile = getKeyStoreFile(false);

		if (keyStoreFile.exists()) {

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

	/**
	 * Get the KeyStore file
	 * 
	 * @param mkdir Whether we should create the <em>libPath\..\security</em> directory
	 * @return File instance
	 * @throws HardwareSecurityException
	 */
	public static File getKeyStoreFile(boolean mkdir) throws HardwareSecurityException {

		File me;
		try {
			me = ResourceHelper.findSource(SecurityManager.class);
		} catch (Exception e) {
			throw new HardwareSecurityException("Error detected when getting local source file to create the keyStore.",
					e);
		}

		if (me == null) {
			throw new HardwareSecurityException("Could not get the local source file to create the keyStore.");
		}

		final Path path = me.getAbsoluteFile().toPath();

		Path parentLibPath = path.getParent();

		// No parent? let's work with the current directory
		if (parentLibPath == null) {
			parentLibPath = path;
		}

		File securityDirectory = Paths.get(parentLibPath.toString(), "..", "security").toFile();
		if (mkdir && !securityDirectory.exists() && !securityDirectory.mkdir()) {
			throw new HardwareSecurityException("Could not create security directory " + securityDirectory.getAbsolutePath());
		}

		// libPath\..\security
		return Paths.get(securityDirectory.getAbsolutePath(), HWS_KEY_STORE_FILE_NAME).toFile();
	}
}
