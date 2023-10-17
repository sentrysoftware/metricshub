package com.sentrysoftware.metricshub.converter.state.source.wbem;

import com.sentrysoftware.metricshub.converter.state.IConnectorStateConverter;
import com.sentrysoftware.metricshub.converter.state.source.common.EntryConcatEndProcessor;
import com.sentrysoftware.metricshub.converter.state.source.common.EntryConcatMethodProcessor;
import com.sentrysoftware.metricshub.converter.state.source.common.EntryConcatStartProcessor;
import com.sentrysoftware.metricshub.converter.state.source.common.ExecuteForEachEntryOfProcessor;
import com.sentrysoftware.metricshub.converter.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.metricshub.converter.state.source.common.TypeProcessor;
import com.sentrysoftware.metricshub.converter.state.source.common.WbemNamespaceProcessor;
import com.sentrysoftware.metricshub.converter.state.source.common.WbemQueryProcessor;
import java.util.Set;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorWbemProperty {

	private static final String WBEM_HDF_TYPE_VALUE = "WBEM";
	private static final String WBEM_YAML_TYPE_VALUE = "wbem";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
			new TypeProcessor(WBEM_HDF_TYPE_VALUE, WBEM_YAML_TYPE_VALUE),
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
