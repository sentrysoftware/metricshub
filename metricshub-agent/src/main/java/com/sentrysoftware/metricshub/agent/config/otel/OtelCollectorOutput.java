package com.sentrysoftware.metricshub.agent.config.otel;

import static com.sentrysoftware.metricshub.agent.config.otel.OtelCollectorConfig.EXECUTABLE_OUTPUT_ID;

import com.sentrysoftware.metricshub.agent.process.config.ProcessOutput;
import com.sentrysoftware.metricshub.agent.service.OtelCollectorProcessService;
import io.grpc.netty.shaded.io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.LoggerFactory;

@AllArgsConstructor
public enum OtelCollectorOutput {
	SILENT(ProcessOutput::silent),
	CONSOLE(() -> ProcessOutput.namedConsoleOutput(EXECUTABLE_OUTPUT_ID)),
	LOG(() -> ProcessOutput.logOutput(LoggerFactory.getLogger(OtelCollectorProcessService.class)));

	@Getter
	private Supplier<ProcessOutput> processOutputSupplier;
}
