package com.sentrysoftware.metricshub.hardware.sustainability;

import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.CONNECTOR_STATUS_METRIC_KEY;
import static com.sentrysoftware.metricshub.hardware.common.Constants.HW_CPU_POWER;
import static com.sentrysoftware.metricshub.hardware.common.Constants.HW_ENCLOSURE_POWER;
import static com.sentrysoftware.metricshub.hardware.common.Constants.HW_HOST_ENERGY;
import static com.sentrysoftware.metricshub.hardware.common.Constants.HW_HOST_POWER;
import static com.sentrysoftware.metricshub.hardware.common.Constants.HW_MEMORY_POWER;
import static com.sentrysoftware.metricshub.hardware.common.Constants.HW_PHYSICAL_DISK_POWER;
import static com.sentrysoftware.metricshub.hardware.common.Constants.LOCALHOST;
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

		// Add connector status metric to both monitors
		host.addMetric(CONNECTOR_STATUS_METRIC_KEY, NumberMetric.builder().value(1.0).build());
		enclosure.addMetric(CONNECTOR_STATUS_METRIC_KEY, NumberMetric.builder().value(1.0).build());

		// Initialize the telemetry manager and add the previously created monitors
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.strategyTime(120L)
			.hostConfiguration(HostConfiguration.builder().hostId(LOCALHOST).build())
			.build();
		telemetryManager.addNewMonitor(host, host.getType(), host.getId());
		telemetryManager.addNewMonitor(enclosure, enclosure.getType(), enclosure.getId());

		// Call computeMeasuredPower and check its response
		final HostMonitorEnergyAndPowerEstimator hostMonitorEnergyAndPowerEstimator = new HostMonitorEnergyAndPowerEstimator(
			host,
			telemetryManager
		);
		Double measuredPower = hostMonitorEnergyAndPowerEstimator.computeMeasuredPower();
		assertNull(measuredPower);
		assertNull(CollectHelper.getUpdatedNumberMetricValue(host, HW_HOST_POWER));
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

		// Add connector status metric to both monitors
		host.addMetric(CONNECTOR_STATUS_METRIC_KEY, NumberMetric.builder().value(1.0).build());
		enclosure.addMetric(CONNECTOR_STATUS_METRIC_KEY, NumberMetric.builder().value(1.0).build());

		// Add the power metric to enclosure
		enclosure.addMetric(
			HW_ENCLOSURE_POWER,
			NumberMetric.builder().value(120.0).collectTime(System.currentTimeMillis()).build()
		);

		// Initialize the telemetry manager and add the previously created monitors
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.strategyTime(120L)
			.hostConfiguration(HostConfiguration.builder().hostId(LOCALHOST).build())
			.build();
		telemetryManager.addNewMonitor(host, host.getType(), host.getId());
		telemetryManager.addNewMonitor(enclosure, enclosure.getType(), enclosure.getId());

		// Call computeMeasuredPower and check its response
		final HostMonitorEnergyAndPowerEstimator hostMonitorEnergyAndPowerEstimator = new HostMonitorEnergyAndPowerEstimator(
			host,
			telemetryManager
		);
		assertEquals(120.0, hostMonitorEnergyAndPowerEstimator.computeMeasuredPower());
		assertNull(CollectHelper.getUpdatedNumberMetricValue(host, HW_HOST_POWER));
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
		host.addMetric(
			CONNECTOR_STATUS_METRIC_KEY,
			NumberMetric.builder().value(1.0).collectTime(telemetryManager.getStrategyTime()).build()
		);

		final Monitor enclosure = Monitor
			.builder()
			.id(KnownMonitorType.ENCLOSURE.getKey())
			.type(KnownMonitorType.ENCLOSURE.getKey())
			.build();
		enclosure.addMetric(
			CONNECTOR_STATUS_METRIC_KEY,
			NumberMetric.builder().value(1.0).collectTime(telemetryManager.getStrategyTime()).build()
		);

		final Monitor cpu = Monitor.builder().id("cpu1").type(KnownMonitorType.CPU.getKey()).build();

		cpu.addMetric(
			HW_CPU_POWER,
			NumberMetric.builder().value(60.0).collectTime(telemetryManager.getStrategyTime()).build()
		);
		cpu.addMetric(
			CONNECTOR_STATUS_METRIC_KEY,
			NumberMetric.builder().value(1.0).collectTime(telemetryManager.getStrategyTime()).build()
		);

		final Monitor memory = Monitor.builder().id("memory1").type(KnownMonitorType.MEMORY.getKey()).build();

		memory.addMetric(
			HW_MEMORY_POWER,
			NumberMetric.builder().value(4.0).collectTime(telemetryManager.getStrategyTime()).build()
		);
		memory.addMetric(
			CONNECTOR_STATUS_METRIC_KEY,
			NumberMetric.builder().value(1.0).collectTime(telemetryManager.getStrategyTime()).build()
		);

		final Monitor disk = Monitor.builder().id("disk_nvm_1").type(KnownMonitorType.PHYSICAL_DISK.getKey()).build();

		disk.addMetric(
			HW_PHYSICAL_DISK_POWER,
			NumberMetric.builder().value(6.0).collectTime(telemetryManager.getStrategyTime()).build()
		);
		disk.addMetric(
			CONNECTOR_STATUS_METRIC_KEY,
			NumberMetric.builder().value(1.0).collectTime(telemetryManager.getStrategyTime()).build()
		);

		final Monitor diskNoPower = Monitor
			.builder()
			.id("disk_noPower")
			.type(KnownMonitorType.PHYSICAL_DISK.getKey())
			.build();
		diskNoPower.addMetric(
			CONNECTOR_STATUS_METRIC_KEY,
			NumberMetric.builder().value(1.0).collectTime(telemetryManager.getStrategyTime()).build()
		);

		final Monitor missingDisk = Monitor
			.builder()
			.id("disk_nvm_2")
			.type(KnownMonitorType.PHYSICAL_DISK.getKey())
			.build();
		missingDisk.addMetric(
			CONNECTOR_STATUS_METRIC_KEY,
			NumberMetric.builder().value(1.0).collectTime(telemetryManager.getStrategyTime()).build()
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
			HW_HOST_POWER,
			60.0,
			telemetryManager.getStrategyTime() - 120 * 1000
		);
		previousPowerValue.save();

		host.addMetric(
			CONNECTOR_STATUS_METRIC_KEY,
			NumberMetric.builder().value(1.0).collectTime(telemetryManager.getStrategyTime()).build()
		);

		final Monitor enclosure = Monitor
			.builder()
			.id(KnownMonitorType.ENCLOSURE.getKey())
			.type(KnownMonitorType.ENCLOSURE.getKey())
			.build();
		enclosure.addMetric(
			CONNECTOR_STATUS_METRIC_KEY,
			NumberMetric.builder().value(1.0).collectTime(telemetryManager.getStrategyTime()).build()
		);

		final Monitor cpu = Monitor.builder().id("cpu1").type(KnownMonitorType.CPU.getKey()).build();

		cpu.addMetric(
			HW_CPU_POWER,
			NumberMetric.builder().value(60.0).collectTime(telemetryManager.getStrategyTime()).build()
		);
		cpu.addMetric(
			CONNECTOR_STATUS_METRIC_KEY,
			NumberMetric.builder().value(1.0).collectTime(telemetryManager.getStrategyTime()).build()
		);

		final Monitor memory = Monitor.builder().id("memory1").type(KnownMonitorType.MEMORY.getKey()).build();

		memory.addMetric(
			HW_MEMORY_POWER,
			NumberMetric.builder().value(4.0).collectTime(telemetryManager.getStrategyTime()).build()
		);
		memory.addMetric(
			CONNECTOR_STATUS_METRIC_KEY,
			NumberMetric.builder().value(1.0).collectTime(telemetryManager.getStrategyTime()).build()
		);

		final Monitor disk = Monitor.builder().id("disk_nvm_1").type(KnownMonitorType.PHYSICAL_DISK.getKey()).build();

		disk.addMetric(
			HW_PHYSICAL_DISK_POWER,
			NumberMetric.builder().value(6.0).collectTime(telemetryManager.getStrategyTime()).build()
		);
		disk.addMetric(
			CONNECTOR_STATUS_METRIC_KEY,
			NumberMetric.builder().value(1.0).collectTime(telemetryManager.getStrategyTime()).build()
		);

		final Monitor diskNoPower = Monitor
			.builder()
			.id("disk_noPower")
			.type(KnownMonitorType.PHYSICAL_DISK.getKey())
			.build();
		diskNoPower.addMetric(
			CONNECTOR_STATUS_METRIC_KEY,
			NumberMetric.builder().value(1.0).collectTime(telemetryManager.getStrategyTime()).build()
		);

		final Monitor missingDisk = Monitor
			.builder()
			.id("disk_nvm_2")
			.type(KnownMonitorType.PHYSICAL_DISK.getKey())
			.build();
		missingDisk.addMetric(
			CONNECTOR_STATUS_METRIC_KEY,
			NumberMetric.builder().value(1.0).collectTime(telemetryManager.getStrategyTime()).build()
		);

		final Monitor vm = Monitor.builder().id("vm-1").type(KnownMonitorType.VM.getKey()).build();
		vm.addMetric(
			CONNECTOR_STATUS_METRIC_KEY,
			NumberMetric.builder().value(1.0).collectTime(telemetryManager.getStrategyTime()).build()
		);
		vm.addMetric(
			HW_VM_POWER,
			NumberMetric.builder().value(4.0).collectTime(telemetryManager.getStrategyTime()).build()
		);

		// Add the previously created monitors to telemetry manager
		telemetryManager.addNewMonitor(host, KnownMonitorType.HOST.getKey(), KnownMonitorType.HOST.getKey());
		telemetryManager.addNewMonitor(cpu, KnownMonitorType.CPU.getKey(), "cpu1");
		telemetryManager.addNewMonitor(disk, KnownMonitorType.PHYSICAL_DISK.getKey(), "disk_nvm_1");
		telemetryManager.addNewMonitor(memory, KnownMonitorType.MEMORY.getKey(), "memory1");
		telemetryManager.addNewMonitor(diskNoPower, KnownMonitorType.PHYSICAL_DISK.getKey(), "disk_noPower");
		telemetryManager.addNewMonitor(missingDisk, KnownMonitorType.PHYSICAL_DISK.getKey(), "disk_nvm_2");
		telemetryManager.addNewMonitor(enclosure, KnownMonitorType.ENCLOSURE.getKey(), KnownMonitorType.ENCLOSURE.getKey());
		telemetryManager.addNewMonitor(vm, KnownMonitorType.VM.getKey(), "vm-1");

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
		host.addMetric(CONNECTOR_STATUS_METRIC_KEY, NumberMetric.builder().value(1.0).build());
		enclosure.addMetric(CONNECTOR_STATUS_METRIC_KEY, NumberMetric.builder().value(1.0).build());
		host.addMetric(
			HW_HOST_ENERGY,
			NumberMetric.builder().value(3520255.0).collectTime(telemetryManager.getStrategyTime()).build()
		);

		// Add the created monitors to telemetry manager
		telemetryManager.addNewMonitor(host, host.getType(), host.getId());
		telemetryManager.addNewMonitor(enclosure, enclosure.getType(), enclosure.getId());

		// Call computeMeasuredEnergy and check its response
		final HostMonitorEnergyAndPowerEstimator hostMonitorEnergyAndPowerEstimator = new HostMonitorEnergyAndPowerEstimator(
			host,
			telemetryManager
		);
		hostMonitorEnergyAndPowerEstimator.computeMeasuredEnergy();

		assertEquals(3520255.0, CollectHelper.getNumberMetricValue(host, HW_HOST_ENERGY, false));
	}
}
