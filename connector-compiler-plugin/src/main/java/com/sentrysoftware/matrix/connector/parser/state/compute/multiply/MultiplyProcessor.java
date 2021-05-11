package com.sentrysoftware.matrix.connector.parser.state.compute.multiply;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Multiply;
import com.sentrysoftware.matrix.connector.parser.state.compute.AbstractComputeParser;

public abstract class MultiplyProcessor extends AbstractComputeParser {

	protected static final String MULTIPLY_TYPE_VALUE = "Multiply";

	@Override
	public Class<TypeProcessor> getTypeProcessor() {
		return TypeProcessor.class;
	}

	@Override
	public Class<Multiply> getComputeType() {
		return Multiply.class;
	}

	@Override
	public String getTypeValue() {
		return MULTIPLY_TYPE_VALUE;
	}
}
