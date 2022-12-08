package com.sentrysoftware.hardware.agent.service.opentelemetry.signal;

import static com.sentrysoftware.hardware.agent.mapping.opentelemetry.MappingConstants.ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BIOS_VERSION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FQDN;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.IDENTIFYING_INFORMATION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.IP_ADDRESS;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MODEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SERIAL_NUMBER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEST_REPORT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VENDOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.EnclosureMapping;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.MappingConstants;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.MetricsMapping;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.StaticIdentifyingAttribute;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.meta.monitor.Enclosure;
import com.sentrysoftware.matrix.common.meta.monitor.MetaConnector;
import com.sentrysoftware.matrix.common.meta.parameter.state.Status;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.TextParam;
import com.sentrysoftware.matrix.engine.host.HostType;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectHelper;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;

class OtelParameterToMetricObserverTest {

	private static final String HOSTNAME = "host.my.domain.sentrysoftware.org";
	private static final long COLLECT_TIME = System.currentTimeMillis();
	private static final String LABEL_VALUE = "monitor";
	private static final String PARENT_ID = "parent_id";
	private static final String MAXIMUM_SPEED = "maximumSpeed";

	@Test
	void testInitObserverOnStatusMetric() {

		final DiscreteParam parameter = DiscreteParam
			.builder()
			.state(Status.OK)
			.collectTime(new Date().getTime())
			.name(STATUS_PARAMETER)
			.build();

		final String expectedMetricName = "hw.status";

		final String expectedState = MappingConstants.OK_ATTRIBUTE_VALUE;

		final Monitor host = Monitor.builder().id(ID).name("host").build();
		host.addMetadata(FQDN, HOSTNAME);

		final Resource resource = OtelHelper.createHostResource(
			host.getId(),
			"host",
			HostType.LINUX,
			HOSTNAME,
			false,
			Collections.emptyMap(),
			Collections.emptyMap()
		);

		final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
		final SdkMeterProvider meterProvider = SdkMeterProvider
			.builder()
			.setResource(resource)
			.registerMetricReader(inMemoryReader)
			.build();

		final MultiHostsConfigurationDto multiHostsConfigurationDto= MultiHostsConfigurationDto
			.builder()
			.extraLabels(Map.of("site", "data center 1"))
			.build();

		final Monitor enclosure = Monitor
			.builder()
			.id("id_enclosure")
			.name("enclosure 1")
			.parentId("host")
			.monitorType(MonitorType.ENCLOSURE)
			.build();

		enclosure.addMetadata(FQDN, HOSTNAME);
		enclosure.addMetadata(DEVICE_ID, "1");
		enclosure.addMetadata(SERIAL_NUMBER, "SN 1");
		enclosure.addMetadata(VENDOR, "Dell");
		enclosure.addMetadata(MODEL, "PowerEdge T30");
		enclosure.addMetadata(BIOS_VERSION, "v1.1");
		enclosure.addMetadata(TYPE, "Server");
		enclosure.addMetadata(IDENTIFYING_INFORMATION, "Server 1 - Dell");
		enclosure.addMetadata(IP_ADDRESS, "192.168.1.1");

		OtelParameterToMetricObserver
			.builder()
			.monitor(enclosure)
			.sdkMeterProvider(meterProvider)
			.multiHostsConfigurationDto(multiHostsConfigurationDto)
			.metricInfoList(MetricsMapping.getMetricInfoList(MonitorType.ENCLOSURE, parameter.getName()).get())
			.matrixParameterName(parameter.getName())
			.build()
			.init();

		// This will trigger the observe callback
		Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();

		// The parameter is not collected yet
		assertTrue(metrics.isEmpty());

		enclosure.collectParameter(parameter);

		// Trigger the observe callback
		metrics = inMemoryReader.collectAllMetrics();

		// We should observe 3 metrics
		assertEquals(3, metrics.size());

		Map<String, MetricData> metricMap = metrics
			.stream()
			.collect(
				Collectors
					.toMap(
						metricData -> 
							metricData
							.getDoubleSumData()
							.getPoints()
							.stream()
							.findFirst()
							.orElseThrow()
							.getAttributes()
							.get(AttributeKey.stringKey("state")),
						Function.identity()
					)
			);

		MetricData metricData;
		String key;
		for (Entry<String, MetricData> metricEntry : metricMap.entrySet()) {
			key = metricEntry.getKey();
			int expectedValue = key.equals(expectedState) ? 1 : 0;

			metricData = metricEntry.getValue();
			assertNotNull(metricData);
			assertNotNull(metricData.getDescription());
			assertEquals(expectedMetricName, metricData.getName());

			final  Collection<DoublePointData> points = metricData.getDoubleSumData().getPoints();

			final DoublePointData dataPoint = points.stream().findFirst().orElse(null);
			assertEquals(expectedValue, dataPoint.getValue());

			final Attributes expected = Attributes.builder()
				.put(MappingConstants.ID, "id_enclosure")
				.put(MappingConstants.NAME, "enclosure 1")
				.put(MappingConstants.PARENT, "host")
				.put("site", "data center 1")
				.put("device_id", "1")
				.put("serial_number", "SN 1")
				.put("vendor", "Dell")
				.put("model", "PowerEdge T30")
				.put("bios_version", "v1.1")
				.put("type", "Server")
				.put("info", "Server 1 - Dell")
				.put("ip_address", "192.168.1.1")
				.put(MappingConstants.STATE_ATTRIBUTE_KEY, expectedValue == 1 ? expectedState : key)
				.put(MappingConstants.HW_TYPE_ATTRIBUTE_KEY, EnclosureMapping.HW_TYPE_ATTRIBUTE_VALUE)
				.build();

			assertEquals(expected, dataPoint.getAttributes());
		}

	}

