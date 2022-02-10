package com.sentrysoftware.matrix.connector.parser.state.source.snmpget;

import static com.sentrysoftware.matrix.connector.parser.state.source.snmpget.SnmpGetProcessor.SNMP_GET_TYPE_VALUE;

import java.util.Set;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetSource;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatEndProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatMethodProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatStartProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ExecuteForEachEntryOfProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;

public class ConnectorSnmpGetProperty {

	private ConnectorSnmpGetProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Set.of(
				new TypeProcessor(SNMPGetSource.class, SNMP_GET_TYPE_VALUE),
				new ForceSerializationProcessor(SNMPGetSource.class, SNMP_GET_TYPE_VALUE),
				new SnmpGetOidProcessor(),
				new ExecuteForEachEntryOfProcessor(SNMPGetSource.class, SNMP_GET_TYPE_VALUE),
				new EntryConcatMethodProcessor(SNMPGetSource.class, SNMP_GET_TYPE_VALUE),
				new EntryConcatStartProcessor(SNMPGetSource.class, SNMP_GET_TYPE_VALUE),
				new EntryConcatEndProcessor(SNMPGetSource.class, SNMP_GET_TYPE_VALUE));
	}
}
