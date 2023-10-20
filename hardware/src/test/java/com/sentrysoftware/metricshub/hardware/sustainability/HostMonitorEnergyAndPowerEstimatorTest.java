package com.sentrysoftware.metricshub.hardware.sustainability;

import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.CONNECTOR_STATUS_METRIC_KEY;
import static com.sentrysoftware.metricshub.hardware.common.Constants.HW_CPU_POWER;
import static com.sentrysoftware.metricshub.hardware.common.Constants.HW_ENCLOSURE_POWER;
import static com.sentrysoftware.metricshub.hardware.common.Constants.HW_MEMORY_POWER;
import static com.sentrysoftware.metricshub.hardware.common.Constants.HW_PHYSICAL_DISK_POWER;
import static com.sentrysoftware.metricshub.hardware.common.Constants.LOCALHOST;
import static com.sentrysoftware.metricshub.hardware.util.HwConstants.HW_HOST_ESTIMATED_POWER;
import static com.sentrysoftware.metricshub.hardware.util.HwConstants.HW_HOST_MEASURED_ENERGY;
import static com.sentrysoftware.metricshub.hardware.util.HwConstants.HW_HOST_MEASURED_POWER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import com.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import com.sentrysoftware.metricshub.engine.strategy.utils.CollectHelper;
import com.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import com.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;
import java.util.Date;
import org.junit.jupiter.api.Test;

class HostMonitorEnergyAndPowerEstimatorTest {

	private static final String HW_VM_POWER = "hw.power{hw.type=\"vm\"}";

	/**
	 * Build a new host monitor
	 *
	 * @return {@link Monitor} instance
	 */
	private Monitor buildHostMonitor() {
		return Monitor.builder().id(KnownMonitorType.HOST.getKey()).type(KnownMonitorType.HOST.getKey()).build();
	}

	@Test
	void testComputeMeasuredPowerNoData() {
		// Create host and enclosure monitors
		final Monitor host = buildHostMonitor();
		final Monitor enclosure = Monitor
			.builder()
			.id(KnownMonitorType.HOST.getKey())
			.type(KnownMonitorType.ENCLOSURE.getKey())
			.build();

		// Initialize the telemetry manager
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.strategyTime(120L)
			.hostConfiguration(HostConfiguration.builder().hostId(LOCALHOST).build())
			.build();

		// Add connector status metric to both monitors
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		metricFactory.collectNumberMetric(host, CONNECTOR_STATUS_METRIC_KEY, 1.0, telemetryManager.getStrategyTime());
		metricFactory.collectNumberMetric(enclosure, CONNECTOR_STATUS_METRIC_KEY, 1.0, telemetryManager.getStrategyTime());

		// Add the previously created monitors to the telemetry manager
		telemetryManager.addNewMonitor(host, host.getType(), host.getId());
		telemetryManager.addNewMonitor(enclosure, enclosure.getType(), enclosure.getId());

		// Call computeMeasuredPower and check its response
		final HostMonitorEnergyAndPowerEstimator hostMonitorEnergyAndPowerEstimator = new HostMonitorEnergyAndPowerEstimator(
			host,
			telemetryManager
		);
		Double measuredPower = hostMonitorEnergyAndPowerEstimator.computeMeasuredPower();
		assertNull(measuredPower);
		assertNull(CollectHelper.getUpdatedNumberMetricValue(host, HW_HOST_MEASURED_POWER));
		assertNull(CollectHelper.getUpdatedNumberMetricValue(host, HW_HOST_ESTIMATED_POWER));
	}

