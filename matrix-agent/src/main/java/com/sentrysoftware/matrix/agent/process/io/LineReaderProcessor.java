package com.sentrysoftware.matrix.agent.process.io;

import java.io.BufferedReader;
import java.io.Reader;

import lombok.NonNull;

/**
 * This runnable reads the process output lines, it blocks until some input is
 * available, an I/O error occurs, or the end of the stream is reached. <br>
 * The {@link StreamProcessor} is called for each available line.
 */
public class LineReaderProcessor extends AbstractReaderProcessor {

	public LineReaderProcessor(@NonNull Reader reader, @NonNull StreamProcessor streamProcessor) {
		super(reader, streamProcessor);
	}

	@Override
	public void run() {

		try (BufferedReader br = new BufferedReader(reader)) {
			String line;
			while ((line = br.readLine()) != null) {
				streamProcessor.process(line);
			}
		} catch (Exception e) {
			// Probably an IO error
		}

	}
}

