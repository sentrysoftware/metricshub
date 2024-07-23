package org.sentrysoftware.metricshub.extension.snmp;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub SNMP Extension
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * This class extends {@link AbstractSnmpExtension}, reports the supported features,
 * and processes SNMP sources and criteria.
 */
@Slf4j
@AllArgsConstructor
public class SnmpExtension extends AbstractSnmpExtension {

	@NonNull
	private SnmpRequestExecutor snmpRequestExecutor;

	/**
	 * Creates a new instance of the {@link SnmpExtension} implementation.
	 */
	public SnmpExtension() {
		snmpRequestExecutor = new SnmpRequestExecutor();
	}

	/**
	 * The SNMP OID value to use in the health check test
	 */
	public static final String SNMP_OID = "1.3.6.1";

	/**
	 * The identifier for the Snmp protocol.
	 */
	private static final String IDENTIFIER = "snmp";

	@Override
	public boolean isValidConfiguration(IConfiguration configuration) {
		return configuration instanceof SnmpConfiguration;
	}

	@Override
	protected Class<SnmpConfiguration> getConfigurationClass() {
		return SnmpConfiguration.class;
	}

	@Override
	public Optional<Boolean> checkProtocol(TelemetryManager telemetryManager) {
		// Retrieve the hostname from the SnmpConfiguration, otherwise from the telemetryManager
		final String hostname = telemetryManager.getHostname(List.of(SnmpConfiguration.class));

		// Create and set the SNMP result to null
		String snmpResult = null;

		// Retrieve SNMP Configuration from the telemetry manager host configuration
		final SnmpConfiguration snmpConfiguration = (SnmpConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(SnmpConfiguration.class);

		// Stop the SNMP health check if there is not an SNMP configuration
		if (snmpConfiguration == null) {
			return Optional.empty();
		}

		log.info("Hostname {} - Performing {} protocol health check.", hostname, getIdentifier());
		log.info("Hostname {} - Checking SNMP protocol status. Sending Get Next request on {}.", hostname, SNMP_OID);

		// Execute SNMP test command
		try {
			snmpResult = snmpRequestExecutor.executeSNMPGetNext(SNMP_OID, snmpConfiguration, hostname, true);
		} catch (Exception e) {
			log.debug(
				"Hostname {} - Checking SNMP protocol status. SNMP exception when performing a SNMP Get Next query on {}: ",
				hostname,
				SNMP_OID,
				e
			);
		}
		return Optional.of(snmpResult != null);
	}

	@Override
	public IConfiguration buildConfiguration(
		@NonNull String configurationType,
		@NonNull JsonNode jsonNode,
		UnaryOperator<char[]> decrypt
	) throws InvalidConfigurationException {
		try {
			final SnmpConfiguration snmpConfiguration = newObjectMapper().treeToValue(jsonNode, SnmpConfiguration.class);

			if (decrypt != null) {
				char[] community = snmpConfiguration.getCommunity();
				if (community != null) {
					// Decrypt the community
					snmpConfiguration.setCommunity(decrypt.apply(community));
				}
			}

			return snmpConfiguration;
		} catch (Exception e) {
			final String errorMessage = String.format(
				"Error while reading SNMP Configuration: %s. Error: %s",
				jsonNode,
				e.getMessage()
			);
			log.error(errorMessage);
			log.debug("Error while reading SNMP Configuration: {}. Stack trace:", jsonNode, e);
			throw new InvalidConfigurationException(errorMessage, e);
		}
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	protected AbstractSnmpRequestExecutor getRequestExecutor() {
		return snmpRequestExecutor;
	}
}
