package com.sentrysoftware.hardware.prometheus.security;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.security.HardwareSecurityException;
import com.sentrysoftware.matrix.security.SecurityManager;

class PasswordEncryptTest {

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

}
