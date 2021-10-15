package com.sentrysoftware.matrix.connector.model.common.sshinteractive.step;

import com.sentrysoftware.matrix.common.exception.StepException;

public interface IStepVisitor {

	void visit(final GetAvailable step) throws StepException;

	void visit(final GetUntilPrompt step) throws StepException;

	void visit(final SendPassword step) throws StepException;

	void visit(final SendText step) throws StepException;

	void visit(final SendUsername step) throws StepException;

	void visit(final Sleep step) throws StepException;

	void visit(final WaitFor step) throws StepException;

	void visit(final WaitForPrompt step) throws StepException;

}
