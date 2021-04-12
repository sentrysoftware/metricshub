package com.sentrysoftware.hardware.cli.component.shell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import com.sentrysoftware.hardware.cli.component.pwd.PasswordReader;
import com.sentrysoftware.hardware.cli.service.EngineService;

@ShellComponent
public class HardwareShell {

	@Autowired
	private EngineService engineService;

	@Autowired
	private PasswordReader passwordReader;

	@ShellMethod("Hardware Sentry")
	public void monitorHardware(
			@ShellOption(value = { "-h", "--hostname" }, help = "Update me") final String hostname) {

		// If the password is not passed in the method arguments then call
		// 'passwordReader.prompt("Password", false)' to ask for the password.

		System.out.println(engineService.call(hostname));
	}
}
