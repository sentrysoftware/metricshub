package com.sentrysoftware.matrix.connector.model.detection.criteria;


import static org.springframework.util.Assert.isTrue;

import java.io.Serializable;

import com.sentrysoftware.matrix.engine.strategy.detection.CriterionTestResult;
import com.sentrysoftware.matrix.engine.strategy.detection.ICriterionVisitor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Criterion implements Serializable {

	private static final long serialVersionUID = -3677479724786317941L;

	protected boolean forceSerialization;

	protected Integer index;

	public void setIndex(int index) {

		isTrue(index > 0, "Invalid index: " + index);
		this.index = index;
	}

	public abstract CriterionTestResult accept(final ICriterionVisitor criterionVisitor);

}
