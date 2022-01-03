package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;

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

		Map<String, String> emptyMap = Collections.emptyMap();

		{
			assertThrows(IllegalArgumentException.class, () -> OtelHelper.createHostResource(null,
					"host", "Linux", "host.my.domain.net", false, emptyMap));
			assertThrows(IllegalArgumentException.class, () -> OtelHelper.createHostResource("id",
					null, "Linux", "host.my.domain.net", false, emptyMap));
			assertThrows(IllegalArgumentException.class, () -> OtelHelper.createHostResource("id",
					"host", null, "host.my.domain.net", false, emptyMap));
			assertThrows(IllegalArgumentException.class, () -> OtelHelper.createHostResource("id",
					"host", "Linux", null, false, emptyMap));
			assertThrows(IllegalArgumentException.class, () -> OtelHelper.createHostResource("id",
					"host", "Linux", "host.my.domain.net", false, null));
			assertNotNull(OtelHelper.createHostResource("id", "host", "Linux", "host.my.domain.net", false, emptyMap));
		}


		{
			final Resource actual = OtelHelper.createHostResource("id", "host", "Linux", "host.my.domain.net", false,
					emptyMap);
			final Resource expected = Resource.create(Attributes.builder()
					.put("host.id", "id")
					.put("host.name", "host")
					.put("host.type", "Linux")
					.put("fqdn", "host.my.domain.net")
					.build());

			assertEquals(expected, actual);
		}

		{
			final Resource actual = OtelHelper.createHostResource("id", "host", "Linux", "host.my.domain.net", true,
					emptyMap);
			final Resource expected = Resource.create(Attributes.builder()
					.put("host.id", "id")
					.put("host.name", "host.my.domain.net")
					.put("host.type", "Linux")
					.put("fqdn", "host.my.domain.net")
					.build());

			assertEquals(expected, actual);
		}

		{
			final Resource actual = OtelHelper.createHostResource("id", "host", "Linux", "host.my.domain.net", true,
					Map.of("host.name", "host.my.domain"));
			final Resource expected = Resource.create(Attributes.builder()
					.put("host.id", "id")
					.put("host.name", "host.my.domain")
					.put("host.type", "Linux")
					.put("fqdn", "host.my.domain.net")
					.build());

			assertEquals(expected, actual);
		}

		{
			final Resource actual = OtelHelper.createHostResource("id", "host", "Linux", "host.my.domain.net", true,
					Map.of(
							"host.name", "host.my.domain",
							"fqdn", "host-01.my.domain.com"
					));
			final Resource expected = Resource.create(Attributes.builder()
					.put("host.id", "id")
					.put("host.name", "host-01.my.domain.com")
					.put("host.type", "Linux")
					.put("fqdn", "host-01.my.domain.com")
					.build());

			assertEquals(expected, actual);
		}
	}

	@Test
	void testCreateServiceResource() {
		assertThrows(IllegalArgumentException.class, () -> OtelHelper.createServiceResource(null));
		assertNotNull(OtelHelper.createServiceResource("Hardware Sentry Agent"));
	}

}
