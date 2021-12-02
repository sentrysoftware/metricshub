package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.MetricsMapping.ID;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.MetricsMapping.LABEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FQDN;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MODEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SERIAL_NUMBER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VENDOR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDTO;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricReader;
import io.opentelemetry.sdk.resources.Resource;

class OtelMetadataObserverTest {

	private static final String LABEL_VALUE = "monitor";
	private static final String PARENT_ID_VALUE = "parent_id";
	private static final String ID_VALUE = ID;
	private static final String MAXIMUM_SPEED = "maximumSpeed";

	@Test
	void testInit() {
		final Monitor target = Monitor.builder().id(ID).name("host").build();
		target.addMetadata(FQDN, "host.my.domain.net");
		final Resource resource = OtelHelper.createHostResource(target, "host");

		final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
		final SdkMeterProvider meterProvider = SdkMeterProvider.builder()
				.setResource(resource)
				.registerMetricReader(inMemoryReader)
				.buildAndRegisterGlobal();

		final MultiHostsConfigurationDTO multiHostsConfigurationDTO= MultiHostsConfigurationDTO
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
		enclosure.addMetadata(SERIAL_NUMBER, "Serial1234");
		enclosure.addMetadata(DEVICE_ID, "1");
		enclosure.addMetadata(VENDOR, "Dell");
		enclosure.addMetadata(MODEL, "FA1000");

		OtelMetadataObserver
			.builder()
			.monitor(enclosure)
			.sdkMeterProvider(meterProvider)
			.multiHostsConfigurationDTO(multiHostsConfigurationDTO)
			.metricInfo(MetricsMapping.getMetricInfoForMonitorType(MonitorType.ENCLOSURE))
			.build()
			.init();

		// Trigger the observe callback
		final Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();

		assertEquals(1, metrics.size());
		final MetricData metricData = metrics.stream().findFirst().orElse(null);
		assertNotNull(metricData);
		assertNotNull(metricData.getUnit());
		assertNotNull(metricData.getDescription());

		final  LongPointData dataPoint = metricData.getLongGaugeData().getPoints().stream().findFirst().orElse(null);
		assertEquals(1, dataPoint.getValue());

		final Attributes expected = Attributes.builder()
				.put("id", "id_enclosure")
				.put("label", "enclosure 1")
				.put("fqdn", "host.my.domain.net")
				.put("parent", "host")
				.put("serial_number", "Serial1234")
				.put("site", "Datacenter 1")
				.put("device_id", "1")
				.put("vendor", "Dell")
				.put("model", "FA1000")
				.put("bios_version", EMPTY)
				.put("type", EMPTY)
				.put("identifying_information", EMPTY)
				.build();

		assertEquals(expected, dataPoint.getAttributes());
	}

	@Test
	void testCreateAttributes() {

		final OtelMetadataObserver metadataObserver = OtelMetadataObserver
			.builder()
			.multiHostsConfigurationDTO(MultiHostsConfigurationDTO
					.builder()
					.extraLabels(Map.of("site", "Datacenter 1"))
					.build())
			.build();

		final Monitor enclosure = Monitor
					.builder()
					.id("id_enclosure")
					.name("enclosure 1")
					.monitorType(MonitorType.ENCLOSURE)
					.build();
		enclosure.addMetadata(FQDN, "host.my.domain.net");
		enclosure.addMetadata("serialNumber", "Serial1234");

		final Attributes actual = metadataObserver.createAttributes(enclosure,
				Set.of(ID, LABEL, FQDN, "size", "serial_number", "site").stream());

		final Attributes expected = Attributes.builder()
				.put(ID, "id_enclosure")
				.put(LABEL, "enclosure 1")
				.put(FQDN, "host.my.domain.net")
				.put("serial_number", "Serial1234")
				.put("site", "Datacenter 1")
				.put("size", EMPTY)
				.build();

		assertEquals(expected, actual);
	}

	@Test
	void testConvertMetadataInfoValue() {
		final OtelMetadataObserver metadataObserver = OtelMetadataObserver.builder().build();
		{

			final Map<String, String> cpuMetadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			cpuMetadata.put(MAXIMUM_SPEED, "4");
			final Monitor monitor = Monitor
					.builder()
					.id(ID_VALUE)
					.parentId(PARENT_ID_VALUE)
					.name(LABEL_VALUE)
					.metadata(cpuMetadata)
					.monitorType(MonitorType.CPU)
					.build();

			assertEquals(Double.toString(4*1000000.0), metadataObserver.convertMetadataInfoValue(monitor, MAXIMUM_SPEED));
			assertTrue(metadataObserver.convertMetadataInfoValue(monitor, EMPTY).isEmpty());
			assertTrue(metadataObserver.convertMetadataInfoValue(monitor, null).isEmpty());

			final Map<String, String> cpuMetadataEmpty = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
			cpuMetadataEmpty.put(MAXIMUM_SPEED, EMPTY);
			final Monitor monitorCpu = Monitor
					.builder()
					.id(ID_VALUE)
					.parentId(PARENT_ID_VALUE)
					.name(LABEL_VALUE)
					.metadata(cpuMetadataEmpty)
					.monitorType(MonitorType.CPU)
					.build();

			assertTrue(metadataObserver.convertMetadataInfoValue(monitorCpu, MAXIMUM_SPEED).isEmpty());

		}

		{
			final Monitor monitor = Monitor.builder()
					.id(ID_VALUE)
					.parentId(PARENT_ID_VALUE)
					.name(LABEL_VALUE)
					.metadata(Collections.emptyMap())
					.build();
			assertTrue(metadataObserver.convertMetadataInfoValue(monitor, MAXIMUM_SPEED).isEmpty());
		}

		{
			final Monitor monitor = Monitor.builder().id(ID_VALUE).parentId(PARENT_ID_VALUE).name(LABEL_VALUE)
					.metadata(null).build();
			assertTrue(metadataObserver.convertMetadataInfoValue(monitor, MAXIMUM_SPEED).isEmpty());
		}

		{
			assertTrue(metadataObserver.convertMetadataInfoValue(null, MAXIMUM_SPEED).isEmpty());
		}
	}

	@Test
	void testCheckStaticAttributes() {
		final Set<String> emptySet = Collections.emptySet();

		assertThrows(IllegalStateException.class,
				() -> OtelMetadataObserver.checkStaticAttributes(MonitorType.ENCLOSURE, emptySet));

		assertThrows(IllegalStateException.class,
				() -> OtelMetadataObserver.checkStaticAttributes(MonitorType.ENCLOSURE, null));

	}

	@Test
	void testObserveNoMonitorType() {
		final Monitor monitor = Monitor.builder().build();
		final ObservableLongMeasurement recorder = new ObservableLongMeasurement() {
			@Override
			public void observe(long value, Attributes attributes) {
			}

			@Override
			public void observe(long value) {
			}
		};

		final OtelMetadataObserver observer = OtelMetadataObserver.builder().build();
		assertThrows(IllegalArgumentException.class, () -> observer.observe(monitor, recorder));
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
