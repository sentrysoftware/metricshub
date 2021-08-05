package com.sentrysoftware.matrix.connector.parser.state.detection.process;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.process.Process;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public class ProcessCommandLineProcessor extends AbstractStateParser {

	private static final Pattern PROCESS_COMMAND_LINE_KEY_PATTERN = Pattern.compile(
			"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.processcommandline\\s*$",
			Pattern.CASE_INSENSITIVE);

	public static final String PROCESS_TYPE_VALUE = "Process";

	@Override
	public Class<Process> getType() {
		return Process.class;
	}

	@Override
	public String getTypeValue() {
		return PROCESS_TYPE_VALUE;
	}

	@Override
	protected Matcher getMatcher(String key) {
		return PROCESS_COMMAND_LINE_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((Process) getCriterion(key, connector)).setProcessCommandLine(value);
	}
}
