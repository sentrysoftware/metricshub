package com.sentrysoftware.matrix.connector.parser.state.source.tablejoin;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;
import lombok.AllArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AllArgsConstructor
public class DefaultRightLineProcessor extends TableJoinProcessor {

	private static final Pattern DEFAULT_RIGHT_LINE_KEY_PATTERN = Pattern.compile(
		"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.defaultrightline\\s*$",
		Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return DEFAULT_RIGHT_LINE_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		TableJoinSource tableJoinSource = getSource(key, connector);

		String[] splitValue = (!value.endsWith(ConnectorParserConstants.SEMICOLON)
			? value + ConnectorParserConstants.SEMICOLON
			: value)
			.split(ConnectorParserConstants.SEMICOLON, -1);

		tableJoinSource.setDefaultRightLine(Stream
			.of(splitValue)
			.limit(splitValue.length - 1L)
			.collect(Collectors.toList()));
	}
}
