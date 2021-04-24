package com.sentrysoftware.matrix.connector.parser;

public class ConnectorParserConstants {

	private ConnectorParserConstants() {

	}

	public static final String EMPTY_STRING = "";
	public static final String DOUBLE_QUOTE = "\"";
	public static final String COMA = ",";
	public static final String ONE = "1";
	public static final String DOUBLE_QUOTES_REGEX_REPLACEMENT = "^\\s*\"(.+)\"\\s*$";
	public static final String SOURCE_REFERENCE_REGEX_REPLACEMENT = "^\\s*%(.+)%\\s*$";
	public static final String DISCOVERY = "discovery";
	public static final String COLLECT = "collect";
}
