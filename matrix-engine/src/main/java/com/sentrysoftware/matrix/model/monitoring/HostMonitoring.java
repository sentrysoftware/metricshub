package com.sentrysoftware.matrix.model.monitoring;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HostMonitoring implements IHostMonitoring {

	public static final HostMonitoring HOST_MONITORING = new HostMonitoring();

	private Map<MonitorType, Map<String, Monitor>> monitors = new EnumMap<>(MonitorType.class);
	private Map<MonitorType, Map<String, Monitor>> previousMonitors = new EnumMap<>(MonitorType.class);

	@Override
	public void clear() {

	}

	@Override
	public void backup() {

	}

	@Override
	public void addMonitor(Monitor monitor) {

	}

	@Override
	public void removeMonitor(Monitor monitor) {

	}

	@Override
	public Map<String, Monitor> selectFromType(MonitorType monitorType) {

		return Collections.emptyMap();
	}

	@Override
	public Set<Monitor> selectChildren(String parentIdentifier, MonitorType childrenMonitorType) {

		return Collections.emptySet();
	}

	@Override
	public String toJsonString() {

		return "{}";
	}

	@Override
	public Map<MonitorType, Map<String, Monitor>> selectFromTypes(MonitorType... monitorTypes) {

		return Collections.emptyMap();
	}
}
