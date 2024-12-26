package org.sentrysoftware.metricshub.extension.snmpv3;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub SNMP V3 Extension
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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.function.UnaryOperator;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.common.helpers.TextTableHelper;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.extension.snmp.AbstractSnmpExtension;
import org.sentrysoftware.metricshub.extension.snmp.AbstractSnmpRequestExecutor;

/**
 * This class extends {@link AbstractSnmpExtension} contract, reports the supported features,
 * processes SNMP V3 sources and criteria.
 */
@Slf4j
public class SnmpV3Extension extends AbstractSnmpExtension {

	/**
	 * The identifier for the Snmp version 3  protocol.
	 */
	private static final String IDENTIFIER = "snmpv3";

	public static final String GET = "get";
	public static final String GET_NEXT = "getNext";
	public static final String WALK = "walk";
	public static final String TABLE = "table";

	private SnmpV3RequestExecutor snmpV3RequestExecutor;

	/**
	 * Creates a new instance of the {@link SnmpV3Extension} implementation.
	 */
	public SnmpV3Extension() {
		snmpV3RequestExecutor = new SnmpV3RequestExecutor();
	}

	@Override
	public boolean isValidConfiguration(IConfiguration configuration) {
		return configuration instanceof SnmpV3Configuration;
	}

	@Override
	public IConfiguration buildConfiguration(
		@NonNull String configurationType,
		@NonNull JsonNode jsonNode,
		UnaryOperator<char[]> decrypt
	) throws InvalidConfigurationException {
		try {
			final SnmpV3Configuration snmpV3Configuration = newObjectMapper()
				.treeToValue(jsonNode, SnmpV3Configuration.class);

			if (decrypt != null) {
				char[] password = snmpV3Configuration.getPassword();
				char[] privacyPassword = snmpV3Configuration.getPrivacyPassword();
				if (password != null) {
					// Decrypt the password
					snmpV3Configuration.setPassword(decrypt.apply(password));
				}
				if (privacyPassword != null) {
					// Decrypt the privacyPassword
					snmpV3Configuration.setPrivacyPassword(decrypt.apply(privacyPassword));
				}
			}

			return snmpV3Configuration;
		} catch (Exception e) {
			final String errorMessage = String.format("Error while reading SNMP V3 Configuration. Error: %s", e.getMessage());
			log.error(errorMessage);
			log.debug("Error while reading SNMP V3 Configuration. Stack trace:", e);
			throw new InvalidConfigurationException(errorMessage, e);
		}
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	protected AbstractSnmpRequestExecutor getRequestExecutor() {
		return snmpV3RequestExecutor;
	}

	@Override
	protected Class<SnmpV3Configuration> getConfigurationClass() {
		return SnmpV3Configuration.class;
	}

	@Override
	public String executeQuery(final IConfiguration configuration, final JsonNode queryNode) throws Exception {
		final SnmpV3Configuration snmpConfiguration = (SnmpV3Configuration) configuration;
		final String hostname = configuration.getHostname();
		String result = "Failed Executing SNMPv3 query";
		final String action = queryNode.get("action").asText();
		final String oId = queryNode.get("oid").asText();

		try {
			switch (action) {
				case GET:
					result = snmpV3RequestExecutor.executeSNMPGet(oId, snmpConfiguration, hostname, false);
					break;
				case GET_NEXT:
					result = snmpV3RequestExecutor.executeSNMPGetNext(oId, snmpConfiguration, hostname, false);
					break;
				case WALK:
					result = snmpV3RequestExecutor.executeSNMPWalk(oId, snmpConfiguration, hostname, false);
					break;
				case TABLE:
					final String[] columns = new ObjectMapper().convertValue(queryNode.get("columns"), String[].class);
					final List<List<String>> resultList = snmpV3RequestExecutor.executeSNMPTable(
						oId,
						columns,
						snmpConfiguration,
						hostname,
						false
					);
					result = TextTableHelper.generateTextTable(columns, resultList);
					break;
				default:
					throw new IllegalArgumentException(String.format("Hostname %s - Invalid SNMPv3 Operation", hostname));
			}
		} catch (Exception e) {
			log.debug("Hostname {} - Error while executing SNMPv3 {} query. Message: {}", hostname, action, e);
		}
		return result;
	}
}
