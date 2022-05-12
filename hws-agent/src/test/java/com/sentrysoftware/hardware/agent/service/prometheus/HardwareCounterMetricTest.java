package com.sentrysoftware.hardware.agent.service.prometheus;

import static com.sentrysoftware.hardware.agent.service.prometheus.HardwareCounterMetric.COUNTER_METRIC_SUFFIX;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.prometheus.client.GaugeMetricFamily;

@Deprecated(since = "1.1")
class HardwareCounterMetricTest {

	@Test
	void test() {
		{
			HardwareCounterMetric counter = new HardwareCounterMetric("metric", "metric help", 1.0);
			assertEquals(Collections.emptyList(), counter.getLabelNames());
			assertEquals(COUNTER_METRIC_SUFFIX, counter.getNameSuffix());
			assertEquals(1.0, counter.samples.get(0).value);
			assertEquals("metric_total", counter.samples.get(0).name);
		}

		{
			final HardwareCounterMetric counter =new HardwareCounterMetric("metric", "metric help", List.of("id", "name"));
			assertEquals(List.of("id", "name"), counter.getLabelNames());
			assertEquals(COUNTER_METRIC_SUFFIX, counter.getNameSuffix());
			assertTrue(counter.samples.isEmpty());
		}

		{
			HardwareCounterMetric counter1 = new HardwareCounterMetric("metric", "metric help", 1.0);
			HardwareCounterMetric counter2 = new HardwareCounterMetric("metric", "metric help", 1.0);
			assertEquals(counter1, counter2);
			assertNotNull(counter1.toString());
			assertEquals(counter1.hashCode(), counter2.hashCode());
			HardwareCounterMetric counter3 = new HardwareCounterMetric("metric_bis", "metric help 2", 3.0);
			assertNotEquals(counter2, counter3);
			assertTrue(counter3.canEqual(counter1));
			assertFalse(counter2.canEqual(new GaugeMetricFamily("v", "v", 0)));
		}
	}

	@Test
	void testAddMetric() {
		{
			final HardwareCounterMetric counter =new HardwareCounterMetric("metric", "metric help", List.of("id", "name"));
			counter.addMetric(List.of("id_value", "name_value"), 1.0, null);
			assertEquals(1.0, counter.samples.get(0).value);
			assertNull(counter.samples.get(0).timestampMs);
		}
		{
			final long ts = new Date().getTime();
			final HardwareCounterMetric counter =new HardwareCounterMetric("metric", "metric help", List.of("id", "name"));
			counter.addMetric(List.of("id_value", "name_value"), 1.0, ts);
			assertEquals(1.0, counter.samples.get(0).value);
			assertEquals(ts, counter.samples.get(0).timestampMs);
		}
		{
			List<String> badLabelValues = List.of("id_value");
			final HardwareCounterMetric counter =new HardwareCounterMetric("metric", "metric help", List.of("id", "name"));
			assertThrows(IllegalArgumentException.class, () -> counter.addMetric(badLabelValues, 1.0, null));
		}
		{
			final HardwareCounterMetric counter =new HardwareCounterMetric("metric", "metric help", List.of("id", "name"));
			counter.addMetric(List.of("id_value", "name_value"), null, null);
			assertTrue(counter.samples.isEmpty());
		}
		{
			final HardwareCounterMetric counter =new HardwareCounterMetric("metric", "metric help", List.of("id", "name"));
			counter.addMetric(Arrays.asList(null, "name_value"), 1.0, null);
			assertEquals(1.0, counter.samples.get(0).value);
			assertNull(counter.samples.get(0).timestampMs);
			assertEquals(List.of("", "name_value"), counter.samples.get(0).labelValues);
		}
	}
}
