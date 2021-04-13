package com.sentrysoftware.matrix.connector.store;

import com.sentrysoftware.matrix.connector.model.Connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;

import static com.sentrysoftware.matrix.utils.Assert.notNull;

public class ConnectorDeserializer {

	private ConnectorDeserializer() {

	}

	public static List<Connector> deserialize() {

		return Collections.emptyList();
	}

	public static Connector deserialize(Path connectorPath) throws IOException, ClassNotFoundException {

		notNull(connectorPath, "connectorPath cannot be null.");

		// Reading the object from a file
		try (
				InputStream inputStream = ConnectorDeserializer.class.getResourceAsStream(connectorPath.toString());
				ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)
		) {
			return (Connector) objectInputStream.readObject();
		}
	}
}
