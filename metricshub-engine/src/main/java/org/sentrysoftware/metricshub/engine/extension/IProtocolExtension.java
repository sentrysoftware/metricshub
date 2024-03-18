package org.sentrysoftware.metricshub.engine.extension;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Engine
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

import java.util.Set;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.Criterion;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * Defines the contract for protocol extensions within the system. Protocol extensions are responsible for
 * executing network queries for data exchange such as HTTP, SNMP, WBEM, WMI, SSH, etc
 * standards. Implementations of this interface must provide mechanisms to validate configurations, determine
 * support for specific sources and criteria, perform protocol checks, and execute operations related to sources
 * and criteria.
 */
public interface IProtocolExtension {
	/**
	 * Checks if the given configuration is valid for this extension. This method
	 * is intended to validate the configuration credentials to ensure they meet
	 * the expectations of the specific protocol extension.
	 *
	 * @param configuration Represents the generic configuration that's used and
	 *                      implemented by various protocol configurations.
	 * @return {@code true} if the configuration is valid, otherwise {@code false}.
	 */
	boolean isValidConfiguration(IConfiguration configuration);

	/**
	 * Retrieves the set of source classes that this extension supports. These sources
	 * define the protocol query this extension uses to fetch data.
	 *
	 * @return A set of classes extending {@link Source}, representing the supported sources.
	 */
	Set<Class<? extends Source>> getSupportedSources();

	/**
	 * Retrieves the set of criterion classes that this extension supports. Criteria
	 * represent specific conditions or checks that the engine execute to match connectors.
	 *
	 * @return A set of classes extending {@link Criterion}, representing the supported criteria.
	 */
	Set<Class<? extends Criterion>> getSupportedCriteria();

	/**
	 * Checks if protocol verification is supported for a given configuration type.
	 * This determines if a specific type of configuration can undergo protocol checks.
	 *
	 * @param configurationType The class of the configuration to check.
	 * @return {@code true} if protocol checks are supported for the given configuration type, otherwise {@code false}.
	 */
	boolean isProtocolCheckSupported(Class<? extends IConfiguration> configurationType);

	/**
	 * Performs a protocol check based on the given telemetry manager and collect time. This method
	 * is used to verify if a specific protocol or condition is met for the hostname defined by the
	 * telemetry manager.
	 *
	 * @param telemetryManager The telemetry manager to use for monitoring.
	 * @param collectTime      The time at which the collect is started.
	 */
	void checkProtocol(TelemetryManager telemetryManager, Long collectTime);

	/**
	 * Executes a source operation based on the given source and configuration within the telemetry manager.
	 *
	 * @param source           The source to execute.
	 * @param connectorId      The unique identifier of the connector.
	 * @param telemetryManager The telemetry manager to use for monitoring.
	 * @return A {@link SourceTable} object representing the result of the source execution.
	 */
	SourceTable executeSource(Source source, String connectorId, TelemetryManager telemetryManager);

	/**
	 * Executes a criterion check based on the given criterion and configuration within the telemetry manager.
	 *
	 * @param criterion        The criterion to execute.
	 * @param connectorId      The unique identifier of the connector.
	 * @param telemetryManager The telemetry manager to use for monitoring.
	 * @return A {@link CriterionTestResult} object representing the result of the criterion execution.
	 */
	CriterionTestResult executeCriterion(Criterion criterion, String connectorId, TelemetryManager telemetryManager);
}
