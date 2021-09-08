package com.sentrysoftware.hardware.prometheus.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.hardware.prometheus.dto.PrometheusParameter;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BYTES_PARAMETER_UNIT;

class PrometheusSpecificitiesTest {

	@Test
	void testGetInfoMetricName() {
		MonitorType.MONITOR_TYPES.stream().forEach(monitorType -> assertNotNull(PrometheusSpecificities.getInfoMetricName(monitorType)));
	}

	@Test
	void testGetPrometheusParameter() {

		for (MonitorType monitorType : MonitorType.MONITOR_TYPES) {

			final Map<String, PrometheusParameter> prometheusSpecificities = PrometheusSpecificities.getPrometheusParameters()
					.get(monitorType);
			if (prometheusSpecificities != null) {
				for (Entry<String, PrometheusParameter> entry : prometheusSpecificities.entrySet())  {
					assertNotNull(entry.getValue());
				}
			}
		}

		Optional<PrometheusParameter> batteryCharge = PrometheusSpecificities
				.getPrometheusParameter(MonitorType.BATTERY, "charge");
		PrometheusParameter expectedBatteryCharge = PrometheusParameter.builder().name("hw_battery_charge_ratio")
				.unit("ratio").factor(0.01).build();
		assertEquals(expectedBatteryCharge, batteryCharge.get());
		assertEquals(Optional.empty(), PrometheusSpecificities.getPrometheusParameter(MonitorType.BATTERY, "blabla"));
		assertEquals(Optional.empty(), PrometheusSpecificities.getPrometheusParameter(MonitorType.BATTERY, ""));

	}

	@Test
	void testGetPrometheusMetadataToParameters() {

		assertNotNull(PrometheusSpecificities.getPrometheusMetadataToParameters(MonitorType.CPU, "maximumSpeed"));
		Optional<PrometheusParameter> memorySize = PrometheusSpecificities.getPrometheusMetadataToParameters(MonitorType.MEMORY, "size");
		PrometheusParameter expectedMemorySize = PrometheusParameter.builder().name("hw_memory_size_bytes")
				.unit(BYTES_PARAMETER_UNIT).factor(1000000.0).build();
		assertEquals(expectedMemorySize, memorySize.get());
		assertEquals(Optional.empty(), PrometheusSpecificities.getPrometheusMetadataToParameters(MonitorType.MEMORY, "blabla"));
		assertEquals(Optional.empty(), PrometheusSpecificities.getPrometheusMetadataToParameters(MonitorType.MEMORY, ""));
		assertEquals(Optional.empty(), PrometheusSpecificities.getPrometheusMetadataToParameters(MonitorType.MEMORY, null));
		assertEquals(Optional.empty(), PrometheusSpecificities.getPrometheusMetadataToParameters(MonitorType.ENCLOSURE, ""));

	}

}
