package com.sentrysoftware.metricshub.converter.state.source.snmptable;

import com.sentrysoftware.metricshub.converter.state.IConnectorStateConverter;
import com.sentrysoftware.metricshub.converter.state.source.common.EntryConcatEndProcessor;
import com.sentrysoftware.metricshub.converter.state.source.common.EntryConcatMethodProcessor;
import com.sentrysoftware.metricshub.converter.state.source.common.EntryConcatStartProcessor;
import com.sentrysoftware.metricshub.converter.state.source.common.ExecuteForEachEntryOfProcessor;
import com.sentrysoftware.metricshub.converter.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.metricshub.converter.state.source.common.SourceTypeProcessor;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorSnmpTableProperty {

	private static final String SNMP_HDF_TYPE_VALUE = "SnmpTable";
	private static final String SNMP_YAML_TYPE_VALUE = "snmpTable";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
			new SourceTypeProcessor(SNMP_HDF_TYPE_VALUE, SNMP_YAML_TYPE_VALUE),
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
