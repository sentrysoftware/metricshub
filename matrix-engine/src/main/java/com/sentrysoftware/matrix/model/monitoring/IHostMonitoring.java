package com.sentrysoftware.matrix.model.monitoring;

import java.util.Map;
import java.util.Set;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.EngineResult;
import com.sentrysoftware.matrix.engine.strategy.IStrategy;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring.PowerMeter;

public interface IHostMonitoring {

	Map<MonitorType, Map<String, Monitor>> getMonitors();

	Map<MonitorType, Map<String, Monitor>> getPreviousMonitors();

	void setMonitors(Map<MonitorType, Map<String, Monitor>> monitors);

	void setPreviousMonitors(Map<MonitorType, Map<String, Monitor>> previousMonitors);

	void clearCurrent();

	void clearPrevious();

	void backup();

	void addMonitor(Monitor monitor);

	void removeMonitor(Monitor monitor);

	Map<String, Monitor> selectFromType(MonitorType monitorType);

	Map<MonitorType, Map<String, Monitor>> selectFromTypes(MonitorType... monitorTypes);

	Monitor findById(String monitorIdentifier);

	String toJson();

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

	Set<String> getPossibleWbemNamespaces();

	EngineConfiguration getEngineConfiguration();

	void setEngineConfiguration(EngineConfiguration engineConfiguration);

	EngineResult run(IStrategy... strategies);

	ConnectorNamespace getConnectorNamespace(final String connectorName);

	ConnectorNamespace getConnectorNamespace(final Connector connector);

	PowerMeter getPowerMeter();

	void setPowerMeter(PowerMeter powerMeter);
}
