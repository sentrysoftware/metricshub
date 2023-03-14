package com.sentrysoftware.matrix.converter.state.detection.device.type;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.converter.ConnectorConverter;
import com.sentrysoftware.matrix.converter.PreConnector;

class ConnectorDeviceTypePropertyTest {

	private String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/deviceType/";
	}

	@Test
	void testKeep() throws IOException {
		String input = 
				"""
				// Only for type storage
				Detection.Criteria(1).Type="OS"
				Detection.Criteria(1).KeepOnly="OOB"
				""";

		testConversion(input, "keep");
	}

	@Test
	void testMultipleCriteria() throws IOException {
		String input = 
				"""
				// Only for type storage
				Detection.Criteria(1).Type="OS"
				Detection.Criteria(1).KeepOnly="OOB"

				// Definitely not linux
				Detection.Criteria(2).Type="OS"
				Detection.Criteria(2).Exclude="Linux"
				""";

		testConversion(input, "multipleCriteria");
	}

	@Test
	void testKeepMultiple() throws IOException {
		String input = 
				"""
				//
				// DETECTION
				//

				Detection.Criteria(1).Type="OS"
				Detection.Criteria(1).KeepOnly="Solaris,SunOS,Linux"
				""";

		testConversion(input, "keepMultiple");
	}

	@Test
	void testExclude() throws IOException {
		String input = 
				"""
				//
				// DETECTION
				//
				
				// Exclude Windows, because on Windows, SCSI disks are monitored through
				// the WBEM layer
				Detection.Criteria(1).Type="OS"
				Detection.Criteria(1).Exclude="NT"
				""";

		testConversion(input, "exclude");
	}

	@Test
	void testExcludeKeep() throws IOException {
		String input = 
				"""
				//
				// DETECTION
				//
				
				// Exclude Windows, because on Windows, SCSI disks are monitored through
				// the WBEM layer
				Detection.Criteria(1).Type="OS"
				Detection.Criteria(1).Exclude="NT"
				Detection.Criteria(1).KeepOnly="Linux"
				""";

		testConversion(input, "excludeKeep");
	}

	
	private void testConversion(String input, String expectedPath)
			throws IOException, JsonProcessingException, JsonMappingException {

		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(new File(getResourcePath() + expectedPath + "/expected.yaml"));

		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();

		assertEquals(expected, connector);
	}
}