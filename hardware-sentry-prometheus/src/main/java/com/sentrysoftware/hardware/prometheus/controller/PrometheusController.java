package com.sentrysoftware.hardware.prometheus.controller;

import com.sentrysoftware.hardware.prometheus.exception.BusinessException;
import com.sentrysoftware.hardware.prometheus.service.PrometheusService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controllers for Prometheus operations.
 */
@Api(value = "/metrics")
@RestController
@RequestMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
public class PrometheusController {

	@Autowired
	private PrometheusService prometheusService;

	@GetMapping("metrics")
	@ApiOperation(
			value = "Get hardware metrics in Prometheus format version 0.0.4.",
			notes = "Get all the hardware metrics collected by Hardware Sentry Prometheus exporter.",
			response = String.class
	)
	public String metrics() throws BusinessException {

		return prometheusService.collectMetrics();
	}
}