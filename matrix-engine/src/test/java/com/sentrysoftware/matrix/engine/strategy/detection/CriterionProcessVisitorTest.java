package com.sentrysoftware.matrix.engine.strategy.detection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;

import java.lang.ProcessHandle.Info;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.Aix;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.FreeBSD;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.Hp;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.Irix;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.Linux;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.MacOSX;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.NetBSD;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.OpenBSD;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.Os2;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.Solaris;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.Sun;
import com.sentrysoftware.matrix.common.helpers.LocalOSHandler.Windows;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;

@ExtendWith(MockitoExtension.class)
class CriterionProcessVisitorTest {

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutor;

	@Test
	void testVisitWindowsMatsyaClientsExecutorKO() {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", null, 0L);
		assertThrows(IllegalArgumentException.class, () -> visitor.visit((Windows) null));
	}

	@Test
	void testVisitWindowsExecuteWMIFailed() throws Exception {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", matsyaClientsExecutor, 120L);

		doThrow(new TimeoutException("over")).when(matsyaClientsExecutor).executeWmi(
				"localhost",
				null,
				null,
				120L,
				"select ProcessId,Name,ParentProcessId,CommandLine from Win32_Process",
				"root\\cimv2");

		visitor.visit((Windows) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(
				"Unable to perform WMI query \"select ProcessId,Name,ParentProcessId,CommandLine from Win32_Process\". TimeoutException: over",
				criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitWindowsEmptyResult() throws Exception {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", matsyaClientsExecutor, 120L);

		doReturn(Collections.emptyList()).when(matsyaClientsExecutor).executeWmi(
				"localhost",
				null,
				null,
				120L,
				"select ProcessId,Name,ParentProcessId,CommandLine from Win32_Process",
				"root\\cimv2");

		visitor.visit((Windows) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(
				"WMI query \"select ProcessId,Name,ParentProcessId,CommandLine from Win32_Process\" return empty value.",
				criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitWindowsResultWithoutSearchedProcess() throws Exception {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", matsyaClientsExecutor, 120L);

		doReturn(
				List.of(
						List.of("0","System Idle Process", "0", ""),
						List.of("10564","eclipse.exe", "11068", "\"C:\\Users\\huan\\eclipse\\eclipse.exe\"")))
		.when(matsyaClientsExecutor).executeWmi(
				"localhost",
				null,
				null,
				120L,
				"select ProcessId,Name,ParentProcessId,CommandLine from Win32_Process",
				"root\\cimv2");

		visitor.visit((Windows) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertFalse(criterionTestResult.isSuccess());
		assertEquals(
				"No currently running processes matches the following regular expression:\n" +
						"- Regexp (should match with the command-line): cimserver\n" +
						"- Currently running process list:\n" +
						"0;System Idle Process;0;\n" +
						"10564;eclipse.exe;11068;\"C:\\Users\\huan\\eclipse\\eclipse.exe\"",
						criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitWindowsOK() throws Exception {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", matsyaClientsExecutor, 120L);

		doReturn(
				List.of(
						List.of("0","System Idle Process", "0", ""),
						List.of("2", "cimserver", "0", "cimserver arg1 arg2"),
						List.of("10564","eclipse.exe", "11068", "\"C:\\Users\\huan\\eclipse\\eclipse.exe\"")))
		.when(matsyaClientsExecutor).executeWmi(
				"localhost",
				null,
				null,
				120L,
				"select ProcessId,Name,ParentProcessId,CommandLine from Win32_Process",
				"root\\cimv2");

		visitor.visit((Windows) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals(
				"One or more currently running processes match the following regular expression:\n- Regexp (should match with the command-line): cimserver",
				criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitLinuxNoResult() {
		try (final MockedStatic<CriterionProcessVisitor> mockedCriterionProcessVisitorImpl = mockStatic(CriterionProcessVisitor.class)) {
			mockedCriterionProcessVisitorImpl.when(CriterionProcessVisitor::listAllProcesses).thenReturn(
					List.of(List.of("1", "ps", "root", "0", "ps -A -o pid,comm,ruser,ppid,args")));

			final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", null, 0L);
			visitor.visit((Linux) null);

			final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

			assertNotNull(criterionTestResult);
			assertFalse(criterionTestResult.isSuccess());
			assertEquals(
					"No currently running processes matches the following regular expression:\n" +
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
			mockedCriterionProcessVisitorImpl.when(CriterionProcessVisitor::listAllProcesses).thenReturn(
					List.of(
							List.of("1", "ps", "root", "0", "ps -A -o pid,comm,ruser,ppid,args"),
							List.of("2", "cimserver", "root", "0", "cimserver args1 args2")));

			final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", null, 0L);
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
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", null, 0L);
		visitor.visit((Sun) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("sunos not implemented.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitNotImplementedSolarisOK() {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", null, 0L);
		visitor.visit((Solaris) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("solaris not implemented.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitNotImplementedOs2OK() {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", null, 0L);
		visitor.visit((Os2) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals(
				"os/2 not implemented.",
				criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitNotImplementedHpOK() {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", null, 0L);
		visitor.visit((Hp) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("hp-ux not implemented.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitNotImplementedAixOK() {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", null, 0L);
		visitor.visit((Aix) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("aix not implemented.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitNotImplementedOpenBsdOK() {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", null, 0L);
		visitor.visit((OpenBSD) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("openbsd not implemented.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitNotImplementedFreeBsdOK() {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", null, 0L);
		visitor.visit((FreeBSD) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("freebsd not implemented.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitNotImplementedNetBsdOK() {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", null, 0L);
		visitor.visit((NetBSD) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("netbsd not implemented.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitNotImplementedIrixOK() {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", null, 0L);
		visitor.visit((Irix) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("irix not implemented.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testVisitNotImplementedMacOsxOK() {
		final CriterionProcessVisitor visitor = new CriterionProcessVisitor("cimserver", null, 0L);
		visitor.visit((MacOSX) null);

		final CriterionTestResult criterionTestResult = visitor.getCriterionTestResult();

		assertNotNull(criterionTestResult);
		assertTrue(criterionTestResult.isSuccess());
		assertEquals("mac os x not implemented.", criterionTestResult.getMessage());
		assertNull(criterionTestResult.getResult());
	}

	@Test
	void testGetInformations() {
		{
			final ProcessHandle processHandle = Mockito.mock(ProcessHandle.class);
			final Info info = Mockito.mock(Info.class);

			Mockito.doReturn(1L).when(processHandle).pid();
			Mockito.doReturn(Optional.empty()).when(processHandle).parent();
			Mockito.doReturn(info).when(processHandle).info();
			Mockito.doReturn(Optional.empty()).when(info).command();
			Mockito.doReturn(Optional.empty()).when(info).user();
			Mockito.doReturn(Optional.empty()).when(info).commandLine();

			Assertions.assertEquals(
					List.of("1", "", "", "", ""),
					CriterionProcessVisitor.getInformations(processHandle));
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

			Assertions.assertEquals(
					List.of("2", "ps", "root", "1", "ps -A -o pid,comm,ruser,ppid,args"),
					CriterionProcessVisitor.getInformations(processHandle));
		}
	}
}
