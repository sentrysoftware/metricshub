package com.sentrysoftware.hardware.prometheus.service;

import java.io.IOException;
import java.io.StringWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sentrysoftware.hardware.prometheus.dto.ErrorCode;
import com.sentrysoftware.hardware.prometheus.exception.BusinessException;

import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import lombok.extern.slf4j.Slf4j;

/**
 * This service is used to build outputs in order to serve the Prometheus clients. This service calls the Matrix Engine to collect metrics
 * then 
 */
@Service
@Slf4j
public class PrometheusService {

	@Autowired
	private MatrixEngineService matrixEngineService;

	@Autowired
	private HostMonitoringCollectorService hostMonitoringCollectorService;

	/**
	 * Call the Matrix engine to collect the configured system then register {@link HostMonitoringCollectorService} 
	 * to parse metrics.
	 * 
	 * @return Text version 0.0.4 of the {@link MetricFamilySamples}
	 * @throws BusinessException
	 */
	public String collectMetrics() throws BusinessException {
		// Need to clear the registry so that we have fresh data, we also avoid errors
		// metric already set...etc.
		CollectorRegistry.defaultRegistry.clear();

		// Call the Matrix engine to run the detection, discovery and collect
		matrixEngineService.performJobs();

		// Register the Prometheus collector with the default registry
		hostMonitoringCollectorService.register();

		// Create the writer
		final StringWriter writer = new StringWriter();

		try {
			// Write out the text version 0.0.4 of the MetricFamilySamples
			TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples());
		} catch (IOException e) {
			final String message = "IO Error while building Prometheus metrics.";
			log.error(message, e);
			throw new BusinessException(ErrorCode.PROMETHEUS_IO_ERROR, e.getMessage(), e);
		}

		// Flush the Stream
		writer.flush();

		// Return the String value
		return writer.toString();

	}

}
