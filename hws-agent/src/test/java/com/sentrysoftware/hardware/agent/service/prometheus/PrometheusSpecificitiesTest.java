package com.sentrysoftware.hardware.agent.service.prometheus;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BYTES_PARAMETER_UNIT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;

@Deprecated(since = "1.1")
class PrometheusSpecificitiesTest {

	@Test
	void testGetInfoMetricName() {
		MonitorType.MONITOR_TYPES.stream().forEach(monitorType -> assertNotNull(PrometheusSpecificities.getInfoMetricName(monitorType)));
	}

	@Test
	void testGetPrometheusParameter() {

		for (MonitorType monitorType : MonitorType.MONITOR_TYPES) {

			final Map<String, MetricInfo> prometheusSpecificities = PrometheusSpecificities.getPrometheusParameters()
					.get(monitorType);
			if (prometheusSpecificities != null) {
				for (Entry<String, MetricInfo> entry : prometheusSpecificities.entrySet())  {
					assertNotNull(entry.getValue());
				}
			}
		}

		Optional<MetricInfo> batteryCharge = PrometheusSpecificities
				.getPrometheusParameter(MonitorType.BATTERY, "charge");
		MetricInfo expectedBatteryCharge = MetricInfo.builder().name("hw_battery_charge_ratio")
				.unit("ratio").factor(0.01).build();
		assertEquals(expectedBatteryCharge, batteryCharge.get());
		assertEquals(Optional.empty(), PrometheusSpecificities.getPrometheusParameter(MonitorType.BATTERY, "blabla"));
		assertEquals(Optional.empty(), PrometheusSpecificities.getPrometheusParameter(MonitorType.BATTERY, ""));

	}

	@Test
	void testGetPrometheusMetadataToParameters() {

		assertNotNull(PrometheusSpecificities.getPrometheusMetadataToParameters(MonitorType.CPU, "maximumSpeed"));
		Optional<MetricInfo> memorySize = PrometheusSpecificities.getPrometheusMetadataToParameters(MonitorType.MEMORY, "size");
		MetricInfo expectedMemorySize = MetricInfo.builder().name("hw_memory_size_bytes")
				.unit(BYTES_PARAMETER_UNIT).factor(1000000.0).build();
		assertEquals(expectedMemorySize, memorySize.get());
		assertEquals(Optional.empty(), PrometheusSpecificities.getPrometheusMetadataToParameters(MonitorType.MEMORY, "blabla"));
		assertEquals(Optional.empty(), PrometheusSpecificities.getPrometheusMetadataToParameters(MonitorType.MEMORY, ""));
		assertEquals(Optional.empty(), PrometheusSpecificities.getPrometheusMetadataToParameters(MonitorType.MEMORY, null));
		assertEquals(Optional.empty(), PrometheusSpecificities.getPrometheusMetadataToParameters(MonitorType.ENCLOSURE, ""));

	}

}