	@Test
	void testInitObserverOnEnergy() {

		final NumberParam parameter = NumberParam
			.builder()
			.name(ENERGY_PARAMETER)
			.collectTime(COLLECT_TIME)
			.value(50000.0)
			.build();
		parameter.setPreviousCollectTime(COLLECT_TIME - 120000);
		parameter.setRawValue(50000.0);
		parameter.setPreviousRawValue(49950.0);

		final String expectedMetricName = "hw.enclosure.energy";

		final Monitor host = Monitor.builder().id(ID).name("host").build();

		host.addMetadata(FQDN, HOSTNAME);
		final Resource resource = OtelHelper.createHostResource(
			host.getId(),
			"host",
			HostType.LINUX,
			HOSTNAME,
			false,
			Collections.emptyMap(),
			Collections.emptyMap()
		);

		final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
		final SdkMeterProvider meterProvider = SdkMeterProvider.builder()
			.setResource(resource)
			.registerMetricReader(inMemoryReader)
			.build();

		final MultiHostsConfigurationDto multiHostsConfigurationDto= MultiHostsConfigurationDto
			.builder()
			.extraLabels(Map.of("site", "data center 1"))
			.build();
		
		final Monitor enclosure = Monitor
			.builder()
			.id("id_enclosure")
			.name("enclosure 1")
			.parentId("host")
			.monitorType(MonitorType.ENCLOSURE)
			.build();

		enclosure.addMetadata(FQDN, HOSTNAME);
		enclosure.addMetadata(DEVICE_ID, "1");
		enclosure.addMetadata(SERIAL_NUMBER, "SN 1");
		enclosure.addMetadata(VENDOR, "Dell");
		enclosure.addMetadata(MODEL, "PowerEdge T30");
		enclosure.addMetadata(BIOS_VERSION, "v1.1");
		enclosure.addMetadata(TYPE, "Server");
		enclosure.addMetadata(IDENTIFYING_INFORMATION, "Server 1 - Dell");
		enclosure.addMetadata(IP_ADDRESS, "192.168.1.1");

		OtelParameterToMetricObserver
			.builder()
			.monitor(enclosure)
			.sdkMeterProvider(meterProvider)
			.multiHostsConfigurationDto(multiHostsConfigurationDto)
			.metricInfoList(MetricsMapping.getMetricInfoList(MonitorType.ENCLOSURE, parameter.getName()).get())
			.matrixParameterName(parameter.getName())
			.build()
			.init();

		// This will trigger the observe callback
		Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();

		// The parameter is not collected yet
		assertTrue(metrics.isEmpty());

		enclosure.collectParameter(parameter);

		// Trigger the observe callback
		metrics = inMemoryReader.collectAllMetrics();

		// We should observe a new value
		assertEquals(1, metrics.size());
		final MetricData metricData = metrics.stream().findFirst().orElse(null);

		assertNotNull(metricData);
		assertNotNull(metricData.getUnit());
		assertNotNull(metricData.getDescription());
		assertEquals(expectedMetricName, metricData.getName());

		final  Collection<DoublePointData> points = metricData.getDoubleSumData().getPoints();

		final DoublePointData dataPoint = points.stream().findFirst().orElse(null);
		assertEquals(parameter.numberValue().doubleValue(), dataPoint.getValue());

		final Attributes expected = Attributes.builder()
			.put(MappingConstants.ID, "id_enclosure")
			.put(MappingConstants.NAME, "enclosure 1")
			.put(MappingConstants.PARENT, "host")
			.put("site", "data center 1")
			.put("device_id", "1")
			.put("serial_number", "SN 1")
			.put("vendor", "Dell")
			.put("model", "PowerEdge T30")
			.put("bios_version", "v1.1")
			.put("type", "Server")
			.put("info", "Server 1 - Dell")
			.put("ip_address", "192.168.1.1")
			.build();

		assertEquals(expected, dataPoint.getAttributes());
	}

