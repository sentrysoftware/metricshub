package com.sentrysoftware.matrix.connector.model.common.sshinteractive.step;

import java.io.Serializable;

import com.sentrysoftware.matrix.common.exception.StepException;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@NoArgsConstructor
public abstract class Step implements Serializable {

	private static final long serialVersionUID = 1631362528155294870L;

	@NonNull
	protected Integer index;

	protected boolean capture;

	/** If true this step is not processed. (Mainly when parameter TelnetOnly is setted) */
	protected boolean ignored;

	public abstract void accept(final IStepVisitor visitor) throws StepException;
}
