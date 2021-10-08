package com.sentrysoftware.matrix.connector.parser.state.detection.oscommand;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.oscommand.OSCommand;
import com.sentrysoftware.matrix.connector.parser.ConnectorParser;

public class OsCommandTest {

	private final ConnectorParser connectorParser = new ConnectorParser();

	@Test
	void testDetectionOSCommandTest() throws Exception {

		// Load the test connector
		Connector connector = connectorParser.parse("src/test/resources/hdf/DetectionOSCommandTest.hdfs");
		assertNotNull(connector);

		// Check it has Detection criteria
		Detection detection = connector.getDetection();
		assertNotNull(detection);
		List<Criterion> criteria = detection.getCriteria();
		assertNotNull(criteria);
		assertEquals(3, criteria.size());

		// Tests on Detection.Criteria(1)
		{
			Criterion criterion = criteria.get(0);
			assertTrue(criterion instanceof OSCommand);

			OSCommand osCommandCriterion = (OSCommand) criterion;
			assertEquals("command line", osCommandCriterion.getCommandLine());
			assertTrue(osCommandCriterion.isExecuteLocally());
			assertEquals("expected result", osCommandCriterion.getExpectedResult());
			assertEquals(5, osCommandCriterion.getTimeout());
			assertEquals("error message", osCommandCriterion.getErrorMessage());
			assertTrue(osCommandCriterion.isForceSerialization());
		}

		// Tests on Detection.Criteria(2)
		{
			Criterion criterion = criteria.get(1);
			assertTrue(criterion instanceof OSCommand);

			OSCommand osCommandCriterion = (OSCommand) criterion;
			assertEquals("command line", osCommandCriterion.getCommandLine());
			assertFalse(osCommandCriterion.isExecuteLocally());
			assertEquals("expected result", osCommandCriterion.getExpectedResult());
			assertEquals(10, osCommandCriterion.getTimeout());
			assertEquals("error message", osCommandCriterion.getErrorMessage());
			assertFalse(osCommandCriterion.isForceSerialization());
		}

		// Tests on Detection.Criteria(3)
		{
			Criterion criterion = criteria.get(2);
			assertTrue(criterion instanceof OSCommand);

			OSCommand osCommandCriterion = (OSCommand) criterion;
			assertEquals("command line", osCommandCriterion.getCommandLine());
			assertFalse(osCommandCriterion.isExecuteLocally());
			assertNull(osCommandCriterion.getExpectedResult());
			assertNull(osCommandCriterion.getTimeout());
			assertNull(osCommandCriterion.getErrorMessage());
			assertFalse(osCommandCriterion.isForceSerialization());
		}

	}

}
