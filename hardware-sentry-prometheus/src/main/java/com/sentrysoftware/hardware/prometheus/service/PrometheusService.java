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
 * This service is used to build outputs in order to serve the Prometheus clients.
 * This service calls the Matrix Engine to collect metrics.
 */
@Service
@Slf4j
public class PrometheusService {

	@Autowired
	private HostMonitoringCollectorService hostMonitoringCollectorService;

	/**
	 * Call the Matrix engine to collect the configured system then register {@link HostMonitoringCollectorService}
	 * in a new {@link CollectorRegistry} wrapping the Prometheus metric family samples.
	 *
	 * @return						Text version 0.0.4 of the {@link MetricFamilySamples}.
	 * @throws BusinessException	If an error occurs.
	 */
	public String collectMetrics() throws BusinessException {

		// Need to create a new registry so that we have fresh data, we also avoid errors
		// metric already set...etc when using the CollectorRegistry.defaultRegistry
		CollectorRegistry collectorRegistry = new CollectorRegistry(true);

		// Register the Prometheus collector with the new registry
		hostMonitoringCollectorService.register(collectorRegistry);

		// Create the writer
		final StringWriter writer = new StringWriter();

		try {
			// Write out the text version 0.0.4 of the MetricFamilySamples
			TextFormat.write004(writer, collectorRegistry.metricFamilySamples());
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
