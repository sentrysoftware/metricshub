package com.sentrysoftware.matrix.agent.service;

import com.sentrysoftware.matrix.agent.process.config.ProcessConfig;
import com.sentrysoftware.matrix.agent.process.config.ProcessOutput;
import com.sentrysoftware.matrix.agent.process.io.LineReaderProcessor;
import com.sentrysoftware.matrix.agent.process.io.ProcessorHelper;
import com.sentrysoftware.matrix.agent.process.runtime.AbstractProcess;

/**
 * OpenTelemetry Collector process service
 */
public class OtelCollectorProcessService extends AbstractProcess {

	public OtelCollectorProcessService(final ProcessConfig processConfig) {
		super(processConfig);
	}

	@Override
	protected void onBeforeProcess() {
		// Not implemented
	}

	@Override
	protected void onBeforeProcessStart(ProcessBuilder processBuilder) {
		// Not implemented
	}

	@Override
	protected void onAfterProcessStart() {
		final ProcessOutput outputConfig = processConfig.getOutput();

		// Is there an output configuration defining output processors?
		if (outputConfig != null) {

			// Connect the reader to the output processor
			ProcessorHelper.connect(getReader(), outputConfig.getOutputProcessor(), LineReaderProcessor::new);

			// Connect the error reader to the error processor
			// If the error processor is not present then the error stream is redirected to output stream
			ProcessorHelper.connect(getError(), outputConfig.getErrorProcessor(), LineReaderProcessor::new);

		}
	}

	@Override
	protected void onBeforeProcessStop() {
		// Not implemented
	}

	@Override
	protected void stopInternal() {
		super.stopProcess();
	}

	@Override
	protected void onAfterProcessStop() {
		// Not implemented
	}

}
