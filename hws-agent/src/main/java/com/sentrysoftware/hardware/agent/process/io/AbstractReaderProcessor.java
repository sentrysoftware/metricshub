package com.sentrysoftware.hardware.agent.process.io;

import java.io.Reader;

import lombok.AllArgsConstructor;
import lombok.NonNull;

@AllArgsConstructor
public abstract class AbstractReaderProcessor implements Runnable {

	@NonNull
	protected final Reader reader;
	@NonNull
	protected final StreamProcessor streamProcessor;

}
