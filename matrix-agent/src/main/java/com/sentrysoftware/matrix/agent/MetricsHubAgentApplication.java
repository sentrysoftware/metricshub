package com.sentrysoftware.matrix.agent;

import com.sentrysoftware.matrix.agent.context.AgentContext;
import com.sentrysoftware.matrix.agent.helper.AgentConstants;
import java.util.Locale;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import picocli.CommandLine;
import picocli.CommandLine.Option;

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
		} catch (Exception e) {
			configureGlobalErrorLogger();
			log.error("Failed to start MetricsHub Agent.", e);
			throw new IllegalStateException("Error dectected during MetricsHub agent startup.", e);
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
