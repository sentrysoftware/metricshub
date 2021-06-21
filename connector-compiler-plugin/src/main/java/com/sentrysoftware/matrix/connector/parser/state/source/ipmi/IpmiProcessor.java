package com.sentrysoftware.matrix.connector.parser.state.source.ipmi;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ipmi.IPMI;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class IpmiProcessor extends AbstractStateParser {

	protected static final String IPMI_TYPE_VALUE = "IPMI";

	@Override
	public Class<IPMI> getType() {
		return IPMI.class;
	}

	@Override
	public String getTypeValue() {
		return IPMI_TYPE_VALUE;
	}
}
