package com.sentrysoftware.matrix.connector.parser.state.source.oscommand;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OSCommandSource;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;
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
						new ExcludeRegExpProcessor(),
						new KeepOnlyRegExpProcessor(),
						new RemoveFooterProcessor(),
						new RemoveHeaderProcessor(),
						new SelectColumnsProcessor(),
						new SeparatorsProcessor(),
						new TimeoutProcessor())
				.collect(Collectors.toSet());
	}
}
