package com.sentrysoftware.matrix.connector.parser.state.detection.process;

import com.sentrysoftware.matrix.connector.model.detection.criteria.process.Process;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class ProcessProcessor  extends AbstractStateParser {

	protected static final String PROCESS_TYPE_VALUE = "Process";

	@Override
	public Class<Process> getType() {
		return Process.class;
	}

	@Override
	public String getTypeValue() {
		return PROCESS_TYPE_VALUE;
	}
}
