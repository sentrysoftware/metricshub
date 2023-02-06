package com.sentrysoftware.matrix.connector.deserializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.exc.InvalidNullException;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Service;

class CriteriaServiceDeserializerTest extends DeserializerTest {

    @Override
    public String getResourcePath() {
        return "src/test/resources/test-files/connector/detection/criteria/service/";
    }

    @Test
    void testDeserializeService() throws IOException {
        final String testResource = "serviceCriterion";
        final Connector service = getConnector(testResource);
        List<Criterion> expected = new ArrayList<>();
        expected.add(new Service("service", false, "TWGIPC"));
        compareCriterion(testResource, service, expected);
    }

    @Test
    /**
     * Checks that null name is rejected
     *
     * @throws IOException
     */
    void testServiceNullName() throws IOException {
        // name is null
        try {
            getConnector("serviceCriterionNullName");
            Assert.fail("Expected an InvalidNullException to be thrown.");
        } catch (InvalidNullException e) {
            final String message = "Invalid `null` value encountered for property \"name\"";
            checkMessage(e, message);
        }
    }

    @Test
    /**
     * Checks that blank name is regected
     *
     * @throws IOException
     */
    void testServiceBlankName() throws IOException {
        // name is " "
        try {
            getConnector("serviceCriterionBlankName");
            Assert.fail("Expected an IOException to be thrown.");
        } catch (IOException e) {
            final String message = "Invalid blank value encountered for property 'name'.";
            checkMessage(e, message);
        }
    }

    @Test
    /**
     * Checks that name is declared 
     *
     * @throws IOException
     */
    void testServiceNoName() throws IOException {
        // name is not declared
        try {
            getConnector("serviceCriterionNoName");
            Assert.fail("Expected an IOException to be thrown.");
        } catch (IOException e) {
            final String message = "Missing required creator property 'name' (index 2)";
            checkMessage(e, message);
        }
    }
}
