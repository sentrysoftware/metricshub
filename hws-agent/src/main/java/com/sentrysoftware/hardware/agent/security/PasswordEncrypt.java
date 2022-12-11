package com.sentrysoftware.hardware.agent.security;

import java.io.Console;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import com.sentrysoftware.hardware.agent.configuration.ConfigHelper;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler;
import com.sentrysoftware.matrix.common.helpers.ResourceHelper;
import com.sentrysoftware.matrix.common.helpers.StringHelper;
import com.sentrysoftware.matrix.security.SecurityManager;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access =  AccessLevel.PRIVATE)
public class PasswordEncrypt {

	static {
		Locale.setDefault(Locale.US);
	}

	/**
	 * Get the key-store file
	 * 
	 * @param mkdir Whether we should create the security directory
	 *              (<em>libPath/../security</em> or <em>%PROGRAMDATA%\hws\security</em>)
	 * @return File instance
	 */
	public static File getKeyStoreFile(boolean mkdir) {

		final Path securityDirectoryPath = LocalOsHandler.isWindows() ?
			getSecurityFolderOnWindows() : getSecurityFolderFromInstallDir();

		return resolveKeyStoreFile(securityDirectoryPath, mkdir);
	}

	/**
	 * Get the security folder located under <em>%PROGRAMDATA%\hws</em>
	 * 
	 * @return {@link Path} instance
	 */
	static Path getSecurityFolderOnWindows() {
		return Paths.get(System.getenv("ProgramData"), "hws", "security");
	}

	/**
	 * Get the security folder located under the install directory in Linux systems e.g.
	 * under /opt/hws/lib/app/../security
	 * 
	 * @return {@link Path} instance
	 */
	static Path getSecurityFolderFromInstallDir() {
		File me;
		try {
			me = ResourceHelper.findSource(PasswordEncrypt.class);
		} catch (Exception e) {
			throw new IllegalStateException(
					"Error detected when getting local source file to get the keyStore.", e);
		}

		if (me == null) {
			throw new IllegalStateException("Could not get the local source file to get the keyStore.");
		}

		final Path path = me.getAbsoluteFile().toPath();

		Path parentLibPath = path.getParent();

		// No parent? let's work with the current directory
		if (parentLibPath == null) {
			parentLibPath = path;
		}

		return Paths.get(parentLibPath.toString(), "..", "security");
	}

	/**
	 * Create the security directory then return the key-store file.
	 * 
	 * @param securityDirectory Security folder path which contains the key-store file.
	 * @param mkdir             Whether the security directory should be created or not
	 * @return {@link File} instance
	 */
	static File resolveKeyStoreFile(Path securityDirectory, final boolean mkdir) {

		if (mkdir && !Files.isDirectory(securityDirectory)) {
			securityDirectory = ConfigHelper.createDirectories(securityDirectory);
		}

		// path/hws-keystore.p12
		return Paths.get(securityDirectory.toAbsolutePath().toString(), SecurityManager.HWS_KEY_STORE_FILE_NAME).toFile();
	}

	public static void main(String[] args) {

		try {
			char[] password;

			// Password can be passed in the first command line argument
			if (args.length > 0) {
				password = args[0].toCharArray();
			} else {
				// Reads a password from the console
				Console console = System.console();

				if (console == null) {
					System.out.println("No console. Cannot read passwords without console."); // NOSONAR
					return;
				}

				System.out.print("Enter the password to encrypt: "); // NOSONAR
				password = console.readPassword();
			}

			// Encrypt
			System.out.println(SecurityManager.encrypt(password, getKeyStoreFile(true))); // NOSONAR
		} catch (Exception e) {
			System.err.println(String.format("Error while encrypting password: %s", StringHelper.getStackMessages(e))); // NOSONAR
		}
	}
}
