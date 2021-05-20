package com.sentrysoftware.matrix.connector.parser;

public class ConnectorParserConstants {

	private ConnectorParserConstants() {

	}

	public static final String EMPTY_STRING = "";
	public static final String COMMA = ",";
	public static final String SEMICOLON = ";";
	public static final String DOT = ".";
	public static final String OPENING_PARENTHESIS = "(";
	public static final String CLOSING_PARENTHESIS = ")";

	public static final String ONE = "1";
	public static final String PERCENT = "%";

	public static final String DOUBLE_QUOTES_REGEX_REPLACEMENT = "^\\s*\"(.*)\"\\s*$";
	public static final String SOURCE_REFERENCE_REGEX_REPLACEMENT = "^\\s*%(.*)%\\s*$";

	public static final String INTEGER_REGEX = "^[1-9]\\d*$";

	public static final String DISCOVERY = "discovery";
	public static final String COLLECT = "collect";

	public static final String MONO_INSTANCE = "monoinstance";
	public static final String MULTI_INSTANCE = "multiinstance";

	public static final String TYPE = "type";
	public static final String VALUE_TABLE = "valuetable";

	public static final String COMPUTE = "compute";
}
