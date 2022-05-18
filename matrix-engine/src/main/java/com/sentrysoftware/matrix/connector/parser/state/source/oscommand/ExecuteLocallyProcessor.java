package com.sentrysoftware.matrix.connector.parser.state.source.oscommand;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.ONE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OsCommandSource;

import lombok.NonNull;

public class ExecuteLocallyProcessor extends OsCommandProcessor {

	private static final Pattern EXECUTE_LOCALLY_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.executelocally\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	protected Matcher getMatcher(
			@NonNull
			final String key) {
		return EXECUTE_LOCALLY_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((OsCommandSource) getSource(key, connector)).setExecuteLocally(ONE.equals(value));
	}
}
