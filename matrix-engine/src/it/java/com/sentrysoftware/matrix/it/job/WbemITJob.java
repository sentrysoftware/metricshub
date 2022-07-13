package com.sentrysoftware.matrix.it.job;

import org.jboss.weld.context.RequestContext;
import org.jboss.weld.context.unbound.UnboundLiteral;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

import com.sentrysoftware.emulation.image.EmulatorImageConfiguration;
import com.sentrysoftware.emulation.server.EmulatorServer;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class WbemITJob extends AbstractITJob {

	private static final Weld WELD = new Weld();

	private boolean serverOn;

	@Override
	public ITJob withServerRecordData(final String... recordDataPaths) throws Exception {

		final WeldContainer weldContainer = WELD.initialize();

		final RequestContext requestContext = weldContainer.select(RequestContext.class, UnboundLiteral.INSTANCE).get();
		requestContext.activate();

		final EmulatorImageConfiguration emulatorImageConfiguration = weldContainer.select(EmulatorImageConfiguration.class).get();
		emulatorImageConfiguration.setImage(recordDataPaths[0]);

		try (final EmulatorServer emulatorServer = weldContainer.select(EmulatorServer.class).get()) {
			emulatorServer.run();
		}

		serverOn = true;

		return this;
	}

	@Override
	public void stopServer() {
		serverOn = false;
		WELD.shutdown();
	}

	@Override
	public boolean isServerStarted() {
		return serverOn;
	}

}
