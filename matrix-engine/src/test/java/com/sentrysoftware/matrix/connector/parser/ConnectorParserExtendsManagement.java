package com.sentrysoftware.matrix.connector.parser;

public class ConnectorParserExtendsManagement extends AbstractConnectorParserManagement {

	public ConnectorParserExtendsManagement(String relativePath) {
		super(relativePath);
	}

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/extends/management/" + relativePath + "/";
	}

}
