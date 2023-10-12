package com.sentrysoftware.matrix.sustainability;

import static com.sentrysoftware.matrix.common.Constants.PHYSICAL_DISK_ENERGY_METRIC;
import static com.sentrysoftware.matrix.common.Constants.PHYSICAL_DISK_POWER_METRIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.sentrysoftware.matrix.common.helpers.KnownMonitorType;
import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.telemetry.MetricFactory;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PhysicalDiskPowerAndEnergyEstimatorTest {

	private PhysicalDiskPowerAndEnergyEstimator physicalDiskPowerAndEnergyEstimator;

	private Monitor monitor = null;
	private TelemetryManager telemetryManager = null;
	private static final String PARENT_MONITOR_ID = "monitor2";

	@BeforeEach
	void init() {
		monitor = Monitor.builder().build();
		telemetryManager =
			TelemetryManager
				.builder()
				.strategyTime(1696597422644L)
				.hostConfiguration(HostConfiguration.builder().hostname("localhost").build())
				.build();
		physicalDiskPowerAndEnergyEstimator = new PhysicalDiskPowerAndEnergyEstimator(monitor, telemetryManager);
	}

	@Test
	void testEstimatePowerForSSD() {
		monitor.setAttributes(new HashMap<>(Map.of("model", "ssd")));
		assertEquals(3.0, physicalDiskPowerAndEnergyEstimator.estimatePower());
	}

	@Test
	void testEstimateEnergyForSSD() {
		monitor.setAttributes(new HashMap<>(Map.of("info", "ssd")));
		// Estimate energy consumption, no previous collect time
		assertNull(physicalDiskPowerAndEnergyEstimator.estimateEnergy());

		// Estimate power consumption
		Double estimatedPower = physicalDiskPowerAndEnergyEstimator.estimatePower();
		Double estimatedEnergy = physicalDiskPowerAndEnergyEstimator.estimateEnergy();
		assertNull(estimatedEnergy);
		// Create metricFactory and collect power
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		final NumberMetric collectedPowerMetric = metricFactory.collectNumberMetric(
			monitor,
			PHYSICAL_DISK_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);

		// Save the collected power metric
		collectedPowerMetric.save();
		telemetryManager.setStrategyTime(telemetryManager.getStrategyTime() + 2 * 60 * 1000);

		// Estimate power consumption again
		estimatedPower = physicalDiskPowerAndEnergyEstimator.estimatePower();

		// Collect the new power consumption metric
		metricFactory.collectNumberMetric(
			monitor,
			PHYSICAL_DISK_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);

		// Estimate the energy
		estimatedEnergy = physicalDiskPowerAndEnergyEstimator.estimateEnergy();
		assertEquals(360.0, estimatedEnergy);
		final NumberMetric collectedEnergyMetric = metricFactory.collectNumberMetric(
			monitor,
			PHYSICAL_DISK_ENERGY_METRIC,
			estimatedEnergy,
			telemetryManager.getStrategyTime()
		);
		collectedEnergyMetric.save();
		collectedPowerMetric.save();

		telemetryManager.setStrategyTime(telemetryManager.getStrategyTime() + 2 * 60 * 1000);

		// Estimate power consumption again
		estimatedPower = physicalDiskPowerAndEnergyEstimator.estimatePower();

		// Collect the new power consumption metric
		metricFactory.collectNumberMetric(
			monitor,
			PHYSICAL_DISK_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);

		// Estimate the energy
		assertEquals(720.0, physicalDiskPowerAndEnergyEstimator.estimateEnergy());
	}

	@Test
	void testEstimatePowerForHDD() {
		monitor.setAttributes(new HashMap<>(Map.of("model", "Sas")));
		assertEquals(12.0, physicalDiskPowerAndEnergyEstimator.estimatePower());
	}

	@Test
	void testEstimateEnergyForHDD() {
		monitor.setAttributes(new HashMap<>(Map.of("info", "Sas")));
		// Estimate energy consumption, no previous collect time
		assertNull(physicalDiskPowerAndEnergyEstimator.estimateEnergy());

		// Estimate power consumption
		Double estimatedPower = physicalDiskPowerAndEnergyEstimator.estimatePower();
		Double estimatedEnergy = physicalDiskPowerAndEnergyEstimator.estimateEnergy();
		assertNull(estimatedEnergy);
		// Create metricFactory and collect power
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		final NumberMetric collectedPowerMetric = metricFactory.collectNumberMetric(
			monitor,
			PHYSICAL_DISK_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);

		// Save the collected power metric
		collectedPowerMetric.save();
		telemetryManager.setStrategyTime(telemetryManager.getStrategyTime() + 2 * 60 * 1000);

		// Estimate power consumption again
		estimatedPower = physicalDiskPowerAndEnergyEstimator.estimatePower();

		// Collect the new power consumption metric
		metricFactory.collectNumberMetric(
			monitor,
			PHYSICAL_DISK_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);

		// Estimate the energy
		estimatedEnergy = physicalDiskPowerAndEnergyEstimator.estimateEnergy();
		assertEquals(1440.0, estimatedEnergy);
		final NumberMetric collectedEnergyMetric = metricFactory.collectNumberMetric(
			monitor,
			PHYSICAL_DISK_ENERGY_METRIC,
			estimatedEnergy,
			telemetryManager.getStrategyTime()
		);
		collectedEnergyMetric.save();
		collectedPowerMetric.save();

		telemetryManager.setStrategyTime(telemetryManager.getStrategyTime() + 2 * 60 * 1000);

		// Estimate power consumption again
		estimatedPower = physicalDiskPowerAndEnergyEstimator.estimatePower();

		// Collect the new power consumption metric
		metricFactory.collectNumberMetric(
			monitor,
			PHYSICAL_DISK_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);

		// Estimate the energy
		assertEquals(2880.0, physicalDiskPowerAndEnergyEstimator.estimateEnergy());
	}

	@Test
	void testEstimatePowerForScsiAndIde() {
		monitor.setAttributes(new HashMap<>(Map.of("name", "scsi")));
		assertEquals(30.0, physicalDiskPowerAndEnergyEstimator.estimatePower());
	}

	@Test
	void testEstimateEnergyForScsiAndIde() {
		monitor.setAttributes(new HashMap<>(Map.of("model", "scsi")));
		// Estimate energy consumption, no previous collect time
		assertNull(physicalDiskPowerAndEnergyEstimator.estimateEnergy());

		// Estimate power consumption
		Double estimatedPower = physicalDiskPowerAndEnergyEstimator.estimatePower();
		Double estimatedEnergy = physicalDiskPowerAndEnergyEstimator.estimateEnergy();
		assertNull(estimatedEnergy);
		// Create metricFactory and collect power
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		final NumberMetric collectedPowerMetric = metricFactory.collectNumberMetric(
			monitor,
			PHYSICAL_DISK_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);

		// Save the collected power metric
		collectedPowerMetric.save();
		telemetryManager.setStrategyTime(telemetryManager.getStrategyTime() + 2 * 60 * 1000);

		// Estimate power consumption again
		estimatedPower = physicalDiskPowerAndEnergyEstimator.estimatePower();

		// Collect the new power consumption metric
		metricFactory.collectNumberMetric(
			monitor,
			PHYSICAL_DISK_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);

		// Estimate the energy
		estimatedEnergy = physicalDiskPowerAndEnergyEstimator.estimateEnergy();
		assertEquals(3600, estimatedEnergy);
		final NumberMetric collectedEnergyMetric = metricFactory.collectNumberMetric(
			monitor,
			PHYSICAL_DISK_ENERGY_METRIC,
			estimatedEnergy,
			telemetryManager.getStrategyTime()
		);
		collectedEnergyMetric.save();
		collectedPowerMetric.save();

		telemetryManager.setStrategyTime(telemetryManager.getStrategyTime() + 2 * 60 * 1000);

		// Estimate power consumption again
		estimatedPower = physicalDiskPowerAndEnergyEstimator.estimatePower();

		// Collect the new power consumption metric
		metricFactory.collectNumberMetric(
			monitor,
			PHYSICAL_DISK_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);

		// Estimate the energy
		assertEquals(7200, physicalDiskPowerAndEnergyEstimator.estimateEnergy());
	}

	@Test
	void testEstimatePowerForSATA() {
		monitor.setAttributes(new HashMap<>(Map.of("model", "any other model")));
		assertEquals(11.0, physicalDiskPowerAndEnergyEstimator.estimatePower());
	}

	@Test
	void testEstimateEnergyForSATA() {
		monitor.setAttributes(new HashMap<>(Map.of("info", "any other model")));
		// Estimate energy consumption, no previous collect time
		assertNull(physicalDiskPowerAndEnergyEstimator.estimateEnergy());

		// Estimate power consumption
		Double estimatedPower = physicalDiskPowerAndEnergyEstimator.estimatePower();
		Double estimatedEnergy = physicalDiskPowerAndEnergyEstimator.estimateEnergy();
		assertNull(estimatedEnergy);
		// Create metricFactory and collect power
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		final NumberMetric collectedPowerMetric = metricFactory.collectNumberMetric(
			monitor,
			PHYSICAL_DISK_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);

		// Save the collected power metric
		collectedPowerMetric.save();
		telemetryManager.setStrategyTime(telemetryManager.getStrategyTime() + 2 * 60 * 1000);

		// Estimate power consumption again
		estimatedPower = physicalDiskPowerAndEnergyEstimator.estimatePower();

		// Collect the new power consumption metric
		metricFactory.collectNumberMetric(
			monitor,
			PHYSICAL_DISK_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);

		// Estimate the energy
		estimatedEnergy = physicalDiskPowerAndEnergyEstimator.estimateEnergy();
		assertEquals(1320.0, estimatedEnergy);
		final NumberMetric collectedEnergyMetric = metricFactory.collectNumberMetric(
			monitor,
			PHYSICAL_DISK_ENERGY_METRIC,
			estimatedEnergy,
			telemetryManager.getStrategyTime()
		);
		collectedEnergyMetric.save();
		collectedPowerMetric.save();

		telemetryManager.setStrategyTime(telemetryManager.getStrategyTime() + 2 * 60 * 1000);

		// Estimate power consumption again
		estimatedPower = physicalDiskPowerAndEnergyEstimator.estimatePower();

		// Collect the new power consumption metric
		metricFactory.collectNumberMetric(
			monitor,
			PHYSICAL_DISK_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);

		// Estimate the energy
		assertEquals(2640.0, physicalDiskPowerAndEnergyEstimator.estimateEnergy());
	}

	@Test
	void testEstimatePowerWithParentMonitorData() {
		final Monitor parentMonitor = Monitor
			.builder()
			.id(PARENT_MONITOR_ID)
			.type(KnownMonitorType.DISK_CONTROLLER.getKey())
			.attributes(new HashMap<>(Map.of("name", "parentName5400")))
			.build();
		final Map<String, Monitor> diskControllerMonitorsMap = new HashMap<>(Map.of(PARENT_MONITOR_ID, parentMonitor));
		telemetryManager.getMonitors().put(KnownMonitorType.DISK_CONTROLLER.getKey(), diskControllerMonitorsMap);
		monitor.setAttributes(
			new HashMap<>(
				Map.of("hw.parent.id", PARENT_MONITOR_ID, "hw.parent.type", KnownMonitorType.DISK_CONTROLLER.getKey())
			)
		);
		// SATA is selected here, since the parent's name attribute contains the string "5400", the estimated power value is 7.0
		assertEquals(7.0, physicalDiskPowerAndEnergyEstimator.estimatePower());
	}

	@Test
	void testEstimateEnergyWithParentMonitorData() {
		final Monitor parentMonitor = Monitor
			.builder()
			.id(PARENT_MONITOR_ID)
			.type(KnownMonitorType.DISK_CONTROLLER.getKey())
			.attributes(new HashMap<>(Map.of("name", "parentName5400")))
			.build();
		final Map<String, Monitor> diskControllerMonitorsMap = new HashMap<>(Map.of(PARENT_MONITOR_ID, parentMonitor));
		telemetryManager.getMonitors().put(KnownMonitorType.DISK_CONTROLLER.getKey(), diskControllerMonitorsMap);
		monitor.setAttributes(
			new HashMap<>(
				Map.of("hw.parent.id", PARENT_MONITOR_ID, "hw.parent.type", KnownMonitorType.DISK_CONTROLLER.getKey())
			)
		);

		// Estimate energy consumption, no previous collect time
		// SATA is selected here, since the parent's name attribute contains the string "5400", the estimated power value is 7.0
		assertNull(physicalDiskPowerAndEnergyEstimator.estimateEnergy());

		// Estimate power consumption
		Double estimatedPower = physicalDiskPowerAndEnergyEstimator.estimatePower();
		Double estimatedEnergy = physicalDiskPowerAndEnergyEstimator.estimateEnergy();
		assertNull(estimatedEnergy);
		// Create metricFactory and collect power
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		final NumberMetric collectedPowerMetric = metricFactory.collectNumberMetric(
			monitor,
			PHYSICAL_DISK_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);

		// Save the collected power metric
		collectedPowerMetric.save();
		telemetryManager.setStrategyTime(telemetryManager.getStrategyTime() + 2 * 60 * 1000);

		// Estimate power consumption again
		estimatedPower = physicalDiskPowerAndEnergyEstimator.estimatePower();

		// Collect the new power consumption metric
		metricFactory.collectNumberMetric(
			monitor,
			PHYSICAL_DISK_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);

		// Estimate the energy
		estimatedEnergy = physicalDiskPowerAndEnergyEstimator.estimateEnergy();
		assertEquals(840.0, estimatedEnergy);
		final NumberMetric collectedEnergyMetric = metricFactory.collectNumberMetric(
			monitor,
			PHYSICAL_DISK_ENERGY_METRIC,
			estimatedEnergy,
			telemetryManager.getStrategyTime()
		);
		collectedEnergyMetric.save();
		collectedPowerMetric.save();

		telemetryManager.setStrategyTime(telemetryManager.getStrategyTime() + 2 * 60 * 1000);

		// Estimate power consumption again
		estimatedPower = physicalDiskPowerAndEnergyEstimator.estimatePower();

		// Collect the new power consumption metric
		metricFactory.collectNumberMetric(
			monitor,
			PHYSICAL_DISK_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);

		// Estimate the energy
		assertEquals(1680.0, physicalDiskPowerAndEnergyEstimator.estimateEnergy());
	}
}
