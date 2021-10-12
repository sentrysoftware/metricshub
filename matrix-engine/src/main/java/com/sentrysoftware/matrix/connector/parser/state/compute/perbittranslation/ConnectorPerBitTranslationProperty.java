package com.sentrysoftware.matrix.connector.parser.state.compute.perbittranslation;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.PerBitTranslation;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.ColumnProcessor;
import com.sentrysoftware.matrix.connector.parser.state.compute.common.TypeProcessor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorPerBitTranslationProperty {

	private ConnectorPerBitTranslationProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
			.of(
				new TypeProcessor(PerBitTranslation.class, PerBitTranslationProcessor.PER_BIT_TRANSLATION_TYPE_VALUE),
				new ColumnProcessor(PerBitTranslation.class, PerBitTranslationProcessor.PER_BIT_TRANSLATION_TYPE_VALUE),
				new BitListProcessor(),
				new BitTranslationTableProcessor())
			.collect(Collectors.toSet());
	}
}
