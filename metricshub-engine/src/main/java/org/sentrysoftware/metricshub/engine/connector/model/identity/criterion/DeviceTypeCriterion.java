package org.sentrysoftware.metricshub.engine.connector.model.identity.criterion;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.DeviceKindSetDeserializer;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.detection.ICriterionProcessor;

/**
 * Criterion for specifying device types to keep or exclude.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeviceTypeCriterion extends Criterion {

	private static final long serialVersionUID = 1L;

	/**
	 * Set of device types to keep.
	 */
	@JsonDeserialize(using = DeviceKindSetDeserializer.class)
	private Set<DeviceKind> keep = new HashSet<>();

	/**
	 * Set of device types to exclude.
	 */
	@JsonDeserialize(using = DeviceKindSetDeserializer.class)
	private Set<DeviceKind> exclude = new HashSet<>();

	/**
	 * Constructor with builder for creating an instance of DeviceTypeCriterion.
	 *
	 * @param type               Type of the criterion.
	 * @param forceSerialization Flag indicating whether serialization should be forced.
	 * @param keep               Set of device types to keep.
	 * @param exclude            Set of device types to exclude.
	 */
	@Builder
	public DeviceTypeCriterion(String type, boolean forceSerialization, Set<DeviceKind> keep, Set<DeviceKind> exclude) {
		super(type, forceSerialization);
		this.keep = keep == null ? new HashSet<>() : keep;
		this.exclude = exclude == null ? new HashSet<>() : exclude;
	}

	/**
	 * Accepts the given criterion processor for evaluation.
	 *
	 * @param criterionProcessor The criterion processor to accept.
	 * @return The result of the criterion test.
	 */
	@Override
	public CriterionTestResult accept(ICriterionProcessor criterionProcessor) {
		return criterionProcessor.process(this);
	}
}
