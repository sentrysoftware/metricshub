package com.sentrysoftware.matrix.engine.strategy.utils;

import java.util.Optional;
import java.util.concurrent.Callable;
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
import com.sentrysoftware.matrix.engine.protocol.SshProtocol;
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
	private final SshProtocol sshProtocol;
	private final String inPrompt;
	private final String currentSourceTag;

	private boolean capture;

	private static final String LOG_BEGIN_OPERATION_TEMPLATE = "Hostname {} - Executing Step [{}]:\n{}\n";
	private static final String LOG_BEGIN_OPERATION_TEMPLATE_WITH_TIMEOUT = "Hostname {} - Executing Step [{} with timeout={}]:\n{}\n";
	private static final String LOG_RESULT_TEMPLATE = "Hostname {} - {}: Got:\n{}";

	@Getter
	private Optional<String> result = Optional.empty();

	@Getter
	private Optional<String> prompt = Optional.empty();

	@Override
	public void visit(@NonNull	final GetAvailable step) throws StepException {

		final String stepName = buildStepName(step);
		final int timeout = getTimeout(null);

		capture = step.isCapture();

		log.info(LOG_BEGIN_OPERATION_TEMPLATE_WITH_TIMEOUT, hostname, stepName, timeout, step.toString());

		final Optional<String> maybe = readAll(stepName, timeout);

		log.info(LOG_RESULT_TEMPLATE, hostname, stepName, maybe);

		if (capture) {
			result = maybe;
		}
	}

	@Override
	public void visit(@NonNull	final GetUntilPrompt step) throws StepException {

		final String stepName = buildStepName(step);

		if (inPrompt == null || inPrompt.isEmpty()) {
			log.warn("Hostname {} - {}: No prompts were received.", hostname, stepName);
			return;
		}

		final int timeout = getTimeout(step.getTimeout());

		capture = step.isCapture();

		log.info(LOG_BEGIN_OPERATION_TEMPLATE_WITH_TIMEOUT, hostname, stepName, timeout, step.toString());

		final Optional<String> maybe = getUntil(stepName, inPrompt, timeout);

		log.info(LOG_RESULT_TEMPLATE, hostname, stepName, maybe);

		if (maybe.isEmpty()) {
			throw new StepException(String.format("%s - Disconnected or timed out while waiting for the prompt (\"%s\") in the SSH session.",
					stepName,
					inPrompt));
		}

		if (capture) {
			// Remove the trailing prompt
			result = maybe.map(getUntilResult -> getUntilResult.substring(0, getUntilResult.length() - inPrompt.length()));
		}
	}

	@Override
	public void visit(@NonNull	final SendPassword step) throws StepException {

		final String stepName = buildStepName(step);

		log.info(LOG_BEGIN_OPERATION_TEMPLATE, hostname, stepName, step.toString());

		final String message = String.format("%s - Could not send the password (********) through SSH.", stepName);

		if (sshProtocol.getPassword() != null) {
			write(message, String.valueOf(sshProtocol.getPassword()) + HardwareConstants.NEW_LINE);
		} else {
			write(message, HardwareConstants.NEW_LINE);
		}
	}

	@Override
	public void visit(@NonNull	final SendText step) throws StepException {

		final String stepName = buildStepName(step);

		if (step.getText() == null || step.getText().isEmpty()) {
			log.warn("Hostname {} - {}: No text to send in step.", hostname, stepName);
			return;
		}

		log.info(LOG_BEGIN_OPERATION_TEMPLATE, hostname, stepName, step.toString());

		final String message = String.format("%s - Could not send the following text through SSH:\n%s", // NOSONAR
				stepName,
				step.getText());

		write(message, step.getText());
	}

	@Override
	public void visit(@NonNull final SendUsername step) throws StepException {

		final String stepName = buildStepName(step);

		log.info("Hostname {} - Executing Step [{} with username={}]:\n{}\n", 
				hostname, stepName, sshProtocol.getUsername(), step.toString());

		final String message = String.format("%s - Could not send the username (%s) through SSH.",
				stepName,
				sshProtocol.getUsername());

		write(message, sshProtocol.getUsername() + HardwareConstants.NEW_LINE);
	}

	@Override
	public void visit(@NonNull final Sleep step) throws StepException {

		if (step.getDuration() == null || step.getDuration() <= 0) {
			return;
		}

		final String stepName = buildStepName(step);

		log.info(LOG_BEGIN_OPERATION_TEMPLATE, hostname, stepName, step.toString());

		sleep(stepName, step.getDuration());
	}

	@Override
	public void visit(@NonNull final WaitFor step) throws StepException {

		final String stepName = buildStepName(step);

		if (step.getText() == null || step.getText().isEmpty()) {
			log.warn("Hostname {} - {}: No specified text to expect in step.", hostname, stepName);
			return;
		}

		capture = step.isCapture();

		final int timeout = getTimeout(step.getTimeout());

		log.info(LOG_BEGIN_OPERATION_TEMPLATE_WITH_TIMEOUT, hostname, stepName, timeout, step.toString());

		final Optional<String> maybe = getUntil(stepName, step.getText(), timeout);
		if (maybe.isEmpty()) {
			throw new StepException(String.format("%s - Disconnected or timeout while waiting for \"%s\" through SSH.",
					stepName,
					step.getText().replaceAll("\\R", HardwareConstants.EMPTY)));
		}

		log.info(LOG_RESULT_TEMPLATE, hostname, stepName, maybe);

		if (capture) {
			result = maybe;
		}
	}

	@Override
	public void visit(@NonNull	final WaitForPrompt step) throws StepException {

		final String stepName = buildStepName(step);
		final int timeout = getTimeout(step.getTimeout());

		log.info(LOG_BEGIN_OPERATION_TEMPLATE_WITH_TIMEOUT, hostname, stepName, timeout, step.toString());

		final String message = String.format("%s - Disconnected or timeout while waiting for the prompt in SSH session.", stepName);

		final Optional<String> maybePrompt = executeTask(
				stepName,
				timeout,
				() -> {
					String promptResult = HardwareConstants.EMPTY;
					String tentativePrompt = HardwareConstants.EMPTY;
					
					while (!promptResult.equals(tentativePrompt) || promptResult.equals(HardwareConstants.EMPTY)) {

						promptResult = tentativePrompt;

						write(message, HardwareConstants.NEW_LINE);

						sleep(stepName, 1);
						
						// Read what we have but keep only the last line
						final Optional<String> maybe = readAll(stepName, timeout);
						
						tentativePrompt = maybe
								.map(data -> data.split("\\R"))
								.map(lines -> lines[lines.length -1])
								.map(String::trim)
								.orElse(HardwareConstants.EMPTY);
					}

					return Optional.of(promptResult);
				});

		if (maybePrompt.isEmpty()) {
			throw new StepException(message);
		} else {

			log.info(LOG_RESULT_TEMPLATE, hostname, stepName, maybePrompt);

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
		return String.format("%s.step(%d) %s",
				currentSourceTag,
				step.getIndex(),
				step.getClass().getSimpleName());
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
	 * Read all the data from the SSH stdout and stderr until the text is found or
	 * timeout occurred.
	 * 
	 * @param stepName The Step name
	 * @param text     The text to be found.
	 * @param timeout  The timeout to use when executing the read task
	 * @return the read data containing the text
	 * @throws StepException When an error occurred in the Step processing
	 */
	Optional<String> getUntil(final String stepName, final String text, final int timeout) throws StepException {

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

		} catch (final Exception e) {
			throw new StepException(stepName, e);

		} finally {
			executor.shutdownNow();
		}
	}
}
