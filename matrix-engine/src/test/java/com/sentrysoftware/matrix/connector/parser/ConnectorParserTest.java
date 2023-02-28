package com.sentrysoftware.matrix.connector.parser;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class ConnectorParserTest {

	@Test
	void testExtendsManagementArrayObjectsDepthExtends() throws IOException {
		new ConnectorParserExtendsManagement("arrayObjectsDepthExtends").test();
	}

	@Test
	void testExtendsManagementArrayObjectsMerge() throws IOException {
		new ConnectorParserExtendsManagement("arrayObjectsMerge").test();
	}

	@Test
	void testExtendsManagementArrayObjectsMergeOneExtends() throws IOException {
		new ConnectorParserExtendsManagement("arrayObjectsMergeOneExtends").test();
	}

	@Test
	void testExtendsManagementMergeObjects() throws IOException {
		new ConnectorParserExtendsManagement("mergeObjects").test();
	}

	@Test
	void testExtendsManagementMergeObjectsOneExtends() throws IOException {
		new ConnectorParserExtendsManagement("mergeObjectsOneExtends").test();
	}

	@Test
	void testExtendsManagementOverwriteArraysSimpleValues() throws IOException {
		new ConnectorParserExtendsManagement("overwriteArraysSimpleValues").test();
	}

	@Test
	void testExtendsManagementOverwriteArraysSimpleValuesOneExtends() throws IOException {
		new ConnectorParserExtendsManagement("overwriteArraysSimpleValuesOneExtends").test();
	}

}
