package com.sentrysoftware.matrix.engine;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.util.Assert;

import com.sentrysoftware.matrix.engine.configuration.ApplicationBeans;
import com.sentrysoftware.matrix.engine.strategy.Context;
import com.sentrysoftware.matrix.engine.strategy.IStrategy;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Engine {

	private static final String STRATEGY_TIME = "strategyTime";
	private static final String STRATEGY_BEAN_NAME = "strategy";
	private static final String STRATEGY_CONFIG_BEAN_NAME = "strategyConfig";

	public EngineResult run(final EngineConfiguration engineConfiguration, final IHostMonitoring hostMonitoring,
			final IStrategy strategy) {

		log.debug("Job Called");

		checkArguments(engineConfiguration, hostMonitoring, strategy);

		final ApplicationContext applicationContext = createApplicationContext(engineConfiguration, hostMonitoring, strategy);

		try {
			final boolean result = applicationContext.getBean(Context.class).executeStrategy();

			return EngineResult.builder().hostMonitoring(hostMonitoring)
					.operationStatus(result ? OperationStatus.SUCCESS : OperationStatus.ERROR).build();
		} catch (ExecutionException e) {
			return EngineResult.builder().hostMonitoring(hostMonitoring)
					.operationStatus(OperationStatus.EXECUTION_EXCEPTION).build();
		} catch (InterruptedException | TimeoutException e) {
			Thread.currentThread().interrupt();
			return EngineResult.builder().hostMonitoring(hostMonitoring)
					.operationStatus(OperationStatus.TIMEOUT_EXCEPTION).build();
		} catch (Exception e) {
			return EngineResult.builder().hostMonitoring(hostMonitoring).operationStatus(OperationStatus.GENERAL_ERROR)
					.build();
		}
	}

	/**
	 * Check Engine arguments
	 * @param engineConfiguration
	 * @param hostMonitoring
	 * @param strategy
	 */
	private static void checkArguments(final EngineConfiguration engineConfiguration, final IHostMonitoring hostMonitoring,
			final IStrategy strategy) {
		Assert.notNull(engineConfiguration, "engineConfiguration cannot be null.");
		Assert.notNull(hostMonitoring, "hostMonitoring cannot be null.");
		Assert.notNull(strategy, "strategy cannot be null.");
		Assert.notNull(engineConfiguration.getProtocolConfigurations(), "protocolConfigurations cannot be null.");
		Assert.isTrue(!engineConfiguration.getProtocolConfigurations().isEmpty(), "protocolConfigurations cannot be empty.");
		Assert.notNull(engineConfiguration.getSelectedConnectors(), "selectedConnectors cannot be null.");
		Assert.notNull(engineConfiguration.getTarget(), "target cannot be null.");
		Assert.notNull(engineConfiguration.getTarget().getHostname(), "target hostname cannot be null.");
		Assert.notNull(engineConfiguration.getTarget().getType(), "target type cannot be null.");
		Assert.notNull(engineConfiguration.getTarget().getId(), "target id cannot be null.");
	}

	/**
	 * Create Spring {@link ApplicationContext} which provides a bean factory for accessing application components 
	 * @param engineConfiguration
	 * @param hostMonitoring
	 * @param strategy
	 * @return {@link ApplicationContext}
	 */
	private ApplicationContext createApplicationContext(final EngineConfiguration engineConfiguration,
			final IHostMonitoring hostMonitoring, final IStrategy strategy) {

		log.debug("Creating spring context");
		final StrategyConfig strategyConfig = StrategyConfig.builder().engineConfiguration(engineConfiguration)
				.hostMonitoring(hostMonitoring).build();

		final AnnotationConfigApplicationContext configContext = new AnnotationConfigApplicationContext();
		configContext.getBeanFactory().destroySingletons();
		configContext.getBeanFactory().registerSingleton(STRATEGY_CONFIG_BEAN_NAME, strategyConfig);
		configContext.getBeanFactory().registerSingleton(STRATEGY_BEAN_NAME, strategy);
		configContext.getBeanFactory().registerSingleton(STRATEGY_TIME, new Date().getTime());
		// Register the configuration and components scan after singleton registrations
		// so that we can avoid the UnsatisfiedDependencyException
		configContext.register(ApplicationBeans.class);
		configContext.refresh();
		configContext.getBeanFactory().autowireBean(strategy);
		configContext.getBeanFactory().autowireBean(strategyConfig);
		

		return configContext;
	}

}
