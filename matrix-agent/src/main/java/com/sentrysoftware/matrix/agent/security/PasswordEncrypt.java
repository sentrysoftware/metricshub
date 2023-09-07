package com.sentrysoftware.matrix.agent.security;

import java.io.Console;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import com.sentrysoftware.matrix.agent.helper.AgentConstants;
import com.sentrysoftware.matrix.agent.helper.ConfigHelper;
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
	 *              (<em>libPath/../security</em> or <em>%PROGRAMDATA%\PRODUCT-CODE\security</em>)
	 * @return File instance
	 */
	public static File getKeyStoreFile(final boolean mkdir) {

		final Path securityDirectoryPath = LocalOsHandler.isWindows() ?
			getSecurityFolderOnWindows() : getSecurityFolderFromInstallDir();

		return resolveKeyStoreFile(securityDirectoryPath, mkdir);
	}

	/**
	 * Get the security folder located under <em>%PROGRAMDATA%\PRODUCT-CODE</em>.<br>
	 * If the ProgramData path is not valid then the security file will be located
	 * under the install directory.
	 * 
	 * @return {@link Path} instance
	 */
	static Path getSecurityFolderOnWindows() {
		return ConfigHelper.getProgramDataPath()
			.stream()
			.map(path -> Paths.get(path, AgentConstants.PRODUCT_CODE, AgentConstants.SECURITY_DIRECTORY_NAME))
			.findFirst()
			.orElseGet(PasswordEncrypt::getSecurityFolderFromInstallDir);
	}

	/**
	 * Get the security folder located under the install directory in Linux systems e.g.
	 * under /opt/PRODUCT-CODE/lib/app/../security
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

		return Paths.get(parentLibPath.toString(), "..", AgentConstants.SECURITY_DIRECTORY_NAME);
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
		return Paths.get(securityDirectory.toAbsolutePath().toString(), SecurityManager.MATRIX_KEY_STORE_FILE_NAME).toFile();
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
