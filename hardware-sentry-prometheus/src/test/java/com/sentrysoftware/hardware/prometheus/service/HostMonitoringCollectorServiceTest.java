package com.sentrysoftware.hardware.prometheus.service;

import static com.sentrysoftware.hardware.prometheus.service.HostMonitoringCollectorService.ID;
import static com.sentrysoftware.hardware.prometheus.service.HostMonitoringCollectorService.LABEL;
import static com.sentrysoftware.hardware.prometheus.service.HostMonitoringCollectorService.PARENT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION1;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FAN_TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FQDN;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MODEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SERIAL_NUMBER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TARGET_FQDN;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VENDOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.ResourceHelper;
import com.sentrysoftware.matrix.common.meta.monitor.Enclosure;
import com.sentrysoftware.matrix.common.meta.monitor.LogicalDisk;
import com.sentrysoftware.matrix.common.meta.monitor.MetaConnector;
import com.sentrysoftware.matrix.common.meta.monitor.Voltage;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.PresentParam;
import com.sentrysoftware.matrix.model.parameter.StatusParam;
import com.sentrysoftware.matrix.model.parameter.TextParam;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.GaugeMetricFamily;
import io.prometheus.client.exporter.common.TextFormat;

@ExtendWith(MockitoExtension.class)
class HostMonitoringCollectorServiceTest {

	private static final String ENCLOSURE_NAME = "Enclosure ECS";
	private static final String ECS = "ecs";
	private static final String ENCLOSURE_ID = "connector1.connector_enclosure_ecs_1.1";
	private static final String HELP_DEFAULT = "help";
	private static final String MONITOR_STATUS_METRIC = "monitor_status";
	private static final String MONITOR_ENERGY_USAGE_METRIC = "monitor_energy_usage";
	private static final String LABEL_VALUE = "monitor";
	private static final String PARENT_ID_VALUE = "parentId";
	private static final String ID_VALUE = "id";
	private static final String FAN_ID = "connector1.connector_fan_ecs_1.1";
	private static final String FAN_NAME = "Fan 1.1";

	@Mock
	private Map<String, IHostMonitoring> hostMonitoringMap;

	@InjectMocks
	private final HostMonitoringCollectorService hostMonitoringCollectorService = new HostMonitoringCollectorService();

