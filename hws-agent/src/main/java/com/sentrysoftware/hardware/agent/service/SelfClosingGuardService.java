package com.sentrysoftware.hardware.agent.service;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SelfClosingGuardService {

	/**
	 * Start the reader
	 */
	@PostConstruct
	public void start() {
		// Never block the start method
		new Thread(this::exitApplicationOnClosedStdin).start();
	}

	/**
	 * Read the stdin and exit the application if the caller closes the channel
	 */
	void exitApplicationOnClosedStdin() {

		try {
			// in.read() returns -1 if the end of the stream is reached
			while (System.in.read() != -1) {
				// Gobble every byte that's sent to us through stdin
			}
		} catch (Exception e) {
			/* Do nothing */
		}

		log.info("Parent process terminated. Exiting now.");

		// Exit the application
		exit();
	}

	/**
	 * Exit the program
	 */
	void exit() {
		System.exit(0);
	}

}
