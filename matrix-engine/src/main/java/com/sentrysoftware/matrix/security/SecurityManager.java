package com.sentrysoftware.matrix.security;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;

public class SecurityManager {

	private SecurityManager() {

	}

	public static char[] generateMasterPassword() throws HardwareCipherException {

		return new HardwareCipher().generateMasterPassword();
	}

	public static char[] encryptPassword(final char[] passwd) throws HardwareCipherException, IOException {

		final HardwareCipher cipher = new HardwareCipher();

		final char[] masterPasswd = readSecuredFile(cipher.getSecLocation());
		return cipher.encryptAndDecorate(passwd, masterPasswd);
	}

	public static char[] decryptPassword(final char[] passwd) throws HardwareCipherException, IOException {

		final HardwareCipher cipher = new HardwareCipher();

		final char[] masterPasswd = readSecuredFile(cipher.getSecLocation());
		return cipher.decryptDecorated(passwd, masterPasswd);
	}

	private static char[] readSecuredFile(final String secLocation) throws IOException {

		final String file = System.getenv(secLocation);

		try (BufferedReader sourceReader = new BufferedReader(new FileReader(file))) {
			return sourceReader.lines().collect(Collectors.joining()).toCharArray();
		}
	}

}