	@Test
	void testComputeMeasuredPowerEnclosureHasPower() {
		// Create host and enclosure monitors
		final Monitor host = buildHostMonitor();
		final Monitor enclosure = Monitor
			.builder()
			.id(KnownMonitorType.ENCLOSURE.getKey())
			.type(KnownMonitorType.ENCLOSURE.getKey())
			.build();

		// Initialize the telemetry manager
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.strategyTime(120L)
			.hostConfiguration(HostConfiguration.builder().hostId(LOCALHOST).build())
			.build();

		// Add connector status metric to both monitors
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		metricFactory.collectNumberMetric(host, CONNECTOR_STATUS_METRIC_KEY, 1.0, telemetryManager.getStrategyTime());
		metricFactory.collectNumberMetric(enclosure, CONNECTOR_STATUS_METRIC_KEY, 1.0, telemetryManager.getStrategyTime());

		// Add the power metric to enclosure
		metricFactory.collectNumberMetric(enclosure, HW_ENCLOSURE_POWER, 120.0, telemetryManager.getStrategyTime());

		// Add the previously created monitors to the telemetry manager
		telemetryManager.addNewMonitor(host, host.getType(), host.getId());
		telemetryManager.addNewMonitor(enclosure, enclosure.getType(), enclosure.getId());

		// Call computeMeasuredPower and check its response
		final HostMonitorEnergyAndPowerEstimator hostMonitorEnergyAndPowerEstimator = new HostMonitorEnergyAndPowerEstimator(
			host,
			telemetryManager
		);
		assertEquals(120.0, hostMonitorEnergyAndPowerEstimator.computeMeasuredPower());
		assertNull(CollectHelper.getUpdatedNumberMetricValue(host, HW_HOST_MEASURED_POWER));
	}

	@Test
	void testComputeEstimatedPowerFirstCollect() {
		// Initialize the telemetry manager
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.strategyTime(120L)
			.hostConfiguration(HostConfiguration.builder().hostId(LOCALHOST).build())
			.build();

		// Initialize the host and other monitors
		final Monitor host = buildHostMonitor();

		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		metricFactory.collectNumberMetric(host, CONNECTOR_STATUS_METRIC_KEY, 1.0, telemetryManager.getStrategyTime());

		final Monitor enclosure = Monitor
			.builder()
			.id(KnownMonitorType.ENCLOSURE.getKey())
			.type(KnownMonitorType.ENCLOSURE.getKey())
			.build();

		metricFactory.collectNumberMetric(enclosure, CONNECTOR_STATUS_METRIC_KEY, 1.0, telemetryManager.getStrategyTime());

		final Monitor cpu = Monitor.builder().id("cpu1").type(KnownMonitorType.CPU.getKey()).build();
		metricFactory.collectNumberMetric(cpu, HW_CPU_POWER, 60.0, telemetryManager.getStrategyTime());
		metricFactory.collectNumberMetric(cpu, CONNECTOR_STATUS_METRIC_KEY, 1.0, telemetryManager.getStrategyTime());

		final Monitor memory = Monitor.builder().id("memory1").type(KnownMonitorType.MEMORY.getKey()).build();

		metricFactory.collectNumberMetric(memory, HW_MEMORY_POWER, 4.0, telemetryManager.getStrategyTime());
		metricFactory.collectNumberMetric(memory, CONNECTOR_STATUS_METRIC_KEY, 1.0, telemetryManager.getStrategyTime());

		final Monitor disk = Monitor.builder().id("disk_nvm_1").type(KnownMonitorType.PHYSICAL_DISK.getKey()).build();

		metricFactory.collectNumberMetric(disk, HW_PHYSICAL_DISK_POWER, 6.0, telemetryManager.getStrategyTime());
		metricFactory.collectNumberMetric(disk, CONNECTOR_STATUS_METRIC_KEY, 1.0, telemetryManager.getStrategyTime());

		final Monitor diskNoPower = Monitor
			.builder()
			.id("disk_noPower")
			.type(KnownMonitorType.PHYSICAL_DISK.getKey())
			.build();
		metricFactory.collectNumberMetric(
			diskNoPower,
			CONNECTOR_STATUS_METRIC_KEY,
			1.0,
			telemetryManager.getStrategyTime()
		);

		final Monitor missingDisk = Monitor
			.builder()
			.id("disk_nvm_2")
			.type(KnownMonitorType.PHYSICAL_DISK.getKey())
			.build();
		metricFactory.collectNumberMetric(
			missingDisk,
			CONNECTOR_STATUS_METRIC_KEY,
			1.0,
			telemetryManager.getStrategyTime()
		);

		// Add the previously created monitors to telemetry manager
		telemetryManager.addNewMonitor(host, KnownMonitorType.HOST.getKey(), KnownMonitorType.HOST.getKey());
		telemetryManager.addNewMonitor(cpu, KnownMonitorType.CPU.getKey(), "cpu1");
		telemetryManager.addNewMonitor(disk, KnownMonitorType.PHYSICAL_DISK.getKey(), "disk_nvm_1");
		telemetryManager.addNewMonitor(memory, KnownMonitorType.MEMORY.getKey(), "memory1");
		telemetryManager.addNewMonitor(diskNoPower, KnownMonitorType.PHYSICAL_DISK.getKey(), "disk_noPower");
		telemetryManager.addNewMonitor(missingDisk, KnownMonitorType.PHYSICAL_DISK.getKey(), "disk_nvm_2");
		telemetryManager.addNewMonitor(enclosure, KnownMonitorType.ENCLOSURE.getKey(), KnownMonitorType.ENCLOSURE.getKey());

		// Call computeEstimatedPower and check its response
		final HostMonitorEnergyAndPowerEstimator hostMonitorEnergyAndPowerEstimator = new HostMonitorEnergyAndPowerEstimator(
			host,
			telemetryManager
		);
		assertEquals(77.78, hostMonitorEnergyAndPowerEstimator.computeEstimatedPower());
	}

