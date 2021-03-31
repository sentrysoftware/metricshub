package com.sentrysoftware.matrix.security;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HardwareCipher implements IHardwareCipher {

	private static final Pattern ENCRYPTED_STRING_PATTERN = Pattern.compile(".*?[^\\\\]?\\{(.*?[^\\\\])\\}.*");

	private static final String HW_SEC_LOCATION = "HW_SEC_LOCATION";

	public static final char ENCRYPTED_STRING_DECORATION_START = '{';

	public static final char ENCRYPTED_STRING_DECORATION_STOP = '}';


	public char[] encrypt(final char[] str, final char[] passPhrase) throws HardwareCipherException {

		if (str == null || str.length < 1) {
			return str;
		}

		return CryptoCipher.encrypt(str, passPhrase);
	}

	public char[] encryptAndDecorate(final char[] str, final char[] passPhrase) throws HardwareCipherException {

		return decorate(encrypt(str, passPhrase));
	}

	public char[] decrypt(final char[] str, final char[] passPhrase) throws HardwareCipherException {

		if (str == null || str.length < 1) {
			return str;
		}

		return CryptoCipher.decrypt(str, passPhrase);
	}

	public char[] decryptDecorated(final char[] str, final char[] passPhrase) throws HardwareCipherException {

		if (str == null || str.length < 1) {
			return str;
		}

		if (isEncryptedString(str)) {
			return decrypt(unDecorate(str), passPhrase);
		}

		return decrypt(str, passPhrase);
	}

	public boolean isEncryptedString(final char[] str) {

		if (str == null || str.length < 1) {
			return false;
		}

		Matcher matcher = ENCRYPTED_STRING_PATTERN.matcher(new String(str));

		return matcher.matches() || matcher.find();
	}

	public char[] unDecorate(final char[] str) throws HardwareCipherException {

		Matcher matcher = ENCRYPTED_STRING_PATTERN.matcher(new String(str));

		if (matcher.matches() || matcher.find()) {
			return matcher.group(1).toCharArray();
		} else {
			throw new HardwareCipherException("default.hardware.cipher.badEncryptedPassword");
		}
	}

	public char[] decorate(final char[] str) {

		String decorated = ENCRYPTED_STRING_DECORATION_START + (str == null ? "" : new String(str)) + ENCRYPTED_STRING_DECORATION_STOP;
		return decorated.toCharArray();
	}

	public String getSecLocation() { 
		return HW_SEC_LOCATION;
	}

	public char[] generateMasterPassword() throws HardwareCipherException {
		return CryptoCipher.generateRandomKeyFromMaster();
	}
}