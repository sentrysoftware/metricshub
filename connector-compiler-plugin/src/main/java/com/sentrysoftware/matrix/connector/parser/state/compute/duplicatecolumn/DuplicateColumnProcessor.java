package com.sentrysoftware.matrix.connector.parser.state.compute.duplicatecolumn;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.DuplicateColumn;
import com.sentrysoftware.matrix.connector.parser.state.compute.AbstractComputeParser;

public abstract class DuplicateColumnProcessor extends AbstractComputeParser {

	protected static final String DUPLICATE_COLUMN_TYPE_VALUE = "DuplicateColumn";

	@Override
	public Class<TypeProcessor> getTypeProcessor() {
		return TypeProcessor.class;
	}

	@Override
	public Class<DuplicateColumn> getComputeType() {
		return DuplicateColumn.class;
	}

	@Override
	public String getTypeValue() {
		return DUPLICATE_COLUMN_TYPE_VALUE;
	}
}
