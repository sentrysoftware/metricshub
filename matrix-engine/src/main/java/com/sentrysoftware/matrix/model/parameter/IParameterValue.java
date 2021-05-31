package com.sentrysoftware.matrix.model.parameter;


public interface IParameterValue {

	public String getName();

	/**
	 * Reset the parameter fields
	 */
	public void reset();

	/**
	 * Get the value as String
	 * 
	 * @return {@link String} representation of the value
	 */
	public String formatValueAsString();

	/**
	 * Return the number value
	 * 
	 * @return {@link Number} value
	 */
	public Number numberValue();
}
