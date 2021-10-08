package com.sentrysoftware.matrix.connector.parser.state.source.tablejoin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;

public class KeyTypeProcessor extends TableJoinProcessor {

	private static final Pattern KEYTYPE_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.keytype\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	protected Matcher getMatcher(String key) {
		return KEYTYPE_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		TableJoinSource tableJoinSource = getSource(key, connector);

		tableJoinSource.setKeyType(value);
	}


}
