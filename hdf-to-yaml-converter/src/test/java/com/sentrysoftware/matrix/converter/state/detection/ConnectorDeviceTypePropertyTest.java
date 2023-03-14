package com.sentrysoftware.matrix.converter.state.detection;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
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

	@Test
	void testKeep() throws IOException {
		String input = """
				// Only for type storage
				Detection.Criteria(1).Type="OS"
				Detection.Criteria(1).KeepOnly="OOB"
				""";

		String yaml = """
				---
				connector:
				detection:
				  criteria:
				  - type: deviceType
				  	_comments: Only for type storage
				  	keep: [ OOB ]
				""";
		testConversion(input, yaml);
	}

	@Test
	void testMultipleCriteria() throws IOException {
		String input = """
				// Only for type storage
				Detection.Criteria(1).Type="OS"
				Detection.Criteria(1).KeepOnly="OOB"

				// Definitely not linux
				Detection.Criteria(2).Type="OS"
				Detection.Criteria(2).Exclude="Linux"
				""";

		String yaml = """
				---
				connector:
				detection:
				  criteria:
				  - type: deviceType
				  	_comments: Only for type storage
				  	keep: [ OOB ]
				  - type: deviceType
				  	_comments: Definitely not linux
				  	exclude: [ Linux ]
				""";
		testConversion(input, yaml);
	}

	@Test
	void testKeepMultiple() throws IOException {
		String input = """
				//
				// DETECTION
				//

				Detection.Criteria(1).Type="OS"
				Detection.Criteria(1).KeepOnly="Solaris,SunOS,Linux"
				""";

		String yaml = """
				---
				connector:
				detection:
				  criteria:
				  - type: deviceType
				  	_comments: >
					  //
					  // DETECTION
					  //
				  	keep: [ Solaris, SunOs, Linux ]
					""";
		testConversion(input, yaml);
	}

	@Test
	void testExclude() throws IOException {
		String input = """
				//
				// DETECTION
				//
				
				// Exclude Windows, because on Windows, SCSI disks are monitored through
				// the WBEM layer
				Detection.Criteria(1).Type="OS"
				Detection.Criteria(1).Exclude="NT"
				""";

		String yaml = """
				---
				connector:
				detection:
				  criteria:
				  - type: deviceType
				  	_comments: >
					  //
					  // DETECTION
					  //
					  
					  // Exclude Windows, because on Windows, SCSI disks are monitored through
					  // the WBEM layer
				  	exclude: [ NT ]
				""";
		testConversion(input, yaml);
	}

	@Test
	void testExcludeKeep() throws IOException {
		String input = """
				//
				// DETECTION
				//
				
				// Exclude Windows, because on Windows, SCSI disks are monitored through
				// the WBEM layer
				Detection.Criteria(1).Type="OS"
				Detection.Criteria(1).Exclude="NT"
				Detection.Criteria(1).KeepOnly="Linux"
				""";

		String yaml = """
				---
				connector:
				detection:
				  criteria:
				  - type: deviceType
				  	_comments: >
					  //
					  // DETECTION
					  //
					  
					  // Exclude Windows, because on Windows, SCSI disks are monitored through
					  // the WBEM layer
				  	exclude: [ NT ]
					keep: [ Linux ]
				""";
		testConversion(input, yaml);
	}

	
	private void testConversion(String input, String yaml)
			throws IOException, JsonProcessingException, JsonMappingException {
		PreConnector preConnector = new PreConnector();
		preConnector.load(new ByteArrayInputStream(input.getBytes()));
		ConnectorConverter connectorConverter = new ConnectorConverter(preConnector);
		JsonNode connector = connectorConverter.convert();

		ObjectMapper mapper = JsonHelper.buildYamlMapper();
		JsonNode expected = mapper.readTree(yaml);
		assertEquals(expected, connector);
	}
}