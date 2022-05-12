package com.sentrysoftware.matrix.connector.parser.state.source.snmptable;

import static com.sentrysoftware.matrix.connector.parser.state.source.snmptable.SnmpTableProcessor.SNMP_TABLE_TYPE_VALUE;

import java.util.Set;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SnmpGetTableSource;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatEndProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatMethodProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatStartProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ExecuteForEachEntryOfProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;

public class ConnectorSnmpTableProperty {

	private ConnectorSnmpTableProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Set.of(
				new TypeProcessor(SnmpGetTableSource.class, SNMP_TABLE_TYPE_VALUE),
				new ForceSerializationProcessor(SnmpGetTableSource.class, SNMP_TABLE_TYPE_VALUE),
				new SnmpTableOidProcessor(),
				new SnmpTableSelectColumnsProcessor(),
				new ExecuteForEachEntryOfProcessor(SnmpGetTableSource.class, SNMP_TABLE_TYPE_VALUE),
				new EntryConcatMethodProcessor(SnmpGetTableSource.class, SNMP_TABLE_TYPE_VALUE),
				new EntryConcatStartProcessor(SnmpGetTableSource.class, SNMP_TABLE_TYPE_VALUE),
				new EntryConcatEndProcessor(SnmpGetTableSource.class, SNMP_TABLE_TYPE_VALUE));
	}
}
