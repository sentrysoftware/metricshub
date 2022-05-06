package com.sentrysoftware.matrix.connector.parser.state.source.common;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class AbstractExecuteForEachEntry extends AbstractStateParser {

	private final Class<? extends Source> type;
	private final String typeValue;

}
