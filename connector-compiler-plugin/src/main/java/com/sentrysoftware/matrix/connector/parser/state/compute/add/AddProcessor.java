package com.sentrysoftware.matrix.connector.parser.state.compute.add;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Add;
import com.sentrysoftware.matrix.connector.parser.state.compute.AbstractComputeParser;

public abstract class AddProcessor extends AbstractComputeParser {

	protected static final String ADD_TYPE_VALUE = "Add";

	@Override
	public Class<TypeProcessor> getTypeProcessor() {
		return TypeProcessor.class;
	}

	@Override
	public Class<Add> getComputeType() {
		return Add.class;
	}

	@Override
	public String getTypeValue() {
		return ADD_TYPE_VALUE;
	}
}
