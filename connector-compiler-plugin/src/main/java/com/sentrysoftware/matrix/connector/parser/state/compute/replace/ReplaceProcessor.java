package com.sentrysoftware.matrix.connector.parser.state.compute.replace;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Replace;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class ReplaceProcessor extends AbstractStateParser {

	protected static final String REPLACE_TYPE_VALUE = "Replace";

	@Override
	public Class<Replace> getType() {
		return Replace.class;
	}

	@Override
	public String getTypeValue() {
		return REPLACE_TYPE_VALUE;
	}
}
