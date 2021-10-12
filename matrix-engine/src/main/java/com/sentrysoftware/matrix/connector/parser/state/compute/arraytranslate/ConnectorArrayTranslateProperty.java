package com.sentrysoftware.matrix.connector.parser.state.compute.arraytranslate;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ArrayTranslate;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorArrayTranslateProperty {

	private ConnectorArrayTranslateProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(ArrayTranslate.class, ArrayTranslateProcessor.ARRAY_TRANSLATE_TYPE_VALUE),
				new ColumnProcessor(ArrayTranslate.class, ArrayTranslateProcessor.ARRAY_TRANSLATE_TYPE_VALUE),
				new TranslationTableProcessor(),
				new ArraySeparatorProcessor(),
				new ResultSeparatorProcessor())
			.collect(Collectors.toSet());
	}
}
