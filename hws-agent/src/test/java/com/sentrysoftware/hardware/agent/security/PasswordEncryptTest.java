package com.sentrysoftware.hardware.agent.security;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.junit.jupiter.api.condition.OS.WINDOWS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import com.sentrysoftware.matrix.common.helpers.ResourceHelper;
import com.sentrysoftware.matrix.security.HardwareSecurityException;
import com.sentrysoftware.matrix.security.SecurityManager;

class PasswordEncryptTest {

	@TempDir
	static Path tempDir;

	private static File securityPath;

	@BeforeAll
	static void setUp() throws Exception {
		securityPath = new File("src/test/resources/hws-keystore.p12");
	}

	@Test
	void testEncryptDecrypt() throws HardwareSecurityException, URISyntaxException, IOException {
		// null password
		assertNull(SecurityManager.decrypt(null, securityPath));
		assertNull(SecurityManager.encrypt(null, securityPath));

		char[] passwd = {};
		char[] res = SecurityManager.encrypt(passwd, securityPath);
		assertArrayEquals(passwd, SecurityManager.decrypt(res, securityPath));

		passwd = "password".toCharArray();
		res = SecurityManager.encrypt(passwd, securityPath);
		assertArrayEquals(passwd, SecurityManager.decrypt(res, securityPath));

		passwd = "password2".toCharArray();
		res = SecurityManager.encrypt(passwd, securityPath);
		assertArrayEquals(passwd, SecurityManager.decrypt(res, securityPath));
	}

	@Test
	@EnabledOnOs(WINDOWS)
	void testGetKeyStoreFileWindows() throws IOException {

		{
			try (final MockedStatic<PasswordEncrypt> mockedPasswordEncrypt = mockStatic(PasswordEncrypt.class)) {
				mockedPasswordEncrypt
					.when(() -> PasswordEncrypt.getSecurityFolderOnWindows())
					.thenAnswer((invocation) ->  Files.createDirectories(tempDir.resolve("hws/security")));

				mockedPasswordEncrypt
					.when(() -> PasswordEncrypt.resolveKeyStoreFile(any(Path.class), any(Boolean.class)))
					.thenCallRealMethod();

				mockedPasswordEncrypt
					.when(() -> PasswordEncrypt.getKeyStoreFile(true))
					.thenCallRealMethod();

				final File actual = PasswordEncrypt.getKeyStoreFile(true);
				assertNotNull(actual);
				assertEquals(SecurityManager.HWS_KEY_STORE_FILE_NAME, actual.getName());
			}
		}

		{
			final File keyStoreFile = PasswordEncrypt.getKeyStoreFile(false);
			assertNotNull(keyStoreFile);
			assertEquals(SecurityManager.HWS_KEY_STORE_FILE_NAME, keyStoreFile.getName());
		}

	}

	@Test
	@EnabledOnOs(LINUX)
	void testGetKeyStoreFileLinux() {

		{
			try (final MockedStatic<ResourceHelper> mockedResourceHelper = mockStatic(ResourceHelper.class)) {
				mockedResourceHelper
					.when(() -> ResourceHelper.findSource(PasswordEncrypt.class))
					.thenAnswer((invocation) -> Files.createDirectories(tempDir.resolve("hws/lib/app/jar")).toFile());

				final File actual = PasswordEncrypt.getKeyStoreFile(true);
				assertNotNull(actual);
				assertEquals(SecurityManager.HWS_KEY_STORE_FILE_NAME, actual.getName());
			}
		}

		{
			try (final MockedStatic<ResourceHelper> mockedResourceHelper = mockStatic(ResourceHelper.class)) {
				mockedResourceHelper
					.when(() -> ResourceHelper.findSource(PasswordEncrypt.class))
					.thenAnswer((invocation) -> tempDir.resolve("hws/lib/app/jar").toFile());

				final File keyStoreFile = PasswordEncrypt.getKeyStoreFile(false);
				assertNotNull(keyStoreFile);
				assertEquals(SecurityManager.HWS_KEY_STORE_FILE_NAME, keyStoreFile.getName());
			}
		}

	}
}
