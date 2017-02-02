package org.apache.river.fileserver.client;

import java.rmi.RemoteException;

import org.apache.river.example.fileserver.FileServer;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;

@Component(immediate = true, property = { "osgi.command.function=list", "osgi.command.scope=fileserver" })
public class FileClient {

	private FileServer server;

	public FileServer getServer() {
		return server;
	}

	@Reference(cardinality = ReferenceCardinality.OPTIONAL, unbind = "unsetFileServer")
	public void setServer(FileServer server) {
		this.server = server;
	}

	public void unsetServer(FileServer server) {
		this.server = null;
	}

	public void list(String... args) {
		try {
			if (server != null) {
				String[] listFiles = server.listFiles();
				for (int i = 0; i < listFiles.length; i++) {
					System.out.println(listFiles[i]);
				}

			} else {
				System.out.println("There are no file servers in the network");
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

}
