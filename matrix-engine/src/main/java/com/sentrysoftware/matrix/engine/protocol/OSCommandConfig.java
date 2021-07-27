package com.sentrysoftware.matrix.engine.protocol;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OSCommandConfig implements IProtocolConfiguration {

	private static final String SUDO = "sudo";

	private boolean useSudo;
	@Default
	private List<String> useSudoCommandList = new ArrayList<>();
	@Default
	private String sudoCommand = SUDO;

	@Default
	private Long timeout = 120L;
}
