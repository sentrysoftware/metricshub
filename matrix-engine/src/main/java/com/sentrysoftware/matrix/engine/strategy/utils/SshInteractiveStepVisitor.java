package com.sentrysoftware.matrix.engine.strategy.utils;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.sentrysoftware.matrix.common.exception.StepException;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.GetAvailable;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.GetUntilPrompt;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.SendPassword;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.SendText;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.SendUsername;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.Sleep;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.Step;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.WaitFor;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.WaitForPrompt;
import com.sentrysoftware.matrix.engine.protocol.SSHProtocol;
import com.sentrysoftware.matsya.ssh.SSHClient;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class SshInteractiveStepVisitor implements ISshInteractiveStepVisitor {

	private static final int DEFAULT_TIMEOUT = 15;

	private final SSHClient sshClient;
	private final String hostname;
	private final SSHProtocol sshProtocol;
	private final String inPrompt;
	private final String currentSourceTag;


	@Getter
	private Optional<String> result = Optional.empty();

	@Getter
	private Optional<String> prompt = Optional.empty();


	@Override
	public void visit(
			@NonNull
			final GetAvailable step) throws StepException {

		log.debug("Step({}) {}: Getting available text. hostname: {}",
				step.getIndex(),
				step.getClass().getSimpleName(),
				hostname);

		final String stepName = buildStepName(step);

		final int timeout = getTimeout(null);

		final Optional<String> maybe = readAll(stepName, timeout);

		log.debug("Step({}) {}: Got:\n{}\nhostname: {}",
				step.getIndex(),
				step.getClass().getSimpleName(),
				maybe,
				hostname);

		if (step.isCapture()) {
			result = maybe;
		}
	}

	@Override
	public void visit(
			@NonNull
			final GetUntilPrompt step) throws StepException {

		if (inPrompt == null || inPrompt.isEmpty()) {
			log.warn("Step({}) {}: Cannot wait for prompt. Haven't got one yet. hostname: {}",
					step.getIndex(),
					step.getClass().getSimpleName(),
					hostname);
			return;
		}

		log.debug("Step({}) {}: Getting until: {}  - Timeout: {} secs. hostname: {}", 
				step.getIndex(),
				step.getClass().getSimpleName(),
				inPrompt,
				step.getTimeout(),
				hostname);

		final String stepName = buildStepName(step);

		final Optional<String> maybe = getUntil(stepName, inPrompt, step.getTimeout());

		log.debug("Step({}) {}: Got:\n{}\nhostname: {}",
				step.getIndex(),
				step.getClass().getSimpleName(),
				maybe,
				hostname);

		if (maybe.isEmpty()) {
			throw new StepException(String.format("%s - Disconnected or timeout while waiting for the prompt (\"%s\") in SSH session",
					stepName,
					inPrompt));
		}

		if (step.isCapture()) {
			// Remove the trailing prompt
			result = maybe.map(getUntilResult -> getUntilResult.substring(0, getUntilResult.length() - inPrompt.length()));
		}
	}

	@Override
	public void visit(
			@NonNull
			final SendPassword step) throws StepException {

		log.debug("Step({}) {}: Sending password: **********. hostname: {}",
				step.getIndex(),
				step.getClass().getSimpleName(),
				hostname);

		final String stepName = buildStepName(step);

		final String message = String.format("%s - Couldn't send the password (********) through SSH", stepName);

		if (sshProtocol.getPassword() != null) {
			write(message, String.valueOf(sshProtocol.getPassword()) + HardwareConstants.NEW_LINE);
		} else {
			write(message, HardwareConstants.NEW_LINE);
		}
	}

	@Override
	public void visit(
			@NonNull
			final SendText step) throws StepException {
		
		if (step.getText() == null || step.getText().isEmpty()) {
			log.warn("Step({}) {}: No text to send in step. hostname: {}",
					step.getIndex(),
					step.getClass().getSimpleName(),
					hostname);
			return;
		}

		log.debug("Step({}) {}: Sending text:\n{}\nhostname: {}",
				step.getIndex(),
				step.getClass().getSimpleName(),
				step.getText(),
				hostname);

		final String stepName = buildStepName(step);

		final String message = String.format("%s - Couldn't send the following text through SSH:\n%s",
				stepName,
				step.getText());

		write(message, step.getText());
	}

	@Override
	public void visit(
			@NonNull
			final SendUsername step) throws StepException {

		log.debug("Step({}) {}: Sending username: {}. hostname: {}",
				step.getIndex(),
				step.getClass().getSimpleName(),
				sshProtocol.getUsername(),
				hostname);

		final String stepName = buildStepName(step);

		final String message = String.format("%s - Couldn't send the username (%s) through SSH",
				stepName,
				sshProtocol.getUsername());

		write(message, sshProtocol.getUsername() + HardwareConstants.NEW_LINE);
	}

	@Override
	public void visit(
			@NonNull
			final Sleep step) throws StepException {

		if (step.getDuration() == null || step.getDuration() <= 0) {
			return;
		}

		log.debug("Step({}) {}: Sleeping : {} secs. hostname: {}",
				step.getIndex(),
				step.getClass().getSimpleName(),
				step.getDuration(),
				hostname);

		final String stepName = buildStepName(step);

		sleep(stepName, step.getDuration());
	}

	@Override
	public void visit(
			@NonNull
			final WaitFor step) throws StepException {

		if (step.getText() == null || step.getText().isEmpty()) {
			log.warn("Step({}) {}: No specified text to wait for in step. hostname: {}",
					step.getIndex(),
					step.getClass().getSimpleName(),
					hostname);
			return;
		}

		log.debug("Step({}) {}: Waiting for: {}  - Timeout: {} secs. hostname: {}", 
				step.getIndex(),
				step.getClass().getSimpleName(),
				step.getText(),
				step.getTimeout(),
				hostname);

		final String stepName = buildStepName(step);

		final Optional<String> maybe = getUntil(stepName, step.getText(), step.getTimeout());
		if (maybe.isEmpty()) {
			throw new StepException(String.format("%s - Disconnected or timeout while waiting for \"%s\" through SSH",
					stepName,
					step.getText().replaceAll("\\R", HardwareConstants.EMPTY)));
		}
		if (step.isCapture()) {
			result = maybe;
		}
	}

	@Override
	public void visit(
			@NonNull
			final WaitForPrompt step) throws StepException {

		log.debug("Step({}) {}: Waiting prompt  - Timeout: {} secs. hostname: {}", 
				step.getIndex(),
				step.getClass().getSimpleName(),
				step.getTimeout(),
				hostname);

		final int timeout = getTimeout(step.getTimeout());

		final String stepName = buildStepName(step);
		
		final String message = String.format("%s - Disconnected or timeout while waiting for the prompt in SSH session.", stepName);

		final Optional<String> maybePrompt = executeTask(
				stepName,
				timeout,
				() -> {
					String prompt = HardwareConstants.EMPTY;
					String tentativePrompt = HardwareConstants.EMPTY;
					
					while (!prompt.equals(tentativePrompt) || prompt.equals(HardwareConstants.EMPTY)) {

						prompt = tentativePrompt;

						write(message, HardwareConstants.NEW_LINE);

						sleep(stepName, 1);
						
						// Read what we have but keep only the last line
						final Optional<String> maybe = readAll(stepName, timeout);
						
						tentativePrompt = maybe
								.map(result -> result.split("\\R"))
								.map(lines -> lines[lines.length -1])
								.map(line -> line.trim())
								.orElse(HardwareConstants.EMPTY);
					}
					
					return Optional.of(prompt);
				});
		if (maybePrompt.isEmpty()) {
			throw new StepException(message);
		} else {
			prompt = maybePrompt;
		}
	}



	/**
	 * Perform a sleep.
	 * 
	 * @param stepName The Step name
	 * @param duration The sleep duration in seconds
	 * @throws StepException When an error occurred in the Step processing
	 */
	static void sleep(final String stepName, final long duration) throws StepException {
		try {
			Thread.sleep(duration * 1000L);
		} catch (final Exception e) {
			throw new StepException(stepName, e);
		}
	}

	/**
	 * Build the Step name for debugging.
	 * 
	 * @param step The Step
	 * @return The Step name
	 */
	String buildStepName(final Step step) {
		return String.format("%s Step(%d) %s: hostname: %s",
				currentSourceTag,
				step.getIndex(),
				step.getClass().getSimpleName(),
				hostname);
	}

	/**
	 * <p>Get the timeout to use if present in this order:
	 * <li>The Step timeout</li>
	 * <li>The SSH timeout</li>
	 * <li>The default timeout (15s)</li>
	 * </p>
	 * @param stepTimeout The timeout defined in the Step
	 * @return The timeout
	 */
	int getTimeout(final Long stepTimeout) {
		if (stepTimeout != null && stepTimeout >= 1L) {
			return stepTimeout.intValue();
		}
		return sshProtocol.getTimeout() != null ? sshProtocol.getTimeout().intValue() : DEFAULT_TIMEOUT;
	}

	/**
	 * Read all the data from the SSH stdout and stderr.
	 * 
	 * @param stepName The Step name
	 * @param timeout The timeout in seconds
	 * @return the read data
	 * @throws StepException When an error occurred in the Step processing
	 */
	Optional<String> readAll(final String stepName, final int timeout) throws StepException {
		return read(stepName, -1, timeout);
	}

	/**
	 * Read the size number of data from the SSH stdout and stderr.
	 * 
	 * @param stepName The Step name
	 * @param size
	 * @param timeout The timeout in seconds
	 * @return the read data
	 * @throws StepException When an error occurred in the Step processing
	 */
	Optional<String> read(final String stepName, final int size, final int timeout) throws StepException {
		try {
			return sshClient.read(size, timeout);
		} catch (final Exception e) {
			throw new StepException(stepName, e);
		}
	}

	/**
	 * Write a text in the SSH stdin.
	 * 
	 * @param message The message to use if an error occurred.
	 * @param text The text to be written
	 * @throws StepException When an error occurred in the Step processing
	 */
	void write(final String message, final String text) throws StepException {
		try {
			if (text == null) {
				return;
			}
			sshClient.write(text.replace("\\n", "\n"));
		} catch (final Exception e) {
			throw new StepException(String.format("%s (error %s:%s)", message, e.getClass().getSimpleName(), e.getMessage()));
		}
	}

	/**
	 * Read all the data from the SSH stdout and stderr until the text is found or timeout occurred.
	 * 
	 * @param stepName The Step name
	 * @param text The text to be found.
	 * @param stepTimeout The timeout defined in the Step
	 * @return the read data containing the text
	 * @throws StepException When an error occurred in the Step processing
	 */
	Optional<String> getUntil(final String stepName, final String text, final Long stepTimeout) throws StepException {

		final int timeout = getTimeout(stepTimeout);

		return executeTask(
				stepName,
				timeout,
				() -> {
					final StringBuilder sb = new StringBuilder();
					Optional<String> maybe;

					while((maybe = read(stepName, 1, timeout)).isPresent()) {

						maybe
						.filter(x -> !x.equals("\r"))
						.ifPresent(sb::append);

						if (sb.toString().contains(text)) {
							return Optional.of(sb.toString());
						}
					}
					return Optional.empty();
				});
	}

	/**
	 * Execute the task.
	 * 
	 * @param stepName The Step name
	 * @param timeout The timeout in seconds
	 * @param task The task to execute
	 * @return the return of the task
	 * @throws StepException When an error occurred in the Step processing
	 */
	private static Optional<String> executeTask(
			final String stepName,
			final int timeout,
			final Callable<Optional<String>> task) throws StepException {

		final ExecutorService executor = Executors.newSingleThreadExecutor();
		
		final Future<Optional<String>> future = executor.submit(task);

		try {
			return future.get(timeout, TimeUnit.SECONDS);

		} catch (final TimeoutException e) {
			future.cancel(true);
			return Optional.empty();

		} catch (final InterruptedException | ExecutionException e) {
			throw new StepException(stepName, e);

		} finally {
			executor.shutdownNow();
		}
	}
}
