package com.sentrysoftware.matrix.connector.deserializer;

import com.fasterxml.jackson.databind.exc.InvalidNullException;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Ucs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

class CriteriaUcsDeserializerTest extends DeserializerTest {

    @Override
    public String getResourcePath() {
        return "src/test/resources/test-files/connector/detection/criteria/ucs/";
    }

	@Test
	void testDeserializeUcs() throws Exception {
        final String testResource = "ucsCriterion";
        final Connector ucs = getConnector(testResource);

		final String query = "SELECT dn FROM networkElement";
		final String expectedResult = "^networkElement;sys/";
		
		List<Criterion> expected = new ArrayList<>();
		expected.add(new Ucs("ucs", true, query, null, expectedResult));
        compareCriterion(testResource, ucs, expected);
	}

    @Test
    /**
     * Check that query is populated
     */
    void testUcsNullQuery() throws IOException {
           // query is null
           try {
            getConnector("ucsCriterionNullQuery");
            Assert.fail("Expected an InvalidNullException to be thrown.");
        } catch (InvalidNullException e) {
            final String message = "Invalid `null` value encountered for property \"query\"";
            checkMessage(e, message);
        }
    }

    @Test
    /**
     * Check that query is not blank
     */
    void testUcsBlankQuery() throws IOException {
           // query is " "
           try {
            getConnector("ucsCriterionBlankQuery");
            Assert.fail("Expected an IOException to be thrown.");
        } catch (IOException e) {
            final String message = "Invalid blank value encountered for property 'query'.";
            checkMessage(e, message);
        }
    }

    @Test
    /**
     * Checks that query is declared 
     *
     * @throws IOException
     */
    void testUcsNoName() throws IOException {
        // query is not declared
        try {
            getConnector("ucsCriterionNoQuery");
            Assert.fail("Expected an IOException to be thrown.");
        } catch (IOException e) {
            final String message = "Missing required creator property 'query' (index 2)";
            checkMessage(e, message);
        }
    }
}
