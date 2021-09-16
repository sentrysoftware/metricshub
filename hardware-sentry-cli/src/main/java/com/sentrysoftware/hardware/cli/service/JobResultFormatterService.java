package com.sentrysoftware.hardware.cli.service;

import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;
import org.springframework.stereotype.Service;

import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
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
			return "{}";
		}

		return hostMonitoring.toJson();
	}

	/**
	 * Print the result to the console output, in a human-readable format.
	 * <p>
	 * @param hostMonitoring the results to parse
	 * @param out Where to print the output (should be Console.out in general)
	 */
	public void printResult(final IHostMonitoring result, final PrintWriter out) {

		printChildrenOf(result, out, null, 0);
	}

	private void printChildrenOf(final IHostMonitoring result, final PrintWriter out, final String parentId, final int indentation) {

		Set<Monitor> monitors = result.findChildren(parentId);

		for (MonitorType type : MonitorType.MONITOR_TYPES) {

			List<Monitor> monitorsOfType = monitors
					.stream()
					.filter(monitor -> monitor.getMonitorType() == type)
					.sorted((m1, m2) -> m1.getName().compareToIgnoreCase(m2.getName()))
					.collect(Collectors.toList());

			if (monitorsOfType.size() > 0) {
				out.print(" ".repeat(indentation));
				out.println(type.getDisplayNamePlural() + ":");
				for (Monitor monitor : monitorsOfType) {

					out.print(" ".repeat(indentation));
					out.print("- ");
					out.print(Ansi.ansi().a(Attribute.INTENSITY_FAINT).a(type.getDisplayName()).reset().toString());
					out.print(": ");
					out.println(monitor.getName());
					out.flush();

					printChildrenOf(result, out, monitor.getId(), indentation + 4);
				}
			}
		}
	}

}
