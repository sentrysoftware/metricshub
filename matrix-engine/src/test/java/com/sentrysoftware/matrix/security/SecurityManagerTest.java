package com.sentrysoftware.matrix.security;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

class SecurityManagerTest {

	//	@Mock
	//	private ResourceHelper resourceHelper;

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
		assertNull(SecurityManager.encrypt(null));
		assertNull(SecurityManager.decrypt(null));

		// empty password
		try (MockedStatic<ResourceHelper> resourceHelper = Mockito.mockStatic(ResourceHelper.class)) {
			char[] passwd = {};

			resourceHelper.when(() -> ResourceHelper.findSource(SecurityManager.class)).thenReturn(securityPath);
			char[] res = SecurityManager.encrypt(passwd);
			assertArrayEquals(passwd, SecurityManager.decrypt(res));
		}

		// not empty password
		try (MockedStatic<ResourceHelper> resourceHelper = Mockito.mockStatic(ResourceHelper.class)) {
			char[] passwd = "password".toCharArray();

			resourceHelper.when(() -> ResourceHelper.findSource(SecurityManager.class)).thenReturn(securityPath);
			char[] res = SecurityManager.encrypt(passwd);
			assertArrayEquals(passwd, SecurityManager.decrypt(res));
		}
	}
	
	@Test
	void loadKeyStoreTest() throws HardwareSecurityException {
		// load null
		assertThrows(IllegalArgumentException.class, () -> SecurityManager.loadKeyStore(null));
		
		// load real key store file
		SecurityManager.loadKeyStore(new File("src/test/resources/security/hwsKeyStore.pkcs12"));
		char[] passwd = "lJ0eR5YqwTKYdPD51rL8Fo+8J/YzEQCV".toCharArray();
		char[] res = SecurityManager.decrypt(passwd);
		assertArrayEquals("password".toCharArray(), res);
	}
}
