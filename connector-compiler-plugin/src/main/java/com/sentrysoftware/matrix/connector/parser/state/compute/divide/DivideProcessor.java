package com.sentrysoftware.matrix.connector.parser.state.compute.divide;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Divide;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class DivideProcessor extends AbstractStateParser {

	protected static final String DIVIDE_TYPE_VALUE = "Divide";

	@Override
	public Class<Divide> getType() {
		return Divide.class;
	}

	@Override
	public String getTypeValue() {
		return DIVIDE_TYPE_VALUE;
	}
}
