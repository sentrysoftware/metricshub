package com.sentrysoftware.matrix.engine.strategy.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.sentrysoftware.matrix.common.exception.StepException;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.GetAvailable;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.GetUntilPrompt;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.SendPassword;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.SendText;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.SendUsername;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.Sleep;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.WaitFor;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.WaitForPrompt;
import com.sentrysoftware.matrix.engine.protocol.SSHProtocol;
import com.sentrysoftware.matsya.ssh.SSHClient;

class CriterionSshInteractiveStepVisitorTest {

	@Test
	void testVisitGetAvailable() throws Exception {

		final CriterionSshInteractiveStepVisitor visitor =
				spy(new CriterionSshInteractiveStepVisitor(null, null, mock(SSHProtocol.class), null));

		assertThrows(IllegalArgumentException.class, () -> visitor.visit((GetAvailable) null));

		// check with capture false
		{
			doReturn(Optional.of("$")).when(visitor).readAll(anyString(), anyInt());

			 visitor.visit(new GetAvailable());
			 assertEquals(Optional.empty(), visitor.getResult());
		}

		// check with capture true
		{
			final GetAvailable step = new GetAvailable();
			step.setCapture(true);

			doReturn(Optional.of("prompt>")).when(visitor).readAll(anyString(), anyInt());

			 visitor.visit(step);
			 assertEquals(Optional.of("prompt>"), visitor.getResult());
		}
	}

	@Test
	void testVisitGetUntilPrompt() throws Exception {

		assertThrows(
				IllegalArgumentException.class,
				() -> spy(new CriterionSshInteractiveStepVisitor(null, null, null, null)).visit((GetUntilPrompt) null));

		// check with inPrompt null
		{
			final GetUntilPrompt step = new GetUntilPrompt();
			step.setCapture(true);

			final CriterionSshInteractiveStepVisitor visitor =
					spy(new CriterionSshInteractiveStepVisitor(null, null, null, null));

			visitor.visit(step);
			verify(visitor, never()).getUntil(anyString(), anyString(), anyLong());
			assertEquals(Optional.empty(), visitor.getResult());
		}

		// check with inPrompt empty
		{
			final GetUntilPrompt step = new GetUntilPrompt();
			step.setCapture(true);

			final CriterionSshInteractiveStepVisitor visitor =
					spy(new CriterionSshInteractiveStepVisitor(null, null, null, ""));
			
			visitor.visit(step);
			verify(visitor, never()).getUntil(anyString(), anyString(), anyLong());
			assertEquals(Optional.empty(), visitor.getResult());
		}

		// check prompt not found
		{
			final GetUntilPrompt step = new GetUntilPrompt();
			step.setTimeout(30L);
			step.setCapture(true);

			final CriterionSshInteractiveStepVisitor visitor =
					spy(new CriterionSshInteractiveStepVisitor(null, null, null, "prompt>"));

			doReturn(
					Optional.of("\r"),
					Optional.of("n"), Optional.of("o"), Optional.of("t"), Optional.of(" "),
					Optional.of("f"), Optional.of("o"), Optional.of("u"), Optional.of("n"), Optional.of("d"),
					Optional.of("\r"), Optional.of(" "), Optional.of("."), Optional.of("."), Optional.of("."), Optional.of("\r"),
					Optional.empty())
			.when(visitor).read(anyString(), eq(1), anyInt());

			assertThrows(StepException.class, () -> visitor.visit(step));
			assertEquals(Optional.empty(), visitor.getResult());
		}

		// check prompt found capture false
		{
			final GetUntilPrompt step = new GetUntilPrompt();
			step.setTimeout(30L);
			step.setCapture(false);

			final CriterionSshInteractiveStepVisitor visitor =
					spy(new CriterionSshInteractiveStepVisitor(null, null, null, "prompt>"));

			doReturn(
					Optional.of("\r"),
					Optional.of("p"), Optional.of("r"), Optional.of("o"), Optional.of("m"), Optional.of("p"), Optional.of("t"), Optional.of(">"),
					Optional.of("\r"), Optional.of(" "), Optional.of("."), Optional.of("."), Optional.of("."), Optional.of("\r"),
					Optional.empty())
			.when(visitor).read(anyString(), eq(1), anyInt());

			visitor.visit(step);
			assertEquals(Optional.empty(), visitor.getResult());
		}

		// check prompt found capture true
		{
			final GetUntilPrompt step = new GetUntilPrompt();
			step.setTimeout(30L);
			step.setCapture(true);

			final CriterionSshInteractiveStepVisitor visitor =
					spy(new CriterionSshInteractiveStepVisitor(null, null, null, "prompt>"));

			doReturn(
					Optional.of("\r"),
					Optional.of("p"), Optional.of("r"), Optional.of("o"), Optional.of("m"), Optional.of("p"), Optional.of("t"), Optional.of(">"),
					Optional.of("\r"), Optional.of(" "), Optional.of("."), Optional.of("."), Optional.of("."), Optional.of("\r"),
					Optional.empty())
			.when(visitor).read(anyString(), eq(1), anyInt());

			visitor.visit(step);
			assertEquals(Optional.of("prompt>"), visitor.getResult());
		}
	}

