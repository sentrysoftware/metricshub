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

	private static File jarPath;

	@BeforeAll
	static void setUp() throws Exception {

		jarPath = tempDir.resolve("lib").toFile();
	}

	@Test
	void testEncryptDecrypt() throws HardwareSecurityException, URISyntaxException, IOException {

		try (MockedStatic<ResourceHelper> resourceHelper = Mockito.mockStatic(ResourceHelper.class)) {
			resourceHelper.when(() -> ResourceHelper.findSource(SecurityManager.class)).thenReturn(jarPath);
			final File keyStoreFile = PasswordEncrypt.getKeyStoreFile(true);

			// null password
			assertNull(SecurityManager.decrypt(null, keyStoreFile));
			assertNull(SecurityManager.encrypt(null, keyStoreFile));

			char[] passwd = {};
			char[] res = SecurityManager.encrypt(passwd, keyStoreFile);
			assertArrayEquals(passwd, SecurityManager.decrypt(res, keyStoreFile));

			passwd = "password".toCharArray();
			res = SecurityManager.encrypt(passwd, keyStoreFile);
			assertArrayEquals(passwd, SecurityManager.decrypt(res, keyStoreFile));

			passwd = "password2".toCharArray();
			res = SecurityManager.encrypt(passwd, keyStoreFile);
			assertArrayEquals(passwd, SecurityManager.decrypt(res, keyStoreFile));

		}
	}

}
