package com.sentrysoftware.hardware.cli.component.cli.printer;

import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.Ansi.Attribute;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.PresentParam;
import com.sentrysoftware.matrix.model.parameter.StatusParam;
import com.sentrysoftware.matrix.model.parameter.TextParam;

public class PrettyPrinter {

	private static final Set<String> EXCLUDED_METADATA = Set.of(
			HardwareConstants.ADDITIONAL_INFORMATION1.toLowerCase(),
			HardwareConstants.ADDITIONAL_INFORMATION2.toLowerCase(),
			HardwareConstants.ADDITIONAL_INFORMATION3.toLowerCase(),
			HardwareConstants.IDENTIFYING_INFORMATION.toLowerCase(),
			HardwareConstants.TARGET_FQDN.toLowerCase(),
			HardwareConstants.ID_COUNT.toLowerCase(),
			HardwareConstants.ATTACHED_TO_DEVICE_ID.toLowerCase(),
			HardwareConstants.ATTACHED_TO_DEVICE_TYPE.toLowerCase(),
			HardwareConstants.DISPLAY_ID.toLowerCase(),
			HardwareConstants.DESCRIPTION.toLowerCase(),
			HardwareConstants.FILE_NAME.toLowerCase()
	);

	private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#########.###");

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
		printChildrenOf(null, 0);
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

			if (monitorsOfType.size() > 0) {

				if (type != MonitorType.TARGET) {
					out.println();
					margin(indentation);
					out.println(Ansi.ansi().bold().a(type.getDisplayNamePlural()).boldOff().a(":").toString());
				}
				for (Monitor monitor : monitorsOfType) {

					out.print(" ".repeat(indentation));
					out.print("- ");
					out.print(Ansi.ansi().a(Attribute.INTENSITY_FAINT).a(type.getDisplayName()).reset().toString());
					out.print(": ");
					out.println(monitor.getName());
					if (showMetadata) {
						printMetadata(monitor, indentation + 2);
					}
					if (showParameters) {
						printParameters(monitor, indentation + 2);
					}
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

		monitor.getMetadata()
				.entrySet()
				.stream()
				.filter(e -> !EXCLUDED_METADATA.contains(e.getKey().toLowerCase()))
				.filter(e -> e.getValue() != null && !e.getValue().isBlank())
				.sorted((e1, e2) -> e1.getKey().compareToIgnoreCase(e2.getKey()))
				.forEachOrdered(metadata -> {
					out.print(Ansi.ansi()
							.a(Attribute.INTENSITY_FAINT)
							.a(metadata.getKey())
							.a(": ")
							.reset()
							.fgCyan()
							.a(metadata.getValue().trim())
							.fgDefault()
							.a(" - ")
							.toString()
					);
				});

		// Add identifying information if it's there
		String identifying = monitor.getMetadata(HardwareConstants.IDENTIFYING_INFORMATION);
		if (identifying != null && !identifying.isBlank()) {
			out.println(Ansi.ansi()
					.a(Attribute.INTENSITY_FAINT)
					.a("More info: ")
					.reset()
					.fgCyan()
					.a(identifying.trim())
					.fgDefault()
					.toString()
			);
		} else {
			out.println();
		}
	}

	public static void main(String[] args) {
		System.out.println("[" + new DecimalFormat("000.##").format(37.2) + "]");
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
				.map(e -> e.getValue())
				.filter(param -> param.getClass() != TextParam.class && param.getClass() != PresentParam.class)
				.filter(param -> param.numberValue() != null)
				.forEachOrdered(param -> {
					margin(indentation);
					if (param instanceof NumberParam) {
						out.println(Ansi.ansi()
								.a(String.format("%-" + (30 - indentation) + "s", param.getName()))
								.bold()
								.a(formatValue(param.numberValue()))
								.boldOff()
								.a(" ")
								.a(((NumberParam) param).getUnit())
								.toString()
						);
					} else if (param instanceof StatusParam) {
						out.print(String.format("%-" + (30 - indentation) + "s", param.getName()));
						switch (((StatusParam) param).getState()) {
						case OK:
							out.println(Ansi.ansi().fgBrightGreen().a("        OK").fgDefault().toString());
							break;
						case WARN:
							out.println(Ansi.ansi().fgBrightYellow().a("      WARN").fgDefault().toString());
							break;
						case ALARM:
							out.println(Ansi.ansi().fgBrightRed().a("     ALARM").fgDefault().toString());
							break;
						default:
							out.println(Ansi.ansi().a(Attribute.INTENSITY_FAINT).a("   Unknown").reset().toString());
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

	private static String formatValue(Number n) {
		String stringValue = DECIMAL_FORMAT.format(n);
		String[] valueParts = stringValue.split("\\.");
		String leftPart = valueParts[0];
		String rightPart = valueParts.length == 2 ? "." + valueParts[1] : "";
		return String.format("%10s%s", leftPart, rightPart);

	}
}
