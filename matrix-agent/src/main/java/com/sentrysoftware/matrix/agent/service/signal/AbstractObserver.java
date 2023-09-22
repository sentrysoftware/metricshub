package com.sentrysoftware.matrix.agent.service.signal;

import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public abstract class AbstractObserver {

	protected final SdkMeterProvider sdkMeterProvider;

	/**
	 * Initialize the observer
	 */
	public abstract void init();
}
