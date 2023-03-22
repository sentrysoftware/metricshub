package com.sentrysoftware.matrix.converter.state.source.ucs;

import java.util.Set;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.source.common.EntryConcatEndProcessor;
import com.sentrysoftware.matrix.converter.state.source.common.EntryConcatMethodProcessor;
import com.sentrysoftware.matrix.converter.state.source.common.EntryConcatStartProcessor;
import com.sentrysoftware.matrix.converter.state.source.common.ExecuteForEachEntryOfProcessor;
import com.sentrysoftware.matrix.converter.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.converter.state.source.common.TypeProcessor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorUcsProperty {

	private static final String HDF_TYPE_VALUE = "UCS";
	private static final String YAML_TYPE_VALUE = "ucs";

	public static Set<IConnectorStateConverter> getConnectorProperties() {

		return Set.of(
			new TypeProcessor(HDF_TYPE_VALUE, YAML_TYPE_VALUE),
			new ForceSerializationProcessor(),
			new ExecuteForEachEntryOfProcessor(),
			new EntryConcatMethodProcessor(),
			new EntryConcatStartProcessor(),
			new EntryConcatEndProcessor(),
			new QueryProcessor(),
			new ExcludeRegExpProcessor(),
			new KeepOnlyRegExpProcessor(),
			new SelectColumnsProcessor()
		);

	}
}