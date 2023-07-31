package com.sentrysoftware.matrix.strategy;

public interface IStrategy extends Runnable {
	void prepare();
	void post();
}
