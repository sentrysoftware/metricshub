package com.sentrysoftware.matrix.connector.parser.state.compute.substring;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Substring;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class SubstringProcessor extends AbstractStateParser {

	protected static final String SUBSTRING_TYPE_VALUE = "Substring";

	@Override
	protected Class<?> getType() {
		return Substring.class;
	}

	@Override
	protected String getTypeValue() {
		return SUBSTRING_TYPE_VALUE;
	}
}
