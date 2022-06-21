package com.sentrysoftware.hardware.cli.component.cli.printer;

import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.NumberHelper;
import com.sentrysoftware.matrix.common.meta.parameter.state.IState;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.alert.Severity;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.TextParam;

public class PrettyPrinter {

	private static final Set<String> EXCLUDED_METADATA = Set.of(
			HardwareConstants.ADDITIONAL_INFORMATION1.toLowerCase(),
			HardwareConstants.ADDITIONAL_INFORMATION2.toLowerCase(),
			HardwareConstants.ADDITIONAL_INFORMATION3.toLowerCase(),
			HardwareConstants.IDENTIFYING_INFORMATION.toLowerCase(),
			HardwareConstants.HOST_FQDN.toLowerCase(),
			HardwareConstants.ID_COUNT.toLowerCase(),
			HardwareConstants.ATTACHED_TO_DEVICE_ID.toLowerCase(),
			HardwareConstants.ATTACHED_TO_DEVICE_TYPE.toLowerCase(),
			HardwareConstants.DISPLAY_ID.toLowerCase(),
			HardwareConstants.DESCRIPTION.toLowerCase(),
			HardwareConstants.COMPILED_FILE_NAME.toLowerCase(),
			HardwareConstants.AVERAGE_CPU_TEMPERATURE_WARNING.toLowerCase(),
			HardwareConstants.CPU_TEMPERATURE_PARAMETER.toLowerCase(),
			HardwareConstants.CPU_THERMAL_DISSIPATION_RATE_PARAMETER.toLowerCase(),
			HardwareConstants.DISK_CONTROLLER_NUMBER.toLowerCase()
	);

	private static final Pattern PARAMETER_ACTIVATION_PATTERN = Pattern.compile("parameteractivation", Pattern.CASE_INSENSITIVE);

	private PrintWriter out;
	private IHostMonitoring result;
	private boolean showParameters;
	private boolean showMetadata;

	private PrettyPrinter(PrintWriter out, IHostMonitoring result, boolean showParameters, boolean showMetadata) {
		this.out = out;
		this.result = result;
		this.showParameters = showParameters;
		this.showMetadata = showMetadata;
	}

	/**
	 * Print the specified {@link IHostMonitoring} result in a human-readable way.
	 * <p>
	 * @param out Where to print the result
	 * @param result The monitoring result to be printed
	 * @param showParameters Whether to display the parameters of each monitor
	 * @param showMetadata Whether to display the metadata of each monitor
	 */
	public static void print(PrintWriter out, IHostMonitoring result, boolean showParameters, boolean showMetadata) {

		PrettyPrinter printer = new PrettyPrinter(out, result, showParameters, showMetadata);
		printer.start();

	}

	/**
	 * Initiate the recursive printing
	 */
	private void start() {

		// Extract the host from the result
		Map<String, Monitor> hostMap = result.selectFromType(MonitorType.HOST);
		if (hostMap == null || hostMap.size() != 1) {
			throw new IllegalStateException("Invalid results");
		}
		Monitor host = hostMap.values().stream().findFirst().orElseThrow();

		// Print what is under the host
		printChildrenOf(host.getId(), 0);
	}

	/**
	 * Search all children of the specified monitor and print them
	 * <p>
	 * @param parentId ObjectID of the parent
	 * @param indentation How much indentation should be used to print the children
	 */
	private void printChildrenOf(final String parentId, final int indentation) {

		Set<Monitor> monitors = result.findChildren(parentId);

		for (MonitorType type : MonitorType.MONITOR_TYPES) {

			List<Monitor> monitorsOfType = monitors
					.stream()
					.filter(monitor -> monitor.getMonitorType() == type)
					.sorted((m1, m2) -> m1.getName().compareToIgnoreCase(m2.getName()))
					.collect(Collectors.toList());

			if (!monitorsOfType.isEmpty()) {

				if (type != MonitorType.HOST) {
					margin(indentation);
					out.println(Ansi.ansi().bold().a(type.getDisplayNamePlural()).boldOff().a(":").toString());
				}
				for (Monitor monitor : monitorsOfType) {

					margin(indentation);
					if (type != MonitorType.HOST) {
						out.print("- ");
					}
					out.print(type.getDisplayName());
					out.print(": ");
					out.println(Ansi.ansi().fgCyan().a(monitor.getName()).reset().toString());

					if (showMetadata) {
						printMetadata(monitor, indentation + 2);
					}

					if (showParameters) {
						printParameters(monitor, indentation + 2);
					}

					out.println();
					out.flush();

					printChildrenOf(monitor.getId(), indentation + 4);
				}
			}
		}
	}

