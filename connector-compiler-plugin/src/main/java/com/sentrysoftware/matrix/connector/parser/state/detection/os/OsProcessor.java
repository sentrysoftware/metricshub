package com.sentrysoftware.matrix.connector.parser.state.detection.os;

import com.sentrysoftware.matrix.connector.model.detection.criteria.os.OS;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class OsProcessor extends AbstractStateParser {

	protected static final String OS_TYPE_VALUE = "OS";

	@Override
	public Class<OS> getType() {
		return OS.class;
	}

	@Override
	public String getTypeValue() {
		return OS_TYPE_VALUE;
	}

}
