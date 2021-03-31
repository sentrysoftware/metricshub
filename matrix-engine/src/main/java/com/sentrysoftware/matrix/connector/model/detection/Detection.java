package com.sentrysoftware.matrix.connector.model.detection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.connector.model.detection.criteria.Criteria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Detection implements Serializable {


	private static final long serialVersionUID = 3331545341360476220L;

	@Default
	private List<Criteria> criteria = new ArrayList<>();
}
