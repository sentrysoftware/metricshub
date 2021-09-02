package com.sentrysoftware.matrix.engine.strategy.discovery;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;

public class HardwareMonitorComparator implements Comparator<HardwareMonitor> {

	public static final List<MonitorType> ORDER = Arrays.asList(MonitorType.ENCLOSURE, MonitorType.BLADE,
			MonitorType.DISK_CONTROLLER, MonitorType.CPU);

	@Override
	public int compare(final HardwareMonitor hardwareMonitor1, final HardwareMonitor hardwareMonitor2) {
		// All the worst cases
		if (hardwareMonitor1 == null && hardwareMonitor2 == null
				|| hardwareMonitor1 == null && hardwareMonitor2.getType() == null
				|| hardwareMonitor1 != null && hardwareMonitor1.getType() == null && hardwareMonitor2 != null
						&& hardwareMonitor2.getType() == null
				|| hardwareMonitor1 != null && hardwareMonitor1.getType() == null && hardwareMonitor2 == null) {
			return 0;
		}

		// hardwareMonitor1 null, hardwareMonitor2 first
		if (hardwareMonitor1 == null || hardwareMonitor1.getType() == null) {
			return 1;
		}

		// hardwareMonitor2 null, hardwareMonitor1 first
		if (hardwareMonitor2 == null || hardwareMonitor2.getType() == null) {
			return -1;
		}

		return compareMonitorType(hardwareMonitor1.getType(), hardwareMonitor2.getType());
	}

	private static int compareMonitorType(final MonitorType type1, final MonitorType type2) {
		// The two types have the order defined in list. The order is going to be the position in the ordered list
		// E.g. ENCLOSURE vs BLADE (0 - 1) so ENCLOSURE first
		if (ORDER.contains(type1) && ORDER.contains(type2)) {
			return ORDER.indexOf(type1) - ORDER.indexOf(type2);
		}

		// type1 is in the ordered list, but type2 isn't. type1 first
		if (ORDER.contains(type1)) {
			return -1;
		}

		// type2 is in the ordered list, but type1 isn't. type2 is first
		if (ORDER.contains(type2)) {
			return 1;
		}

		// Otherwise compare the two types by their Enum ordinal
		return type1.compareTo(type2);
	}
}
