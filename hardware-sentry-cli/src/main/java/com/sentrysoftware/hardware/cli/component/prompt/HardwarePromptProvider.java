package com.sentrysoftware.hardware.cli.component.prompt;

import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.jline.PromptProvider;
import org.springframework.stereotype.Component;

@Component
public class HardwarePromptProvider implements PromptProvider {

	@Override
	public AttributedString getPrompt() {
		return new AttributedString("hardware-sentry:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
	}
}
