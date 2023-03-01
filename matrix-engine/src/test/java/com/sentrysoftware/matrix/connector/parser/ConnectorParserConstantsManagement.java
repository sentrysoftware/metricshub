package com.sentrysoftware.matrix.connector.parser;

public class ConnectorParserConstantsManagement extends AbstractConnectorParserManagement {

	public ConnectorParserConstantsManagement(String relativePath) {
		super(relativePath);
	}

	@Override
	public String getResourcePath() {
		return String.format("src/test/resources/test-files/constants/%s/", relativePath);
	}

}
