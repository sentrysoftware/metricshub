package com.sentrysoftware.matrix.engine.protocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Builder.Default;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SNMPProtocol implements IProtocolConfiguration {

	@Default
	private SNMPVersion version = SNMPVersion.V1;
	@Default
	private String community = "public";
	@Default
	private Integer port = 161;
	@Default
	private Long timeout = 120L;
	private Privacy privacy;
	private char[] privacyPassword;
	private String username;
	private char[] password;

	/**
	 * Enum of SNMP versions and authentication types.
	 */
	@AllArgsConstructor
	public enum SNMPVersion {

		V1(1, null),
		V2C(2, null),
		V3_NO_AUTH(3, null),
		V3_MD5(3, "MD5"),
		V3_SHA(3, "SHA");

		@Getter
		private int intVersion;

		@Getter
		private String authType;

		/**
		 * Interpret the specified label and returns corresponding value.
		 *
		 * @param label	String to be interpreted
		 *
		 * @return Corresponding {@link SNMPVersion} value
		 */
		public static SNMPVersion interpretValueOf(@NonNull final String label) {

			final String lCaseVersion = label.toLowerCase();

			if ("1".equals(lCaseVersion) || "v1".equals(lCaseVersion)) {
				return SNMPProtocol.SNMPVersion.V1;
			}

			if ("2".equals(lCaseVersion) || "v2".equals(lCaseVersion) || "v2c".equals(lCaseVersion)) {
				return SNMPProtocol.SNMPVersion.V2C;
			}

			if (lCaseVersion.startsWith("3") || lCaseVersion.startsWith("v3")) {
				if (lCaseVersion.contains("md5")) {
					return SNMPProtocol.SNMPVersion.V3_MD5;
				}
				if (lCaseVersion.contains("no") && lCaseVersion.contains("auth")) {
					return SNMPProtocol.SNMPVersion.V3_NO_AUTH;
				}
				return SNMPVersion.V3_SHA;
			}

			throw new IllegalArgumentException("Invalid SNMP version: " + label);

		}
	}

	/**
	 * SNMP v3 Privacy (encryption type). Represents the encryption algorithm to be used
	 * in SNMP v3 connections.
	 */
	public enum Privacy {

		NO_ENCRYPTION, AES, DES;

		/**
		 * Interpret the specified label and returns corresponding value.
		 *
		 * @param label	String to be interpreted
		 *
		 * @return Corresponding {@link Privacy} value
		 */
		public static Privacy interpretValueOf(@NonNull final String label) {

			final String lCasePrivacy = label.toLowerCase();

			if (lCasePrivacy.equals("des")) {
				return DES;
			}
			if (lCasePrivacy.equals("aes")) {
				return AES;
			}
			if (lCasePrivacy.equals("none") || lCasePrivacy.equals("no")) {
				return NO_ENCRYPTION;
			}

			throw new IllegalArgumentException("Invalid Privacy value: " + label);
		}

	}

}
