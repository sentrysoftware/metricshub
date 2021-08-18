package com.sentrysoftware.matrix.engine.strategy.detection;

import java.util.List;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.common.helpers.LocalOSEnum;
import com.sentrysoftware.matrix.common.helpers.LocalOSEnum.Aix;
import com.sentrysoftware.matrix.common.helpers.LocalOSEnum.FreeBSD;
import com.sentrysoftware.matrix.common.helpers.LocalOSEnum.Hp;
import com.sentrysoftware.matrix.common.helpers.LocalOSEnum.Irix;
import com.sentrysoftware.matrix.common.helpers.LocalOSEnum.Linux;
import com.sentrysoftware.matrix.common.helpers.LocalOSEnum.MacOSX;
import com.sentrysoftware.matrix.common.helpers.LocalOSEnum.NetBSD;
import com.sentrysoftware.matrix.common.helpers.LocalOSEnum.OpenBSD;
import com.sentrysoftware.matrix.common.helpers.LocalOSEnum.Os2;
import com.sentrysoftware.matrix.common.helpers.LocalOSEnum.Solaris;
import com.sentrysoftware.matrix.common.helpers.LocalOSEnum.Sun;
import com.sentrysoftware.matrix.common.helpers.LocalOSEnum.Windows;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class CriterionProcessVisitorImpl implements LocalOSEnum.ILocalOSVisitor {

	private static final String SERVICE_CRITERION = "Service Criterion, ";
	private static final String VISIT = "visit ";
	private static final String PROCESS_COMMAND_LINE_QUERY = "select ProcessId,Name,ParentProcessId,CommandLine from Win32_Process";
	private static final String EMPTY = "";
	private static final String DOT = ".";
	private static final int COMMAND_INDEX = 1;

	@NonNull
	private final String command;
	private final MatsyaClientsExecutor matsyaClientsExecutor;
	private final long timeout;

	@Getter
	private CriterionTestResult criterionTestResult;

	@Override
	public void visit(final Windows os) {
		log.debug(SERVICE_CRITERION + VISIT + LocalOSEnum.WINDOWS + DOT);

		if (matsyaClientsExecutor == null) {
			throw new IllegalArgumentException("matsyaClientsExecutor nust not been null.");
		}

		try {
			final List<List<String>> queryResult = matsyaClientsExecutor.executeWmi(
					"localhost",
					null,
					null,
					timeout,
					PROCESS_COMMAND_LINE_QUERY,
					"root\\cimv2");
			if (queryResult.isEmpty()) {
				fail(String.format("WMI query \"%s\" return empty value.", PROCESS_COMMAND_LINE_QUERY));
			} else {
				processResult(queryResult);
			}
		} catch (final Exception e) {
			fail(
					String.format(
							"Unable to perform WMI query \"%s\". %s: %s",
							PROCESS_COMMAND_LINE_QUERY,
							e.getClass().getSimpleName(),
							e.getMessage()));
		}
	}

	@Override
	public void visit(final Linux os) {
		log.debug(SERVICE_CRITERION + VISIT + LocalOSEnum.LINUX + DOT);
		final List<List<String>> result = listAllProcesses();
		processResult(result);
	}

	@Override
	public void visit(final Hp os) {
		log.debug(SERVICE_CRITERION + VISIT + LocalOSEnum.HP + DOT);
		notImplemented(LocalOSEnum.HP.toString());
	}

	@Override
	public void visit(final Sun os) {
		log.debug(SERVICE_CRITERION + VISIT + LocalOSEnum.SUN_OS + DOT);
		notImplemented(LocalOSEnum.SUN_OS.toString());
	}

	@Override
	public void visit(final Solaris os) {
		log.debug(SERVICE_CRITERION + VISIT + LocalOSEnum.SOLARIS + DOT);
		notImplemented(LocalOSEnum.SOLARIS.toString());
	}

	@Override
	public void visit(final Os2 os) {
		log.debug(SERVICE_CRITERION + VISIT + LocalOSEnum.OS2 + DOT);
		notImplemented(LocalOSEnum.OS2.toString());
	}

	@Override
	public void visit(final Aix os) {
		log.debug(SERVICE_CRITERION + VISIT + LocalOSEnum.AIX + DOT);
		notImplemented(LocalOSEnum.AIX.toString());
	}

	@Override
	public void visit(final FreeBSD os) {
		log.debug(SERVICE_CRITERION + VISIT + LocalOSEnum.FREE_BSD + DOT);
		notImplemented(LocalOSEnum.FREE_BSD.toString());
	}

	@Override
	public void visit(final OpenBSD os) {
		log.debug(SERVICE_CRITERION + VISIT + LocalOSEnum.OPEN_BSD + DOT);
		notImplemented(LocalOSEnum.OPEN_BSD.toString());
	}

	@Override
	public void visit(final NetBSD os) {
		log.debug(SERVICE_CRITERION + VISIT + LocalOSEnum.NET_BSD + DOT);
		notImplemented(LocalOSEnum.NET_BSD.toString());
	}

	@Override
	public void visit(final Irix os) {
		log.debug(SERVICE_CRITERION + VISIT + LocalOSEnum.IRIX + DOT);
		notImplemented(LocalOSEnum.IRIX.toString());
	}

	@Override
	public void visit(final MacOSX os) {
		log.debug(SERVICE_CRITERION + VISIT + LocalOSEnum.MAC_OS_X + DOT);
		notImplemented(LocalOSEnum.MAC_OS_X.toString());
	}

	static List<List<String>> listAllProcesses() {
		return ProcessHandle.allProcesses()
				.map(CriterionProcessVisitorImpl::getInformations)
				.collect(Collectors.toList());
	}

	/**
	 * Get the ps output useful informations: pid;comm;ruser;ppid;args
	 * @param processHandle
	 * @return
	 */
	static List<String> getInformations(final ProcessHandle processHandle) {
		return List.of(
				String.valueOf(processHandle.pid()),
				processHandle.info().command().orElse(EMPTY),
				processHandle.info().user().orElse(EMPTY),
				processHandle.parent().map(ProcessHandle::pid).map(String::valueOf).orElse(EMPTY),
				processHandle.info().commandLine().orElse(EMPTY));
	}

	private void processResult(final List<List<String>> result) {
		result.stream()
		.filter(line -> line.get(COMMAND_INDEX).contains(command))
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
								result.stream().map(line -> line.stream().collect(Collectors.joining( ";"))).collect(Collectors.joining("\n")))));
	}

	private void notImplemented(final String os) {
		success(String.format("%s not implemented.", os));
	}

	private void fail(final String message) {
		log.error(SERVICE_CRITERION + message);
		criterionTestResult = CriterionTestResult.builder()
				.message(message)
				.build();
	}

	private void success(final String message) {
		log.debug(SERVICE_CRITERION + message);
		criterionTestResult = CriterionTestResult.builder()
				.success(true)
				.message(message)
				.build();
	}
}