	@Test
	void testVisitSendPassword() throws Exception {

		final SSHProtocol sshProtocol = mock(SSHProtocol.class);

		final CriterionSshInteractiveStepVisitor visitor =
				spy(new CriterionSshInteractiveStepVisitor(mock(SSHClient.class), null, sshProtocol, null));

		assertThrows(IllegalArgumentException.class, () -> visitor.visit((SendPassword) null));

		final SendPassword step = new SendPassword();

		// check password null
		visitor.visit(step);
		verify(visitor, times(1)).write(anyString(), eq(HardwareConstants.NEW_LINE));

		// check password not null
		doReturn(new char[] {'p', 'w', 'd'}).when(sshProtocol).getPassword();
		visitor.visit(step);
		verify(visitor, times(1)).write(anyString(), eq("pwd\n"));
	}

	@Test
	void testVisitSendText() throws Exception {

		final CriterionSshInteractiveStepVisitor visitor =
				spy(new CriterionSshInteractiveStepVisitor(mock(SSHClient.class), null, null, null));

		assertThrows(IllegalArgumentException.class, () -> visitor.visit((SendText) null));

		// Check text null
		{
			visitor.visit(new SendText());
			verify(visitor, never()).write(anyString(), anyString());
		}

		// Check text empty
		{
			final SendText step = new SendText();
			step.setText(HardwareConstants.EMPTY);

			visitor.visit(step);
			verify(visitor, never()).write(anyString(), anyString());
		}

		// Check write text
		{
			final SendText step = new SendText();
			step.setText("Text\n");

			visitor.visit(step);
			verify(visitor, times(1)).write(anyString(), eq("Text\n"));
		}
	}

	@Test
	void testVisitSendUsername() throws Exception {

		final SSHProtocol sshProtocol = mock(SSHProtocol.class);

		final CriterionSshInteractiveStepVisitor visitor =
				spy(new CriterionSshInteractiveStepVisitor(mock(SSHClient.class), null, sshProtocol, null));

		assertThrows(IllegalArgumentException.class, () -> visitor.visit((SendUsername) null));

		doReturn("user").when(sshProtocol).getUsername();
		visitor.visit(new SendUsername());
		verify(visitor, times(1)).write(anyString(), eq("user\n"));
	}

