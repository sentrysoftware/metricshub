package com.sentrysoftware.matrix.connector.parser.state.source.tablejoin;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.INTEGER_REGEX;
import static org.springframework.util.Assert.isTrue;

public class RightKeyColumnProcessor extends TableJoinProcessor {

	private static final Pattern RIGHT_KEY_COLUMN_KEY_PATTERN = Pattern.compile(
		"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.rightkeycolumn\\s*$",
		Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return RIGHT_KEY_COLUMN_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		TableJoinSource tableJoinSource = getSource(key, connector);

		isTrue(value.matches(INTEGER_REGEX), () -> "Invalid value: " + value);
		tableJoinSource.setRightKeyColumn(Integer.parseInt(value));
	}
}
