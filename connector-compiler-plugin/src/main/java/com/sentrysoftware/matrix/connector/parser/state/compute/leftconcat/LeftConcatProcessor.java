package com.sentrysoftware.matrix.connector.parser.state.compute.leftconcat;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.LeftConcat;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class LeftConcatProcessor extends AbstractStateParser {

	protected static final String LEFT_CONCAT_TYPE_VALUE = "LeftConcat";

	@Override
	public Class<LeftConcat> getType() {
		return LeftConcat.class;
	}

	@Override
	public String getTypeValue() {
		return LEFT_CONCAT_TYPE_VALUE;
	}
}
