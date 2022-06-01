package com.sentrysoftware.matrix.engine.strategy.utils;

import static org.springframework.util.Assert.state;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.common.exception.MatsyaException;
import com.sentrysoftware.matrix.common.exception.NoCredentialProvidedException;
import com.sentrysoftware.matrix.common.exception.StepException;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.Step;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SshProtocol;
import com.sentrysoftware.matrix.engine.protocol.AbstractCommand;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matsya.ssh.SSHClient;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SshInteractiveHelper {

	/**
	 * Run The SSH Interactive Steps processing.
	 * 
	 * @param engineConfiguration
	 * @param steps The Step list to process
	 * @param currentSourceTag A tag to indicate the source or criterion in debug
	 * @return
	 * @throws StepException When an error occurred in the Step processing
	 * @throws MatsyaException When an error occurred in the SSH
	 * @throws NoCredentialProvidedException When no credential for the user is provided
	 */
	public static List<String> runSshInteractive(
			@NonNull
			final EngineConfiguration engineConfiguration,
			@NonNull
			final List<Step> steps,
			final String currentSourceTag)
					throws StepException,
					MatsyaException,
					NoCredentialProvidedException {

		final SshProtocol sshProtocol =
				(SshProtocol) engineConfiguration.getProtocolConfigurations().get(SshProtocol.class);
		state(sshProtocol != null, "Can't find SSHProtocol in ProtocolConfigurations.");

		final String username = sshProtocol.getUsername();
		if (username == null || username.isBlank()) {
			throw new NoCredentialProvidedException();
		}

		final String hostname = engineConfiguration.getHost().getHostname();

		final int timeout = sshProtocol.getTimeout() != null ?
				sshProtocol.getTimeout().intValue() :
					AbstractCommand.DEFAULT_TIMEOUT.intValue();

		try (final SSHClient sshClient = MatsyaClientsExecutor.connectSshClientTerminal(
				hostname,
				username,
				sshProtocol.getPassword(),
				sshProtocol.getPrivateKey(),
				timeout)) {

			final List<String> results = new ArrayList<>();

			String prompt = HardwareConstants.EMPTY;

			for (final Step step : steps) {
				if (step.isIgnored()) {
					continue;
				}

				final ISshInteractiveStepVisitor visitor = new SshInteractiveStepVisitor(
						sshClient,
						hostname,
						sshProtocol,
						prompt,
						currentSourceTag);
				step.accept(visitor);

				visitor.getResult().ifPresent(result -> {
					final String[] array = result.split("\\R");
					if (array.length == 1) {
						results.add(array[0]);
					} else {
						results.addAll(Stream.of(array).collect(Collectors.toList()));
					}
				});

				prompt = visitor.getPrompt().orElse(prompt);
			}

			log.debug("Hostname {} - Run SSH Interactive: Result for {}:\n {}",
					hostname,
					currentSourceTag,
					results.stream().collect(Collectors.joining("\",\n\"", "[\"", "\"]")));
			return results;
		}
	}
}
