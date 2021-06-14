package com.sentrysoftware.matrix.connector.parser.state.source.http;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HTTPSource;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;

public class ConnectorHttpProperty {
	private ConnectorHttpProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Stream
				.of(
						new TypeProcessor(HTTPSource.class, HttpProcessor.HTTP_TYPE_VALUE),
						new MethodProcessor(),
						new UrlProcessor(),
						new HeaderProcessor(),
						new BodyProcessor(),
						new AuthenticationTokenProcessor(),
						new ExecuteForEachEntryOfProcessor(),
						new ResultContentProcessor(),
						new EntryConcatMethodProcessor(),
						new EntryConcatStartProcessor(),
						new EntryConcatEndProcessor())
				.collect(Collectors.toSet());
	}
}
