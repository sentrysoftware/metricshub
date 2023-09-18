package com.sentrysoftware.matrix.converter.state.source.snmptable;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.source.common.EntryConcatEndProcessor;
import com.sentrysoftware.matrix.converter.state.source.common.EntryConcatMethodProcessor;
import com.sentrysoftware.matrix.converter.state.source.common.EntryConcatStartProcessor;
import com.sentrysoftware.matrix.converter.state.source.common.ExecuteForEachEntryOfProcessor;
import com.sentrysoftware.matrix.converter.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.converter.state.source.common.TypeProcessor;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorSnmpTableProperty {

	private static final String SNMP_HDF_TYPE_VALUE = "SnmpTable";
	private static final String SNMP_YAML_TYPE_VALUE = "snmpTable";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
			new TypeProcessor(SNMP_HDF_TYPE_VALUE, SNMP_YAML_TYPE_VALUE),
			new ForceSerializationProcessor(),
			new SnmpTableOidProcessor(),
			new SelectColumnsProcessor(),
			new ExecuteForEachEntryOfProcessor(),
			new EntryConcatMethodProcessor(),
			new EntryConcatStartProcessor(),
			new EntryConcatEndProcessor()
		);
	}
}
