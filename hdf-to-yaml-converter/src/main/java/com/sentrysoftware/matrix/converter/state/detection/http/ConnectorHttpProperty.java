package com.sentrysoftware.matrix.converter.state.detection.http;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.detection.common.ErrorMessageProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.TypeProcessor;

public class ConnectorHttpProperty {

	private ConnectorHttpProperty() {}

	public static Set<IConnectorStateConverter> getConnectorProperties() {

		return Stream
				.of(
					new TypeProcessor(HttpProcessor.HTTP_TYPE_VALUE),
					new ForceSerializationProcessor(HttpProcessor.HTTP_TYPE_VALUE),
					new ExpectedResultProcessor(HttpProcessor.HTTP_TYPE_VALUE),
					new ErrorMessageProcessor(HttpProcessor.HTTP_TYPE_VALUE),
					new MethodProcessor(),
					new UrlProcessor(),
					new HeaderProcessor(),
					new BodyProcessor(),
					new ResultContentProcessor())
				.collect(Collectors.toSet());
	}
}
