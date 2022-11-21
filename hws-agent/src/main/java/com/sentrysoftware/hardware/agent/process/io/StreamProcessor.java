package com.sentrysoftware.hardware.agent.process.io;

/**
 * Stream processor interface to be implemented in order to handle the process output
 */
public interface StreamProcessor {

	/**
	 * Process the given block
	 * 
	 * @param block
	 */
	void process(String block);

}
