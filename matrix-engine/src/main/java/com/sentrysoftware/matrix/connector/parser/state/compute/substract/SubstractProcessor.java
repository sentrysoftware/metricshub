package com.sentrysoftware.matrix.connector.parser.state.compute.substract;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Substract;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class SubstractProcessor extends AbstractStateParser {

	protected static final String SUBSTRACT_TYPE_VALUE = "Substract";

	@Override
	public Class<Substract> getType() {
		return Substract.class;
	}

	@Override
	public String getTypeValue() {
		return SUBSTRACT_TYPE_VALUE;
	}
}
