package com.sentrysoftware.matrix.agent.helper;

import static com.sentrysoftware.matrix.agent.helper.OtelHelper.FQDN_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.*;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.COMPUTE_HOST_TYPE;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.HOSTNAME;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_NAME;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OtelHelperTest {

	private static final String EXTRA_HOSTNAME_ATTRIBUTE_VALUE = "extraHostname";
	private static final String HOSTNAME_ATTRIBUTE_VALUE = "hostname";

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
			assertThrows(IllegalArgumentException.class, () -> OtelHelper.createHostResource(null, emptyMap, false));
			assertThrows(IllegalArgumentException.class, () -> OtelHelper.createHostResource(emptyMap, null, false));
			assertDoesNotThrow(() -> OtelHelper.createHostResource(emptyMap, emptyMap, false));
		}
		{
			final Resource resource = OtelHelper.createHostResource(
				Map.of(HOST_NAME, HOSTNAME_ATTRIBUTE_VALUE, HOST_TYPE_ATTRIBUTE_KEY, COMPUTE_HOST_TYPE),
				Map.of(HOST_NAME, EXTRA_HOSTNAME_ATTRIBUTE_VALUE),
				false
			);
			final Resource expected = Resource.create(
				Attributes
					.builder()
					.put(HOST_NAME, EXTRA_HOSTNAME_ATTRIBUTE_VALUE)
					.put(HOST_TYPE_ATTRIBUTE_KEY, COMPUTE_HOST_TYPE)
					.build()
			);

			assertEquals(expected, resource);
		}
		{
			final Resource resource = OtelHelper.createHostResource(
				Map.of(HOST_NAME, HOSTNAME_ATTRIBUTE_VALUE, HOST_TYPE_ATTRIBUTE_KEY, COMPUTE_HOST_TYPE),
				emptyMap,
				true
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
				Map.of(HOST_NAME, HOSTNAME_ATTRIBUTE_VALUE, HOST_TYPE_ATTRIBUTE_KEY, COMPUTE_HOST_TYPE),
				Map.of(FQDN_ATTRIBUTE_KEY, HOSTNAME),
				true
			);
			final Resource expected = Resource.create(
				Attributes
					.builder()
					.put(HOST_NAME, HOSTNAME)
					.put(HOST_TYPE_ATTRIBUTE_KEY, COMPUTE_HOST_TYPE)
					.put(FQDN_ATTRIBUTE_KEY, HOSTNAME)
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
