package com.sentrysoftware.matrix.engine.strategy.utils;

import java.util.Optional;

import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.IStepVisitor;

public interface ICriterionSshInteractiveStepVisitor extends IStepVisitor {

	public Optional<String> getResult();

	public Optional<String> getPrompt();
}
