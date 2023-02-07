package com.sentrysoftware.matrix.connector.deserializer;

import java.io.IOException;

import com.sentrysoftware.matrix.connector.model.Connector;

interface IDeserializerTest {

	public Connector getConnector(String file) throws IOException;

	public String getResourcePath();

	static final String RESOURCE_EXT = ".yaml";

	static final String JSON_MAPPING_EXCEPTION_MSG = "Expected a JsonMappingException to be thrown";
	static final String INVALID_FORMAT_EXCEPTION_MSG = "Expected an InvalidFormatException to be thrown";
	static final String INVALID_NULL_EXCEPTION_MSG = "Expected an InvalidNullException to be thrown";
	static final String MISMATCHED_EXCEPTION_MSG = "Expected a MismatchedInputException to be thrown";

	static final ConnectorDeserializer deserializer = new ConnectorDeserializer();

}
