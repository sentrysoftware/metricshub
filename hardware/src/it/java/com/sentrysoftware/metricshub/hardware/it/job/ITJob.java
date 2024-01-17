package com.sentrysoftware.metricshub.hardware.it.job;

import java.io.IOException;
import java.nio.file.Path;

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
	 * Run the engine
	 *
	 * @return The actual {@link ITJob}
	 */
	ITJob executeDiscoveryStrategy();

	/**
	 * Run the engine
	 *
	 * @return The actual {@link ITJob}
	 */
	ITJob executeCollectStrategy();

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
	ITJob saveTelemetryManagerJson(final Path path) throws IOException;
}
