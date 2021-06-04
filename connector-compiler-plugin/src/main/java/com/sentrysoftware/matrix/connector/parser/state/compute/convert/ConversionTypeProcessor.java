package com.sentrysoftware.matrix.connector.parser.state.compute.convert;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.ConversionType;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Convert;

public class ConversionTypeProcessor extends ConvertProcessor {

	private static final Pattern CONVERSION_TYPE_KEY_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.compute\\(([1-9]\\d*)\\)\\.conversiontype\\s*$",
			Pattern.CASE_INSENSITIVE);
	
	@Override
	protected Matcher getMatcher(String key) {
		return CONVERSION_TYPE_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(String key, String value, Connector connector) {
		super.parse(key, value, connector);

		((Convert) getCompute(key, connector)).setConversionType(ConversionType.getByName(value));
	}
}
