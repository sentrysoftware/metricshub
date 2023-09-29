package com.sentrysoftware.matrix.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SnmpConfiguration implements IConfiguration {

	private static final String INVALID_SNMP_VERSION_EXCEPTION_MESSAGE = "Invalid SNMP version: ";
	private static final String INVALID_PRIVACY_VALUE_EXCEPTION_MESSAGE = " Invalid Privacy value: ";

	@Builder.Default
	private final SnmpVersion version = SnmpVersion.V1;

	@Builder.Default
	private final String community = "public";

	@Builder.Default
	private final Integer port = 161;

	@Builder.Default
	private final Long timeout = 120L;

	private Privacy privacy;
	private char[] privacyPassword;
	private String username;
	private char[] password;

	@Override
	public String toString() {
		String description = version.getDisplayName();
		if (version == SnmpVersion.V1 || version == SnmpVersion.V2C) {
			description = description + " (" + community + ")";
		} else {
			if (username != null) {
				description = description + " as " + username;
			}
			if (privacy != null && privacy != Privacy.NO_ENCRYPTION) {
				description = description + " (" + privacy + "-encrypted)";
			}
		}
		return description;
	}

	/**
	 * Enum of SNMP versions and authentication types.
	 */
	@AllArgsConstructor
	public enum SnmpVersion {
		V1(1, null, "SNMP v1"),
		V2C(2, null, "SNMP v2c"),
		V3_NO_AUTH(3, null, "SNMP v3"),
		V3_MD5(3, "MD5", "SNMP v3 with MD5 auth"),
		V3_SHA(3, "SHA", "SNMP v3 with SHA auth");

		@Getter
		private final int intVersion;

		@Getter
		private final String authType;

		@Getter
		private final String displayName;

		/**
		 * Interpret the specified label and returns corresponding value.
		 *
		 * @param version String to be interpreted
		 * @return Corresponding {@link SnmpVersion} value
		 */
		public static SnmpVersion interpretValueOf(@NonNull final String version) {
			final String lowerCaseVersion = version.toLowerCase();

			if ("1".equals(lowerCaseVersion) || "v1".equals(lowerCaseVersion)) {
				return SnmpConfiguration.SnmpVersion.V1;
			}

			if ("2".equals(lowerCaseVersion) || "v2".equals(lowerCaseVersion) || "v2c".equals(lowerCaseVersion)) {
				return SnmpConfiguration.SnmpVersion.V2C;
			}

			if (lowerCaseVersion.startsWith("3") || lowerCaseVersion.startsWith("v3")) {
				if (lowerCaseVersion.contains("md5")) {
					return SnmpConfiguration.SnmpVersion.V3_MD5;
				}
				if (lowerCaseVersion.contains("no") && lowerCaseVersion.contains("auth")) {
					return SnmpConfiguration.SnmpVersion.V3_NO_AUTH;
				}
				return SnmpVersion.V3_SHA;
			}

			throw new IllegalArgumentException(INVALID_SNMP_VERSION_EXCEPTION_MESSAGE + version);
		}
	}

	/**
	 * SNMP v3 Privacy (encryption type). Represents the encryption algorithm to be used
	 * in SNMP v3 connections.
	 */
	public enum Privacy {
		NO_ENCRYPTION,
		AES,
		DES;

		/**
		 * Interpret the specified label and returns corresponding value.
		 *
		 * @param privacy String to be interpreted
		 * @return Corresponding {@link Privacy} value
		 */
		public static Privacy interpretValueOf(@NonNull final String privacy) {
			final String lowerCasePrivacy = privacy.toLowerCase();

			if (lowerCasePrivacy.equals("des")) {
				return DES;
			}
			if (lowerCasePrivacy.equals("aes")) {
				return AES;
			}
			if (lowerCasePrivacy.equals("none") || lowerCasePrivacy.equals("no")) {
				return NO_ENCRYPTION;
			}

			throw new IllegalArgumentException(INVALID_PRIVACY_VALUE_EXCEPTION_MESSAGE + privacy);
		}
	}
}
