package com.sentrysoftware.hardware.prometheus.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.hardware.prometheus.exception.BusinessException;
import com.sentrysoftware.hardware.prometheus.service.PrometheusService;

@ExtendWith(MockitoExtension.class)
class PrometheusControllerTest {

	@Mock
	private PrometheusService prometheusService; 

	@InjectMocks
	private PrometheusController prometheusController = new PrometheusController();

	@Test
	void testMetrics() throws BusinessException {
		final String expected = "enclosure_status{id=\"1\", parentId=\"0\", label=\"encolosure 1\"} 0";
		doReturn(expected).when(prometheusService).collectMetrics(null);
		assertEquals(expected, prometheusController.metrics());
		verify(prometheusService, times(1)).collectMetrics(null);
	}

	@Test
	void testMetricsWithTargetId() throws BusinessException {

		final String expected = "enclosure_status{id=\"1\", parentId=\"0\", label=\"encolosure 1\"} 0";
		doReturn(expected).when(prometheusService).collectMetrics(anyString());
		assertEquals(expected, prometheusController.metrics("1"));
		verify(prometheusService).collectMetrics(anyString());
	}
}
