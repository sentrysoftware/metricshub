package com.sentrysoftware.matrix.connector.serialize;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.sentrysoftware.matrix.connector.model.Connector;

public class ConnectorSerializer {

	private ConnectorSerializer() {

	}

	/**
	 * Under the given path serialize the passed connector
	 * @param path
	 * @param connector
	 * @throws IOException
	 */
	public static void serialize(final String path, final Connector connector) throws IOException {

		try (FileOutputStream fileOutputStream = new FileOutputStream(path + "/" + connector.getCompiledFilename());
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
			objectOutputStream.writeObject(connector);
			objectOutputStream.flush();
		}
	}
}
