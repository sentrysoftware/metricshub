package com.sentrysoftware.matrix.engine.strategy.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.sentrysoftware.matrix.common.exception.NoCredentialProvidedException;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.GetUntilPrompt;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.SendPassword;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.SendText;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.SendUsername;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.Step;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.WaitFor;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.WaitForPrompt;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SshProtocol;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matsya.ssh.SSHClient;

class SshInteractiveHelperTest {

	@Test
	void testRunSshInteractiveEngineNull() throws Exception {
		assertThrows(
				IllegalArgumentException.class,
				() -> SshInteractiveHelper.runSshInteractive(null, List.of(), "SshInteractive(1)"));
	}

	@Test
	void testRunSshInteractiveStepsNull() throws Exception {
		assertThrows(
				IllegalArgumentException.class,
				() -> SshInteractiveHelper.runSshInteractive(EngineConfiguration.builder().build(), null, "SshInteractive(1)"));
	}

	@Test
	void testRunSshInteractiveSshProtocolNull() throws Exception {
		assertThrows(
				IllegalStateException.class,
				() -> SshInteractiveHelper.runSshInteractive(EngineConfiguration.builder().build(), List.of(), "SshInteractive(1)"));
	}

	@Test
	void testRunSshInteractiveUsernameNull() throws Exception {
		assertThrows(
				NoCredentialProvidedException.class,
				() -> SshInteractiveHelper.runSshInteractive(
						EngineConfiguration.builder().protocolConfigurations(
								Map.of(SshProtocol.class, SshProtocol.builder().build())).build(),
						List.of(),
						"SshInteractive(1)"));
	}

	@Test
	void testRunSshInteractiveUsernameEmpty() throws Exception {
		assertThrows(
				NoCredentialProvidedException.class,
				() -> SshInteractiveHelper.runSshInteractive(
						EngineConfiguration.builder()
							.protocolConfigurations(
									Map.of(
											SshProtocol.class,
											SshProtocol.builder().username(HardwareConstants.EMPTY).build()))
							.build(),
						List.of(),
						"SshInteractive(1)"));
	}
	
	@Test
	void testRunSshInteractiveOK() throws Exception {

		final EngineConfiguration engineConfiguration = EngineConfiguration.builder()
				.protocolConfigurations(Map.of(
						SshProtocol.class,
						SshProtocol.builder().username("user").password("pwd".toCharArray()).build()))
				.target(HardwareTarget.builder().hostname("host").build())
				.build();

		final WaitForPrompt waitForPrompt = new WaitForPrompt();

		final GetUntilPrompt getUntilPrompt = new GetUntilPrompt();

		final WaitFor waitForLogin = new WaitFor();
		waitForLogin.setText("ogin:");
		waitForLogin.setIgnored(true);

		final SendUsername sendUsername = new SendUsername();
		sendUsername.setIgnored(true);

		final WaitFor waitForPassword = new WaitFor();
		waitForPassword.setText("ssword:");
		waitForPassword.setIgnored(true);

		final SendPassword sendPassword = new SendPassword();
		sendPassword.setIgnored(true);

		final WaitFor waitFor = new WaitFor();
		waitFor.setText(">");
		waitFor.setTimeout(30L);
		waitFor.setCapture(true);

		final SendText sendText = new SendText();
		sendText.setText("quit\\n");

		final List<Step> steps = List.of(
				waitForPrompt, getUntilPrompt,
				waitForLogin, sendUsername, waitForPassword, sendPassword,
				waitFor, sendText);

		final String currentSourceTag = "SshInteractive(1)";

		final SSHClient sshClient = mock(SSHClient.class);

		try (final MockedStatic<MatsyaClientsExecutor> mockedMatsyaClientsExecutor = mockStatic(MatsyaClientsExecutor.class)) {

			mockedMatsyaClientsExecutor.when(
					() -> MatsyaClientsExecutor.connectSshClientTerminal("host", "user", "pwd".toCharArray(), null, 30))
			.thenReturn(sshClient);

			doReturn(Optional.of("x >")).when(sshClient).read(eq(-1), anyInt());
			doReturn(Optional.of("x"), Optional.of(" "), Optional.of(">"), Optional.of(">")).when(sshClient).read(eq(1), anyInt());

			doNothing().when(sshClient).write(anyString());

			assertEquals(
					List.of(">"),
					SshInteractiveHelper.runSshInteractive(engineConfiguration, steps, currentSourceTag));
		}
	}
}
