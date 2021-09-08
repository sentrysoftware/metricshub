package com.sentrysoftware.matrix.model.monitoring;

import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.EngineResult;
import com.sentrysoftware.matrix.engine.strategy.IStrategy;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.model.monitor.Monitor;

import java.util.Map;
import java.util.Set;

public interface IHostMonitoring {

	Map<MonitorType, Map<String, Monitor>> getMonitors();

	Map<MonitorType, Map<String, Monitor>> getPreviousMonitors();

	Map<String, SourceTable> getSourceTables();

	void setMonitors(Map<MonitorType, Map<String, Monitor>> monitors);

	void setPreviousMonitors(Map<MonitorType, Map<String, Monitor>> previousMonitors);

	void clearCurrent();

	void clearPrevious();

	void backup();

	void addMonitor(Monitor monitor);

	void removeMonitor(Monitor monitor);

	Map<String, Monitor> selectFromType(MonitorType monitorType);

	Map<MonitorType, Map<String, Monitor>> selectFromTypes(MonitorType... monitorTypes);

	Set<Monitor> selectChildren(String parentIdentifier, MonitorType childrenMonitorType);

	Monitor findById(String monitorIdentifier);

	String toJson();

	void addSourceTable(String key, SourceTable sourceTable);

	SourceTable getSourceTableByKey(String key);

	void addMonitor(Monitor monitor, String id, String connectorName, MonitorType monitorType,
			String attachedToDeviceId, String attachedToDeviceType);

	void resetParameters();

	void addMissingMonitor(Monitor monitor);

	String getIpmitoolCommand();
	void setIpmitoolCommand(String ipmitoolCommand);

	int getIpmiExecutionCount();
	void setIpmiExecutionCount(int ipmiExecutionCount);

	boolean isLocalhost();
	void setLocalhost(boolean isLocalHost);

	Set<String> getPossibleWmiNamespaces();

	String getAutomaticWmiNamespace();
	void setAutomaticWmiNamespace(String automaticNamespace);

	Set<String> getPossibleWbemNamespaces();

	String getAutomaticWbemNamespace();
	void setAutomaticWbemNamespace(String automaticNamespace);

	EngineConfiguration getEngineConfiguration();

	void setEngineConfiguration(EngineConfiguration engineConfiguration);

	EngineResult run(IStrategy... strategies);
}
