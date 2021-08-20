package com.sentrysoftware.hardware.prometheus.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.hardware.prometheus.dto.PrometheusParameter;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;

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

	}

}
