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
 * processes IPMI sources and criteria.
 */
@Slf4j
public class IpmiTestExtension implements IProtocolExtension {

	@Override
	public boolean isValidConfiguration(IConfiguration configuration) {
		return configuration instanceof IpmiTestConfiguration;
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
	public void checkProtocol(TelemetryManager telemetryManager) {}

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
		return "ipmi".equalsIgnoreCase(configurationType);
	}

	@Override
	public IConfiguration buildConfiguration(
		@NonNull String configurationType,
		@NonNull JsonNode jsonNode,
		UnaryOperator<char[]> decrypt
	) throws InvalidConfigurationException {
		try {
			final IpmiTestConfiguration ipmiConfiguration = new ObjectMapper()
				.treeToValue(jsonNode, IpmiTestConfiguration.class);

			// Decrypt the password
			final char[] passwordDecrypted = decrypt.apply(ipmiConfiguration.getPassword());
			ipmiConfiguration.setPassword(passwordDecrypted);

			return ipmiConfiguration;
		} catch (Exception e) {
			final String errorMessage = String.format(
				"Error while reading IPMI Configuration: %s. Error: %s",
				jsonNode,
				e.getMessage()
			);
			log.error(errorMessage);
			log.debug("Error while reading IPMI Configuration: {}. Stack trace:", jsonNode, e);
			throw new InvalidConfigurationException(errorMessage, e);
		}
	}
}
