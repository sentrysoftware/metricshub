package com.sentrysoftware.matrix.strategy;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static com.sentrysoftware.matrix.constants.Constants.THREAD_SLEEP_DURATION;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StrategyTest implements IStrategy {
	private long threadSleepDuration = 0L;

	private ContextExecutor contextExecutor = null;

	@Override
	public void prepare() {

	}

	@Override
	public void post() {

	}

	@Override
	public void run() {
		try {
			Thread.sleep(threadSleepDuration);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * This test checks whether execute() is executed without errors if there is no strategy timeout nor thread interruption
	 *
	 * @throws InterruptedException
	 */
	@Test
	public void testContextExecutorWithoutTimeoutWithoutInterruption() throws InterruptedException {
		contextExecutor = new ContextExecutor(this);
		assertTrue(contextExecutor.execute());
	}

	/**
	 * This test checks whether execute() will throw an InterruptedException when the thread is interrupted while waiting
	 */
	@Test
	public void testContextExecutorWithInterruptedThread() {
		contextExecutor = new ContextExecutor(this);
		assertThrows(InterruptedException.class, () -> {
			Thread.currentThread().interrupt();
			contextExecutor.execute();
		});
	}

	/**
	 * This test checks whether strategy timeout is elapsed before the executor termination
	 *
	 * @throws InterruptedException
	 */
	@Test
	@Disabled
	/**
	 * This test is disabled because it tests the real timeout. It will take 5 minutes to complete.
	 * Currently, it slows down the build process.
	 * We have to mock the contextExecutor later to use another smaller strategyTimeout value
	 */
	public void testContextExecutorWithExpiringStrategyTimeout() throws InterruptedException {
		contextExecutor = new ContextExecutor(this);
		this.threadSleepDuration = THREAD_SLEEP_DURATION;
		// ExecutorService.awaitTermination returns false if strategy timeout is elapsed before the executor termination
		assertFalse(contextExecutor.execute());
	}

}
