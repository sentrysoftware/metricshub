package org.sentrysoftware.metricshub.engine.strategy;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ContextExecutor {

	private IStrategy strategy;

	/**
	 * This method prepares the strategy, runs the run method it in a separate thread.
	 * Upon thread completion, it calls the post method of the IStrategy instance and ensures proper termination of the task
	 *
	 * @throws InterruptedException if the thread is interrupted while waiting
	 * @throws TimeoutException     if the wait timed out
	 * @throws ExecutionException   if the computation threw an exception
	 */
	public void execute() throws InterruptedException, ExecutionException, TimeoutException {
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		try {
			final Future<?> handler = executorService.submit(strategy);

			handler.get(strategy.getStrategyTimeout(), TimeUnit.SECONDS);
		} finally {
			executorService.shutdownNow();
		}
	}
}
