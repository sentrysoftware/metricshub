package com.sentrysoftware.matrix.connector.parser.state.detection.wbem;

import com.sentrysoftware.matrix.connector.model.detection.criteria.wbem.WBEM;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class WbemProcessor extends AbstractStateParser {

	protected static final String WBEM_TYPE_VALUE = "WBEM";

	@Override
	public Class<WBEM> getType() {
		return WBEM.class;
	}

	@Override
	public String getTypeValue() {
		return WBEM_TYPE_VALUE;
	}
}
