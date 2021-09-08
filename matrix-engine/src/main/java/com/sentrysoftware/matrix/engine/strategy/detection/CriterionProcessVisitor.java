package com.sentrysoftware.matrix.engine.strategy.detection;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.Aix;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.FreeBSD;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.Hp;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.Linux;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.MacOSX;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.NetBSD;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.OpenBSD;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.Solaris;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.Sun;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.Windows;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wmi.WMI;
import com.sentrysoftware.matrix.engine.protocol.WMIProtocol;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.strategy.utils.WqlDetectionHelper;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CriterionProcessVisitor implements LocalOSHandler.ILocalOSVisitor {

	@NonNull
	private final String command;
	private final StrategyConfig strategyConfig;
	private final MatsyaClientsExecutor matsyaClientsExecutor;

	@Autowired
	private final WqlDetectionHelper wqlDetectionHelper;

	@Getter
	private CriterionTestResult criterionTestResult;

	@Override
	public void visit(final Windows os) {

		Assert.state(strategyConfig != null, "strategyConfig mustn't be null.");
		Assert.state(matsyaClientsExecutor != null, "matsyaClientsExecutor mustn't be null.");

		final WMIProtocol localWmiConfig = WMIProtocol
				.builder()
				.username(null)
				.password(null)
				.timeout(30L)
				.build();

		final WMI criterion = (WMI) WMI
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
		notImplemented(LocalOSHandler.HP.getOsTag());
	}

	@Override
	public void visit(final Sun os) {
		notImplemented(LocalOSHandler.SUN.getOsTag());
	}

	@Override
	public void visit(final Solaris os) {
		notImplemented(LocalOSHandler.SOLARIS.getOsTag());
	}

	@Override
	public void visit(final FreeBSD os) {
		notImplemented(LocalOSHandler.FREE_BSD.getOsTag());
	}

	@Override
	public void visit(final OpenBSD os) {
		notImplemented(LocalOSHandler.OPEN_BSD.getOsTag());
	}

	@Override
	public void visit(final NetBSD os) {
		notImplemented(LocalOSHandler.NET_BSD.getOsTag());
	}

	@Override
	public void visit(final Aix os) {
		notImplemented(LocalOSHandler.AIX.getOsTag());
	}

	@Override
	public void visit(final MacOSX os) {
		notImplemented(LocalOSHandler.MAC_OS_X.getOsTag());
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
				processHandle.info().command().orElse(HardwareConstants.EMPTY),
				processHandle.info().user().orElse(HardwareConstants.EMPTY),
				processHandle.parent().map(ProcessHandle::pid).map(String::valueOf).orElse(HardwareConstants.EMPTY),
				processHandle.info().commandLine().orElse(HardwareConstants.EMPTY));
	}

	/**
	 * Process the command pocess list result.
	 * @param result
	 */
	private void processResult(final List<List<String>> result) {
		result.stream()
		.filter(line -> line.get(1).matches(command))
		.findFirst()
		.ifPresentOrElse(
				line -> success(
						String.format(
								"One or more currently running processes match the following regular expression:\n- Regexp (should match with the command-line): %s",
								command)),
				() -> fail(
						String.format(
								"No currently running processes matches the following regular expression:\n- Regexp (should match with the command-line): %s\n- Currently running process list:\n%s",
								command,
								result.stream().map(line -> line.stream().collect(Collectors.joining(HardwareConstants.SEMICOLON))).collect(Collectors.joining(HardwareConstants.NEW_LINE)))));
	}

	/**
	 * Not implemented OS case.
	 * @param os
	 */
	private void notImplemented(final String os) {
		success(String.format("Process presence check: no test will be performed for OS: %s.", os));
	}

	/**
	 * Create a failed criterionTestResult.
	 * @param message error message.
	 */
	private void fail(final String message) {
		log.error("Process Criterion, {}", message);
		criterionTestResult = CriterionTestResult.builder()
				.message(message)
				.build();
	}

	/**
	 * Create a success criterionTestResult.
	 * @param message success message.
	 */
	private void success(final String message) {
		log.debug("Process Criterion, {}", message);
		criterionTestResult = CriterionTestResult.builder()
				.success(true)
				.message(message)
				.build();
	}
}
