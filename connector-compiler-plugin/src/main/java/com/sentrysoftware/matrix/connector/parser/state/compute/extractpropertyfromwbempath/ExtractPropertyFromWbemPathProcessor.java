package com.sentrysoftware.matrix.connector.parser.state.compute.extractpropertyfromwbempath;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ExtractPropertyFromWbemPath;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class ExtractPropertyFromWbemPathProcessor extends AbstractStateParser {
	protected static final String EXTRACT_PROPERTY_FROM_WBEM_PATH_TYPE_VALUE = "extractpropertyfromwbempath";

	@Override
	public Class<ExtractPropertyFromWbemPath> getType() {
		return ExtractPropertyFromWbemPath.class;
	}

	@Override
	public String getTypeValue() {
		return EXTRACT_PROPERTY_FROM_WBEM_PATH_TYPE_VALUE;
	}
}
