package com.sentrysoftware.matrix.connector.store;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.List;

import com.sentrysoftware.matrix.connector.model.Connector;

public class ConnectorDeserialize {

	private ConnectorDeserialize() {

	}

	public static List<Connector> deserialize() {

		return Collections.emptyList();
	}

	public static Connector deserialize(File connectorFile) throws IOException, ClassNotFoundException {
		// Reading the object from a file
		try (FileInputStream is = new FileInputStream(connectorFile);
				ObjectInputStream in = new ObjectInputStream(is);) {
			return (Connector) in.readObject();
		}
	}
}
