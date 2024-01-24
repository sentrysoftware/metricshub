package org.sentrysoftware.metricshub.engine.strategy;

/**
 * Interface for defining a strategy to be executed in the MetricsHub engine.
 */
public interface IStrategy extends Runnable {
	/**
	 * Get the timeout of the strategy.
	 *
	 * @return long value representing the timeout in seconds.
	 */
	long getStrategyTimeout();

	/**
	 * Provide the current time from which the strategy starts.
	 *
	 * @return {@link Long} value representing the start time of the strategy.
	 */
	Long getStrategyTime();
}
