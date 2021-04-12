package com.sentrysoftware.hardware.cli.component.runner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.shell.Shell;
import org.springframework.shell.jline.InteractiveShellApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@Order(InteractiveShellApplicationRunner.PRECEDENCE - 100)
public class NonInteractiveShellRunner implements ApplicationRunner {

	private static final String SPRING_CONFIG_PREFIX = "--spring";

	public static final List<String> NON_INTERACTIVE_COMMANDS = Collections.singletonList("monitor-hardware");

	private final Shell shell;
	private final ConfigurableEnvironment environment;

	public NonInteractiveShellRunner(Shell shell, ConfigurableEnvironment environment) {
		this.shell = shell;
		this.environment = environment;
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {

		// Non interactive check.
		// Verify if the SpringBoot application is started with known arguments
		InteractiveShellApplicationRunner.disable(environment);
		final List<String> arguments = Arrays.asList(args.getSourceArgs());
		final Optional<String> knownArgument = NON_INTERACTIVE_COMMANDS.stream().filter(arguments::contains)
				.findFirst();

		if (knownArgument.isPresent()) {

			int index = arguments.indexOf(knownArgument.get());

			var input = arguments.stream().skip(index).filter(arg -> !arg.startsWith(SPRING_CONFIG_PREFIX))
					.collect(Collectors.joining(" "));

			shell.evaluate(() -> input);
			shell.evaluate(() -> "exit");
		} else {
			environment.getPropertySources().addFirst(new MapPropertySource("interactive.override", Collections
					.singletonMap(InteractiveShellApplicationRunner.SPRING_SHELL_INTERACTIVE_ENABLED, "true")));

		}

	}
}