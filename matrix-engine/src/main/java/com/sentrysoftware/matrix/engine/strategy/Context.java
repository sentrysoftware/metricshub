package com.sentrysoftware.matrix.engine.strategy;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Context {

	@Autowired
	private IStrategy strategy;

	@Autowired
	private StrategyConfig strategyConfig;

	/**
	 * Executes the current context strategy
	 * 
	 * @return <code>true</code> if the execution succeeds
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public boolean executeStrategy() throws InterruptedException, ExecutionException, TimeoutException {

		Boolean result = false;

		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		final long operationTimeout = strategyConfig.getEngineConfiguration().getOperationTimeout();

		try {

			strategy.prepare();

			final Future<Boolean> handler = executorService.submit(strategy);

			result = handler.get(operationTimeout, TimeUnit.SECONDS);

			strategy.post();

		} finally {
			strategy.release();
			executorService.shutdownNow();
		}

		if (null == result) {
			result = false;
		}

		return result;
	}

}
