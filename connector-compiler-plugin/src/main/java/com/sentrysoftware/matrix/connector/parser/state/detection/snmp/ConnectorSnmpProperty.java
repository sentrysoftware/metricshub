package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public enum ConnectorSnmpProperty implements IConnectorStateParser {

    OID(new OidProcessor()),
    EXPECTED_RESULT(new ExpectedResultProcessor()),
    FORCE_SERIALIZATION(new ForceSerializationProcessor());

    private final IConnectorStateParser connectorStateProcessor;

    public boolean detect(final String key, final String value, final Connector connector) {

        return connectorStateProcessor.detect(key, value, connector);
    }

    public void parse(final String key, final String value, final Connector connector) {

        connectorStateProcessor.parse(key, value, connector);
    }

    public static Set<ConnectorSnmpProperty> getConnectorProperties() {

        return Arrays.stream(ConnectorSnmpProperty.values()).collect(Collectors.toSet());
    }
}
