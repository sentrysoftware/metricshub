package com.sentrysoftware.matrix.strategy;

public interface IStrategy extends Runnable {
	public void prepare();

	public void post();
}