	@Test
	void testCollect() throws IOException {

		final StatusParam statusParam = StatusParam.builder().name(HardwareConstants.STATUS_PARAMETER).state(ParameterState.OK).build();
		final NumberParam numberParam = NumberParam.builder().name(HardwareConstants.ENERGY_USAGE_PARAMETER).value(3000D).build();

		Monitor enclosureMonitor = Monitor.builder()
			.id(ENCLOSURE_ID)
			.parentId(ECS)
			.name(ENCLOSURE_NAME)
			.parameters(Map.of(
				HardwareConstants.STATUS_PARAMETER, statusParam,
				HardwareConstants.ENERGY_USAGE_PARAMETER, numberParam))
			.build();
		enclosureMonitor.addMetadata(TARGET_FQDN, TARGET_FQDN);
		enclosureMonitor.addMetadata(DEVICE_ID, "1.1");
		enclosureMonitor.addMetadata(SERIAL_NUMBER, "XXX888");
		enclosureMonitor.addMetadata(VENDOR, "Dell");
		enclosureMonitor.addMetadata(MODEL, "PowerEdge R630");
		enclosureMonitor.addMetadata(TYPE, "Computer");
		enclosureMonitor.addMetadata(ADDITIONAL_INFORMATION1, "Additional info test");
		Map<String, Monitor> enclosures = Map.of(ENCLOSURE_ID, enclosureMonitor);

		Monitor fan1Monitor = Monitor.builder()
			.id(FAN_ID + 1)
			.parentId(ENCLOSURE_ID)
			.name(FAN_NAME + 1)
			.parameters(Map.of(HardwareConstants.STATUS_PARAMETER, statusParam))
			.build();
		fan1Monitor.addMetadata(TARGET_FQDN, TARGET_FQDN);
		fan1Monitor.addMetadata(DEVICE_ID, "1.11");
		fan1Monitor.addMetadata(FAN_TYPE, "default cooling");

		Monitor fan2Monitor = Monitor.builder()
			.id(FAN_ID + 2)
			.parentId(ENCLOSURE_ID)
			.name(FAN_NAME + 2)
			.parameters(Map.of(HardwareConstants.STATUS_PARAMETER, statusParam))
			.build();
		fan2Monitor.addMetadata(TARGET_FQDN, TARGET_FQDN);
		fan2Monitor.addMetadata(DEVICE_ID, "1.12");
		fan2Monitor.addMetadata(FAN_TYPE, "default cooling");

		Map<String, Monitor> fans = new LinkedHashMap<>();
		fans.put(FAN_ID + 1, fan1Monitor);
		fans.put(FAN_ID + 2, fan2Monitor);

		Map<MonitorType, Map<String, Monitor>> monitors = new LinkedHashMap<>();
		monitors.put(MonitorType.ENCLOSURE, enclosures);
		monitors.put(MonitorType.FAN, fans);

		IHostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setMonitors(monitors);
		doReturn(Map.of(ECS, hostMonitoring).entrySet()).when(hostMonitoringMap).entrySet();

		CollectorRegistry.defaultRegistry.clear();

		hostMonitoringCollectorService.register();

		final StringWriter writer = new StringWriter();
		TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples());
		writer.flush();

