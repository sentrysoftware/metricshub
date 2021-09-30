package com.sentrysoftware.matrix.connector.parser.state.detection.oscommand;

import com.sentrysoftware.matrix.connector.model.detection.criteria.oscommand.OSCommand;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class OSCommandProcessor extends AbstractStateParser {

	protected static final String OSCOMMAND_TYPE_VALUE = "OSCommand";

	@Override
	protected Class<OSCommand> getType() {
		return OSCommand.class;
	}

	@Override
	protected String getTypeValue() {
		return OSCOMMAND_TYPE_VALUE;
	}

}