	@Test
	void testInitObserverNoEnergy() {

		final NumberParam energyParameter = NumberParam
			.builder()
			.name(ENERGY_PARAMETER)
			.collectTime(COLLECT_TIME)
			.value(50000.0)
			.build();
		energyParameter.setPreviousCollectTime(COLLECT_TIME - 120000);
		energyParameter.setRawValue(50000.0);
		energyParameter.setPreviousRawValue(50000.0);

		final NumberParam powerParameter = NumberParam
			.builder()
			.name(POWER_CONSUMPTION_PARAMETER)
			.collectTime(COLLECT_TIME)
			.value(0.0)
			.build();
		powerParameter.setPreviousCollectTime(COLLECT_TIME - 120000);
		powerParameter.setRawValue(0.0);
		powerParameter.setPreviousRawValue(120.0);

		final Monitor host = Monitor.builder().id(ID).name("host").build();

		host.addMetadata(FQDN, HOSTNAME);
		final Resource resource = OtelHelper.createHostResource(
			host.getId(),
			"host",
			HostType.LINUX,
			HOSTNAME,
			false,
			Collections.emptyMap(),
			Collections.emptyMap()
		);

		final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
		final SdkMeterProvider meterProvider = SdkMeterProvider.builder()
			.setResource(resource)
			.registerMetricReader(inMemoryReader)
			.build();

		final MultiHostsConfigurationDto multiHostsConfigurationDto= MultiHostsConfigurationDto
			.builder()
			.build();

		final Monitor enclosure = Monitor
			.builder()
			.id("id_enclosure")
			.name("enclosure 1")
			.parentId("host")
			.monitorType(MonitorType.ENCLOSURE)
			.build();

		enclosure.addMetadata(FQDN, HOSTNAME);
		enclosure.addMetadata(DEVICE_ID, "1");
		enclosure.addMetadata(SERIAL_NUMBER, "SN 1");
		enclosure.addMetadata(VENDOR, "Dell");
		enclosure.addMetadata(MODEL, "PowerEdge T30");
		enclosure.addMetadata(BIOS_VERSION, "v1.1");
		enclosure.addMetadata(TYPE, "Server");
		enclosure.addMetadata(IDENTIFYING_INFORMATION, "Server 1 - Dell");
		enclosure.addMetadata(IP_ADDRESS, "192.168.1.1");

		OtelParameterToMetricObserver
			.builder()
			.monitor(enclosure)
			.sdkMeterProvider(meterProvider)
			.multiHostsConfigurationDto(multiHostsConfigurationDto)
			.metricInfoList(MetricsMapping.getMetricInfoList(MonitorType.ENCLOSURE, energyParameter.getName()).get())
			.matrixParameterName(energyParameter.getName())
			.build()
			.init();

		OtelParameterToMetricObserver
			.builder()
			.monitor(enclosure)
			.sdkMeterProvider(meterProvider)
			.multiHostsConfigurationDto(multiHostsConfigurationDto)
			.metricInfoList(MetricsMapping.getMetricInfoList(MonitorType.ENCLOSURE, powerParameter.getName()).get())
			.matrixParameterName(powerParameter.getName())
			.build()
			.init();

		// This will trigger the observe callback
		Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();

		// The parameter is not collected yet
		assertTrue(metrics.isEmpty());

		enclosure.collectParameter(energyParameter);
		enclosure.collectParameter(powerParameter);

		// Trigger the observe callback
		metrics = inMemoryReader.collectAllMetrics();

		// We shouldn't observe a value
		assertTrue(metrics.isEmpty());

	}

