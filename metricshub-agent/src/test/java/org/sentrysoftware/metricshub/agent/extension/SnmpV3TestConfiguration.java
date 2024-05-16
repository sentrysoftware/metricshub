package org.sentrysoftware.metricshub.agent.extension;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.common.helpers.StringHelper;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.deserialization.TimeDeserializer;

/**
 * The SnmpV3Configuration class represents the configuration for SNMP V3 in the
 * MetricsHub engine. It implements the IConfiguration interface and includes
 * settings such as SNMP version, community, port, timeout, context name,
 * authentication type, privacy, privacy password, username, and password.
 */

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SnmpV3TestConfiguration implements IConfiguration {

	private static final int V3 = 3;
	private static final String INVALID_AUTH_TYPE_EXCEPTION_MESSAGE = "Invalid authentication type: ";
	private static final String INVALID_PRIVACY_VALUE_EXCEPTION_MESSAGE = "Invalid privacy value: ";

	@Default
	private char[] community = new char[] { 'p', 'u', 'b', 'l', 'i', 'c' };

	@Default
	private Integer port = 161;

	@Default
	@JsonDeserialize(using = TimeDeserializer.class)
	private Long timeout = 120L;

	private String contextName;

	private AuthType authType;
	private Privacy privacy;
	private char[] privacyPassword;
	private String username;
	private char[] password;

	@Override
	public String toString() {
		return "SNMP V" + getIntVersion() + " (" + new String(community) + ")";
	}

	@Override
	public void validateConfiguration(final String resourceKey) throws InvalidConfigurationException {
		StringHelper.validateConfigurationAttribute(
			community,
			attr -> attr == null || attr.length == 0,
			() ->
				String.format(
					"Resource %s - No community string configured for %s. This resource will not be monitored.",
					resourceKey,
					community
				)
		);

		StringHelper.validateConfigurationAttribute(
			port,
			attr -> attr == null || attr < 1 || attr > 65535,
			() ->
				String.format(
					"Resource %s - Invalid port configured: %s. Please verify the configured port value.",
					resourceKey,
					port
				)
		);

		StringHelper.validateConfigurationAttribute(
			timeout,
			attr -> attr == null || attr < 0L,
			() ->
				String.format(
					"Resource %s - Timeout value is invalid: %s. Please verify the configured timeout value.",
					resourceKey,
					timeout
				)
		);

		StringHelper.validateConfigurationAttribute(
			username,
			attr -> attr == null || attr.isEmpty(),
			() ->
				String.format(
					"Resource %s - No username configured for protocol %s." +
					" This resource will not be monitored. Please verify the configured username.",
					resourceKey,
					username
				)
		);

		StringHelper.validateConfigurationAttribute(
			authType,
			attr -> attr == null,
			() ->
				String.format(
					"Resource %s - No username configured for protocol %s." +
					" This resource will not be monitored. Please verify the configured authtype.",
					resourceKey,
					authType
				)
		);
	}

	/**
	 * Enum of authentication types for SNMP v3.
	 */
	@AllArgsConstructor
	public enum AuthType {
		NO_AUTH("no"),
		MD5("md5"),
		SHA("sha");

		@Getter
		private final String stringAuthType;

		/**
		 * Interpret the specified label and returns corresponding value.
		 *
		 * @param authType String to be interpreted
		 * @return Corresponding {@link AuthType} value
		 */
		public static AuthType interpretValueOf(@NonNull final String authType) {
			final String lowerCaseAuthType = authType.toLowerCase();

			for (AuthType type : AuthType.values()) {
				if (type.getStringAuthType().equals(lowerCaseAuthType)) {
					return type;
				}
			}

			throw new IllegalArgumentException(INVALID_AUTH_TYPE_EXCEPTION_MESSAGE + authType);
		}
	}

	/**
	 * SNMP v3 Privacy (encryption type). Represents the encryption algorithm to be
	 * used in SNMP v3 connections.
	 */
	@AllArgsConstructor
	public enum Privacy {
		NO_ENCRYPTION("No encryption"),
		AES("AES encryption"),
		DES("DES encryption");

		@Getter
		private final String privacyType;

		/**
		 * Interpret the specified label and returns corresponding value.
		 *
		 * @param privacy String to be interpreted
		 * @return Corresponding {@link Privacy} value
		 */
		public static Privacy interpretValueOf(@NonNull final String privacy) {
			final String lowerCasePrivacy = privacy.toLowerCase();

			for (Privacy type : Privacy.values()) {
				if (type.getPrivacyType().toLowerCase().equals(lowerCasePrivacy)) {
					return type;
				}
			}

			throw new IllegalArgumentException(INVALID_PRIVACY_VALUE_EXCEPTION_MESSAGE + privacy);
		}
	}

	public int getIntVersion() {
		return V3;
	}
}
