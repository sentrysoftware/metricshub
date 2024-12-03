package org.sentrysoftware.metricshub.cli.http;

import static org.junit.jupiter.api.Assertions.*;

import org.fusesource.jansi.AnsiConsole;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.cli.service.PrintExceptionMessageHandlerService;
import picocli.CommandLine;

class HttpCliTest {

	HttpCli httpCli;

	void initHttpCli() {
		httpCli = new HttpCli();
		final CommandLine commandLine = new CommandLine(httpCli);

		commandLine.execute(
			"hostname",
			"--http",
			"--http-username",
			"username",
			"--http-password",
			"password",
			"--http-path",
			"path",
			"--http-url",
			"url",
			"--http-header",
			"key1, value1",
			"--http-header",
			"key2, value2",
			"--http-body",
			"body",
			"--http-authenticationToken",
			"authenticationToken",
			"--http-resultContent",
			"all"
		);
	}

	@Test
	void testValidate() {
		initHttpCli();
		assertDoesNotThrow(() -> httpCli.validate());
	}

	@Test
	void testExecute() {
		System.setProperty("log4j2.configurationFile", "log4j2-cli.xml");

		// Enable colors on Windows terminal
		AnsiConsole.systemInstall();

		httpCli = new HttpCli();
		final CommandLine commandLine = new CommandLine(httpCli);

		commandLine.setExecutionExceptionHandler(new PrintExceptionMessageHandlerService());

		commandLine.setCaseInsensitiveEnumValuesAllowed(true);

		commandLine.parseArgs(
			"netapp-e2824",
			"--http-method",
			"get",
			"--http-path",
			"/devmgr/v2/storage-systems/1",
			"--https",
			"--http-port",
			"443",
			"--http-username",
			"admin",
			"--http-password",
			"nationale"
		);

		try {
			Integer result = httpCli.call();
			System.out.println("result" + result);
		} catch (Exception e) {
			e.printStackTrace();
		}
		//		final int result = commandLine.execute(
		//			"netapp-e2824",
		//			"--http-method",
		//			"get",
		//			"--http-path",
		//			"/devmgr/v2/storage-systems/1",
		//			"--https",
		//			"--http-port",
		//			"443",
		//			"--http-username",
		//			"admin",
		//			"--http-password",
		//			"nationale"
		//		);
	}
}
