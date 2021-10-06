package com.sentrysoftware.matrix.model.parameter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")

@JsonSubTypes({ @JsonSubTypes.Type(value = DiscreteParam.class, name = DiscreteParam.DISCRETE_TYPE),
	@JsonSubTypes.Type(value = NumberParam.class, name = NumberParam.NUMBER_TYPE),
	@JsonSubTypes.Type(value = TextParam.class, name = TextParam.TEXT_TYPE)})
public interface IParameter {

	public String getName();

	/**
	 * Save the parameter value
	 */
	public void save();

	/**
	 * Return the number value
	 *
	 * @return {@link Number} value
	 */
	public Number numberValue();

	/**
	 * Return the type used by the deserialization
	 * 
	 * @return {@link String} value
	 */
	public String getType();

	/**
	 * Return the parameter collect time in milliseconds
	 * 
	 * @return {@link Long} value
	 */
	public Long getCollectTime();

}