package com.sentrysoftware.hardware.prometheus.service;

import static com.sentrysoftware.hardware.prometheus.service.HostMonitoringCollectorService.ID;
import static com.sentrysoftware.hardware.prometheus.service.HostMonitoringCollectorService.LABEL;
import static com.sentrysoftware.hardware.prometheus.service.HostMonitoringCollectorService.PARENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.ResourceHelper;
import com.sentrysoftware.matrix.common.meta.monitor.Enclosure;
import com.sentrysoftware.matrix.common.meta.monitor.MetaConnector;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
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
	private IHostMonitoring hostMonitoring;

	@InjectMocks
	private HostMonitoringCollectorService hostMonitoringCollectorService = new HostMonitoringCollectorService();

	@Test
	void testCollect() throws IOException {
		final StatusParam statusParam = StatusParam.builder().name(HardwareConstants.STATUS_PARAMETER).state(ParameterState.OK).build();
		final NumberParam numberParam = NumberParam.builder().name(HardwareConstants.ENERGY_USAGE_PARAMETER).value(3000D).build();
		Map<String, Monitor> enclosures = Map.of(ENCLOSURE_ID, Monitor.builder()
				.id(ENCLOSURE_ID)
				.parentId(ECS)
				.name(ENCLOSURE_NAME)
				.parameters(Map.of(
						HardwareConstants.STATUS_PARAMETER, statusParam,
						HardwareConstants.ENERGY_USAGE_PARAMETER, numberParam))
				.build());

		Map<String, Monitor> fans = new LinkedHashMap<String, Monitor>(); 
		fans.put(FAN_ID + 1, Monitor.builder()
				.id(FAN_ID + 1)
				.parentId(ENCLOSURE_ID)
				.name(FAN_NAME + 1)
				.parameters(Map.of(HardwareConstants.STATUS_PARAMETER, statusParam))
				.build());
		fans.put(FAN_ID + 2, Monitor.builder()
				.id(FAN_ID + 2)
				.parentId(ENCLOSURE_ID)
				.name(FAN_NAME + 2)
				.parameters(Map.of(HardwareConstants.STATUS_PARAMETER, statusParam))
				.build());

		Map<MonitorType, Map<String, Monitor>> monitors = new LinkedHashMap<>();
		monitors.put(MonitorType.ENCLOSURE, enclosures);
		monitors.put(MonitorType.FAN, fans);

		doReturn(monitors).when(hostMonitoring).getMonitors();

		CollectorRegistry.defaultRegistry.clear();

		hostMonitoringCollectorService.register();

		final StringWriter writer = new StringWriter();
		TextFormat.write004(writer , CollectorRegistry.defaultRegistry.metricFamilySamples());
		writer.flush();

		assertEquals(ResourceHelper.getResourceAsString("/data/metrics.txt", HostMonitoringCollectorService.class), writer.toString());
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

		Set<Sample> actual = mfs.get(0).samples.stream().collect(Collectors.toSet());
		final Sample sample1 = new Sample("enclosure_status", Arrays.asList(ID, PARENT, LABEL),
				Arrays.asList(monitor1.getId(), monitor1.getParentId(), monitor1.getName()), ParameterState.OK.ordinal());
		final Sample sample2 = new Sample("enclosure_status", Arrays.asList(ID, PARENT, LABEL),
				Arrays.asList(monitor2.getId(), monitor2.getParentId(), monitor2.getName()), ParameterState.OK.ordinal());
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
				Arrays.asList(ID, PARENT, LABEL));
		expected.addMetric(Arrays.asList(monitor1.getId(), PARENT_ID_VALUE, LABEL_VALUE), 0);

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
		assertEquals("Metric: Enclosure energyUsage - Unit: Joules",
				HostMonitoringCollectorService.buildHelp("Enclosure", Enclosure.ENERGY_USAGE));
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
			assertTrue(HostMonitoringCollectorService.isParameterAvailable(Enclosure.STATUS, monitor));
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
			assertFalse(HostMonitoringCollectorService.isParameterAvailable(Enclosure.STATUS, monitor));

		}

		{
			TextParam textParam = TextParam.builder().name(HardwareConstants.TEST_REPORT_PARAMETER).value("text").build();
			final Monitor monitor = Monitor.builder()
						.id(ID_VALUE)
						.parentId(PARENT_ID_VALUE)
						.name(LABEL_VALUE)
						.parameters(Map.of(HardwareConstants.TEST_REPORT_PARAMETER, textParam))
						.build();
			assertFalse(HostMonitoringCollectorService.isParameterAvailable(MetaConnector.TEST_REPORT, monitor));

		}
		
	}

	@Test
	void testCheckNumberParameter() {
		{
			final Monitor monitor = Monitor.builder()
					.id(ID_VALUE)
					.parentId(PARENT_ID_VALUE)
					.name(LABEL_VALUE)
					.parameters(Map.of(HardwareConstants.ENERGY_USAGE_PARAMETER,
							NumberParam.builder().name(HardwareConstants.ENERGY_USAGE_PARAMETER)
							.value(3000D).build()))
					.build();
			assertTrue(HostMonitoringCollectorService.checkNumberParameter(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER));
		}

		{
			final Monitor monitor = Monitor.builder()
					.id(ID_VALUE)
					.parentId(PARENT_ID_VALUE)
					.name(LABEL_VALUE)
					.parameters(Map.of(HardwareConstants.ENERGY_USAGE_PARAMETER,
							NumberParam.builder().name(HardwareConstants.ENERGY_USAGE_PARAMETER)
							.value(null).build()))
					.build();
			assertFalse(HostMonitoringCollectorService.checkNumberParameter(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER));
		}
		{
			final Monitor monitor = Monitor.builder()
					.id(ID_VALUE)
					.parentId(PARENT_ID_VALUE)
					.name(LABEL_VALUE)
					.parameters(Collections.emptyMap())
					.build();
			assertFalse(HostMonitoringCollectorService.checkNumberParameter(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER));
		}
	}

	@Test
	void testCheckStatusParameter() {

		{
			final StatusParam statusParam = StatusParam.builder().name(HardwareConstants.STATUS_PARAMETER).state(ParameterState.OK).build();
			final Monitor monitor = Monitor.builder()
						.id(ID_VALUE)
						.parentId(PARENT_ID_VALUE)
						.name(LABEL_VALUE)
						.parameters(Map.of(HardwareConstants.STATUS_PARAMETER, statusParam))
						.build();
			assertTrue(HostMonitoringCollectorService.checkStatusParameter(monitor, HardwareConstants.STATUS_PARAMETER));
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
			assertFalse(HostMonitoringCollectorService.checkStatusParameter(monitor, HardwareConstants.STATUS_PARAMETER));
		}
		
		{
			final Monitor monitor = Monitor.builder()
						.id(ID_VALUE)
						.parentId(PARENT_ID_VALUE)
						.name(LABEL_VALUE)
						.parameters(Collections.emptyMap())
						.build();
			assertFalse(HostMonitoringCollectorService.checkStatusParameter(monitor, HardwareConstants.STATUS_PARAMETER));
		}
	}

	@Test
	void testGetStatusParameterValue() {
		final Monitor monitor = Monitor.builder()
				.id(ID_VALUE)
				.parentId(PARENT_ID_VALUE)
				.name(LABEL_VALUE)
				.parameters(Map.of(HardwareConstants.STATUS_PARAMETER,
						StatusParam.builder()
						.name(HardwareConstants.STATUS_PARAMETER)
						.state(ParameterState.OK).build()))
				.build();
		assertEquals(0, HostMonitoringCollectorService.getStatusParameterValue(monitor, HardwareConstants.STATUS_PARAMETER));
	}

	@Test
	void testGetNumberParameterValue() {
		final Monitor monitor = Monitor.builder()
				.id(ID_VALUE)
				.parentId(PARENT_ID_VALUE)
				.name(LABEL_VALUE)
				.parameters(Map.of(HardwareConstants.ENERGY_USAGE_PARAMETER,
						NumberParam.builder().name(HardwareConstants.ENERGY_USAGE_PARAMETER)
						.value(3000D).build()))
				.build();
		assertEquals(3000D, HostMonitoringCollectorService.getNumberParameterValue(monitor, HardwareConstants.ENERGY_USAGE_PARAMETER));
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
	void testAddStatusMetric() {
		final GaugeMetricFamily gauge = new GaugeMetricFamily(MONITOR_STATUS_METRIC, HELP_DEFAULT, Arrays.asList(ID, PARENT, LABEL));
		final Monitor monitor = Monitor.builder()
				.id(ID_VALUE)
				.parentId(PARENT_ID_VALUE)
				.name(LABEL_VALUE)
				.parameters(Map.of(HardwareConstants.STATUS_PARAMETER,
						StatusParam.builder()
						.name(HardwareConstants.STATUS_PARAMETER)
						.state(ParameterState.OK).build()))
				.build();
		HostMonitoringCollectorService.addStatusMetric(gauge, monitor, HardwareConstants.STATUS_PARAMETER);
		final Sample actual = gauge.samples.get(0);
		final Sample expected = new Sample(MONITOR_STATUS_METRIC, Arrays.asList(ID, PARENT, LABEL),
				Arrays.asList(ID_VALUE, PARENT_ID_VALUE, LABEL_VALUE), ParameterState.OK.ordinal());

		assertEquals(expected, actual);
	}

	@Test
	void testAddNumberMetric() {
		final GaugeMetricFamily gauge = new GaugeMetricFamily(MONITOR_ENERGY_USAGE_METRIC, HELP_DEFAULT, Arrays.asList(ID, PARENT, LABEL));
		final Monitor monitor = Monitor.builder()
				.id(ID_VALUE)
				.parentId(PARENT_ID_VALUE)
				.name(LABEL_VALUE)
				.parameters(Map.of(HardwareConstants.ENERGY_USAGE_PARAMETER,
						NumberParam.builder().name(HardwareConstants.ENERGY_USAGE_PARAMETER)
						.value(3000D).build()))
				.build();
		HostMonitoringCollectorService.addNumberMetric(gauge, monitor, HardwareConstants.ENERGY_USAGE_PARAMETER);
		final Sample actual = gauge.samples.get(0);
		final Sample expected = new Sample(MONITOR_ENERGY_USAGE_METRIC, Arrays.asList(ID, PARENT, LABEL),
				Arrays.asList(ID_VALUE, PARENT_ID_VALUE, LABEL_VALUE), 3000D);

		assertEquals(expected, actual);
	}

	@Test
	void testCreateLabels() {
		final List<String> actual = HostMonitoringCollectorService.createLabels(
				Monitor.builder().id(ID_VALUE).parentId(PARENT_ID_VALUE).name(LABEL_VALUE).build());
		final List<String> expected = Arrays.asList(ID_VALUE, PARENT_ID_VALUE, LABEL_VALUE);
		assertEquals(expected, actual);
	}

}
