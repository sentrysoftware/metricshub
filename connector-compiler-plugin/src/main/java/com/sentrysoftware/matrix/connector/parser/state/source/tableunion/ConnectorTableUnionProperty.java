package com.sentrysoftware.matrix.connector.parser.state.source.tableunion;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tableunion.TableUnionSource;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorTableUnionProperty {

	private ConnectorTableUnionProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(TableUnionSource.class, TableUnionProcessor.TABLE_UNION_TYPE_VALUE),
				new ForceSerializationProcessor(TableUnionSource.class, TableUnionProcessor.TABLE_UNION_TYPE_VALUE),
				new TableProcessor())
			.collect(Collectors.toSet());
	}
}
