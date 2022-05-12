package com.sentrysoftware.hardware.agent.service.prometheus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

@Deprecated(since = "1.1")
class HardwareGaugeMetricTest {

	@Test
	void test() {
		{
			final HardwareGaugeMetric gauge = new HardwareGaugeMetric("metric", "metric help", 1.0);
			assertEquals(Collections.emptyList(), gauge.getLabelNames());
			assertEquals("", gauge.getNameSuffix());
			assertEquals(1.0, gauge.samples.get(0).value);
		}

		{
			final HardwareGaugeMetric gauge =new HardwareGaugeMetric("metric", "metric help", List.of("id", "name"));
			assertEquals(List.of("id", "name"), gauge.getLabelNames());
			assertEquals("", gauge.getNameSuffix());
			assertTrue(gauge.samples.isEmpty());
		}
	}

	@Test
	void testAddMetric() {
		{
			final HardwareGaugeMetric gauge =new HardwareGaugeMetric("metric", "metric help", List.of("id", "name"));
			gauge.addMetric(List.of("id_value", "name_value"), 1.0, null);
			assertEquals(1.0, gauge.samples.get(0).value);
			assertNull(gauge.samples.get(0).timestampMs);
		}
		{
			List<String> badLabelValues = List.of("id_value");
			final HardwareGaugeMetric gauge =new HardwareGaugeMetric("metric", "metric help", List.of("id", "name"));
			assertThrows(IllegalArgumentException.class, () -> gauge.addMetric(badLabelValues, 1.0, null));
		}
		{
			final HardwareGaugeMetric gauge =new HardwareGaugeMetric("metric", "metric help", List.of("id", "name"));
			gauge.addMetric(List.of("id_value", "name_value"), null, null);
			assertTrue(gauge.samples.isEmpty());
		}
		{
			final HardwareGaugeMetric gauge =new HardwareGaugeMetric("metric", "metric help", List.of("id", "name"));
			gauge.addMetric(Arrays.asList(null, "name_value"), 1.0, null);
			assertEquals(1.0, gauge.samples.get(0).value);
			assertNull(gauge.samples.get(0).timestampMs);
			assertEquals(List.of("", "name_value"), gauge.samples.get(0).labelValues);
		}
	}
}
