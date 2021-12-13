package com.sentrysoftware.hardware.agent.configuration;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReaderFactory;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;

@Configuration
public class OtelConfig {

	@Value("#{ '${grpc}'.trim() <= '' ? 'http://localhost:4317' : '${grpc}' }")
	private String grpcEndpoint;

	@Bean
	public MetricReaderFactory periodicReaderFactory() {
		// set up the metricInfo exporter and wire it into the SDK and a timed periodic
		// reader.
		final OtlpGrpcMetricExporter metricExporter = OtlpGrpcMetricExporter
				.builder()
				.setEndpoint(grpcEndpoint)
				.build();

		return PeriodicMetricReader
				.builder(metricExporter)
				// Set a large interval of reads. The metrics flush is called after each collect
				.setInterval(Duration.ofDays(365 * 10L)) 
				.newMetricReaderFactory();

	}

}
