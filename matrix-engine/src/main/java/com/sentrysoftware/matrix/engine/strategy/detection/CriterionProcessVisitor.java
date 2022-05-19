package com.sentrysoftware.matrix.engine.strategy.detection;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TABLE_SEP;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.common.helpers.LocalOsHandler;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler.Aix;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler.FreeBsd;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler.Hp;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler.Linux;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler.MacOsx;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler.NetBsd;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler.OpenBsd;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler.Solaris;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler.Sun;
import com.sentrysoftware.matrix.common.helpers.LocalOsHandler.Windows;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wmi.Wmi;
import com.sentrysoftware.matrix.engine.protocol.WmiProtocol;
import com.sentrysoftware.matrix.engine.strategy.utils.WqlDetectionHelper;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
	public void visit(final Windows os) {

		Assert.state(wqlDetectionHelper != null, "wqlDetectionHelper cannot be null.");

		final WmiProtocol localWmiConfig = WmiProtocol
				.builder()
				.username(null)
				.password(null)
				.timeout(30L)
				.build();

		final Wmi criterion = Wmi
				.builder()
				.wbemQuery("SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process")
				.wbemNamespace("root\\cimv2")
				.expectedResult(command)
				.build();

		criterionTestResult = wqlDetectionHelper.performDetectionTest("localhost", localWmiConfig, criterion);

	}

	@Override
	public void visit(final Linux os) {
		final List<List<String>> result = listAllLinuxProcesses();
		processResult(result);
	}

	@Override
	public void visit(final Hp os) {
		notImplemented(LocalOsHandler.HP.getOsTag());
	}

	@Override
	public void visit(final Sun os) {
		notImplemented(LocalOsHandler.SUN.getOsTag());
	}

	@Override
	public void visit(final Solaris os) {
		notImplemented(LocalOsHandler.SOLARIS.getOsTag());
	}

	@Override
	public void visit(final FreeBsd os) {
		notImplemented(LocalOsHandler.FREE_BSD.getOsTag());
	}

	@Override
	public void visit(final OpenBsd os) {
		notImplemented(LocalOsHandler.OPEN_BSD.getOsTag());
	}

	@Override
	public void visit(final NetBsd os) {
		notImplemented(LocalOsHandler.NET_BSD.getOsTag());
	}

	@Override
	public void visit(final Aix os) {
		notImplemented(LocalOsHandler.AIX.getOsTag());
	}

	@Override
	public void visit(final MacOsx os) {
		notImplemented(LocalOsHandler.MAC_OS_X.getOsTag());
	}

	/**
	 * List all Linux process.
	 * @return
	 */
	static List<List<String>> listAllLinuxProcesses() {
		return ProcessHandle.allProcesses()
				.map(CriterionProcessVisitor::getProcessDetails)
				.collect(Collectors.toList());
	}

	/**
	 * Get the ps output useful informations: pid;comm;ruser;ppid;args
	 * @param processHandle
	 * @return
	 */
	static List<String> getProcessDetails(final ProcessHandle processHandle) {
		return List.of(
				String.valueOf(processHandle.pid()),
				processHandle.info().command().orElse(""),
				processHandle.info().user().orElse(""),
				processHandle.parent().map(ProcessHandle::pid).map(String::valueOf).orElse(""),
				processHandle.info().commandLine().orElse(""));
	}

	/**
	 * Process the command process list result.
	 * @param result
	 */
	private void processResult(final List<List<String>> result) {
		result.stream()
		.filter(line -> line.get(1).matches(command))
		.findFirst()
		.ifPresentOrElse(
				line -> success(
						String.format( //NOSONAR
								"One or more currently running processes match the following regular expression:\n- Regexp (should match with the command-line): %s",
								command)),
				() -> fail(
						String.format( //NOSONAR
								"No currently running process matches the following regular expression:\n- Regexp (should match with the command-line): %s\n- Currently running process list:\n%s",
								command,
								result.stream().map(line -> line.stream().collect(Collectors.joining(TABLE_SEP))).collect(Collectors.joining(NEW_LINE)))));
	}

	/**
	 * Not implemented OS case.
	 * @param os
	 */
	private void notImplemented(final String os) {
		success(String.format("Process presence check: No test will be performed for OS: %s.", os));
	}

	/**
	 * Create a failed criterionTestResult.
	 * @param message error message.
	 */
	private void fail(final String message) {
		log.error("Hostname {} - Process Criterion, {}", hostname, message);
		criterionTestResult = CriterionTestResult.builder()
				.message(message)
				.build();
	}

	/**
	 * Create a success criterionTestResult.
	 * @param message success message.
	 */
	private void success(final String message) {
		log.debug("Hostname {} - Process Criterion, {}", hostname, message);
		criterionTestResult = CriterionTestResult.builder()
				.success(true)
				.message(message)
				.build();
	}
}