	@Test
	void testIsParameterAvailable() {

		{
			final DiscreteParam statusParam = DiscreteParam
				.builder()
				.name(STATUS_PARAMETER)
				.state(Status.OK)
				.collectTime(COLLECT_TIME)
				.build();
			final Monitor monitor = Monitor
				.builder()
				.id(ID)
				.parentId(PARENT_ID)
				.name(LABEL_VALUE)
				.parameters(Map.of(STATUS_PARAMETER, statusParam))
				.build();
			assertTrue(OtelParameterToMetricObserver.isParameterAvailable(monitor, Enclosure.STATUS.getName()));
		}

		{

			final DiscreteParam statusParamNotAvailable = DiscreteParam
				.builder()
				.name(STATUS_PARAMETER)
				.state(Status.OK)
				.collectTime(COLLECT_TIME)
				.build();

			statusParamNotAvailable.setState(null);

			final Monitor monitor = Monitor
				.builder()
				.id(ID)
				.parentId(PARENT_ID)
				.name(LABEL_VALUE)
				.parameters(Map.of(STATUS_PARAMETER, statusParamNotAvailable))
				.build();
			assertFalse(OtelParameterToMetricObserver.isParameterAvailable(monitor, Enclosure.STATUS.getName()));

		}

		{
			final TextParam textParam = TextParam
				.builder()
				.name(TEST_REPORT_PARAMETER)
				.value("text")
				.collectTime(COLLECT_TIME)
				.build();
			final Monitor monitor = Monitor
				.builder()
				.id(ID)
				.parentId(PARENT_ID)
				.name(LABEL_VALUE)
				.parameters(Map.of(TEST_REPORT_PARAMETER, textParam))
				.build();
			assertFalse(OtelParameterToMetricObserver.isParameterAvailable(monitor, MetaConnector.TEST_REPORT.getName()));

		}

		{
			final DiscreteParam statusParam = DiscreteParam
				.builder()
				.name(STATUS_PARAMETER)
				.state(Status.OK)
				.build();
			statusParam.setCollectTime(COLLECT_TIME);
			statusParam.setPreviousCollectTime(COLLECT_TIME);
			final Monitor monitor = Monitor
				.builder()
				.id(ID)
				.parentId(PARENT_ID)
				.name(LABEL_VALUE)
				.parameters(Map.of(STATUS_PARAMETER, statusParam))
				.build();
			assertFalse(OtelParameterToMetricObserver.isParameterAvailable(monitor, Enclosure.STATUS.getName()));
		}
	}

	@Test
	void testCheckParameter() {
		{
			final Monitor monitor = Monitor.builder()
					.id(ID)
					.parentId(PARENT_ID)
					.name(LABEL_VALUE)
					.parameters(Map.of(ENERGY_USAGE_PARAMETER,
							NumberParam.builder().name(ENERGY_USAGE_PARAMETER)
							.value(3000D).build()))
					.build();
			assertTrue(OtelParameterToMetricObserver.checkParameter(monitor, ENERGY_USAGE_PARAMETER));
		}

		{
			final Monitor monitor = Monitor.builder()
					.id(ID)
					.parentId(PARENT_ID)
					.name(LABEL_VALUE)
					.parameters(Collections.emptyMap())
					.build();
			assertFalse(OtelParameterToMetricObserver.checkParameter(monitor, ENERGY_USAGE_PARAMETER));
		}

		{
			final Monitor monitor = Monitor.builder()
					.id(ID)
					.parentId(PARENT_ID)
					.name(LABEL_VALUE)
					.parameters(null)
					.build();
			assertFalse(OtelParameterToMetricObserver.checkParameter(monitor, ENERGY_USAGE_PARAMETER));
		}

		{
			assertFalse(OtelParameterToMetricObserver.checkParameter(null, ENERGY_USAGE_PARAMETER));
		}
	}

	@Test
	void testConvertValue() {
		assertEquals(18, OtelParameterToMetricObserver.convertValue("1.8", 10));
		assertThrows(IllegalArgumentException.class, () -> OtelParameterToMetricObserver.convertValue(null, 10));
	}

