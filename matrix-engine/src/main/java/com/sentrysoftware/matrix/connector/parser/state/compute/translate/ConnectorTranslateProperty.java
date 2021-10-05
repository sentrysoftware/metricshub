package com.sentrysoftware.matrix.connector.parser.state.compute.translate;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Translate;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorTranslateProperty {

	private ConnectorTranslateProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(Translate.class, TranslateProcessor.TRANSLATE_TYPE_VALUE),
				new ColumnProcessor(Translate.class, TranslateProcessor.TRANSLATE_TYPE_VALUE),
				new TranslationTableProcessor())
			.collect(Collectors.toSet());
	}
}
