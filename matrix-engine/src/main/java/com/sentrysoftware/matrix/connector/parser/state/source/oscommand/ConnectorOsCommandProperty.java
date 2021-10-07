package com.sentrysoftware.matrix.connector.parser.state.source.oscommand;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OSCommandSource;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ExcludeRegExpProcessor;
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
		return Stream.of(
						new TypeProcessor(OSCommandSource.class, OsCommandProcessor.OS_COMMAND_TYPE),
						new ForceSerializationProcessor(OSCommandSource.class, OsCommandProcessor.OS_COMMAND_TYPE),
						new CommandLineProcessor(),
						new ExecuteLocallyProcessor(),
						new ExcludeRegExpProcessor(OSCommandSource.class, OsCommandProcessor.OS_COMMAND_TYPE),
						new KeepOnlyRegExpProcessor(OSCommandSource.class, OsCommandProcessor.OS_COMMAND_TYPE),
						new RemoveFooterProcessor(OSCommandSource.class, OsCommandProcessor.OS_COMMAND_TYPE),
						new RemoveHeaderProcessor(OSCommandSource.class, OsCommandProcessor.OS_COMMAND_TYPE),
						new SelectColumnsProcessor(OSCommandSource.class, OsCommandProcessor.OS_COMMAND_TYPE),
						new SeparatorsProcessor(OSCommandSource.class, OsCommandProcessor.OS_COMMAND_TYPE),
						new TimeoutProcessor())
				.collect(Collectors.toSet());
	}
}
