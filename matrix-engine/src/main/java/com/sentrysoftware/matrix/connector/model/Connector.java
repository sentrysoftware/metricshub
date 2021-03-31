package com.sentrysoftware.matrix.connector.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.connector.model.common.OSType;
import com.sentrysoftware.matrix.connector.model.common.TranslationTable;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Connector implements Serializable {


	private static final long serialVersionUID = 7362309567524807896L;

	private String compiledFilename;
	private String displayName;
	private String typicalPlatform;
	private String reliesOn;
	private String version;
	private String comments;
	private boolean remoteSupport;
	private boolean localSupport;
	@Default
	private Set<OSType> appliesToOS = new HashSet<>();
	@Default
	private Set<String> supersedes = new HashSet<>();

	@Default
	private List<String> sudoCommands = new ArrayList<>();

	private Detection detection;

	@Default
	private List<HardwareMonitor> hardwareMonitors = new ArrayList<>();

	@Default
	private Map<String, TranslationTable> translationTables = new HashMap<>();

	@Default
	private List<EmbeddedFile> embeddedFiles = new ArrayList<>();
}
