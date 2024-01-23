package org.sentrysoftware.metricshub.engine.strategy.utils;

import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.LOCALHOST;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.NEW_LINE;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.TABLE_SEP;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.WMI_DEFAULT_NAMESPACE;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.WMI_PROCESS_QUERY;

import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.common.helpers.LocalOsHandler;
import org.sentrysoftware.metricshub.engine.configuration.WmiConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.WmiCriterion;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.springframework.util.Assert;

@Slf4j
@RequiredArgsConstructor
public class CriterionProcessVisitor implements LocalOsHandler.ILocalOsVisitor {

	private static final String CRITERION_PROCESSOR_VISITOR_LOG_MESSAGE = "Hostname {} - Process Criterion, {}";

	@NonNull
	private final String command;

	private final WqlDetectionHelper wqlDetectionHelper;
	private final String hostname;

	@Getter
	private CriterionTestResult criterionTestResult;

	@Override
	public void visit(final LocalOsHandler.Windows os) {
		Assert.state(wqlDetectionHelper != null, "wqlDetectionHelper cannot be null.");

		final WmiConfiguration localWmiConfiguration = WmiConfiguration
			.builder()
			.username(null)
			.password(null)
			.timeout(30L)
			.build();

		final WmiCriterion criterion = WmiCriterion
			.builder()
			.query(WMI_PROCESS_QUERY)
			.namespace(WMI_DEFAULT_NAMESPACE)
			.expectedResult(command)
			.build();

		criterionTestResult = wqlDetectionHelper.performDetectionTest(LOCALHOST, localWmiConfiguration, criterion);
	}

	@Override
	public void visit(final LocalOsHandler.Linux os) {
		final List<List<String>> result = listAllLinuxProcesses();
		processResult(result);
	}

	@Override
	public void visit(final LocalOsHandler.Hp os) {
		notImplemented(LocalOsHandler.HP.getOsTag());
	}

	@Override
	public void visit(final LocalOsHandler.Sun os) {
		notImplemented(LocalOsHandler.SUN.getOsTag());
	}

	@Override
	public void visit(final LocalOsHandler.Solaris os) {
		notImplemented(LocalOsHandler.SOLARIS.getOsTag());
	}

	@Override
	public void visit(final LocalOsHandler.FreeBsd os) {
		notImplemented(LocalOsHandler.FREE_BSD.getOsTag());
	}

	@Override
	public void visit(final LocalOsHandler.OpenBsd os) {
		notImplemented(LocalOsHandler.OPEN_BSD.getOsTag());
	}

	@Override
	public void visit(final LocalOsHandler.NetBsd os) {
		notImplemented(LocalOsHandler.NET_BSD.getOsTag());
	}

	@Override
	public void visit(final LocalOsHandler.Aix os) {
		notImplemented(LocalOsHandler.AIX.getOsTag());
	}

	@Override
	public void visit(final LocalOsHandler.MacOsx os) {
		notImplemented(LocalOsHandler.MAC_OS_X.getOsTag());
	}

	/**
	 * List all Linux process.
	 *
	 * @return
	 */
	public static List<List<String>> listAllLinuxProcesses() {
		return ProcessHandle.allProcesses().map(CriterionProcessVisitor::getProcessDetails).collect(Collectors.toList()); //NOSONAR
	}

	/**
	 * Get the "ps Command" output useful information: pid;comm;ruser;ppid;args
	 *
	 * @param processHandle
	 * @return
	 */
	static List<String> getProcessDetails(final ProcessHandle processHandle) {
		return List.of(
			String.valueOf(processHandle.pid()),
			processHandle.info().command().orElse(""),
			processHandle.info().user().orElse(""),
			processHandle.parent().map(ProcessHandle::pid).map(String::valueOf).orElse(""),
			processHandle.info().commandLine().orElse("")
		);
	}

	/**
	 * Process the command process list result.
	 *
	 * @param result
	 */
	private void processResult(final List<List<String>> result) {
		result
			.stream()
			.filter(line -> line.get(1).matches(command))
			.findFirst()
			.ifPresentOrElse(
				line ->
					success(
						String.format(
							"One or more currently running processes match the following regular expression:\n- " +
							"Regexp (should match with the command-line): %s",
							command
						)
					),
				() ->
					fail(
						String.format(
							"""
							No currently running processes match the following regular expression:
							- Regexp (should match with the command-line): %s
							- Currently running process list:
							%s""",
							command,
							result
								.stream()
								.map(line -> line.stream().collect(Collectors.joining(TABLE_SEP)))
								.collect(Collectors.joining(NEW_LINE))
						)
					)
			);
	}

	/**
	 * Not implemented OS case.
	 *
	 * @param os
	 */
	private void notImplemented(final String os) {
		success(String.format("Process presence check: No tests will be performed for OS: %s.", os));
	}

	/**
	 * Create a failed criterionTestResult.
	 *
	 * @param message error message.
	 */
	private void fail(final String message) {
		log.error(CRITERION_PROCESSOR_VISITOR_LOG_MESSAGE, hostname, message);
		criterionTestResult = CriterionTestResult.builder().message(message).build();
	}

	/**
	 * Create a success criterionTestResult.
	 *
	 * @param message success message.
	 */
	private void success(final String message) {
		log.debug(CRITERION_PROCESSOR_VISITOR_LOG_MESSAGE, hostname, message);
		criterionTestResult = CriterionTestResult.builder().success(true).message(message).build();
	}
}
