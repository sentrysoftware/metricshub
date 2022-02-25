package com.sentrysoftware.hardware.agent.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sentrysoftware.hardware.agent.dto.TargetContext;
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

	@GetMapping("metrics/{targetId}")
	@Operation(
			summary = "Get hardware metrics for a specific target in Prometheus format version 0.0.4.",
			description = "Get all the hardware metrics collected for a specific target by Hardware Sentry Prometheus exporter."
	)
	public String metrics(
					@PathVariable("targetId")
					@Parameter(description = "The target identifier", example = "ecs1-01")
					String targetId) throws BusinessException {

		try {

			TargetContext.setTargetId(targetId);

			return prometheusService.collectMetrics();

		} finally {

			TargetContext.clear();
		}
	}
}