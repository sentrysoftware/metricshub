package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COMPUTER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CONNECTOR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FQDN;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ID_COUNT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MODEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SERIAL_NUMBER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SIZE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TARGET_FQDN;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VENDOR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;
import com.sentrysoftware.matrix.common.helpers.ResourceHelper;
import com.sentrysoftware.matrix.common.meta.monitor.Battery;
import com.sentrysoftware.matrix.common.meta.parameter.state.Present;
import com.sentrysoftware.matrix.common.meta.parameter.state.Status;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectHelper;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.alert.AlertDetails;
import com.sentrysoftware.matrix.model.alert.AlertInfo;
import com.sentrysoftware.matrix.model.alert.AlertRule;
import com.sentrysoftware.matrix.model.alert.Severity;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

public class OtelAlertHelperTest {

	private static long collectTime = System.currentTimeMillis(); 
	private static final String HOSTNAME = "localhost";

	@Test
	void testBuildHardwareProblem() {

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();

			initMonitorsForAlert(hostMonitoring);

			final Monitor physicalDisk = hostMonitoring.findById("localhost@connector1_physical_disk_1");

			// Inject the alert information and a custom testing trigger verifying that our generated report ;)
			injectAlertInfoAndTriggerTest(hostMonitoring, physicalDisk, this::assertAlertMessage);

			CollectHelper.updateNumberParameter(physicalDisk, ENERGY_PARAMETER, EMPTY, collectTime, 1000D, 1000D);
			CollectHelper.updateStatusInformation(physicalDisk, collectTime, "Disk Failure X001256", Status.FAILED);
			CollectHelper.updateDiscreteParameter(physicalDisk, STATUS_PARAMETER, collectTime, Status.FAILED);

		}

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();

			initMonitorsForAlert(hostMonitoring);

			final Monitor physicalDisk = hostMonitoring.findById("localhost@connector1_physical_disk_1");

			// Inject the alert information and a custom testing trigger verifying that our generated report ;)
			injectAlertInfoAndTriggerTest(hostMonitoring, physicalDisk,
					info -> assertEquals("Physical Disk", OtelAlertHelper.buildHardwareProblem(info, "${MONITOR_TYPE}")));

