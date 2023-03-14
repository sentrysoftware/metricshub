package com.sentrysoftware.matrix.converter.state.detection.device.type;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.AbstractConnectorPropertyConverterTest;

class ConnectorDeviceTypePropertyTest extends AbstractConnectorPropertyConverterTest {

	@Override
	protected String getResourcePath() {
		return "src/test/resources/test-files/connector/detection/criteria/deviceType/";
	}

	@Test
	void testKeep() throws IOException {
		String input = """
				// Only for type storage
				Detection.Criteria(1).Type="OS"
				Detection.Criteria(1).KeepOnly="OOB"
				""";

		testConversion(input, "keep");
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

		testConversion(input, "multipleCriteria");
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

		testConversion(input, "keepMultiple");
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

		testConversion(input, "exclude");
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

		testConversion(input, "excludeKeep");
	}
}