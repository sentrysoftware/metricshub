package com.sentrysoftware.matrix.agent.service.signal;

import com.sentrysoftware.matrix.agent.context.AgentInfo;
import com.sentrysoftware.matrix.agent.helper.ConfigHelper;
import com.sentrysoftware.matrix.agent.helper.OtelHelper;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SelfObserver extends AbstractObserver {

	private final Map<String, String> metricAttributes;
	private final Map<String, String> userAttributes;

	@Builder
	public SelfObserver(
		final SdkMeterProvider sdkMeterProvider,
		final Map<String, String> metricAttributes,
		final Map<String, String> userAttributes
	) {
		super(sdkMeterProvider);
		this.metricAttributes = metricAttributes;
		this.userAttributes = userAttributes;
	}

	@Override
	public void init() {
		final Map<String, String> attributeMap = new HashMap<>();

		// Add our attributes
		ConfigHelper.mergeAttributes(metricAttributes, attributeMap);

		// Override with the user's attributes
		ConfigHelper.mergeAttributes(userAttributes, attributeMap);

		// Build the OTEL attributes instance
		final Attributes attributes = OtelHelper.buildOtelAttributesFromMap(attributeMap);

		// Register a recorder
		sdkMeterProvider
			.get("com.sentrysoftware.metricshub.agent")
			.gaugeBuilder(AgentInfo.METRICS_HUB_AGENT_METRIC_NAME)
			.setDescription("MetricsHub agent information.")
			.ofLongs()
			.buildWithCallback(recorder -> recorder.record(1, attributes));
	}
}
