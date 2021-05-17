package com.sentrysoftware.matrix.connector.parser.state.compute.rightconcat;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.RightConcat;
import com.sentrysoftware.matrix.connector.parser.state.compute.AbstractComputeParser;

public abstract class RightConcatProcessor extends AbstractComputeParser {

	protected static final String RIGHT_CONCAT_TYPE_VALUE = "RightConcat";

	@Override
	public Class<TypeProcessor> getTypeProcessor() {
		return TypeProcessor.class;
	}

	@Override
	public Class<RightConcat> getComputeType() {
		return RightConcat.class;
	}

	@Override
	public String getTypeValue() {
		return RIGHT_CONCAT_TYPE_VALUE;
	}
}
