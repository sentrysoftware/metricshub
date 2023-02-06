package com.sentrysoftware.matrix.connector.deserializer.custom;

public class SupersedesDeserializer extends AbstractNonBlankNonNullInSetDeserializer {

	@Override
	protected String getErrorMessage() {
		return "The connector referenced by 'supersedes' cannot be empty.";
	}

}
