package com.sentrysoftware.matrix.engine;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.sentrysoftware.matrix.engine.strategy.Context;
import com.sentrysoftware.matrix.engine.strategy.IStrategy;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Engine {

	public EngineResult run(final EngineConfiguration engineConfiguration, final IHostMonitoring hostMonitoring,
			final IStrategy strategy) {

		log.debug("Run called");

		final StrategyConfig config = StrategyConfig.builder().engineConfiguration(engineConfiguration)
				.hostMonitoring(hostMonitoring).build();

		final Context context = new Context(strategy, config);

		try {
			boolean result = context.executeStrategy();
			return EngineResult.builder().hostMonitoring(hostMonitoring)
					.operationStatus(result ? OperationStatus.SUCCESS : OperationStatus.ERROR).build();
		} catch (ExecutionException e) {
			return EngineResult.builder().hostMonitoring(hostMonitoring)
					.operationStatus(OperationStatus.EXECUTION_EXCEPTION).build();
		} catch (InterruptedException | TimeoutException e) {
			return EngineResult.builder().hostMonitoring(hostMonitoring)
					.operationStatus(OperationStatus.TIMEOUT_EXCEPTION).build();
		} catch (Exception e) {
			return EngineResult.builder().hostMonitoring(hostMonitoring).operationStatus(OperationStatus.GENERAL_ERROR)
					.build();
		}
	}

}
