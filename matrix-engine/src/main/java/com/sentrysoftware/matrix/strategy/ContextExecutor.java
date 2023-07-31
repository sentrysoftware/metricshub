package com.sentrysoftware.matrix.strategy;


import com.sentrysoftware.matrix.configuration.HostConfiguration;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContextExecutor {

	private IStrategy strategy;

	/**
	 * This method prepares the strategy, runs the run method it in a separate thread.
	 * Upon thread completion, it calls the post method of the IStrategy instance and ensures proper termination of the task
	 * @return boolean true if the executor is terminated and false if it is timed out
	 * @throws InterruptedException if the thread is interrupted while waiting
	 */
	public boolean execute() throws InterruptedException {

		final ExecutorService executorService = Executors.newSingleThreadExecutor();

		// Retrieve default strategy timeout from HostConfiguration
		final HostConfiguration hostConfiguration = new HostConfiguration();
		final long strategyTimeout = hostConfiguration.getStrategyTimeout();

		strategy.prepare();

		// Call run method of strategy
		executorService.execute(strategy);

		// Order shutdown
		executorService.shutdown();

		// Blocks until the task has completed execution after a shutdown request, or the timeout occurs.
		final boolean isTerminated = executorService.awaitTermination(strategyTimeout, TimeUnit.SECONDS);

		strategy.post();

		return isTerminated;
	}
}
