package org.sentrysoftware.metricshub.engine.common.helpers;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JUtils {

	/**
	 * Encodes the input string using the SHA-256 algorithm and returns the result in hexadecimal format.
	 *
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
			log.error("Error while encoding SHA-256: {}", e.getMessage());
			return null;
		}
	}
}
