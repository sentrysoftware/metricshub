package com.sentrysoftware.matrix.connector.parser.state.compute.leftconcat;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.LeftConcat;
import com.sentrysoftware.matrix.connector.parser.state.compute.AbstractComputeParser;

public abstract class LeftConcatProcessor extends AbstractComputeParser {

	protected static final String LEFT_CONCAT_TYPE_VALUE = "LeftConcat";

	@Override
	public Class<TypeProcessor> getTypeProcessor() {
		return TypeProcessor.class;
	}

	@Override
	public Class<LeftConcat> getComputeType() {
		return LeftConcat.class;
	}

	@Override
	public String getTypeValue() {
		return LEFT_CONCAT_TYPE_VALUE;
	}
}
