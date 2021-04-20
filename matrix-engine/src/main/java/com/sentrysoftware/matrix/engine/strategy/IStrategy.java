package com.sentrysoftware.matrix.engine.strategy;

import java.util.concurrent.Callable;


public interface IStrategy extends Callable<Boolean> {

	void release();

	void prepare();

	void post();
}

