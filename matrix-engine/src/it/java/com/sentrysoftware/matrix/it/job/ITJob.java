package com.sentrysoftware.matrix.it.job;

import java.io.IOException;
import java.nio.file.Path;

import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.strategy.IStrategy;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectOperation;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

public interface ITJob {

	/**
	 * Start the server and load the record data from the given paths
	 *
	 * @param recordDataPaths Record data located under src/it/resources, the path are expected without src/it/resources
	 * @return The actual {@link ITJob}
	 * @throws Exception
	 */
	ITJob withServerRecordData(String... recordDataPaths) throws Exception;

	/**
	 * Load the engine required data: {@link EngineConfiguration} and the {@link HostMonitoring}
	 *
	 * @param engineConfiguration {@link EngineConfiguration} instance defining the host, the configured protocol, selected connectors and
	 *                            timeouts
	 * @param hostMonitoring      The container of the monitors
	 * @return The actual {@link ITJob}
	 */
	ITJob prepareEngine(EngineConfiguration engineConfiguration, IHostMonitoring hostMonitoring);

	/**
	 * Run the engine {@link IStrategy}
	 *
	 * @param strategy The strategy to run. E.g. {@link DiscoveryOperation}, {@link DetectionOperation} or {@link CollectOperation}
	 * @return The actual {@link ITJob}
	 */
	ITJob executeStrategy(IStrategy strategy);

	/**
	 * Verify the expected result located under the given expectedPath
	 *
	 * @param expectedPath Expected result path located under src/it/resources, the path are expected without src/it/resources
	 * @return actual {@link ITJob}
	 * @throws Exception
	 */
	ITJob verifyExpected(String expectedPath) throws Exception;

	/**
	 * Stop the server
	 */
	void stopServer();

	/**
	 * @return <code>true</code> if the server is started otherwise <code>false</code>
	 */
	boolean isServerStarted();

	/**
	 * Save the hostMonitoring JSON into a file.
	 *
	 * @param path path of the saving file.
	 * @return actual {@link ITJob}
	 * @throws IOException
	 */
	ITJob saveHostMonitoringJson(final Path path) throws IOException;
}
