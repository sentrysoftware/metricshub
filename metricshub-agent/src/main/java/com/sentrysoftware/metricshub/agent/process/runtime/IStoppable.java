package com.sentrysoftware.metricshub.agent.process.runtime;

/**
 * Use this interface to implement a stoppable process
 */
public interface IStoppable {
	/**
	 * Stop the process
	 */
	void stop();
}
