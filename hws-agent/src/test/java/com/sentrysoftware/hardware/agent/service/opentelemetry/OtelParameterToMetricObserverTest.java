package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BIOS_VERSION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER;
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
import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants;
import com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MetricsMapping;
import com.sentrysoftware.matrix.common.meta.monitor.Enclosure;
import com.sentrysoftware.matrix.common.meta.monitor.MetaConnector;
import com.sentrysoftware.matrix.common.meta.parameter.state.Status;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.TextParam;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;

class OtelParameterToMetricObserverTest {

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

		final String expectedMetricName = "hw.enclosure.status";

		final String expectedState = MappingConstants.OK_ATTRIBUTE_VALUE;

		final Monitor target = Monitor.builder().id(ID).name("host").build();
		target.addMetadata(FQDN, "host.my.domain.net");

		final Resource resource = OtelHelper.createHostResource(
			target.getId(),
			"host",
			TargetType.LINUX,
			"host.my.domain.net",
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
			.extraLabels(Map.of("site", "Datacenter 1"))
			.build();

		final Monitor enclosure = Monitor
			.builder()
			.id("id_enclosure")
			.name("enclosure 1")
			.parentId("host")
			.monitorType(MonitorType.ENCLOSURE)
			.build();

		enclosure.addMetadata(FQDN, "host.my.domain.net");
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
							.getDoubleGaugeData()
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

			final  Collection<DoublePointData> points = metricData.getDoubleGaugeData().getPoints();

			final DoublePointData dataPoint = points.stream().findFirst().orElse(null);
			assertEquals(expectedValue, dataPoint.getValue());

			final Attributes expected = Attributes.builder()
				.put(MappingConstants.ID, "id_enclosure")
				.put(MappingConstants.NAME, "enclosure 1")
				.put(MappingConstants.PARENT, "host")
				.put("site", "Datacenter 1")
				.put("device_id", "1")
				.put("serial_number", "SN 1")
				.put("vendor", "Dell")
				.put("model", "PowerEdge T30")
				.put("bios_version", "v1.1")
				.put("type", "Server")
				.put("identifying_information", "Server 1 - Dell")
				.put("ip_address", "192.168.1.1")
				.put(MappingConstants.STATE_ATTRIBUTE_KEY, expectedValue == 1 ? expectedState : key)
				.build();

			assertEquals(expected, dataPoint.getAttributes());
		}

	}

	@Test
	void testInitObserverOnEnergy() {

		final NumberParam parameter = NumberParam
			.builder()
			.name(ENERGY_PARAMETER)
			.collectTime(new Date().getTime())
			.value(50000.0)
			.build();

		final String expectedMetricName = "hw.enclosure.energy";

		final Monitor target = Monitor.builder().id(ID).name("host").build();

		target.addMetadata(FQDN, "host.my.domain.net");
		final Resource resource = OtelHelper.createHostResource(
			target.getId(),
			"host",
			TargetType.LINUX,
			"host.my.domain.net",
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
			.extraLabels(Map.of("site", "Datacenter 1"))
			.build();
		
		final Monitor enclosure = Monitor
			.builder()
			.id("id_enclosure")
			.name("enclosure 1")
			.parentId("host")
			.monitorType(MonitorType.ENCLOSURE)
			.build();

		enclosure.addMetadata(FQDN, "host.my.domain.net");
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
			.put("site", "Datacenter 1")
			.put("device_id", "1")
			.put("serial_number", "SN 1")
			.put("vendor", "Dell")
			.put("model", "PowerEdge T30")
			.put("bios_version", "v1.1")
			.put("type", "Server")
			.put("identifying_information", "Server 1 - Dell")
			.put("ip_address", "192.168.1.1")
			.build();

		assertEquals(expected, dataPoint.getAttributes());
	}

	@Test
	void testIsParameterAvailable() {

		{
			final DiscreteParam statusParam = DiscreteParam.builder().name(STATUS_PARAMETER).state(Status.OK).build();
			final Monitor monitor = Monitor.builder()
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
					.build();

			statusParamNotAvailable.setState(null);

			final Monitor monitor = Monitor.builder()
						.id(ID)
						.parentId(PARENT_ID)
						.name(LABEL_VALUE)
						.parameters(Map.of(STATUS_PARAMETER, statusParamNotAvailable))
						.build();
			assertFalse(OtelParameterToMetricObserver.isParameterAvailable(monitor, Enclosure.STATUS.getName()));

		}

		{
			final TextParam textParam = TextParam.builder().name(TEST_REPORT_PARAMETER).value("text").build();
			final Monitor monitor = Monitor.builder()
						.id(ID)
						.parentId(PARENT_ID)
						.name(LABEL_VALUE)
						.parameters(Map.of(TEST_REPORT_PARAMETER, textParam))
						.build();
			assertFalse(OtelParameterToMetricObserver.isParameterAvailable(monitor, MetaConnector.TEST_REPORT.getName()));

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
					.extraLabels(Map.of("site", "Datacenter 1"))
					.build())
			.build();

		final Monitor enclosure = Monitor
					.builder()
					.id("id_enclosure")
					.name("enclosure 1")
					.monitorType(MonitorType.ENCLOSURE)
					.parentId("host")
					.build();
		enclosure.addMetadata(FQDN, "host.my.domain.net");
		enclosure.addMetadata("serialNumber", "Serial1234");
		enclosure.addMetadata(IP_ADDRESS, "192.168.1.1");

		final MetricInfo metricInfo = MetricsMapping
			.getMetricInfoList(MonitorType.ENCLOSURE, STATUS_PARAMETER)
			.get()
			.stream()
			.filter(info -> info.getIdentifyingAttribute().getValue().equals(MappingConstants.OK_ATTRIBUTE_VALUE))
			.findFirst()
			.orElseThrow();

		final Attributes actual = parameterToMetricObserver.createAttributes(metricInfo, enclosure);

		final Attributes expected = Attributes.builder()
				.put(MappingConstants.ID, "id_enclosure")
				.put(MappingConstants.NAME, "enclosure 1")
				.put(MappingConstants.PARENT, "host")
				.put("site", "Datacenter 1")
				.put("device_id", "")
				.put("serial_number", "Serial1234")
				.put("vendor", "")
				.put("model", "")
				.put("bios_version", "")
				.put("type", "")
				.put("identifying_information", "")
				.put("ip_address", "192.168.1.1")
				.put(MappingConstants.STATE_ATTRIBUTE_KEY, MappingConstants.OK_ATTRIBUTE_VALUE)
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
}