			CollectHelper.updateDiscreteParameter(physicalDisk, STATUS_PARAMETER, collectTime, Status.FAILED);
		}

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();

			initMonitorsForAlert(hostMonitoring);

			final Monitor physicalDisk = hostMonitoring.findById("localhost@connector1_physical_disk_1");

			// Inject the alert information and a custom testing trigger verifying that our generated report ;)
			injectAlertInfoAndTriggerTest(hostMonitoring, physicalDisk,
					info -> assertEquals("hw.physical_disk.status{state=\"failed\"}", OtelAlertHelper.buildHardwareProblem(info, "${METRIC_NAME}")));

			CollectHelper.updateDiscreteParameter(physicalDisk, STATUS_PARAMETER, collectTime, Status.FAILED);
		}

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();

			initMonitorsForAlert(hostMonitoring);

			final Monitor physicalDisk = hostMonitoring.findById("localhost@connector1_physical_disk_1");

			// Inject the alert information and a custom testing trigger verifying that our generated report ;)
			injectAlertInfoAndTriggerTest(hostMonitoring, physicalDisk,
					info -> assertEquals("1 (Failed)", OtelAlertHelper.buildHardwareProblem(info, "${METRIC_VALUE}")));

			CollectHelper.updateDiscreteParameter(physicalDisk, STATUS_PARAMETER, collectTime, Status.FAILED);
		}

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();

			initMonitorsForAlert(hostMonitoring);

			final Monitor physicalDisk = hostMonitoring.findById("localhost@connector1_physical_disk_1");

			// Inject the alert information and a custom testing trigger verifying that our generated report ;)
			injectAlertInfoAndTriggerTest(hostMonitoring, physicalDisk,
					info -> assertEquals("ALARM", OtelAlertHelper.buildHardwareProblem(info, "${SEVERITY}")));

			CollectHelper.updateDiscreteParameter(physicalDisk, STATUS_PARAMETER, collectTime, Status.FAILED);
		}

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();

			initMonitorsForAlert(hostMonitoring);

			final Monitor physicalDisk = hostMonitoring.findById("localhost@connector1_physical_disk_1");

			// Inject the alert information and a custom testing trigger verifying that our generated report ;)
			injectAlertInfoAndTriggerTest(hostMonitoring, physicalDisk,
					info -> assertEquals("hw.physical_disk.status{state=\"failed\"} == 1", OtelAlertHelper.buildHardwareProblem(info, "${ALERT_RULE}")));

			CollectHelper.updateDiscreteParameter(physicalDisk, STATUS_PARAMETER, collectTime, Status.FAILED);
		}

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();

			initMonitorsForAlert(hostMonitoring);

			final Monitor physicalDisk = hostMonitoring.findById("localhost@connector1_physical_disk_1");

			// Inject the alert information and a custom testing trigger verifying that our generated report ;)
			injectAlertInfoAndTriggerTest(hostMonitoring, physicalDisk,
					info ->
						assertTrue(OtelAlertHelper.buildHardwareProblem(info, "${ALERT_DATE}")
							.matches("^\\d{4}\\-\\d{2}\\-\\d{2}T\\d{2}\\:\\d{2}\\:\\d{2}\\.\\d+$")));

			CollectHelper.updateDiscreteParameter(physicalDisk, STATUS_PARAMETER, collectTime, Status.FAILED);
		}

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();

			initMonitorsForAlert(hostMonitoring);

			final Monitor physicalDisk = hostMonitoring.findById("localhost@connector1_physical_disk_1");

			// Inject the alert information and a custom testing trigger verifying that our generated report ;)
			injectAlertInfoAndTriggerTest(hostMonitoring, physicalDisk,
					info -> assertNotEquals("${CONSEQUENCE}", OtelAlertHelper.buildHardwareProblem(info, "${CONSEQUENCE}")));

			CollectHelper.updateDiscreteParameter(physicalDisk, STATUS_PARAMETER, collectTime, Status.FAILED);
		}

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();

			initMonitorsForAlert(hostMonitoring);

			final Monitor physicalDisk = hostMonitoring.findById("localhost@connector1_physical_disk_1");

			// Inject the alert information and a custom testing trigger verifying that our generated report ;)
			injectAlertInfoAndTriggerTest(hostMonitoring, physicalDisk,
					info -> assertNotEquals("${RECOMMENDED_ACTION}", OtelAlertHelper.buildHardwareProblem(info, "${RECOMMENDED_ACTION}")));

			CollectHelper.updateDiscreteParameter(physicalDisk, STATUS_PARAMETER, collectTime, Status.FAILED);
		}

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();

			initMonitorsForAlert(hostMonitoring);

			final Monitor physicalDisk = hostMonitoring.findById("localhost@connector1_physical_disk_1");

			// Inject the alert information and a custom testing trigger verifying that our generated report ;)
			injectAlertInfoAndTriggerTest(hostMonitoring, physicalDisk,
					info -> assertNotEquals("${PROBLEM}", OtelAlertHelper.buildHardwareProblem(info, "${PROBLEM}")));

			CollectHelper.updateDiscreteParameter(physicalDisk, STATUS_PARAMETER, collectTime, Status.FAILED);
		}

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();

			initMonitorsForAlert(hostMonitoring);

			final Monitor physicalDisk = hostMonitoring.findById("localhost@connector1_physical_disk_1");

			// Inject the alert information and a custom testing trigger verifying that our generated report ;)
			injectAlertInfoAndTriggerTest(hostMonitoring, physicalDisk,
					info -> assertNotEquals("${ALERT_DETAILS}", OtelAlertHelper.buildHardwareProblem(info, "${ALERT_DETAILS}")));

			CollectHelper.updateDiscreteParameter(physicalDisk, STATUS_PARAMETER, collectTime, Status.FAILED);
		}

	}

	@Test
	void testBuildHardwareProblemEdgeCases() {

		assertEquals(EMPTY, OtelAlertHelper.buildHardwareProblem(
				AlertInfo
					.builder()
					.alertRule(new AlertRule())
					.parameterName("unknownParam")
					.hostMonitoring(new HostMonitoring())
					.hardwareTarget(HardwareTarget.builder().build())
					.monitor(Monitor.builder().build())
					.build(), "template"));
		assertEquals(EMPTY, OtelAlertHelper.buildHardwareProblem(
				AlertInfo
					.builder()
					.alertRule(new AlertRule())
					.parameterName(STATUS_PARAMETER)
					.hostMonitoring(new HostMonitoring())
					.hardwareTarget(HardwareTarget.builder().build())
					.monitor(Monitor.builder().monitorType(MonitorType.BATTERY).build())
					.build(), ""));
		assertEquals(EMPTY, OtelAlertHelper.buildHardwareProblem(
				AlertInfo
					.builder()
					.alertRule(new AlertRule())
					.parameterName(STATUS_PARAMETER)
					.hostMonitoring(new HostMonitoring())
					.hardwareTarget(HardwareTarget.builder().build())
					.monitor(Monitor.builder().monitorType(MonitorType.BATTERY).build())
					.build(), null));
		assertThrows(IllegalArgumentException.class, () -> OtelAlertHelper.buildHardwareProblem(null, "template"));
	}
	/**
	 * Initialize target, enclosure and disk for alert testing
	 * 
	 * @param hostMonitoring
	 */
	public static void initMonitorsForAlert(final IHostMonitoring hostMonitoring) {
		final Monitor target = Monitor
				.builder()
				.id(HOSTNAME)
				.parentId(null)
				.targetId(HOSTNAME)
				.name(HOSTNAME)
				.monitorType(MonitorType.TARGET)
				.metadata(Map.of(FQDN, HOSTNAME))
				.build();

		hostMonitoring.addMonitor(target);

		final Map<String, String> enclosureMetadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		enclosureMetadata.put(ID_COUNT, "1");
		enclosureMetadata.put(VENDOR, "Pure");
		enclosureMetadata.put(MODEL, "FA-X20R2");
		enclosureMetadata.put(SERIAL_NUMBER, "FA-123456789");
		enclosureMetadata.put(TARGET_FQDN, HOSTNAME);

		final Monitor enclosure = Monitor.builder()
				.id("localhost@connector1_enclosure_1")
				.name("PureStorage FA-X20R2")
				.parentId(HOSTNAME)
				.targetId(HOSTNAME)
				.monitorType(MonitorType.ENCLOSURE)
				.extendedType(COMPUTER)
				.metadata(enclosureMetadata)
				.build();

		hostMonitoring.addMonitor(enclosure);

		final Map<String, String> diskMetadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		diskMetadata.put(VENDOR, "Pure");
		diskMetadata.put(MODEL, "SAS Flash Module");
		diskMetadata.put(SERIAL_NUMBER, "FM-123456789");
		diskMetadata.put(SIZE, "1000000000000");
		diskMetadata.put(TARGET_FQDN, HOSTNAME);
		diskMetadata.put(CONNECTOR, "Connector1");

		final Monitor physicalDisk = Monitor.builder()
				.id("localhost@connector1_physical_disk_1")
				.name("SAS Flash Module - CH0.BAY9")
				.parentId(enclosure.getId())
				.targetId(HOSTNAME)
				.metadata(diskMetadata)
				.monitorType(MonitorType.PHYSICAL_DISK)
				.build();

		hostMonitoring.addMonitor(physicalDisk);
	}

	/**
	 * Loop over all the monitor alert rules and set the alert information bean and
	 * the alertTrigger consumer
	 * 
	 * @param hostMonitoring  Wrapper of all the monitors
	 * @param monitor         Monitor instance
	 * @param alertTrigger    Consumer
	 */
	private static void injectAlertInfoAndTriggerTest(final IHostMonitoring hostMonitoring, final Monitor monitor,
			final Consumer<AlertInfo> alertTrigger) {

		// Loop over all the alert rules
		monitor.getAlertRules().entrySet().forEach(alertRulesEntry -> {
			final String parameterName = alertRulesEntry.getKey();
			// A parameter can have several alert rules
			alertRulesEntry.getValue().forEach(alertRule -> {
				// The trigger passed by the hws-agent
				alertRule.setTrigger(alertTrigger);
				// Create and set a new AlertInfo
				alertRule.setAlertInfo(AlertInfo
						.builder()
						.alertRule(alertRule)
						.monitor(monitor)
						.parameterName(parameterName)
						.hardwareTarget(HardwareTarget.builder().type(TargetType.LINUX).id(HOSTNAME).hostname(HOSTNAME).build())
						.hostMonitoring(hostMonitoring)
						.build()
				);
			});
		});
	}

	/**
	 * Assert the alert message
	 * 
	 * @param alertInfo
	 */
	private void assertAlertMessage(final AlertInfo alertInfo) {
		String actual = OtelAlertHelper.buildHardwareProblem(alertInfo, MultiHostsConfigurationDto.HW_PROBLEM_DEFAULT_TEMPLATE);
		// Replace the Local ISO date as it is impossible to get the exact platform date
		actual = actual.replaceAll("\\(\\d{4}\\-\\d{2}\\-\\d{2}T\\d{2}\\:\\d{2}\\:\\d{2}\\.\\d+\\)", "(2022-04-13T18:37:02.212)");
		String expected = ResourceHelper.getResourceAsString("/data/hardware-problem.txt", this.getClass());
		assertEquals(expected, actual);
	}

	@Test
	void testGetStatusInformation() {
		final Monitor physicalDisk = Monitor.builder()
				.monitorType(MonitorType.PHYSICAL_DISK)
				.build();
		CollectHelper.updateStatusInformation(physicalDisk, collectTime, Status.FAILED.name(), Status.FAILED);
		assertEquals(Status.FAILED.name(), OtelAlertHelper.getStatusInformation(physicalDisk).get());
		assertEquals(Optional.empty(), OtelAlertHelper.getStatusInformation(Monitor.builder().build()));
		assertThrows(IllegalArgumentException.class, () -> OtelAlertHelper.getStatusInformation(null));
	}

	@Test
	void testGetAlertTime() {
		final AlertRule alertRule = new AlertRule();
		alertRule.setFirstTriggerTimestamp(collectTime);
		final AlertInfo alertInfo = AlertInfo.builder().alertRule(alertRule).build();
		assertEquals(collectTime, OtelAlertHelper.getAlertTime(alertInfo));
		assertThrows(IllegalArgumentException.class, () -> OtelAlertHelper.getAlertTime(null));
		
	}

	@Test
	void testConvertToOtelSeverity() {
		final AlertRule alertRule = new AlertRule();
		final AlertInfo alertInfo = AlertInfo.builder().alertRule(alertRule).build();

		alertRule.setSeverity(Severity.ALARM);
		assertEquals(io.opentelemetry.sdk.logs.data.Severity.ERROR, OtelAlertHelper.convertToOtelSeverity(alertInfo));

		alertRule.setSeverity(Severity.WARN);
		assertEquals(io.opentelemetry.sdk.logs.data.Severity.WARN, OtelAlertHelper.convertToOtelSeverity(alertInfo));

		alertRule.setSeverity(Severity.INFO);
		assertEquals(io.opentelemetry.sdk.logs.data.Severity.INFO, OtelAlertHelper.convertToOtelSeverity(alertInfo));

		assertThrows(IllegalArgumentException.class, () -> OtelAlertHelper.convertToOtelSeverity(null));
	}

	@Test
	void testBuildAlertDetails() {
		final Monitor monitor = Monitor.builder()
							.id("battery")
							.name("battery")
							.parentId("enclosure")
							.targetId(HOSTNAME)
							.monitorType(MonitorType.BATTERY)
							.build();

		{
			CollectHelper.updateDiscreteParameter(monitor, STATUS_PARAMETER, collectTime, Status.FAILED);

			final AlertRule alertRule = Battery.STATUS_ALARM_ALERT_RULE.copy();
			alertRule.setDetails(AlertDetails.builder().problem("Elbow problem").consequence("Cannot play babyfoot")
					.recommendedAction("Do a massage").build());

			final String expected = "Alert Severity    : ALARM\n"
					+ "Alert Rule        : hw.battery.status{state=\"failed\"} == 1\n"
					+ "\n"
					+ "Alert Details\n"
					+ "=============\n"
					+ "Problem           : Elbow problem\n"
					+ "Consequence       : Cannot play babyfoot\n"
					+ "Recommended Action: Do a massage";

			assertEquals(expected, OtelAlertHelper.buildAlertDetails(alertRule, monitor, STATUS_PARAMETER));
			assertThrows(IllegalArgumentException.class, () -> OtelAlertHelper.buildAlertDetails(null, monitor, STATUS_PARAMETER));
			assertThrows(IllegalArgumentException.class, () -> OtelAlertHelper.buildAlertDetails(alertRule, null, STATUS_PARAMETER));
			assertThrows(IllegalArgumentException.class, () -> OtelAlertHelper.buildAlertDetails(null, monitor, null));
		}

		{
			CollectHelper.updateDiscreteParameter(monitor, PRESENT_PARAMETER, collectTime, Present.MISSING);
			final AlertRule alertRule = Battery.PRESENT_ALERT_RULE.copy();
			alertRule.setDetails(AlertDetails.builder().problem("Elbow problem").consequence("Cannot play babyfoot")
					.recommendedAction("Do a massage").build());

			final String expected = "Alert Severity    : ALARM\n"
					+ "Alert Rule        : hw.battery.status{state=\"present\"} == 0\n"
					+ "\n"
					+ "Alert Details\n"
					+ "=============\n"
					+ "Problem           : Elbow problem\n"
					+ "Consequence       : Cannot play babyfoot\n"
					+ "Recommended Action: Do a massage";
			assertEquals(expected, OtelAlertHelper.buildAlertDetails(alertRule, monitor, PRESENT_PARAMETER));
		}


		{
			CollectHelper.updateNumberParameter(monitor, CHARGE_PARAMETER, "", collectTime, 10D, 10D);
			final AlertRule alertRule = Battery.CHARGE_ALARM_ALERT_RULE.copy();
			alertRule.setDetails(AlertDetails.builder().problem("Elbow problem").consequence("Cannot play babyfoot")
					.recommendedAction("Do a massage").build());

			final String expected = "Alert Severity    : ALARM\n"
					+ "Alert Rule        : hw.battery.charge <= 0.3\n"
					+ "\n"
					+ "Alert Details\n"
					+ "=============\n"
					+ "Problem           : Elbow problem\n"
					+ "Consequence       : Cannot play babyfoot\n"
					+ "Recommended Action: Do a massage";
			assertEquals(expected, OtelAlertHelper.buildAlertDetails(alertRule, monitor, CHARGE_PARAMETER));
		}
	}

	@Test
	void testBuildMetricValue() {
		final Monitor physicalDisk = Monitor.builder()
				.monitorType(MonitorType.PHYSICAL_DISK)
				.build();

		CollectHelper.updateDiscreteParameter(physicalDisk, STATUS_PARAMETER, collectTime, Status.FAILED);
		CollectHelper.updateNumberParameter(physicalDisk, ERROR_COUNT_PARAMETER, "", collectTime, 2d, 2d);

		assertEquals("1 (Failed)", OtelAlertHelper.buildMetricValue(physicalDisk, STATUS_PARAMETER));
		assertEquals("2", OtelAlertHelper.buildMetricValue(physicalDisk, ERROR_COUNT_PARAMETER));
		assertEquals(EMPTY, OtelAlertHelper.buildMetricValue(physicalDisk, ENDURANCE_REMAINING_PARAMETER));
		assertThrows(IllegalArgumentException.class, () -> OtelAlertHelper.buildMetricValue(physicalDisk, null));
		assertThrows(IllegalArgumentException.class, () -> OtelAlertHelper.buildMetricValue(null, STATUS_PARAMETER));
		assertEquals("", OtelAlertHelper.buildMetricValue(Monitor.builder().build(), STATUS_PARAMETER));
	}

	@Test
	void testBuildMetricName() {
		final Monitor physicalDisk = Monitor.builder()
				.monitorType(MonitorType.PHYSICAL_DISK)
				.build();
		CollectHelper.updateDiscreteParameter(physicalDisk, STATUS_PARAMETER, collectTime, Status.FAILED);

		assertEquals("hw.physical_disk.status{state=\"failed\"}", OtelAlertHelper.buildMetricName(physicalDisk, STATUS_PARAMETER));
		assertThrows(IllegalArgumentException.class, () -> OtelAlertHelper.buildMetricName(null, STATUS_PARAMETER));
		assertThrows(IllegalArgumentException.class, () -> OtelAlertHelper.buildMetricName(physicalDisk, (String) null));

		CollectHelper.updateNumberParameter(physicalDisk, ENERGY_PARAMETER, EMPTY, collectTime, 1000D, 1000D);
		assertEquals("hw.physical_disk.energy", OtelAlertHelper.buildMetricName(physicalDisk, ENERGY_PARAMETER));
	}

	@Test
	void testBuildParentInformation() {
		assertEquals("target is the root monitor", OtelAlertHelper.buildParentInformation("target", null, new HostMonitoring()));
		assertEquals("No information available on the parent", OtelAlertHelper.buildParentInformation(null, "parent_id", new HostMonitoring()));

		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Map<String, String> enclosureMetadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		enclosureMetadata.put(ID_COUNT, "1");
		enclosureMetadata.put(VENDOR, "Pure");
		enclosureMetadata.put(MODEL, "FA-X20R2");
		enclosureMetadata.put(SERIAL_NUMBER, "FA-123456789");
		enclosureMetadata.put(TARGET_FQDN, HOSTNAME);

		final Monitor enclosure = Monitor.builder()
				.id("localhost@connector1_enclosure_1")
				.name("PureStorage FA-X20R2")
				.parentId(HOSTNAME)
				.targetId(HOSTNAME)
				.metadata(enclosureMetadata)
				.monitorType(MonitorType.ENCLOSURE)
				.extendedType(COMPUTER)
				.build();

		hostMonitoring.addMonitor(enclosure);

		final String id = enclosure.getId();

		assertEquals("This object is attached to: PureStorage FA-X20R2\n"
				+ "Type              : Enclosure\n"
				+ "Manufacturer      : Pure\n"
				+ "Model             : FA-X20R2\n"
				+ "Serial Number     : FA-123456789", OtelAlertHelper.buildParentInformation("disk", id, hostMonitoring));
		
		assertThrows(IllegalArgumentException.class, () -> OtelAlertHelper.buildParentInformation("disk", id, null));
	}

}
