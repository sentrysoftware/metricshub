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
 * The SnmpConfiguration class represents the configuration for SNMP in the MetricsHub engine.
 * It implements the IConfiguration interface and includes settings such as SNMP version, community,
 * port, timeout, context name, privacy, privacy password, username, and password.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SnmpTestConfiguration implements IConfiguration {

	private static final String INVALID_SNMP_VERSION_EXCEPTION_MESSAGE = "Invalid SNMP version: ";

	@Default
	@JsonDeserialize(using = SnmpTestVersionDeserializer.class)
	private final SnmpVersion version = SnmpVersion.V1;

	@Default
	private char[] community = new char[] { 'p', 'u', 'b', 'l', 'i', 'c' };

	@Default
	private Integer port = 161;

	@Default
	@JsonDeserialize(using = TimeDeserializer.class)
	private final Long timeout = 120L;

	@Override
	public String toString() {
		return version.getDisplayName() + " (" + new String(community) + ")";
	}

	/**
	 * Enum of SNMP versions and authentication types.
	 */
	@AllArgsConstructor
	public enum SnmpVersion {
		/**
		 * SNMP version 1 (v1) without authentication.
		 */
		V1(1, "SNMP v1"),
		/**
		 * SNMP version 2 (v2c) without authentication.
		 */
		V2C(2, "SNMP v2c");

		@Getter
		private final int intVersion;

		@Getter
		private final String displayName;

		/**
		 * Interpret the specified label and returns corresponding value.
		 *
		 * @param version String to be interpreted
		 * @return Corresponding {@link SnmpVersion} value
		 * @throws IllegalArgumentException If the provided SNMP version label is invalid.
		 */
		public static SnmpVersion interpretValueOf(@NonNull final String version) {
			final String lowerCaseVersion = version.toLowerCase();

			if ("1".equals(lowerCaseVersion) || "v1".equals(lowerCaseVersion)) {
				return SnmpVersion.V1;
			}

			if ("2".equals(lowerCaseVersion) || "v2".equals(lowerCaseVersion) || "v2c".equals(lowerCaseVersion)) {
				return SnmpVersion.V2C;
			}

			throw new IllegalArgumentException(INVALID_SNMP_VERSION_EXCEPTION_MESSAGE + version);
		}
	}

	@Override
	public void validateConfiguration(final String resourceKey) throws InvalidConfigurationException {
		final SnmpVersion snmpVersion = version;

		final String displayName = snmpVersion.getDisplayName();

		StringHelper.validateConfigurationAttribute(
			port,
			attr -> attr == null || attr < 1 || attr > 65535,
			() ->
				String.format(
					"Resource %s - Invalid port configured for protocol %s. Port value returned: %s." +
					" This resource will not be monitored. Please verify the configured port value.",
					resourceKey,
					displayName,
					port
				)
		);

		StringHelper.validateConfigurationAttribute(
			timeout,
			attr -> attr == null || attr < 0L,
			() ->
				String.format(
					"Resource %s - Timeout value is invalid for protocol %s." +
					" Timeout value returned: %s. This resource will not be monitored. Please verify the configured timeout value.",
					resourceKey,
					displayName,
					timeout
				)
		);
	}
}
