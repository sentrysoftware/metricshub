package com.sentrysoftware.matrix.connector.parser.state.compute.and;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.And;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class AndProcessor extends AbstractStateParser {

	protected static final String AND_TYPE_VALUE = "And";

	@Override
	public Class<And> getType() {
		return And.class;
	}

	@Override
	public String getTypeValue() {
		return AND_TYPE_VALUE;
	}
}
