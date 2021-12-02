package com.sentrysoftware.hardware.agent.configuration;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReaderFactory;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;

@Configuration
public class OtelConfig {

	@Bean
	public MetricReaderFactory periodicReaderFactory() {
		// set up the metricInfo exporter and wire it into the SDK and a timed periodic
		// reader.
		final OtlpGrpcMetricExporter metricExporter = OtlpGrpcMetricExporter.getDefault();

		return PeriodicMetricReader
				.builder(metricExporter)
				// Set a large interval of reads. The metrics flush is called after each collect
				.setInterval(Duration.ofDays(365 * 10L)) 
				.newMetricReaderFactory();

	}

}
