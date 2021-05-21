package com.sentrysoftware.matrix.connector.parser.state.compute.duplicatecolumn;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.DuplicateColumn;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorDuplicateColumnProperty {

	private static final String DUPLICATE_COLUMN_TYPE_VALUE = "DuplicateColumn";

	private ConnectorDuplicateColumnProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(DuplicateColumn.class, DUPLICATE_COLUMN_TYPE_VALUE),
				new ColumnProcessor(DuplicateColumn.class, DUPLICATE_COLUMN_TYPE_VALUE))
			.collect(Collectors.toSet());
	}
}
