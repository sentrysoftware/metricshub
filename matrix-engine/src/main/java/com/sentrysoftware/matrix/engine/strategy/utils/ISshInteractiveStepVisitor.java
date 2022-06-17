package com.sentrysoftware.matrix.engine.strategy.utils;

import java.util.Optional;

import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.IStepVisitor;

public interface ISshInteractiveStepVisitor extends IStepVisitor {

	public Optional<String> getResult();

	public Optional<String> getPrompt();

	public Boolean getCapture();
}
