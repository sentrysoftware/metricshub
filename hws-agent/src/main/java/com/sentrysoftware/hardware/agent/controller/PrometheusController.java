package com.sentrysoftware.hardware.agent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sentrysoftware.hardware.agent.dto.HostContext;
import com.sentrysoftware.hardware.agent.exception.BusinessException;
import com.sentrysoftware.hardware.agent.service.prometheus.PrometheusService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controllers for Prometheus operations.
 * 
 * @deprecated This controller is no longer supported as the agent pushes metrics to the OTEL Collector through OTLP
 */
@Tag(name = "/metrics")
@RestController
@RequestMapping(value = "/", produces = MediaType.TEXT_PLAIN_VALUE)
@Deprecated(since = "1.1")
public class PrometheusController {

	@Autowired
	private PrometheusService prometheusService;

	@GetMapping("metrics")
	@Operation(
			summary = "Get hardware metrics in Prometheus format version 0.0.4.",
			description = "Get all the hardware metrics collected by Hardware Sentry Prometheus exporter."
	)
	public String metrics() throws BusinessException {

		return prometheusService.collectMetrics();
	}

	@GetMapping("metrics/{hostId}")
	@Operation(
			summary = "Get hardware metrics for a specific host in Prometheus format version 0.0.4.",
			description = "Get all the hardware metrics collected for a specific host by Hardware Sentry Prometheus exporter."
	)
	public String metrics(
					@PathVariable("hostId")
					@Parameter(description = "The host identifier", example = "ecs1-01")
					String hostId) throws BusinessException {

		try {

			HostContext.setHostId(hostId);

			return prometheusService.collectMetrics();

		} finally {

			HostContext.clear();
		}
	}
}