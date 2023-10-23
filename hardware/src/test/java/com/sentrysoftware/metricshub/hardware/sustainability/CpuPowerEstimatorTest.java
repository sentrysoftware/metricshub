package com.sentrysoftware.metricshub.hardware.sustainability;

import static com.sentrysoftware.metricshub.hardware.common.Constants.CPU_ENERGY_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.CPU_POWER_METRIC;
import static com.sentrysoftware.metricshub.hardware.common.Constants.LOCALHOST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import com.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import com.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import org.junit.jupiter.api.Test;

class CpuPowerEstimatorTest {

	@Test
	void testEstimatePowerAndEnergy() {
		Monitor monitor = Monitor.builder().build();
		TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.strategyTime(1696597422644L)
			.hostConfiguration(HostConfiguration.builder().hostname(LOCALHOST).build())
			.build();
		CpuPowerEstimator cpuPowerEstimator = new CpuPowerEstimator(monitor, telemetryManager);
		// Default is 0.25 * (2500000 / 1000) / (1000 * 19) = 0.03289473684210526
		Double estimatedPower = cpuPowerEstimator.estimatePower();
		assertEquals(0.03289473684210526, estimatedPower);
		assertNull(cpuPowerEstimator.estimateEnergy());

		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		final NumberMetric collectedPowerMetric = metricFactory.collectNumberMetric(
			monitor,
			CPU_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);
		collectedPowerMetric.save();

		// CPU speed limit is 1900000 and CPU thermal dissipation rate is 0.5
		telemetryManager.setStrategyTime(1696597422644L + 60 * 1000);
		metricFactory.collectNumberMetric(
			monitor,
			"hw.cpu.speed.limit{limit_type=\"max\"}",
			1900000D,
			telemetryManager.getStrategyTime()
		);

		metricFactory.collectNumberMetric(
			monitor,
			"__hw.host.cpu.thermal_dissipation_rate",
			0.5,
			telemetryManager.getStrategyTime()
		);

		// Estimated power is 0.5 * (1900000 / 1000) / (1000 * 19) = 0.05
		estimatedPower = cpuPowerEstimator.estimatePower();
		assertEquals(0.05, estimatedPower);
		collectedPowerMetric.save();

		// Estimated energy is 3.0
		Double estimatedEnergy = cpuPowerEstimator.estimateEnergy();
		final NumberMetric collectedEnergyMetric = metricFactory.collectNumberMetric(
			monitor,
			CPU_ENERGY_METRIC,
			estimatedEnergy,
			telemetryManager.getStrategyTime()
		);
		collectedEnergyMetric.save();

		assertEquals(3.0, estimatedEnergy);

		// CPU speed limit is 3800000 and CPU thermal dissipation rate is 0.4
		telemetryManager.setStrategyTime(1696597422644L + 2 * 60 * 1000);
		metricFactory.collectNumberMetric(
			monitor,
			"hw.cpu.speed.limit{limit_type=\"max\"}",
			3800000D,
			telemetryManager.getStrategyTime()
		);

		metricFactory.collectNumberMetric(
			monitor,
			"__hw.host.cpu.thermal_dissipation_rate",
			0.4,
			telemetryManager.getStrategyTime()
		);

		// Estimated power is 0.4 * (3800000 / 1000) / (1000 * 19) = 0.08
		estimatedPower = cpuPowerEstimator.estimatePower();
		assertEquals(0.08, estimatedPower);
		collectedPowerMetric.save();

		// Estimated energy is 12.6
		estimatedEnergy = cpuPowerEstimator.estimateEnergy();
		collectedEnergyMetric.save();

		assertEquals(12.6, estimatedEnergy);
	}
}
