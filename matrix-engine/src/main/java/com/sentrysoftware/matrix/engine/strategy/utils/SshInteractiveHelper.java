package com.sentrysoftware.matrix.engine.strategy.utils;

import static org.springframework.util.Assert.state;

import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.common.exception.MatsyaException;
import com.sentrysoftware.matrix.common.exception.NoCredentialProvidedException;
import com.sentrysoftware.matrix.common.exception.StepException;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.Step;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SSHProtocol;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matsya.ssh.SSHClient;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SshInteractiveHelper {

	/**
	 * Run The SSH Interactive Steps processing.
	 * 
	 * @param engineConfiguration
	 * @param steps The Step list to process
	 * @return
	 * @throws StepException When an error occurred in the Step processing
	 * @throws MatsyaException When an error occurred in the SSH
	 * @throws NoCredentialProvidedException When no credential for the user is provided
	 */
	public static List<String> runSshInteractive(
			@NonNull
			final EngineConfiguration engineConfiguration,
			@NonNull
			final List<Step> steps)
					throws StepException,
					MatsyaException,
					NoCredentialProvidedException {

		final SSHProtocol sshProtocol =
				(SSHProtocol) engineConfiguration.getProtocolConfigurations().get(SSHProtocol.class);
		state(sshProtocol != null, "Can't find SSHProtocol in ProtocolConfigurations.");

		final String username = sshProtocol.getUsername();
		if (username == null || username.isBlank()) {
			throw new NoCredentialProvidedException();
		}

		final String hostname = engineConfiguration.getTarget().getHostname();

		final int timeout = sshProtocol.getTimeout() != null ?
				sshProtocol.getTimeout().intValue() :
					SSHProtocol.DEFAULT_TIMEOUT.intValue();

		try (final SSHClient sshClient = MatsyaClientsExecutor.connectSshClientTerminal(
				hostname,
				username,
				sshProtocol.getPassword(),
				sshProtocol.getPrivateKey(),
				timeout)) {

			final List<String> resut = new ArrayList<>();

			String prompt = HardwareConstants.EMPTY;

			for (final Step step : steps) {
				if (step.isIgnored()) {
					continue;
				}

				final ISshInteractiveStepVisitor visitor = new SshInteractiveStepVisitor(
						sshClient,
						hostname,
						sshProtocol,
						prompt);
				step.accept(visitor);

				visitor.getResult().ifPresent(resut::add);

				prompt = visitor.getPrompt().orElse(prompt);
			}

			return resut;
		}
	}
}
