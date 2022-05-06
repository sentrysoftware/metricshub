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

import com.sentrysoftware.matrix.common.helpers.LocalOsHandler;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler.ILocalOS;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler.ILocalOSVisitor;
import com.sentrysoftware.matrix.engine.target.TargetType;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;

class OtelHelperTest {

	@Test
	void testInitOpenTelemetrySdk() {
		Map<String, String> emptyMap = Collections.emptyMap();
		final Resource resource = Resource.create(Attributes.empty());
		assertNotNull(OtelHelper.initOpenTelemetrySdk(resource, emptyMap));
		assertThrows(IllegalArgumentException.class, () -> OtelHelper.initOpenTelemetrySdk(null, emptyMap));
		assertThrows(IllegalArgumentException.class, () -> OtelHelper.initOpenTelemetrySdk(resource, null));
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
					.put("host.type", OtelHelper.HOST_TYPE_COMPUTE)
					.put("os.type", "linux")
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
					.put("host.type", OtelHelper.HOST_TYPE_COMPUTE)
					.put("os.type", "linux")
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
					.put("host.type", OtelHelper.HOST_TYPE_COMPUTE)
					.put("os.type", "linux")
					.put("agent.host.name", OtelHelper.AGENT_HOSTNAME)
					.build());

			assertEquals(expected, actual);
		}

		{
			final Resource actual = OtelHelper.createHostResource("id", "host", TargetType.LINUX, "host.my.domain.net", true,
					Map.of(
							"host.name", "host.my.domain",
							"fqdn", "host-01.my.domain.com" // The user added a new extra label: fqdn.
					),
					emptyMap);
			final Resource expected = Resource.create(Attributes.builder()
					.put("host.id", "id")
					.put("host.name", "host-01.my.domain.com")
					.put("host.type", OtelHelper.HOST_TYPE_COMPUTE)
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
					.put("host.type", OtelHelper.HOST_TYPE_COMPUTE)
					.put("os.type", "linux")
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

		try(MockedStatic<LocalOsHandler> localOsHandler = mockStatic(LocalOsHandler.class)){
			localOsHandler.when(() -> LocalOsHandler.getOS()).thenReturn(Optional.of(LocalOsHandler.LINUX));

			final Resource actual = OtelHelper.createServiceResource("Hardware Sentry Agent", Map.of(
					"agent.host.name", "my.agent.host.name"
			));

			final Resource expected = Resource.create(Attributes.builder()
					.put("service.name", "Hardware Sentry Agent")
					.put("host.type", OtelHelper.HOST_TYPE_COMPUTE)
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
			LocalOsHandler.WINDOWS, "windows",
			LocalOsHandler.LINUX, "linux",
			LocalOsHandler.SUN, "sun",
			LocalOsHandler.HP, "hpux",
			LocalOsHandler.SOLARIS, "solaris",
			LocalOsHandler.AIX, "aix",
			LocalOsHandler.FREE_BSD, "freebsd",
			LocalOsHandler.OPEN_BSD, "openbsd",
			LocalOsHandler.NET_BSD, "netbsd",
			LocalOsHandler.MAC_OS_X, "macosx"
		);

		for (Entry<ILocalOS, String> entry : expectedOSTypes.entrySet()) {
			try(MockedStatic<LocalOsHandler> localOsHandler = mockStatic(LocalOsHandler.class)){
				localOsHandler.when(() -> LocalOsHandler.getOS()).thenReturn(Optional.of(entry.getKey()));
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

			try(MockedStatic<LocalOsHandler> localOsHandler = mockStatic(LocalOsHandler.class)){
				localOsHandler.when(() -> LocalOsHandler.getOS()).thenReturn(Optional.of(unknown));
				assertEquals("unknown", OtelHelper.getAgentOsType());
			}
		}

		{
			try(MockedStatic<LocalOsHandler> localOsHandler = mockStatic(LocalOsHandler.class)){
				localOsHandler.when(() -> LocalOsHandler.getOS()).thenReturn(Optional.empty());
				assertEquals("unknown", OtelHelper.getAgentOsType());
			}
		}
	}
}
