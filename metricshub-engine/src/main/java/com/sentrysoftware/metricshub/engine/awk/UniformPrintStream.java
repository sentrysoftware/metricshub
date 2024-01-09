package com.sentrysoftware.metricshub.engine.awk;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * Overrides the println method of {@link PrintStream}
 */
public class UniformPrintStream extends PrintStream {

	/**
	 * Construct a new instance of the {@link UniformPrintStream}.
	 *
	 * @param out {@link OutputStream} that needs to be passed to super class
	 */
	public UniformPrintStream(OutputStream out) {
		super(out);
	}

	@Override
	public void println() {
		this.write('\n');
	}
}
