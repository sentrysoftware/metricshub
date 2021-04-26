package com.sentrysoftware.matrix.model.monitoring;

import java.util.Map;
import java.util.Set;

import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.model.monitor.Monitor;

public interface IHostMonitoring {

	Map<MonitorType, Map<String, Monitor>> getMonitors();

	Map<MonitorType, Map<String, Monitor>> getPreviousMonitors();

	Map<String, SourceTable> getSourceTables();

	void setMonitors(Map<MonitorType, Map<String, Monitor>> monitors);

	void setPreviousMonitors(Map<MonitorType, Map<String, Monitor>> previousMonitors);

	void clear();

	void backup();

	void addMonitor(Monitor monitor);

	void removeMonitor(Monitor monitor);
	
	Map<String, Monitor> selectFromType(MonitorType monitorType);

	Map<MonitorType, Map<String, Monitor>> selectFromTypes(MonitorType... monitorTypes);

	Set<Monitor> selectChildren(String parentIdentifier, MonitorType childrenMonitorType);

	String toJsonString();

	void addSourceTable(String key, SourceTable sourceTable);

	SourceTable getSourceTableByKey(String key);

	void addMonitor(Monitor monitor, String id, String connectorName, MonitorType monitorType,
			String attachedToDeviceId, String attachedToDeviceType);
}
