package com.sentrysoftware.matrix.connector.parser.state.source.oscommand;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OsCommandSource;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class OsCommandProcessor extends AbstractStateParser {

	public static final String OS_COMMAND_TYPE = "OSCommand";
	
	@Override
	protected Class<?> getType() {
		return OsCommandSource.class;
	}

	@Override
	protected String getTypeValue() {
		return OS_COMMAND_TYPE;
	}

}
