package com.sentrysoftware.hardware.cli.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentrysoftware.hardware.cli.helpers.StringHelper;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoringVO;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

@Service
public class JobResultFormatterService {

	/**
	 * Parse and write the monitors of a host monitoring into a JSON format.
	 * @param hostMonitoring The hostMonitoring to parse.
	 * @return The data from the monitors parsed into a JSON format.
	 */
	public String format(final IHostMonitoring hostMonitoring) {

		if (hostMonitoring == null || hostMonitoring.getMonitors() == null || hostMonitoring.getMonitors().isEmpty()) {
			return null;
		}

		Map<MonitorType, Map<String, Monitor>> allMonitors = hostMonitoring.getMonitors();

		HostMonitoringVO hostMonitoringVO = new HostMonitoringVO();

		List<MonitorType> monitorTypes = new ArrayList<>(allMonitors.keySet());
		Collections.sort(monitorTypes, new MonitorTypeComparator());

		for (MonitorType monitorType : monitorTypes) {
			if (allMonitors.get(monitorType) == null  || allMonitors.get(monitorType).values() == null) {
				continue;
			}
			List<Monitor> monitorList = new ArrayList<>(allMonitors.get(monitorType).values());
			Collections.sort(monitorList, new MonitorComparator());
			hostMonitoringVO.addAll(monitorList);
		}

		try {
			return new ObjectMapper()
					.writerWithDefaultPrettyPrinter()
					.writeValueAsString(hostMonitoringVO);
		} catch (JsonProcessingException e) {
			return StringHelper.EMPTY;
		}
	}

	/*
	 *  These two classes are used to sort monitors and monitorTypes before adding them to a HostMonitoringVO
	 *  so that the parsing always returns the same value, and not with monitor types and monitors displayed randomly
	 */
	class MonitorComparator implements Comparator<Monitor> {
		@Override
		public int compare(Monitor a, Monitor b) {
			return a.getId().compareTo(b.getId());
		}
	}

	class MonitorTypeComparator implements Comparator<MonitorType> {
		@Override
		public int compare(MonitorType a, MonitorType b) {
			return a.name().compareTo(b.name());
		}
	}
}
