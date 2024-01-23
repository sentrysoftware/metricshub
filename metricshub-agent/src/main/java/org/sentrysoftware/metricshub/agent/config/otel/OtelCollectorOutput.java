package org.sentrysoftware.metricshub.agent.config.otel;

import static org.sentrysoftware.metricshub.agent.config.otel.OtelCollectorConfig.EXECUTABLE_OUTPUT_ID;

import io.grpc.netty.shaded.io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.sentrysoftware.metricshub.agent.process.config.ProcessOutput;
import org.sentrysoftware.metricshub.agent.service.OtelCollectorProcessService;
import org.slf4j.LoggerFactory;

/**
 * Enumeration representing different output options for the OpenTelemetry Collector.
 */
@AllArgsConstructor
public enum OtelCollectorOutput {
	/**
	 * Silent output, no logging or console output.
	 */
	SILENT(ProcessOutput::silent),
	/**
	 * Console output.
	 */
	CONSOLE(() -> ProcessOutput.namedConsoleOutput(EXECUTABLE_OUTPUT_ID)),
	/**
	 * Logging output using SLF4J.
	 */
	LOG(() -> ProcessOutput.logOutput(LoggerFactory.getLogger(OtelCollectorProcessService.class)));

	@Getter
	private Supplier<ProcessOutput> processOutputSupplier;
}
