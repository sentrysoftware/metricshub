package com.sentrysoftware.matrix.connector.parser.state.compute.keepcolumns;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepColumns;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorKeepColumnsProperty {

	private ConnectorKeepColumnsProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(KeepColumns.class, KeepColumnsProcessor.KEEP_COLUMNS_TYPE_VALUE),
				new ColumnNumbersProcessor())
			.collect(Collectors.toSet());
	}
}
