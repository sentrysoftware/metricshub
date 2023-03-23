package com.sentrysoftware.matrix.converter.state.source.oscommand;

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
public class ConnectorOsCommandProperty {

	private static final String OSCOMMAND_HDF_TYPE_VALUE = "OsCommand";
	private static final String OSCOMMAND_YAML_TYPE_VALUE = "osCommand";

	public static Set<IConnectorStateConverter> getConnectorProperties() {
		return Set.of(
			new TypeProcessor(OSCOMMAND_HDF_TYPE_VALUE, OSCOMMAND_YAML_TYPE_VALUE),
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
