package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.model.monitor.Monitor;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricReader;
import io.opentelemetry.sdk.resources.Resource;

class OtelHelperTest {

	@Test
	void testInitOpenTelemetryMetrics() {
		final InMemoryMetricReader metricReader = InMemoryMetricReader.create();
		final Resource resource = Resource.create(Attributes.empty());
		assertNotNull(OtelHelper.initOpenTelemetryMetrics(resource, metricReader));
		assertThrows(IllegalArgumentException.class, () -> OtelHelper.initOpenTelemetryMetrics(null, metricReader));
		assertThrows(IllegalArgumentException.class, () -> OtelHelper.initOpenTelemetryMetrics(resource, null));
	}

	@Test
	void testCreateHostResource() {

		final Monitor target = Monitor.builder().id("id").name("host").build();
		target.addMetadata("fqdn", "host.my.domain.net");
		assertThrows(IllegalArgumentException.class, () -> OtelHelper.createHostResource(null, "host"));
		assertThrows(IllegalArgumentException.class, () -> OtelHelper.createHostResource(target, null));
		assertNotNull(OtelHelper.createHostResource(target, "host"));
	}

	@Test
	void testCreateServiceResource() {
		assertThrows(IllegalArgumentException.class, () -> OtelHelper.createServiceResource(null));
		assertNotNull(OtelHelper.createServiceResource("Hardware Sentry Agent"));
	}

}
