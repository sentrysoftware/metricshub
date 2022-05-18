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
public class SnmpProtocol implements IProtocolConfiguration {

	@Default
	private SnmpVersion version = SnmpVersion.V1;
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

	@Override
	public String toString() {
		String desc = version.getDisplayName();
		if (version == SnmpVersion.V1 || version == SnmpVersion.V2C) {
			desc = desc + " (" + community + ")";
		} else {
			if (username != null) {
				desc = desc + " as " + username;
			}
			if (privacy != null && privacy != Privacy.NO_ENCRYPTION) {
				desc = desc + " (" + privacy + "-encrypted)";
			}
		}
		return desc;
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
		private int intVersion;

		@Getter
		private String authType;

		@Getter
		private String displayName;

		/**
		 * Interpret the specified label and returns corresponding value.
		 *
		 * @param label	String to be interpreted
		 *
		 * @return Corresponding {@link SnmpVersion} value
		 */
		public static SnmpVersion interpretValueOf(@NonNull final String label) {

			final String lCaseVersion = label.toLowerCase();

			if ("1".equals(lCaseVersion) || "v1".equals(lCaseVersion)) {
				return SnmpProtocol.SnmpVersion.V1;
			}

			if ("2".equals(lCaseVersion) || "v2".equals(lCaseVersion) || "v2c".equals(lCaseVersion)) {
				return SnmpProtocol.SnmpVersion.V2C;
			}

			if (lCaseVersion.startsWith("3") || lCaseVersion.startsWith("v3")) {
				if (lCaseVersion.contains("md5")) {
					return SnmpProtocol.SnmpVersion.V3_MD5;
				}
				if (lCaseVersion.contains("no") && lCaseVersion.contains("auth")) {
					return SnmpProtocol.SnmpVersion.V3_NO_AUTH;
				}
				return SnmpVersion.V3_SHA;
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
