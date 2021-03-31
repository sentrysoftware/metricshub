package com.sentrysoftware.matrix.connector.model.detection.criteria;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Criteria implements Serializable {


	private static final long serialVersionUID = -3677479724786317941L;

	private boolean forceSerialization;
}
