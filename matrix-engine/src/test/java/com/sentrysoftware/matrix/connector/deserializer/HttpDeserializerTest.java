package com.sentrysoftware.matrix.connector.deserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.ResultContent;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Criterion;
import com.sentrysoftware.matrix.connector.model.identity.criterion.Http;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class HttpDeserializerTest {

  @Test
  /**
   * Checks input properties for Http detection criteria
   *
   * @throws IOException
   */
  void testDeserializeDoesntThrow() throws IOException {
    final ConnectorDeserializer deserializer = new ConnectorDeserializer();
    final Connector connector =
        deserializer.deserialize(new File("src/test/resources/test-files/connector/http.yaml"));

    List<Criterion> expected = new ArrayList<>();

    final String headerText =
        "Content-Type: application/json\n"
            + "Accept: application/json\n"
            + "Cookie: %{AUTHENTICATIONTOKEN}";

    Http http = new Http();
    http.setMethod("GET");
    http.setUrl("test");
    http.setHeader(headerText);
    http.setAuthenticationToken("test-auth-token");
    http.setBody("test-body");
    http.setResultContent(ResultContent.ALL);
    http.setExpectedResult("result");

    expected.add(http);

    assertNotNull(connector);
    assertEquals("connector", connector.getConnectorIdentity().getCompiledFilename());

    assertNotNull(connector.getConnectorIdentity().getDetection());
    assertEquals(expected, connector.getConnectorIdentity().getDetection().getCriteria());
  }
}
