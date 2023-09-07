package com.sentrysoftware.matrix.security;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SecurityManagerTest {


	private static File securityPath;

	@BeforeAll
	static void beforeEeach() throws Exception {
		securityPath = new File("src/test/resources/security/matrix-keystore.p12");
	}

	@Test
	void testEncryptDecrypt() throws Exception {
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