	@Test
	void testCreateAttributes() {

		final OtelParameterToMetricObserver parameterToMetricObserver = OtelParameterToMetricObserver
			.builder()
			.multiHostsConfigurationDto(MultiHostsConfigurationDto
					.builder()
					.extraLabels(Map.of("site", "data center 1"))
					.build())
			.build();

		final Monitor enclosure = Monitor
					.builder()
					.id("id_enclosure")
					.name("enclosure 1")
					.monitorType(MonitorType.ENCLOSURE)
					.parentId("host")
					.build();
		enclosure.addMetadata(FQDN, HOSTNAME);
		enclosure.addMetadata("serialNumber", "Serial1234");
		enclosure.addMetadata(IP_ADDRESS, "192.168.1.1");

		final MetricInfo metricInfo = MetricsMapping
			.getMetricInfoList(MonitorType.ENCLOSURE, STATUS_PARAMETER)
			.get()
			.stream()
			.filter(info -> info.getIdentifyingAttributes()
					.stream()
					.filter(id -> id.getValue().equals(MappingConstants.OK_ATTRIBUTE_VALUE))
					.findFirst()
					.isPresent())
			.findFirst()
			.orElseThrow();

		final Attributes actual = parameterToMetricObserver.createAttributes(metricInfo, enclosure);

		final Attributes expected = Attributes.builder()
				.put(MappingConstants.ID, "id_enclosure")
				.put(MappingConstants.NAME, "enclosure 1")
				.put(MappingConstants.PARENT, "host")
				.put("site", "data center 1")
				.put("device_id", "")
				.put("serial_number", "Serial1234")
				.put("vendor", "")
				.put("model", "")
				.put("bios_version", "")
				.put("type", "")
				.put("info", "")
				.put("ip_address", "192.168.1.1")
				.put(MappingConstants.STATE_ATTRIBUTE_KEY, MappingConstants.OK_ATTRIBUTE_VALUE)
				.put(MappingConstants.HW_TYPE_ATTRIBUTE_KEY, EnclosureMapping.HW_TYPE_ATTRIBUTE_VALUE)
				.build();

		assertEquals(expected, actual);
	}

	@Test
	void testConvertMetadataInfoValue() {
		final OtelParameterToMetricObserver parameterToMetricObserver = OtelParameterToMetricObserver.builder().build();
		{

			final Map<String, String> cpuMetadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			cpuMetadata.put(MAXIMUM_SPEED, "4");
			final Monitor monitor = Monitor
					.builder()
					.id(ID)
					.parentId(PARENT_ID)
					.name(LABEL_VALUE)
					.metadata(cpuMetadata)
					.monitorType(MonitorType.CPU)
					.build();

			assertEquals(Double.toString(4*1000000.0), parameterToMetricObserver.convertMetadataInfoValue(monitor, MAXIMUM_SPEED));
			assertTrue(parameterToMetricObserver.convertMetadataInfoValue(monitor, EMPTY).isEmpty());
			assertTrue(parameterToMetricObserver.convertMetadataInfoValue(monitor, null).isEmpty());

			final Map<String, String> cpuMetadataEmpty = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			cpuMetadataEmpty.put(MAXIMUM_SPEED, EMPTY);
			final Monitor monitorCpu = Monitor
					.builder()
					.id(ID)
					.parentId(PARENT_ID)
					.name(LABEL_VALUE)
					.metadata(cpuMetadataEmpty)
					.monitorType(MonitorType.CPU)
					.build();

			assertTrue(parameterToMetricObserver.convertMetadataInfoValue(monitorCpu, MAXIMUM_SPEED).isEmpty());

		}

		{
			final Monitor monitor = Monitor.builder()
					.id(ID)
					.parentId(PARENT_ID)
					.name(LABEL_VALUE)
					.metadata(Collections.emptyMap())
					.build();
			assertTrue(parameterToMetricObserver.convertMetadataInfoValue(monitor, MAXIMUM_SPEED).isEmpty());
		}

		{
			final Monitor monitor = Monitor.builder().id(ID).parentId(PARENT_ID).name(LABEL_VALUE)
					.metadata(null).build();
			assertTrue(parameterToMetricObserver.convertMetadataInfoValue(monitor, MAXIMUM_SPEED).isEmpty());
		}

		{
			assertTrue(parameterToMetricObserver.convertMetadataInfoValue(null, MAXIMUM_SPEED).isEmpty());
		}
	}

