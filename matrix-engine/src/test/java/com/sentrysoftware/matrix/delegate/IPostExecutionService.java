package com.sentrysoftware.matrix.delegate;

public interface IPostExecutionService {
	/**
	 * This method runs the computation of energy, power consumption, temperature and other metrics of hardware, storage, etc ...
	 */
	void run();
}
