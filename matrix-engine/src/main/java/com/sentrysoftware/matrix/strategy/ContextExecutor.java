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
