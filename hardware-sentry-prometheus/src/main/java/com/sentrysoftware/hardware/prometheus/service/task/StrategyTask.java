package com.sentrysoftware.hardware.prometheus.service.task;

import org.apache.logging.log4j.ThreadContext;

import com.sentrysoftware.matrix.engine.strategy.collect.CollectOperation;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class StrategyTask implements Runnable {

	@NonNull
	private StrategyTaskInfo strategyTaskInfo;

	private int numberOfCollects;

	@Override
	public void run() {

		// Configure Logger
		final IHostMonitoring hostMonitoring = strategyTaskInfo.getHostMonitoring();
		final int discoveryCycle = strategyTaskInfo.getDiscoveryCycle();

		final String targetId = hostMonitoring.getEngineConfiguration().getTarget().getId();

		configureLoggerContext(targetId);

		// Are we supposed to run the target discovery?
		if (numberOfCollects == 0) {
			log.info("Calling the engine to discover target: {}.", targetId);

			// Run all the strategies
			hostMonitoring.run(new DetectionOperation(), new DiscoveryOperation(), new CollectOperation());
		} else {
			log.info("Calling the engine to collect target: {}.", targetId);

			// One more, run only the collect strategy
			hostMonitoring.run(new CollectOperation());
		}

		numberOfCollects++;

		// Reset the number of collects
		if (numberOfCollects >= discoveryCycle) {
			numberOfCollects = 0;
		} 
	}

	/**
	 * Configure the logger context with the targetId, port, debugMode and outputDirectory.
	 *
	 * @param targetId	The unique identifier of the target
	 */
	void configureLoggerContext(final String targetId) {

		ThreadContext.put("targetId", targetId);
		ThreadContext.put("debugMode", String.valueOf(strategyTaskInfo.isDebugMode()));
		ThreadContext.put("port", String.valueOf(strategyTaskInfo.getServerPort()));

		String outputDirectory = strategyTaskInfo.getOutputDirectory();
		if (outputDirectory  != null) {
			ThreadContext.put("outputDirectory", outputDirectory);
		}
	}

}