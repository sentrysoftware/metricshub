package com.sentrysoftware.matrix.engine.strategy;

import com.sentrysoftware.matrix.utils.Assert;

public abstract class AbstractStrategy implements IStrategy {

	protected StrategyConfig strategyConfig;

	@Override
	public void prepare(StrategyConfig strategyConfig) {
		Assert.notNull(strategyConfig, "strategyConfig cannot be null");

		this.strategyConfig = strategyConfig;
	}
}
