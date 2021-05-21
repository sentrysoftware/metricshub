package com.sentrysoftware.matrix.connector.parser.state.compute.excludematchinglines;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ExcludeMatchingLines;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorExcludeMatchingLinesProperty {

	private ConnectorExcludeMatchingLinesProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(ExcludeMatchingLines.class, ExcludeMatchingLinesProcessor.EXCLUDE_MATCHING_LINES_TYPE_VALUE),
				new ColumnProcessor(ExcludeMatchingLines.class, ExcludeMatchingLinesProcessor.EXCLUDE_MATCHING_LINES_TYPE_VALUE),
				new ValueListProcessor(),
				new RegexpProcessor())
			.collect(Collectors.toSet());
	}
}