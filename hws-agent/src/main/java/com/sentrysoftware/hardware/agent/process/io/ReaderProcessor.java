package com.sentrysoftware.hardware.agent.process.io;

import java.io.Reader;

import lombok.NonNull;

/**
 * This thread reads the process output using a 512 characters buffer, it blocks until some input is
 * available, an I/O error occurs, or the end of the stream is reached. <br>
 * The {@link StreamProcessor} is called for each available block.
 */
public class ReaderProcessor extends AbstractReaderProcessor {

	public ReaderProcessor(@NonNull Reader reader, @NonNull StreamProcessor streamProcessor) {
		super(reader, streamProcessor);
	}

	private static final int CHAR_BUFFER_LENGTH = 512;

	@Override
	public void run() {
		try {
			int read;
			char[] buf = new char[CHAR_BUFFER_LENGTH];
			// Try to read a block
			while ((read = reader.read(buf)) != -1) {
				// Process the block
				streamProcessor.process(new String(buf, 0, read));
			}
		} catch (Exception e) {
			// Error for any unknown error
		}

	}

}
