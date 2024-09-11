package org.sentrysoftware.metricshub.engine.strategy;

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
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.common.JobInfo;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;
import org.sentrysoftware.metricshub.engine.strategy.source.OrderedSources;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

import java.util.ArrayList;
import java.util.Map;

@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class SurroundingStrategy extends AbstractStrategy {

    private Connector connector;

    /**
     * Initializes a new instance of {@code PreSourcesStrategy} with the necessary components for executing the strategy.
     * This constructor sets up the strategy with a telemetry manager, strategy execution time, a clients executor,
     * and a specific connector to process pre-sources.
     *
     * @param telemetryManager The telemetry manager responsible for managing telemetry data (monitors and metrics).
     * @param strategyTime     The execution time of the strategy, used for timing purpose.
     * @param clientsExecutor  An executor service for handling client operations within the pre-sources.
     * @param connector        The specific connector instance where the pre-sources are defined.
     * @param extensionManager The extension manager where all the required extensions are handled.
     */
    @Builder
    public SurroundingStrategy(
            @NonNull final TelemetryManager telemetryManager,
            @NonNull final Long strategyTime,
            @NonNull final ClientsExecutor clientsExecutor,
            @NonNull final Connector connector,
            @NonNull final ExtensionManager extensionManager
    ) {
        super(telemetryManager, strategyTime, clientsExecutor, extensionManager);
        this.connector = connector;
    }

    /**
     * Executes the strategy's core logic, processing pre-sources associated with the connector.
     */
    @Override
    public void run() {
        // Retrieve the connector's identifier and hostname for logging and processing.
        final String connectorId = connector.getCompiledFilename();
        final String hostname = telemetryManager.getHostname();

        // Fetch beforeAll sources and afterAll sources from the connector.
        final Map<String, Source> beforeAllSources = connector.getBeforeAll();
        final Map<String, Source> afterAllSources = connector.getAfterAll();

        if ((beforeAllSources == null || beforeAllSources.isEmpty()) && (afterAllSources == null || afterAllSources.isEmpty())) {
            log.debug(
                    "Hostname {} - Attempted to process beforeAll and afterAll sources, but none are available for connector {}.",
                    hostname,
                    connectorId
            );
            return;
        }

        if(beforeAllSources == null || beforeAllSources.isEmpty()) {
            log.debug(
                    "Hostname {} - Attempted to process beforeAll sources, but none are available for connector {}.",
                    hostname,
                    connectorId
            );
        } else {
            // Construct beforeAll job information including job name, connector identifier, hostname and monitor type.
            final JobInfo beforeAllJobInfo = JobInfo
                    .builder()
                    .hostname(hostname)
                    .connectorId(connectorId)
                    .jobName("beforeAll")
                    .monitorType("none")
                    .build();
            // Build and order beforeAll sources based on dependencies.
            final OrderedSources beforeAllOrderedSources = OrderedSources
                    .builder()
                    .sources(beforeAllSources, new ArrayList<>(), connector.getBeforeAllSourceDep(), beforeAllJobInfo)
                    .build();
            // Process the ordered sources along with computes, based on the constructed job information.
            processSourcesAndComputes(beforeAllOrderedSources.getSources(), beforeAllJobInfo);

        }

        if(afterAllSources == null || afterAllSources.isEmpty()) {
            log.debug(
                    "Hostname {} - Attempted to process afterAll sources, but none are available for connector {}.",
                    hostname,
                    connectorId
            );
        } else {
            // Construct beforeAll job information including job name, connector identifier, hostname and monitor type.
            final JobInfo afterAllJobInfo = JobInfo
                    .builder()
                    .hostname(hostname)
                    .connectorId(connectorId)
                    .jobName("afterAll")
                    .monitorType("none")
                    .build();


            // Build and order afterAll sources based on dependencies.
            final OrderedSources afterAllOrderedSources = OrderedSources
                    .builder()
                    .sources(afterAllSources, new ArrayList<>(), connector.getAfterAllSourceDep(), afterAllJobInfo)
                    .build();

            // Process the ordered sources along with computes, based on the constructed job information.
            processSourcesAndComputes(afterAllOrderedSources.getSources(), afterAllJobInfo);
        }
    }
}