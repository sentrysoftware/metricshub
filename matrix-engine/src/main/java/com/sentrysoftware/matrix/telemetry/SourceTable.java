package com.sentrysoftware.matrix.telemetry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SourceTable {

	private String rawData;
	private List<List<String>> table;
	private List<String> headers;
	private ReentrantLock forceSerializationLock;
}
