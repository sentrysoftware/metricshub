package com.sentrysoftware.matrix.sustainability;

import static com.sentrysoftware.matrix.common.Constants.LOCALHOST;
import static com.sentrysoftware.matrix.common.Constants.MEMORY_ENERGY_METRIC;
import static com.sentrysoftware.matrix.common.Constants.MEMORY_POWER_METRIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.telemetry.MetricFactory;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import org.junit.jupiter.api.Test;

class MemoryPowerAndEnergyEstimatorTest {

	@Test
	void testDoPowerEstimation() {
		Monitor monitor = Monitor.builder().build();
		TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.strategyTime(1696597422644L)
			.hostConfiguration(HostConfiguration.builder().hostname(LOCALHOST).build())
			.build();
		MemoryPowerAndEnergyEstimator memoryPowerAndEnergyEstimator = new MemoryPowerAndEnergyEstimator(
			monitor,
			telemetryManager
		);

		assertEquals(4.0, memoryPowerAndEnergyEstimator.doPowerEstimation());
	}

	@Test
	void testEstimateEnergy() {
		Monitor monitor = Monitor.builder().build();
		TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.strategyTime(1696597422644L)
			.hostConfiguration(HostConfiguration.builder().hostname(LOCALHOST).build())
			.build();
		MemoryPowerAndEnergyEstimator memoryPowerAndEnergyEstimator = new MemoryPowerAndEnergyEstimator(
			monitor,
			telemetryManager
		);
		// Estimate energy consumption, no previous collect time
		assertNull(memoryPowerAndEnergyEstimator.estimateEnergy());

		// Estimate power consumption
		Double estimatedPower = memoryPowerAndEnergyEstimator.estimatePower();
		Double estimatedEnergy = memoryPowerAndEnergyEstimator.estimateEnergy();
		assertNull(estimatedEnergy);
		// Create metricFactory and collect power
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		final NumberMetric collectedPowerMetric = metricFactory.collectNumberMetric(
			monitor,
			MEMORY_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);

		// Save the collected power metric
		collectedPowerMetric.save();
		telemetryManager.setStrategyTime(telemetryManager.getStrategyTime() + 2 * 60 * 1000);

		// Estimate power consumption again
		estimatedPower = memoryPowerAndEnergyEstimator.estimatePower();

		// Collect the new power consumption metric
		metricFactory.collectNumberMetric(monitor, MEMORY_POWER_METRIC, estimatedPower, telemetryManager.getStrategyTime());

		// Estimate the energy
		estimatedEnergy = memoryPowerAndEnergyEstimator.estimateEnergy();
		assertEquals(480.0, estimatedEnergy);
		final NumberMetric collectedEnergyMetric = metricFactory.collectNumberMetric(
			monitor,
			MEMORY_ENERGY_METRIC,
			estimatedEnergy,
			telemetryManager.getStrategyTime()
		);
		collectedEnergyMetric.save();
		collectedPowerMetric.save();

		telemetryManager.setStrategyTime(telemetryManager.getStrategyTime() + 2 * 60 * 1000);

		// Estimate power consumption again
		estimatedPower = memoryPowerAndEnergyEstimator.estimatePower();

		// Collect the new power consumption metric
		metricFactory.collectNumberMetric(monitor, MEMORY_POWER_METRIC, estimatedPower, telemetryManager.getStrategyTime());

		// Estimate the energy
		assertEquals(960.0, memoryPowerAndEnergyEstimator.estimateEnergy());
	}
}
