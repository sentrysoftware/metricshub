package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

public class ConnectorSnmpDetectionParser implements IConnectorStateParser {

    @Override
    public boolean detect(String key, String value, Connector connector) {

        return ConnectorSnmpProperty
                .getConnectorProperties()
                .stream()
                .anyMatch(connectorSnmpProperty -> connectorSnmpProperty.detect(key, value, connector));
    }

    @Override
    public void parse(String key, String value, Connector connector) {

        ConnectorSnmpProperty
                .getConnectorProperties()
                .stream()
                .filter(connectorSnmpProperty -> connectorSnmpProperty.detect(key, value, connector))
                .forEach(connectorSnmpProperty -> connectorSnmpProperty.parse(key, value, connector));
    }
}
