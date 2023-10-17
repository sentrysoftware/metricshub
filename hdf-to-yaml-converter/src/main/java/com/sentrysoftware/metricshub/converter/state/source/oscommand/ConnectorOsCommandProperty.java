package com.sentrysoftware.metricshub.converter.state.source.oscommand;

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
public class ConnectorOsCommandProperty {

	private static final String OSCOMMAND_HDF_TYPE_VALUE = "OsCommand";
	private static final String OSCOMMAND_YAML_TYPE_VALUE = "osCommand";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
			new SourceTypeProcessor(OSCOMMAND_HDF_TYPE_VALUE, OSCOMMAND_YAML_TYPE_VALUE),
			new ForceSerializationProcessor(),
			new CommandLineProcessor(),
			new ExecuteLocallyProcessor(),
			new ExcludeRegExpProcessor(),
			new KeepOnlyRegExpProcessor(),
			new RemoveFooterProcessor(),
			new RemoveHeaderProcessor(),
			new SelectColumnsProcessor(),
			new SeparatorsProcessor(),
			new TimeoutProcessor(),
			new ExecuteForEachEntryOfProcessor(),
			new EntryConcatMethodProcessor(),
			new EntryConcatStartProcessor(),
			new EntryConcatEndProcessor()
		);
	}
}
