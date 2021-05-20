package com.sentrysoftware.matrix.connector.parser.state.source.tableunion;


import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tableunion.TableUnionSource;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class TableUnionProcessor extends AbstractStateParser {

	protected static final String TABLE_UNION_TYPE_VALUE = "TableUnion";

	@Override
	public Class<TableUnionSource> getType() {
		return TableUnionSource.class;
	}

	@Override
	public String getTypeValue() {
		return TABLE_UNION_TYPE_VALUE;
	}
}
