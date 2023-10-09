package com.sentrysoftware.matrix.strategy;

public interface IStrategy extends Runnable {
	/**
	 * Operations to execute before running the strategy.
	 */
	void prepare();

	/**
	 * Operations to execute after the termination of the strategy.
	 */
	void post();

	/**
	 * Get the timeout of the strategy.
	 *
	 * @return long value
	 */
	long getStrategyTimeout();
}
