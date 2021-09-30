package com.sentrysoftware.matrix.connector.parser.state.compute.translate;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Translate;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class TranslateProcessor extends AbstractStateParser {

	protected static final String TRANSLATE_TYPE_VALUE = "Translate";

	@Override
	public Class<Translate> getType() {
		return Translate.class;
	}

	@Override
	public String getTypeValue() {
		return TRANSLATE_TYPE_VALUE;
	}
}
