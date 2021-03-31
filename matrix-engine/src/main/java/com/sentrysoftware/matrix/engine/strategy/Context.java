package com.sentrysoftware.matrix.engine.strategy;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sentrysoftware.matrix.utils.Assert;

public class Context {

	private IStrategy strategy;
	private StrategyConfig strategyConfig;

	public Context(IStrategy strategy, StrategyConfig strategyConfig) {

		Assert.notNull(strategy, "strategy cannot be null");
		Assert.notNull(strategyConfig, "strategyConfig cannot be null");

		this.strategy = strategy;
		this.strategyConfig = strategyConfig;
	}

	public boolean executeStrategy() throws InterruptedException, ExecutionException, TimeoutException {

		Boolean result = false;

		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		final long operationTimeout = strategyConfig.getEngineConfiguration().getOperationTimeout();

		try {

			strategy.prepare(strategyConfig);

			final Future<Boolean> handler = executorService.submit(strategy);

			result = handler.get(operationTimeout, TimeUnit.MILLISECONDS);

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
