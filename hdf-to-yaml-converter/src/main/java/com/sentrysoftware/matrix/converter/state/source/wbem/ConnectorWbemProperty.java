package com.sentrysoftware.matrix.converter.state.source.wbem;

import java.util.Set;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.source.common.EntryConcatEndProcessor;
import com.sentrysoftware.matrix.converter.state.source.common.EntryConcatMethodProcessor;
import com.sentrysoftware.matrix.converter.state.source.common.EntryConcatStartProcessor;
import com.sentrysoftware.matrix.converter.state.source.common.ExecuteForEachEntryOfProcessor;
import com.sentrysoftware.matrix.converter.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.converter.state.source.common.TypeProcessor;
import com.sentrysoftware.matrix.converter.state.source.common.WbemNamespaceProcessor;
import com.sentrysoftware.matrix.converter.state.source.common.WbemQueryProcessor;

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