	@Test
	void testComputeEstimatedPowerSecondCollect() {
		// Initialize the telemetry manager
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.strategyTime(new Date().getTime())
			.hostConfiguration(HostConfiguration.builder().hostId(LOCALHOST).build())
			.build();

		// Initialize the host and other monitors
		final Monitor host = buildHostMonitor();

		// Set a previous collected host power value
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		final NumberMetric previousPowerValue = metricFactory.collectNumberMetric(
			host,
			HW_HOST_ESTIMATED_POWER,
			60.0,
			telemetryManager.getStrategyTime() - 120 * 1000
		);
		previousPowerValue.save();

		metricFactory.collectNumberMetric(host, CONNECTOR_STATUS_METRIC_KEY, 1.0, telemetryManager.getStrategyTime());

		final Monitor enclosure = Monitor
			.builder()
			.id(KnownMonitorType.ENCLOSURE.getKey())
			.type(KnownMonitorType.ENCLOSURE.getKey())
			.build();

		metricFactory.collectNumberMetric(enclosure, CONNECTOR_STATUS_METRIC_KEY, 1.0, telemetryManager.getStrategyTime());

		final Monitor cpu = Monitor.builder().id("cpu1").type(KnownMonitorType.CPU.getKey()).build();
		metricFactory.collectNumberMetric(cpu, HW_CPU_POWER, 60.0, telemetryManager.getStrategyTime());
		metricFactory.collectNumberMetric(cpu, CONNECTOR_STATUS_METRIC_KEY, 1.0, telemetryManager.getStrategyTime());

		final Monitor memory = Monitor.builder().id("memory1").type(KnownMonitorType.MEMORY.getKey()).build();

		metricFactory.collectNumberMetric(memory, HW_MEMORY_POWER, 4.0, telemetryManager.getStrategyTime());
		metricFactory.collectNumberMetric(memory, CONNECTOR_STATUS_METRIC_KEY, 1.0, telemetryManager.getStrategyTime());

		final Monitor disk = Monitor.builder().id("disk_nvm_1").type(KnownMonitorType.PHYSICAL_DISK.getKey()).build();

		metricFactory.collectNumberMetric(disk, HW_PHYSICAL_DISK_POWER, 6.0, telemetryManager.getStrategyTime());
		metricFactory.collectNumberMetric(disk, CONNECTOR_STATUS_METRIC_KEY, 1.0, telemetryManager.getStrategyTime());

		final Monitor diskNoPower = Monitor
			.builder()
			.id("disk_noPower")
			.type(KnownMonitorType.PHYSICAL_DISK.getKey())
			.build();
		metricFactory.collectNumberMetric(
			diskNoPower,
			CONNECTOR_STATUS_METRIC_KEY,
			1.0,
			telemetryManager.getStrategyTime()
		);

		final Monitor missingDisk = Monitor
			.builder()
			.id("disk_nvm_2")
			.type(KnownMonitorType.PHYSICAL_DISK.getKey())
			.build();
		metricFactory.collectNumberMetric(
			missingDisk,
			CONNECTOR_STATUS_METRIC_KEY,
			1.0,
			telemetryManager.getStrategyTime()
		);

		// Add the previously created monitors to telemetry manager
		telemetryManager.addNewMonitor(host, KnownMonitorType.HOST.getKey(), KnownMonitorType.HOST.getKey());
		telemetryManager.addNewMonitor(cpu, KnownMonitorType.CPU.getKey(), "cpu1");
		telemetryManager.addNewMonitor(disk, KnownMonitorType.PHYSICAL_DISK.getKey(), "disk_nvm_1");
		telemetryManager.addNewMonitor(memory, KnownMonitorType.MEMORY.getKey(), "memory1");
		telemetryManager.addNewMonitor(diskNoPower, KnownMonitorType.PHYSICAL_DISK.getKey(), "disk_noPower");
		telemetryManager.addNewMonitor(missingDisk, KnownMonitorType.PHYSICAL_DISK.getKey(), "disk_nvm_2");
		telemetryManager.addNewMonitor(enclosure, KnownMonitorType.ENCLOSURE.getKey(), KnownMonitorType.ENCLOSURE.getKey());

		// Call computeEstimatedPower and computeEstimatedEnergy and check their response
		final HostMonitorEnergyAndPowerEstimator hostMonitorEnergyAndPowerEstimator = new HostMonitorEnergyAndPowerEstimator(
			host,
			telemetryManager
		);
		hostMonitorEnergyAndPowerEstimator.computeEstimatedPower();
		assertEquals(9333.6, hostMonitorEnergyAndPowerEstimator.computeEstimatedEnergy());
	}

