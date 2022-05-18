package com.sentrysoftware.matrix.connector.parser.state.detection.oscommand;

import com.sentrysoftware.matrix.connector.model.detection.criteria.oscommand.OsCommand;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class OsCommandProcessor extends AbstractStateParser {

	protected static final String OSCOMMAND_TYPE_VALUE = "OSCommand";

	@Override
	protected Class<OsCommand> getType() {
		return OsCommand.class;
	}

	@Override
	protected String getTypeValue() {
		return OSCOMMAND_TYPE_VALUE;
	}

}
