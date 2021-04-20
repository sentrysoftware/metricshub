package com.sentrysoftware.matrix.engine.strategy;

import org.springframework.beans.factory.annotation.Autowired;

import com.sentrysoftware.matrix.connector.ConnectorStore;

public abstract class AbstractStrategy implements IStrategy {

	@Autowired
	protected ConnectorStore store;

	@Autowired
	protected StrategyConfig strategyConfig;

	@Override
	public void prepare() {

	}
}
