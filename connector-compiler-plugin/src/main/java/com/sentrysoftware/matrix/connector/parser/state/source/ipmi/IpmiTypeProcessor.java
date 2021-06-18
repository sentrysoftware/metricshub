package com.sentrysoftware.matrix.connector.parser.state.source.ipmi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ipmi.IPMI;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;

public class IpmiTypeProcessor extends IpmiProcessor {

	private static final Pattern IPMI_TYPE_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.type\\s*$",
			Pattern.CASE_INSENSITIVE);

	@Override
	public Matcher getMatcher(String key) {
		return IPMI_TYPE_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		new TypeProcessor(IPMI.class, IpmiProcessor.IPMI_TYPE_VALUE).parse(key, value, connector);

		connector.getSudoCommands().add(ConnectorParserConstants.IPMI_TOOL);
	}
}
