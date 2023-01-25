package com.sentrysoftware.matrix.connector.deserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.ProductRequirements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class ProductRequirementsDeserializerTest {

  @Test
  /**
   * Checks input properties for product requirements detection criteria
   *
   * @throws IOException
   */
  void testDeserializeDoesntThrow() throws IOException {
    final ConnectorDeserializer deserializer = new ConnectorDeserializer();
    final Connector connector =
        deserializer.deserialize(new File("src/test/resources/test-files/connector/product-requirements.yaml"));

    List<Criterion> expected = new ArrayList<>();
    ProductRequirements test = new ProductRequirements();
    test.setEngineVersion("testengineversion");
    test.setKmVersion("testkmversion");

    expected.add(test);

    assertNotNull(connector);
    assertEquals("connector", connector.getConnectorIdentity().getCompiledFilename());

    assertNotNull(connector.getConnectorIdentity().getDetection());
    assertEquals(expected, connector.getConnectorIdentity().getDetection().getCriteria());
  }
}