	@Test
	void testCheckAttributesMap() {
		final Map<String, String> emptyMap = Collections.emptyMap();

		assertThrows(IllegalStateException.class,
				() -> OtelParameterToMetricObserver.checkAttributesMap(MonitorType.ENCLOSURE, emptyMap));

		assertThrows(IllegalStateException.class,
				() -> OtelParameterToMetricObserver.checkAttributesMap(MonitorType.ENCLOSURE, null));

	}

	@Test
	void testCanParseDoubleValue() {

		assertFalse(OtelParameterToMetricObserver.canParseDoubleValue(null));
		assertFalse(OtelParameterToMetricObserver.canParseDoubleValue(""));
		assertFalse(OtelParameterToMetricObserver.canParseDoubleValue(" "));
		assertFalse(OtelParameterToMetricObserver.canParseDoubleValue("a"));
		assertTrue(OtelParameterToMetricObserver.canParseDoubleValue("8"));
		assertTrue(OtelParameterToMetricObserver.canParseDoubleValue("8 "));
		assertTrue(OtelParameterToMetricObserver.canParseDoubleValue("8.0"));
	}

	@Test
	void testDetermineMeterId() {
		{
			final MetricInfo metricInfo = MetricInfo
				.builder()
				.name("hw.monitor.metric")
				.identifyingAttribute(
					StaticIdentifyingAttribute
						.builder()
						.key("type")
						.value("value")
						.build()
				)
				.build();
			final Monitor monitor = Monitor
				.builder()
				.id("monitor1")
				.build();
			assertEquals("monitor1.hw.monitor.metric.type.value", OtelParameterToMetricObserver.determineMeterId(metricInfo, monitor));
		}

		{
			final MetricInfo metricInfo = MetricInfo
				.builder()
				.name("hw.monitor.metric")
				.build();
			final Monitor monitor = Monitor
				.builder()
				.id("monitor1")
				.build();
			assertEquals("monitor1.hw.monitor.metric", OtelParameterToMetricObserver.determineMeterId(metricInfo, monitor));
		}

		{
			final MetricInfo metricInfo = MetricInfo
				.builder()
				.name("hw.monitor.metric")
				.additionalId("1")
				.build();
			final Monitor monitor = Monitor
				.builder()
				.id("monitor1")
				.build();
			assertEquals("monitor1.hw.monitor.metric.1", OtelParameterToMetricObserver.determineMeterId(metricInfo, monitor));
		}
	}

	@Test
	void testIncreasedEnergyUsage() {
		final Monitor host = Monitor
				.builder()
				.id("hostname")
				.parentId(null)
				.hostId(HOSTNAME)
				.name(HOSTNAME)
				.monitorType(MonitorType.HOST)
				.metadata(Map.of(FQDN, HOSTNAME))
				.build();
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		hostMonitoring.addMonitor(host);

		// Energy is not collected
		assertFalse(OtelParameterToMetricObserver.increasedEnergyUsage(host));

		// Collect 1
		CollectHelper.collectEnergyUsageFromPower(host, COLLECT_TIME, 120D, HOSTNAME);

		// We need two collects to get the energy usage
		assertFalse(OtelParameterToMetricObserver.increasedEnergyUsage(host));

		// Collect 2
		hostMonitoring.saveParameters();
		CollectHelper.collectEnergyUsageFromPower(host, COLLECT_TIME + 120000, 120D, HOSTNAME);
		assertEquals(14400.0, host.getParameter(ENERGY_PARAMETER, NumberParam.class).getRawValue());
		// OK, now energy is available
		assertTrue(OtelParameterToMetricObserver.increasedEnergyUsage(host));

		// Collect 3 decreased energy
		hostMonitoring.saveParameters();
		CollectHelper.collectPowerFromEnergyUsage(host, COLLECT_TIME + 120000 * 2, 14300.0, HOSTNAME);
		// The energy is available but not increased
		assertFalse(OtelParameterToMetricObserver.increasedEnergyUsage(host));

		// Weird case
		host.getParameter(HardwareConstants.ENERGY_PARAMETER, NumberParam.class).setRawValue(null);
		assertFalse(OtelParameterToMetricObserver.increasedEnergyUsage(host));
	}
}