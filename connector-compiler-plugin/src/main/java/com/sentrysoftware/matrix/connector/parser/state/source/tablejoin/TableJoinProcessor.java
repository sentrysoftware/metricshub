package com.sentrysoftware.matrix.connector.parser.state.source.tablejoin;


import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class TableJoinProcessor extends AbstractStateParser {

	protected static final String TABLE_JOIN_TYPE_VALUE = "TableJoint";

	@Override
	public Class<TableJoinSource> getType() {
		return TableJoinSource.class;
	}

	@Override
	public String getTypeValue() {
		return TABLE_JOIN_TYPE_VALUE;
	}
}
