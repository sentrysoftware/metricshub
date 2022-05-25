package com.sentrysoftware.matrix.engine.strategy.detection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;

import java.lang.ProcessHandle.Info;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.common.exception.MatsyaException;
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
import com.sentrysoftware.matrix.engine.protocol.WmiProtocol;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.strategy.utils.WqlDetectionHelper;

@ExtendWith(MockitoExtension.class)
class CriterionProcessVisitorTest {

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutor;
	@Spy
	@InjectMocks
	private WqlDetectionHelper wqlDetectionHelper = new WqlDetectionHelper();

	@Test
	void testVisitWindowsMatsyaClientsExecutorKO() {
		{
			final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", null, null);
			assertThrows(IllegalStateException.class, () -> visitor.visit((Windows) null));
		}
	}

	@Test
	void testVisitWindowsExecuteWMIFailed() throws Exception {

		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", wqlDetectionHelper, "localhost");

		doThrow(new MatsyaException("over")).when(matsyaClientsExecutor).executeWql(
				eq("localhost"),
				any(WmiProtocol.class),
				eq("SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process"),
				eq("root\\cimv2"));

		visitor.visit((Windows) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertTrue(criterionTestResult.getMessage().contains("over") && criterionTestResult.getMessage().contains("MatsyaException"));
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitWindowsEmptyResult() throws Exception {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", wqlDetectionHelper, "localhost");

		final WmiProtocol wmiConfig = WmiProtocol
				.builder()
				.username(null)
				.password(null)
				.timeout(30L)
				.build();

		doReturn(Collections.emptyList()).when(matsyaClientsExecutor).executeWql(
				"localhost",
				wmiConfig,
				"SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process",
				"root\\cimv2");

		visitor.visit((Windows) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertTrue(criterionTestResult.getMessage().toLowerCase().contains("no result"));
		assertEquals("No result.", criterionTestResult.getResult());
	}

	@Test
	void testVisitWindowsResultWithoutSearchedProcess() throws Exception {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", wqlDetectionHelper, "localhost");

		final WmiProtocol wmiConfig = WmiProtocol
				.builder()
				.username(null)
				.password(null)
				.timeout(30L)
				.build();

		doReturn(
				List.of(
						List.of("0","System Idle Process", "0", ""),
						List.of("10564","eclipse.exe", "11068", "\"C:\\Users\\huan\\eclipse\\eclipse.exe\"")))
		.when(matsyaClientsExecutor).executeWql(
				"localhost",
				wmiConfig,
				"SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process",
				"root\\cimv2");

		visitor.visit((Windows) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertTrue(criterionTestResult.getMessage().toLowerCase().contains("wmi test ran but failed"));
		assertNotNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitWindowsOK() throws Exception {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", wqlDetectionHelper, "localhost");

		final WmiProtocol wmiConfig = WmiProtocol
				.builder()
				.username(null)
				.password(null)
				.timeout(30L)
				.build();

		doReturn(
				List.of(
						List.of("0","System Idle Process", "0", ""),
						List.of("2", "cimserver", "0", "cimserver arg1 arg2"),
						List.of("10564","eclipse.exe", "11068", "\"C:\\Users\\huan\\eclipse\\eclipse.exe\"")))
		.when(matsyaClientsExecutor).executeWql(
				"localhost",
				wmiConfig,
				"SELECT ProcessId,Name,ParentProcessId,CommandLine FROM Win32_Process",
				"root\\cimv2");

		visitor.visit((Windows) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertTrue(criterionTestResult.getMessage().toLowerCase().contains("wmi test succeeded"));
		assertNotNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitLinuxNoResult() {
		try (final MockedStatic<CriterionProcessVisitor> mockedCriterionProcessVisitorImpl = mockStatic(CriterionProcessVisitor.class)) {
			mockedCriterionProcessVisitorImpl.when(CriterionProcessVisitor::listAllLinuxProcesses).thenReturn(
					List.of(List.of("1", "ps", "root", "0", "ps -A -o pid,comm,ruser,ppid,args")));

			final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", wqlDetectionHelper, "localhost");
			visitor.visit((Linux) null);

			final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

			assertNotNull(criterionTestResult);
			assertFalse(criterionTestResult.isSuccess());
			assertEquals(
					"No currently running processes match the following regular expression:\n" +
							"- Regexp (should match with the command-line): cimserver\n" +
							"- Currently running process list:\n" +
							"1;ps;root;0;ps -A -o pid,comm,ruser,ppid,args",
							criterionTestResult.getMessage());
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitLinuxOK() {
		try (final MockedStatic<CriterionProcessVisitor> mockedCriterionProcessVisitorImpl = mockStatic(CriterionProcessVisitor.class)) {
			mockedCriterionProcessVisitorImpl.when(CriterionProcessVisitor::listAllLinuxProcesses).thenReturn(
					List.of(
							List.of("1", "ps", "root", "0", "ps -A -o pid,comm,ruser,ppid,args"),
							List.of("2", "cimserver", "root", "0", "cimserver args1 args2")));

			final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", wqlDetectionHelper, "localhost");
			visitor.visit((Linux) null);

			final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

			assertNotNull(criterionTestResult);
			assertTrue(criterionTestResult.isSuccess());
			assertEquals(
					"One or more currently running processes match the following regular expression:\n- Regexp (should match with the command-line): cimserver",
					criterionTestResult.getMessage());
			assertNull(criterionTestResult.getResult());
		}
	}

	@Test
	void testVisitNotImplementedSunOK() {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", wqlDetectionHelper, "localhost");
		visitor.visit((Sun) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("Process presence check: No tests will be performed for OS: Sun OS.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitNotImplementedSolarisOK() {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", wqlDetectionHelper, "localhost");
		visitor.visit((Solaris) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("Process presence check: No tests will be performed for OS: Solaris.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitNotImplementedHpOK() {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", wqlDetectionHelper, "localhost");
		visitor.visit((Hp) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("Process presence check: No tests will be performed for OS: HP-UX.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitNotImplementedAixOK() {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", wqlDetectionHelper, "localhost");
		visitor.visit((Aix) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("Process presence check: No tests will be performed for OS: IBM AIX.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitNotImplementedFreeBsdOK() {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", wqlDetectionHelper, "localhost");
		visitor.visit((FreeBsd) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("Process presence check: No tests will be performed for OS: FreeBSD.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitNotImplementedOpenBsdOK() {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", wqlDetectionHelper, "localhost");
		visitor.visit((OpenBsd) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("Process presence check: No tests will be performed for OS: OpenBSD.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitNotImplementedNetBsdOK() {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", wqlDetectionHelper, "localhost");
		visitor.visit((NetBsd) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("Process presence check: No tests will be performed for OS: NetBSD.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitNotImplementedMacOsxOK() {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", wqlDetectionHelper, "localhost");
		visitor.visit((MacOsx) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("Process presence check: No tests will be performed for OS: Mac OS X.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testGetProcessDetails() {
		{
			final ProcessHandle processHandle = Mockito.mock(ProcessHandle.class);
			final Info info = Mockito.mock(Info.class);

			Mockito.doReturn(1L).when(processHandle).pid();
			Mockito.doReturn(Optional.empty()).when(processHandle).parent();
			Mockito.doReturn(info).when(processHandle).info();
			Mockito.doReturn(Optional.empty()).when(info).command();
			Mockito.doReturn(Optional.empty()).when(info).user();
			Mockito.doReturn(Optional.empty()).when(info).commandLine();

			assertEquals(
					List.of("1", "", "", "", ""),
					CriterionProcessVisitor.getProcessDetails(processHandle));
		}

		{
			final ProcessHandle processHandle = Mockito.mock(ProcessHandle.class);
			final Info info = Mockito.mock(Info.class);
			final ProcessHandle parent = Mockito.mock(ProcessHandle.class);

			Mockito.doReturn(2L).when(processHandle).pid();
			Mockito.doReturn(Optional.of(parent)).when(processHandle).parent();
			Mockito.doReturn(1L).when(parent).pid();
			Mockito.doReturn(info).when(processHandle).info();
			Mockito.doReturn(Optional.of("ps")).when(info).command();
			Mockito.doReturn(Optional.of("root")).when(info).user();
			Mockito.doReturn(Optional.of("ps -A -o pid,comm,ruser,ppid,args")).when(info).commandLine();

			assertEquals(
					List.of("2", "ps", "root", "1", "ps -A -o pid,comm,ruser,ppid,args"),
					CriterionProcessVisitor.getProcessDetails(processHandle));
		}
	}
}
