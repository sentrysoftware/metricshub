package com.sentrysoftware.matrix.converter.state.source.ipmi;

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
public class ConnectorIpmiProperty {

	private static final String IPMI_HDF_TYPE_VALUE = "IPMI";
	private static final String IPMI_YAML_TYPE_VALUE = "ipmi";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
			new TypeProcessor(IPMI_HDF_TYPE_VALUE, IPMI_YAML_TYPE_VALUE),
			new ForceSerializationProcessor(),
			new ExecuteForEachEntryOfProcessor(),
			new EntryConcatMethodProcessor(),
			new EntryConcatStartProcessor(),
			new EntryConcatEndProcessor()
		);
	}
}
