package com.sentrysoftware.matrix.connector.parser.state.source.wbem;


import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem.WBEMSource;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class WbemProcessor extends AbstractStateParser {

	protected static final String WBEM_TYPE_VALUE = "WBEM";

	@Override
	public Class<WBEMSource> getType() {
		return WBEMSource.class;
	}

	@Override
	public String getTypeValue() {
		return WBEM_TYPE_VALUE;
	}
}
