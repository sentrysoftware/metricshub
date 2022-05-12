package com.sentrysoftware.matrix.connector.parser.state.detection.ipmi;

import com.sentrysoftware.matrix.connector.model.detection.criteria.ipmi.Ipmi;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class IpmiProcessor extends AbstractStateParser {
	protected static final String IPMI_TYPE_VALUE = "IPMI";

	@Override
	public Class<Ipmi> getType() {
		return Ipmi.class;
	}

	@Override
	public String getTypeValue() {
		return IPMI_TYPE_VALUE;
	}
}