		assertEquals(ResourceHelper.getResourceAsString("/data/metrics.txt", HostMonitoringCollectorService.class), writer.toString());
	}

	@Test
	void testCollectNoSpecificInfo() throws IOException {

		final StatusParam statusParam = StatusParam.builder().name(HardwareConstants.STATUS_PARAMETER).state(ParameterState.OK).build();
		final NumberParam numberParam = NumberParam.builder().name(HardwareConstants.ENERGY_USAGE_PARAMETER).value(3000D).build();

		Monitor enclosureMonitor = Monitor.builder()
			.id(ENCLOSURE_ID)
			.parentId(ECS)
			.name(ENCLOSURE_NAME)
			.parameters(Map.of(
				HardwareConstants.STATUS_PARAMETER, statusParam,
				HardwareConstants.ENERGY_USAGE_PARAMETER, numberParam))
			.build();
		enclosureMonitor.addMetadata(TARGET_FQDN, TARGET_FQDN);
		Map<String, Monitor> enclosures = Map.of(ENCLOSURE_ID, enclosureMonitor);

		Map<MonitorType, Map<String, Monitor>> monitors = new LinkedHashMap<>();
		monitors.put(MonitorType.ENCLOSURE, enclosures);

		IHostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.setMonitors(monitors);
		doReturn(Map.of(ECS, hostMonitoring).entrySet()).when(hostMonitoringMap).entrySet();

		try(MockedStatic<PrometheusSpecificities> prometheusSpecificities = mockStatic(PrometheusSpecificities.class)){
			prometheusSpecificities.when(() -> PrometheusSpecificities.getLabels(MonitorType.ENCLOSURE)).thenReturn(null);
			CollectorRegistry.defaultRegistry.clear();

			assertThrows(IllegalStateException.class, () -> hostMonitoringCollectorService.register());

		}

		try (MockedStatic<PrometheusSpecificities> prometheusSpecificities = mockStatic(PrometheusSpecificities.class)) {
			prometheusSpecificities.when(() -> PrometheusSpecificities.getLabels(MonitorType.ENCLOSURE)).thenReturn(Collections.emptyList());
			CollectorRegistry.defaultRegistry.clear();

			assertThrows(IllegalStateException.class, () -> hostMonitoringCollectorService.register());

		}

	}

	@Test
	void testProcessSameTypeMonitors() {
		final StatusParam statusParam = StatusParam.builder().name(HardwareConstants.STATUS_PARAMETER).state(ParameterState.OK).build();
		final Monitor monitor1 = Monitor.builder()
				.id(ID_VALUE + 1)
				.parentId(PARENT_ID_VALUE)
				.name(LABEL_VALUE + 1)
				.parameters(Map.of(HardwareConstants.STATUS_PARAMETER, statusParam))
				.build();
		final Monitor monitor2 = Monitor.builder()
				.id(ID_VALUE  + 2)
				.parentId(PARENT_ID_VALUE)
				.name(LABEL_VALUE + 2)
				.parameters(Map.of(HardwareConstants.STATUS_PARAMETER, statusParam))
				.build();
		final Map<String, Monitor> monitors = Map.of(
				monitor1.getId(), monitor1,
				monitor2.getId(), monitor2);

		final List<MetricFamilySamples> mfs = new ArrayList<>();

		HostMonitoringCollectorService.processSameTypeMonitors(MonitorType.ENCLOSURE, monitors, mfs);

		Set<Sample> actual = new HashSet<>(mfs.get(0).samples);
		final Sample sample1 = new Sample("enclosure_status", Arrays.asList(ID, PARENT, LABEL, FQDN),
				Arrays.asList(monitor1.getId(), monitor1.getParentId(), monitor1.getName(), null), ParameterState.OK.ordinal());
		final Sample sample2 = new Sample("enclosure_status", Arrays.asList(ID, PARENT, LABEL, FQDN),
				Arrays.asList(monitor2.getId(), monitor2.getParentId(), monitor2.getName(), null), ParameterState.OK.ordinal());
		final Set<Sample> expected = Stream.of(sample1, sample2).collect(Collectors.toSet());

		assertEquals(expected, actual);
	}

	@Test
	void testProcessSameTypeMonitorsNoMonitors() {
		final List<MetricFamilySamples> mfs = new ArrayList<>();
		HostMonitoringCollectorService.processSameTypeMonitors(MonitorType.ENCLOSURE, Collections.emptyMap(), mfs);
		assertTrue(mfs.isEmpty());

		HostMonitoringCollectorService.processSameTypeMonitors(MonitorType.ENCLOSURE, null, mfs);
		assertTrue(mfs.isEmpty());
	}

	@Test
	void testProcessMonitorsMetric() {
		final StatusParam statusParam = StatusParam.builder().name(HardwareConstants.STATUS_PARAMETER).state(ParameterState.OK).build();
		final Monitor monitor1 = Monitor.builder()
					.id(ID_VALUE + 1)
					.parentId(PARENT_ID_VALUE)
					.name(LABEL_VALUE)
					.parameters(Map.of(HardwareConstants.STATUS_PARAMETER, statusParam))
					.build();
		final Monitor monitor2 = Monitor.builder()
				.id(ID_VALUE  + 2)
				.parentId(PARENT_ID_VALUE)
				.name(LABEL_VALUE)
				.parameters(Collections.emptyMap())
				.build();
		final Map<String, Monitor> monitors = Map.of(
				monitor1.getId(), monitor1,
				monitor2.getId(), monitor2);

		final List<MetricFamilySamples> mfs = new ArrayList<>();

		HostMonitoringCollectorService.processMonitorsMetric(Enclosure.STATUS, MonitorType.ENCLOSURE, monitors, mfs);

		final GaugeMetricFamily expected = new GaugeMetricFamily(
				"enclosure_status",
				"Metric: Enclosure status - Unit: {0 = OK ; 1 = Degraded ; 2 = Failed}",
				Arrays.asList(ID, PARENT, LABEL, FQDN));
		expected.addMetric(Arrays.asList(monitor1.getId(), PARENT_ID_VALUE, LABEL_VALUE, null), 0);

		assertEquals(expected, mfs.get(0));
	}

	@Test
	void testProcessMonitorsMetricNotAvailable() {
		final Monitor monitor1 = Monitor.builder()
				.id(ID_VALUE + 1)
				.parentId(PARENT_ID_VALUE)
				.name(LABEL_VALUE)
				.parameters(Collections.emptyMap())
				.build();
		final Monitor monitor2 = Monitor.builder()
				.id(ID_VALUE  + 2)
				.parentId(PARENT_ID_VALUE)
				.name(LABEL_VALUE)
				.parameters(Collections.emptyMap())
				.build();
		final Map<String, Monitor> monitors = Map.of(
				monitor1.getId(), monitor1,
				monitor2.getId(), monitor2);
		final List<MetricFamilySamples> mfs = new ArrayList<>();

		HostMonitoringCollectorService.processMonitorsMetric(Enclosure.STATUS, MonitorType.ENCLOSURE, monitors, mfs);

		assertTrue(mfs.isEmpty());
	}

	@Test
	void testBuildHelp() {
		assertEquals("Metric: Enclosure energy_usage - Unit: Joules",
				HostMonitoringCollectorService.buildHelp("Enclosure", Enclosure.ENERGY_USAGE));
		assertEquals("Metric: Voltage voltage - Unit: volts",
				HostMonitoringCollectorService.buildHelp("Voltage", Voltage._VOLTAGE));
		// this should be wrong
		assertEquals("Metric: logicaldisk voltage - Unit: mV",
				HostMonitoringCollectorService.buildHelp("logicaldisk", Voltage._VOLTAGE));
		assertEquals("Metric: logicaldisk unallocated_space - Unit: bytes",
				HostMonitoringCollectorService.buildHelp("logicaldisk", LogicalDisk.UNALLOCATED_SPACE));
	}

	@Test
	void testBuildMetricName() {
		assertEquals("enclosure_status", HostMonitoringCollectorService.buildMetricName("Enclosure", "status"));
		assertEquals("enclosure_status", HostMonitoringCollectorService.buildMetricName("Enclosure", "Status"));
		assertEquals("enclosure_energy_usage", HostMonitoringCollectorService.buildMetricName("Enclosure", "energyUsage"));
	}

	@Test
	void testIsParameterFamilyAvailableOnMonitors() {
		{
			final StatusParam statusParam = StatusParam.builder().name(HardwareConstants.STATUS_PARAMETER).state(ParameterState.OK).build();
			final Monitor monitor1 = Monitor.builder()
						.id(ID_VALUE + 1)
						.parentId(PARENT_ID_VALUE)
						.name(LABEL_VALUE)
						.parameters(Map.of(HardwareConstants.STATUS_PARAMETER, statusParam))
						.build();
			final Monitor monitor2 = Monitor.builder()
					.id(ID_VALUE  + 2)
					.parentId(PARENT_ID_VALUE)
					.name(LABEL_VALUE)
					.parameters(Collections.emptyMap())
					.build();
			final Map<String, Monitor> monitors = Map.of(
					monitor1.getId(), monitor1,
					monitor2.getId(), monitor2);
			assertTrue(HostMonitoringCollectorService.isParameterFamilyAvailableOnMonitors(Enclosure.STATUS, monitors));
		}

		{
			final Monitor monitor1 = Monitor.builder()
						.id(ID_VALUE + 1)
						.parentId(PARENT_ID_VALUE)
						.name(LABEL_VALUE)
						.parameters(Collections.emptyMap())
						.build();
			final Monitor monitor2 = Monitor.builder()
					.id(ID_VALUE  + 2)
					.parentId(PARENT_ID_VALUE)
					.name(LABEL_VALUE)
					.parameters(Collections.emptyMap())
					.build();
			final Map<String, Monitor> monitors = Map.of(
					monitor1.getId(), monitor1,
					monitor2.getId(), monitor2);
			assertFalse(HostMonitoringCollectorService.isParameterFamilyAvailableOnMonitors(Enclosure.STATUS, monitors));
		}
		assertFalse(HostMonitoringCollectorService.isParameterFamilyAvailableOnMonitors(Enclosure.STATUS, null));
		assertFalse(HostMonitoringCollectorService.isParameterFamilyAvailableOnMonitors(Enclosure.STATUS, Collections.emptyMap()));
	}

	@Test
	void testIsParameterAvailable() {

		{
			final StatusParam statusParam = StatusParam.builder().name(HardwareConstants.STATUS_PARAMETER).state(ParameterState.OK).build();
			final Monitor monitor = Monitor.builder()
						.id(ID_VALUE)
						.parentId(PARENT_ID_VALUE)
						.name(LABEL_VALUE)
						.parameters(Map.of(HardwareConstants.STATUS_PARAMETER, statusParam))
						.build();
			assertTrue(HostMonitoringCollectorService.isParameterAvailable(monitor, Enclosure.STATUS.getName()));
		}

		{
			StatusParam statusParamToReset = StatusParam.builder().name(HardwareConstants.STATUS_PARAMETER).state(ParameterState.OK).build();
			statusParamToReset.reset();
			final Monitor monitor = Monitor.builder()
						.id(ID_VALUE)
						.parentId(PARENT_ID_VALUE)
						.name(LABEL_VALUE)
						.parameters(Map.of(HardwareConstants.STATUS_PARAMETER, statusParamToReset))
						.build();
			assertFalse(HostMonitoringCollectorService.isParameterAvailable(monitor, Enclosure.STATUS.getName()));

		}

		{
			TextParam textParam = TextParam.builder().name(HardwareConstants.TEST_REPORT_PARAMETER).value("text").build();
			final Monitor monitor = Monitor.builder()
						.id(ID_VALUE)
						.parentId(PARENT_ID_VALUE)
						.name(LABEL_VALUE)
						.parameters(Map.of(HardwareConstants.TEST_REPORT_PARAMETER, textParam))
						.build();
			assertFalse(HostMonitoringCollectorService.isParameterAvailable(monitor, MetaConnector.TEST_REPORT.getName()));

		}
		
	}

	@Test
	void testGetParameterValueStatus() {
		final Monitor monitor = Monitor.builder()
				.id(ID_VALUE)
				.parentId(PARENT_ID_VALUE)
				.name(LABEL_VALUE)
				.parameters(Map.of(HardwareConstants.STATUS_PARAMETER,
						StatusParam.builder()
						.name(HardwareConstants.STATUS_PARAMETER)
						.state(ParameterState.OK).build()))
				.build();
		assertEquals(0, HostMonitoringCollectorService.getParameterValue(monitor, HardwareConstants.STATUS_PARAMETER));
	}

	@Test
	void testGetParameterValueNumber() {
		final Monitor monitor = Monitor.builder()
				.id(ID_VALUE)
				.parentId(PARENT_ID_VALUE)
				.name(LABEL_VALUE)
				.parameters(Map.of(HardwareConstants.ENERGY_USAGE_PARAMETER,
						NumberParam.builder().name(HardwareConstants.ENERGY_USAGE_PARAMETER)
						.value(3000D).build()))
				.build();
		assertEquals(3000D, HostMonitoringCollectorService.getParameterValue(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER));
	}

	@Test
	void testConvertParameterValueNumber() {
		final String logicalDiskMonitor = "LogicalDisk";
		final Monitor monitor = Monitor.builder()
				.id(ID_VALUE)
				.parentId(PARENT_ID_VALUE)
				.name(logicalDiskMonitor)
				.parameters(Map.of(HardwareConstants.UNALLOCATED_SPACE_PARAMETER,
						NumberParam.builder().name(HardwareConstants.UNALLOCATED_SPACE_PARAMETER)
						.value(100D).build()))
				.build();
		// make sure that the conversion is well done : factor 1073741824.0
		assertEquals(107374182400.0, HostMonitoringCollectorService.convertParameterValue(monitor, HardwareConstants.UNALLOCATED_SPACE_PARAMETER));

		final Monitor monitorValueNull = Monitor.builder()
				.id(ID_VALUE)
				.parentId(PARENT_ID_VALUE)
				.name(logicalDiskMonitor)
				.parameters(Map.of(HardwareConstants.UNALLOCATED_SPACE_PARAMETER,
						NumberParam.builder().name(HardwareConstants.UNALLOCATED_SPACE_PARAMETER)
						.build()))
				.build();
		assertEquals(null, HostMonitoringCollectorService.convertParameterValue(monitorValueNull, HardwareConstants.UNALLOCATED_SPACE_PARAMETER));
	}

	@Test
	void testCheckParameter() {
		{
			final Monitor monitor = Monitor.builder()
					.id(ID_VALUE)
					.parentId(PARENT_ID_VALUE)
					.name(LABEL_VALUE)
					.parameters(Map.of(HardwareConstants.ENERGY_USAGE_PARAMETER,
							NumberParam.builder().name(HardwareConstants.ENERGY_USAGE_PARAMETER)
							.value(3000D).build()))
					.build();
			assertTrue(HostMonitoringCollectorService.checkParameter(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER));
		}

		{
			final Monitor monitor = Monitor.builder()
					.id(ID_VALUE)
					.parentId(PARENT_ID_VALUE)
					.name(LABEL_VALUE)
					.parameters(Collections.emptyMap())
					.build();
			assertFalse(HostMonitoringCollectorService.checkParameter(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER));
		}

		{
			final Monitor monitor = Monitor.builder()
					.id(ID_VALUE)
					.parentId(PARENT_ID_VALUE)
					.name(LABEL_VALUE)
					.parameters(null)
					.build();
			assertFalse(HostMonitoringCollectorService.checkParameter(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER));
		}
		{
			assertFalse(HostMonitoringCollectorService.checkParameter(null, HardwareConstants.ENERGY_USAGE_PARAMETER));
		}
	}

	@Test
	void testGetValueOrElse() {
		assertEquals("value", HostMonitoringCollectorService.getValueOrElse("value", "other"));
		assertEquals("other", HostMonitoringCollectorService.getValueOrElse(null, "other"));
	}

	@Test
	void testAddMetricStatus() {
		final GaugeMetricFamily gauge = new GaugeMetricFamily(MONITOR_STATUS_METRIC, HELP_DEFAULT, Arrays.asList(ID, PARENT, LABEL, FQDN));
		final Monitor monitor = Monitor.builder()
				.id(ID_VALUE)
				.parentId(PARENT_ID_VALUE)
				.name(LABEL_VALUE)
				.parameters(Map.of(HardwareConstants.STATUS_PARAMETER,
						StatusParam.builder()
						.name(HardwareConstants.STATUS_PARAMETER)
						.state(ParameterState.OK).build()))
				.build();
		HostMonitoringCollectorService.addMetric(gauge, monitor, HardwareConstants.STATUS_PARAMETER);
		final Sample actual = gauge.samples.get(0);
		final Sample expected = new Sample(MONITOR_STATUS_METRIC, Arrays.asList(ID, PARENT, LABEL, FQDN),
				Arrays.asList(ID_VALUE, PARENT_ID_VALUE, LABEL_VALUE, null), ParameterState.OK.ordinal());

		assertEquals(expected, actual);
	}

	@Test
	void testAddMetricNumber() {
		final GaugeMetricFamily gauge = new GaugeMetricFamily(MONITOR_ENERGY_USAGE_METRIC, HELP_DEFAULT, Arrays.asList(ID, PARENT, LABEL, FQDN));
		final Monitor monitor = Monitor.builder()
				.id(ID_VALUE)
				.parentId(PARENT_ID_VALUE)
				.name(LABEL_VALUE)
				.parameters(Map.of(HardwareConstants.ENERGY_USAGE_PARAMETER,
						NumberParam.builder().name(HardwareConstants.ENERGY_USAGE_PARAMETER)
						.value(3000D).build()))
				.build();
		HostMonitoringCollectorService.addMetric(gauge, monitor, HardwareConstants.ENERGY_USAGE_PARAMETER);
		final Sample actual = gauge.samples.get(0);
		final Sample expected = new Sample(MONITOR_ENERGY_USAGE_METRIC, Arrays.asList(ID, PARENT, LABEL, FQDN),
				Arrays.asList(ID_VALUE, PARENT_ID_VALUE, LABEL_VALUE, null), 3000D);

		assertEquals(expected, actual);
	}

	@Test
	void testCreateLabels() {
		final List<String> actual = HostMonitoringCollectorService.createLabels(
				Monitor.builder().id(ID_VALUE).parentId(PARENT_ID_VALUE).name(LABEL_VALUE).build());
		final List<String> expected = Arrays.asList(ID_VALUE, PARENT_ID_VALUE, LABEL_VALUE, null);
		assertEquals(expected, actual);
	}

	@Test
	void testIsParameterAvailablePresent() {

		{
			final PresentParam presentParam = PresentParam.present();
			final Monitor monitor = Monitor.builder()
						.id(ID_VALUE)
						.parentId(PARENT_ID_VALUE)
						.name(LABEL_VALUE)
						.parameters(Map.of(HardwareConstants.PRESENT_PARAMETER, presentParam))
						.build();
			assertTrue(HostMonitoringCollectorService.isParameterAvailable(monitor, HardwareConstants.PRESENT_PARAMETER));
		}

		{
			final PresentParam presentParam = PresentParam.present();
			presentParam.discoveryReset();
			final Monitor monitor = Monitor.builder()
						.id(ID_VALUE)
						.parentId(PARENT_ID_VALUE)
						.name(LABEL_VALUE)
						.parameters(Map.of(HardwareConstants.PRESENT_PARAMETER, presentParam))
						.build();
			assertFalse(HostMonitoringCollectorService.isParameterAvailable(monitor, HardwareConstants.PRESENT_PARAMETER));
		}

		{
			final Monitor monitor = Monitor.builder()
						.id(ID_VALUE)
						.parentId(PARENT_ID_VALUE)
						.name(LABEL_VALUE)
						.parameters(Collections.emptyMap())
						.build();
			assertFalse(HostMonitoringCollectorService.isParameterAvailable(monitor, HardwareConstants.PRESENT_PARAMETER));
		}
	}

	@Test
	void testAddMetricPresent() {
		final Monitor monitor = Monitor.builder()
					.id(ID_VALUE)
					.parentId(PARENT_ID_VALUE)
					.name(LABEL_VALUE)
					.monitorType(MonitorType.FAN)
					.build();
		monitor.setAsPresent();

		final GaugeMetricFamily labeledGauge = new GaugeMetricFamily("monitor_present",
				"Metric: Fan present - Unit: {0 = Missing ; 1 = Present}", Arrays.asList(ID, PARENT, LABEL, FQDN));

		HostMonitoringCollectorService.addMetric(labeledGauge, monitor, HardwareConstants.PRESENT_PARAMETER);

		final GaugeMetricFamily expected = new GaugeMetricFamily(
				"monitor_present",
				"Metric: Fan present - Unit: {0 = Missing ; 1 = Present}",
				Arrays.asList(ID, PARENT, LABEL, FQDN));
		expected.addMetric(Arrays.asList(monitor.getId(), PARENT_ID_VALUE, LABEL_VALUE, null), 1);

		assertEquals(expected, labeledGauge);
	}
}
