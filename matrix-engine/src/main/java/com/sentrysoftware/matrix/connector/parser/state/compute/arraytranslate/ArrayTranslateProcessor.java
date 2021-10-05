package com.sentrysoftware.matrix.connector.parser.state.compute.arraytranslate;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ArrayTranslate;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class ArrayTranslateProcessor extends AbstractStateParser {

	protected static final String ARRAY_TRANSLATE_TYPE_VALUE = "ArrayTranslate";

	@Override
	public Class<ArrayTranslate> getType() {
		return ArrayTranslate.class;
	}

	@Override
	public String getTypeValue() {
		return ARRAY_TRANSLATE_TYPE_VALUE;
	}
}
