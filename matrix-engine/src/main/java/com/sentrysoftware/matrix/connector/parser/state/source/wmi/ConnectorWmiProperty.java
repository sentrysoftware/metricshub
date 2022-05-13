package com.sentrysoftware.matrix.connector.parser.state.source.wmi;

import java.util.Set;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wmi.WmiSource;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatEndProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatMethodProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatStartProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ExecuteForEachEntryOfProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.TypeProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.WbemNamespaceProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.WbemQueryProcessor;

public class ConnectorWmiProperty {

	private static final String WMI_TYPE_VALUE = "WMI";

	private ConnectorWmiProperty() {}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Set.of(
				new TypeProcessor(WmiSource.class, WMI_TYPE_VALUE),
				new ForceSerializationProcessor(WmiSource.class, WMI_TYPE_VALUE),
				new WbemQueryProcessor(WmiSource.class, WMI_TYPE_VALUE),
				new WbemNamespaceProcessor(WmiSource.class, WMI_TYPE_VALUE),
				new ExecuteForEachEntryOfProcessor(WmiSource.class, WMI_TYPE_VALUE),
				new EntryConcatMethodProcessor(WmiSource.class, WMI_TYPE_VALUE),
				new EntryConcatStartProcessor(WmiSource.class, WMI_TYPE_VALUE),
				new EntryConcatEndProcessor(WmiSource.class, WMI_TYPE_VALUE));
	}
}
