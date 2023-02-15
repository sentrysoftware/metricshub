package com.sentrysoftware.matrix.connector.deserializer.source;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.sshstep.GetAvailable;
import com.sentrysoftware.matrix.connector.model.common.sshstep.GetUntilPrompt;
import com.sentrysoftware.matrix.connector.model.common.sshstep.SendPassword;
import com.sentrysoftware.matrix.connector.model.common.sshstep.SendText;
import com.sentrysoftware.matrix.connector.model.common.sshstep.SendUsername;
import com.sentrysoftware.matrix.connector.model.common.sshstep.Sleep;
import com.sentrysoftware.matrix.connector.model.common.sshstep.WaitFor;
import com.sentrysoftware.matrix.connector.model.common.sshstep.WaitForPrompt;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.SshInteractiveSource;

class SshInteractiveSourceDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/source/sshInteractive/";
	}

	@Test
	void testDeserializeSshInterractiveSource() throws IOException {
		final String testResource = "sshInteractiveSource";
		final Connector connector = getConnector(testResource);

		final Map<String, Source> expected = new LinkedHashMap<String, Source>(
			Map.of("testSshInteractiveSource",
				SshInteractiveSource
					.builder()
					.key("$pre.testSshInteractiveSource")
					.type("sshInteractive")
					.port(22)
					.exclude("unknown")
					.keep("drive")
					.beginAtLineNumber(1)
					.endAtLineNumber(null)
					.selectColumns("1,2,3")
					.separators(",")
					.step(new GetAvailable("getAvailable", true, false))
					.step(new GetUntilPrompt("getUntilPrompt", null, false, 10L))
					.step(new SendUsername("sendUsername", null, false))
					.step(new SendPassword("sendPassword", null, false))
					.step(new SendText("sendText", null, false, "getSensors"))
					.step(new Sleep("sleep", null, false, 2L))
					.step(new WaitFor("waitFor", null, false, "result", null))
					.step(new WaitForPrompt("waitForPrompt", false, false, null))
					.forceSerialization(true)
					.build()
			)
		);

		assertEquals(expected, connector.getPre());
	}
}