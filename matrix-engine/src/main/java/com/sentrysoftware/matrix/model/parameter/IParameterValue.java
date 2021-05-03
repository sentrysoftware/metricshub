package com.sentrysoftware.matrix.model.parameter;


public interface IParameterValue {

	public String getName();

	/**
	 * Reset the parameter fields
	 */
	public void reset();

	/**
	 * Get the value as String
	 */
	public String getValueAsString();
}
