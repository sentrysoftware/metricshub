package com.sentrysoftware.matrix.strategy.utils;

import com.sentrysoftware.matrix.common.helpers.LocalOsHandler;
import com.sentrysoftware.matrix.configuration.WmiConfiguration;
import com.sentrysoftware.matrix.connector.model.identity.criterion.WmiCriterion;
import com.sentrysoftware.matrix.strategy.detection.CriterionTestResult;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.List;
import java.util.stream.Collectors;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.CRITERION_PROCESSOR_VISITOR_LOG_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.CRITERION_PROCESSOR_VISITOR_NAMESPACE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.CRITERION_PROCESSOR_VISITOR_QUERY;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.LOCALHOST;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NO_RUNNING_PROCESS_MATCH_REGEX_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NO_TEST_FOR_OS_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.RUNNING_PROCESS_MATCH_REGEX_MESSAGE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.TABLE_SEP;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.WQL_DETECTION_HELPER_NULL_MESSAGE;

@Slf4j
@RequiredArgsConstructor
public class CriterionProcessVisitor implements LocalOsHandler.ILocalOsVisitor {

	@NonNull
	private final String command;
	private final WqlDetectionHelper wqlDetectionHelper;
	private final String hostname;

	@Getter
	private CriterionTestResult criterionTestResult;

	@Override
	public void visit(final LocalOsHandler.Windows os) {

		Assert.state(wqlDetectionHelper != null, WQL_DETECTION_HELPER_NULL_MESSAGE);

		final WmiConfiguration localWmiConfiguration = WmiConfiguration
			.builder()
			.username(null)
			.password(null)
			.timeout(30L)
			.build();

		final WmiCriterion criterion = WmiCriterion
			.builder()
			.query(CRITERION_PROCESSOR_VISITOR_QUERY)
			.namespace(CRITERION_PROCESSOR_VISITOR_NAMESPACE)
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
		return ProcessHandle
			.allProcesses()
			.map(CriterionProcessVisitor::getProcessDetails)
			.toList();
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
		result.stream()
			.filter(line -> line.get(1).matches(command))
			.findFirst()
			.ifPresentOrElse(
				line -> success(
					String.format( //NOSONAR
						RUNNING_PROCESS_MATCH_REGEX_MESSAGE,
						command
					)
				),
				() -> fail(
					String.format( //NOSONAR
						NO_RUNNING_PROCESS_MATCH_REGEX_MESSAGE,
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
		success(String.format(NO_TEST_FOR_OS_MESSAGE, os));
	}

	/**
	 * Create a failed criterionTestResult.
	 *
	 * @param message error message.
	 */
	private void fail(final String message) {
		log.error(CRITERION_PROCESSOR_VISITOR_LOG_MESSAGE, hostname, message);
		criterionTestResult = CriterionTestResult
			.builder()
			.message(message)
			.build();
	}

	/**
	 * Create a success criterionTestResult.
	 *
	 * @param message success message.
	 */
	private void success(final String message) {
		log.debug(CRITERION_PROCESSOR_VISITOR_LOG_MESSAGE, hostname, message);
		criterionTestResult = CriterionTestResult
			.builder()
			.success(true)
			.message(message)
			.build();
	}
}