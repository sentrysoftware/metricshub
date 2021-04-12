package com.sentrysoftware.hardware.cli.component.help;

import java.io.IOException;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.ParameterResolver;
import org.springframework.shell.Shell;
import org.springframework.shell.standard.CommandValueProvider;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.standard.commands.Help;

@ShellComponent
public class HardwareSentryHelp extends Help {

	public HardwareSentryHelp(List<ParameterResolver> parameterResolvers) {
		super(parameterResolvers);
	}

	@Autowired
	private Shell shell;

	@PostConstruct
	public void setCommandRegistry() {
		super.setCommandRegistry(shell);
	}

	@ShellMethod(value = "Hardware Sentry CLI Custom Help", key = "hardware-sentry-help")
	@Override
	public CharSequence help(
			@ShellOption(defaultValue = ShellOption.NULL, valueProvider = CommandValueProvider.class, value = { "-C",
					"--command" }, help = "The command to obtain help for.") String command)
			throws IOException {

		final CharSequence charSequence = super.help(command);

		if (command == null) {
			return charSequence;
		}

		final StringBuilder builder = new StringBuilder(charSequence);

		// ********************************
		// Override the built-in help HERE
		// ********************************

		return builder.toString().replace("\n\n\n", "\n\n");
	}
}
