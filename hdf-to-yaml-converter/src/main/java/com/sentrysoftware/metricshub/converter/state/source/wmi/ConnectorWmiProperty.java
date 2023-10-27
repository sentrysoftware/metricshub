package com.sentrysoftware.metricshub.converter.state.source.wmi;

import com.sentrysoftware.metricshub.converter.state.IConnectorStateConverter;
import com.sentrysoftware.metricshub.converter.state.source.common.EntryConcatEndProcessor;
import com.sentrysoftware.metricshub.converter.state.source.common.EntryConcatMethodProcessor;
import com.sentrysoftware.metricshub.converter.state.source.common.EntryConcatStartProcessor;
import com.sentrysoftware.metricshub.converter.state.source.common.ExecuteForEachEntryOfProcessor;
import com.sentrysoftware.metricshub.converter.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.metricshub.converter.state.source.common.SourceTypeProcessor;
import com.sentrysoftware.metricshub.converter.state.source.common.WbemNamespaceProcessor;
import com.sentrysoftware.metricshub.converter.state.source.common.WbemQueryProcessor;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorWmiProperty {

	private static final String WMI_HDF_TYPE_VALUE = "WMI";
	private static final String WMI_YAML_TYPE_VALUE = "wmi";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
			new SourceTypeProcessor(WMI_HDF_TYPE_VALUE, WMI_YAML_TYPE_VALUE),
			new ForceSerializationProcessor(),
			new WbemQueryProcessor(),
			new WbemNamespaceProcessor(),
			new ExecuteForEachEntryOfProcessor(),
			new EntryConcatMethodProcessor(),
			new EntryConcatStartProcessor(),
			new EntryConcatEndProcessor()
		);
	}
}