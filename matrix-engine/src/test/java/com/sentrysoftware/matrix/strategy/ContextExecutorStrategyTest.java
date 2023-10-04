package com.sentrysoftware.matrix.strategy;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.DEFAULT_JOB_TIMEOUT;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;

class ContextExecutorStrategyTest implements IStrategy {

	private ContextExecutor contextExecutor = null;

	@Override
	public void prepare() {}

	@Override
	public void post() {}

	@Override
	public void run() {}

	/**
	 * This test checks whether execute() is executed without errors if there is no strategy timeout nor thread interruption
	 *
	 * @throws InterruptedException
	 */
	@Test
	void testContextExecutorWithoutTimeoutWithoutInterruption() {
		contextExecutor = new ContextExecutor(this);
		assertDoesNotThrow(() -> contextExecutor.execute());
	}

	/**
	 * This test checks whether execute() will throw an InterruptedException when the thread is interrupted while waiting
	 */
	@Test
	void testContextExecutorWithInterruptedThread() {
		contextExecutor = new ContextExecutor(this);
		assertThrows(
			InterruptedException.class,
			() -> {
				Thread.currentThread().interrupt();
				contextExecutor.execute();
			}
		);
	}

	/**
	 * This test checks whether strategy timeout is elapsed before the executor termination
	 */
	@Test
	void testContextExecutorWithExpiringStrategyTimeout() {
		contextExecutor =
			new ContextExecutor(
				new IStrategy() {
					@Override
					public void run() {
						while (true) {
							if (Thread.currentThread().isInterrupted()) {
								return;
							}
						}
					}

					@Override
					public void prepare() {
						// not implemented
					}

					@Override
					public void post() {
						// not implemented
					}

					@Override
					public long getStrategyTimeout() {
						// One second
						return 1;
					}
				}
			);

		// The context executor throws the TimeoutException
		assertThrows(TimeoutException.class, () -> contextExecutor.execute());
	}

	@Override
	public long getStrategyTimeout() {
		return DEFAULT_JOB_TIMEOUT;
	}
}
