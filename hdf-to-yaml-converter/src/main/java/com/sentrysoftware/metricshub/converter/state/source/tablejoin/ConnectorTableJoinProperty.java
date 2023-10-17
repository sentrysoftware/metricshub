package com.sentrysoftware.metricshub.converter.state.source.tablejoin;

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
public class ConnectorTableJoinProperty {

	private static final String HDF_TYPE_VALUE = "TableJoint";
	private static final String YAML_TYPE_VALUE = "tableJoin";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
			new SourceTypeProcessor(HDF_TYPE_VALUE, YAML_TYPE_VALUE),
			new ForceSerializationProcessor(),
			new ExecuteForEachEntryOfProcessor(),
			new EntryConcatMethodProcessor(),
			new EntryConcatStartProcessor(),
			new EntryConcatEndProcessor(),
			new LeftTableProcessor(),
			new RightTableProcessor(),
			new LeftKeyColumnProcessor(),
			new RightKeyColumnProcessor(),
			new KeyTypeProcessor(),
			new DefaultRightLineProcessor()
		);
	}
}
