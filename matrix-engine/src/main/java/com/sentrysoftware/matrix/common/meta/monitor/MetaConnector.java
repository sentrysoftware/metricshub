package com.sentrysoftware.matrix.common.meta.monitor;

import java.util.Collections;
import java.util.Map;

import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;

public class MetaConnector implements IMetaMonitor {

	@Override
	public void accept(IMonitorVisitor monitorVisitor) {
		monitorVisitor.visit(this);

	}

	@Override
	public Map<String, MetaParameter> getMetaParameters() {
		return Collections.emptyMap();
	}

	@Override
	public MonitorType getMonitorType() {
		return MonitorType.CONNECTOR;
	}
}