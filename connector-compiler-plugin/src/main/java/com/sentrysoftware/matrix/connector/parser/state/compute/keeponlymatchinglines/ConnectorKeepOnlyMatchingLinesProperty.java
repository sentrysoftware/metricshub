package com.sentrysoftware.matrix.connector.parser.state.compute.keeponlymatchinglines;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepOnlyMatchingLines;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorKeepOnlyMatchingLinesProperty {

	private ConnectorKeepOnlyMatchingLinesProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(KeepOnlyMatchingLines.class,
					KeepOnlyMatchingLinesProcessor.KEEP_ONLY_MATCHING_LINES_TYPE_VALUE),
				new ColumnProcessor(KeepOnlyMatchingLines.class,
					KeepOnlyMatchingLinesProcessor.KEEP_ONLY_MATCHING_LINES_TYPE_VALUE),
				new ValueListProcessor(),
				new RegexpProcessor())
			.collect(Collectors.toSet());
	}
}