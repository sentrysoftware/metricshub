package org.sentrysoftware.metricshub.hardware.threshold;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.sentrysoftware.metricshub.hardware.util.HwConstants.HW_ERRORS_LIMIT_LIMIT_TYPE_CRITICAL_HW_TYPE_CPU;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.metric.AbstractMetric;
import org.sentrysoftware.metricshub.engine.telemetry.metric.NumberMetric;

public class CpuMetricNormalizerTest {

	private CpuMetricNormalizer cpuMetricNormalizer;
	private Monitor mockMonitor;
	private MetricFactory mockMetricFactory;

	@BeforeEach
	public void setUp() {
		cpuMetricNormalizer = new CpuMetricNormalizer();
		mockMonitor = mock(Monitor.class);
		mockMetricFactory = mock(MetricFactory.class);
	}

	@Test
	public void testContainsAllEntries() {
		Map<String, String> firstMap = new HashMap<>();
		firstMap.put("key1", "value1");
		firstMap.put("key2", "value2");

		Map<String, String> secondMap = new HashMap<>();
		secondMap.put("key1", "value1");

		assertTrue(AbstractMetricNormalizer.containsAllEntries(firstMap, secondMap));

		secondMap.put("key2", "differentValue");
		assertFalse(AbstractMetricNormalizer.containsAllEntries(firstMap, secondMap));
	}

	@Test
	public void testIsMetricAvailable() {
		String metricName = "metric.name{attribute1=value1,attribute2=value2}";
		String prefix = "metric.name";
		Map<String, String> attributes = Map.of("attribute1", "value1", "attribute2", "value2");

		// Mocking the static methods of MetricFactory
		try (MockedStatic<MetricFactory> mockedFactory = Mockito.mockStatic(MetricFactory.class)) {
			mockedFactory.when(() -> MetricFactory.extractName(metricName)).thenReturn(prefix);
			mockedFactory.when(() -> MetricFactory.extractAttributesFromMetricName(metricName)).thenReturn(attributes);

			boolean result = cpuMetricNormalizer.isMetricAvailable(metricName, prefix, attributes);
			assertTrue(result);
		}
	}
}
