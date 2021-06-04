package com.sentrysoftware.matrix.connector.parser.state.compute.extract;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Extract;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class ExtractProcessor extends AbstractStateParser {

	protected static final String EXTRACT_TYPE_VALUE = "Extract";

	@Override
	public Class<Extract> getType() {
		return Extract.class;
	}

	@Override
	public String getTypeValue() {
		return EXTRACT_TYPE_VALUE;
	}
}
