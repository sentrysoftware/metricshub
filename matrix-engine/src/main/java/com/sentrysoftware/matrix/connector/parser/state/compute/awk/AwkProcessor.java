package com.sentrysoftware.matrix.connector.parser.state.compute.awk;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Awk;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class AwkProcessor extends AbstractStateParser {
	protected static final String AWK_TYPE_VALUE = "Awk";

	@Override
	public Class<Awk> getType() {
		return Awk.class;
	}

	@Override
	public String getTypeValue() {
		return AWK_TYPE_VALUE;
	}
}
