package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.sentrysoftware.matrix.common.helpers.LocalOSHandler;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.ILocalOS;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.ILocalOSVisitor;
import com.sentrysoftware.matrix.engine.target.TargetType;

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
					"host", TargetType.LINUX, "host.my.domain.net", false, emptyMap, emptyMap));
			assertThrows(IllegalArgumentException.class, () -> OtelHelper.createHostResource("id",
					null, TargetType.LINUX, "host.my.domain.net", false, emptyMap, emptyMap));
			assertThrows(IllegalArgumentException.class, () -> OtelHelper.createHostResource("id",
					"host", null, "host.my.domain.net", false, emptyMap, emptyMap));
			assertThrows(IllegalArgumentException.class, () -> OtelHelper.createHostResource("id",
					"host", TargetType.LINUX, null, false, emptyMap, emptyMap));
			assertThrows(IllegalArgumentException.class, () -> OtelHelper.createHostResource("id",
					"host", TargetType.LINUX, "host.my.domain.net", false, null, emptyMap));
			assertThrows(IllegalArgumentException.class, () -> OtelHelper.createHostResource("id",
					"host", TargetType.LINUX, "host.my.domain.net", false, emptyMap, null));
			assertNotNull(OtelHelper.createHostResource("id", "host", TargetType.LINUX, "host.my.domain.net", false, emptyMap, emptyMap));
		}


		{
			final Resource actual = OtelHelper.createHostResource("id", "host", TargetType.LINUX, "host.my.domain.net", false,
					emptyMap, emptyMap);
			final Resource expected = Resource.create(Attributes.builder()
					.put("host.id", "id")
					.put("host.name", "host")
					.put("host.type", "linux")
					.put("os.type", "linux")
					.put("fqdn", "host.my.domain.net")
					.put("agent.host.name", OtelHelper.AGENT_HOSTNAME)
					.build());

			assertEquals(expected, actual);
		}

		{
			final Resource actual = OtelHelper.createHostResource("id", "host", TargetType.LINUX, "host.my.domain.net", true,
					emptyMap, emptyMap);
			final Resource expected = Resource.create(Attributes.builder()
					.put("host.id", "id")
					.put("host.name", "host.my.domain.net")
					.put("host.type", "linux")
					.put("os.type", "linux")
					.put("fqdn", "host.my.domain.net")
					.put("agent.host.name", OtelHelper.AGENT_HOSTNAME)
					.build());

			assertEquals(expected, actual);
		}

		{
			final Resource actual = OtelHelper.createHostResource("id", "host", TargetType.LINUX, "host.my.domain.net", true,
					Map.of("host.name", "host.my.domain"), emptyMap);
			final Resource expected = Resource.create(Attributes.builder()
					.put("host.id", "id")
					.put("host.name", "host.my.domain")
					.put("host.type", "linux")
					.put("os.type", "linux")
					.put("fqdn", "host.my.domain.net")
					.put("agent.host.name", OtelHelper.AGENT_HOSTNAME)
					.build());

			assertEquals(expected, actual);
		}

		{
			final Resource actual = OtelHelper.createHostResource("id", "host", TargetType.LINUX, "host.my.domain.net", true,
					Map.of(
							"host.name", "host.my.domain",
							"fqdn", "host-01.my.domain.com"
					),
					emptyMap);
			final Resource expected = Resource.create(Attributes.builder()
					.put("host.id", "id")
					.put("host.name", "host-01.my.domain.com")
					.put("host.type", "linux")
					.put("os.type", "linux")
					.put("fqdn", "host-01.my.domain.com")
					.put("agent.host.name", OtelHelper.AGENT_HOSTNAME)
					.build());

			assertEquals(expected, actual);
		}
		{
			final Resource actual = OtelHelper.createHostResource("id", "host", TargetType.LINUX, "host.my.domain.net", true,
					emptyMap,
					Map.of(
							"host.name", "should.n.t",
							"agent.host.name", "my.agent.host.name"
					));
			final Resource expected = Resource.create(Attributes.builder()
					.put("host.id", "id")
					.put("host.name", "host.my.domain.net")
					.put("host.type", "linux")
					.put("os.type", "linux")
					.put("fqdn", "host.my.domain.net")
					.put("agent.host.name", "my.agent.host.name")
					.build());

			assertEquals(expected, actual);
		}
	}

	@Test
	void testCreateServiceResource() {
		final Map<String, String> emptyMap = Collections.emptyMap();
		assertThrows(IllegalArgumentException.class, () -> OtelHelper.createServiceResource(null, emptyMap));
		assertThrows(IllegalArgumentException.class, () -> OtelHelper.createServiceResource("Hardware Sentry Agent", null));
		assertNotNull(OtelHelper.createServiceResource("Hardware Sentry Agent", emptyMap));

		try(MockedStatic<LocalOSHandler> localOSHandler = mockStatic(LocalOSHandler.class)){
			localOSHandler.when(() -> LocalOSHandler.getOS()).thenReturn(Optional.of(LocalOSHandler.LINUX));

			final Resource actual = OtelHelper.createServiceResource("Hardware Sentry Agent", Map.of(
					"agent.host.name", "my.agent.host.name"
			));

			final Resource expected = Resource.create(Attributes.builder()
					.put("service.name", "Hardware Sentry Agent")
					.put("host.type", "linux")
					.put("os.type", "linux")
					.put("agent.host.name", "my.agent.host.name")
					.put("host.name", OtelHelper.AGENT_HOSTNAME)
					.put("host.id", OtelHelper.AGENT_HOSTNAME)
					.build());
			assertEquals(expected, actual);
		}

		
	}

	@Test
	void testGetAgentOsType() {
		final Map<ILocalOS, String> expectedOSTypes = Map.of(
			LocalOSHandler.WINDOWS, "windows",
			LocalOSHandler.LINUX, "linux",
			LocalOSHandler.SUN, "sun",
			LocalOSHandler.HP, "hpux",
			LocalOSHandler.SOLARIS, "solaris",
			LocalOSHandler.AIX, "aix",
			LocalOSHandler.FREE_BSD, "freebsd",
			LocalOSHandler.OPEN_BSD, "openbsd",
			LocalOSHandler.NET_BSD, "netbsd",
			LocalOSHandler.MAC_OS_X, "macosx"
		);

		for (Entry<ILocalOS, String> entry : expectedOSTypes.entrySet()) {
			try(MockedStatic<LocalOSHandler> localOSHandler = mockStatic(LocalOSHandler.class)){
				localOSHandler.when(() -> LocalOSHandler.getOS()).thenReturn(Optional.of(entry.getKey()));
				assertEquals(entry.getValue(), OtelHelper.getAgentOsType());
			}
		}

	}

	@Test
	void testGetAgentOsTypeUnknown() {
		{
			final ILocalOS unknown = new ILocalOS() {
				@Override
				public void accept(ILocalOSVisitor visitor) {
					// Not implemented
				}
			};

			try(MockedStatic<LocalOSHandler> localOSHandler = mockStatic(LocalOSHandler.class)){
				localOSHandler.when(() -> LocalOSHandler.getOS()).thenReturn(Optional.of(unknown));
				assertEquals("unknown", OtelHelper.getAgentOsType());
			}
		}

		{
			try(MockedStatic<LocalOSHandler> localOSHandler = mockStatic(LocalOSHandler.class)){
				localOSHandler.when(() -> LocalOSHandler.getOS()).thenReturn(Optional.empty());
				assertEquals("unknown", OtelHelper.getAgentOsType());
			}
		}
	}
}
