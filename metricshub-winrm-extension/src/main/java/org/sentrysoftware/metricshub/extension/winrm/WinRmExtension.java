package org.sentrysoftware.metricshub.extension.winrm;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub WMI Extension
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
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.CommandLineCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.Criterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.IpmiCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.ServiceCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.WmiCriterion;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.CommandLineSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.IpmiSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WmiSource;
import org.sentrysoftware.metricshub.engine.extension.IProtocolExtension;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.metricshub.extension.win.IWinConfiguration;
import org.sentrysoftware.metricshub.extension.win.WinCommandService;
import org.sentrysoftware.metricshub.extension.win.detection.CommandLineCriterionProcessor;
import org.sentrysoftware.metricshub.extension.win.detection.IpmiCriterionProcessor;
import org.sentrysoftware.metricshub.extension.win.detection.ServiceCriterionProcessor;
import org.sentrysoftware.metricshub.extension.win.detection.WmiCriterionProcessor;
import org.sentrysoftware.metricshub.extension.win.detection.WmiDetectionService;

/**
 * This class implements the {@link IProtocolExtension} contract, reports the supported features,
 * processes WMI sources and criteria.
 */
public class WinRmExtension implements IProtocolExtension {

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
	public static final String WMI_UP_METRIC = "metricshub.host.up{protocol=\"wmi\"}";

	/**
	 * WMI namespace
	 */
	public static final String WMI_TEST_NAMESPACE = "root\\cimv2";

	private WmiRequestExecutor wmiRequestExecutor;
	private WmiDetectionService wmiDetectionService;
	private WinCommandService winCommandService;

	/**
	 * Creates a new instance of the {@link WinRmExtension} implementation.
	 */
	public WinRmExtension() {
		wmiRequestExecutor = new WmiRequestExecutor();
		wmiDetectionService = new WmiDetectionService(wmiRequestExecutor);
		winCommandService = new WinCommandService(wmiRequestExecutor);
	}

	@Override
	public boolean isValidConfiguration(IConfiguration configuration) {
		return configuration instanceof WinRmConfiguration;
	}

	@Override
	public Set<Class<? extends Source>> getSupportedSources() {
		return Set.of(WmiSource.class, CommandLineSource.class, IpmiSource.class);
	}

	@Override
	public Map<Class<? extends IConfiguration>, Set<Class<? extends Source>>> getConfigurationToSourceMapping() {
		return Map.of(WinRmConfiguration.class, Set.of(WmiSource.class, IpmiSource.class, CommandLineSource.class));
	}

	@Override
	public Set<Class<? extends Criterion>> getSupportedCriteria() {
		return Set.of(WmiCriterion.class, ServiceCriterion.class, CommandLineCriterion.class, IpmiCriterion.class);
	}

	@Override
	public void checkProtocol(TelemetryManager telemetryManager) {
		
	}

	@Override
	public CriterionTestResult processCriterion(
		Criterion criterion,
		String connectorId,
		TelemetryManager telemetryManager
	) {
		final Function<TelemetryManager, IWinConfiguration> configurationRetriever = manager ->
			(IWinConfiguration) manager.getHostConfiguration().getConfigurations().get(WinRmConfiguration.class);

		if (criterion instanceof WmiCriterion wmiCriterion) {
			return new WmiCriterionProcessor(
				wmiDetectionService,
				configurationRetriever,
				connectorId
			)
				.process(wmiCriterion, telemetryManager);
		} else if (criterion instanceof ServiceCriterion serviceCriterion) {
			return new ServiceCriterionProcessor(wmiDetectionService, configurationRetriever)
				.process(serviceCriterion, telemetryManager);
		} else if (criterion instanceof CommandLineCriterion commandLineCriterion) {
			return new CommandLineCriterionProcessor(winCommandService, configurationRetriever)
				.process(commandLineCriterion, telemetryManager);
		} else if (criterion instanceof IpmiCriterion ipmiCriterion) {
			return new IpmiCriterionProcessor(wmiDetectionService, configurationRetriever)
					.process(ipmiCriterion, telemetryManager);
		}

		throw new IllegalArgumentException(
			String.format(
				"Hostname %s - Cannot process criterion %s.",
				telemetryManager.getHostname(),
				criterion != null ? criterion.getClass().getSimpleName() : "<null>"
			)
		);
	}

	@Override
	public SourceTable processSource(Source source, String connectorId, TelemetryManager telemetryManager) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isSupportedConfigurationType(String configurationType) {
		return "wmi".equalsIgnoreCase(configurationType);
	}

	@Override
	public IConfiguration buildConfiguration(String configurationType, JsonNode jsonNode, UnaryOperator<char[]> decrypt)
		throws InvalidConfigurationException {
		// TODO Auto-generated method stub
		return null;
	}
}
