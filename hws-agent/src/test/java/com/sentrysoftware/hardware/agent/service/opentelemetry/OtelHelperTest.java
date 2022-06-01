package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.JOULES_UNIT;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.OK_ATTRIBUTE_VALUE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.OK_STATUS_PREDICATE;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.STATE_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.sentrysoftware.hardware.agent.dto.metric.DynamicIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.dto.metric.IIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.metric.StaticIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo.MetricType;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler.ILocalOs;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler.ILocalOsVisitor;
import com.sentrysoftware.matrix.common.meta.parameter.state.Status;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectHelper;
import com.sentrysoftware.matrix.engine.host.HostType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring.PowerMeter;

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
					"host", HostType.LINUX, "host.my.domain.net", false, emptyMap, emptyMap));
			assertThrows(IllegalArgumentException.class, () -> OtelHelper.createHostResource("id",
					null, HostType.LINUX, "host.my.domain.net", false, emptyMap, emptyMap));
			assertThrows(IllegalArgumentException.class, () -> OtelHelper.createHostResource("id",
					"host", null, "host.my.domain.net", false, emptyMap, emptyMap));
			assertThrows(IllegalArgumentException.class, () -> OtelHelper.createHostResource("id",
					"host", HostType.LINUX, null, false, emptyMap, emptyMap));
			assertThrows(IllegalArgumentException.class, () -> OtelHelper.createHostResource("id",
					"host", HostType.LINUX, "host.my.domain.net", false, null, emptyMap));
			assertThrows(IllegalArgumentException.class, () -> OtelHelper.createHostResource("id",
					"host", HostType.LINUX, "host.my.domain.net", false, emptyMap, null));
			assertNotNull(OtelHelper.createHostResource("id", "host", HostType.LINUX, "host.my.domain.net", false, emptyMap, emptyMap));
		}


		{
			final Resource actual = OtelHelper.createHostResource("id", "host", HostType.LINUX, "host.my.domain.net", false,
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
			final Resource actual = OtelHelper.createHostResource("id", "host", HostType.LINUX, "host.my.domain.net", true,
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
			final Resource actual = OtelHelper.createHostResource("id", "host", HostType.LINUX, "host.my.domain.net", true,
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
			final Resource actual = OtelHelper.createHostResource("id", "host", HostType.LINUX, "host.my.domain.net", true,
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
			final Resource actual = OtelHelper.createHostResource("id", "host", HostType.LINUX, "host.my.domain.net", true,
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
			localOsHandler.when(() -> LocalOsHandler.getOs()).thenReturn(Optional.of(LocalOsHandler.LINUX));

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
		final Map<ILocalOs, String> expectedOSTypes = Map.of(
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

		for (Entry<ILocalOs, String> entry : expectedOSTypes.entrySet()) {
			try(MockedStatic<LocalOsHandler> localOsHandler = mockStatic(LocalOsHandler.class)){
				localOsHandler.when(() -> LocalOsHandler.getOs()).thenReturn(Optional.of(entry.getKey()));
				assertEquals(entry.getValue(), OtelHelper.getAgentOsType());
			}
		}

	}

	@Test
	void testGetAgentOsTypeUnknown() {
		{
			final ILocalOs unknown = new ILocalOs() {
				@Override
				public void accept(ILocalOsVisitor visitor) {
					// Not implemented
				}
			};

			try(MockedStatic<LocalOsHandler> localOsHandler = mockStatic(LocalOsHandler.class)){
				localOsHandler.when(() -> LocalOsHandler.getOs()).thenReturn(Optional.of(unknown));
				assertEquals("unknown", OtelHelper.getAgentOsType());
			}
		}

		{
			try(MockedStatic<LocalOsHandler> localOsHandler = mockStatic(LocalOsHandler.class)){
				localOsHandler.when(() -> LocalOsHandler.getOs()).thenReturn(Optional.empty());
				assertEquals("unknown", OtelHelper.getAgentOsType());
			}
		}
	}

	@Test
	void testExtractIdentifyingAttribute() {
		{
			final MetricInfo metricInfo = MetricInfo
				.builder()
				.name("hw.battery.status")
				.description("Whether the battery status is ok or not.")
				.identifyingAttribute(
					StaticIdentifyingAttribute
						.builder()
						.key(STATE_ATTRIBUTE_KEY)
						.value(OK_ATTRIBUTE_VALUE)
						.build()
				)
				.predicate(OK_STATUS_PREDICATE)
				.build();
			final Monitor monitor = Monitor.builder().build();
			CollectHelper.updateDiscreteParameter(monitor, STATUS_PARAMETER, new Date().getTime(), Status.OK);
			assertEquals(Set.of(new String[] {STATE_ATTRIBUTE_KEY, OK_ATTRIBUTE_VALUE}), Set.of(OtelHelper.extractIdentifyingAttribute(metricInfo, monitor).get()));
		}

		{
			final MetricInfo metricInfo = MetricInfo
				.builder()
				.name("hw.host.energy")
				.unit(JOULES_UNIT)
				.type(MetricType.COUNTER)
				.description("Energy consumed by the components since the start of the Hardware Sentry agent.")
				.identifyingAttribute(
					DynamicIdentifyingAttribute
						.builder()
						.key("quality")
						.value(HardwareConstants.POWER_METER)
						.build()
				)
				.build();
			final Monitor monitor = Monitor.builder().build();
			monitor.addMetadata(POWER_METER, PowerMeter.ESTIMATED.name());
			assertEquals(Set.of(new String[] {"quality", "estimated"}), Set.of(OtelHelper.extractIdentifyingAttribute(metricInfo, monitor).get()));
		}

		{
			final MetricInfo metricInfo = MetricInfo
				.builder()
				.name("hw.battery.charge")
				.factor(0.01)
				.unit("1")
				.description("Battery charge ratio.")
				.build();
			assertTrue(OtelHelper.extractIdentifyingAttribute(metricInfo, Monitor.builder().build()).isEmpty());
		}

		{
			final MetricInfo metricInfo = MetricInfo
					.builder()
					.name("hw.battery.some.metric")
					.factor(0.01)
					.unit("1")
					.identifyingAttribute(new IIdentifyingAttribute() {
						
						@Override
						public String getValue() {
							return "";
						}
						
						@Override
						public String getKey() {
							return "";
						}
					})
					.build();
			final Monitor monitor = Monitor.builder().build();
			assertThrows(IllegalStateException.class, () -> OtelHelper.extractIdentifyingAttribute(metricInfo, monitor));
		}
	}

}
