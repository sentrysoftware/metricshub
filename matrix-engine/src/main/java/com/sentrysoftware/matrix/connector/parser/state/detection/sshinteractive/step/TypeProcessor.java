package com.sentrysoftware.matrix.connector.parser.state.detection.sshinteractive.step;

import static org.springframework.util.Assert.isTrue;
import static org.springframework.util.Assert.notNull;
import static org.springframework.util.Assert.state;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.Step;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.sshinteractive.SshInteractive;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor
public class TypeProcessor extends AbstractStateParser {

	private static final Pattern TYPE_KEY_PATTERN = Pattern.compile(
			"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.Step\\(([1-9]\\d*)\\)\\.Type\\s*$",
			Pattern.CASE_INSENSITIVE);

	private final Class<? extends Step> type;
	private final String typeValue;

	@Override
	protected Matcher getMatcher(
			@NonNull
			String key) {
		return TYPE_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		isTrue(getTypeValue().equalsIgnoreCase(value), String.format("Invalid Step type: %s", value));

		final Matcher matcher = getMatcher(key);
		isTrue(matcher.matches(), String.format("Invalid key: %s.", key));

		Detection detection = connector.getDetection();
		if (detection == null) {
			detection = new Detection();
			connector.setDetection(detection);
		}

		final List<Criterion> criteria = detection.getCriteria();
		notNull(detection, String.format("No criteria in the connector for the following key: %s.", key));

		final int criterionIndex = getCriterionIndex(matcher);
		isTrue(criterionIndex <= criteria.size(),
				String.format("Criterion %d not detected yet for the following key: %s.", criterionIndex, key));

		final Criterion criterion = detection.getCriteria().get(criterionIndex -1);
		state(criterion != null && criterion instanceof SshInteractive, "criterion should be SshInteractive");
		
		final SshInteractive sshInteractive = (SshInteractive) criterion;

		try {			

			final int stepIndex = getDetectionStepIndex(matcher);

			final Step step = type.getConstructor().newInstance();
			step.setIndex(stepIndex);

			if (sshInteractive.getSteps() == null) {
				sshInteractive.setSteps(new ArrayList<>());
			}
			sshInteractive.getSteps().add(step);

		} catch (final Exception e) {
			throw new IllegalStateException(String.format("TypeProcessor parse: Could not instantiate %s Step: %s",
					type.getSimpleName(),
					e.getMessage()));
		}
	}
}
