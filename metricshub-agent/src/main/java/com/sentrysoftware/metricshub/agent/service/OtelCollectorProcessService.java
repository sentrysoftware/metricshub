package com.sentrysoftware.metricshub.agent.service;

import com.sentrysoftware.metricshub.agent.config.AgentConfig;
import com.sentrysoftware.metricshub.agent.config.otel.OtelCollectorConfig;
import com.sentrysoftware.metricshub.agent.config.otel.OtelCollectorOutput;
import com.sentrysoftware.metricshub.agent.process.config.ProcessOutput;
import com.sentrysoftware.metricshub.agent.process.io.LineReaderProcessor;
import com.sentrysoftware.metricshub.agent.process.io.ProcessorHelper;
import com.sentrysoftware.metricshub.agent.process.runtime.AbstractProcess;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;

/**
 * OpenTelemetry Collector process service
 */
@Slf4j
public class OtelCollectorProcessService extends AbstractProcess {

	private AgentConfig agentConfig;

	public OtelCollectorProcessService(final AgentConfig agentConfig) {
		super(agentConfig.getOtelCollector().toProcessConfig());
		this.agentConfig = agentConfig;
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

	/**
	 * Launch the OpenTelemetry Collector. If the configuration specifies that the collector must
	 * be disabled, the collector startup will not be initiated.
	 */
	public void launch() {
		final OtelCollectorConfig otelCollectorConfig = agentConfig.getOtelCollector();

		if (agentConfig.getOtelCollector().isDisabled()) {
			log.info("The MetricsHub Agent is configured to not start the OpenTelemetry Collector.");
			return;
		}

		// The subprocess must be ran using a separate thread so that it creates a log context for himself only.
		// The process output will be redirected to a dedicated file, see how log4j2.xml is configured using the
		// thread context pattern.
		final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();

		// Run the execution
		singleThreadExecutor.submit(() -> {
			// Configure logger
			ThreadContext.put("logId", OtelCollectorConfig.EXECUTABLE_OUTPUT_ID);

			// By default otelcol debug is enabled
			String loggerLevel = Level.DEBUG.name();

			// User might want to disable logging for the process
			if (otelCollectorConfig.getOutput() != OtelCollectorOutput.LOG) {
				loggerLevel = Level.OFF.name();
			}

			ThreadContext.put("loggerLevel", loggerLevel);

			final String outputDirectory = agentConfig.getOutputDirectory();
			if (outputDirectory != null) {
				ThreadContext.put("outputDirectory", outputDirectory);
			}

			// Start the executable
			try {
				start();
			} catch (Exception e) {
				log.error("Could not start process using command line: {}.", processConfig.getCommandLine());
				log.debug("Error: ", e);
			}
		});

		try {
			singleThreadExecutor.awaitTermination(otelCollectorConfig.getStartupDelay(), TimeUnit.SECONDS);
		} catch (Exception e) { // NOSONAR
			log.error(
				"Startup process has been interrupted after {} seconds. Command line: {}.",
				otelCollectorConfig.getStartupDelay(),
				processConfig.getCommandLine()
			);
			log.debug("Error: ", e);
		}
	}
}
