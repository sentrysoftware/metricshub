package com.sentrysoftware.matrix.connector.parser.state.compute.divide;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Divide;
import com.sentrysoftware.matrix.connector.parser.state.compute.AbstractComputeParser;

public abstract class DivideProcessor extends AbstractComputeParser {

	protected static final String DIVIDE_TYPE_VALUE = "Divide";

	@Override
	public Class<TypeProcessor> getTypeProcessor() {
		return TypeProcessor.class;
	}

	@Override
	public Class<Divide> getComputeType() {
		return Divide.class;
	}

	@Override
	public String getTypeValue() {
		return DIVIDE_TYPE_VALUE;
	}
}
