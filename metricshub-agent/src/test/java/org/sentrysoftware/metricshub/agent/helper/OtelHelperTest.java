package org.sentrysoftware.metricshub.agent.helper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.ATTRIBUTES;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.COMPANY_ATTRIBUTE_KEY;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.COMPANY_ATTRIBUTE_VALUE;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.COMPUTE_HOST_TYPE;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.HOST_TYPE_ATTRIBUTE_KEY;
import static org.sentrysoftware.metricshub.agent.helper.TestConstants.OS_TYPE_ATTRIBUTE_KEY;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.HOST_NAME;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;

class OtelHelperTest {

	private static final String HOSTNAME_ATTRIBUTE_VALUE = "OtelHelperTestHostname." + UUID.randomUUID().toString();

	@Test
	void testInitOpenTelemetrySdk() {
		Map<String, String> emptyMap = Map.of();
		final Resource resource = Resource.create(Attributes.empty());
		assertNotNull(OtelHelper.initOpenTelemetrySdk(resource, emptyMap));
		assertThrows(IllegalArgumentException.class, () -> OtelHelper.initOpenTelemetrySdk(null, emptyMap));
		assertThrows(IllegalArgumentException.class, () -> OtelHelper.initOpenTelemetrySdk(resource, null));
	}

	@Test
	void testCreateHostResource() {
		final Map<String, String> emptyMap = Map.of();
		{
			assertThrows(IllegalArgumentException.class, () -> OtelHelper.createHostResource(null, emptyMap));
			assertThrows(IllegalArgumentException.class, () -> OtelHelper.createHostResource(emptyMap, null));
			assertDoesNotThrow(() -> OtelHelper.createHostResource(emptyMap, emptyMap));
		}
		{
			final Resource resource = OtelHelper.createHostResource(
				Map.of(
					HOST_NAME,
					HOSTNAME_ATTRIBUTE_VALUE,
					HOST_TYPE_ATTRIBUTE_KEY,
					COMPUTE_HOST_TYPE,
					OS_TYPE_ATTRIBUTE_KEY,
					MetricsHubConstants.OTEL_LINUX_OS_TYPE
				),
				Map.of(HOST_NAME, "extraHostname", HOST_TYPE_ATTRIBUTE_KEY, "extraHostType")
			);
			final Resource expected = Resource.create(
				Attributes
					.builder()
					.put(HOST_NAME, HOSTNAME_ATTRIBUTE_VALUE)
					.put(HOST_TYPE_ATTRIBUTE_KEY, COMPUTE_HOST_TYPE)
					.put(OS_TYPE_ATTRIBUTE_KEY, MetricsHubConstants.OTEL_LINUX_OS_TYPE)
					.build()
			);

			assertEquals(expected, resource);
		}
		{
			final Resource resource = OtelHelper.createHostResource(
				Map.of(HOST_NAME, HOSTNAME_ATTRIBUTE_VALUE, HOST_TYPE_ATTRIBUTE_KEY, COMPUTE_HOST_TYPE),
				emptyMap
			);
			final Resource expected = Resource.create(
				Attributes
					.builder()
					.put(HOST_NAME, HOSTNAME_ATTRIBUTE_VALUE)
					.put(HOST_TYPE_ATTRIBUTE_KEY, COMPUTE_HOST_TYPE)
					.build()
			);

			assertEquals(expected, resource);
		}
		{
			final Resource resource = OtelHelper.createHostResource(
				Map.of(
					HOST_NAME,
					HOSTNAME_ATTRIBUTE_VALUE,
					HOST_TYPE_ATTRIBUTE_KEY,
					COMPUTE_HOST_TYPE,
					OS_TYPE_ATTRIBUTE_KEY,
					MetricsHubConstants.OTEL_LINUX_OS_TYPE
				),
				Map.of(OS_TYPE_ATTRIBUTE_KEY, MetricsHubConstants.OTEL_WINDOWS_OS_TYPE)
			);
			final Resource expected = Resource.create(
				Attributes
					.builder()
					.put(HOST_NAME, HOSTNAME_ATTRIBUTE_VALUE)
					.put(HOST_TYPE_ATTRIBUTE_KEY, COMPUTE_HOST_TYPE)
					.put(OS_TYPE_ATTRIBUTE_KEY, MetricsHubConstants.OTEL_WINDOWS_OS_TYPE)
					.build()
			);

			assertEquals(expected, resource);
		}
	}

	@Test
	void testIsAcceptedKey() {
		assertFalse(OtelHelper.isAcceptedKey("__key"));
		assertTrue(OtelHelper.isAcceptedKey("key"));
	}

	@Test
	void testBuildOtelAttributesFromMap() {
		assertEquals(
			ATTRIBUTES,
			OtelHelper.buildOtelAttributesFromMap(Map.of(COMPANY_ATTRIBUTE_KEY, COMPANY_ATTRIBUTE_VALUE))
		);
	}
}
