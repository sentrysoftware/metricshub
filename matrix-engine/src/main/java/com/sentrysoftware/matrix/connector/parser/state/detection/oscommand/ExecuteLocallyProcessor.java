package com.sentrysoftware.matrix.connector.parser.state.detection.oscommand;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.ONE;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.oscommand.OsCommand;

public class ExecuteLocallyProcessor extends OsCommandProcessor {

	private static final Pattern EXECUTELOCALLY_KEY_PATTERN = Pattern.compile(
			"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.executelocally\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	protected Matcher getMatcher(String key) {
		return EXECUTELOCALLY_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		((OsCommand) getCriterion(key, connector)).setExecuteLocally(ONE.equals(value));
	}

}
