package com.sentrysoftware.matrix.model.monitoring;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.model.monitor.Monitor;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class HostMonitoring implements IHostMonitoring {

	public static final HostMonitoring HOST_MONITORING = new HostMonitoring();

	private Map<MonitorType, Map<String, Monitor>> monitors = new EnumMap<>(MonitorType.class);
	private Map<MonitorType, Map<String, Monitor>> previousMonitors = new EnumMap<>(MonitorType.class);

	private Map<String, SourceTable> sourceTables = new LinkedHashMap<>();

	@Override
	public void clear() {

	}

	@Override
	public void backup() {

	}

	@Override
	public void addMonitor(Monitor monitor) {
		Assert.notNull(monitor, "monitor cannot be null.");

		final String deviceId = monitor.getDeviceId();
		Assert.notNull(deviceId, "monitor id cannot be null.");

		final MonitorType monitorType = monitor.getMonitorType();
		Assert.notNull(monitorType, "monitor type cannot be null.");

		Assert.isTrue(MonitorType.DEVICE.equals(monitorType) || Objects.nonNull(monitor.getParentId()), "Parent Id cannot be null");
		Assert.isTrue(Objects.nonNull(monitor.getTargetId()), "Target Id cannot be null");
	
		if (monitors.containsKey(monitorType)) {
			Map<String, Monitor> collection = monitors.get(monitorType);
			collection.put(deviceId, monitor);
		} else {
			monitors.put(monitorType, createHashMap(deviceId, monitor));
		}
	}

	private <K, V> Map<K, V> createHashMap(K key, V value) {
		Map<K, V> map = new HashMap<>();
		map.put(key, value);
		return map;
	}

	@Override
	public synchronized void removeMonitor(Monitor monitor) {
		if (null == monitor) {
			return;
		}

		final String deviceId = monitor.getDeviceId();
		Assert.notNull(deviceId, "monitor id cannot be null.");

		final MonitorType monitorType = monitor.getMonitorType();
		Assert.notNull(monitorType, "monitor type cannot be null.");

		removeRelatedChildren(deviceId);

		if (monitors.containsKey(monitorType)) {
			Map<String, Monitor> instances = monitors.get(monitorType);

			if (null != instances) {
				instances.remove(deviceId);
			}
		}

	}

	private void removeRelatedChildren(String monitorId) {
		monitors.values().stream().filter(Objects::nonNull).forEach(instances -> instances.entrySet().removeIf(entry -> {
			final Monitor monitor = entry.getValue();
			boolean remove = monitorId.equals(monitor.getParentId());
			if (remove) {
				removeRelatedChildren(monitor.getDeviceId());
			}
			return remove;
		}));
	}

	@Override
	public Map<String, Monitor> selectFromType(MonitorType monitorType) {
		return monitors.get(monitorType);
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

	@Override
	public void addSourceTable(String key, SourceTable sourceTable) {
		Assert.notNull(key, "The key cannot be null");
		Assert.notNull(sourceTable, "The sourceTable cannot be null");

		sourceTables.put(key, sourceTable);
	}

	@Override
	public SourceTable getSourceTableByKey(String key) {
		Assert.notNull(key, "The key cannot be null");
		return sourceTables.get(key);
	}
}
