package com.sentrysoftware.matrix.connector.parser.state.compute.rightconcat;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.RightConcat;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class RightConcatProcessor extends AbstractStateParser {

	protected static final String RIGHT_CONCAT_TYPE_VALUE = "RightConcat";

	@Override
	public Class<RightConcat> getType() {
		return RightConcat.class;
	}

	@Override
	public String getTypeValue() {
		return RIGHT_CONCAT_TYPE_VALUE;
	}
}
