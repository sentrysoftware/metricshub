package com.sentrysoftware.matrix.connector.parser.state.source.wbem;

import java.util.Set;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem.WbemSource;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatEndProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatMethodProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatStartProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ExecuteForEachEntryOfProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.WbemNamespaceProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.WbemQueryProcessor;

public class ConnectorWbemProperty {

	private static final String WBEM_TYPE_VALUE = "WBEM";

	private ConnectorWbemProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Set.of(
				new TypeProcessor(WbemSource.class, WBEM_TYPE_VALUE),
				new ForceSerializationProcessor(WbemSource.class, WBEM_TYPE_VALUE),
				new WbemQueryProcessor(WbemSource.class, WBEM_TYPE_VALUE),
				new WbemNamespaceProcessor(WbemSource.class, WBEM_TYPE_VALUE),
				new ExecuteForEachEntryOfProcessor(WbemSource.class, WBEM_TYPE_VALUE),
				new EntryConcatMethodProcessor(WbemSource.class, WBEM_TYPE_VALUE),
				new EntryConcatStartProcessor(WbemSource.class, WBEM_TYPE_VALUE),
				new EntryConcatEndProcessor(WbemSource.class, WBEM_TYPE_VALUE));

	}
}
