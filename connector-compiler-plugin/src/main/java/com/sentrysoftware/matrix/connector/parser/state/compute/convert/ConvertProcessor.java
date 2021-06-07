package com.sentrysoftware.matrix.connector.parser.state.compute.convert;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Convert;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class ConvertProcessor extends AbstractStateParser {

	protected static final String CONVERT_TYPE_VALUE = "Convert";

	@Override
	protected Class<?> getType() {
		return Convert.class;
	}

	@Override
	protected String getTypeValue() {
		return CONVERT_TYPE_VALUE;
	}

}
