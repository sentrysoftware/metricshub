package com.sentrysoftware.hardware.cli.component.history;

import java.io.IOException;

import org.jline.reader.impl.history.DefaultHistory;
import org.springframework.stereotype.Component;

@Component
public class NoSaveHistory extends DefaultHistory {
	@Override
	public void save() throws IOException {
		// In our implementation save is not implemented as for now we don't need a
		// separated log file for all the executed commands. The logger will take care
		// of this.
	}
}