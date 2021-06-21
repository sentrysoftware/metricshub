package com.sentrysoftware.matrix.model.parameter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")

@JsonSubTypes({ @JsonSubTypes.Type(value = PresentParam.class, name = PresentParam.PRESENT_TYPE),
	@JsonSubTypes.Type(value = NumberParam.class, name = NumberParam.NUMBER_TYPE),
	@JsonSubTypes.Type(value = TextParam.class, name = TextParam.TEXT_TYPE),
	@JsonSubTypes.Type(value = StatusParam.class, name = StatusParam.STATUS_TYPE)})
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

	/**
	 * Return the type used by the deserialization
	 */
	public String getType();

}