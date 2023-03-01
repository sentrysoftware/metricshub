package com.sentrysoftware.matrix.connector.parser;

import java.io.IOException;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class ConnectorParserTest {

	@Disabled("Disabled until Extends management is up!")
	@Test
	void testExtendsManagementArrayObjectsMerge() throws IOException {
		new ConnectorParserExtendsManagement("arrayObjectsMerge").test();
	}

	@Disabled("Disabled until Extends management is up!")
	@Test
	void testExtendsManagementMergeObjects() throws IOException {
		new ConnectorParserExtendsManagement("mergeObjects").test();
	}

	@Disabled("Disabled until Extends management is up!")
	@Test
	void testExtendsManagementOverwriteArraysSimpleValues() throws IOException {
		new ConnectorParserExtendsManagement("overwriteArraysSimpleValues").test();
	}

	@Test
	void testConstantsManagement() throws IOException {
		new ConnectorParserConstantsManagement("management").test();
	}
}
