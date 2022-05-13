package com.sentrysoftware.matrix.connector.parser.state.source.oscommand;

import static com.sentrysoftware.matrix.connector.parser.state.source.oscommand.OsCommandProcessor.OS_COMMAND_TYPE;

import java.util.Set;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OsCommandSource;
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
				new TypeProcessor(OsCommandSource.class, OS_COMMAND_TYPE),
				new ForceSerializationProcessor(OsCommandSource.class, OS_COMMAND_TYPE),
				new CommandLineProcessor(),
				new ExecuteLocallyProcessor(),
				new ExcludeRegExpProcessor(OsCommandSource.class, OS_COMMAND_TYPE),
				new KeepOnlyRegExpProcessor(OsCommandSource.class, OS_COMMAND_TYPE),
				new RemoveFooterProcessor(OsCommandSource.class, OS_COMMAND_TYPE),
				new RemoveHeaderProcessor(OsCommandSource.class, OS_COMMAND_TYPE),
				new SelectColumnsProcessor(OsCommandSource.class, OS_COMMAND_TYPE),
				new SeparatorsProcessor(OsCommandSource.class, OS_COMMAND_TYPE),
				new TimeoutProcessor(),
				new ExecuteForEachEntryOfProcessor(OsCommandSource.class, OS_COMMAND_TYPE),
				new EntryConcatMethodProcessor(OsCommandSource.class, OS_COMMAND_TYPE),
				new EntryConcatStartProcessor(OsCommandSource.class, OS_COMMAND_TYPE),
				new EntryConcatEndProcessor(OsCommandSource.class, OS_COMMAND_TYPE));
	}
}
