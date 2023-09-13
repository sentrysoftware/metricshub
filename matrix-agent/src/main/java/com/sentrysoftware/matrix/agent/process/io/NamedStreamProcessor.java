package com.sentrysoftware.matrix.agent.process.io;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * {@link StreamProcessor} implementation which adds the name to the beginning of the block
 * then forward next processing to the {@link StreamProcessor} destination.
 */
@Data
@RequiredArgsConstructor
public class NamedStreamProcessor implements StreamProcessor {

	@NonNull
	private final String name;
	@NonNull
	private final StreamProcessor destination;

	@Override
	public void process(final String block) {
		// Next processing
		destination.process(String.format("%s %s", name, block));
	}

}