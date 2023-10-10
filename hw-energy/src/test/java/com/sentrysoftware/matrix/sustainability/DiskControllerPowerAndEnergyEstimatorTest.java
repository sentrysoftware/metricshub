package com.sentrysoftware.matrix.sustainability;

import static com.sentrysoftware.matrix.common.Constants.DISK_CONTROLLER_POWER_METRIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.telemetry.MetricFactory;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

class DiskControllerPowerAndEnergyEstimatorTest {

	@InjectMocks
	private DiskControllerPowerAndEnergyEstimator diskControllerPowerAndEnergyEstimator;

	private Monitor monitor = null;
	private TelemetryManager telemetryManager = null;

	@BeforeEach
	void init() {
		monitor = Monitor.builder().build();
		telemetryManager =
			TelemetryManager
				.builder()
				.strategyTime(1696597422644L)
				.hostConfiguration(HostConfiguration.builder().hostname("localhost").build())
				.build();
		diskControllerPowerAndEnergyEstimator = new DiskControllerPowerAndEnergyEstimator(monitor, telemetryManager);
	}

	@Test
	void testEstimatePower() {
		assertEquals(15.0, diskControllerPowerAndEnergyEstimator.estimatePower());
	}

	@Test
	void testEstimateEnergy() {
		// Estimate energy consumption, no previous collect time
		assertNull(diskControllerPowerAndEnergyEstimator.estimateEnergy());

		// Estimate power consumption
		Double estimatedPower = diskControllerPowerAndEnergyEstimator.estimatePower();

		// Create metricFactory and collect power
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		final NumberMetric collectedPowerMetric = metricFactory.collectNumberMetric(
			monitor,
			DISK_CONTROLLER_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);

		// Save the collected power metric
		collectedPowerMetric.save();

		// Estimate power consumption again
		estimatedPower = diskControllerPowerAndEnergyEstimator.estimatePower();

		// Collect the new power consumption metric
		metricFactory.collectNumberMetric(
			monitor,
			DISK_CONTROLLER_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);

		// Estimate the energy
		assertEquals(15.0, diskControllerPowerAndEnergyEstimator.estimateEnergy());
	}
}
