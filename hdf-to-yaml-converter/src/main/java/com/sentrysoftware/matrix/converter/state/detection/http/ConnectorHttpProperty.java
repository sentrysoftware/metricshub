package com.sentrysoftware.matrix.converter.state.detection.http;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.converter.state.IConnectorStateConverter;
import com.sentrysoftware.matrix.converter.state.detection.common.ErrorMessageProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.ExpectedResultProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.converter.state.detection.common.TypeProcessor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConnectorHttpProperty {

	public static final String HTTP_HDF_TYPE_VALUE = "HTTP";
	public static final String HTTP_YAML_TYPE_VALUE = "http";

	public static Set<IConnectorStateConverter> getConnectorProperties() {

		return Stream.of(
			new TypeProcessor(HTTP_HDF_TYPE_VALUE, HTTP_YAML_TYPE_VALUE),
			new ForceSerializationProcessor(),
			new ExpectedResultProcessor(),
			new ErrorMessageProcessor(),
			new MethodProcessor(),
			new UrlProcessor(),
			new HeaderProcessor(),
			new BodyProcessor(),
			new ResultContentProcessor(),
			new AuthenticationTokenProcessor()
		)
		.collect(Collectors.toSet());
	}
}
