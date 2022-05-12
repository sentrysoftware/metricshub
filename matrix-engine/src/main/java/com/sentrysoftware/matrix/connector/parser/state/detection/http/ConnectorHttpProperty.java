package com.sentrysoftware.matrix.connector.parser.state.detection.http;

import com.sentrysoftware.matrix.connector.model.detection.criteria.http.HTTP;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ErrorMessageProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.detection.common.TypeProcessor;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ConnectorHttpProperty {

	private ConnectorHttpProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(
					new TypeProcessor(HTTP.class, HttpProcessor.HTTP_TYPE_VALUE),
					new ForceSerializationProcessor(HTTP.class, HttpProcessor.HTTP_TYPE_VALUE),
					new ExpectedResultProcessor(HTTP.class, HttpProcessor.HTTP_TYPE_VALUE),
					new ErrorMessageProcessor(HTTP.class, HttpProcessor.HTTP_TYPE_VALUE),
					new MethodProcessor(),
					new UrlProcessor(),
					new HeaderProcessor(),
					new BodyProcessor(),
					new ResultContentProcessor())
				.collect(Collectors.toSet());
	}
}
