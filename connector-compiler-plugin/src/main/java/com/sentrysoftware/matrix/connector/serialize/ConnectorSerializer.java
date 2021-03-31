package com.sentrysoftware.matrix.connector.serialize;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import com.sentrysoftware.matrix.connector.model.Connector;

public class ConnectorSerializer {

	
	private ConnectorSerializer() {

	}

	public static void serialize(Connector connector, String path) throws IOException {

		try (FileOutputStream fileOutputStream = new FileOutputStream(path + connector.getCompiledFilename());
				ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)){
			objectOutputStream.writeObject(connector);
			objectOutputStream.flush();
		}
	}
}
