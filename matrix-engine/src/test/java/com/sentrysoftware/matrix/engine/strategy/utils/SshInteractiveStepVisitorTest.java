package com.sentrysoftware.matrix.engine.strategy.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
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

class SshInteractiveStepVisitorTest {

	private static final String CURRENT_SOURCE_TAG = "sshInteractive enclosure.discovery.source(1)";

	@Test
	void testVisitGetAvailable() throws Exception {

		final SshInteractiveStepVisitor visitor =
				spy(new SshInteractiveStepVisitor(null, null, mock(SSHProtocol.class), null, CURRENT_SOURCE_TAG));

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

		final SshInteractiveStepVisitor spy = spy(new SshInteractiveStepVisitor(null, null, null, null, CURRENT_SOURCE_TAG));
		assertThrows(IllegalArgumentException.class, () -> spy.visit((GetUntilPrompt) null));

		// check with inPrompt null
		{
			final GetUntilPrompt step = new GetUntilPrompt();
			step.setCapture(true);

			final SshInteractiveStepVisitor visitor =
					spy(new SshInteractiveStepVisitor(null, null, null, null, CURRENT_SOURCE_TAG));

			visitor.visit(step);
			verify(visitor, never()).getUntil(anyString(), anyString(), anyInt());
			assertEquals(Optional.empty(), visitor.getResult());
		}

		// check with inPrompt empty
		{
			final GetUntilPrompt step = new GetUntilPrompt();
			step.setCapture(true);

			final SshInteractiveStepVisitor visitor =
					spy(new SshInteractiveStepVisitor(null, null, null, "", CURRENT_SOURCE_TAG));
			
			visitor.visit(step);
			verify(visitor, never()).getUntil(anyString(), anyString(), anyInt());
			assertEquals(Optional.empty(), visitor.getResult());
		}

		// check prompt not found
		{
			final GetUntilPrompt step = new GetUntilPrompt();
			step.setTimeout(30L);
			step.setCapture(true);

			final SshInteractiveStepVisitor visitor =
					spy(new SshInteractiveStepVisitor(null, null, null, "prompt>", CURRENT_SOURCE_TAG));

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

			final SshInteractiveStepVisitor visitor =
					spy(new SshInteractiveStepVisitor(null, null, null, "prompt>", CURRENT_SOURCE_TAG));

			doReturn(
					Optional.of("\r"),
					Optional.of("v"), Optional.of("a"), Optional.of("l"), Optional.of("u"), Optional.of("e"), Optional.of("\r"), Optional.of("\n"),
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

			final SshInteractiveStepVisitor visitor =
					spy(new SshInteractiveStepVisitor(null, null, null, "prompt>", CURRENT_SOURCE_TAG));

			doReturn(
					Optional.of("\r"),
					Optional.of("v"), Optional.of("a"), Optional.of("l"), Optional.of("u"), Optional.of("e"), Optional.of("\r"), Optional.of("\n"),
					Optional.of("p"), Optional.of("r"), Optional.of("o"), Optional.of("m"), Optional.of("p"), Optional.of("t"), Optional.of(">"),
					Optional.of("\r"), Optional.of(" "), Optional.of("."), Optional.of("."), Optional.of("."), Optional.of("\r"),
					Optional.empty())
			.when(visitor).read(anyString(), eq(1), anyInt());

			visitor.visit(step);
			assertEquals(Optional.of("value\n"), visitor.getResult());
		}
	}

	@Test
	void testVisitSendPassword() throws Exception {

		final SSHProtocol sshProtocol = mock(SSHProtocol.class);

		final SshInteractiveStepVisitor visitor =
				spy(new SshInteractiveStepVisitor(mock(SSHClient.class), null, sshProtocol, null, CURRENT_SOURCE_TAG));

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

		final SshInteractiveStepVisitor visitor =
				spy(new SshInteractiveStepVisitor(mock(SSHClient.class), null, null, null, CURRENT_SOURCE_TAG));

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

		final SshInteractiveStepVisitor visitor =
				spy(new SshInteractiveStepVisitor(mock(SSHClient.class), null, sshProtocol, null, CURRENT_SOURCE_TAG));

		assertThrows(IllegalArgumentException.class, () -> visitor.visit((SendUsername) null));

		doReturn("user").when(sshProtocol).getUsername();
		visitor.visit(new SendUsername());
		verify(visitor, times(1)).write(anyString(), eq("user\n"));
	}

	@Test
	void testVisitSleep() throws Exception {

		final SshInteractiveStepVisitor visitor =
				spy(new SshInteractiveStepVisitor(null, null, null, null, CURRENT_SOURCE_TAG));

		assertThrows(IllegalArgumentException.class, () -> visitor.visit((Sleep) null));

		// Check duration null
		try (final MockedStatic<SshInteractiveStepVisitor> mockedVisitor = mockStatic(SshInteractiveStepVisitor.class)) {

			visitor.visit(new Sleep());
			mockedVisitor.verifyNoInteractions();
		}

		// Check duration 0
		try (final MockedStatic<SshInteractiveStepVisitor> mockedVisitor = mockStatic(SshInteractiveStepVisitor.class)) {

			final Sleep step = new Sleep();
			step.setDuration(0L);

			visitor.visit(step);
			mockedVisitor.verifyNoInteractions();
		}

		// Check duration > 0
		try (final MockedStatic<SshInteractiveStepVisitor> mockedVisitor = mockStatic(SshInteractiveStepVisitor.class)) {

			final Sleep step = new Sleep();
			step.setDuration(1L);

			visitor.visit(step);
			mockedVisitor.verify(times(1), () -> SshInteractiveStepVisitor.sleep(anyString(), eq(1L))); 
		}
	}

	@Test
	void testVisitWaitFor() throws Exception {

		final SshInteractiveStepVisitor spy = spy(new SshInteractiveStepVisitor(null, null, null, null, CURRENT_SOURCE_TAG));
		assertThrows(
				IllegalArgumentException.class,
				() -> spy.visit((WaitFor) null));

		// check with text null
		{
			final WaitFor step = new WaitFor();
			step.setCapture(true);

			final SshInteractiveStepVisitor visitor =
					spy(new SshInteractiveStepVisitor(null, null, null, null, CURRENT_SOURCE_TAG));

			visitor.visit(step);
			verify(visitor, never()).getUntil(anyString(), anyString(), anyInt());
			assertEquals(Optional.empty(), visitor.getResult());
		}

		// check with text empty
		{
			final WaitFor step = new WaitFor();
			step.setText(HardwareConstants.EMPTY);
			step.setCapture(true);

			final SshInteractiveStepVisitor visitor =
					spy(new SshInteractiveStepVisitor(null, null,null, null, CURRENT_SOURCE_TAG));
			
			visitor.visit(step);
			verify(visitor, never()).getUntil(anyString(), anyString(), anyInt());
			assertEquals(Optional.empty(), visitor.getResult());
		}

		// check text not found
		{
			final WaitFor step = new WaitFor();
			step.setText("$");
			step.setTimeout(30L);
			step.setCapture(true);

			final SshInteractiveStepVisitor visitor =
					spy(new SshInteractiveStepVisitor(null, null, null, null, CURRENT_SOURCE_TAG));

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

			final SshInteractiveStepVisitor visitor =
					spy(new SshInteractiveStepVisitor(null, null, null, null, CURRENT_SOURCE_TAG));

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

			final SshInteractiveStepVisitor visitor =
					spy(new SshInteractiveStepVisitor(null, null, null, null, CURRENT_SOURCE_TAG));

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

		final SshInteractiveStepVisitor spy = spy(new SshInteractiveStepVisitor(null, null, null, null, CURRENT_SOURCE_TAG));
		assertThrows(
				IllegalArgumentException.class,
				() -> spy.visit((WaitForPrompt) null));

		try (final MockedStatic<SshInteractiveStepVisitor> mockedVisitor = mockStatic(SshInteractiveStepVisitor.class)) {

			final WaitForPrompt step = new WaitForPrompt();
			step.setTimeout(30L);

			final SshInteractiveStepVisitor visitor =
					spy(new SshInteractiveStepVisitor(null, null, null, null, CURRENT_SOURCE_TAG));

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
				CURRENT_SOURCE_TAG + ".step(1) GetAvailable", 
				spy(new SshInteractiveStepVisitor(null, "host", null, null, CURRENT_SOURCE_TAG)).buildStepName(step));
	}

	@Test
	void testGetTimeout() {

		// check timeout from step
		assertEquals(1, spy(new SshInteractiveStepVisitor(null, null, null, null, CURRENT_SOURCE_TAG)).getTimeout(1L));

		// check timeout default
		{
			final SSHProtocol sshProtocol = mock(SSHProtocol.class);
			doReturn(null).when(sshProtocol).getTimeout();
			assertEquals(15, spy(new SshInteractiveStepVisitor(null, null, sshProtocol, null, CURRENT_SOURCE_TAG)).getTimeout(null));
			assertEquals(15, spy(new SshInteractiveStepVisitor(null, null, sshProtocol, null, CURRENT_SOURCE_TAG)).getTimeout(0L));
		}

		// check timeout from SSH protocol
		{
			final SSHProtocol sshProtocol = mock(SSHProtocol.class);
			doReturn(30L).when(sshProtocol).getTimeout();
			assertEquals(30, spy(new SshInteractiveStepVisitor(null, null, sshProtocol, null, CURRENT_SOURCE_TAG)).getTimeout(null));
			assertEquals(30, spy(new SshInteractiveStepVisitor(null, null, sshProtocol, null, CURRENT_SOURCE_TAG)).getTimeout(0L));
		}
	}

	@Test
	void testRead() throws Exception {
		final SSHClient sshClient = mock(SSHClient.class);

		final SshInteractiveStepVisitor visitor =
				spy(new SshInteractiveStepVisitor(sshClient, null, null, null, CURRENT_SOURCE_TAG));

		doThrow(IOException.class).when(sshClient).read(-1, 30);
		assertThrows(StepException.class, () -> visitor.read("Step(1) GetAvailable: hostname: host", -1, 30));

		doReturn(Optional.of("$")).when(sshClient).read(1, 30);
		assertEquals(Optional.of("$"), visitor.read("Step(1) WaitFor: hostname: host", 1, 30));
	}

	@Test
	void testWrite() throws Exception {
		final SSHClient sshClient = mock(SSHClient.class);

		final SshInteractiveStepVisitor visitor =
				spy(new SshInteractiveStepVisitor(sshClient, null, null, null, CURRENT_SOURCE_TAG));

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
