package com.sentrysoftware.matrix.connector.parser.state.compute.replace;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Replace;
import com.sentrysoftware.matrix.connector.parser.state.compute.AbstractComputeParser;

public abstract class ReplaceProcessor extends AbstractComputeParser {

	protected static final String REPLACE_TYPE_VALUE = "Replace";

	@Override
	public Class<TypeProcessor> getTypeProcessor() {
		return TypeProcessor.class;
	}

	@Override
	public Class<Replace> getComputeType() {
		return Replace.class;
	}

	@Override
	public String getTypeValue() {
		return REPLACE_TYPE_VALUE;
	}
}
