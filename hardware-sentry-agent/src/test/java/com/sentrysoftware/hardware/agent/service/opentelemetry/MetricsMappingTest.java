package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MAXIMUM_SPEED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.hardware.agent.dto.MetricInfo;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;

class MetricsMappingTest {

	@Test
	void testGetMetricInfoForMonitorType() {
		MonitorType.MONITOR_TYPES.forEach(monitorType -> assertNotNull(MetricsMapping.getMetricInfoForMonitorType(monitorType)));
	}

	@Test
	void testGetAttributes() {
		MonitorType.MONITOR_TYPES.forEach(monitorType -> assertNotNull(MetricsMapping.getAttributes(monitorType)));
	}

	@Test
	void testGetMetricInfo() {

		for (MonitorType monitorType : MonitorType.MONITOR_TYPES) {

			final Map<String, MetricInfo> matrixParamToMetricInfo = MetricsMapping.getMatrixParamToMetricMap()
					.get(monitorType);
			if (matrixParamToMetricInfo != null) {
				for (Entry<String, MetricInfo> entry : matrixParamToMetricInfo.entrySet())  {
					assertNotNull(entry.getValue());
				}
			}
		}

		final Optional<MetricInfo> batteryChargeInfo = MetricsMapping.getMetricInfo(MonitorType.BATTERY, "charge");

		assertTrue(batteryChargeInfo.isPresent());
		assertTrue(MetricsMapping.getMetricInfo(MonitorType.BATTERY, "undefinedMatrixParamName").isEmpty());
		assertTrue(MetricsMapping.getMetricInfo(MonitorType.BATTERY, "").isEmpty());

	}

	@Test
	void testGetMetadataAsMetricInfo() {

		assertTrue(MetricsMapping.getMetadataAsMetricInfo(MonitorType.CPU, MAXIMUM_SPEED).isPresent());
		assertEquals(Optional.empty(), MetricsMapping.getMetadataAsMetricInfo(MonitorType.MEMORY, "undefinedMatrixMetadataName"));
		assertEquals(Optional.empty(), MetricsMapping.getMetadataAsMetricInfo(MonitorType.MEMORY, ""));
		assertEquals(Optional.empty(), MetricsMapping.getMetadataAsMetricInfo(MonitorType.MEMORY, null));
		assertEquals(Optional.empty(), MetricsMapping.getMetadataAsMetricInfo(MonitorType.ENCLOSURE, ""));

	}

}
