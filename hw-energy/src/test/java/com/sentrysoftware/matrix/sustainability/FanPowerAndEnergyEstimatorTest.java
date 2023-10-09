package com.sentrysoftware.matrix.sustainability;

import static com.sentrysoftware.matrix.common.Constants.FAN_POWER_METRIC;
import static com.sentrysoftware.matrix.common.Constants.FAN_SPEED_METRIC;
import static com.sentrysoftware.matrix.common.Constants.FAN_SPEED_RATIO_METRIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.telemetry.MetricFactory;
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
class FanPowerAndEnergyEstimatorTest {

	@InjectMocks
	private FanPowerAndEnergyEstimator fanPowerAndEnergyEstimator;

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
							FAN_SPEED_METRIC,
							NumberMetric.builder().value(7.0).build(),
							FAN_SPEED_RATIO_METRIC,
							NumberMetric.builder().value(0.2).build()
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
		fanPowerAndEnergyEstimator = new FanPowerAndEnergyEstimator(monitor, telemetryManager);
	}

	@Test
	void testEstimatePower() {
		// If fanSpeed metric value is a valid positive number
		assertEquals(0.007, fanPowerAndEnergyEstimator.estimatePower());

		// If fanSpeed metric value is not a valid positive number
		monitor.getMetric(FAN_SPEED_METRIC, NumberMetric.class).setValue(-7000.0);
		assertEquals(1.0, fanPowerAndEnergyEstimator.estimatePower());
	}

	@Test
	void testEstimateEnergy() {
		// Estimate energy consumption, no previous collect time
		assertNull(fanPowerAndEnergyEstimator.estimateEnergy());

		// Estimate power consumption
		Double estimatedPower = fanPowerAndEnergyEstimator.estimatePower();

		// Create metricFactory and collect power
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		final NumberMetric collectedPowerMetric = metricFactory.collectNumberMetric(
			monitor,
			FAN_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);

		// Save the collected power metric
		collectedPowerMetric.save();

		// Estimate power consumption again
		estimatedPower = fanPowerAndEnergyEstimator.estimatePower();

		// Collect the new power consumption metric
		metricFactory.collectNumberMetric(monitor, FAN_POWER_METRIC, estimatedPower, telemetryManager.getStrategyTime());

		// Estimate the energy
		assertEquals(0.007, fanPowerAndEnergyEstimator.estimateEnergy());
	}
}
