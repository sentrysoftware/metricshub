package com.sentrysoftware.matrix.engine.strategy;

import org.springframework.beans.factory.annotation.Autowired;

import com.sentrysoftware.matrix.connector.ConnectorStore;
import com.sentrysoftware.matrix.engine.strategy.source.SourceVisitor;

import lombok.Setter;

public abstract class AbstractStrategy implements IStrategy {

	@Autowired
	protected ConnectorStore store;

	@Autowired
	protected StrategyConfig strategyConfig;

	@Autowired
	protected SourceVisitor sourceVisitor;

	@Autowired
	@Setter
	protected Long strategyTime;

	@Override
	public void prepare() {

	}
}
