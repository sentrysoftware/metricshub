package com.sentrysoftware.matrix.sustainability;

import static com.sentrysoftware.matrix.common.Constants.FAN_ENERGY_METRIC;
import static com.sentrysoftware.matrix.common.Constants.FAN_POWER_METRIC;
import static com.sentrysoftware.matrix.common.Constants.FAN_SPEED_METRIC;
import static com.sentrysoftware.matrix.common.Constants.ROBOTICS_ENERGY_METRIC;
import static com.sentrysoftware.matrix.common.Constants.ROBOTICS_MOVE_COUNT_METRIC;
import static com.sentrysoftware.matrix.common.Constants.ROBOTICS_POWER_METRIC;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sentrysoftware.matrix.HardwareEnergyPostExecutionService;
import com.sentrysoftware.matrix.common.helpers.KnownMonitorType;
import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HardwareEnergyPostExecutionServiceTest {

	@InjectMocks
	private HardwareEnergyPostExecutionService hardwareEnergyPostExecutionService;

	private TelemetryManager telemetryManager = null;

	private static final String FAN = KnownMonitorType.FAN.getKey();
	private static final String ROBOTICS = KnownMonitorType.ROBOTICS.getKey();

	@BeforeEach
	void init() {
		telemetryManager =
			TelemetryManager
				.builder()
				.strategyTime(1696597422644L)
				.hostConfiguration(HostConfiguration.builder().hostname("localhost").build())
				.build();
	}

	@Test
	void testRun() {
		// Create a fan monitor
		final Monitor fanMonitor = Monitor
			.builder()
			.type(FAN)
			.metrics(new HashMap<>(Map.of(FAN_SPEED_METRIC, NumberMetric.builder().value(0.7).build())))
			.build();

		// Set the previously created fan monitor in telemetryManager
		final Map<String, Monitor> fanMonitors = new HashMap<>(Map.of("monitor1", fanMonitor));
		telemetryManager.setMonitors(new HashMap<>(Map.of(FAN, fanMonitors)));

		// Call run method in HardwareEnergyPostExecutionService
		hardwareEnergyPostExecutionService = new HardwareEnergyPostExecutionService(telemetryManager);
		hardwareEnergyPostExecutionService.run();

		// Check the computed and collected power metric
		assertNotNull(fanMonitor.getMetric(FAN_POWER_METRIC, NumberMetric.class));

		// Check the computed and collected energy metric
		assertNotNull(fanMonitor.getMetric(FAN_ENERGY_METRIC, NumberMetric.class));

		// Create a robotics monitor
		final Monitor roboticsMonitor = Monitor
				.builder()
				.type(ROBOTICS)
				.metrics(new HashMap<>(Map.of(ROBOTICS_MOVE_COUNT_METRIC, NumberMetric.builder().value(0.7).build())))
				.build();

		// Set the previously created robotics monitor in telemetryManager
		final Map<String, Monitor> roboticsMonitors = new HashMap<>(Map.of("monitor2", roboticsMonitor));
		telemetryManager.setMonitors(new HashMap<>(Map.of(ROBOTICS, roboticsMonitors)));

		// Call run method in HardwareEnergyPostExecutionService
		hardwareEnergyPostExecutionService.run();

		// Check the computed and collected power metric
		assertNotNull(roboticsMonitor.getMetric(ROBOTICS_POWER_METRIC, NumberMetric.class));

		// Check the computed and collected energy metric
		assertNotNull(roboticsMonitor.getMetric(ROBOTICS_ENERGY_METRIC, NumberMetric.class));
	}
}
