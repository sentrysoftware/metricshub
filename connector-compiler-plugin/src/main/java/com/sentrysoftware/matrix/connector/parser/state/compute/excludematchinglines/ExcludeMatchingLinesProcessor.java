package com.sentrysoftware.matrix.connector.parser.state.compute.excludematchinglines;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ExcludeMatchingLines;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class ExcludeMatchingLinesProcessor extends AbstractStateParser {

	protected static final String EXCLUDE_MATCHING_LINES_TYPE_VALUE = "ExcludeMatchingLines";

	@Override
	public Class<ExcludeMatchingLines> getType() {
		return ExcludeMatchingLines.class;
	}

	@Override
	public String getTypeValue() {
		return EXCLUDE_MATCHING_LINES_TYPE_VALUE;
	}
}
