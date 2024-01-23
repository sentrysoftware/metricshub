package org.sentrysoftware.metricshub.engine.strategy;

public interface IStrategy extends Runnable {
	/**
	 * Get the timeout of the strategy.
	 *
	 * @return long value
	 */
	long getStrategyTimeout();

	/**
	 * Provide the current time from which the strategy starts.
	 *
	 * @return {@link Long} value
	 */
	Long getStrategyTime();
}
