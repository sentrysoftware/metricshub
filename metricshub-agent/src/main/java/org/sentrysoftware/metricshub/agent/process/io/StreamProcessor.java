package org.sentrysoftware.metricshub.agent.process.io;

/**
 * Interface for a stream processor to be implemented for handling process output.
 */
public interface StreamProcessor {
	/**
	 * Process the given block
	 *
	 * @param block The block of text to be processed
	 */
	void process(String block);
}
