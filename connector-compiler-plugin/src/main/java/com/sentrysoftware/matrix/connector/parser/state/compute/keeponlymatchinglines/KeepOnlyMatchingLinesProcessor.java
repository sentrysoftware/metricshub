package com.sentrysoftware.matrix.connector.parser.state.compute.keeponlymatchinglines;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepOnlyMatchingLines;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class KeepOnlyMatchingLinesProcessor extends AbstractStateParser {

	protected static final String KEEP_ONLY_MATCHING_LINES_TYPE_VALUE = "KeepOnlyMatchingLines";

	@Override
	public Class<KeepOnlyMatchingLines> getType() {
		return KeepOnlyMatchingLines.class;
	}

	@Override
	public String getTypeValue() {
		return KEEP_ONLY_MATCHING_LINES_TYPE_VALUE;
	}
}
