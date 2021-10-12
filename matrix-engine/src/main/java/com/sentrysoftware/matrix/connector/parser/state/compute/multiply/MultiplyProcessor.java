package com.sentrysoftware.matrix.connector.parser.state.compute.multiply;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Multiply;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class MultiplyProcessor extends AbstractStateParser {

	protected static final String MULTIPLY_TYPE_VALUE = "Multiply";

	@Override
	public Class<Multiply> getType() {
		return Multiply.class;
	}

	@Override
	public String getTypeValue() {
		return MULTIPLY_TYPE_VALUE;
	}
}
