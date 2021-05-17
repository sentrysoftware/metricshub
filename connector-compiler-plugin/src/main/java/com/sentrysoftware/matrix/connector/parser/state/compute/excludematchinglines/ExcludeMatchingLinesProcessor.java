package com.sentrysoftware.matrix.connector.parser.state.compute.excludematchinglines;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ExcludeMatchingLines;
import com.sentrysoftware.matrix.connector.parser.state.compute.AbstractComputeParser;

public abstract class ExcludeMatchingLinesProcessor extends AbstractComputeParser {

	protected static final String EXCLUDE_MATCHING_LINES_TYPE_VALUE = "ExcludeMatchingLines";

	@Override
	public Class<TypeProcessor> getTypeProcessor() {
		return TypeProcessor.class;
	}

	@Override
	public Class<ExcludeMatchingLines> getComputeType() {
		return ExcludeMatchingLines.class;
	}

	@Override
	public String getTypeValue() {
		return EXCLUDE_MATCHING_LINES_TYPE_VALUE;
	}
}