	@Test
	void testVisitSleep() throws Exception {

		final CriterionSshInteractiveStepVisitor visitor =
				spy(new CriterionSshInteractiveStepVisitor(null, null, null, null));

		assertThrows(IllegalArgumentException.class, () -> visitor.visit((Sleep) null));

		// Check duration null
		try (final MockedStatic<CriterionSshInteractiveStepVisitor> mockedVisitor = mockStatic(CriterionSshInteractiveStepVisitor.class)) {

			visitor.visit(new Sleep());
			mockedVisitor.verifyNoInteractions();
		}

		// Check duration 0
		try (final MockedStatic<CriterionSshInteractiveStepVisitor> mockedVisitor = mockStatic(CriterionSshInteractiveStepVisitor.class)) {

			final Sleep step = new Sleep();
			step.setDuration(0L);

			visitor.visit(step);
			mockedVisitor.verifyNoInteractions();
		}

		// Check duration > 0
		try (final MockedStatic<CriterionSshInteractiveStepVisitor> mockedVisitor = mockStatic(CriterionSshInteractiveStepVisitor.class)) {

			final Sleep step = new Sleep();
			step.setDuration(1L);

			visitor.visit(step);
			mockedVisitor.verify(times(1), () -> CriterionSshInteractiveStepVisitor.sleep(anyString(), eq(1L))); 
		}
	}

	@Test
	void testVisitWaitFor() throws Exception {

		assertThrows(
				IllegalArgumentException.class,
				() -> spy(new CriterionSshInteractiveStepVisitor(null, null, null, null)).visit((WaitFor) null));

		// check with text null
		{
			final WaitFor step = new WaitFor();
			step.setCapture(true);

			final CriterionSshInteractiveStepVisitor visitor =
					spy(new CriterionSshInteractiveStepVisitor(null, null, null, null));

			visitor.visit(step);
			verify(visitor, never()).getUntil(anyString(), anyString(), anyLong());
			assertEquals(Optional.empty(), visitor.getResult());
		}

		// check with text empty
		{
			final WaitFor step = new WaitFor();
			step.setText(HardwareConstants.EMPTY);
			step.setCapture(true);

			final CriterionSshInteractiveStepVisitor visitor =
					spy(new CriterionSshInteractiveStepVisitor(null, null,null, null));
			
			visitor.visit(step);
			verify(visitor, never()).getUntil(anyString(), anyString(), anyLong());
			assertEquals(Optional.empty(), visitor.getResult());
		}

		// check text not found
		{
			final WaitFor step = new WaitFor();
			step.setText("$");
			step.setTimeout(30L);
			step.setCapture(true);

			final CriterionSshInteractiveStepVisitor visitor =
					spy(new CriterionSshInteractiveStepVisitor(null, null, null, null));

			doReturn(
					Optional.of("\r"),
					Optional.of("n"), Optional.of("o"), Optional.of("t"), Optional.of(" "),
					Optional.of("f"), Optional.of("o"), Optional.of("u"), Optional.of("n"), Optional.of("d"),
					Optional.of("\r"),
					Optional.empty())
			.when(visitor).read(anyString(), eq(1), anyInt());

			assertThrows(StepException.class, () -> visitor.visit(step));
			assertEquals(Optional.empty(), visitor.getResult());
		}

		// check text found capture false
		{
			final WaitFor step = new WaitFor();
			step.setText("$");
			step.setTimeout(30L);
			step.setCapture(false);

			final CriterionSshInteractiveStepVisitor visitor =
					spy(new CriterionSshInteractiveStepVisitor(null, null, null, null));

			doReturn(
					Optional.of("\r"),
					Optional.of("h"), Optional.of("o"), Optional.of("s"), Optional.of("t"), Optional.of(" "), Optional.of("$"),
					Optional.of("\r"),
					Optional.empty())
			.when(visitor).read(anyString(), eq(1), anyInt());

			visitor.visit(step);
			assertEquals(Optional.empty(), visitor.getResult());
		}

		// check text found capture true
		{
			final WaitFor step = new WaitFor();
			step.setText("$");
			step.setTimeout(30L);
			step.setCapture(true);

			final CriterionSshInteractiveStepVisitor visitor =
					spy(new CriterionSshInteractiveStepVisitor(null, null, null, null));

			doReturn(
					Optional.of("\r"),
					Optional.of("h"), Optional.of("o"), Optional.of("s"), Optional.of("t"), Optional.of(" "), Optional.of("$"),
					Optional.of("\r"),
					Optional.empty())
			.when(visitor).read(anyString(), eq(1), anyInt());

			visitor.visit(step);
			assertEquals(Optional.of("host $"), visitor.getResult());
		}
	}

