package com.sentrysoftware.matrix.connector.model.identity.criterion;

import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.matrix.connector.deserializer.custom.DeviceKindSetDeserializer;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import com.sentrysoftware.matrix.strategy.detection.CriterionTestResult;
import com.sentrysoftware.matrix.strategy.detection.ICriterionProcessor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DeviceTypeCriterion extends Criterion {

	private static final long serialVersionUID = 1L;

	@JsonDeserialize(using = DeviceKindSetDeserializer.class)
	private Set<DeviceKind> keep = new HashSet<>();
	@JsonDeserialize(using = DeviceKindSetDeserializer.class)
	private Set<DeviceKind> exclude = new HashSet<>();

	@Builder
	public DeviceTypeCriterion(String type, boolean forceSerialization, Set<DeviceKind> keep, Set<DeviceKind> exclude) {
		super(type, forceSerialization);
		this.keep = keep == null ? new HashSet<>() : keep;
		this.exclude = exclude == null ? new HashSet<>() : exclude;
	}

	@Override
	public CriterionTestResult accept(ICriterionProcessor criterionProcessor) {
		return criterionProcessor.process(this);
	}

}