	@Test
	void testComputeMeasuredPowerHostHasEnergy() {
		// Build host and enclosure monitors
		final Monitor host = buildHostMonitor();
		final Monitor enclosure = Monitor
			.builder()
			.id(KnownMonitorType.HOST.getKey())
			.type(KnownMonitorType.ENCLOSURE.getKey())
			.build();

		// Initialize the telemetry manager and add metrics to created monitors
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.strategyTime(120L)
			.hostConfiguration(HostConfiguration.builder().hostId(LOCALHOST).build())
			.build();
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		metricFactory.collectNumberMetric(host, CONNECTOR_STATUS_METRIC_KEY, 1.0, telemetryManager.getStrategyTime());
		metricFactory.collectNumberMetric(enclosure, CONNECTOR_STATUS_METRIC_KEY, 1.0, telemetryManager.getStrategyTime());
		metricFactory.collectNumberMetric(host, HW_HOST_MEASURED_ENERGY, 3520255.0, telemetryManager.getStrategyTime());

		// Add the created monitors to telemetry manager
		telemetryManager.addNewMonitor(host, host.getType(), host.getId());
		telemetryManager.addNewMonitor(enclosure, enclosure.getType(), enclosure.getId());

		// Call computeMeasuredEnergy and check its response
		final HostMonitorEnergyAndPowerEstimator hostMonitorEnergyAndPowerEstimator = new HostMonitorEnergyAndPowerEstimator(
			host,
			telemetryManager
		);
		hostMonitorEnergyAndPowerEstimator.computeMeasuredEnergy();

		assertEquals(3520255.0, CollectHelper.getNumberMetricValue(host, HW_HOST_MEASURED_ENERGY, false));
	}
}
