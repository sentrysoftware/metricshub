package com.sentrysoftware.matrix.sustainability;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.telemetry.MetricFactory;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.sentrysoftware.matrix.common.Constants.ROBOTICS_MOVE_COUNT_METRIC;
import static com.sentrysoftware.matrix.common.Constants.ROBOTICS_POWER_METRIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class RoboticsPowerAndEnergyEstimatorTest {
	@InjectMocks
	private RoboticsPowerAndEnergyEstimator roboticsPowerAndEnergyEstimator;

	private Monitor monitor = null;
	private TelemetryManager telemetryManager = null;

	@BeforeEach
	void init() {
		monitor =
				Monitor
						.builder()
						.metrics(
								new HashMap<>(
										Map.of(
												ROBOTICS_MOVE_COUNT_METRIC,
												NumberMetric.builder().value(7.0).build()
										)
								)
						)
						.build();
		telemetryManager =
				TelemetryManager
						.builder()
						.strategyTime(1696597422644L)
						.hostConfiguration(HostConfiguration.builder().hostname("localhost").build())
						.build();
		roboticsPowerAndEnergyEstimator = new RoboticsPowerAndEnergyEstimator(monitor, telemetryManager);
	}

	@Test
	void testEstimatePower() {
		// If moveCount metric value is not null
		monitor.getMetric(ROBOTICS_MOVE_COUNT_METRIC, NumberMetric.class).setValue(7.0);
		assertEquals(154.0, roboticsPowerAndEnergyEstimator.estimatePower());

		// If moveCount metric value is null
		monitor.setMetrics(Collections.EMPTY_MAP);
		assertEquals(null, roboticsPowerAndEnergyEstimator.estimatePower());
	}

	@Test
	void testEstimateEnergy() {
		// Estimate energy consumption, no previous collect time
		assertNull(roboticsPowerAndEnergyEstimator.estimateEnergy());

		// Estimate power consumption
		Double estimatedPower = roboticsPowerAndEnergyEstimator.estimatePower();

		// Create metricFactory and collect power
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		final NumberMetric collectedPowerMetric = metricFactory.collectNumberMetric(
				monitor,
				ROBOTICS_POWER_METRIC,
				estimatedPower,
				telemetryManager.getStrategyTime()
		);

		// Save the collected power metric
		collectedPowerMetric.save();

		// Estimate power consumption again
		estimatedPower = roboticsPowerAndEnergyEstimator.estimatePower();

		// Collect the new power consumption metric
		metricFactory.collectNumberMetric(monitor, ROBOTICS_POWER_METRIC, estimatedPower, telemetryManager.getStrategyTime());

		// Estimate the energy
		assertEquals(154, roboticsPowerAndEnergyEstimator.estimateEnergy());
	}
}
