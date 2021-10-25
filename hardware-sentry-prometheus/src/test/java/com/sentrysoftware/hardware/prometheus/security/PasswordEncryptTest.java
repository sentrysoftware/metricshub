package com.sentrysoftware.hardware.prometheus.security;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sentrysoftware.matrix.common.helpers.ResourceHelper;
import com.sentrysoftware.matrix.security.HardwareSecurityException;
import com.sentrysoftware.matrix.security.SecurityManager;

class PasswordEncryptTest {

	@TempDir
	static Path tempDir;

	private static File securityPath;

	@BeforeAll
	static void setUp() throws Exception {
		securityPath = tempDir.resolve("security").toFile();
	}

	@Test
	void testEncryptDecrypt() throws HardwareSecurityException, URISyntaxException, IOException {

		// null password
		assertNull(SecurityManager.encrypt(null, PasswordEncrypt.getKeyStoreFile(true)));
		assertNull(SecurityManager.decrypt(null, PasswordEncrypt.getKeyStoreFile(true)));

		// empty password
		try (MockedStatic<ResourceHelper> resourceHelper = Mockito.mockStatic(ResourceHelper.class)) {
			char[] passwd = {};

			resourceHelper.when(() -> ResourceHelper.findSource(SecurityManager.class)).thenReturn(securityPath);
			char[] res = SecurityManager.encrypt(passwd, PasswordEncrypt.getKeyStoreFile(true));
			assertArrayEquals(passwd, SecurityManager.decrypt(res, PasswordEncrypt.getKeyStoreFile(true)));
		}

		// not empty password
		try (MockedStatic<ResourceHelper> resourceHelper = Mockito.mockStatic(ResourceHelper.class)) {
			char[] passwd = "password".toCharArray();

			resourceHelper.when(() -> ResourceHelper.findSource(SecurityManager.class)).thenReturn(securityPath);
			char[] res = SecurityManager.encrypt(passwd, PasswordEncrypt.getKeyStoreFile(true));
			assertArrayEquals(passwd, SecurityManager.decrypt(res, PasswordEncrypt.getKeyStoreFile(true)));
		}
	}
}
