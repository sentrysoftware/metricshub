package org.sentrysoftware.metricshub.agent.extension;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.Set;
import java.util.function.UnaryOperator;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.Criterion;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import org.sentrysoftware.metricshub.engine.extension.IProtocolExtension;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * This class implements the {@link IProtocolExtension} contract, reports the supported features,
 * processes SNMP sources and criteria.
 */
@Slf4j
public class SnmpTestExtension implements IProtocolExtension {

	/**
	 * Protocol up status value '1.0'
	 */
	public static final Double UP = 1.0;

	/**
	 * Protocol down status value '0.0'
	 */
	public static final Double DOWN = 0.0;

	/**
	 * Up metric name format that will be saved by the metric factory
	 */
	static final String SNMP_UP_METRIC = "metricshub.host.up{operation=\"snmp\"}";

	/**
	 * The SNMP OID value to use in the health check test
	 */
	public static final String SNMP_OID = "1.3.6.1";

	@Override
	public boolean isValidConfiguration(IConfiguration configuration) {
		return configuration instanceof SnmpTestConfiguration;
	}

	@Override
	public Set<Class<? extends Source>> getSupportedSources() {
		return Set.of();
	}

	@Override
	public Set<Class<? extends Criterion>> getSupportedCriteria() {
		return Set.of();
	}

	@Override
	public void checkProtocol(TelemetryManager telemetryManager, Long collectTime) {}

	@Override
	public SourceTable processSource(Source source, String connectorId, TelemetryManager telemetryManager) {
		return SourceTable.empty();
	}

	@Override
	public CriterionTestResult processCriterion(
		Criterion criterion,
		String connectorId,
		TelemetryManager telemetryManager
	) {
		return CriterionTestResult.empty();
	}

	@Override
	public Map<Class<? extends IConfiguration>, Set<Class<? extends Source>>> getConfigurationToSourceMapping() {
		return Map.of();
	}

	@Override
	public boolean isSupportedConfigurationType(String configurationType) {
		return "snmp".equalsIgnoreCase(configurationType);
	}

	@Override
	public IConfiguration buildConfiguration(
		@NonNull String configurationType,
		@NonNull JsonNode jsonNode,
		UnaryOperator<char[]> decrypt
	) throws InvalidConfigurationException {
		try {
			final SnmpTestConfiguration snmpConfiguration = new ObjectMapper()
				.treeToValue(jsonNode, SnmpTestConfiguration.class);

			// Decrypt the community
			final char[] communityDecypted = decrypt.apply(snmpConfiguration.getCommunity());
			snmpConfiguration.setCommunity(communityDecypted);

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
}
