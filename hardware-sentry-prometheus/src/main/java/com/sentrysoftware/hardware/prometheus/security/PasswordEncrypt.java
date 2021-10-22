package com.sentrysoftware.hardware.prometheus.security;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.sentrysoftware.matrix.common.helpers.ResourceHelper;
import com.sentrysoftware.matrix.security.HardwareSecurityException;
import com.sentrysoftware.matrix.security.SecurityManager;

public class PasswordEncrypt {

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
			throw new HardwareSecurityException("Error detected when getting local source file to get the keyStore.",
					e);
		}

		if (me == null) {
			throw new HardwareSecurityException("Could not get the local source file to get the keyStore.");
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
		return Paths.get(securityDirectory.getAbsolutePath(), SecurityManager.HWS_KEY_STORE_FILE_NAME).toFile();
	}

	public static void main(String[] args) throws IOException {

		try {
			char[] password;

			Console console = System.console();

			if (console == null) {
				System.out.println("No console. Cannot read passwords without console."); // NOSONAR
				return;
			}

			System.out.print("Enter the password to encrypt: "); // NOSONAR
			password = console.readPassword();

			System.out.print(SecurityManager.encrypt(password, getKeyStoreFile(true))); // NOSONAR
		} catch (HardwareSecurityException e) {
			System.err.println(String.format("Error while encrypting password: %s", e.getMessage())); // NOSONAR
		}
	}
}
