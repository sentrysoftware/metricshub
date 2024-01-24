package org.sentrysoftware.metricshub.agent;

import java.util.Locale;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import org.sentrysoftware.metricshub.agent.context.AgentContext;
import org.sentrysoftware.metricshub.agent.helper.AgentConstants;
import org.sentrysoftware.metricshub.agent.helper.ConfigHelper;
import org.sentrysoftware.metricshub.agent.service.OtelCollectorProcessService;
import org.sentrysoftware.metricshub.agent.service.TaskSchedulingService;
import org.sentrysoftware.metricshub.agent.service.task.FileWatcherTask;
import picocli.CommandLine;
import picocli.CommandLine.Option;

/**
 * MetricsHub Agent application entry point.
 */
@Data
@Slf4j
public class MetricsHubAgentApplication implements Runnable {
	static {
		Locale.setDefault(Locale.US);
	}

	@Option(names = { "-h", "-?", "--help" }, usageHelp = true, description = "Shows this help message and exits")
	private boolean usageHelpRequested;

	@Option(
		names = { "-c", "--config" },
		usageHelp = false,
		required = false,
		description = "Alternate MetricsHub's configuration file"
	)
	private String alternateConfigFile;

	/**
	 * The main entry point for the MetricsHub Agent application.
	 * Creates an instance of MetricsHubAgentApplication and executes it using CommandLine.
	 *
	 * @param args The command-line arguments passed to the application.
	 */
	public static void main(String[] args) {
		new CommandLine(new MetricsHubAgentApplication()).execute(args);
	}

	@Override
	public void run() {
		try {
			// Initialize the application context
			final AgentContext agentContext = new AgentContext(alternateConfigFile);

			// Start OpenTelemetry Collector process
			agentContext.getOtelCollectorProcessService().launch();

			// Start the Scheduler
			agentContext.getTaskSchedulingService().start();

			FileWatcherTask
				.builder()
				.file(agentContext.getConfigFile())
				.filter(event ->
					event.context() != null && agentContext.getConfigFile().getName().equals(event.context().toString())
				)
				.checksum(ConfigHelper.calculateMD5Checksum(agentContext.getConfigFile()))
				.await(500)
				.onChange(() -> resetContext(agentContext, alternateConfigFile))
				.build()
				.start();
		} catch (Exception e) {
			configureGlobalErrorLogger();
			log.error("Failed to start MetricsHub Agent.", e);
			throw new IllegalStateException("Error dectected during MetricsHub agent startup.", e);
		}
	}

	/**
	 * Resets the agent context by restarting {@link TaskSchedulingService} and {@link OtelCollectorProcessService}:
	 * @param agentContext The agent context
	 * @param alternateConfigFile Alternation configuration file passed by the user
	 */
	private synchronized void resetContext(final AgentContext agentContext, String alternateConfigFile) {
		try {
			agentContext.getTaskSchedulingService().stop();
			agentContext.getOtelCollectorProcessService().stop();

			agentContext.build(alternateConfigFile, false);
			agentContext.getOtelCollectorProcessService().launch();
			agentContext.getTaskSchedulingService().start();
		} catch (Exception e) {
			configureGlobalErrorLogger();
			log.error("Failed to start MetricsHub Agent.", e);
			throw new IllegalStateException("Error detected during MetricsHub agent startup.", e);
		}
	}

	/**
	 * Configure the global error logger to be able to log startup fatal errors
	 * preventing the application from starting
	 */
	static void configureGlobalErrorLogger() {
		ThreadContext.put("logId", "metricshub-agent-global-error");
		ThreadContext.put("loggerLevel", Level.ERROR.toString());
		ThreadContext.put("outputDirectory", AgentConstants.DEFAULT_OUTPUT_DIRECTORY.toString());
	}
}
