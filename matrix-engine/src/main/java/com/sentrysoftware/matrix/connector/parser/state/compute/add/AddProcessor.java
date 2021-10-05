package com.sentrysoftware.matrix.connector.parser.state.compute.add;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Add;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class AddProcessor extends AbstractStateParser {

	protected static final String ADD_TYPE_VALUE = "Add";

	@Override
	public Class<Add> getType() {
		return Add.class;
	}

	@Override
	public String getTypeValue() {
		return ADD_TYPE_VALUE;
	}
}
