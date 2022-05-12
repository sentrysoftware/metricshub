package com.sentrysoftware.matrix.connector.parser.state.detection.oscommand;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.oscommand.OSCommand;

public class CommandLineProcessor extends OSCommandProcessor {

	private static final Pattern COMMANDLINE_KEY_PATTERN = Pattern.compile(
			"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.commandline\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	protected Matcher getMatcher(String key) {
		return COMMANDLINE_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((OSCommand) getCriterion(key, connector)).setCommandLine(value);
	}
}
