package com.sentrysoftware.matrix.connector.parser.state.source.tablejoin;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorTableJoinProperty {

	private ConnectorTableJoinProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(TableJoinSource.class, TableJoinProcessor.TABLE_JOIN_TYPE_VALUE),
				new ForceSerializationProcessor(TableJoinSource.class, TableJoinProcessor.TABLE_JOIN_TYPE_VALUE),
				new LeftTableProcessor(),
				new RightTableProcessor(),
				new LeftKeyColumnProcessor(),
				new RightKeyColumnProcessor(),
				new DefaultRightLineProcessor())
			.collect(Collectors.toSet());
	}
}