	/**
	 * Print the metadata associated to a monitor
	 * @param monitor The monitor whose metadata must be printed
	 * @param indentation How much indentation to use
	 */
	private void printMetadata(Monitor monitor, int indentation) {

		margin(indentation);

		out.print(monitor.getMetadata()
				.entrySet()
				.stream()
				.filter(e -> !EXCLUDED_METADATA.contains(e.getKey().toLowerCase()))
				.filter(e -> !PARAMETER_ACTIVATION_PATTERN.matcher(e.getKey()).find())
				.filter(e -> e.getValue() != null && !e.getValue().isBlank())
				.sorted((e1, e2) -> e1.getKey().compareToIgnoreCase(e2.getKey()))
				.map(metadata -> Ansi.ansi()
							.a(Attribute.INTENSITY_FAINT)
							.a(metadata.getKey())
							.a(": ")
							.reset()
							.a(metadata.getValue().trim())
							.toString()
				)
				.collect(Collectors.joining(" - "))
		);

		// Add identifying information if it's there
		String identifying = monitor.getMetadata(HardwareConstants.IDENTIFYING_INFORMATION);
		if (identifying != null && !identifying.isBlank()) {
			out.println(" - " + identifying);
		} else {
			out.println();
		}
	}

	/**
	 * Print the parameters associated to a monitor
	 * @param monitor The monitor whose parameters must be printed
	 * @param indentation How much indentation to use
	 */
	private void printParameters(Monitor monitor, int indentation) {
		monitor.getParameters()
				.entrySet()
				.stream()
				.sorted((e1, e2) -> e1.getKey().compareToIgnoreCase(e2.getKey()))
				.map(Map.Entry::getValue)
				.filter(param -> param.getClass() != TextParam.class)
				.filter(param -> param.numberValue() != null)
				.forEachOrdered(param -> {
					margin(indentation);
					String paramNameFormat = "%-" + (30 - indentation) + "s";
					if (param instanceof NumberParam) {
						out.println(Ansi.ansi()
								.a(String.format(paramNameFormat, param.getName()))
								.bold()
								.a(NumberHelper.formatNumber(param.numberValue(), "%10s%s"))
								.boldOff()
								.a(" ")
								.a(Attribute.INTENSITY_FAINT)
								.a(((NumberParam) param).getUnit())
								.reset()
								.toString()
						);
					} else if (param instanceof DiscreteParam) {
						out.print(String.format(paramNameFormat, param.getName()));
						final IState state = ((DiscreteParam) param).getState();
						if (state == null) {
							return;
						}
						final Severity severity = state.getSeverity();

						switch (severity) {
						case INFO:
							out.println(Ansi.ansi().bold().fgBrightGreen().a(String.format("%10s", state.getDisplayName())).reset().toString());
							break;
						case WARN:
							out.println(Ansi.ansi().bold().fgYellow().a(String.format("%10s", state.getDisplayName())).reset().toString());
							break;
						case ALARM:
							out.println(Ansi.ansi().bold().fgRed().a(String.format("%10s", state.getDisplayName())).reset().toString());
							break;
						default:
							out.println(Ansi.ansi().a(Attribute.INTENSITY_FAINT).a(String.format("%10s", "Unknown")).reset().toString());
							break;
						}
					}
				});
	}

	/**
	 * Prints the specified margin
	 * @param indentation Number of chars in indentation
	 */
	private void margin(int indentation) {
		out.print(" ".repeat(indentation));
	}

}
