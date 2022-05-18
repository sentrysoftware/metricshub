package com.sentrysoftware.matrix.connector.parser.state.source.snmpget;

import static com.sentrysoftware.matrix.connector.parser.state.source.snmpget.SnmpGetProcessor.SNMP_GET_TYPE_VALUE;

import java.util.Set;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetSource;
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
				new TypeProcessor(SnmpGetSource.class, SNMP_GET_TYPE_VALUE),
				new ForceSerializationProcessor(SnmpGetSource.class, SNMP_GET_TYPE_VALUE),
				new SnmpGetOidProcessor(),
				new ExecuteForEachEntryOfProcessor(SnmpGetSource.class, SNMP_GET_TYPE_VALUE),
				new EntryConcatMethodProcessor(SnmpGetSource.class, SNMP_GET_TYPE_VALUE),
				new EntryConcatStartProcessor(SnmpGetSource.class, SNMP_GET_TYPE_VALUE),
				new EntryConcatEndProcessor(SnmpGetSource.class, SNMP_GET_TYPE_VALUE));
	}
}
