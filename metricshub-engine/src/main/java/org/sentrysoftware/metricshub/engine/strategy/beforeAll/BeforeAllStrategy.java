package org.sentrysoftware.metricshub.engine.strategy.beforeAll;

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

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;
import org.sentrysoftware.metricshub.engine.strategy.SurroundingStrategy;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * Implements pre-sources processing strategy extending the functionality of {@code SurroundingStrategy}.
 * This strategy is specifically designed to handle pre-sources using a specified connector. Pre-sources
 * are defined by a connector to facilitate factorization and convenience, allowing these sources to be processed
 * at the beginning of all jobs. This early processing ensures that any necessary setup or preliminary data manipulation
 * is completed before the main job execution phases.
 */
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class BeforeAllStrategy extends SurroundingStrategy {

	/**
	 * Initializes a new instance of {@code BeforeAllStrategy} with the necessary components for executing the strategy.
	 * This constructor sets up the strategy with a telemetry manager, strategy execution time, a clients executor,
	 * and a specific connector to process pre-sources.
	 *
	 * @param telemetryManager The telemetry manager responsible for managing telemetry data (monitors and metrics).
	 * @param strategyTime     The execution time of the strategy, used for timing purpose.
	 * @param clientsExecutor  An executor service for handling client operations within the pre-sources.
	 * @param connector        The specific connector instance where the pre-sources are defined.
	 * @param extensionManager The extension manager where all the required extensions are handled.
	 */
	@Builder(builderMethodName = "beforeAllBuilder")
	public BeforeAllStrategy(@NonNull TelemetryManager telemetryManager, @NonNull Long strategyTime, @NonNull ClientsExecutor clientsExecutor, @NonNull Connector connector, @NonNull ExtensionManager extensionManager) {
		super(telemetryManager, strategyTime, clientsExecutor, connector, extensionManager);
	}
}
