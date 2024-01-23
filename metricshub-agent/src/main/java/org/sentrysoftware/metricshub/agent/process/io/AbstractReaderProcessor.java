package org.sentrysoftware.metricshub.agent.process.io;

import java.io.Reader;
import lombok.AllArgsConstructor;
import lombok.NonNull;

/**
 * AbstractReaderProcessor is the base class for implementing reader-based processors in the MetricsHub agent.
 * It extends the Runnable interface to allow for concurrent execution.
 */
@AllArgsConstructor
public abstract class AbstractReaderProcessor implements Runnable {

	@NonNull
	protected final Reader reader;

	@NonNull
	protected final StreamProcessor streamProcessor;
}
