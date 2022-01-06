package com.sentrysoftware.hardware.agent.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SelfClosingGuardService implements ApplicationRunner {

	@Override
	public void run(@NonNull final ApplicationArguments args) {

		// SelfClosingGuard can be disabled, for tests or for any other reason
		if (!args.containsOption("enable-self-closing-guard")) {
			return;
		}

		// Multiple ApplicationRunner instances could be defined in the application and
		// then the SpringApplication runs them in serial mode that's why our
		// SelfClosingGuardService runner shouldn't block another ApplicationRunner instance
		new Thread(this::exitApplicationOnClosedStdin).start();
	}

	/**
	 * Read the stdin and exit the application if the caller closes the channel
	 */
	void exitApplicationOnClosedStdin() {
		try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
			// in.readLine() is blocker
			log.info("Caller has closed stdin. Message: {}", in.readLine());
		} catch (Exception e) {
			log.error("An error has occurred reading stdin", e);
		}

		// Stops gracefully even if the caller received the SIGKILL
		exit();
	}

	/**
	 * Exit the program
	 */
	void exit() {
		System.exit(0);
	}

}
