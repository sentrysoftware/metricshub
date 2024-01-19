package com.sentrysoftware.metricshub.agent.process.io;

import com.sentrysoftware.metricshub.agent.process.config.Slf4jLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.slf4j.Logger;

/**
 * Use this {@link StreamProcessor} in order to consume process output blocks
 * using the Slf4j logger facade.
 */
@AllArgsConstructor
public class Slf4jStreamProcessor implements StreamProcessor {

	@NonNull
	private final Logger logger;

	@NonNull
	private final Slf4jLevel level;

	@Override
	public void process(final String block) {
		// Simply log the block with the current logger
		level.withLogger(logger).log(block);
	}
}
