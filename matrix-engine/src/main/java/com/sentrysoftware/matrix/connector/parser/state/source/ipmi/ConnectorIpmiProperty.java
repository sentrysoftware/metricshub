package com.sentrysoftware.matrix.connector.parser.state.source.ipmi;

import static com.sentrysoftware.matrix.connector.parser.state.source.ipmi.IpmiTypeProcessor.IPMI_TYPE_VALUE;

import java.util.Set;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ipmi.Ipmi;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatEndProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatMethodProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.EntryConcatStartProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ExecuteForEachEntryOfProcessor;
import com.sentrysoftware.matrix.connector.parser.state.source.common.ForceSerializationProcessor;

public class ConnectorIpmiProperty {

	private ConnectorIpmiProperty() {
	}

	public static Set<IConnectorStateParser> getConnectorProperties() {

		return Set.of(
				new IpmiTypeProcessor(Ipmi.class, IPMI_TYPE_VALUE),
				new ForceSerializationProcessor(Ipmi.class, IPMI_TYPE_VALUE),
				new ExecuteForEachEntryOfProcessor(Ipmi.class, IPMI_TYPE_VALUE),
				new EntryConcatMethodProcessor(Ipmi.class, IPMI_TYPE_VALUE),
				new EntryConcatStartProcessor(Ipmi.class, IPMI_TYPE_VALUE),
				new EntryConcatEndProcessor(Ipmi.class, IPMI_TYPE_VALUE));

	}
}
