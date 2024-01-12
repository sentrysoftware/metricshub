package com.sentrysoftware.metricshub.engine.common.helpers;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class JUtils {

	private JUtils() {} // Static only

	/**
	 * @param input String to convert
	 * @return the encoded string in hex
	 */
	public static String encodeSha256(final String input) {

		if (input == null) {
			return null;
		}

		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");

			md.update(input.getBytes(StandardCharsets.UTF_8));
			byte[] digest = md.digest();

			return String.format("%064x", new BigInteger(1, digest));
		} catch (NoSuchAlgorithmException e) {
			return null;
		}

	}
}
