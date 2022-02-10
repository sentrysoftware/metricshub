package com.sentrysoftware.matrix.connector.parser.state.source.oscommand;

import static com.sentrysoftware.matrix.connector.parser.state.source.oscommand.OsCommandProcessor.OS_COMMAND_TYPE;

import java.util.Set;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OSCommandSource;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatEndProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatMethodProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatStartProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ExcludeRegExpProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ExecuteForEachEntryOfProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.KeepOnlyRegExpProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.RemoveFooterProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.RemoveHeaderProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.SelectColumnsProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.SeparatorsProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorOsCommandProperty {

	public static Set<IConnectorStateParser> getConnectorProperties() {
		return Set.of(
				new TypeProcessor(OSCommandSource.class, OS_COMMAND_TYPE),
				new ForceSerializationProcessor(OSCommandSource.class, OS_COMMAND_TYPE),
				new CommandLineProcessor(),
				new ExecuteLocallyProcessor(),
				new ExcludeRegExpProcessor(OSCommandSource.class, OS_COMMAND_TYPE),
				new KeepOnlyRegExpProcessor(OSCommandSource.class, OS_COMMAND_TYPE),
				new RemoveFooterProcessor(OSCommandSource.class, OS_COMMAND_TYPE),
				new RemoveHeaderProcessor(OSCommandSource.class, OS_COMMAND_TYPE),
				new SelectColumnsProcessor(OSCommandSource.class, OS_COMMAND_TYPE),
				new SeparatorsProcessor(OSCommandSource.class, OS_COMMAND_TYPE),
				new TimeoutProcessor(),
				new ExecuteForEachEntryOfProcessor(OSCommandSource.class, OS_COMMAND_TYPE),
				new EntryConcatMethodProcessor(OSCommandSource.class, OS_COMMAND_TYPE),
				new EntryConcatStartProcessor(OSCommandSource.class, OS_COMMAND_TYPE),
				new EntryConcatEndProcessor(OSCommandSource.class, OS_COMMAND_TYPE));
	}
}
