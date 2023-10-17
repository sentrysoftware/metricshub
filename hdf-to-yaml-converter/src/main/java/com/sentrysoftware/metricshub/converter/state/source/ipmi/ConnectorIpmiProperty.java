package com.sentrysoftware.metricshub.converter.state.source.ipmi;

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
public class ConnectorIpmiProperty {

	private static final String IPMI_HDF_TYPE_VALUE = "IPMI";
	private static final String IPMI_YAML_TYPE_VALUE = "ipmi";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
			new SourceTypeProcessor(IPMI_HDF_TYPE_VALUE, IPMI_YAML_TYPE_VALUE),
			new ForceSerializationProcessor(),
			new ExecuteForEachEntryOfProcessor(),
			new EntryConcatMethodProcessor(),
			new EntryConcatStartProcessor(),
			new EntryConcatEndProcessor()
		);
	}
}
