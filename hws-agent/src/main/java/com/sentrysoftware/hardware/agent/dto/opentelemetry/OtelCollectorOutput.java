package com.sentrysoftware.hardware.agent.dto.opentelemetry;

import static com.sentrysoftware.hardware.agent.dto.opentelemetry.OtelCollectorConfigDto.EXECUTABLE_OUTPUT_ID;

import org.slf4j.LoggerFactory;

import com.sentrysoftware.hardware.agent.process.config.ProcessOutput;
import com.sentrysoftware.hardware.agent.service.opentelemetry.process.OtelCollectorProcess;

import io.grpc.netty.shaded.io.netty.util.internal.shaded.org.jctools.queues.MessagePassingQueue.Supplier;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum OtelCollectorOutput {

	SILENT(ProcessOutput::silent),
	CONSOLE(() -> ProcessOutput.namedConsoleOutput(EXECUTABLE_OUTPUT_ID)),
	LOG(() -> ProcessOutput.logOutput(LoggerFactory.getLogger(OtelCollectorProcess.class)));

	@Getter
	private Supplier<ProcessOutput> processOutputSupplier;
}
