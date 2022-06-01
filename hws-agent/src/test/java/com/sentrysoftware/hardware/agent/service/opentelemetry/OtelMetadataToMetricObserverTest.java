package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FQDN;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;
import com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MetricsMapping;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;

import com.sentrysoftware.matrix.engine.host.HostType;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;

class OtelMetadataToMetricObserverTest {

	@Test
	void testInit() {

		final Monitor host = Monitor.builder().id(ID).name("host").build();
		host.addMetadata(FQDN, "host.my.domain.net");
		final Resource resource = OtelHelper.createHostResource(host.getId(),
				"host", HostType.LINUX, "host.my.domain.net", false, Collections.emptyMap(), Collections.emptyMap());

		final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
		final SdkMeterProvider meterProvider = SdkMeterProvider.builder()
				.setResource(resource)
				.registerMetricReader(inMemoryReader)
				.build();

		final MultiHostsConfigurationDto multiHostsConfigurationDto= MultiHostsConfigurationDto
				.builder()
				.extraLabels(Map.of("site", "Datacenter 1"))
				.build();

		final Monitor physicalDisk = Monitor
				.builder()
				.id("id_disk")
				.name("disk 1")
				.parentId("host")
				.monitorType(MonitorType.PHYSICAL_DISK)
				.build();
		physicalDisk.addMetadata(FQDN, "host.my.domain.net");

		OtelMetadataToMetricObserver
			.builder()
			.monitor(physicalDisk)
			.sdkMeterProvider(meterProvider)
			.multiHostsConfigurationDto(multiHostsConfigurationDto)
			.metricInfoList(MetricsMapping.getMetadataAsMetricInfoList(MonitorType.PHYSICAL_DISK, SIZE).get())
			.matrixMetadata(SIZE)
			.build()
			.init();

		// Trigger the observe callback
		Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();

		assertTrue(metrics.isEmpty());

		physicalDisk.addMetadata(SIZE, "500000000000");

		// Trigger the observe callback
		metrics = inMemoryReader.collectAllMetrics();

		assertEquals(1, metrics.size());
		final MetricData metricData = metrics.stream().findFirst().orElse(null);
		assertNotNull(metricData);
		assertNotNull(metricData.getUnit());
		assertNotNull(metricData.getDescription());

		final DoublePointData dataPoint = metricData.getDoubleGaugeData().getPoints().stream().findFirst().orElse(null);
		assertEquals(5e11, dataPoint.getValue());

		final Attributes actual = dataPoint.getAttributes();
		assertEquals("id_disk", actual.get(AttributeKey.stringKey("id")));
		assertEquals("disk 1", actual.get(AttributeKey.stringKey("name")));
		assertNull(actual.get(AttributeKey.stringKey("fqdn")));
		assertEquals("host", actual.get(AttributeKey.stringKey("parent")));
		assertEquals("Datacenter 1", actual.get(AttributeKey.stringKey("site")));
		assertTrue(actual.get(AttributeKey.stringKey("device_id")).isEmpty());
		assertTrue(actual.get(AttributeKey.stringKey("identifying_information")).isEmpty());
		assertTrue(actual.get(AttributeKey.stringKey("vendor")).isEmpty());
		assertTrue(actual.get(AttributeKey.stringKey("model")).isEmpty());
		assertNull(actual.get(AttributeKey.stringKey("size"))); // Already mapped as metric


	}

	@Test
	void testCheckMetadata() {
		assertTrue(OtelMetadataToMetricObserver
				.checkMetadata(Monitor.builder().metadata(Map.of(SIZE, "500000000000")).build(), SIZE));
		assertFalse(OtelMetadataToMetricObserver.checkMetadata(null, SIZE));
		assertFalse(OtelMetadataToMetricObserver.checkMetadata(Monitor.builder().metadata(null).build(), SIZE));
		assertFalse(OtelMetadataToMetricObserver.checkMetadata(
				Monitor.builder().metadata(Map.of(SIZE, "Not-A-Number")).build(), SIZE));
	}
}
