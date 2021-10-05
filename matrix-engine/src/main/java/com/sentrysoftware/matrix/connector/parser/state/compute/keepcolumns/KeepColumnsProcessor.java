package com.sentrysoftware.matrix.connector.parser.state.compute.keepcolumns;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepColumns;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class KeepColumnsProcessor extends AbstractStateParser {

	protected static final String KEEP_COLUMNS_TYPE_VALUE = "KeepColumns";

	@Override
	public Class<KeepColumns> getType() {
		return KeepColumns.class;
	}

	@Override
	public String getTypeValue() {
		return KEEP_COLUMNS_TYPE_VALUE;
	}
}
