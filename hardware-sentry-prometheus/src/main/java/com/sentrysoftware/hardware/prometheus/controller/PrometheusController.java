package com.sentrysoftware.hardware.prometheus.controller;

import com.sentrysoftware.hardware.prometheus.dto.TargetContext;
import com.sentrysoftware.hardware.prometheus.exception.BusinessException;
import com.sentrysoftware.hardware.prometheus.service.PrometheusService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

		return prometheusService.collectMetrics(null);
	}

	@GetMapping("metrics/{targetId}")
	@ApiOperation(
		value = "Get hardware metrics for a specific target in Prometheus format version 0.0.4.",
		notes = "Get all the hardware metrics collected for a specific target by Hardware Sentry Prometheus exporter.",
		response = String.class
	)
	public String metrics(

		@PathVariable("targetId")
		@ApiParam(
			value = "The target identifier",
			example = "ecs1-01"
		)
			String targetId

	) throws BusinessException {

		try {

			TargetContext.setTargetId(targetId);

			return prometheusService.collectMetrics(targetId);

		} finally {

			TargetContext.clear();
		}
	}
}