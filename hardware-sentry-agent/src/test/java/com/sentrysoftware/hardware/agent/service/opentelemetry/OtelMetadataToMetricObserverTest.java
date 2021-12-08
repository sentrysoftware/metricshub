package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.MetricsMapping.ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FQDN;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MAXIMUM_SPEED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDTO;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricReader;
import io.opentelemetry.sdk.resources.Resource;

class OtelMetadataToMetricObserverTest {

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

		final Monitor cpu = Monitor
				.builder()
				.id("id_cpu")
				.name("cpu 1")
				.parentId("host")
				.monitorType(MonitorType.CPU)
				.build();
		cpu.addMetadata(FQDN, "host.my.domain.net");

		OtelMetadataToMetricObserver
			.builder()
			.monitor(cpu)
			.sdkMeterProvider(meterProvider)
			.multiHostsConfigurationDTO(multiHostsConfigurationDTO)
			.metricInfo(MetricsMapping.getMetadataAsMetricInfo(MonitorType.CPU, MAXIMUM_SPEED).get())
			.matrixMetadata(MAXIMUM_SPEED)
			.build()
			.init();

		// Trigger the observe callback
		Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();

		assertTrue(metrics.isEmpty());

		cpu.addMetadata(MAXIMUM_SPEED, "5000");

		// Trigger the observe callback
		metrics = inMemoryReader.collectAllMetrics();

		assertEquals(1, metrics.size());
		final MetricData metricData = metrics.stream().findFirst().orElse(null);
		assertNotNull(metricData);
		assertNotNull(metricData.getUnit());
		assertNotNull(metricData.getDescription());

		final DoublePointData dataPoint = metricData.getDoubleGaugeData().getPoints().stream().findFirst().orElse(null);
		assertEquals(5000000000D, dataPoint.getValue());

		final Attributes expected = Attributes.builder()
				.put("id", "id_cpu")
				.put("label", "cpu 1")
				.put("fqdn", "host.my.domain.net")
				.put("parent", "host")
				.put("site", "Datacenter 1")
				.build();

		assertEquals(expected, dataPoint.getAttributes());
	}

	@Test
	void testCheckMetadata() {
		assertTrue(OtelMetadataToMetricObserver
				.checkMetadata(Monitor.builder().metadata(Map.of(MAXIMUM_SPEED, "5000")).build(), MAXIMUM_SPEED));
		assertFalse(OtelMetadataToMetricObserver.checkMetadata(null, MAXIMUM_SPEED));
		assertFalse(OtelMetadataToMetricObserver.checkMetadata(Monitor.builder().metadata(null).build(), MAXIMUM_SPEED));
		assertFalse(OtelMetadataToMetricObserver.checkMetadata(
				Monitor.builder().metadata(Map.of(MAXIMUM_SPEED, "Not-A-Number")).build(), MAXIMUM_SPEED));
	}
}