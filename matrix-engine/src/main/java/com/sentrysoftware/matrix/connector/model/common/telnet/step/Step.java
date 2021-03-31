package com.sentrysoftware.matrix.connector.model.common.telnet.step;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Step implements Serializable {

	private static final long serialVersionUID = 1631362528155294870L;

	private boolean capture;
	private boolean telnetOnly;
}
