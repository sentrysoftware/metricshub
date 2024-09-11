package org.sentrysoftware.metricshub.engine.strategy.afterAll;

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
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;
import org.sentrysoftware.metricshub.engine.strategy.SurroundingStrategy;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

@Slf4j
@EqualsAndHashCode(callSuper = true)
public class AfterAllStrategy extends SurroundingStrategy  {
    /**
     * Initializes a new instance of {@code AfterAllStrategy} with the necessary components for executing the strategy.
     * This constructor sets up the strategy with a telemetry manager, strategy execution time, a clients executor,
     * and a specific connector to process post-sources.
     *
     * @param telemetryManager The telemetry manager responsible for managing telemetry data (monitors and metrics).
     * @param strategyTime     The execution time of the strategy, used for timing purpose.
     * @param clientsExecutor  An executor service for handling client operations within the pre-sources.
     * @param connector        The specific connector instance where the pre-sources are defined.
     * @param extensionManager The extension manager where all the required extensions are handled.
     */
    @Builder(builderMethodName = "afterAllBuilder")
    public AfterAllStrategy(@NonNull TelemetryManager telemetryManager, @NonNull Long strategyTime, @NonNull ClientsExecutor clientsExecutor, @NonNull Connector connector, @NonNull ExtensionManager extensionManager) {
        super(telemetryManager, strategyTime, clientsExecutor, connector, extensionManager);
    }
}