	@Test
	void testVisitWaitForPrompt() throws Exception {

		assertThrows(
				IllegalArgumentException.class,
				() -> spy(new CriterionSshInteractiveStepVisitor(null, null, null, null)).visit((WaitForPrompt) null));

		try (final MockedStatic<CriterionSshInteractiveStepVisitor> mockedVisitor = mockStatic(CriterionSshInteractiveStepVisitor.class)) {

			final WaitForPrompt step = new WaitForPrompt();
			step.setTimeout(30L);

			final CriterionSshInteractiveStepVisitor visitor =
					spy(new CriterionSshInteractiveStepVisitor(null, null, null, null));

			doNothing().when(visitor).write(anyString(), eq(HardwareConstants.NEW_LINE));
			doReturn(Optional.of("Host plaform v0.0\nprompt>\n"), Optional.empty(), Optional.of("prompt>\n"))
			.when(visitor).readAll(anyString(), anyInt());

			assertThrows(StepException.class, () -> visitor.visit(step));
		}
	}

	@Test
	void testBuildStepName() {
		final GetAvailable step = new GetAvailable();
		step.setIndex(1);

		assertEquals(
				"Step(1) GetAvailable: hostname: host", 
				spy(new CriterionSshInteractiveStepVisitor(null, "host", null, null)).buildStepName(step));
	}

	@Test
	void testGetTimeout() {

		// check timeout from step
		assertEquals(1, spy(new CriterionSshInteractiveStepVisitor(null, null, null, null)).getTimeout(1L));

		// check timeout default
		{
			final SSHProtocol sshProtocol = mock(SSHProtocol.class);
			doReturn(null).when(sshProtocol).getTimeout();
			assertEquals(15, spy(new CriterionSshInteractiveStepVisitor(null, null, sshProtocol, null)).getTimeout(null));
			assertEquals(15, spy(new CriterionSshInteractiveStepVisitor(null, null, sshProtocol, null)).getTimeout(0L));
		}

		// check timeout from SSH protocol
		{
			final SSHProtocol sshProtocol = mock(SSHProtocol.class);
			doReturn(30L).when(sshProtocol).getTimeout();
			assertEquals(30, spy(new CriterionSshInteractiveStepVisitor(null, null, sshProtocol, null)).getTimeout(null));
			assertEquals(30, spy(new CriterionSshInteractiveStepVisitor(null, null, sshProtocol, null)).getTimeout(0L));
		}
	}

	@Test
	void testRead() throws Exception {
		final SSHClient sshClient = mock(SSHClient.class);

		final CriterionSshInteractiveStepVisitor visitor =
				spy(new CriterionSshInteractiveStepVisitor(sshClient, null, null, null));

		doThrow(IOException.class).when(sshClient).read(-1, 30);
		assertThrows(StepException.class, () -> visitor.read("Step(1) GetAvailable: hostname: host", -1, 30));

		doReturn(Optional.of("$")).when(sshClient).read(1, 30);
		assertEquals(Optional.of("$"), visitor.read("Step(1) WaitFor: hostname: host", 1, 30));
	}

	@Test
	void testWrite() throws Exception {
		final SSHClient sshClient = mock(SSHClient.class);

		final CriterionSshInteractiveStepVisitor visitor =
				spy(new CriterionSshInteractiveStepVisitor(sshClient, null, null, null));

		final String message = "Step(1) SendText: hostname: host - Couldn't send the following text through SSH:\n";

		// check text null
		visitor.write(message, null);
		verify(sshClient, never()).write(anyString());

		// check IOException
		doThrow(IOException.class).when(sshClient).write("quit\n");
		assertThrows(StepException.class, () -> visitor.write(message, "quit\n"));
		
		// check replace \\n
		visitor.write(message, "show enclosure info\\n");
		verify(sshClient, times(1)).write("show enclosure info\n");

		// check without replace
		visitor.write(message, "user");
		verify(sshClient, times(1)).write("user");
	}
}
